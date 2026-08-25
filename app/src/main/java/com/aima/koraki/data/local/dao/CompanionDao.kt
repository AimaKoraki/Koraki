package com.aima.koraki.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.aima.koraki.data.local.entity.CompanionEntity
import kotlinx.coroutines.flow.Flow

/** Data Access Object for companions. */
@Dao
interface CompanionDao {

    /** Returns all companions sorted alphabetically by name. */
    @Query("SELECT * FROM companions ORDER BY name ASC")
    fun getAllCompanions(): Flow<List<CompanionEntity>>

    /** Returns count of companions for profile stats. */
    @Query("SELECT COUNT(*) FROM companions")
    fun getCompanionCount(): Flow<Int>

    /** Single companion lookup by id. */
    @Query("SELECT * FROM companions WHERE id = :id LIMIT 1")
    suspend fun getCompanionById(id: Long): CompanionEntity?

    /** Single companion lookup by name. */
    @Query("SELECT * FROM companions WHERE name = :name LIMIT 1")
    suspend fun getCompanionByName(name: String): CompanionEntity?

    /** Increment affection level by companion id. */
    @Query("UPDATE companions SET affectionLevel = affectionLevel + 1 WHERE id = :id")
    suspend fun incrementAffectionById(id: Long)

    /** Increment affection level by companion name. */
    @Query("UPDATE companions SET affectionLevel = affectionLevel + 1 WHERE name = :name")
    suspend fun incrementAffectionByName(name: String)

    /** Insert or update a companion. */
    @Upsert
    suspend fun upsertCompanion(companion: CompanionEntity)

    /** Permanently delete a companion. */
    @Delete
    suspend fun deleteCompanion(companion: CompanionEntity)

    /** Returns all companions. Used ONLY for backup/export. */
    @Query("SELECT * FROM companions")
    suspend fun getAllCompanionsSnapshot(): List<CompanionEntity>
}
