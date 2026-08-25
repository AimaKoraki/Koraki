package com.aima.koraki.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.aima.koraki.data.local.entity.NoteEntity
import com.aima.koraki.ui.theme.BorderCrimson
import com.aima.koraki.ui.theme.BorderSubtle
import com.aima.koraki.ui.theme.Borders
import com.aima.koraki.ui.theme.ElevatedSurface
import com.aima.koraki.ui.theme.GlowGeneral
import com.aima.koraki.ui.theme.JournalEntryGradient
import com.aima.koraki.ui.theme.NoteBackground
import com.aima.koraki.ui.theme.NoteBodyText
import com.aima.koraki.ui.theme.NoteTimestampText
import com.aima.koraki.ui.theme.NoteTitleText
import com.aima.koraki.ui.theme.PrimaryCrimson
import com.aima.koraki.ui.theme.RubyHeart
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val dateFormatter = SimpleDateFormat("MMM d", Locale.getDefault())

/** Estimates reading time in minutes based on ~200 words/min average. Minimum 1 min. */
private fun estimateReadMinutes(content: String): Int {
    if (content.isBlank()) return 1
    val wordCount = content.trim().split("\\s+".toRegex()).count { it.isNotBlank() }
    return maxOf(1, (wordCount + 199) / 200)
}

/**
 * A staggered-grid card that displays a [NoteEntity].
 *
 * If [note.isPinned] is true ("Love Letter" / Special entry):
 *   - Gradient background: [JournalEntryGradient] (#1A080D → #2A0B12)
 *   - Border: [BorderCrimson]
 *   - Glowing [RubyHeart] icon badge top-right
 */
@Composable
fun NoteCard(
    note: NoteEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cardBorder = if (note.isPinned) BorderStroke(1.dp, BorderCrimson) else BorderStroke(1.dp, Borders)
    val cardGlow = if (note.isPinned) {
        Modifier.shadow(
            elevation = 10.dp,
            shape = RoundedCornerShape(12.dp),
            ambientColor = GlowGeneral,
            spotColor = RubyHeart,
        )
    } else Modifier

    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .then(cardGlow),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (note.isPinned) Color.Transparent else NoteBackground,
            contentColor = NoteTitleText,
        ),
        border = cardBorder,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (note.isPinned) Modifier.background(Brush.linearGradient(JournalEntryGradient))
                    else Modifier.background(NoteBackground)
                )
        ) {
            Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                // Thin crimson left bar — visual indicator for locked notes
                if (note.isLocked) {
                    Spacer(
                        modifier = Modifier
                            .width(4.dp)
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                ) {
                    // Header row with title and optional heart badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top,
                    ) {
                        if (note.title.isNotBlank()) {
                            Text(
                                text = note.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = NoteTitleText,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f),
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }

                        if (note.isPinned) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "Special entry",
                                tint = RubyHeart,
                                modifier = Modifier
                                    .padding(start = 6.dp)
                                    .size(16.dp),
                            )
                        }
                    }

                    if (note.title.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    if (note.content.isNotBlank()) {
                        Text(
                            text = note.content,
                            style = MaterialTheme.typography.bodyMedium,
                            color = NoteBodyText,
                            maxLines = 6,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // ── Media strip — images then audio clips ────────────────
                    if (note.images.isNotEmpty() || note.audio.isNotEmpty()) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(bottom = 10.dp),
                        ) {
                            items(note.images.size) { index ->
                                AsyncImage(
                                    model = note.images[index],
                                    contentDescription = "Attached image",
                                    modifier = Modifier
                                        .height(72.dp)
                                        .width(100.dp)
                                        .clip(RoundedCornerShape(6.dp)),
                                    contentScale = ContentScale.Crop,
                                )
                            }
                            items(note.audio.size) { _ ->
                                VelvetAudioWaveform(
                                    isPlaying = false,
                                    onPlayClick = onClick,
                                )
                            }
                        }
                    }

                    if (note.mood != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        MoodBadge(moodName = note.mood)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val dateStr = dateFormatter.format(Date(note.timestamp))
                    val readMin = if (note.content.isNotBlank()) estimateReadMinutes(note.content) else 1
                    Text(
                        text = "$dateStr · $readMin min read",
                        style = MaterialTheme.typography.labelSmall,
                        color = NoteTimestampText,
                    )
                }
            }
        }
    }
}


