package com.latertogether.domain.chat

import com.latertogether.domain.model.Message

/**
 * Idempotent reveal tracking with seek-backward re-show ([§10.2](latertogether-companion-sync-spec.md)).
 */
data class MessageRevealUpdate(
    val visibleMessages: List<Message>,
    /** Messages newly surfaced this tick (not yet shown this forward pass). */
    val newlyRevealed: List<Message>,
)

class MessageRevealLedger {
    private val revealedOnceThisForwardPass = mutableSetOf<String>()

    fun evaluate(messages: List<Message>, tEstSec: Double): MessageRevealUpdate {
        val visible = messages
            .filter { it.mediaTimestamp <= tEstSec }
            .sortedBy { it.mediaTimestamp }

        revealedOnceThisForwardPass.removeAll { messageId ->
            val message = messages.firstOrNull { it.id == messageId }
            message == null || message.mediaTimestamp > tEstSec
        }

        val newly = visible.filter { it.id !in revealedOnceThisForwardPass }
        newly.forEach { revealedOnceThisForwardPass.add(it.id) }

        return MessageRevealUpdate(
            visibleMessages = visible,
            newlyRevealed = newly,
        )
    }

    fun reset() {
        revealedOnceThisForwardPass.clear()
    }
}
