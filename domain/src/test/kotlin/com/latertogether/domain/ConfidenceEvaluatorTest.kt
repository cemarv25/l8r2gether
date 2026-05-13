package com.latertogether.domain

import com.google.common.truth.Truth.assertThat
import com.latertogether.domain.confidence.ConfidenceEvaluator
import com.latertogether.domain.model.ClockMode
import com.latertogether.domain.model.Confidence
import com.latertogether.domain.model.PlaybackState
import com.latertogether.domain.model.WatchSessionState
import org.junit.Test

class ConfidenceEvaluatorTest {
    private val t0 = 100_000_000_000L

    @Test
    fun should_prompt_when_not_high_or_stale() {
        val weak = WatchSessionState.initial("k", "s", t0).copy(confidence = Confidence.Medium)
        assertThat(ConfidenceEvaluator.shouldPrompt(weak, t0, staleMinutes = 5.0)).isTrue()

        val strongFresh = WatchSessionState.initial("k", "s", t0).copy(
            confidence = Confidence.High,
            lastObservationWallNanos = t0,
        )
        assertThat(ConfidenceEvaluator.shouldPrompt(strongFresh, t0 + 60_000_000_000L, staleMinutes = 5.0)).isFalse()
    }

    @Test
    fun dismiss_sets_low() {
        val s = WatchSessionState.initial("k", "s", t0).copy(confidence = Confidence.High)
        val next = ConfidenceEvaluator.onDismissSyncPrompt(s)
        assertThat(next.confidence).isEqualTo(Confidence.Low)
        assertThat(next.dismissedPromptCount).isEqualTo(1)
    }

    @Test
    fun staleness_decay_high_to_medium_to_low() {
        var s = WatchSessionState.initial("k", "s", t0).copy(
            confidence = Confidence.High,
            lastObservationWallNanos = t0,
        )
        val after16m = t0 + 16L * 60_000_000_000L
        s = ConfidenceEvaluator.decayForStaleness(s, after16m)
        assertThat(s.confidence).isEqualTo(Confidence.Medium)

        val after31mMore = after16m + 31L * 60_000_000_000L
        s = ConfidenceEvaluator.decayForStaleness(s, after31mMore)
        assertThat(s.confidence).isEqualTo(Confidence.Low)
    }

    @Test
    fun minutes_since_observation_tracks_wall_gap() {
        val s = WatchSessionState.initial("k", "s", t0).copy(
            clockMode = ClockMode.Monotonic,
            lastObservationWallNanos = t0,
            playbackState = PlaybackState.Paused,
        )
        val mins = ConfidenceEvaluator.minutesSinceObservation(s, t0 + 3L * 60_000_000_000L)
        assertThat(mins).isWithin(1e-6).of(3.0)
    }
}
