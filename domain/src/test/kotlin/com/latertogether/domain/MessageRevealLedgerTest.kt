package com.latertogether.domain

import com.google.common.truth.Truth.assertThat
import com.latertogether.domain.chat.MessageRevealLedger
import com.latertogether.domain.model.Message
import org.junit.Test

class MessageRevealLedgerTest {
    private fun msg(id: String, t: Double, body: String = id) = Message(
        id = id,
        contentKey = "k",
        mediaTimestamp = t,
        body = body,
        authorId = "a",
        clientCreatedAt = "2026-01-01T00:00:00Z",
    )

    @Test
    fun seek_backward_allows_reshow() {
        val ledger = MessageRevealLedger()
        val m1 = msg("1", 5.0)
        val m2 = msg("2", 20.0)
        val all = listOf(m1, m2)

        val a = ledger.evaluate(all, 25.0)
        assertThat(a.newlyRevealed.map { it.id }).containsExactly("1", "2")

        val b = ledger.evaluate(all, 10.0)
        assertThat(b.visibleMessages).containsExactly(m1)
        assertThat(b.newlyRevealed).isEmpty()

        val c = ledger.evaluate(all, 25.0)
        assertThat(c.newlyRevealed.map { it.id }).containsExactly("2")
    }
}
