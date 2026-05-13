package com.latertogether.data

import com.latertogether.domain.model.Message
import com.latertogether.domain.repository.MessageRepository
import com.latertogether.domain.repository.NewMessage
import java.util.UUID

/**
 * Used when Supabase URL/key are not configured, or for offline bring-up ([§13.3](latertogether-companion-sync-spec.md)).
 */
class StubMessageRepository : MessageRepository {
    private val byKey = mutableMapOf<String, MutableList<Message>>()

    override suspend fun fetchMessages(
        contentKey: String,
        minMediaTimestampSec: Double?,
        maxMediaTimestampSec: Double?,
        limit: Int,
        offset: Long,
    ): Result<List<Message>> {
        val rows = byKey[contentKey].orEmpty().asSequence()
            .filter { m ->
                (minMediaTimestampSec == null || m.mediaTimestamp >= minMediaTimestampSec) &&
                    (maxMediaTimestampSec == null || m.mediaTimestamp <= maxMediaTimestampSec)
            }
            .sortedBy { it.mediaTimestamp }
            .drop(offset.toInt().coerceAtLeast(0))
            .take(limit.coerceAtLeast(0))
            .toList()
        return Result.success(rows)
    }

    override suspend fun insertMessage(newMessage: NewMessage): Result<Message> {
        val m = Message(
            id = UUID.randomUUID().toString(),
            contentKey = newMessage.contentKey,
            mediaTimestamp = newMessage.mediaTimestamp,
            body = newMessage.body,
            authorId = newMessage.authorId,
            clientCreatedAt = newMessage.clientCreatedAt,
        )
        byKey.getOrPut(newMessage.contentKey) { mutableListOf() }.add(m)
        return Result.success(m)
    }
}
