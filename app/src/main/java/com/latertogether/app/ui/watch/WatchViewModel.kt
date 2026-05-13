package com.latertogether.app.ui.watch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.latertogether.app.playback.PlaybackObservationBus
import com.latertogether.domain.chat.MessageRevealLedger
import com.latertogether.domain.confidence.ConfidenceEvaluator
import com.latertogether.domain.fusion.FusionPolicy
import com.latertogether.domain.fusion.FusionResult
import com.latertogether.domain.model.MediaTimeCheckpoint
import com.latertogether.domain.model.Message
import com.latertogether.domain.model.ObservationSource
import com.latertogether.domain.model.SessionEstimator
import com.latertogether.domain.model.WatchSessionState
import com.latertogether.domain.repository.MessageRepository
import com.latertogether.domain.repository.NewMessage
import com.latertogether.domain.time.MediaTimeParseResult
import com.latertogether.domain.time.MediaTimeParser
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.UUID

data class WatchUiState(
    val session: WatchSessionState,
    val thread: List<Message>,
    val visibleMessages: List<Message>,
    val newlyRevealed: List<Message>,
    val tEstSec: Double,
    val syncTimeInput: String,
    val chatInput: String,
    val loadingThread: Boolean,
    val threadError: String?,
    val syncInputError: String?,
    val sendError: String?,
    val showConfidencePrompt: Boolean,
)

class WatchViewModel(
    private val repository: MessageRepository,
    private val contentKey: String,
    /** Must match signed-in Supabase user when using [MessageRepositoryImpl]. */
    private val authorId: String = "local-dev",
) : ViewModel() {

    private val ledger = MessageRevealLedger()

    private val _state = MutableStateFlow(buildInitialState())
    val state: StateFlow<WatchUiState> = _state.asStateFlow()

    private var ticker: Job? = null

    init {
        observePlayback()
        startTicker()
        refreshMessages()
    }

    private fun buildInitialState(): WatchUiState {
        val now = System.nanoTime()
        val session = WatchSessionState.initial(
            contentKey = contentKey,
            sessionId = UUID.randomUUID().toString(),
            nowNanos = now,
        )
        return evaluateLedger(
            WatchUiState(
                session = session,
                thread = emptyList(),
                visibleMessages = emptyList(),
                newlyRevealed = emptyList(),
                tEstSec = SessionEstimator.estimateMediaTime(session, now),
                syncTimeInput = "",
                chatInput = "",
                loadingThread = false,
                threadError = null,
                syncInputError = null,
                sendError = null,
                showConfidencePrompt = ConfidenceEvaluator.shouldPrompt(session, now),
            ),
        )
    }

    private fun observePlayback() {
        PlaybackObservationBus.observations
            .onEach { observation ->
                mutateSession { current ->
                    when (val r = FusionPolicy.applyObservation(current, observation)) {
                        is FusionResult.Updated -> r.state
                        is FusionResult.Unchanged -> current
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun startTicker() {
        ticker?.cancel()
        ticker = viewModelScope.launch {
            while (isActive) {
                delay(TICK_MS)
                tick()
            }
        }
    }

    private fun tick() {
        val now = System.nanoTime()
        mutateSession { ConfidenceEvaluator.decayForStaleness(it, now) }
    }

    private fun mutateSession(transform: (WatchSessionState) -> WatchSessionState) {
        val session = transform(_state.value.session)
        val now = System.nanoTime()
        val tEst = SessionEstimator.estimateMediaTime(session, now)
        val prompt = ConfidenceEvaluator.shouldPrompt(session, now)
        val next = _state.value.copy(
            session = session,
            tEstSec = tEst,
            showConfidencePrompt = prompt,
        )
        _state.value = evaluateLedger(next)
    }

    private fun evaluateLedger(base: WatchUiState): WatchUiState {
        val update = ledger.evaluate(base.thread, base.tEstSec)
        return base.copy(
            visibleMessages = update.visibleMessages,
            newlyRevealed = update.newlyRevealed,
        )
    }

    fun setSyncInput(value: String) {
        _state.value = _state.value.copy(syncTimeInput = value)
    }

    fun setChatInput(value: String) {
        _state.value = _state.value.copy(chatInput = value)
    }

    fun applyManualSync() {
        when (val parsed = MediaTimeParser.parse(_state.value.syncTimeInput)) {
            is MediaTimeParseResult.Invalid -> {
                _state.value = _state.value.copy(syncInputError = parsed.reason)
            }
            is MediaTimeParseResult.Ok -> {
                val cp = MediaTimeCheckpoint(
                    wallNanos = System.nanoTime(),
                    mediaTimeSec = parsed.seconds,
                    source = ObservationSource.Manual,
                )
                mutateSession { FusionPolicy.applyCheckpoint(it, cp) }
                _state.value = _state.value.copy(syncTimeInput = "", syncInputError = null)
            }
        }
    }

    fun togglePlayback(toPlaying: Boolean) {
        val now = System.nanoTime()
        mutateSession {
            if (toPlaying) FusionPolicy.userPlay(it, now) else FusionPolicy.userPause(it, now)
        }
    }

    fun dismissConfidencePrompt() {
        mutateSession { ConfidenceEvaluator.onDismissSyncPrompt(it) }
    }

    fun acknowledgeStillSynced() {
        mutateSession { ConfidenceEvaluator.onStillSyncedAck(it, System.nanoTime()) }
    }

    fun refreshMessages() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loadingThread = true, threadError = null)
            repository.fetchMessages(contentKey).fold(
                onSuccess = { list ->
                    ledger.reset()
                    val sorted = list.sortedBy { it.mediaTimestamp }
                    val base = _state.value.copy(thread = sorted, loadingThread = false)
                    _state.value = evaluateLedger(base)
                },
                onFailure = { e ->
                    _state.value = _state.value.copy(loadingThread = false, threadError = e.message)
                },
            )
        }
    }

    fun sendChat() {
        val body = _state.value.chatInput.trim()
        if (body.isEmpty()) return
        viewModelScope.launch {
            val now = System.nanoTime()
            val mediaTs = SessionEstimator.estimateMediaTime(_state.value.session, now)
            val payload = NewMessage(
                contentKey = contentKey,
                mediaTimestamp = mediaTs,
                body = body,
                authorId = authorId,
                clientCreatedAt = Instant.now().toString(),
            )
            repository.insertMessage(payload).fold(
                onSuccess = {
                    _state.value = _state.value.copy(chatInput = "", sendError = null)
                    refreshMessages()
                },
                onFailure = { e ->
                    _state.value = _state.value.copy(sendError = e.message)
                },
            )
        }
    }

    override fun onCleared() {
        ticker?.cancel()
        super.onCleared()
    }

    companion object {
        private const val TICK_MS = 400L
    }
}

class WatchViewModelFactory(
    private val repository: MessageRepository,
    private val contentKey: String,
    private val authorId: String = "local-dev",
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WatchViewModel::class.java)) {
            return WatchViewModel(repository, contentKey, authorId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
