package com.latertogether.domain.model

import kotlin.math.max

/**
 * Persistable session fields ([§6.1](latertogether-companion-sync-spec.md)).
 * Wall/anchor times use the same nanosecond clock as [PlaybackObservation].
 */
data class WatchSessionState(
    val contentKey: String,
    val sessionId: String,
    val clockMode: ClockMode,
    val sessionAnchorWallNanos: Long,
    val baseMediaTimeSec: Double,
    val playbackState: PlaybackState,
    val pausedAtMediaTimeSec: Double?,
    val playbackRate: Double,
    /** Small additive correction from checkpoints; seconds contribution = driftMs / 1000. */
    val driftMs: Double,
    val confidence: Confidence,
    val observationSource: ObservationSource,
    val lastObservationWallNanos: Long?,
    val dismissedPromptCount: Int,
    val minutesSinceSolidObservation: Double,
) {
    init {
        require(playbackRate > 0) { "playbackRate must be positive" }
    }

    companion object {
        fun initial(
            contentKey: String,
            sessionId: String,
            nowNanos: Long,
            clockMode: ClockMode = ClockMode.Monotonic,
        ): WatchSessionState = WatchSessionState(
            contentKey = contentKey,
            sessionId = sessionId,
            clockMode = clockMode,
            sessionAnchorWallNanos = nowNanos,
            baseMediaTimeSec = 0.0,
            playbackState = PlaybackState.Paused,
            pausedAtMediaTimeSec = 0.0,
            playbackRate = 1.0,
            driftMs = 0.0,
            confidence = Confidence.Low,
            observationSource = ObservationSource.Manual,
            lastObservationWallNanos = null,
            dismissedPromptCount = 0,
            minutesSinceSolidObservation = 0.0,
        )
    }
}

object SessionEstimator {
    fun elapsedWallSeconds(anchorNanos: Long, nowNanos: Long): Double =
        max(0.0, (nowNanos - anchorNanos) / 1_000_000_000.0)

    /**
     * [§6.2](latertogether-companion-sync-spec.md): extrapolated media time when not integrating a fresh observation.
     */
    fun estimateMediaTime(state: WatchSessionState, nowNanos: Long): Double {
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

    /** Spec alias: estimated media time **t_est** ([§6.2](latertogether-companion-sync-spec.md)). */
    fun estimateTEst(state: WatchSessionState, nowNanos: Long): Double =
        estimateMediaTime(state, nowNanos)
}
