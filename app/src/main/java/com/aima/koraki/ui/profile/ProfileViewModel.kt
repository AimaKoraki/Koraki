package com.aima.koraki.ui.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aima.koraki.data.preferences.VaultPreferences
import com.aima.koraki.data.repository.BackupRepository
import com.aima.koraki.data.repository.CompanionRepository
import com.aima.koraki.data.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.aima.koraki.util.LevelManager

/** UI state for the Profile screen. */
data class ProfileUiState(
    val publicNoteCount: Int = 0,
    val lockedNoteCount: Int = 0,
    val companionCount: Int = 0,
    val vaultCode: String = VaultPreferences.DEFAULT_VAULT_CODE,
    val vaultHint: String = "",
    val username: String = "User",
    val isFingerprintEnabled: Boolean = false,
    val isAppLockEnabled: Boolean = false,
    val hasAppPin: Boolean = false,
    val anniversaryDate: Long? = null,
    val totalXp: Int = 0,
    val userLevel: Int = 1,
    val levelTitle: String = "Budding Romance",
    val levelProgress: Float = 0f,
    val nextLevelRequiredXp: Int = 200,
    val streakCount: Int = 0,
    val showVaultCodeSaved: Boolean = false,
    val exportMessage: String? = null,
    val isLoading: Boolean = true,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    private val companionRepository: CompanionRepository,
    private val vaultPreferences: VaultPreferences,
    private val backupRepository: BackupRepository,
) : ViewModel() {

    private val _showVaultCodeSaved = MutableStateFlow(false)
    private val _exportMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<ProfileUiState> = combine(
        noteRepository.getPublicNoteCount(),
        noteRepository.getLockedNoteCount(),
        companionRepository.getCompanionCount(),
        vaultPreferences.getVaultCode,
        vaultPreferences.getVaultHint,
        vaultPreferences.getUsername,
        vaultPreferences.isFingerprintEnabled,
        vaultPreferences.isAppLockEnabled,
        vaultPreferences.getAppPinHash,
        vaultPreferences.getAnniversaryDate,
        vaultPreferences.getTotalXp,
        vaultPreferences.getStreakCount,
        _showVaultCodeSaved,
        _exportMessage
    ) { args ->
        val totalXp = args[10] as Int
        val streakCount = args[11] as Int
        val milestone = LevelManager.getMilestone(totalXp)
        val nextMilestone = LevelManager.getNextMilestone(totalXp)

        ProfileUiState(
            publicNoteCount = args[0] as Int,
            lockedNoteCount = args[1] as Int,
            companionCount = args[2] as Int,
            vaultCode = args[3] as String,
            vaultHint = args[4] as String,
            username = args[5] as String,
            isFingerprintEnabled = args[6] as Boolean,
            isAppLockEnabled = args[7] as Boolean,
            hasAppPin = (args[8] as String).isNotEmpty(),
            anniversaryDate = args[9] as Long?,
            totalXp = totalXp,
            userLevel = milestone.level,
            levelTitle = milestone.title,
            levelProgress = LevelManager.getLevelProgress(totalXp),
            nextLevelRequiredXp = nextMilestone?.requiredXp ?: milestone.requiredXp,
            streakCount = streakCount,
            showVaultCodeSaved = args[12] as Boolean,
            exportMessage = args[13] as String?,
            isLoading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ProfileUiState(),
    )

    private val _vaultCodeInput = MutableStateFlow("")
    val vaultCodeInput: StateFlow<String> = _vaultCodeInput

    private val _vaultHintInput = MutableStateFlow("")
    val vaultHintInput: StateFlow<String> = _vaultHintInput

    private val _usernameInput = MutableStateFlow("")
    val usernameInput: StateFlow<String> = _usernameInput
    
    private val _resetPasskeyInput = MutableStateFlow("")
    val resetPasskeyInput: StateFlow<String> = _resetPasskeyInput

    // App Lock PIN fields
    private val _pinInput = MutableStateFlow("")
    val pinInput: StateFlow<String> = _pinInput

    private val _pinConfirmInput = MutableStateFlow("")
    val pinConfirmInput: StateFlow<String> = _pinConfirmInput

    fun onVaultCodeInputChange(value: String) = _vaultCodeInput.update { value }
    fun onVaultHintInputChange(value: String) = _vaultHintInput.update { value }
    fun onUsernameInputChange(value: String) = _usernameInput.update { value }
    fun onResetPasskeyInputChange(value: String) = _resetPasskeyInput.update { value }
    fun onPinInputChange(value: String) = _pinInput.update { value }
    fun onPinConfirmInputChange(value: String) = _pinConfirmInput.update { value }
    
    init {
        viewModelScope.launch {
            vaultPreferences.getUsername.collect { name ->
                _usernameInput.update { name }
            }
        }
    }

    fun saveUsername() {
        val newName = _usernameInput.value.trim()
        if (newName.isBlank()) return
        viewModelScope.launch {
            kotlinx.coroutines.withContext(kotlinx.coroutines.NonCancellable) {
                vaultPreferences.setUsername(newName)
            }
        }
    }

    fun toggleFingerprint(enabled: Boolean) {
        viewModelScope.launch {
            kotlinx.coroutines.withContext(kotlinx.coroutines.NonCancellable) {
                vaultPreferences.setFingerprintEnabled(enabled)
            }
        }
    }

    fun toggleAppLock(enabled: Boolean) {
        viewModelScope.launch {
            kotlinx.coroutines.withContext(kotlinx.coroutines.NonCancellable) {
                vaultPreferences.setAppLockEnabled(enabled)
                if (!enabled) {
                    // Turn off fingerprint if app lock is turned off
                    vaultPreferences.setFingerprintEnabled(false)
                }
            }
        }
    }

    fun saveAppPin(): Boolean {
        val pin = _pinInput.value
        val confirm = _pinConfirmInput.value
        
        if (pin.isBlank() || pin != confirm) {
            return false
        }
        
        viewModelScope.launch {
            kotlinx.coroutines.withContext(kotlinx.coroutines.NonCancellable) {
                vaultPreferences.setAppPin(pin)
            }
            _pinInput.update { "" }
            _pinConfirmInput.update { "" }
        }
        return true
    }

    fun saveVaultCode() {
        val newCode = _vaultCodeInput.value.trim()
        if (newCode.isBlank()) return
        val newHint = _vaultHintInput.value.trim()
        viewModelScope.launch {
            kotlinx.coroutines.withContext(kotlinx.coroutines.NonCancellable) {
                vaultPreferences.setVaultCode(newCode)
                vaultPreferences.setVaultHint(newHint)
            }
            _showVaultCodeSaved.update { true }
            _vaultCodeInput.update { "" }
            _vaultHintInput.update { "" }
            kotlinx.coroutines.delay(2_000)
            _showVaultCodeSaved.update { false }
        }
    }
    
    fun resetVaultCode(passkey: String): Boolean {
        if (passkey == "Jana&Afra4ever") {
            viewModelScope.launch {
                kotlinx.coroutines.withContext(kotlinx.coroutines.NonCancellable) {
                    vaultPreferences.setVaultCode(VaultPreferences.DEFAULT_VAULT_CODE)
                    vaultPreferences.setVaultHint("")
                }
            }
            return true
        }
        return false
    }

    fun setAnniversaryDate(timestamp: Long?) {
        viewModelScope.launch {
            kotlinx.coroutines.withContext(kotlinx.coroutines.NonCancellable) {
                vaultPreferences.setAnniversaryDate(timestamp)
            }
        }
    }

    fun exportDatabase(uri: Uri) {
        viewModelScope.launch {
            val result = backupRepository.exportDatabaseToUri(uri)
            if (result.isSuccess) {
                _exportMessage.update { "Backup exported successfully!" }
            } else {
                _exportMessage.update { "Export failed: ${result.exceptionOrNull()?.message}" }
            }
            kotlinx.coroutines.delay(3_000)
            _exportMessage.update { null }
        }
    }

    fun importDatabase(uri: Uri) {
        viewModelScope.launch {
            val result = backupRepository.importDatabaseFromUri(uri)
            if (result.isSuccess) {
                _exportMessage.update { "Backup imported successfully! Data restored." }
            } else {
                _exportMessage.update { "Import failed: Invalid JSON or corrupted file." }
            }
            kotlinx.coroutines.delay(3_000)
            _exportMessage.update { null }
        }
    }
}

