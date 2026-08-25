package com.aima.koraki.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aima.koraki.data.local.entity.NoteMood
import com.aima.koraki.ui.theme.BlackRose
import com.aima.koraki.ui.theme.BorderSubtle
import com.aima.koraki.ui.theme.Burgundy
import com.aima.koraki.ui.theme.CrimsonRose
import com.aima.koraki.ui.theme.DeepWine
import com.aima.koraki.ui.theme.ElevatedSurface
import com.aima.koraki.ui.theme.GlowGeneral
import com.aima.koraki.ui.theme.MutedText
import com.aima.koraki.ui.theme.Red200
import com.aima.koraki.ui.theme.RubyHeart
import com.aima.koraki.ui.theme.SoftRose
import com.aima.koraki.ui.theme.TrueCrimson

/**
 * Styling definition for each romantic mood tag.
 */
data class MoodColorStyle(
    val selectedBg: Color,
    val selectedBorder: Color,
    val selectedText: Color,
    val badgeBg: Color,
    val badgeBorder: Color,
    val badgeText: Color,
)

fun NoteMood.getColorStyle(): MoodColorStyle {
    return when (this) {
        NoteMood.ROMANTIC -> MoodColorStyle(
            selectedBg = DeepWine,
            selectedBorder = TrueCrimson,
            selectedText = SoftRose,
            badgeBg = DeepWine.copy(alpha = 0.85f),
            badgeBorder = TrueCrimson.copy(alpha = 0.7f),
            badgeText = SoftRose,
        )
        NoteMood.DEEP_THOUGHTS -> MoodColorStyle(
            selectedBg = DeepWine,
            selectedBorder = CrimsonRose,
            selectedText = CrimsonRose,
            badgeBg = DeepWine.copy(alpha = 0.85f),
            badgeBorder = CrimsonRose.copy(alpha = 0.7f),
            badgeText = CrimsonRose,
        )
        NoteMood.VENTING -> MoodColorStyle(
            selectedBg = BlackRose,
            selectedBorder = Burgundy,
            selectedText = SoftRose.copy(alpha = 0.8f),
            badgeBg = BlackRose.copy(alpha = 0.85f),
            badgeBorder = Burgundy.copy(alpha = 0.7f),
            badgeText = SoftRose.copy(alpha = 0.8f),
        )
        NoteMood.JOY -> MoodColorStyle(
            selectedBg = DeepWine,
            selectedBorder = RubyHeart,
            selectedText = Red200,
            badgeBg = DeepWine.copy(alpha = 0.85f),
            badgeBorder = RubyHeart.copy(alpha = 0.7f),
            badgeText = Red200,
        )
    }
}

/**
 * Inline Mood Selector chip bar for Note & Journal editors.
 */
@Composable
fun MoodSelector(
    selectedMood: String?,
    onMoodSelect: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .horizontalScroll(scrollState)
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NoteMood.entries.forEach { mood ->
            val isSelected = selectedMood.equals(mood.name, ignoreCase = true) ||
                selectedMood.equals(mood.label, ignoreCase = true)
            val style = mood.getColorStyle()

            val bgColor by animateColorAsState(
                targetValue = if (isSelected) style.selectedBg else ElevatedSurface,
                label = "mood_bg",
            )
            val borderColor by animateColorAsState(
                targetValue = if (isSelected) style.selectedBorder else BorderSubtle,
                label = "mood_border",
            )
            val textColor by animateColorAsState(
                targetValue = if (isSelected) style.selectedText else MutedText,
                label = "mood_text",
            )
            val elevation by animateDpAsState(
                targetValue = if (isSelected) 6.dp else 0.dp,
                label = "mood_elevation",
            )

            val shape = RoundedCornerShape(12.dp)

            Box(
                modifier = Modifier
                    .then(
                        if (isSelected) {
                            Modifier.shadow(
                                elevation = elevation,
                                shape = shape,
                                ambientColor = GlowGeneral,
                                spotColor = style.selectedBorder,
                            )
                        } else Modifier,
                    )
                    .clip(shape)
                    .background(bgColor)
                    .border(if (isSelected) 1.5.dp else 1.dp, borderColor, shape)
                    .clickable {
                        // Toggle: clicking active mood clears it, clicking another selects it
                        if (isSelected) {
                            onMoodSelect(null)
                        } else {
                            onMoodSelect(mood.name)
                        }
                    }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = mood.emoji,
                        fontSize = 15.sp,
                    )
                    Text(
                        text = mood.label,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        ),
                        color = textColor,
                    )
                }
            }
        }
    }
}

/**
 * Compact mood badge pill displayed on Note & Journal cards.
 */
@Composable
fun MoodBadge(
    moodName: String,
    modifier: Modifier = Modifier,
) {
    val mood = NoteMood.fromString(moodName) ?: return
    val style = mood.getColorStyle()
    val shape = RoundedCornerShape(8.dp)

    Box(
        modifier = modifier
            .clip(shape)
            .background(style.badgeBg)
            .border(1.dp, style.badgeBorder, shape)
            .padding(horizontal = 7.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = mood.emoji,
                fontSize = 12.sp,
            )
            Text(
                text = mood.label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                color = style.badgeText,
                maxLines = 1,
                softWrap = false,
            )
        }
    }
}
