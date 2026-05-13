package com.latertogether.domain

import com.google.common.truth.Truth.assertThat
import com.latertogether.domain.fusion.FusionAction
import com.latertogether.domain.fusion.FusionConfig
import com.latertogether.domain.fusion.FusionPolicy
import com.latertogether.domain.fusion.FusionResult
import com.latertogether.domain.model.Confidence
import com.latertogether.domain.model.ObservationSource
import com.latertogether.domain.model.PlaybackObservation
import com.latertogether.domain.model.PlaybackState
import com.latertogether.domain.model.WatchSessionState
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class FusionPolicyTableTest(
    private val caseName: String,
    private val base: WatchSessionState,
    private val observation: PlaybackObservation,
    private val expectedAction: FusionAction,
    private val expectedConfidence: Confidence?,
) {
    @Test
    fun fusion_cases() {
        val config = FusionConfig(snapThresholdSec = 2.0, seekSuspectedThresholdSec = 12.0)
        val result = FusionPolicy.applyObservation(base, observation, config) as FusionResult.Updated
        assertThat(result.action).isEqualTo(expectedAction)
        expectedConfidence?.let { assertThat(result.state.confidence).isEqualTo(it) }
    }

    companion object {
        private val t0 = 10_000_000_000L

        private fun playingAt(media: Double) =
            WatchSessionState.initial("k", "s", t0).copy(
                playbackState = PlaybackState.Playing,
                sessionAnchorWallNanos = t0,
                baseMediaTimeSec = media,
                confidence = Confidence.High,
                lastObservationWallNanos = t0,
            )

        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun data(): Collection<Array<Any?>> {
            val snapObs = PlaybackObservation.Position(
                mediaTimeSec = 100.0,
                observedAtWallNanos = t0 + 2_000_000_000L,
                source = ObservationSource.MediaSession,
                playbackStateHint = PlaybackState.Playing,
            )
            val basePlaying100 = playingAt(100.0)
            return listOf(
                arrayOf(
                    "snap_near_extrapolation",
                    basePlaying100,
                    snapObs,
                    FusionAction.SnappedToObservation,
                    Confidence.High,
                ),
                arrayOf(
                    "seek_suspected_far",
                    basePlaying100,
                    PlaybackObservation.Position(
                        mediaTimeSec = 200.0,
                        observedAtWallNanos = t0 + 2_000_000_000L,
                        source = ObservationSource.MediaSession,
                    ),
                    FusionAction.SeekSuspected,
                    Confidence.Medium,
                ),
                arrayOf(
                    "mid_band_hold",
                    basePlaying100,
                    PlaybackObservation.Position(
                        mediaTimeSec = 111.0,
                        observedAtWallNanos = t0 + 2_000_000_000L,
                        source = ObservationSource.MediaSession,
                    ),
                    FusionAction.MidBandHold,
                    Confidence.Medium,
                ),
            )
        }
    }
}
