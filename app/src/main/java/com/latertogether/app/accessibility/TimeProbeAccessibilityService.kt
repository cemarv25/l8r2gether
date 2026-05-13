package com.latertogether.app.accessibility

import android.accessibilityservice.AccessibilityService
import android.os.SystemClock
import android.view.accessibility.AccessibilityNodeInfo
import com.latertogether.app.playback.PlaybackObservationBus
import com.latertogether.domain.model.ObservationSource
import com.latertogether.domain.model.PlaybackObservation
import com.latertogether.domain.model.PlaybackState as DomainPlaybackState
import com.latertogether.domain.time.MediaTimeParseResult
import com.latertogether.domain.time.MediaTimeParser

/**
 * Tree-only reads ([§11.3](latertogether-companion-sync-spec.md)); no capture/OCR of video frames.
 */
class TimeProbeAccessibilityService : AccessibilityService() {

    private var lastEmitElapsedMs: Long = 0L

    override fun onAccessibilityEvent(event: android.view.accessibility.AccessibilityEvent?) {
        if (event == null) return
        val root = rootInActiveWindow ?: return
        val nowElapsed = SystemClock.elapsedRealtime()
        if (nowElapsed - lastEmitElapsedMs < MIN_EMIT_INTERVAL_MS) {
            root.recycle()
            return
        }

        val texts = LinkedHashSet<String>()
        collectText(root, texts)
        root.recycle()

        var bestSec: Double? = null
        for (t in texts) {
            when (val r = MediaTimeParser.parse(t.trim())) {
                is MediaTimeParseResult.Ok -> {
                    bestSec = r.seconds
                    break
                }
                is MediaTimeParseResult.Invalid -> Unit
            }
        }

        if (bestSec != null) {
            lastEmitElapsedMs = nowElapsed
            PlaybackObservationBus.tryEmit(
                PlaybackObservation.Position(
                    mediaTimeSec = bestSec,
                    observedAtWallNanos = System.nanoTime(),
                    source = ObservationSource.Accessibility,
                    playbackStateHint = DomainPlaybackState.Playing,
                ),
            )
        }
    }

    override fun onInterrupt() = Unit

    private fun collectText(node: AccessibilityNodeInfo?, out: MutableSet<String>) {
        if (node == null) return
        node.text?.toString()?.let { if (it.isNotBlank()) out.add(it) }
        node.contentDescription?.toString()?.let { if (it.isNotBlank()) out.add(it) }
        for (i in 0 until node.childCount) {
            collectText(node.getChild(i), out)
        }
    }

    private companion object {
        const val MIN_EMIT_INTERVAL_MS = 900L
    }
}
