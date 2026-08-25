package com.aima.koraki.util

/**
 * Level milestone details and XP threshold progression.
 */
data class LevelMilestone(
    val level: Int,
    val title: String,
    val requiredXp: Int,
    val unlockDescription: String,
)

object LevelManager {
    val MILESTONES = listOf(
        LevelMilestone(1, "Budding Romance", 0, "Birdy (Default Companion)"),
        LevelMilestone(2, "Velvet Whisper", 200, "Casper Companion"),
        LevelMilestone(3, "Crimson Heartbeat", 500, "Niffty Companion"),
        LevelMilestone(4, "Devoted Quill", 900, "Custom Card Tinting"),
        LevelMilestone(5, "Soulbound Flame", 1400, "Obsidian Journal Theme"),
    )

    /** Returns the current [LevelMilestone] based on total XP. */
    fun getMilestone(totalXp: Int): LevelMilestone {
        return MILESTONES.lastOrNull { totalXp >= it.requiredXp } ?: MILESTONES.first()
    }

    /** Returns the current level number (1..5). */
    fun getLevel(totalXp: Int): Int = getMilestone(totalXp).level

    /** Returns the romantic level title (e.g. "Velvet Whisper"). */
    fun getLevelTitle(totalXp: Int): String = getMilestone(totalXp).title

    /** Returns the next milestone, or null if max level reached. */
    fun getNextMilestone(totalXp: Int): LevelMilestone? {
        val current = getMilestone(totalXp)
        return MILESTONES.firstOrNull { it.level > current.level }
    }

    /**
     * Calculates the fraction (0f..1f) of progress toward the next level milestone.
     */
    fun getLevelProgress(totalXp: Int): Float {
        val current = getMilestone(totalXp)
        val next = getNextMilestone(totalXp) ?: return 1.0f

        val xpInCurrentLevel = totalXp - current.requiredXp
        val xpNeededForNext = next.requiredXp - current.requiredXp

        return (xpInCurrentLevel.toFloat() / xpNeededForNext.toFloat()).coerceIn(0f, 1f)
    }

    /**
     * Returns true if adding XP causes the user to cross a level boundary.
     */
    fun isLevelUp(oldXp: Int, newXp: Int): Boolean {
        return getLevel(newXp) > getLevel(oldXp)
    }
}
