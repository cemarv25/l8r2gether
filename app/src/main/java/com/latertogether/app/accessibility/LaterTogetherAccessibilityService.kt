package com.latertogether.app.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.accessibility.AccessibilityEvent
import com.latertogether.app.playback.PlaybackObservationBus
import com.latertogether.domain.model.ObservationSource
import com.latertogether.domain.model.PlaybackObservation
import com.latertogether.domain.model.PlaybackState
import com.latertogether.domain.time.MediaTimeParseResult
import com.latertogether.domain.time.MediaTimeParser

/**
 * Optional fallback: inspect accessibility node trees only ([§11.3](latertogether-companion-sync-spec.md)).
 */
class LaterTogetherAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        serviceInfo = serviceInfo.apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED or AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = flags or AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val root = rootInActiveWindow ?: return
        try {
            val sb = StringBuilder()
            fun scan(node: android.view.accessibility.AccessibilityNodeInfo?, depth: Int) {
                if (node == null || depth > 24) return
                node.text?.let { sb.append(it).append(' ') }
                node.contentDescription?.let { sb.append(it).append(' ') }
                for (i in 0 until node.childCount) {
                    val child = node.getChild(i)
                    try {
                        scan(child, depth + 1)
                    } finally {
                        child?.recycle()
                    }
                }
            }
            scan(root, 0)
            val tokens = sb.toString().split(Regex("\\s+")).filter { it.isNotBlank() }
            var best: Double? = null
            for (t in tokens) {
                when (val r = MediaTimeParser.parse(t)) {
                    is MediaTimeParseResult.Ok -> {
                        best = if (best == null) r.seconds else kotlin.math.max(best!!, r.seconds)
                    }
                    else -> Unit
                }
            }
            if (best != null) {
                PlaybackObservationBus.tryEmit(
                    PlaybackObservation.Position(
                        mediaTimeSec = best,
                        observedAtWallNanos = System.nanoTime(),
                        source = ObservationSource.Accessibility,
                        playbackStateHint = PlaybackState.Playing,
                    ),
                )
            }
        } finally {
            root.recycle()
        }
    }

    override fun onInterrupt() {}

    companion object {
        fun settingsIntent(): Intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)

        fun isEnabled(context: Context): Boolean {
            val cn = ComponentName(context, LaterTogetherAccessibilityService::class.java).flattenToString()
            val enabled = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
            ) ?: return false
            return enabled.split(':').any { it.equals(cn, ignoreCase = true) }
        }
    }
}
