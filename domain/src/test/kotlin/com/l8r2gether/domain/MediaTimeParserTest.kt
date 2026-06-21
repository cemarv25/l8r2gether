package com.l8r2gether.domain

import com.l8r2gether.domain.time.MediaTimeParseResult
import com.l8r2gether.domain.time.MediaTimeParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MediaTimeParserTest {
    @Test
    fun parseSeconds() {
        val r = MediaTimeParser.parse("125")
        assertTrue(r is MediaTimeParseResult.Ok)
        assertEquals(125.0, (r as MediaTimeParseResult.Ok).seconds, 0.001)
    }

    @Test
    fun parseMmSs() {
        val r = MediaTimeParser.parse("39:18")
        assertTrue(r is MediaTimeParseResult.Ok)
        assertEquals(39 * 60 + 18.0, (r as MediaTimeParseResult.Ok).seconds, 0.001)
    }

    @Test
    fun formatMmSs() {
        assertEquals("39:18", MediaTimeParser.formatMmSs(39 * 60 + 18.0))
        assertEquals("0:05", MediaTimeParser.formatMmSs(5.0))
    }

    @Test
    fun invalidEmpty() {
        assertTrue(MediaTimeParser.parse("") is MediaTimeParseResult.Invalid)
    }

    @Test
    fun adjustSecondsClampsAtZero() {
        assertEquals(0.0, MediaTimeParser.adjustSeconds(10.0, -20.0), 0.001)
    }
}
