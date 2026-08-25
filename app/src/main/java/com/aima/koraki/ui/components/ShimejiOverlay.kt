package com.aima.koraki.ui.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.aima.koraki.data.model.CompanionMetadata
import com.aima.koraki.data.model.VirtualCompanion
import com.aima.koraki.ui.theme.Red100
import com.aima.koraki.ui.theme.RubyHeart
import com.aima.koraki.ui.theme.SoftRose
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.random.Random

private data class HeartParticle(
    val id: Long,
    var x: Float,
    var y: Float,
    var alpha: Float,
    val size: Float,
    val velocityY: Float,
    val driftX: Float,
    val color: Color,
)

/**
 * Renders a walking sprite companion that bounces left and right across the screen.
 * Tapping the companion triggers upward-floating heart particle animations and haptic feedback.
 *
 * All touch events outside the sprite body pass through cleanly to lists and cards below.
 */
@Composable
fun ShimejiOverlay(
    activeCompanion: VirtualCompanion?,
    modifier: Modifier = Modifier,
    onSpriteTapped: ((String) -> Unit)? = null,
) {
    val spriteSize = 48.dp
    val bandHeight = 72.dp
    val walkSpeedDpPerSec = 60f
    val tickMs = 16L

    if (activeCompanion == null) return

    val context = LocalContext.current
    val density = LocalDensity.current
    val haptic = LocalHapticFeedback.current
    val spriteSizePx = with(density) { spriteSize.toPx() }
    val walkSpeedPxPerMs = with(density) { walkSpeedDpPerSec.dp.toPx() } / 1000f

    var containerWidthPx by remember { mutableFloatStateOf(0f) }
    var xPx by remember { mutableFloatStateOf(0f) }
    var facingRight by remember { mutableStateOf(false) }
    var currentFramePath by remember {
        mutableStateOf("companions/${activeCompanion.spriteAsset}/walk_0.png")
    }

    val heartColors = listOf(RubyHeart, SoftRose, Red100)
    val particles = remember { mutableStateListOf<HeartParticle>() }

    // Particle update tick loop
    LaunchedEffect(particles.size) {
        while (particles.isNotEmpty()) {
            delay(16L)
            val iterator = particles.iterator()
            while (iterator.hasNext()) {
                val p = iterator.next()
                p.y -= p.velocityY
                p.x += p.driftX
                p.alpha -= 0.025f
                if (p.alpha <= 0f) {
                    iterator.remove()
                }
            }
        }
    }

    val bitmap: ImageBitmap? = remember(currentFramePath) {
        runCatching {
            context.assets.open(currentFramePath).use {
                BitmapFactory.decodeStream(it)?.asImageBitmap()
            }
        }.getOrNull()
    }

    LaunchedEffect(activeCompanion.spriteAsset) {
        val meta: CompanionMetadata = withContext(Dispatchers.IO) {
            runCatching {
                context.assets.open("companions/${activeCompanion.spriteAsset}/metadata.json")
                    .use { Gson().fromJson(it.bufferedReader(), CompanionMetadata::class.java) }
            }.getOrElse { CompanionMetadata() }
        }

        val base = "companions/${activeCompanion.spriteAsset}"
        fun walkPath(frame: String) = "$base/$frame"

        suspend fun playTurnFrames() {
            for (frame in meta.turnFrames) {
                currentFramePath = walkPath(frame)
                delay(meta.walkFrameDurationMs)
            }
        }

        suspend fun playIdleLoop() {
            if (meta.idleLoopFrames.isNotEmpty()) {
                val loopCount = Random.nextInt(meta.idleLoopMinCount, meta.idleLoopMaxCount + 1)
                for (frame in meta.idleIntroFrames) {
                    currentFramePath = walkPath(frame)
                    delay(meta.idleFrameDurationMs)
                }
                repeat(loopCount) {
                    for (frame in meta.idleLoopFrames) {
                        currentFramePath = walkPath(frame)
                        delay(meta.idleFrameDurationMs)
                    }
                }
                for (frame in meta.idleOutroFrames) {
                    currentFramePath = walkPath(frame)
                    delay(meta.idleFrameDurationMs)
                }
            } else if (meta.idlePlayFullCycle) {
                if (meta.idleFrames.isEmpty()) return
                val idleEnd = System.currentTimeMillis() + Random.nextLong(meta.idleMinMs, meta.idleMaxMs)
                do {
                    for (frame in meta.idleFrames) {
                        currentFramePath = walkPath(frame)
                        delay(meta.idleFrameDurationMs)
                    }
                } while (System.currentTimeMillis() < idleEnd)
            } else {
                if (meta.idleFrames.isEmpty()) return
                val idleEnd = System.currentTimeMillis() + Random.nextLong(meta.idleMinMs, meta.idleMaxMs)
                var idx = 0
                while (System.currentTimeMillis() < idleEnd) {
                    currentFramePath = walkPath(meta.idleFrames[idx % meta.idleFrames.size])
                    delay(meta.idleFrameDurationMs)
                    idx++
                }
            }
        }

        currentFramePath = walkPath(meta.walkFrames[0])
        var walkFrameIdx = 0
        var walkFrameTimer = 0L

        while (true) {
            delay(tickMs)
            val travel = (containerWidthPx - spriteSizePx).coerceAtLeast(0f)
            if (travel == 0f) continue

            val step = walkSpeedPxPerMs * tickMs
            xPx = (xPx + (if (facingRight) step else -step)).coerceIn(0f, travel)

            walkFrameTimer += tickMs
            if (walkFrameTimer >= meta.walkFrameDurationMs) {
                walkFrameTimer = 0L
                walkFrameIdx = if (meta.walkHoldOnLastFrame) {
                    (walkFrameIdx + 1).coerceAtMost(meta.walkFrames.size - 1)
                } else {
                    (walkFrameIdx + 1) % meta.walkFrames.size
                }
                currentFramePath = walkPath(meta.walkFrames[walkFrameIdx])
            }

            if (xPx <= 0f || xPx >= travel) {
                val willIdle = meta.idleFrames.isNotEmpty() &&
                    Random.nextFloat() < meta.idleChanceOnEdge

                if (willIdle && meta.preWalkFrames.isNotEmpty()) {
                    for (frame in meta.preWalkFrames) {
                        currentFramePath = walkPath(frame)
                        delay(meta.walkFrameDurationMs)
                    }
                } else if (meta.turnFrames.isNotEmpty()) {
                    playTurnFrames()
                }

                facingRight = !facingRight

                if (willIdle) {
                    playIdleLoop()
                    if (meta.preWalkFrames.isNotEmpty()) {
                        for (frame in meta.preWalkFrames.reversed()) {
                            currentFramePath = walkPath(frame)
                            delay(meta.walkFrameDurationMs)
                        }
                    }
                }

                walkFrameIdx = 0
                walkFrameTimer = 0L
                currentFramePath = walkPath(meta.walkFrames[0])
            }
        }
    }

    if (bitmap == null) return

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(bandHeight)
            .onSizeChanged { containerWidthPx = it.width.toFloat() },
    ) {
        // Single-pass Canvas for heart particles across the overlay
        if (particles.isNotEmpty()) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                particles.forEach { p ->
                    val s = p.size
                    val half = s / 2f
                    val path = Path().apply {
                        moveTo(p.x, p.y + half * 0.4f)
                        cubicTo(p.x - half, p.y - half * 0.6f, p.x - half * 1.2f, p.y + half * 0.6f, p.x, p.y + s)
                        cubicTo(p.x + half * 1.2f, p.y + half * 0.6f, p.x + half, p.y - half * 0.6f, p.x, p.y + half * 0.4f)
                        close()
                    }
                    drawPath(path = path, color = p.color.copy(alpha = p.alpha.coerceIn(0f, 1f)))
                }
            }
        }

        // Sprite image with touch handler isolated strictly to sprite bounds
        Image(
            bitmap = bitmap,
            contentDescription = "${activeCompanion.name} sprite",
            modifier = Modifier
                .size(spriteSize)
                .offset { IntOffset(xPx.toInt(), 16.dp.roundToPx()) }
                .graphicsLayer {
                    scaleX = if (facingRight) -1f else 1f
                }
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onSpriteTapped?.invoke(activeCompanion.name)

                    // Spawn 3 floating hearts
                    val centerX = xPx + spriteSizePx / 2f
                    val startY = 32f
                    repeat(3) {
                        particles.add(
                            HeartParticle(
                                id = System.nanoTime() + it,
                                x = centerX + Random.nextFloat() * 24f - 12f,
                                y = startY,
                                alpha = 1f,
                                size = Random.nextFloat() * 10f + 12f,
                                velocityY = Random.nextFloat() * 1.8f + 1.2f,
                                driftX = Random.nextFloat() * 1.2f - 0.6f,
                                color = heartColors.random(),
                            )
                        )
                    }
                },
        )
    }
}

