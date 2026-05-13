package com.latertogether.domain.time

import kotlin.math.floor
import kotlin.math.max

sealed class MediaTimeParseResult {
    data class Ok(val seconds: Double) : MediaTimeParseResult()
    data class Invalid(val reason: String) : MediaTimeParseResult()
}

/**
 * [§8.3](latertogether-companion-sync-spec.md): numeric seconds and mm:ss entry.
 */
object MediaTimeParser {
    /** Optional quick nudges for sync UX (±30s style). */
    const val NUDGE_HALF_MINUTE_SEC: Double = 30.0

    /** Optional quick nudges (±2m style). */
    const val NUDGE_TWO_MINUTES_SEC: Double = 120.0

    /** Keeps result non-negative; does not re-validate against runtime (use [parseWithRuntimeHint] first if needed). */
    fun adjustSeconds(baseSeconds: Double, deltaSeconds: Double): Double =
        max(0.0, baseSeconds + deltaSeconds)

    fun parse(input: String): MediaTimeParseResult {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return MediaTimeParseResult.Invalid("empty")

        val mmSs = Regex("""^(\d+):(\d{1,2})(?:\.(\d+))?$""")
        mmSs.matchEntire(trimmed)?.let { m ->
            val mins = m.groupValues[1].toLong()
            val secs = m.groupValues[2].toInt()
            if (secs >= 60) return MediaTimeParseResult.Invalid("seconds must be < 60 in mm:ss")
            val fractional = m.groupValues.getOrNull(3)?.let { "0.$it".toDouble() } ?: 0.0
            return MediaTimeParseResult.Ok(mins * 60 + secs + fractional)
        }

        val numeric = trimmed.toDoubleOrNull()
            ?: return MediaTimeParseResult.Invalid("expected seconds or mm:ss")
        if (numeric < 0) return MediaTimeParseResult.Invalid("negative time")
        return MediaTimeParseResult.Ok(numeric)
    }

    fun parseWithRuntimeHint(input: String, runtimeSec: Double?): MediaTimeParseResult {
        val base = parse(input)
        if (base !is MediaTimeParseResult.Ok) return base
        val rt = runtimeSec ?: return base
        if (base.seconds > rt + 1e-6) {
            return MediaTimeParseResult.Invalid("beyond known runtime (${formatMmSs(rt)})")
        }
        return base
    }

    fun formatMmSs(seconds: Double): String {
        if (seconds.isNaN() || seconds.isInfinite()) return "--:--"
        val total = floor(seconds).toLong()
        val mm = total / 60
        val ss = total % 60
        return "%d:%02d".format(mm, ss)
    }
}
