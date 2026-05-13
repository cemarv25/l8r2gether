package com.latertogether.domain.fusion

import com.latertogether.domain.model.Confidence
import com.latertogether.domain.model.MediaTimeCheckpoint
import com.latertogether.domain.model.ObservationSource
import com.latertogether.domain.model.PlaybackObservation
import com.latertogether.domain.model.PlaybackState
import com.latertogether.domain.model.WatchSessionState
import com.latertogether.domain.model.SessionEstimator
import kotlin.math.abs

sealed class FusionResult {
    data class Updated(
        val state: WatchSessionState,
        val action: FusionAction,
    ) : FusionResult()

    data class Unchanged(
        val state: WatchSessionState,
    ) : FusionResult()
}

enum class FusionAction {
    SnappedToObservation,
    SeekSuspected,
    StateOnlyApplied,
    MidBandHold,
}

object FusionPolicy {
    fun applyObservation(
        state: WatchSessionState,
        observation: PlaybackObservation,
        config: FusionConfig = FusionConfig(),
    ): FusionResult {
        return when (observation) {
            is PlaybackObservation.Position -> {
                val extrapolated = SessionEstimator.estimateMediaTime(state, observation.observedAtWallNanos)
                val delta = abs(observation.mediaTimeSec - extrapolated)
                var newState = state.copy(
                    lastObservationWallNanos = observation.observedAtWallNanos,
                    minutesSinceSolidObservation = 0.0,
                    observationSource = mergeSource(state.observationSource, observation.source),
                )
                when {
                    delta <= config.snapThresholdSec -> {
                        newState = reanchorAtObservation(
                            newState,
                            observation.mediaTimeSec,
                            observation.observedAtWallNanos,
                            observation.playbackStateHint,
                        )
                        if (newState.confidence != Confidence.High) {
                            newState = newState.copy(
                                confidence = bumpConfidenceForObservation(newState, observation.source),
                            )
                        }
                        FusionResult.Updated(
                            newState,
                            FusionAction.SnappedToObservation,
                        )
                    }
                    delta >= config.seekSuspectedThresholdSec -> {
                        newState = newState.copy(
                            confidence = downgradeForSeek(newState),
                        )
                        FusionResult.Updated(
                            newState,
                            FusionAction.SeekSuspected,
                        )
                    }
                    else -> {
                        // Mid band: do not fight extrapolation; light confidence nudge
                        newState = newState.copy(
                            confidence = nudgeDownIfWeak(newState),
                        )
                        FusionResult.Updated(
                            newState,
                            FusionAction.MidBandHold,
                        )
                    }
                }
            }
            is PlaybackObservation.StateOnly,
            is PlaybackObservation.Transport,
            -> {
                val ps = when (observation) {
                    is PlaybackObservation.StateOnly -> observation.playbackState
                    is PlaybackObservation.Transport -> observation.playbackState
                    else -> error("unreachable")
                }
                val source = observation.source
                var newState = state.copy(
                    lastObservationWallNanos = observation.observedAtWallNanos,
                    observationSource = mergeSource(state.observationSource, source),
                )
                newState = applyTransportState(
                    newState,
                    ps,
                    observation.observedAtWallNanos,
                )
                FusionResult.Updated(
                    newState,
                    FusionAction.StateOnlyApplied,
                )
            }
        }
    }

    fun applyCheckpoint(
        state: WatchSessionState,
        checkpoint: MediaTimeCheckpoint,
    ): WatchSessionState {
        return state.copy(
            sessionAnchorWallNanos = checkpoint.wallNanos,
            baseMediaTimeSec = checkpoint.mediaTimeSec,
            playbackState = PlaybackState.Playing,
            pausedAtMediaTimeSec = null,
            driftMs = 0.0,
            confidence = Confidence.High,
            observationSource = checkpoint.source,
            lastObservationWallNanos = checkpoint.wallNanos,
            minutesSinceSolidObservation = 0.0,
        )
    }

    /** [§7.2](latertogether-companion-sync-spec.md) explicit pause in app. */
    fun userPause(state: WatchSessionState, nowNanos: Long): WatchSessionState {
        val t = SessionEstimator.estimateMediaTime(state, nowNanos)
        return state.copy(
            playbackState = PlaybackState.Paused,
            pausedAtMediaTimeSec = t,
            sessionAnchorWallNanos = nowNanos,
            baseMediaTimeSec = t,
        )
    }

    /** [§7.2](latertogether-companion-sync-spec.md) explicit play in app. */
    fun userPlay(state: WatchSessionState, nowNanos: Long): WatchSessionState {
        val now = nowNanos
        val media = state.pausedAtMediaTimeSec ?: state.baseMediaTimeSec
        return state.copy(
            playbackState = PlaybackState.Playing,
            pausedAtMediaTimeSec = null,
            sessionAnchorWallNanos = now,
            baseMediaTimeSec = media,
        )
    }

    private fun reanchorAtObservation(
        state: WatchSessionState,
        mediaSec: Double,
        wallNanos: Long,
        playbackHint: PlaybackState?,
    ): WatchSessionState {
        val nextPlayback = playbackHint ?: state.playbackState
        return when (nextPlayback) {
            PlaybackState.Playing -> state.copy(
                sessionAnchorWallNanos = wallNanos,
                baseMediaTimeSec = mediaSec,
                playbackState = PlaybackState.Playing,
                pausedAtMediaTimeSec = null,
                driftMs = 0.0,
            )
            PlaybackState.Paused -> state.copy(
                sessionAnchorWallNanos = wallNanos,
                baseMediaTimeSec = mediaSec,
                playbackState = PlaybackState.Paused,
                pausedAtMediaTimeSec = mediaSec,
                driftMs = 0.0,
            )
        }
    }

    private fun applyTransportState(
        state: WatchSessionState,
        playback: PlaybackState,
        wallNanos: Long,
    ): WatchSessionState {
        return when (playback) {
            PlaybackState.Paused -> {
                val t = SessionEstimator.estimateMediaTime(state, wallNanos)
                state.copy(
                    playbackState = PlaybackState.Paused,
                    pausedAtMediaTimeSec = t,
                    sessionAnchorWallNanos = wallNanos,
                    baseMediaTimeSec = t,
                )
            }
            PlaybackState.Playing -> {
                val media = state.pausedAtMediaTimeSec ?: SessionEstimator.estimateMediaTime(state, wallNanos)
                state.copy(
                    playbackState = PlaybackState.Playing,
                    pausedAtMediaTimeSec = null,
                    sessionAnchorWallNanos = wallNanos,
                    baseMediaTimeSec = media,
                )
            }
        }
    }

    private fun mergeSource(a: ObservationSource, b: ObservationSource): ObservationSource {
        if (a == b) return a
        return ObservationSource.Mixed
    }

    private fun bumpConfidenceForObservation(
        state: WatchSessionState,
        source: ObservationSource,
    ): Confidence {
        return when (source) {
            ObservationSource.MediaSession -> Confidence.High
            ObservationSource.Accessibility -> Confidence.Medium
            ObservationSource.Manual -> state.confidence
            ObservationSource.Mixed -> Confidence.Medium
        }
    }

    private fun downgradeForSeek(state: WatchSessionState): Confidence =
        when (state.confidence) {
            Confidence.High -> Confidence.Medium
            Confidence.Medium, Confidence.Low -> Confidence.Low
        }

    private fun nudgeDownIfWeak(state: WatchSessionState): Confidence =
        when (state.confidence) {
            Confidence.High -> Confidence.Medium
            else -> state.confidence
        }
}
