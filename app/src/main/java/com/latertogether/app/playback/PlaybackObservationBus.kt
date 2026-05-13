package com.latertogether.app.playback

import com.latertogether.domain.model.PlaybackObservation
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object PlaybackObservationBus {
    private val sink = MutableSharedFlow<PlaybackObservation>(
        extraBufferCapacity = 32,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val observations: SharedFlow<PlaybackObservation> = sink.asSharedFlow()

    fun tryEmit(observation: PlaybackObservation) {
        sink.tryEmit(observation)
    }
}
