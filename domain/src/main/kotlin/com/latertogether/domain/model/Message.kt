package com.latertogether.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Client ↔ Supabase `messages` row contract ([§10.1](latertogether-companion-sync-spec.md)).
 * [mediaTimestamp] is **seconds** (float / `media_timestamp` in Postgres), authoritative for replay.
 */
@Serializable
data class Message(
    val id: String,
    @SerialName("content_key")
    val contentKey: String,
    @SerialName("media_timestamp")
    val mediaTimestamp: Double,
    val body: String,
    @SerialName("author_id")
    val authorId: String,
    /** ISO-8601 string; maps to `client_created_at`. */
    @SerialName("client_created_at")
    val clientCreatedAt: String,
)
