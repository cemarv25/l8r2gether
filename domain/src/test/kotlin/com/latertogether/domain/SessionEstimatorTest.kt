package com.latertogether.domain

import com.google.common.truth.Truth.assertThat
import com.latertogether.domain.model.ClockMode
import com.latertogether.domain.model.PlaybackState
import com.latertogether.domain.model.WatchSessionState
import com.latertogether.domain.model.SessionEstimator
import org.junit.Test

class SessionEstimatorTest {
    @Test
    fun playing_extrapolates_with_playback_rate() {
        val t0 = 1_000_000_000L
        val state = WatchSessionState.initial("k", "s", t0).copy(
            clockMode = ClockMode.Monotonic,
            playbackState = PlaybackState.Playing,
            sessionAnchorWallNanos = t0,
            baseMediaTimeSec = 10.0,
            playbackRate = 1.5,
            driftMs = 500.0,
        )
        val now = t0 + 10_000_000_000L // +10s wall
        val tEst = SessionEstimator.estimateMediaTime(state, now)
        assertThat(tEst).isWithin(1e-6).of(10.0 + 1.5 * 10.0 + 0.5)
        assertThat(SessionEstimator.estimateTEst(state, now)).isWithin(1e-6).of(tEst)
    }

    @Test
    fun paused_freezes_at_pause_position_plus_drift() {
        val t0 = 5_000_000_000L
        val state = WatchSessionState.initial("k", "s", t0).copy(
            playbackState = PlaybackState.Paused,
            pausedAtMediaTimeSec = 123.0,
            driftMs = -1000.0,
            sessionAnchorWallNanos = t0,
        )
        val later = t0 + 999_000_000_000L
        val tEst = SessionEstimator.estimateMediaTime(state, later)
        assertThat(tEst).isWithin(1e-6).of(123.0 - 1.0)
    }
}
