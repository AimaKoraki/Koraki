package com.aima.koraki.data.repository

import com.aima.koraki.data.local.dao.NoteDao
import com.aima.koraki.data.local.entity.NoteEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository that abstracts the [NoteDao] from the ViewModel layer.
 * All locked/unlocked filtering is enforced in the DAO; this layer
 * only routes calls through.
 */
@Singleton
class NoteRepository @Inject constructor(
    private val noteDao: NoteDao,
) {
    fun getPublicNotes(): Flow<List<NoteEntity>> = noteDao.getPublicNotes()

    fun getJournalNotes(): Flow<List<NoteEntity>> = noteDao.getJournalNotes()

    fun getLockedNotes(): Flow<List<NoteEntity>> = noteDao.getLockedNotes()

    fun searchPublicNotes(query: String): Flow<List<NoteEntity>> =
        noteDao.searchPublicNotes(query)

    fun getNotesForCompanion(companionId: Long): Flow<List<NoteEntity>> =
        noteDao.getNotesForCompanion(companionId)

    suspend fun getNoteById(id: Long): NoteEntity? = noteDao.getNoteById(id)

    fun getPublicNoteCount(): Flow<Int> = noteDao.getPublicNoteCount()

    fun getLockedNoteCount(): Flow<Int> = noteDao.getLockedNoteCount()

    suspend fun upsertNote(note: NoteEntity) = noteDao.upsertNote(note)

    suspend fun deleteNote(note: NoteEntity) = noteDao.deleteNote(note)
}
