package com.aima.koraki.ui.editor

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.aima.koraki.ui.components.KorakiDestructiveDialog
import com.aima.koraki.ui.components.MoodSelector
import com.aima.koraki.ui.components.VelvetAudioWaveform
import com.aima.koraki.ui.theme.BrightCrimson
import com.aima.koraki.ui.theme.ElevatedSurface
import com.aima.koraki.ui.theme.MutedText
import com.aima.koraki.ui.theme.PrimaryCrimson
import com.aima.koraki.ui.theme.RubyHeart
import com.aima.koraki.ui.theme.SemanticNeutral
import com.aima.koraki.ui.theme.SecondaryText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    noteId: Int,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EditorViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val companions by viewModel.companions.collectAsStateWithLifecycle()
    val currentContext = LocalContext.current

    var showCompanionMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Auto-save when pressing the system back button
    BackHandler {
        viewModel.saveNote()
        onNavigateBack()
    }

    // Level Up celebration dialog
    uiState.levelUpEvent?.let { evt ->
        com.aima.koraki.ui.components.LevelUpDialog(
            newLevel = evt.newLevel,
            newTitle = evt.newTitle,
            unlockDescription = evt.unlockDescription,
            onDismiss = viewModel::onLevelUpDismissed,
        )
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        KorakiDestructiveDialog(
            title = "Delete Note?",
            message = "This note will be permanently deleted and cannot be recovered.",
            confirmText = "Delete",
            onConfirm = {
                showDeleteDialog = false
                viewModel.deleteNote()
                onNavigateBack()
            },
            onDismiss = { showDeleteDialog = false },
        )
    }

    Scaffold(
        modifier = modifier.imePadding(),
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.saveNote()
                        onNavigateBack()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                title = {},
                actions = {
                    // Favorite / Special Love Letter Toggle
                    IconButton(onClick = { viewModel.onPinToggle(!uiState.isPinned) }) {
                        Icon(
                            imageVector = if (uiState.isPinned) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (uiState.isPinned) "Marked as Special" else "Mark as Special",
                            tint = if (uiState.isPinned) RubyHeart else SecondaryText,
                        )
                    }

                    // Delete Button (only show if it's an existing note)
                    if (noteId > 0) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete note",
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 8.dp),
        ) {
            // ── Companion Tag Selector ───────────────────────────────────────
            if (companions.isNotEmpty()) {
                Box {
                    val selectedCompanion = companions.find { it.id == uiState.companionId }

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .clickable { showCompanionMenu = true }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (selectedCompanion != null) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(
                                        try {
                                            Color(android.graphics.Color.parseColor(selectedCompanion.avatarColorHex))
                                        } catch (e: Exception) {
                                            SemanticNeutral
                                        }
                                    )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = selectedCompanion.name,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        } else {
                            Text(
                                text = "Tag Companion",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = showCompanionMenu,
                        onDismissRequest = { showCompanionMenu = false },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        DropdownMenuItem(
                            text = { Text("None", color = MaterialTheme.colorScheme.onSurface) },
                            onClick = {
                                viewModel.onCompanionSelect(null)
                                showCompanionMenu = false
                            }
                        )
                        companions.forEach { companion ->
                            DropdownMenuItem(
                                text = { Text(companion.name, color = MaterialTheme.colorScheme.onSurface) },
                                onClick = {
                                    viewModel.onCompanionSelect(companion.id)
                                    showCompanionMenu = false
                                },
                                leadingIcon = {
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(
                                                try {
                                                    Color(android.graphics.Color.parseColor(companion.avatarColorHex))
                                                } catch (e: Exception) {
                                                    SemanticNeutral
                                                }
                                            )
                                    )
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // ── Mood Selector ────────────────────────────────────────────────
            MoodSelector(
                selectedMood = uiState.mood,
                onMoodSelect = viewModel::onMoodChange,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ── Title field ──────────────────────────────────────────────────
            BasicTextField(
                value = uiState.title,
                onValueChange = viewModel::onTitleChange,
                modifier = Modifier.fillMaxWidth(),
                textStyle = LocalTextStyle.current.copy(
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                decorationBox = { inner ->
                    if (uiState.title.isEmpty()) {
                        Text(
                            text = "Title",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                        )
                    }
                    inner()
                },
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ── Body field ───────────────────────────────────────────────────
            BasicTextField(
                value = uiState.content,
                onValueChange = viewModel::onContentChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                textStyle = LocalTextStyle.current.copy(
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 16.sp,
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                decorationBox = { inner ->
                    if (uiState.content.isEmpty()) {
                        Text(
                            text = "Start writing…",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                        )
                    }
                    inner()
                },
            )

            // ── Attached media display ───────────────────────────────────────
            if (uiState.images.isNotEmpty() || uiState.audio.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(uiState.images.size) { index ->
                        AsyncImage(
                            model = uiState.images[index],
                            contentDescription = "Attached image",
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop,
                        )
                    }
                    items(uiState.audio.size) { index ->
                        val audioPath = uiState.audio[index]
                        val isPlaying = uiState.isPlayingAudio && uiState.playingAudioPath == audioPath
                        VelvetAudioWaveform(
                            isPlaying = isPlaying,
                            onPlayClick = { viewModel.toggleAudioPlayback(audioPath) },
                            onDeleteClick = { viewModel.deleteAudio(audioPath) },
                            modifier = Modifier.padding(vertical = 4.dp),
                        )
                    }
                }
            }

            // ── Media toolbar ────────────────────────────────────────────────
            Spacer(modifier = Modifier.height(8.dp))

            val photoPickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia()
            ) { uri ->
                if (uri != null) viewModel.addImage(uri)
            }

            val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                if (isGranted) viewModel.toggleAudioRecording()
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Image picker
                IconButton(onClick = {
                    photoPickerLauncher.launch(
                        androidx.activity.result.PickVisualMediaRequest(
                            androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly
                        )
                    )
                }) {
                    Icon(
                        imageVector = Icons.Filled.Image,
                        contentDescription = "Attach image",
                        tint = SecondaryText,
                    )
                }

                // Audio recorder
                IconButton(onClick = {
                    val permission = android.Manifest.permission.RECORD_AUDIO
                    if (androidx.core.content.ContextCompat.checkSelfPermission(
                            currentContext, permission
                        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                    ) {
                        viewModel.toggleAudioRecording()
                    } else {
                        permissionLauncher.launch(permission)
                    }
                }) {
                    Icon(
                        imageVector = if (uiState.isRecordingAudio) Icons.Filled.Stop else Icons.Filled.Mic,
                        contentDescription = if (uiState.isRecordingAudio) "Stop recording" else "Record audio",
                        tint = if (uiState.isRecordingAudio) BrightCrimson else SecondaryText,
                    )
                }

                if (uiState.isRecordingAudio) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Recording…",
                        color = BrightCrimson,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
        }
    }
}

