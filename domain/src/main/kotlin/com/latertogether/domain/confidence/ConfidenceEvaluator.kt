package com.latertogether.domain.confidence

import com.latertogether.domain.model.Confidence
import com.latertogether.domain.model.WatchSessionState

/**
 * [§9](latertogether-companion-sync-spec.md) confidence transitions and prompt eligibility.
 */
object ConfidenceEvaluator {
    fun minutesSinceObservation(state: WatchSessionState, nowNanos: Long): Double {
        val last = state.lastObservationWallNanos ?: return state.minutesSinceSolidObservation
        return (nowNanos - last) / 1_000_000_000.0 / 60.0
    }

    /** Prompt when not high confidence or stale observation ([§9.1](latertogether-companion-sync-spec.md)). */
    fun shouldPrompt(
        state: WatchSessionState,
        nowNanos: Long,
        staleMinutes: Double = 5.0,
    ): Boolean {
        val stale = minutesSinceObservation(state, nowNanos) >= staleMinutes
        val weakConfidence = state.confidence != Confidence.High
        return weakConfidence || stale
    }

    fun onDismissSyncPrompt(state: WatchSessionState): WatchSessionState =
        state.copy(
            dismissedPromptCount = state.dismissedPromptCount + 1,
            confidence = Confidence.Low,
        )

    fun onStillSyncedAck(state: WatchSessionState, nowNanos: Long): WatchSessionState =
        state.copy(
            confidence = Confidence.Medium,
            lastObservationWallNanos = nowNanos,
            minutesSinceSolidObservation = 0.0,
        )

    /** Example policy table [§9.2]: weaken confidence over long gaps without observation. */
    fun decayForStaleness(state: WatchSessionState, nowNanos: Long): WatchSessionState {
        val minutes = minutesSinceObservation(state, nowNanos)
        return when {
            minutes > 15 && state.confidence == Confidence.High ->
                state.copy(confidence = Confidence.Medium, minutesSinceSolidObservation = minutes)
            minutes > 30 && state.confidence == Confidence.Medium ->
                state.copy(confidence = Confidence.Low, minutesSinceSolidObservation = minutes)
            else -> state.copy(minutesSinceSolidObservation = minutes)
        }
    }
}
