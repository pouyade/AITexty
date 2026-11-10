package com.dpouya.messages.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "conversations", indices = [Index(value = ["address"], unique = true)])
data class ConversationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val address: String,
    val displayName: String,
    val snippet: String = "",
    val timestamp: Long = 0L,
    val unreadCount: Int = 0,
)

@Entity(tableName = "messages", indices = [Index("conversationId"), Index("timestamp")])
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val conversationId: Long,
    val address: String,
    val body: String,
    val timestamp: Long,
    val isOutgoing: Boolean,
    val status: String = Status.SENT, // SENT, DELIVERED, FAILED
) {
    object Status {
        const val SENT = "SENT"
        const val DELIVERED = "DELIVERED"
        const val FAILED = "FAILED"
    }
}
