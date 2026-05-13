package com.latertogether.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class ObservationSource {
    MediaSession,
    Accessibility,
    Manual,
    Mixed,
}
