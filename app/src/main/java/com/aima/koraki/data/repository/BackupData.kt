package com.aima.koraki.data.repository

import com.aima.koraki.data.local.entity.CompanionEntity
import com.aima.koraki.data.local.entity.NoteEntity

/**
 * Data class representing the entire database snapshot.
 */
data class KorakiBackup(
    val exportDateMs: Long = System.currentTimeMillis(),
    val notes: List<NoteEntity> = emptyList(),
    val companions: List<CompanionEntity> = emptyList()
)
