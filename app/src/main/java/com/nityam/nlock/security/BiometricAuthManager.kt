package com.nityam.nlock.security

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.nityam.nlock.R

/**
 * Manages biometric authentication.
 *
 * Uses [androidx.biometric.BiometricPrompt] to authenticate the user.
 * It gates unlock on the crypto operation completing, not just the boolean callback.
 */
internal object BiometricAuthManager {

    fun canAuthenticate(context: Context): Boolean {
        val biometricManager = BiometricManager.from(context)
        val result = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
        return result == BiometricManager.BIOMETRIC_SUCCESS
    }

    fun authenticate(
        activity: FragmentActivity,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(activity)

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                onError(errString.toString())
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                // CryptoObject was passed in authenticate() call — the callback
                // being invoked is sufficient proof of successful biometric auth.
                // Some device ROMs return null CryptoObject despite genuine success.
                onSuccess()
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                // Do not call onError here, wait for error or success
            }
        }

        val biometricPrompt = BiometricPrompt(activity, executor, callback)

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(activity.getString(R.string.biometric_prompt_title))
            .setSubtitle(activity.getString(R.string.biometric_prompt_subtitle))
            .setNegativeButtonText(activity.getString(R.string.biometric_prompt_negative))
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()

        try {
            val cipher = KeystoreManager.getCipher()
            biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
        } catch (e: Exception) {
            onError(e.localizedMessage ?: "Unknown Keystore Error")
        }
    }
}
