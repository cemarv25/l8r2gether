package com.latertogether.domain

import com.google.common.truth.Truth.assertThat
import com.latertogether.domain.time.MediaTimeParseResult
import com.latertogether.domain.time.MediaTimeParser
import org.junit.Test

class MediaTimeParserTest {
    @Test
    fun parses_seconds() {
        val r = MediaTimeParser.parse("90") as MediaTimeParseResult.Ok
        assertThat(r.seconds).isEqualTo(90.0)
    }

    @Test
    fun parses_mm_ss() {
        val r = MediaTimeParser.parse("1:05") as MediaTimeParseResult.Ok
        assertThat(r.seconds).isEqualTo(65.0)
    }

    @Test
    fun runtime_hint_rejects_overflow() {
        val r = MediaTimeParser.parseWithRuntimeHint("4000", runtimeSec = 3600.0)
        assertThat(r).isInstanceOf(MediaTimeParseResult.Invalid::class.java)
    }

    @Test
    fun adjust_seconds_clamps_negative_to_zero() {
        assertThat(MediaTimeParser.adjustSeconds(10.0, -15.0)).isEqualTo(0.0)
        assertThat(MediaTimeParser.adjustSeconds(100.0, MediaTimeParser.NUDGE_HALF_MINUTE_SEC))
            .isEqualTo(130.0)
    }
}
