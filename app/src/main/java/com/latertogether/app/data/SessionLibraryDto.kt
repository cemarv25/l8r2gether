package com.latertogether.app.data

import com.latertogether.domain.model.ClockMode
import com.latertogether.domain.model.Confidence
import com.latertogether.domain.model.ObservationSource
import com.latertogether.domain.model.PlaybackState
import com.latertogether.domain.model.WatchSessionState
import com.latertogether.domain.session.SessionLibraryEntry
import kotlinx.serialization.Serializable

@Serializable
data class SessionLibraryDto(
    val entries: Map<String, StoredEntry> = emptyMap(),
)

@Serializable
data class StoredEntry(
    val contentKey: String,
    val displayTitle: String,
    val sessionId: String,
    val clockMode: String,
    val sessionAnchorWallNanos: Long,
    val baseMediaTimeSec: Double,
    val playbackState: String,
    val pausedAtMediaTimeSec: Double?,
    val playbackRate: Double,
    val driftMs: Double,
    val confidence: String,
    val observationSource: String,
    val lastActivityAtEpochMs: Long,
)

fun StoredEntry.toDomain(): SessionLibraryEntry {
    val session = WatchSessionState(
        contentKey = contentKey,
        sessionId = sessionId,
        clockMode = ClockMode.Monotonic,
        sessionAnchorWallNanos = sessionAnchorWallNanos,
        baseMediaTimeSec = baseMediaTimeSec,
        playbackState = PlaybackState.valueOf(playbackState),
        pausedAtMediaTimeSec = pausedAtMediaTimeSec,
        playbackRate = playbackRate,
        driftMs = driftMs,
        confidence = Confidence.valueOf(confidence),
        observationSource = ObservationSource.valueOf(observationSource),
        lastActivityAtEpochMs = lastActivityAtEpochMs,
    )
    return SessionLibraryEntry(contentKey, displayTitle, session, lastActivityAtEpochMs)
}

fun SessionLibraryEntry.toStored(): StoredEntry = StoredEntry(
    contentKey = contentKey,
    displayTitle = displayTitle,
    sessionId = session.sessionId,
    clockMode = session.clockMode.name,
    sessionAnchorWallNanos = session.sessionAnchorWallNanos,
    baseMediaTimeSec = session.baseMediaTimeSec,
    playbackState = session.playbackState.name,
    pausedAtMediaTimeSec = session.pausedAtMediaTimeSec,
    playbackRate = session.playbackRate,
    driftMs = session.driftMs,
    confidence = session.confidence.name,
    observationSource = session.observationSource.name,
    lastActivityAtEpochMs = lastActivityAtEpochMs,
)
