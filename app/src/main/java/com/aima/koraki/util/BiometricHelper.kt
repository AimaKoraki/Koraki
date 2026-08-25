package com.aima.koraki.util

import android.content.Context
import android.content.ContextWrapper
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/**
 * Utility helper for managing biometric authentication across Koraki.
 * Enforces correct context unwrapping, authenticator parameter matching, and mutual exclusion rules.
 */
object BiometricHelper {

    val DEFAULT_AUTHENTICATORS = BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK

    /**
     * Recursively traverses context wrappers to find the underlying [FragmentActivity].
     * Essential for Jetpack Compose where [androidx.compose.ui.platform.LocalContext.current]
     * may be wrapped by theme or Hilt wrappers.
     */
    fun findFragmentActivity(context: Context): FragmentActivity? {
        var currentContext: Context? = context
        while (currentContext is ContextWrapper) {
            if (currentContext is FragmentActivity) return currentContext
            currentContext = currentContext.baseContext
        }
        return null
    }

    /**
     * Checks if biometric authentication can be performed on the device.
     */
    fun canAuthenticate(context: Context, authenticators: Int = DEFAULT_AUTHENTICATORS): Boolean {
        val biometricManager = BiometricManager.from(context)
        return biometricManager.canAuthenticate(authenticators) == BiometricManager.BIOMETRIC_SUCCESS
    }

    /**
     * Returns the [BiometricManager] status code for diagnostic feedback.
     */
    fun getBiometricStatus(context: Context, authenticators: Int = DEFAULT_AUTHENTICATORS): Int {
        val biometricManager = BiometricManager.from(context)
        return biometricManager.canAuthenticate(authenticators)
    }

    /**
     * Launches the system [BiometricPrompt].
     *
     * @param context Host Android context (will be unwrapped to find [FragmentActivity]).
     * @param title Title displayed on the biometric prompt dialog.
     * @param subtitle Subtitle description for the prompt.
     * @param negativeButtonText Negative button label (used only when DEVICE_CREDENTIAL is not set).
     * @param allowedAuthenticators Bitmask of allowed authenticators.
     * @param onSuccess Invoked on successful authentication.
     * @param onError Invoked with error message if authentication fails with an actionable error.
     */
    fun authenticate(
        context: Context,
        title: String = "Unlock Koraki",
        subtitle: String = "Use your fingerprint to continue",
        negativeButtonText: String = "Use PIN",
        allowedAuthenticators: Int = DEFAULT_AUTHENTICATORS,
        onSuccess: () -> Unit,
        onError: ((String) -> Unit)? = null,
    ) {
        val fragmentActivity = findFragmentActivity(context)
        if (fragmentActivity == null) {
            onError?.invoke("Authentication not supported on this context.")
            return
        }

        val biometricManager = BiometricManager.from(fragmentActivity)
        if (biometricManager.canAuthenticate(allowedAuthenticators) != BiometricManager.BIOMETRIC_SUCCESS) {
            onError?.invoke("Biometric authentication unavailable.")
            return
        }

        val executor = ContextCompat.getMainExecutor(fragmentActivity)
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                // Do not notify on normal dismissals / cancels / PIN clicks
                if (errorCode != BiometricPrompt.ERROR_USER_CANCELED &&
                    errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON &&
                    errorCode != BiometricPrompt.ERROR_CANCELED
                ) {
                    onError?.invoke(errString.toString())
                }
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                // Biometric failed but prompt stays open for retries
            }
        }

        val prompt = BiometricPrompt(fragmentActivity, executor, callback)
        val promptInfoBuilder = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setAllowedAuthenticators(allowedAuthenticators)

        // Parameter Mutual Exclusion: setNegativeButtonText MUST NOT be set if DEVICE_CREDENTIAL is used
        val hasDeviceCredential = (allowedAuthenticators and BiometricManager.Authenticators.DEVICE_CREDENTIAL) != 0
        if (!hasDeviceCredential) {
            promptInfoBuilder.setNegativeButtonText(negativeButtonText)
        }

        prompt.authenticate(promptInfoBuilder.build())
    }
}
