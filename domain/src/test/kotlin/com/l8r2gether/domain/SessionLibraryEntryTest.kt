package com.l8r2gether.domain

import com.l8r2gether.domain.model.WatchSessionState
import com.l8r2gether.domain.session.SessionLibraryEntry
import com.l8r2gether.domain.session.pauseSubtitle
import com.l8r2gether.domain.session.prettifyContentKey
import com.l8r2gether.domain.session.sortByLastActivity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class SessionLibraryEntryTest {
    private fun entry(key: String, lastActivity: Long, pausedSec: Double = 100.0): SessionLibraryEntry {
        val session = WatchSessionState.initial(key, UUID.randomUUID().toString(), 0L, lastActivity)
            .copy(pausedAtMediaTimeSec = pausedSec)
        return SessionLibraryEntry(key, prettifyContentKey(key), session, lastActivity)
    }

    @Test
    fun sortByLastActivityDescending() {
        val sorted = sortByLastActivity(
            listOf(
                entry("a", 100L),
                entry("b", 300L),
                entry("c", 200L),
            ),
        )
        assertEquals(listOf("b", "c", "a"), sorted.map { it.contentKey })
    }

    @Test
    fun pauseSubtitleUsesPausedTime() {
        val subtitle = pauseSubtitle(entry("movie:1", 1L, 39 * 60 + 18.0))
        assertEquals("Paused at 39:18", subtitle)
    }

    @Test
    fun prettifyContentKey() {
        assertTrue(prettifyContentKey("yt:abc123").contains("Yt"))
        assertEquals("plain", prettifyContentKey("plain"))
    }
}
