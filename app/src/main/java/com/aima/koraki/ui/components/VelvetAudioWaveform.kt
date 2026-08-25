package com.aima.koraki.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aima.koraki.ui.theme.BorderSubtle
import com.aima.koraki.ui.theme.ElevatedSurface
import com.aima.koraki.ui.theme.PrimaryCrimson
import com.aima.koraki.ui.theme.Red300
import com.aima.koraki.ui.theme.RubyGradient
import com.aima.koraki.ui.theme.TextMuted
import com.aima.koraki.ui.theme.TextOnRed
import com.aima.koraki.ui.theme.TextSecondary

/**
 * Velvet Audio Waveform component.
 * Renders an animated amplitude waveform bar visualizer styled with [RubyGradient].
 *
 * @param isPlaying Whether this audio clip is currently actively playing.
 * @param onPlayClick Toggles playback.
 * @param onDeleteClick Optional callback to delete the audio file.
 */
@Composable
fun VelvetAudioWaveform(
    isPlaying: Boolean,
    onPlayClick: () -> Unit,
    modifier: Modifier = Modifier,
    onDeleteClick: (() -> Unit)? = null,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "audio_wave")

    // Heights multipliers for 12 waveform bars
    val waveAnim1 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "wave_1",
    )
    val waveAnim2 by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(550, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "wave_2",
    )
    val waveAnim3 by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(350, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "wave_3",
    )

    val baseHeights = listOf(0.4f, 0.7f, 0.3f, 0.9f, 0.6f, 0.85f, 0.45f, 0.75f, 0.35f, 0.95f, 0.5f, 0.65f)

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(ElevatedSurface)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Play / Pause round button
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(RubyGradient))
                .clickable(onClick = onPlayClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                contentDescription = if (isPlaying) "Stop audio" else "Play audio",
                tint = TextOnRed,
                modifier = Modifier.size(20.dp),
            )
        }

        // Waveform bars
        Row(
            modifier = Modifier
                .height(28.dp)
                .weight(1f, fill = false),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            baseHeights.forEachIndexed { index, baseFraction ->
                val dynamicFraction = if (isPlaying) {
                    when (index % 3) {
                        0 -> (baseFraction * waveAnim1).coerceIn(0.2f, 1f)
                        1 -> (baseFraction * waveAnim2).coerceIn(0.2f, 1f)
                        else -> (baseFraction * waveAnim3).coerceIn(0.2f, 1f)
                    }
                } else {
                    baseFraction
                }

                Box(
                    modifier = Modifier
                        .width(3.5.dp)
                        .fillMaxHeight(dynamicFraction)
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            if (isPlaying) Brush.verticalGradient(RubyGradient)
                            else Brush.verticalGradient(listOf(Red300.copy(alpha = 0.5f), BorderSubtle))
                        ),
                )
            }
        }

        if (onDeleteClick != null) {
            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier.size(24.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove audio",
                    tint = TextMuted,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}
