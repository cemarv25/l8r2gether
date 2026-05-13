package com.latertogether.domain

import com.google.common.truth.Truth.assertThat
import com.latertogether.domain.model.ObservationSource
import com.latertogether.domain.model.PlaybackObservation
import com.latertogether.domain.model.PlaybackState
import kotlinx.serialization.json.Json
import org.junit.Test

class PlaybackObservationSerializationTest {
    private val json = Json { encodeDefaults = true; ignoreUnknownKeys = true }

    @Test
    fun position_round_trips() {
        val obs: PlaybackObservation = PlaybackObservation.Position(
            mediaTimeSec = 99.0,
            observedAtWallNanos = 5_000_000_000L,
            source = ObservationSource.MediaSession,
            playbackStateHint = PlaybackState.Playing,
        )
        val back = json.decodeFromString(
            PlaybackObservation.serializer(),
            json.encodeToString(PlaybackObservation.serializer(), obs),
        )
        assertThat(back).isEqualTo(obs)
    }

    @Test
    fun serializes_discriminated_kind() {
        val obs: PlaybackObservation = PlaybackObservation.StateOnly(
            playbackState = PlaybackState.Paused,
            observedAtWallNanos = 1L,
            source = ObservationSource.Accessibility,
        )
        val raw = json.encodeToString(PlaybackObservation.serializer(), obs)
        assertThat(raw).contains("state_only")
    }
}
