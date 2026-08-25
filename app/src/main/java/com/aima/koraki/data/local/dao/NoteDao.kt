package com.aima.koraki.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.aima.koraki.data.local.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for notes.
 *
 * SECURITY: [getPublicNotes] and [searchPublicNotes] enforce `isLocked = 0` at the
 * SQL level. Locked notes are NEVER returned by these queries under any circumstances.
 */
@Dao
interface NoteDao {

    /** Returns all unlocked notes ordered by pinned first, then most recent. */
    @Query("SELECT * FROM notes WHERE type = 'NOTE' AND isLocked = 0 ORDER BY isPinned DESC, timestamp DESC")
    fun getPublicNotes(): Flow<List<NoteEntity>>

    /** Returns all journal notes ordered by pinned first, then most recent. */
    @Query("SELECT * FROM notes WHERE type = 'JOURNAL' AND isLocked = 0 ORDER BY isPinned DESC, timestamp DESC")
    fun getJournalNotes(): Flow<List<NoteEntity>>

    /**
     * Returns all locked (vault) notes ordered by pinned first, then most recent.
     * Only call from vault-authenticated screens.
     */
    @Query("SELECT * FROM notes WHERE isLocked = 1 ORDER BY isPinned DESC, timestamp DESC")
    fun getLockedNotes(): Flow<List<NoteEntity>>

    /**
     * Full-text search over title and content.
     */
    @Query(
        """
        SELECT * FROM notes
        WHERE type = 'NOTE' AND isLocked = 0
          AND (title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%')
        ORDER BY isPinned DESC, timestamp DESC
        """,
    )
    fun searchPublicNotes(query: String): Flow<List<NoteEntity>>

    /** Returns all unlocked notes linked to a specific companion. */
    @Query("SELECT * FROM notes WHERE companionId = :companionId AND isLocked = 0 ORDER BY isPinned DESC, timestamp DESC")
    fun getNotesForCompanion(companionId: Long): Flow<List<NoteEntity>>

    /** Single note lookup by id — used by the editor to pre-populate fields. */
    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    suspend fun getNoteById(id: Long): NoteEntity?

    /** Count of all standard notes (including locked ones) — used by Profile stats. */
    @Query("SELECT COUNT(*) FROM notes WHERE type = 'NOTE'")
    fun getPublicNoteCount(): Flow<Int>

    /** Count of all locked/journal notes — used by Profile stats. */
    @Query("SELECT COUNT(*) FROM notes WHERE isLocked = 1 OR type = 'JOURNAL'")
    fun getLockedNoteCount(): Flow<Int>

    /** Insert or update a note. Uses Room's @Upsert (replaces on PK conflict). */
    @Upsert
    suspend fun upsertNote(note: NoteEntity)

    /** Permanently delete a note from the database. */
    @Delete
    suspend fun deleteNote(note: NoteEntity)
    
    /** Returns all notes in the database. Used ONLY for backup/export. */
    @Query("SELECT * FROM notes")
    suspend fun getAllNotesSnapshot(): List<NoteEntity>
}
