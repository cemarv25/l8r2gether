package com.latertogether.data

import com.latertogether.data.dto.MessageDto
import com.latertogether.data.dto.MessageInsertDto
import com.latertogether.data.dto.toDomain
import com.latertogether.domain.model.Message
import com.latertogether.domain.repository.MessageRepository
import com.latertogether.domain.repository.NewMessage
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order

class MessageRepositoryImpl(
    private val client: SupabaseClient,
) : MessageRepository {

    override suspend fun fetchMessages(
        contentKey: String,
        minMediaTimestampSec: Double?,
        maxMediaTimestampSec: Double?,
        limit: Int,
        offset: Long,
    ): Result<List<Message>> =
        runCatching {
            val rows = client.from("messages").select {
                filter {
                    eq("content_key", contentKey)
                    if (minMediaTimestampSec != null) {
                        gte("media_timestamp", minMediaTimestampSec!!)
                    }
                    if (maxMediaTimestampSec != null) {
                        lte("media_timestamp", maxMediaTimestampSec!!)
                    }
                }
                order(column = "media_timestamp", order = Order.ASCENDING)
                if (limit > 0) {
                    range(offset until offset + limit)
                }
            }.decodeList<MessageDto>()
            rows.map { it.toDomain() }
        }

    override suspend fun insertMessage(newMessage: NewMessage): Result<Message> =
        runCatching {
            val user = client.auth.currentUserOrNull()
                ?: error("Not signed in; cannot insert message.")
            if (user.id != newMessage.authorId) {
                error("authorId must match signed-in user.")
            }
            val payload = MessageInsertDto(
                contentKey = newMessage.contentKey,
                mediaTimestamp = newMessage.mediaTimestamp,
                body = newMessage.body,
                authorId = newMessage.authorId,
                clientCreatedAt = newMessage.clientCreatedAt,
            )
            client.from("messages").insert(payload) {
                select()
            }.decodeSingle<MessageDto>().toDomain()
        }
}
