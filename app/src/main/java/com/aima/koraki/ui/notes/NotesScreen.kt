package com.aima.koraki.ui.notes

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aima.koraki.data.local.entity.NoteEntity
import com.aima.koraki.ui.components.AnniversaryBanner
import com.aima.koraki.ui.components.JournalEntryCard
import com.aima.koraki.ui.components.NoteCard
import com.aima.koraki.ui.theme.AmbientGlow
import com.aima.koraki.ui.theme.Borders
import com.aima.koraki.ui.theme.BrightCrimson
import com.aima.koraki.ui.theme.MutedText
import com.aima.koraki.ui.theme.NoteBackground
import com.aima.koraki.ui.theme.PrimaryCrimson
import com.aima.koraki.ui.theme.PrimaryText
import com.aima.koraki.ui.theme.SecondaryText
import com.aima.koraki.ui.theme.TextOnRed
import com.aima.koraki.ui.theme.VaultAccent
import com.aima.koraki.ui.theme.VaultActive
import com.aima.koraki.ui.theme.VaultBackground
import com.aima.koraki.ui.theme.VaultBorder
import com.aima.koraki.ui.theme.VaultMuted
import com.aima.koraki.ui.theme.VaultText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Public notes screen with inline Stealth Journaling Page.
 *
 * @param onNoteClick Navigate to editor for an existing note.
 * @param onNewNote   Navigate to editor for a new note.
 * @param onJournalEntryClick Navigate to journal editor for an existing entry.
 * @param onNewJournalEntry Navigate to journal editor for a new entry.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    onNoteClick: (Long) -> Unit,
    onNewNote: () -> Unit,
    onJournalEntryClick: (Long) -> Unit,
    onNewJournalEntry: () -> Unit,
    onOpenVault: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NotesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is NotesEvent.OpenVault -> {
                    onOpenVault()
                }
                is NotesEvent.TriggerHaptic -> {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }
            }
        }
    }

    // The entire Scaffold swaps to the journal dark palette when journal is active
    val scaffoldBackground = if (uiState.isJournalUnlocked) VaultBackground
    else MaterialTheme.colorScheme.background

    Scaffold(
        modifier = modifier,
        containerColor = scaffoldBackground,
        floatingActionButton = {
            if (uiState.isJournalUnlocked) {
                FloatingActionButton(
                    onClick = onNewJournalEntry,
                    containerColor = VaultAccent,
                    contentColor = VaultActive,
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "New journal entry")
                }
            } else {
                // Crimson-glowing FAB: use shadow() with spotColor for the subtle glow
                FloatingActionButton(
                    onClick = onNewNote,
                    modifier = Modifier.shadow(
                        elevation = 16.dp,
                        shape = RoundedCornerShape(16.dp),
                        spotColor = PrimaryCrimson,
                        ambientColor = PrimaryCrimson.copy(alpha = 0.5f),
                    ),
                    containerColor = PrimaryCrimson,
                    contentColor = TextOnRed,
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "New note")
                }
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(scaffoldBackground)
                .padding(innerPadding)
        ) {
            // Header Section
            if (uiState.isJournalUnlocked) {
                JournalHeader(onLock = { viewModel.lockJournal() })
            } else {
                NotesHeader(
                    username = uiState.username,
                    noteCount = uiState.notes.size,
                    query = uiState.query,
                    onQueryChange = { viewModel.onQueryChange(it) }
                )
            }

            // Main Content Switcher
            AnimatedContent(
                targetState = uiState.isJournalUnlocked,
                transitionSpec = {
                    fadeIn(animationSpec = tween(400)) togetherWith fadeOut(animationSpec = tween(400))
                },
                label = "journal_transition"
            ) { isJournal ->
                if (isJournal) {
                    JournalView(
                        notes = uiState.journalNotes,
                        anniversaryDate = uiState.anniversaryDate,
                        onNoteClick = onJournalEntryClick,
                    )
                } else {
                    StandardNotesView(
                        notes = uiState.notes,
                        isLoading = uiState.isLoading,
                        isFingerprintEnabled = uiState.isFingerprintEnabled,
                        anniversaryDate = uiState.anniversaryDate,
                        onNoteClick = onNoteClick,
                    )
                }
            }
        }
    }
}

/**
 * Top header for the public notes screen.
 *
 * Visual spec:
 *   - Title: "username's Koraki" at displaySmall (38sp / Bold)
 *   - Subtitle: "N notes · Updated MMM d" at labelSmall (12sp) in MutedText
 *   - Search bar below, 64 dp tall, dark background #151012 with 1 dp border
 */
@Composable
fun NotesHeader(
    username: String,
    noteCount: Int,
    query: String,
    onQueryChange: (String) -> Unit,
) {
    val updatedLabel = remember {
        "Updated " + SimpleDateFormat("MMM d", Locale.getDefault()).format(Date())
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 8.dp)
    ) {
        Text(
            text = "${username}'s Koraki",
            style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "$noteCount notes · $updatedLabel",
            style = MaterialTheme.typography.labelSmall,
            color = MutedText,
        )
        Spacer(modifier = Modifier.height(12.dp))
        NotesSearchBar(
            query = query,
            onQueryChange = onQueryChange,
        )
    }
}

/**
 * Editorial journal header: full-bleed dark banner with today's date
 * and the rotating prompt in serif italic. A stealth close button locks the journal.
 */
@Composable
fun JournalHeader(
    onLock: () -> Unit,
) {
    // Derive today's date label (e.g. "Monday, August 25")
    val dateLabel = remember {
        SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date())
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(VaultBackground),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 8.dp, top = 20.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // Day + date
                Text(
                    text = dateLabel,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontFamily = FontFamily.Serif,
                        fontStyle = FontStyle.Italic,
                        letterSpacing = 0.5.sp,
                    ),
                    color = VaultMuted,
                )
                Spacer(modifier = Modifier.height(4.dp))
                // Editorial prompt
                Text(
                    text = "What's on your mind tonight?",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = VaultText,
                )
            }
            // Stealth close / lock button
            IconButton(onClick = onLock) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Lock Journal",
                    tint = VaultMuted,
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = VaultBorder, thickness = 1.dp)
    }
}

@Composable
fun StandardNotesView(
    notes: List<NoteEntity>,
    isLoading: Boolean,
    isFingerprintEnabled: Boolean,
    anniversaryDate: Long?,
    onNoteClick: (Long) -> Unit
) {
    if (notes.isEmpty() && !isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "No notes yet.\nTap + to create one.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            // Subtle ambient crimson radial glow — rgba(181, 30, 53, 0.04)
            // Gives the background depth without destroying the minimalist aesthetic
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(AmbientGlow, Color.Transparent),
                        center = Offset(size.width / 2f, size.height * 0.18f),
                        radius = size.width * 0.9f,
                    )
                )
            }

            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 4.dp,
                    bottom = 88.dp, // space above FAB
                ),
                verticalItemSpacing = 16.dp,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                if (anniversaryDate != null) {
                    item(span = StaggeredGridItemSpan.FullLine) {
                        AnniversaryBanner(
                            anniversaryTimestampMs = anniversaryDate,
                            modifier = Modifier.padding(bottom = 4.dp),
                        )
                    }
                }

                items(notes, key = { it.id }) { note ->
                    val context = androidx.compose.ui.platform.LocalContext.current
                    NoteCard(
                        note = note,
                        onClick = {
                            if (note.isLocked) {
                                if (isFingerprintEnabled) {
                                    com.aima.koraki.util.BiometricHelper.authenticate(
                                        context = context,
                                        onSuccess = { onNoteClick(note.id) },
                                        onError = { /* handle error if needed */ }
                                    )
                                } else {
                                    android.widget.Toast.makeText(context, "Enable Fingerprint Login in Profile to unlock here.", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                onNoteClick(note.id)
                            }
                        },
                    )
                }
            }
        }
    }
}

/**
 * Date-anchored vertical timeline for journal entries.
 *
 * Notes are grouped by calendar day. Each group is preceded by a sticky
 * date-label divider (e.g. "Monday, August 24") rendered in serif italic.
 */
@Composable
fun JournalView(
    notes: List<NoteEntity>,
    anniversaryDate: Long?,
    onNoteClick: (Long) -> Unit,
) {
    if (notes.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(VaultBackground),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "The pages are empty.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.Serif,
                    fontStyle = FontStyle.Italic,
                ),
                color = VaultMuted,
            )
        }
    } else {
        // Group entries by calendar day label ("Monday, August 24")
        val dayFormatter = remember { SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()) }
        val groupedNotes: List<Pair<String?, NoteEntity>> = remember(notes) {
            notes
                .sortedByDescending { it.timestamp }
                .groupBy { dayFormatter.format(Date(it.timestamp)) }
                .flatMap { (day, entries) ->
                    // First entry in each group carries the date label; rest carry null
                    entries.mapIndexed { index, note ->
                        if (index == 0) day to note else null to note
                    }
                }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(VaultBackground),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 16.dp,
                bottom = 88.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (anniversaryDate != null) {
                item {
                    AnniversaryBanner(
                        anniversaryTimestampMs = anniversaryDate,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                }
            }

            items(groupedNotes, key = { (_, note) -> note.id }) { (dayLabel, note) ->
                Column {
                    // Date-anchor divider — only shown for the first entry of each day
                    if (dayLabel != null) {
                        if (groupedNotes.indexOfFirst { it.second == note } > 0) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            HorizontalDivider(
                                modifier = Modifier.weight(1f),
                                color = VaultBorder,
                                thickness = 1.dp,
                            )
                            Text(
                                text = dayLabel,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontFamily = FontFamily.Serif,
                                    fontStyle = FontStyle.Italic,
                                ),
                                color = VaultMuted,
                            )
                            HorizontalDivider(
                                modifier = Modifier.weight(1f),
                                color = VaultBorder,
                                thickness = 1.dp,
                            )
                        }
                    }

                    JournalEntryCard(
                        note = note,
                        onClick = { onNoteClick(note.id) },
                    )
                }
            }
        }
    }
}

/**
 * Full-width search bar used inside the Notes screen.
 *
 * Visual spec:
 *   - Height: 64 dp
 *   - Background: NoteBackground (#151012)
 *   - Border: 1 dp Borders (#352126)
 *   - Search icon: SecondaryText (#B9A6AA)
 *   - Placeholder: MutedText (#77686C)
 *   - Cursor: BrightCrimson
 */
@Composable
fun NotesSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(width = 1.dp, color = Borders, shape = RoundedCornerShape(12.dp)),
    ) {
        TextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxSize(),
            placeholder = {
                Text(
                    text = "Search notes…",
                    color = MutedText,
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = SecondaryText,
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = NoteBackground,
                unfocusedContainerColor = NoteBackground,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = BrightCrimson,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { /* dismiss keyboard */ }),
        )
    }
}
