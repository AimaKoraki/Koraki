package com.aima.koraki.data.repository

import android.content.Context
import android.net.Uri
import com.aima.koraki.data.local.dao.CompanionDao
import com.aima.koraki.data.local.dao.NoteDao
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupRepository @Inject constructor(
    private val noteDao: NoteDao,
    private val companionDao: CompanionDao,
    @ApplicationContext private val context: Context
) {
    private val gson = Gson()

    /**
     * Exports the entire database to the specified [Uri] (typically selected via Storage Access Framework).
     * 
     * TODO: Upgrade this backup system to create a `.zip` archive containing the JSON data 
     * PLUS the actual media files from `filesDir/images` and `filesDir/audio`. Currently, 
     * this only backs up text and media file paths.
     */
    suspend fun exportDatabaseToUri(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val notes = noteDao.getAllNotesSnapshot()
            val companions = companionDao.getAllCompanionsSnapshot()
            
            val backup = KorakiBackup(
                exportDateMs = System.currentTimeMillis(),
                notes = notes,
                companions = companions
            )
            
            val jsonString = gson.toJson(backup)
            
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(jsonString.toByteArray(Charsets.UTF_8))
            } ?: throw IllegalStateException("Could not open output stream for URI")
            
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Imports a JSON backup from the specified [Uri] and merges it into the database.
     */
    suspend fun importDatabaseFromUri(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val jsonString = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.bufferedReader(Charsets.UTF_8).readText()
            } ?: throw IllegalStateException("Could not open input stream for URI")

            val backup = gson.fromJson(jsonString, KorakiBackup::class.java)

            // Merge companions
            backup.companions.forEach { companion ->
                companionDao.upsertCompanion(companion)
            }

            // Merge notes
            backup.notes.forEach { note ->
                noteDao.upsertNote(note)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}
