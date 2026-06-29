package com.nityam.nlock.ui.lock

import android.graphics.drawable.Drawable

/**
 * UI state for the lock screen overlay.
 */
internal sealed interface LockScreenState {
    /** Lock screen is waiting for PIN/biometric input. */
    data class Locked(
        val targetPackageName: String,
        val appIconDrawable: Drawable?,
        val pinLength: Int,
        val enteredDigits: Int,
        val biometricAvailable: Boolean,
        val isBiometricPrompting: Boolean,
        val showError: Boolean,
    ) : LockScreenState

    /** No lock screen needed right now. */

    /** No lock screen needed right now. */
    data object Idle : LockScreenState
}
