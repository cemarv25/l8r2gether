package com.latertogether.domain

import com.google.common.truth.Truth.assertThat
import com.latertogether.domain.model.Message
import kotlinx.serialization.json.Json
import org.junit.Test

class MessageDtoSerializationTest {
    private val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    @Test
    fun json_uses_supabase_column_names() {
        val m = Message(
            id = "550e8400-e29b-41d4-a716-446655440000",
            contentKey = "yt:abc",
            mediaTimestamp = 123.5,
            body = "hi",
            authorId = "auth-1",
            clientCreatedAt = "2026-05-09T12:00:00Z",
        )
        val s = json.encodeToString(Message.serializer(), m)
        assertThat(s).contains("\"media_timestamp\":123.5")
        assertThat(s).contains("\"content_key\":\"yt:abc\"")
        assertThat(s).contains("\"author_id\":\"auth-1\"")
        assertThat(s).contains("\"client_created_at\":\"2026-05-09T12:00:00Z\"")
    }

    @Test
    fun round_trips_equal_message() {
        val m = Message(
            id = "1",
            contentKey = "k",
            mediaTimestamp = 0.25,
            body = "x",
            authorId = "a",
            clientCreatedAt = "2026-01-01T00:00:00Z",
        )
        val decoded = json.decodeFromString(Message.serializer(), json.encodeToString(Message.serializer(), m))
        assertThat(decoded).isEqualTo(m)
    }
}
