package com.latertogether.domain

import com.google.common.truth.Truth.assertThat
import com.latertogether.domain.fusion.FusionPolicy
import com.latertogether.domain.model.Confidence
import com.latertogether.domain.model.MediaTimeCheckpoint
import com.latertogether.domain.model.ObservationSource
import com.latertogether.domain.model.PlaybackState
import com.latertogether.domain.model.WatchSessionState
import org.junit.Test

class FusionPolicyCheckpointTest {
    @Test
    fun checkpoint_reanchors() {
        val t0 = 10_000_000_000L
        val s0 = WatchSessionState.initial("k", "s", t0)
        val cp = MediaTimeCheckpoint(
            wallNanos = t0 + 5_000_000_000L,
            mediaTimeSec = 42.0,
            source = ObservationSource.Manual,
        )
        val s1 = FusionPolicy.applyCheckpoint(s0, cp)
        assertThat(s1.baseMediaTimeSec).isEqualTo(42.0)
        assertThat(s1.sessionAnchorWallNanos).isEqualTo(cp.wallNanos)
        assertThat(s1.confidence).isEqualTo(Confidence.High)
        assertThat(s1.playbackState).isEqualTo(PlaybackState.Playing)
    }
}
