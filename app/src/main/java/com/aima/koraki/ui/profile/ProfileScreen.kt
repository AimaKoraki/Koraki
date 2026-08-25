package com.aima.koraki.ui.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aima.koraki.data.preferences.VaultPreferences
import com.aima.koraki.ui.components.calculateDaysTogether
import com.aima.koraki.ui.theme.RubyHeart
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Profile screen showing app statistics, anniversary milestone, and vault passcode settings.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val vaultCodeInput by viewModel.vaultCodeInput.collectAsStateWithLifecycle()
    val vaultHintInput by viewModel.vaultHintInput.collectAsStateWithLifecycle()
    val usernameInput by viewModel.usernameInput.collectAsStateWithLifecycle()
    val resetPasskeyInput by viewModel.resetPasskeyInput.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()

    var isPasscodeExpanded by remember { mutableStateOf(false) }
    var showHint by remember { mutableStateOf(false) }
    var showReset by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.showVaultCodeSaved) {
        if (uiState.showVaultCodeSaved) {
            snackbarHostState.showSnackbar("Vault passcode updated ✓")
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Profile",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // ── Username Section ───────────────────────────────────────────
            Text(
                text = "Username",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = usernameInput,
                    onValueChange = viewModel::onUsernameInputChange,
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        cursorColor = MaterialTheme.colorScheme.primary,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { viewModel.saveUsername() }),
                )
                Button(
                    onClick = viewModel::saveUsername,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                    shape = RoundedCornerShape(10.dp),
                ) {
                    Text("Update")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── Stats Section ──────────────────────────────────────────────
            Text(
                text = "Statistics",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )

            val totalEntries = uiState.publicNoteCount + uiState.lockedNoteCount

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(
                                text = totalEntries.toString(),
                                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Text(
                                text = "Notes & Journal Pages",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        if (uiState.streakCount > 0) {
                            androidx.compose.foundation.layout.Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(com.aima.koraki.ui.theme.DeepWine)
                                    .border(1.dp, com.aima.koraki.ui.theme.RubyHeart, RoundedCornerShape(20.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                            ) {
                                Text(
                                    text = "🔥 ${uiState.streakCount} Day Streak",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = RubyHeart,
                                )
                            }
                        }
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "Level ${uiState.userLevel} · ${uiState.levelTitle}",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Text(
                                text = "${uiState.totalXp} / ${uiState.nextLevelRequiredXp} XP",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        // Custom RubyGradient XP Progress Bar
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                        ) {
                            androidx.compose.foundation.layout.Box(
                                modifier = Modifier
                                    .fillMaxWidth(uiState.levelProgress.coerceIn(0.04f, 1f))
                                    .height(8.dp)
                                    .background(androidx.compose.ui.graphics.Brush.linearGradient(com.aima.koraki.ui.theme.RubyGradient)),
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── Anniversary & Special Date Section ──────────────────────────
            Text(
                text = "Anniversary & Special Date 💖",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    val daysTogether = calculateDaysTogether(uiState.anniversaryDate)
                    if (daysTogether != null && uiState.anniversaryDate != null) {
                        val dateStr = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
                            .format(Date(uiState.anniversaryDate!!))
                        Text(
                            text = "Day $daysTogether Together ❤️",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = RubyHeart,
                        )
                        Text(
                            text = "Special Date: $dateStr",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        Text(
                            text = "Set your special anniversary date to display your relationship milestone and daily romantic prompts on your journal.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Button(
                            onClick = { showDatePicker = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                            ),
                            shape = RoundedCornerShape(10.dp),
                        ) {
                            Text(if (uiState.anniversaryDate != null) "Change Date" else "Set Anniversary")
                        }

                        if (uiState.anniversaryDate != null) {
                            OutlinedButton(
                                onClick = { viewModel.setAnniversaryDate(null) },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                ),
                            ) {
                                Text("Clear")
                            }
                        }
                    }
                }
            }

            if (showDatePicker) {
                val datePickerState = rememberDatePickerState(
                    initialSelectedDateMillis = uiState.anniversaryDate ?: System.currentTimeMillis(),
                )
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                val selected = datePickerState.selectedDateMillis
                                if (selected != null) {
                                    viewModel.setAnniversaryDate(selected)
                                }
                                showDatePicker = false
                            },
                        ) {
                            Text("Set Date", color = MaterialTheme.colorScheme.primary)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) {
                            Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    },
                ) {
                    DatePicker(state = datePickerState)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── Vault Passcode Section ──────────────────────────────────────
            Text(
                text = "Vault Passcode",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )

            if (uiState.vaultCode == VaultPreferences.DEFAULT_VAULT_CODE) {
                Text(
                    text = "Set up a custom passcode to silently open the Vault from the Notes search bar. Emojis are supported!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                OutlinedTextField(
                    value = vaultCodeInput,
                    onValueChange = viewModel::onVaultCodeInputChange,
                    label = { Text("Passcode", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    )
                )

                OutlinedTextField(
                    value = vaultHintInput,
                    onValueChange = viewModel::onVaultHintInputChange,
                    label = { Text("Hint (Optional)", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { viewModel.saveVaultCode() }),
                )

                Button(
                    onClick = viewModel::saveVaultCode,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                    shape = RoundedCornerShape(10.dp),
                ) {
                    Text("Save Passcode")
                }
            } else {
                Button(
                    onClick = { isPasscodeExpanded = !isPasscodeExpanded },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                    shape = RoundedCornerShape(10.dp),
                ) {
                    Text("Passcode")
                }

                AnimatedVisibility(visible = isPasscodeExpanded) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { showHint = !showHint; showReset = false },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    contentColor = MaterialTheme.colorScheme.primary,
                                ),
                                shape = RoundedCornerShape(10.dp),
                            ) {
                                Text("Forgot Passcode")
                            }
                            Button(
                                onClick = { showReset = !showReset; showHint = false },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                                    contentColor = MaterialTheme.colorScheme.error,
                                ),
                                shape = RoundedCornerShape(10.dp),
                            ) {
                                Text("Reset Passcode")
                            }
                        }
                    }
                }

                AnimatedVisibility(visible = showHint && isPasscodeExpanded) {
                    Text(
                        text = if (uiState.vaultHint.isNotBlank()) "Hint: ${uiState.vaultHint}" else "No hint was set.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                AnimatedVisibility(visible = showReset && isPasscodeExpanded) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    ) {
                        OutlinedTextField(
                            value = resetPasskeyInput,
                            onValueChange = viewModel::onResetPasskeyInputChange,
                            label = { Text("Enter Reset Passkey") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.error,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = {
                                if (viewModel.resetVaultCode(resetPasskeyInput)) {
                                    isPasscodeExpanded = false
                                    showReset = false
                                    viewModel.onResetPasskeyInputChange("")
                                }
                            }),
                        )
                        Button(
                            onClick = {
                                if (viewModel.resetVaultCode(resetPasskeyInput)) {
                                    isPasscodeExpanded = false
                                    showReset = false
                                    viewModel.onResetPasskeyInputChange("")
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError,
                            ),
                            shape = RoundedCornerShape(10.dp),
                        ) {
                            Text("Confirm Reset")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── App Security ───────────────────────────────────────────────
            Text(
                text = "App Security",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )

            var pendingAppLockEnable by remember { mutableStateOf(false) }
            val showSecurityDetails = uiState.isAppLockEnabled || pendingAppLockEnable
            var showPinSetup by remember { mutableStateOf(false) }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "App Lock",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Text(
                        text = "Require PIN or fingerprint when opening Koraki.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                androidx.compose.material3.Switch(
                    checked = uiState.isAppLockEnabled || pendingAppLockEnable,
                    onCheckedChange = { checked ->
                        if (checked) {
                            if (uiState.hasAppPin) {
                                viewModel.toggleAppLock(true)
                            } else {
                                pendingAppLockEnable = true
                                showPinSetup = true
                            }
                        } else {
                            pendingAppLockEnable = false
                            showPinSetup = false
                            viewModel.toggleAppLock(false)
                        }
                    }
                )
            }

            AnimatedVisibility(visible = showSecurityDetails) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    if (uiState.hasAppPin && !showPinSetup) {
                        Button(
                            onClick = { showPinSetup = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurface,
                            ),
                            shape = RoundedCornerShape(10.dp),
                        ) {
                            Text("Change PIN")
                        }
                    } else {
                        val pin by viewModel.pinInput.collectAsStateWithLifecycle()
                        val confirm by viewModel.pinConfirmInput.collectAsStateWithLifecycle()
                        
                        OutlinedTextField(
                            value = pin,
                            onValueChange = viewModel::onPinInputChange,
                            label = { Text("New PIN") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Password, imeAction = ImeAction.Next),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            )
                        )
                        OutlinedTextField(
                            value = confirm,
                            onValueChange = viewModel::onPinConfirmInputChange,
                            label = { Text("Confirm PIN") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Password, imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = {
                                if (viewModel.saveAppPin()) {
                                    showPinSetup = false
                                    if (pendingAppLockEnable) {
                                        viewModel.toggleAppLock(true)
                                        pendingAppLockEnable = false
                                    }
                                }
                            }),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            )
                        )
                        Button(
                            onClick = {
                                if (viewModel.saveAppPin()) {
                                    showPinSetup = false
                                    if (pendingAppLockEnable) {
                                        viewModel.toggleAppLock(true)
                                        pendingAppLockEnable = false
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                            ),
                            shape = RoundedCornerShape(10.dp),
                        ) {
                            Text(if (uiState.hasAppPin) "Update PIN" else "Set PIN")
                        }
                        
                        if (showPinSetup) {
                            androidx.compose.material3.TextButton(
                                onClick = { 
                                    showPinSetup = false
                                    if (pendingAppLockEnable) {
                                        pendingAppLockEnable = false
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                            ) {
                                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Fingerprint Login",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                            Text(
                                text = "Use biometric authentication to unlock the app on launch.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        androidx.compose.material3.Switch(
                            checked = uiState.isFingerprintEnabled,
                            onCheckedChange = { checked ->
                                if (checked) {
                                    if (com.aima.koraki.util.BiometricHelper.canAuthenticate(context)) {
                                        viewModel.toggleFingerprint(true)
                                    } else {
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Biometric authentication is not supported or not enrolled on this device.")
                                        }
                                    }
                                } else {
                                    viewModel.toggleFingerprint(false)
                                }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Data Backup Section ─────────────────────────────────────────
            Text(
                text = "Data Backup",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )

            Text(
                text = "Export your entire database (including Vault and Companions) as a JSON file to your device storage or Google Drive.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            val exportLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                androidx.activity.result.contract.ActivityResultContracts.CreateDocument("application/json")
            ) { uri ->
                uri?.let { viewModel.exportDatabase(it) }
            }

            val importLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                androidx.activity.result.contract.ActivityResultContracts.OpenDocument()
            ) { uri ->
                uri?.let { viewModel.importDatabase(it) }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { exportLauncher.launch("koraki_backup.json") },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                    shape = RoundedCornerShape(10.dp),
                ) {
                    Text("Export")
                }

                Button(
                    onClick = { importLauncher.launch(arrayOf("application/json", "*/*")) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                    shape = RoundedCornerShape(10.dp),
                ) {
                    Text("Import")
                }
            }
            
            LaunchedEffect(uiState.exportMessage) {
                uiState.exportMessage?.let { msg ->
                    snackbarHostState.showSnackbar(msg)
                }
            }
        }
    }
}

