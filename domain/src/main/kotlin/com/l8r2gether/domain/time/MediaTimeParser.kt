package com.l8r2gether.domain.time

import kotlin.math.floor
import kotlin.math.max

sealed class MediaTimeParseResult {
    data class Ok(val seconds: Double) : MediaTimeParseResult()
    data class Invalid(val reason: String) : MediaTimeParseResult()
}

object MediaTimeParser {
    const val NUDGE_HALF_MINUTE_SEC: Double = 30.0
    const val NUDGE_TWO_MINUTES_SEC: Double = 120.0

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

    fun formatMmSs(seconds: Double): String {
        val total = max(0.0, seconds)
        val mins = floor(total / 60).toInt()
        val secs = floor(total % 60).toInt()
        return "%d:%02d".format(mins, secs)
    }
}
