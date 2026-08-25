package com.aima.koraki.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class NoteType {
    NOTE,
    JOURNAL
}

enum class NoteMood(
    val emoji: String,
    val label: String,
) {
    ROMANTIC("🌹", "Loving"),
    DEEP_THOUGHTS("🍷", "Deep Thoughts"),
    VENTING("🥀", "Quiet"),
    JOY("✨", "Joy");

    companion object {
        fun fromString(name: String?): NoteMood? {
            if (name == null) return null
            return entries.find { it.name.equals(name, ignoreCase = true) || it.label.equals(name, ignoreCase = true) }
        }
    }
}

/**
 * Represents a single note in the Koraki database.
 *
 * Security invariant: all DAO queries that expose notes publicly MUST filter
 * on [isLocked] = false. Locked notes are never surfaced in public search results.
 */
@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val timestamp: Long,
    val isLocked: Boolean = false,
    val isPinned: Boolean = false,
    val companionId: Long? = null,
    val type: NoteType = NoteType.NOTE,
    val images: List<String> = emptyList(),
    val audio: List<String> = emptyList(),
    val mood: String? = null,
    val xpAwarded: Boolean = false,
)
