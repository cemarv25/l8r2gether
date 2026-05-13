package com.latertogether.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class PlaybackState {
    Playing,
    Paused,
}
