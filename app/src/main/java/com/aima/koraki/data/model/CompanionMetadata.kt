package com.aima.koraki.data.model

import com.google.gson.annotations.SerializedName

/**
 * Animation and behaviour settings for a specific companion sprite.
 * Loaded from `assets/companions/<spriteAsset>/metadata.json`.
 */
data class CompanionMetadata(
    @SerializedName("id") val id: String = "",
    @SerializedName("walkFrames") val walkFrames: List<String> = listOf("walk_0.png", "walk_1.png"),
    @SerializedName("turnFrames") val turnFrames: List<String> = emptyList(),
    @SerializedName("preWalkFrames") val preWalkFrames: List<String> = emptyList(),
    @SerializedName("idleFrames") val idleFrames: List<String> = listOf("idle_0.png"),
    @SerializedName("walkFrameDurationMs") val walkFrameDurationMs: Long = 200L,
    @SerializedName("idleFrameDurationMs") val idleFrameDurationMs: Long = 150L,
    @SerializedName("idleChanceOnEdge") val idleChanceOnEdge: Float = 0.45f,
    @SerializedName("idleMinMs") val idleMinMs: Long = 800L,
    @SerializedName("idleMaxMs") val idleMaxMs: Long = 3500L,
    @SerializedName("walkHoldOnLastFrame") val walkHoldOnLastFrame: Boolean = false,
    /** When true, idle animation always finishes a full cycle before stopping. */
    @SerializedName("idlePlayFullCycle") val idlePlayFullCycle: Boolean = false,
    // --- Structured idle (intro → loop → outro) ---
    // When idleLoopFrames is non-empty this path is used instead of idleFrames/idlePlayFullCycle.
    /** Played exactly once at the very start of idle. */
    @SerializedName("idleIntroFrames") val idleIntroFrames: List<String> = emptyList(),
    /** Loops [idleLoopMinCount]..[idleLoopMaxCount] times as the core idle section. */
    @SerializedName("idleLoopFrames") val idleLoopFrames: List<String> = emptyList(),
    /** Played exactly once at the very end of idle, before resuming walk. */
    @SerializedName("idleOutroFrames") val idleOutroFrames: List<String> = emptyList(),
    /** Minimum number of times [idleLoopFrames] repeats. */
    @SerializedName("idleLoopMinCount") val idleLoopMinCount: Int = 2,
    /** Maximum number of times [idleLoopFrames] repeats. */
    @SerializedName("idleLoopMaxCount") val idleLoopMaxCount: Int = 4,
    /** Walk speed range in dp/s — a random value is picked from this range each session. */
    @SerializedName("walkSpeedMinDpPerSec") val walkSpeedMinDpPerSec: Float = 40f,
    @SerializedName("walkSpeedMaxDpPerSec") val walkSpeedMaxDpPerSec: Float = 80f,
)
