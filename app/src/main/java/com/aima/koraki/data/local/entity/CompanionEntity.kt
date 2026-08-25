package com.aima.koraki.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a close personal contact ("Companion") in Koraki.
 * Notes can be tagged with a [companionId] to link them to a companion.
 */
@Entity(tableName = "companions")
data class CompanionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val roleOrRelation: String,
    val avatarColorHex: String,
    val bio: String,
    val createdAt: Long,
    val affectionLevel: Int = 0,
)
