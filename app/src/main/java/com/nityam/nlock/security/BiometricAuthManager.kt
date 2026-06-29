package com.nityam.nlock.security

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.nityam.nlock.R

/**
 * Manages biometric authentication (fingerprint / face).
 *
 * Uses [BIOMETRIC_WEAK] to support the widest range of devices — both Class 2
 * (weak, e.g. some OEM fingerprint sensors) and Class 3 (strong) biometrics.
 *
 * A "Use PIN" negative button is shown so the user can fall back to our custom
 * PIN pad without ever seeing the system's own credential screen.
 */
internal object BiometricAuthManager {

    /**
     * Whether the device has at least one enrolled biometric (fingerprint / face).
     */
    fun canAuthenticate(context: Context): Boolean {
        val biometricManager = BiometricManager.from(context)
        val result = biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_WEAK
        )
        return result == BiometricManager.BIOMETRIC_SUCCESS
    }

    /**
     * Show the system biometric prompt.
     *
     * @param onSuccess  Called when biometric auth succeeds.
     * @param onError    Called on system error or user cancel (swipe away).
     * @param onUsePin   Called when the user taps the "Use PIN" button.
     */
    fun authenticate(
        activity: FragmentActivity,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        onUsePin: (() -> Unit)? = null,
    ) {
        val executor = ContextCompat.getMainExecutor(activity)

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                when (errorCode) {
                    BiometricPrompt.ERROR_NEGATIVE_BUTTON -> {
                        // User tapped "Use PIN"
                        onUsePin?.invoke() ?: onError(errString.toString())
                    }
                    else -> {
                        // User cancelled, lockout, HW error, etc.
                        onError(errString.toString())
                    }
                }
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess()
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                // Single attempt failed (wrong finger) — system handles retries.
            }
        }

        val biometricPrompt = BiometricPrompt(activity, executor, callback)

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(activity.getString(R.string.biometric_prompt_title))
            .setSubtitle(activity.getString(R.string.biometric_prompt_subtitle))
            .setNegativeButtonText(activity.getString(R.string.biometric_prompt_negative))
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_WEAK)
            .build()

        try {
            biometricPrompt.authenticate(promptInfo)
        } catch (e: Exception) {
            onError(e.localizedMessage ?: "Unknown Biometric Error")
        }
    }
}
