package com.latertogether.domain.session

import com.latertogether.domain.model.PlaybackState
import com.latertogether.domain.model.WatchSessionState
import com.latertogether.domain.time.MediaTimeParser

data class SessionLibraryEntry(
    val contentKey: String,
    val displayTitle: String,
    val session: WatchSessionState,
    val lastActivityAtEpochMs: Long,
)

fun sortByLastActivity(entries: List<SessionLibraryEntry>): List<SessionLibraryEntry> =
    entries.sortedByDescending { it.lastActivityAtEpochMs }

fun pauseSubtitle(entry: SessionLibraryEntry): String {
    val mediaSec = when (entry.session.playbackState) {
        PlaybackState.Paused -> entry.session.pausedAtMediaTimeSec ?: entry.session.baseMediaTimeSec
        PlaybackState.Playing -> entry.session.baseMediaTimeSec
    }
    return "Paused at ${MediaTimeParser.formatMmSs(mediaSec)}"
}

fun prettifyContentKey(contentKey: String): String {
    val trimmed = contentKey.trim()
    if (trimmed.isEmpty()) return trimmed
    val prefixMatch = Regex("""^(\w+):(.+)$""").matchEntire(trimmed)
    return if (prefixMatch != null) {
        val prefix = prefixMatch.groupValues[1].replaceFirstChar { it.uppercase() }
        val rest = prefixMatch.groupValues[2]
        "$prefix · $rest"
    } else {
        trimmed
    }
}
