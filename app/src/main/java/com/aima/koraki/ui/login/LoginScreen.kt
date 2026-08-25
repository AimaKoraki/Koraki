package com.aima.koraki.ui.login

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.aima.koraki.ui.theme.BrightCrimson
import com.aima.koraki.ui.theme.DeepBackground
import com.aima.koraki.ui.theme.MutedText
import com.aima.koraki.ui.theme.NoteBodyText
import com.aima.koraki.ui.theme.PrimaryCrimson
import com.aima.koraki.ui.theme.TextOnRed
import com.aima.koraki.ui.theme.TextPrimary
import com.aima.koraki.ui.theme.VaultBorder

/**
 * Full-screen authentication gate.
 *
 * Shown on every cold launch when app lock is enabled.
 * - If biometric is enabled, fires [BiometricPrompt] immediately.
 * - User can also type their PIN and tap Unlock.
 * - On success, [onAuthSuccess] is called and the nav graph pops past this screen.
 */
@Composable
fun LoginScreen(
    onAuthSuccess: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Navigate away once auth succeeds
    LaunchedEffect(uiState.isAuthSuccess) {
        if (uiState.isAuthSuccess) onAuthSuccess()
    }

    // Fire biometric prompt immediately on launch if enabled
    LaunchedEffect(uiState.isBiometricEnabled) {
        if (uiState.isBiometricEnabled) {
            com.aima.koraki.util.BiometricHelper.authenticate(
                context = context,
                onSuccess = { viewModel.onAuthSuccess() },
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBackground)
            .imePadding(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            // ── App identity ────────────────────────────────────────────────
            AsyncImage(
                model = "file:///android_asset/logoKoraki.png",
                contentDescription = "Koraki Logo",
                modifier = Modifier.size(64.dp),
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Koraki",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 36.sp,
                ),
                color = TextPrimary,
            )

            Text(
                text = "κόρακι",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Serif,
                    fontStyle = FontStyle.Italic,
                    letterSpacing = 2.sp,
                ),
                color = MutedText,
            )

            Spacer(modifier = Modifier.height(48.dp))

            // ── PIN field ───────────────────────────────────────────────────
            OutlinedTextField(
                value = uiState.pinInput,
                onValueChange = viewModel::onPinInputChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                placeholder = {
                    Text(
                        text = "Enter your PIN",
                        color = MutedText,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                },
                isError = uiState.isPinError,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(onDone = { viewModel.verifyPin() }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrightCrimson,
                    unfocusedBorderColor = VaultBorder,
                    errorBorderColor = MaterialTheme.colorScheme.error,
                    cursorColor = BrightCrimson,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = NoteBodyText,
                    errorTextColor = TextPrimary,
                ),
            )

            // Error message
            if (uiState.isPinError) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Wrong PIN. Try again.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Unlock button ───────────────────────────────────────────────
            Button(
                onClick = viewModel::verifyPin,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryCrimson,
                    contentColor = TextOnRed,
                ),
            ) {
                Text(
                    text = "Unlock",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp,
                    ),
                )
            }

            // ── Fingerprint fallback ────────────────────────────────────────
            if (uiState.isBiometricEnabled) {
                Spacer(modifier = Modifier.height(24.dp))
                IconButton(
                    onClick = {
                        com.aima.koraki.util.BiometricHelper.authenticate(
                            context = context,
                            onSuccess = { viewModel.onAuthSuccess() },
                        )
                    },
                    modifier = Modifier.size(56.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = "Use fingerprint",
                        tint = BrightCrimson,
                        modifier = Modifier.size(40.dp),
                    )
                }
                Text(
                    text = "Use fingerprint",
                    color = MutedText,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            var showResetDialog by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
            androidx.compose.material3.TextButton(onClick = { showResetDialog = true }) {
                Text(
                    text = "Forgot PIN?",
                    color = MutedText,
                    style = MaterialTheme.typography.labelMedium,
                )
            }

            if (showResetDialog) {
                var resetInput by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }
                var isResetError by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

                androidx.compose.material3.AlertDialog(
                    onDismissRequest = { showResetDialog = false },
                    title = { Text("Reset PIN", color = TextPrimary) },
                    text = {
                        Column {
                            Text(
                                "Enter the master reset phrase to disable App Lock.",
                                color = NoteBodyText,
                                style = MaterialTheme.typography.bodySmall,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = resetInput,
                                onValueChange = { 
                                    resetInput = it
                                    isResetError = false
                                },
                                singleLine = true,
                                isError = isResetError,
                                visualTransformation = PasswordVisualTransformation(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BrightCrimson,
                                    unfocusedBorderColor = VaultBorder,
                                    errorBorderColor = MaterialTheme.colorScheme.error,
                                    cursorColor = BrightCrimson,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = NoteBodyText,
                                )
                            )
                            if (isResetError) {
                                Text(
                                    "Incorrect phrase.",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    },
                    containerColor = DeepBackground,
                    confirmButton = {
                        androidx.compose.material3.TextButton(
                            onClick = {
                                if (viewModel.resetAppLock(resetInput)) {
                                    showResetDialog = false
                                } else {
                                    isResetError = true
                                }
                            }
                        ) {
                            Text("Reset", color = BrightCrimson)
                        }
                    },
                    dismissButton = {
                        androidx.compose.material3.TextButton(
                            onClick = { showResetDialog = false }
                        ) {
                            Text("Cancel", color = MutedText)
                        }
                    }
                )
            }
        }
    }
}
