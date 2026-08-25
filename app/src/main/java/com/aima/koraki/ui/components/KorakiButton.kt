package com.aima.koraki.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aima.koraki.ui.theme.BorderStrong
import com.aima.koraki.ui.theme.ButtonDestructiveBg
import com.aima.koraki.ui.theme.ButtonDestructiveText
import com.aima.koraki.ui.theme.ButtonPrimaryBg
import com.aima.koraki.ui.theme.ButtonPrimaryText
import com.aima.koraki.ui.theme.ButtonSecondaryBg
import com.aima.koraki.ui.theme.ButtonSecondaryBorder
import com.aima.koraki.ui.theme.ButtonSecondaryText
import com.aima.koraki.ui.theme.ElevatedSurface
import com.aima.koraki.ui.theme.SemanticWarning
import com.aima.koraki.ui.theme.TextMuted
import com.aima.koraki.ui.theme.TextPrimary
import com.aima.koraki.ui.theme.TextSecondary

/**
 * Primary action button adhering to the Unified Red Design System.
 * Background: #B51E35 (ButtonPrimaryBg), Text: #FFFFFF (ButtonPrimaryText).
 */
@Composable
fun KorakiPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null,
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = ButtonPrimaryBg,
            contentColor = ButtonPrimaryText,
            disabledContainerColor = ButtonPrimaryBg.copy(alpha = 0.4f),
            disabledContentColor = ButtonPrimaryText.copy(alpha = 0.5f),
        ),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            if (leadingIcon != null) {
                leadingIcon()
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.3.sp,
                ),
            )
        }
    }
}

/**
 * Secondary action button adhering to the Unified Red Design System.
 * Background: #2A0810 (ButtonSecondaryBg), Border: #54202B (ButtonSecondaryBorder), Text: #E4D7DA.
 */
@Composable
fun KorakiSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, ButtonSecondaryBorder),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = ButtonSecondaryBg,
            contentColor = ButtonSecondaryText,
        ),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            if (leadingIcon != null) {
                leadingIcon()
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Medium,
                ),
            )
        }
    }
}

/**
 * Destructive action button adhering to the Unified Red Design System.
 * Background: #650D1B (ButtonDestructiveBg), Text: #FFFFFF.
 */
@Composable
fun KorakiDestructiveButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = ButtonDestructiveBg,
            contentColor = ButtonDestructiveText,
        ),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.SemiBold,
            ),
        )
    }
}

/**
 * Polished Destructive Confirmation Modal.
 * Styled with ElevatedSurface, BorderStrong stroke, SemanticWarning header accent, and Destructive CTA.
 */
@Composable
fun KorakiDestructiveDialog(
    title: String,
    message: String,
    confirmText: String = "Delete",
    dismissText: String = "Cancel",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ElevatedSurface,
        shape = RoundedCornerShape(16.dp),
        icon = {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = SemanticWarning,
                modifier = Modifier.size(32.dp),
            )
        },
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary,
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ButtonDestructiveBg,
                    contentColor = ButtonDestructiveText,
                ),
            ) {
                Text(confirmText, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissText, color = TextMuted)
            }
        },
    )
}
