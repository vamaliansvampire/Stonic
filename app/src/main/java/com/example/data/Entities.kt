package com.example.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "conversations")
data class Conversation(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val modelUsed: String = "gemini-2.5-flash"
)

@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = Conversation::class,
            parentColumns = ["id"],
            childColumns = ["conversationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["conversationId"])]
)
data class Message(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val conversationId: String,
    val sender: String, // "USER" or "STONIC"
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val imageUri: String? = null,
    val isFavorite: Boolean = false
)

@Entity(tableName = "reminders")
data class ReminderItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val notes: String = "",
    val dueTime: Long = System.currentTimeMillis() + 3600000L, // 1 hour ahead
    val isCompleted: Boolean = false,
    val priority: String = "MEDIUM" // "HIGH", "MEDIUM", "LOW"
)
