package com.l8r2gether.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.l8r2gether.app.BuildConfig
import com.l8r2gether.app.data.SessionLibraryRepository
import com.l8r2gether.app.ui.home.HomeSampleSessions
import com.l8r2gether.domain.model.PlaybackState
import com.l8r2gether.domain.model.WatchSessionState
import com.l8r2gether.domain.session.SessionLibraryEntry
import com.l8r2gether.domain.session.pauseSubtitle
import com.l8r2gether.domain.session.prettifyContentKey
import com.l8r2gether.domain.time.MediaTimeParseResult
import com.l8r2gether.domain.time.MediaTimeParser
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class SessionListItemUi(
    val contentKey: String,
    val displayTitle: String,
    val pauseSubtitle: String,
    val isMostRecent: Boolean = false,
)

data class HomeUiState(
    val sessions: List<SessionListItemUi> = emptyList(),
    val isEmpty: Boolean = true,
    val showNewSessionSheet: Boolean = false,
    val newSessionContentKey: String = "",
    val syncTargetContentKey: String? = null,
    val syncTimeInput: String = "",
    val errorMessage: String? = null,
)

sealed interface HomeEvent {
    data class NavigateToWatch(val contentKey: String) : HomeEvent
}

class HomeViewModel(
    private val repository: SessionLibraryRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<HomeEvent>()
    val events: SharedFlow<HomeEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            repository.sessions.collect { entries ->
                val sessions = resolveSessions(entries)
                _uiState.update { state ->
                    state.copy(
                        sessions = sessions,
                        isEmpty = sessions.isEmpty(),
                    )
                }
            }
        }
    }

    fun onStartNewSessionClick() {
        _uiState.update { it.copy(showNewSessionSheet = true, newSessionContentKey = "", errorMessage = null) }
    }

    fun onDismissNewSessionSheet() {
        _uiState.update { it.copy(showNewSessionSheet = false, errorMessage = null) }
    }

    fun onNewSessionContentKeyChange(value: String) {
        _uiState.update { it.copy(newSessionContentKey = value, errorMessage = null) }
    }

    fun onConfirmNewSession() {
        val key = _uiState.value.newSessionContentKey.trim()
        if (key.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Enter a content key") }
            return
        }
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val session = WatchSessionState.initial(
                contentKey = key,
                sessionId = UUID.randomUUID().toString(),
                nowNanos = System.nanoTime(),
                lastActivityAtEpochMs = now,
            )
            repository.upsert(
                SessionLibraryEntry(
                    contentKey = key,
                    displayTitle = prettifyContentKey(key),
                    session = session,
                    lastActivityAtEpochMs = now,
                ),
            )
            _uiState.update { it.copy(showNewSessionSheet = false, errorMessage = null) }
            _events.emit(HomeEvent.NavigateToWatch(key))
        }
    }

    fun onResumeSession(contentKey: String) {
        viewModelScope.launch {
            _events.emit(HomeEvent.NavigateToWatch(contentKey))
        }
    }

    fun onOpenSyncNow(contentKey: String) {
        _uiState.update {
            it.copy(
                syncTargetContentKey = contentKey,
                syncTimeInput = "",
                errorMessage = null,
            )
        }
    }

    fun onDismissSyncNow() {
        _uiState.update { it.copy(syncTargetContentKey = null, syncTimeInput = "", errorMessage = null) }
    }

    fun onSyncTimeInputChange(value: String) {
        _uiState.update { it.copy(syncTimeInput = value, errorMessage = null) }
    }

    fun onNudgeSyncTime(deltaSec: Double) {
        val current = _uiState.value.syncTimeInput
        val base = when (val parsed = MediaTimeParser.parse(current)) {
            is MediaTimeParseResult.Ok -> parsed.seconds
            else -> 0.0
        }
        _uiState.update {
            it.copy(syncTimeInput = MediaTimeParser.formatMmSs(MediaTimeParser.adjustSeconds(base, deltaSec)))
        }
    }

    fun onApplySyncNow() {
        val contentKey = _uiState.value.syncTargetContentKey ?: return
        when (val parsed = MediaTimeParser.parse(_uiState.value.syncTimeInput)) {
            is MediaTimeParseResult.Invalid -> {
                _uiState.update { it.copy(errorMessage = parsed.reason) }
                return
            }
            is MediaTimeParseResult.Ok -> {
                viewModelScope.launch {
                    val existing = repository.sessions.first()
                        .find { it.contentKey == contentKey } ?: return@launch
                    val nowNanos = System.nanoTime()
                    val nowMs = System.currentTimeMillis()
                    val media = parsed.seconds
                    val updatedSession = existing.session.copy(
                        playbackState = PlaybackState.Paused,
                        pausedAtMediaTimeSec = media,
                        sessionAnchorWallNanos = nowNanos,
                        baseMediaTimeSec = media,
                        lastActivityAtEpochMs = nowMs,
                    )
                    repository.upsert(
                        existing.copy(
                            session = updatedSession,
                            lastActivityAtEpochMs = nowMs,
                        ),
                    )
                    _uiState.update {
                        it.copy(syncTargetContentKey = null, syncTimeInput = "", errorMessage = null)
                    }
                }
            }
        }
    }

    fun onBrowseLibraryClick() {
        _uiState.update { it.copy(errorMessage = "Shared library coming soon") }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun resolveSessions(entries: List<SessionLibraryEntry>): List<SessionListItemUi> {
        if (entries.isNotEmpty()) {
            return entries.mapIndexed { index, entry -> entry.toListItem(isMostRecent = index == 0) }
        }
        return if (BuildConfig.DEBUG) HomeSampleSessions.items else emptyList()
    }

    private fun SessionLibraryEntry.toListItem(isMostRecent: Boolean): SessionListItemUi {
        return SessionListItemUi(
            contentKey = contentKey,
            displayTitle = displayTitle,
            pauseSubtitle = pauseSubtitle(this),
            isMostRecent = isMostRecent,
        )
    }
}

class HomeViewModelFactory(
    private val repository: SessionLibraryRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}
