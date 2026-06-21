package com.l8r2gether.app.data

import com.l8r2gether.domain.model.ClockMode
import com.l8r2gether.domain.model.Confidence
import com.l8r2gether.domain.model.ObservationSource
import com.l8r2gether.domain.model.PlaybackState
import com.l8r2gether.domain.model.WatchSessionState
import com.l8r2gether.domain.session.SessionLibraryEntry
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
