package com.aima.koraki.data.repository

import com.aima.koraki.data.local.dao.CompanionDao
import com.aima.koraki.data.local.entity.CompanionEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/** Repository that abstracts [CompanionDao] from the ViewModel layer. */
@Singleton
class CompanionRepository @Inject constructor(
    private val companionDao: CompanionDao,
) {
    fun getAllCompanions(): Flow<List<CompanionEntity>> = companionDao.getAllCompanions()

    fun getCompanionCount(): Flow<Int> = companionDao.getCompanionCount()

    suspend fun getCompanionById(id: Long): CompanionEntity? = companionDao.getCompanionById(id)

    suspend fun getCompanionByName(name: String): CompanionEntity? = companionDao.getCompanionByName(name)

    suspend fun incrementAffectionById(id: Long) = companionDao.incrementAffectionById(id)

    suspend fun incrementAffectionByName(name: String) = companionDao.incrementAffectionByName(name)

    suspend fun upsertCompanion(companion: CompanionEntity) =
        companionDao.upsertCompanion(companion)

    suspend fun deleteCompanion(companion: CompanionEntity) =
        companionDao.deleteCompanion(companion)
}
