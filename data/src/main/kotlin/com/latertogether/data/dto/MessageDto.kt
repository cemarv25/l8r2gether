package com.latertogether.data.dto

import com.latertogether.domain.model.Message
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MessageDto(
    val id: String? = null,
    @SerialName("content_key") val contentKey: String,
    @SerialName("media_timestamp") val mediaTimestamp: Double,
    val body: String,
    @SerialName("author_id") val authorId: String,
    @SerialName("client_created_at") val clientCreatedAt: String,
)

@Serializable
data class MessageInsertDto(
    @SerialName("content_key") val contentKey: String,
    @SerialName("media_timestamp") val mediaTimestamp: Double,
    val body: String,
    @SerialName("author_id") val authorId: String,
    @SerialName("client_created_at") val clientCreatedAt: String,
)

fun MessageDto.toDomain(): Message =
    Message(
        id = id ?: "",
        contentKey = contentKey,
        mediaTimestamp = mediaTimestamp,
        body = body,
        authorId = authorId,
        clientCreatedAt = clientCreatedAt,
    )
