package com.aima.koraki.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aima.koraki.ui.theme.CrimsonGradient
import com.aima.koraki.ui.theme.GlowStrong
import com.aima.koraki.ui.theme.RubyHeart
import com.aima.koraki.ui.theme.TextOnRed
import com.aima.koraki.ui.theme.TextPrimary
import com.aima.koraki.ui.theme.TextSecondary
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

private val ROMANTIC_PROMPTS = listOf(
    "What made your heart smile today?",
    "A quiet moment I cherish with you…",
    "What is your favorite memory of us?",
    "A thought I wanted to whisper to you…",
    "Three words that describe us right now.",
    "The sweetest part of my day with you was…",
    "Something new I learned to adore about you.",
)

/**
 * Calculates days together using timezone-safe [java.time.LocalDate] arithmetic.
 */
fun calculateDaysTogether(anniversaryTimestampMs: Long?): Long? {
    if (anniversaryTimestampMs == null || anniversaryTimestampMs <= 0) return null
    return runCatching {
        val startDate = Instant.ofEpochMilli(anniversaryTimestampMs)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        val today = LocalDate.now(ZoneId.systemDefault())
        ChronoUnit.DAYS.between(startDate, today).coerceAtLeast(0) + 1
    }.getOrNull()
}

/**
 * Romantic Anniversary & Daily Prompt Banner.
 * Styled with [CrimsonGradient] and subtle romantic glow.
 *
 * @param anniversaryTimestampMs The anniversary start date timestamp in milliseconds.
 * @param modifier Layout modifier.
 */
@Composable
fun AnniversaryBanner(
    anniversaryTimestampMs: Long?,
    modifier: Modifier = Modifier,
) {
    val daysTogether = remember(anniversaryTimestampMs) {
        calculateDaysTogether(anniversaryTimestampMs)
    }

    // Pick daily prompt based on day-of-year
    val dailyPrompt = remember {
        val dayOfYear = LocalDate.now(ZoneId.systemDefault()).dayOfYear
        ROMANTIC_PROMPTS[dayOfYear % ROMANTIC_PROMPTS.size]
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = GlowStrong,
                spotColor = RubyHeart,
            )
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.linearGradient(CrimsonGradient))
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Heart badge
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(RubyHeart.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = TextOnRed,
                    modifier = Modifier.size(22.dp),
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                if (daysTogether != null) {
                    Text(
                        text = "Day $daysTogether Together ❤️",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.2.sp,
                        ),
                        color = TextOnRed,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                } else {
                    Text(
                        text = "Love Notes & Memories 🌹",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                        color = TextOnRed,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                }

                Text(
                    text = dailyPrompt,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Serif,
                        fontStyle = FontStyle.Italic,
                    ),
                    color = TextPrimary.copy(alpha = 0.9f),
                )
            }
        }
    }
}
