package com.latertogether.app.playback

import android.content.ComponentName
import android.os.Build
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.Handler
import android.os.Looper
import android.service.notification.NotificationListenerService
import com.latertogether.domain.model.ObservationSource
import com.latertogether.domain.model.PlaybackObservation
import com.latertogether.domain.model.PlaybackState as DomainPlaybackState

/**
 * Uses notification listener permission so [MediaSessionManager.getActiveSessions] can subscribe to
 * active media controllers ([§11.2](latertogether-companion-sync-spec.md)).
 * User must enable this service in system settings.
 */
class PlaybackNotificationListenerService : NotificationListenerService() {

    private lateinit var sessionManager: MediaSessionManager
    private val mainHandler = Handler(Looper.getMainLooper())
    private val activeControllers = mutableMapOf<MediaController, MediaController.Callback>()
    private val sessionsListener = MediaSessionManager.OnActiveSessionsChangedListener {
        refreshControllers()
    }

    private val componentName: ComponentName
        get() = ComponentName(this, PlaybackNotificationListenerService::class.java)

    override fun onListenerConnected() {
        super.onListenerConnected()
        sessionManager = getSystemService(MEDIA_SESSION_SERVICE) as MediaSessionManager
        sessionManager.addOnActiveSessionsChangedListener(
            sessionsListener,
            componentName,
        )
        refreshControllers()
    }

    override fun onDestroy() {
        unregisterAll()
        if (::sessionManager.isInitialized && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            sessionManager.removeOnActiveSessionsChangedListener(sessionsListener)
        }
        super.onDestroy()
    }

    private fun unregisterAll() {
        for ((controller, callback) in activeControllers) {
            controller.unregisterCallback(callback)
        }
        activeControllers.clear()
    }

    private fun refreshControllers() {
        if (!::sessionManager.isInitialized) return
        unregisterAll()
        val controllers = sessionManager.getActiveSessions(componentName)
        for (controller in controllers) {
            val callback = object : MediaController.Callback() {
                override fun onPlaybackStateChanged(state: PlaybackState?) {
                    emitAndroidPlayback(state)
                }
            }
            controller.registerCallback(callback, mainHandler)
            activeControllers[controller] = callback
            emitAndroidPlayback(controller.playbackState)
        }
    }

    private fun emitAndroidPlayback(state: PlaybackState?) {
        val observedAtWallNanos = System.nanoTime()
        if (state == null) {
            PlaybackObservationBus.tryEmit(
                PlaybackObservation.StateOnly(
                    playbackState = DomainPlaybackState.Paused,
                    observedAtWallNanos = observedAtWallNanos,
                    source = ObservationSource.MediaSession,
                ),
            )
            return
        }

        val domainState =
            if (state.state == PlaybackState.STATE_PLAYING) {
                DomainPlaybackState.Playing
            } else {
                DomainPlaybackState.Paused
            }

        val posMs = state.position
        val lastPosMs = state.lastPositionUpdateTime
        val nowElapsed = android.os.SystemClock.elapsedRealtime()
        val ageMs = if (lastPosMs > 0L) nowElapsed - lastPosMs else Long.MAX_VALUE
        val stalePosition = ageMs > 10_000L || posMs < 0L

        if (!stalePosition) {
            PlaybackObservationBus.tryEmit(
                PlaybackObservation.Position(
                    mediaTimeSec = posMs / 1000.0,
                    observedAtWallNanos = observedAtWallNanos,
                    source = ObservationSource.MediaSession,
                    playbackStateHint = domainState,
                ),
            )
        } else {
            PlaybackObservationBus.tryEmit(
                PlaybackObservation.StateOnly(
                    playbackState = domainState,
                    observedAtWallNanos = observedAtWallNanos,
                    source = ObservationSource.MediaSession,
                ),
            )
        }
    }
}
