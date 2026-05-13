package com.latertogether.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Adapter → domain playback signals (MediaSession, Accessibility, or fused).
 * Serializable for stable logging / future persistence; wall clock aligns with [WatchSessionState] anchors.
 */
@Serializable
sealed interface PlaybackObservation {
    val observedAtWallNanos: Long
    val source: ObservationSource

    @Serializable
    @SerialName("position")
    data class Position(
        @SerialName("media_time_sec")
        val mediaTimeSec: Double,
        @SerialName("observed_at_wall_nanos")
        override val observedAtWallNanos: Long,
        override val source: ObservationSource,
        val playbackStateHint: PlaybackState? = null,
    ) : PlaybackObservation

    @Serializable
    @SerialName("state_only")
    data class StateOnly(
        val playbackState: PlaybackState,
        @SerialName("observed_at_wall_nanos")
        override val observedAtWallNanos: Long,
        override val source: ObservationSource,
    ) : PlaybackObservation

    @Serializable
    @SerialName("transport")
    data class Transport(
        val playbackState: PlaybackState,
        @SerialName("observed_at_wall_nanos")
        override val observedAtWallNanos: Long,
        override val source: ObservationSource,
    ) : PlaybackObservation
}
