package com.latertogether.app.playback

import android.content.Context
import android.media.AudioManager
import android.media.AudioPlaybackConfiguration
import android.os.Build
import android.os.Handler
import android.os.Looper
import com.latertogether.domain.model.ObservationSource
import com.latertogether.domain.model.PlaybackObservation
import com.latertogether.domain.model.PlaybackState
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Best-effort playback cues from [AudioManager] routing ([§11.2](latertogether-companion-sync-spec.md)).
 * Does not expose precise media position when the OS withholds it — fusion degrades gracefully.
 */
class MediaObservationAdapter(private val context: Context) {

    fun observeTransport(): Flow<PlaybackObservation> = callbackFlow {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            awaitClose { }
            return@callbackFlow
        }
        val audioManager = context.getSystemService(AudioManager::class.java)
        val handler = Handler(Looper.getMainLooper())
        val callback = object : AudioManager.AudioPlaybackCallback() {
            override fun onPlaybackConfigChanged(configs: MutableList<AudioPlaybackConfiguration>?) {
                val active =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        audioManager.activePlaybackConfigurations.isNotEmpty()
                    } else {
                        !configs.isNullOrEmpty()
                    }
                trySend(
                    PlaybackObservation.Transport(
                        playbackState = if (active) PlaybackState.Playing else PlaybackState.Paused,
                        observedAtWallNanos = System.nanoTime(),
                        source = ObservationSource.MediaSession,
                    ),
                )
            }
        }
        audioManager.registerAudioPlaybackCallback(callback, handler)
        trySend(
            PlaybackObservation.Transport(
                playbackState = if (audioManager.isMusicActive) PlaybackState.Playing else PlaybackState.Paused,
                observedAtWallNanos = System.nanoTime(),
                source = ObservationSource.MediaSession,
            ),
        )
        awaitClose {
            audioManager.unregisterAudioPlaybackCallback(callback)
        }
    }
}
