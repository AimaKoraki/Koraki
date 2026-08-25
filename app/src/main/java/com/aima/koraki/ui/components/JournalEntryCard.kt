package com.aima.koraki.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.aima.koraki.data.local.entity.NoteEntity
import com.aima.koraki.ui.theme.DarkCrimson
import com.aima.koraki.ui.theme.JournalEntryGradient
import com.aima.koraki.ui.theme.NoteTagBackground
import com.aima.koraki.ui.theme.NoteTagText
import com.aima.koraki.ui.theme.RubyHeart
import com.aima.koraki.ui.theme.SecondaryText
import com.aima.koraki.ui.theme.VaultBorder
import com.aima.koraki.ui.theme.VaultMuted
import com.aima.koraki.ui.theme.VaultText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * A journal entry styled like an open page in a dark leather-bound book.
 * If [note.isPinned] is true, renders with a romantic glowing velvet tone and [RubyHeart] bookmark.
 */
@Composable
fun JournalEntryCard(
    note: NoteEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val timeFormatter = SimpleDateFormat("h:mm a", Locale.getDefault())
    val timeString = timeFormatter.format(Date(note.timestamp))

    // Open page — no clipped box, just a left accent bar and transparent field
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .then(
                if (note.isPinned) {
                    Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Brush.linearGradient(JournalEntryGradient))
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                } else Modifier
            )
            .padding(bottom = 4.dp), // breathing room before the divider
    ) {

        // ── Leather spine margin ──────────────────────────────────────────
        // 3 dp crimson rule + gutter that mimics a page margin
        Box(
            modifier = Modifier
                .width(3.dp)
                .fillMaxHeight()
                .background(if (note.isPinned) RubyHeart else DarkCrimson),
        )

        Spacer(modifier = Modifier.width(16.dp))

        // ── Page content ─────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 4.dp),
        ) {

            // Time + tag badge row (the "margin note")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = timeString,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Serif,
                            fontStyle = FontStyle.Italic,
                            letterSpacing = 0.3.sp,
                        ),
                        color = VaultMuted,
                    )
                    if (note.isPinned) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Love Letter",
                            tint = RubyHeart,
                            modifier = Modifier.size(14.dp),
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    if (note.mood != null) {
                        MoodBadge(moodName = note.mood)
                    }

                    if (note.companionId != null) {
                        Box(
                            modifier = Modifier
                                .background(NoteTagBackground, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                        ) {
                            Text(
                                text = "Linked",
                                style = MaterialTheme.typography.labelSmall,
                                color = NoteTagText,
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title — bold serif, full width
            if (note.title.isNotBlank()) {
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 28.sp,
                    ),
                    color = VaultText,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Body — relaxed-leading serif, no line cap so it reads like a journal page
            if (note.content.isNotBlank()) {
                Text(
                    text = note.content,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = FontFamily.Serif,
                        lineHeight = 26.sp,
                        fontStyle = FontStyle.Italic,
                    ),
                    color = SecondaryText,
                    maxLines = 6,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            // Media strip (images + audio visualizers)
            if (note.images.isNotEmpty() || note.audio.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(note.images) { imagePath ->
                        AsyncImage(
                            model = imagePath,
                            contentDescription = "Attached image",
                            modifier = Modifier
                                .height(80.dp)
                                .width(110.dp)
                                .clip(RoundedCornerShape(4.dp)),
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

            Spacer(modifier = Modifier.height(20.dp))

            // Page-turn rule — the only separator between entries
            HorizontalDivider(
                color = VaultBorder,
                thickness = 1.dp,
            )

            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}
