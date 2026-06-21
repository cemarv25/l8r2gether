package com.l8r2gether.domain.model

import kotlin.math.max

data class WatchSessionState(
    val contentKey: String,
    val sessionId: String,
    val clockMode: ClockMode,
    val sessionAnchorWallNanos: Long,
    val baseMediaTimeSec: Double,
    val playbackState: PlaybackState,
    val pausedAtMediaTimeSec: Double?,
    val playbackRate: Double,
    val driftMs: Double,
    val confidence: Confidence,
    val observationSource: ObservationSource,
    val lastActivityAtEpochMs: Long,
) {
    init {
        require(playbackRate > 0) { "playbackRate must be positive" }
    }

    companion object {
        fun initial(
            contentKey: String,
            sessionId: String,
            nowNanos: Long,
            lastActivityAtEpochMs: Long = System.currentTimeMillis(),
        ): WatchSessionState = WatchSessionState(
            contentKey = contentKey,
            sessionId = sessionId,
            clockMode = ClockMode.Monotonic,
            sessionAnchorWallNanos = nowNanos,
            baseMediaTimeSec = 0.0,
            playbackState = PlaybackState.Paused,
            pausedAtMediaTimeSec = 0.0,
            playbackRate = 1.0,
            driftMs = 0.0,
            confidence = Confidence.Low,
            observationSource = ObservationSource.Manual,
            lastActivityAtEpochMs = lastActivityAtEpochMs,
        )
    }
}

object SessionEstimator {
    fun elapsedWallSeconds(anchorNanos: Long, nowNanos: Long): Double =
        max(0.0, (nowNanos - anchorNanos) / 1_000_000_000.0)

    fun estimateTEst(state: WatchSessionState, nowNanos: Long): Double {
        val driftSec = state.driftMs / 1000.0
        return when (state.playbackState) {
            PlaybackState.Playing -> {
                state.baseMediaTimeSec +
                    state.playbackRate * elapsedWallSeconds(state.sessionAnchorWallNanos, nowNanos) +
                    driftSec
            }
            PlaybackState.Paused -> {
                (state.pausedAtMediaTimeSec ?: state.baseMediaTimeSec) + driftSec
            }
        }
    }
}
