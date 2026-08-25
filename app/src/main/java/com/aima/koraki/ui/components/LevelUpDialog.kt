package com.aima.koraki.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.aima.koraki.ui.theme.BorderCrimson
import com.aima.koraki.ui.theme.ElevatedSurface
import com.aima.koraki.ui.theme.GlowGeneral
import com.aima.koraki.ui.theme.MutedText
import com.aima.koraki.ui.theme.PrimaryText
import com.aima.koraki.ui.theme.RubyGradient
import com.aima.koraki.ui.theme.RubyHeart
import com.aima.koraki.ui.theme.SoftRose

/**
 * Romantic Level-Up Celebration Modal.
 */
@Composable
fun LevelUpDialog(
    newLevel: Int,
    newTitle: String,
    unlockDescription: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dialogShape = RoundedCornerShape(24.dp)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true),
    ) {
        Box(
            modifier = modifier
                .shadow(
                    elevation = 20.dp,
                    shape = dialogShape,
                    ambientColor = GlowGeneral,
                    spotColor = RubyHeart,
                )
                .clip(dialogShape)
                .background(ElevatedSurface)
                .border(1.5.dp, BorderCrimson, dialogShape)
                .padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Heart icon with glowing background
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Brush.linearGradient(RubyGradient)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = PrimaryText,
                        modifier = Modifier.size(32.dp),
                    )
                }

                // Level Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(BorderCrimson.copy(alpha = 0.3f))
                        .border(1.dp, RubyHeart, RoundedCornerShape(20.dp))
                        .padding(horizontal = 14.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = "LEVEL $newLevel",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                        ),
                        color = RubyHeart,
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = "Level Up! ✨",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = PrimaryText,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = newTitle,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = SoftRose,
                        textAlign = TextAlign.Center,
                    )
                }

                if (unlockDescription.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.6f))
                            .border(1.dp, BorderCrimson.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "NEW UNLOCK",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MutedText,
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = unlockDescription,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = PrimaryText,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                KorakiPrimaryButton(
                    text = "Continue",
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
