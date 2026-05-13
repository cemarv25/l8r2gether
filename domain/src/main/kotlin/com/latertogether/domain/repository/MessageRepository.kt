package com.latertogether.domain.repository

import com.latertogether.domain.model.Message

/**
 * Persistence port for thread messages ([§10.2](latertogether-companion-sync-spec.md)).
 */
interface MessageRepository {
    suspend fun fetchMessages(
        contentKey: String,
        minMediaTimestampSec: Double? = null,
        maxMediaTimestampSec: Double? = null,
        limit: Int = 100,
        offset: Long = 0,
    ): Result<List<Message>>

    suspend fun insertMessage(newMessage: NewMessage): Result<Message>
}

/**
 * Insert payload without server-assigned id.
 */
data class NewMessage(
    val contentKey: String,
    /** Seconds from media start; maps to JSON `mediaTimestamp` ([§10.1](latertogether-companion-sync-spec.md)). */
    val mediaTimestamp: Double,
    val body: String,
    val authorId: String,
    /** ISO-8601; maps to JSON `clientCreatedAt`. */
    val clientCreatedAt: String,
)
