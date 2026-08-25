package com.aima.koraki.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.aima.koraki.data.local.dao.CompanionDao
import com.aima.koraki.data.local.dao.NoteDao
import com.aima.koraki.data.local.entity.CompanionEntity
import com.aima.koraki.data.local.entity.NoteEntity

/**
 * The single Room database for Koraki.
 *
 * Increment [version] and provide a [Migration] whenever the schema changes.
 */
@Database(
    entities = [NoteEntity::class, CompanionEntity::class],
    version = 7,
    exportSchema = false,
)
@androidx.room.TypeConverters(Converters::class)
abstract class KorakiDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun companionDao(): CompanionDao

    companion object {
        const val DATABASE_NAME = "koraki_db"
    }
}
