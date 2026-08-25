package com.aima.koraki.ui.companions

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aima.koraki.data.model.CompanionMetadata
import com.aima.koraki.data.model.VirtualCompanion
import com.aima.koraki.ui.theme.AppBackground
import com.aima.koraki.ui.theme.Borders
import com.aima.koraki.ui.theme.ElevatedSurface
import com.aima.koraki.ui.theme.GlowGeneral
import com.aima.koraki.ui.theme.MutedText
import com.aima.koraki.ui.theme.NoteTagBackground
import com.aima.koraki.ui.theme.NoteTagText
import com.aima.koraki.ui.theme.PrimaryCrimson
import com.aima.koraki.ui.theme.PrimaryText
import com.aima.koraki.ui.theme.SecondaryText
import com.aima.koraki.ui.theme.StrongBorder

/**
 * Companions screen — fully dedicated to the Shimeji virtual sprite picker.
 *
 * Displays a 2-column grid of available sprite companions. Tapping an unlocked companion
 * activates it (starts the walking overlay across all main screens). Tapping the active
 * companion deactivates it.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanionsScreen(
    onNoteClick: (Long) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: CompanionsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        containerColor = AppBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Companions",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = PrimaryText,
                        )
                        Text(
                            text = "Your virtual pets",
                            style = MaterialTheme.typography.labelMedium,
                            color = SecondaryText,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppBackground),
            )
        },
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                Text("Loading companions…", style = MaterialTheme.typography.bodyMedium, color = MutedText)
            }
            return@Scaffold
        }

        if (uiState.companions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                Text("No companions found.", style = MaterialTheme.typography.bodyMedium, color = MutedText)
            }
            return@Scaffold
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = innerPadding.calculateTopPadding() + 8.dp,
                bottom = innerPadding.calculateBottomPadding() + 16.dp,
            ),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(uiState.companions, key = { it.spriteAsset }) { companion ->
                val isActive = companion.name == uiState.activeCompanionName
                ShimejiCompanionCard(
                    companion = companion,
                    isActive = isActive,
                    onClick = {
                        viewModel.setActive(if (isActive) null else companion.name)
                    },
                )
            }
        }
    }
}

@Composable
private fun ShimejiCompanionCard(
    companion: VirtualCompanion,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val cardShape = RoundedCornerShape(16.dp)
    val grayscaleFilter = remember {
        ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })
    }

    // Read metadata to find the exact first frame (supports .webp, .png, etc.)
    val previewBitmap: ImageBitmap? = remember(companion.spriteAsset) {
        runCatching {
            val metaJson = context.assets.open("companions/${companion.spriteAsset}/metadata.json").bufferedReader().readText()
            val meta = com.google.gson.Gson().fromJson(metaJson, CompanionMetadata::class.java)
            val firstFrame = meta.walkFrames.firstOrNull() ?: "walk_0.png"
            
            context.assets.open("companions/${companion.spriteAsset}/$firstFrame").use {
                BitmapFactory.decodeStream(it)?.asImageBitmap()
            }
        }.getOrNull()
    }

    val borderColor = if (isActive) StrongBorder else Borders
    val borderWidth = if (isActive) 2.dp else 1.dp
    val glowModifier = if (isActive) {
        Modifier.shadow(
            elevation = 12.dp,
            shape = cardShape,
            ambientColor = GlowGeneral,
            spotColor = PrimaryCrimson,
        )
    } else Modifier

    Box(
        modifier = modifier
            .then(glowModifier)
            .clip(cardShape)
            .background(ElevatedSurface)
            .border(borderWidth, borderColor, cardShape)
            .clickable(
                enabled = companion.unlockStatus,
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick,
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Sprite preview
            Box(
                modifier = Modifier.size(96.dp),
                contentAlignment = Alignment.Center,
            ) {
                if (previewBitmap != null) {
                    Image(
                        bitmap = previewBitmap,
                        contentDescription = "${companion.name} sprite",
                        colorFilter = if (!companion.unlockStatus) grayscaleFilter else null,
                        modifier = Modifier
                            .size(80.dp)
                            .then(
                                if (!companion.unlockStatus)
                                    Modifier.background(ElevatedSurface.copy(alpha = 0.4f))
                                else Modifier,
                            ),
                    )
                } else {
                    // Fallback placeholder box
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (companion.unlockStatus) PrimaryCrimson.copy(alpha = 0.2f) else Borders),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = companion.name.take(1).uppercase(),
                            style = MaterialTheme.typography.headlineMedium,
                            color = if (companion.unlockStatus) PrimaryCrimson else MutedText,
                        )
                    }
                }

                // Lock icon overlay for locked companions
                if (!companion.unlockStatus) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = MutedText,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(20.dp),
                    )
                }
            }

            // Name
            Text(
                text = companion.name,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = if (companion.unlockStatus) PrimaryText else MutedText,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            // Species
            Text(
                text = companion.species,
                style = MaterialTheme.typography.labelSmall,
                color = SecondaryText,
                textAlign = TextAlign.Center,
            )

            // Affection / Love points
            if (companion.unlockStatus) {
                androidx.compose.foundation.layout.Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Affection",
                        tint = com.aima.koraki.ui.theme.RubyHeart,
                        modifier = Modifier.size(12.dp),
                    )
                    Text(
                        text = "${companion.affectionLevel} Affection",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                        color = com.aima.koraki.ui.theme.RubyHeart,
                    )
                }

                // Mini Affection Progress Bar
                val progressFraction = ((companion.affectionLevel % 20) / 20f).coerceIn(0.05f, 1f)
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Borders),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progressFraction)
                            .height(4.dp)
                            .background(androidx.compose.ui.graphics.Brush.linearGradient(com.aima.koraki.ui.theme.RubyGradient)),
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Status badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        when {
                            isActive -> NoteTagBackground
                            !companion.unlockStatus -> Borders.copy(alpha = 0.5f)
                            else -> Borders
                        },
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center,
            ) {
                if (isActive) {
                    androidx.compose.foundation.layout.Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = NoteTagText,
                            modifier = Modifier.size(12.dp),
                        )
                        Text(
                            text = "Active",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = NoteTagText,
                        )
                    }
                } else {
                    Text(
                        text = if (companion.unlockStatus) "Tap to activate" else "Level ${companion.requiredLevel} Required",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (companion.unlockStatus) SecondaryText else MutedText,
                    )
                }
            }
        }
    }
}
