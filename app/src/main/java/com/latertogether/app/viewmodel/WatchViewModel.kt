package com.latertogether.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.latertogether.domain.chat.MessageRevealLedger
import com.latertogether.domain.confidence.ConfidenceEvaluator
import com.latertogether.domain.fusion.FusionPolicy
import com.latertogether.domain.fusion.FusionResult
import com.latertogether.domain.model.Message
import com.latertogether.domain.model.MediaTimeCheckpoint
import com.latertogether.domain.model.ObservationSource
import com.latertogether.domain.model.PlaybackObservation
import com.latertogether.domain.model.PlaybackState
import com.latertogether.domain.model.SessionEstimator
import com.latertogether.domain.model.WatchSessionState
import com.latertogether.domain.repository.MessageRepository
import com.latertogether.domain.repository.NewMessage
import com.latertogether.domain.time.MediaTimeParseResult
import com.latertogether.domain.time.MediaTimeParser
import com.latertogether.app.playback.MediaObservationAdapter
import com.latertogether.app.playback.PlaybackObservationBus
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.UUID

data class WatchUiState(
    val contentKey: String = "",
    val sessionActive: Boolean = false,
    val session: WatchSessionState? = null,
    val tEstSec: Double = 0.0,
    val playbackExplicit: PlaybackState = PlaybackState.Paused,
    val messages: List<Message> = emptyList(),
    val visibleMessages: List<Message> = emptyList(),
    val composerText: String = "",
    val syncTimeInput: String = "",
    val statusLine: String = "",
    val confidencePrompt: Boolean = false,
    val authorId: String = "local-demo-user",
    val accessibilityFallbackEnabled: Boolean = false,
)

class WatchViewModel(
    application: Application,
    private val repository: MessageRepository,
    private val supabaseClient: SupabaseClient?,
    private val prefs: android.content.SharedPreferences,
) : AndroidViewModel(application) {

    private val ledger = MessageRevealLedger()
    private val mediaAdapter = MediaObservationAdapter(application.applicationContext)

    private val _ui = MutableStateFlow(WatchUiState())
    val uiState: StateFlow<WatchUiState> = _ui.asStateFlow()

    private var ticker: Job? = null
    private var observationJobs: Job? = null

    init {
        refreshAuthorId()
        _ui.value = _ui.value.copy(
            accessibilityFallbackEnabled = prefs.getBoolean(PREF_A11Y, false),
        )
    }

    fun refreshAuthorId() {
        val id = supabaseClient?.auth?.currentUserOrNull()?.id
            ?: "local-demo-user"
        _ui.value = _ui.value.copy(authorId = id)
    }

    fun setContentKey(key: String) {
        _ui.value = _ui.value.copy(contentKey = key.trim())
    }

    fun setComposerText(text: String) {
        _ui.value = _ui.value.copy(composerText = text)
    }

    fun setSyncInput(text: String) {
        _ui.value = _ui.value.copy(syncTimeInput = text)
    }

    fun setPlaybackRate(rate: Double) {
        val s0 = currentSession() ?: return
        val clamped = rate.coerceIn(0.25, 4.0)
        val next = s0.copy(playbackRate = clamped)
        _ui.value = _ui.value.copy(session = next)
        updateDerivedTimes()
        refreshVisible(next)
    }

    fun setAccessibilityFallback(enabled: Boolean) {
        prefs.edit().putBoolean(PREF_A11Y, enabled).apply()
        _ui.value = _ui.value.copy(accessibilityFallbackEnabled = enabled)
    }

    fun startWatchSession() {
        val key = _ui.value.contentKey
        if (key.isBlank()) {
            _ui.value = _ui.value.copy(statusLine = "Enter a content key first.")
            return
        }
        val now = System.nanoTime()
        val session = WatchSessionState.initial(key, UUID.randomUUID().toString(), now)
        ledger.reset()
        _ui.value = _ui.value.copy(
            sessionActive = true,
            session = session,
            playbackExplicit = PlaybackState.Paused,
            statusLine = "Session started — calibrate with Sync now if needed.",
            confidencePrompt = false,
        )
        startLoops()
        refreshMessages()
    }

    fun stopWatchSession() {
        ticker?.cancel()
        observationJobs?.cancel()
        ticker = null
        observationJobs = null
        ledger.reset()
        _ui.value = _ui.value.copy(sessionActive = false, session = null, visibleMessages = emptyList())
    }

    private fun startLoops() {
        ticker?.cancel()
        observationJobs?.cancel()
        ticker = viewModelScope.launch {
            while (isActive) {
                delay(300)
                tick()
            }
        }
        observationJobs = viewModelScope.launch {
            launch {
                mediaAdapter.observeTransport().collectLatest { applyObservation(it) }
            }
            launch {
                PlaybackObservationBus.observations.collectLatest {
                    if (_ui.value.accessibilityFallbackEnabled) {
                        applyObservation(it)
                    }
                }
            }
        }
    }

    private fun currentSession(): WatchSessionState? = _ui.value.session

    private fun applyObservation(observation: PlaybackObservation) {
        val s0 = currentSession() ?: return
        when (val result = FusionPolicy.applyObservation(s0, observation)) {
            is FusionResult.Updated -> {
                val aged = ConfidenceEvaluator.decayForStaleness(result.state, System.nanoTime())
                _ui.value = _ui.value.copy(session = aged)
                maybePrompt(aged)
            }
            is FusionResult.Unchanged -> Unit
        }
        updateDerivedTimes()
    }

    private fun tick() {
        val s0 = currentSession() ?: return
        val now = System.nanoTime()
        val decayed = ConfidenceEvaluator.decayForStaleness(s0, now)
        if (decayed != s0) {
            _ui.value = _ui.value.copy(session = decayed)
            maybePrompt(decayed)
        }
        updateDerivedTimes()
        refreshVisible(decayed)
    }

    private fun maybePrompt(session: WatchSessionState) {
        val show = ConfidenceEvaluator.shouldPrompt(session, System.nanoTime())
        _ui.value = _ui.value.copy(confidencePrompt = show)
    }

    private fun updateDerivedTimes() {
        val s = currentSession() ?: return
        val now = System.nanoTime()
        val tEst = SessionEstimator.estimateMediaTime(s, now)
        _ui.value = _ui.value.copy(
            tEstSec = tEst,
            playbackExplicit = s.playbackState,
        )
    }

    private fun refreshVisible(session: WatchSessionState) {
        val now = System.nanoTime()
        val tEst = SessionEstimator.estimateMediaTime(session, now)
        val update = ledger.evaluate(_ui.value.messages, tEst)
        _ui.value = _ui.value.copy(visibleMessages = update.visibleMessages)
    }

    fun userSetPlayingExplicit(state: PlaybackState) {
        val s0 = currentSession() ?: return
        val now = System.nanoTime()
        val next = when (state) {
            PlaybackState.Paused -> FusionPolicy.userPause(s0, now)
            PlaybackState.Playing -> FusionPolicy.userPlay(s0, now)
        }
        _ui.value = _ui.value.copy(session = next, playbackExplicit = state)
        updateDerivedTimes()
        refreshVisible(next)
    }

    fun manualSyncNow() {
        val s0 = currentSession() ?: return
        val raw = _ui.value.syncTimeInput.trim()
        if (raw.isEmpty()) {
            _ui.value = _ui.value.copy(statusLine = "Enter current media time (mm:ss or seconds).")
            return
        }
        when (val parsed = MediaTimeParser.parse(raw)) {
            is MediaTimeParseResult.Invalid -> {
                _ui.value = _ui.value.copy(statusLine = parsed.reason)
            }
            is MediaTimeParseResult.Ok -> {
                val cp = MediaTimeCheckpoint(
                    wallNanos = System.nanoTime(),
                    mediaTimeSec = parsed.seconds,
                    source = ObservationSource.Manual,
                )
                val merged = FusionPolicy.applyCheckpoint(s0, cp)
                _ui.value = _ui.value.copy(
                    session = merged,
                    statusLine = "Anchored at ${parsed.seconds}s",
                    syncTimeInput = "",
                    confidencePrompt = false,
                )
                updateDerivedTimes()
                refreshVisible(merged)
            }
        }
    }

    fun dismissConfidencePrompt() {
        val s0 = currentSession() ?: return
        _ui.value = _ui.value.copy(
            session = ConfidenceEvaluator.onDismissSyncPrompt(s0),
            confidencePrompt = false,
        )
    }

    fun acknowledgeStillSynced() {
        val s0 = currentSession() ?: return
        _ui.value = _ui.value.copy(
            session = ConfidenceEvaluator.onStillSyncedAck(s0, System.nanoTime()),
            confidencePrompt = false,
        )
    }

    fun refreshMessages() {
        val key = _ui.value.contentKey
        if (key.isBlank()) return
        viewModelScope.launch {
            val result = repository.fetchMessages(contentKey = key)
            result.onSuccess { list ->
                _ui.value = _ui.value.copy(messages = list.sortedBy { it.mediaTimestamp })
                val s = currentSession()
                if (s != null) refreshVisible(s)
            }.onFailure {
                _ui.value = _ui.value.copy(statusLine = it.message ?: "Fetch failed")
            }
        }
    }

    fun sendCurrentMessage() {
        val key = _ui.value.contentKey
        val body = _ui.value.composerText.trim()
        val s = currentSession()
        if (key.isBlank() || body.isEmpty() || s == null) return
        val nowIso = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
        val tEst = SessionEstimator.estimateMediaTime(s, System.nanoTime())
        viewModelScope.launch {
            val msg = NewMessage(
                contentKey = key,
                mediaTimestamp = tEst,
                body = body,
                authorId = _ui.value.authorId,
                clientCreatedAt = nowIso,
            )
            repository.insertMessage(msg)
                .onSuccess {
                    _ui.value = _ui.value.copy(composerText = "", statusLine = "Sent.")
                    refreshMessages()
                }
                .onFailure {
                    _ui.value = _ui.value.copy(statusLine = it.message ?: "Send failed")
                }
        }
    }

    fun signIn(email: String, password: String) {
        val client = supabaseClient ?: return
        viewModelScope.launch {
            runCatching {
                client.auth.signInWith(Email) {
                    this.email = email
                    this.password = password
                }
            }.onSuccess {
                refreshAuthorId()
                _ui.value = _ui.value.copy(statusLine = "Signed in.")
            }.onFailure {
                _ui.value = _ui.value.copy(statusLine = it.message ?: "Sign-in failed")
            }
        }
    }

    companion object {
        private const val PREF_A11Y = "accessibility_fallback"
    }
}
