package com.latertogether.domain.model

/**
 * Authoritative (wall, media) anchor from manual sync or strong observation.
 */
data class MediaTimeCheckpoint(
    val wallNanos: Long,
    val mediaTimeSec: Double,
    val source: ObservationSource,
)
