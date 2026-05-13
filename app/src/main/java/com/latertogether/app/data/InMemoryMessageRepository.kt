package com.latertogether.app.data

import com.latertogether.domain.model.Message
import com.latertogether.domain.repository.MessageRepository
import com.latertogether.domain.repository.NewMessage
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Offline placeholder when Supabase URL/key are not configured in `local.properties`.
 */
class InMemoryMessageRepository : MessageRepository {
    private val byKey = ConcurrentHashMap<String, MutableList<Message>>()

    override suspend fun fetchMessages(
        contentKey: String,
        minMediaTimestampSec: Double?,
        maxMediaTimestampSec: Double?,
        limit: Int,
        offset: Long,
    ): Result<List<Message>> {
        val all = byKey[contentKey].orEmpty().asSequence()
            .filter { m ->
                (minMediaTimestampSec == null || m.mediaTimestamp >= minMediaTimestampSec) &&
                    (maxMediaTimestampSec == null || m.mediaTimestamp <= maxMediaTimestampSec)
            }
            .sortedBy { it.mediaTimestamp }
            .drop(offset.toInt())
            .take(limit)
            .toList()
        return Result.success(all)
    }

    override suspend fun insertMessage(newMessage: NewMessage): Result<Message> {
        val msg = Message(
            id = UUID.randomUUID().toString(),
            contentKey = newMessage.contentKey,
            mediaTimestamp = newMessage.mediaTimestamp,
            body = newMessage.body,
            authorId = newMessage.authorId,
            clientCreatedAt = newMessage.clientCreatedAt,
        )
        byKey.getOrPut(newMessage.contentKey) { mutableListOf() }.add(msg)
        return Result.success(msg)
    }
}
