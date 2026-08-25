package com.aima.koraki.ui.vault

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aima.koraki.data.local.entity.NoteEntity
import com.aima.koraki.ui.components.MoodBadge
import com.aima.koraki.ui.theme.BrightCrimson
import com.aima.koraki.ui.theme.MutedText
import com.aima.koraki.ui.theme.NoteBodyText
import com.aima.koraki.ui.theme.NoteTitleText
import com.aima.koraki.ui.theme.PrimaryCrimson
import com.aima.koraki.ui.theme.TextOnRed
import com.aima.koraki.ui.theme.VaultBackground
import com.aima.koraki.ui.theme.VaultBorder
import com.aima.koraki.ui.theme.VaultElevated
import com.aima.koraki.ui.theme.VaultMuted
import com.aima.koraki.ui.theme.VaultSurface
import com.aima.koraki.ui.theme.VaultTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val vaultDateFmt = SimpleDateFormat("MMM d", Locale.getDefault())

/** Estimates reading time — minimum 1 min. */
private fun vaultReadMin(content: String): Int {
    if (content.isBlank()) return 1
    val words = content.trim().split("\\s+".toRegex()).count { it.isNotBlank() }
    return maxOf(1, (words + 199) / 200)
}

/**
 * Stealth Vault / Journaling screen displaying only locked notes.
 *
 * Entry is triggered stealthily from [NotesScreen] by typing the stored passcode.
 * There is no visible link anywhere in the app.
 *
 * @param onNoteClick     Navigate to editor for a locked note.
 * @param onNewLockedNote Create a new note pre-marked as locked.
 * @param onBack          Pop back to Notes screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultScreen(
    onNoteClick: (Long) -> Unit,
    onNewLockedNote: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: VaultViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    VaultTheme {
        Scaffold(
            modifier = modifier,
            containerColor = VaultBackground,
            topBar = {
                Column {
                    // ── Header ──────────────────────────────────────────────
                    TopAppBar(
                        title = {
                            Text(
                                text = "Journaling Vault",
                                style = TextStyle(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 30.sp,
                                    lineHeight = 36.sp,
                                    letterSpacing = 0.sp,
                                ),
                                color = BrightCrimson,       // #D72D48
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier.size(28.dp),
                                )
                            }
                        },
                        actions = {
                            // Lock icon — right side, reinforces private area
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Private vault",
                                tint = BrightCrimson,        // #D72D48
                                modifier = Modifier
                                    .padding(end = 16.dp)
                                    .size(22.dp),
                            )
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = VaultBackground,
                        ),
                    )

                    // ── Writing Prompt Banner ────────────────────────────────
                    WritingPromptBanner()
                }
            },
            floatingActionButton = {
                // Subtle glow — rgba(181,30,53,0.20)
                Box(
                    modifier = Modifier.shadow(
                        elevation = 14.dp,
                        shape = RoundedCornerShape(18.dp),
                        ambientColor = PrimaryCrimson.copy(alpha = 0.20f),
                        spotColor = PrimaryCrimson.copy(alpha = 0.20f),
                    ),
                ) {
                    FloatingActionButton(
                        onClick = onNewLockedNote,
                        modifier = Modifier.size(56.dp),
                        shape = RoundedCornerShape(18.dp),
                        containerColor = PrimaryCrimson,     // #B51E35
                        contentColor = TextOnRed,            // #FFFFFF
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "New journal entry",
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }
            },
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                if (uiState.notes.isEmpty() && !uiState.isLoading) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(horizontal = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = VaultMuted,
                            modifier = Modifier.size(36.dp),
                        )
                        Text(
                            text = "Your vault is empty.",
                            style = TextStyle(
                                fontWeight = FontWeight.Normal,
                                fontSize = 15.sp,
                                letterSpacing = 0.25.sp,
                            ),
                            color = VaultMuted,
                        )
                    }
                } else {
                    // ── Masonry staggered grid — 2 columns, natural heights ──
                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 12.dp,
                            bottom = 96.dp,   // FAB clearance
                        ),
                        verticalItemSpacing = 16.dp,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        items(uiState.notes, key = { it.id }) { note ->
                            VaultJournalCard(
                                note = note,
                                onPinToggle = { viewModel.togglePin(note) },
                                onClick = { onNoteClick(note.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Writing Prompt Banner
// ─────────────────────────────────────────────────────────────────────────────

/**
 * A soft, encouraging strip just below the header.
 * Invites the user to write — not a security notice.
 *
 * Height: ~64dp / Background: VaultElevated (#211116) / Border bottom: 1dp VaultBorder (#352126)
 */
@Composable
private fun WritingPromptBanner() {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(VaultElevated)                   // #211116 — slightly lifted
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = null,
                tint = MutedText,                            // #77686C — soft, not alarming
                modifier = Modifier.size(16.dp),
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "What's in your mind tonight?",
                    style = TextStyle(
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        letterSpacing = 0.1.sp,
                    ),
                    color = NoteBodyText,                    // #B9A6AA
                )
                Text(
                    text = "Tap + to begin a new entry.",
                    style = TextStyle(
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        letterSpacing = 0.25.sp,
                    ),
                    color = MutedText,                      // #77686C
                )
            }
        }
        // Bottom divider
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(VaultBorder),                   // #352126
        )
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// Vault Journal Card
// ─────────────────────────────────────────────────────────────────────────────

/**
 * A masonry card purpose-built for the Journaling Vault.
 *
 * Visual spec:
 *   - Background: VaultSurface #151012
 *   - Border: 1dp VaultBorder #352126
 *   - Title: #F5EDEF / 20sp / SemiBold
 *   - Preview: #B9A6AA / 15sp / Normal — max 5 lines, calmer than Notes screen
 *   - Metadata: "◷ Aug 25 · 1 min read" / 12sp / Medium / #77686C
 *   - Bookmark top-right: unpinned #77686C, pinned #D72D48 filled
 *   - Crimson left bar for locked entries
 *   - Natural intrinsic height — no forced equalisation
 *
 * @param note        The journal entry to display.
 * @param onPinToggle Called when the bookmark icon is tapped.
 * @param onClick     Called when the card body is tapped.
 */
@Composable
private fun VaultJournalCard(
    note: NoteEntity,
    onPinToggle: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = VaultSurface,                   // #151012
            contentColor = NoteTitleText,                    // #F5EDEF
        ),
        border = BorderStroke(1.dp, VaultBorder),            // #352126
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            // 4dp crimson left bar — locked entry indicator
            if (note.isLocked) {
                Spacer(
                    modifier = Modifier
                        .width(4.dp)
                        .fillMaxHeight()
                        .background(BrightCrimson),          // #D72D48
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 10.dp, top = 14.dp, bottom = 14.dp),
            ) {
                // ── Title row with bookmark icon ─────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                ) {
                    if (note.title.isNotBlank()) {
                        Text(
                            text = note.title,
                            style = TextStyle(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 20.sp,
                                lineHeight = 26.sp,
                                letterSpacing = 0.15.sp,
                            ),
                            color = NoteTitleText,           // #F5EDEF
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }

                    // Bookmark — top-right of every card
                    IconButton(
                        onClick = onPinToggle,
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(
                            imageVector = if (note.isPinned) Icons.Default.Bookmark
                            else Icons.Default.BookmarkBorder,
                            contentDescription = if (note.isPinned) "Unpin entry" else "Pin entry",
                            tint = if (note.isPinned) BrightCrimson  // #D72D48
                            else MutedText,                           // #77686C
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }

                // ── Preview body ─────────────────────────────────────────
                if (note.content.isNotBlank()) {
                    if (note.title.isNotBlank()) Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = note.content,
                        style = TextStyle(
                            fontWeight = FontWeight.Normal,
                            fontSize = 15.sp,
                            lineHeight = 22.sp,
                            letterSpacing = 0.25.sp,
                        ),
                        color = NoteBodyText,                // #B9A6AA
                        maxLines = 5,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                // ── Mood & Metadata — bottom of every card ─────────────────────
                if (note.mood != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    MoodBadge(moodName = note.mood)
                }

                Spacer(modifier = Modifier.height(8.dp))
                val dateStr = vaultDateFmt.format(Date(note.timestamp))
                val readMin = if (note.content.isNotBlank()) vaultReadMin(note.content) else 1
                Text(
                    text = "◷ $dateStr · $readMin min read",
                    style = TextStyle(
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        letterSpacing = 0.5.sp,
                    ),
                    color = MutedText,                       // #77686C
                )
            }
        }
    }
}

