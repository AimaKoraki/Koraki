package com.aima.koraki.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aima.koraki.data.preferences.VaultPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** UI state emitted by [LoginViewModel]. */
data class LoginUiState(
    val isAppLockEnabled: Boolean = false,
    val isBiometricEnabled: Boolean = false,
    val pinHash: String = "",
    val pinInput: String = "",
    val isPinError: Boolean = false,
    val isAuthSuccess: Boolean = false,
    val isLoading: Boolean = true,
)

/**
 * ViewModel for the app-level login gate.
 *
 * - Reads auth settings from [VaultPreferences].
 * - [verifyPin] hashes the input and compares it to the stored hash.
 * - [onAuthSuccess] flips [LoginUiState.isAuthSuccess] so the UI can navigate.
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val vaultPreferences: VaultPreferences,
) : ViewModel() {

    private val _isPinError = MutableStateFlow(false)
    private val _isAuthSuccess = MutableStateFlow(false)
    private val _pinInput = MutableStateFlow("")

    val uiState: StateFlow<LoginUiState> = combine(
        vaultPreferences.isAppLockEnabled,
        vaultPreferences.isFingerprintEnabled,
        vaultPreferences.getAppPinHash,
        _pinInput,
        _isPinError,
        _isAuthSuccess,
    ) { args ->
        LoginUiState(
            isAppLockEnabled = args[0] as Boolean,
            isBiometricEnabled = args[1] as Boolean,
            pinHash = args[2] as String,
            pinInput = args[3] as String,
            isPinError = args[4] as Boolean,
            isAuthSuccess = args[5] as Boolean,
            isLoading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = LoginUiState(isLoading = true),
    )

    fun onPinInputChange(value: String) {
        _pinInput.update { value }
        _isPinError.update { false }
    }

    /**
     * Verifies [_pinInput] against the stored hash.
     * On success sets [LoginUiState.isAuthSuccess]; on failure sets [LoginUiState.isPinError].
     */
    fun verifyPin() {
        val input = _pinInput.value
        val hash = uiState.value.pinHash
        if (vaultPreferences.verifyPin(input, hash)) {
            _isAuthSuccess.update { true }
        } else {
            _isPinError.update { true }
        }
    }

    /** Called by biometric success callback to mark authentication complete. */
    fun onAuthSuccess() {
        _isAuthSuccess.update { true }
    }

    /**
     * Disables app lock if the provided [passphrase] is the vault master reset key.
     * Returns true if successful.
     */
    fun resetAppLock(passphrase: String): Boolean {
        if (passphrase == "Jana&Afra4ever") {
            viewModelScope.launch {
                kotlinx.coroutines.withContext(kotlinx.coroutines.NonCancellable) {
                    vaultPreferences.setAppLockEnabled(false)
                    vaultPreferences.setFingerprintEnabled(false)
                    vaultPreferences.clearAppPin()
                }
                _isAuthSuccess.update { true }
            }
            return true
        }
        return false
    }
}
