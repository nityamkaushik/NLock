package com.nityam.nlock.ui.lock

import android.graphics.drawable.Drawable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nityam.nlock.data.repository.AppLockRepository
import com.nityam.nlock.security.PinHashManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for managing the state of the lock screen overlay.
 *
 * Created once by [com.nityam.nlock.service.LockOverlayManager] and reused
 * across all lock events. Call [prepare] each time a new app needs locking.
 */
internal class LockScreenViewModel(
    private val repository: AppLockRepository,
    private val pinHashManager: PinHashManager,
    private val onUnlock: (String) -> Unit
) : ViewModel() {

    private val _state = MutableStateFlow<LockScreenState>(LockScreenState.Idle)
    val state: StateFlow<LockScreenState> = _state.asStateFlow()

    private var pinBuffer = ""
    private var cachedHash: String? = null
    private var cachedSalt: String? = null

    /**
     * Reconfigure the lock screen for a new target app.
     * Resets the PIN buffer and sets up a fresh Locked state.
     */
    fun prepare(
        packageName: String,
        icon: Drawable?,
        biometricAvailable: Boolean,
        pinLength: Int = 4,
    ) {
        pinBuffer = ""
        _state.value = LockScreenState.Locked(
            targetPackageName = packageName,
            appIconDrawable = icon,
            pinLength = pinLength,
            enteredDigits = 0,
            biometricAvailable = biometricAvailable,
            isBiometricPrompting = false,
            showError = false,
        )
        
        // Prefetch hash and salt for faster validation
        viewModelScope.launch {
            if (cachedHash == null) {
                cachedHash = repository.pinHash.firstOrNull()
                cachedSalt = repository.pinSalt.firstOrNull()
            }
        }
    }

    fun setBiometricPrompting(isPrompting: Boolean) {
        _state.update {
            if (it is LockScreenState.Locked) {
                it.copy(isBiometricPrompting = isPrompting)
            } else it
        }
    }

    /** Reset to idle (overlay dismissed). */
    fun reset() {
        pinBuffer = ""
        _state.value = LockScreenState.Idle
    }

    fun onDigit(digit: Char) {
        val currentState = _state.value as? LockScreenState.Locked ?: return
        if (pinBuffer.length >= currentState.pinLength) return

        pinBuffer += digit
        val newCount = pinBuffer.length
        _state.update {
            currentState.copy(
                enteredDigits = newCount,
                showError = false,
            )
        }

        // Auto-confirm when PIN length is reached
        if (newCount == currentState.pinLength) {
            onConfirm()
        }
    }

    fun onBackspace() {
        val currentState = _state.value as? LockScreenState.Locked ?: return
        if (pinBuffer.isEmpty()) return

        pinBuffer = pinBuffer.dropLast(1)
        _state.update {
            currentState.copy(
                enteredDigits = pinBuffer.length,
                showError = false,
            )
        }
    }

    fun onConfirm() {
        val currentState = _state.value as? LockScreenState.Locked ?: return
        if (pinBuffer.isEmpty()) return

        viewModelScope.launch {
            val hash = cachedHash ?: repository.pinHash.firstOrNull() ?: return@launch
            val salt = cachedSalt ?: repository.pinSalt.firstOrNull() ?: return@launch

            val isCorrect = pinHashManager.verifyPin(pinBuffer, hash, salt)

            if (isCorrect) {
                onUnlock(currentState.targetPackageName)
            } else {
                pinBuffer = ""
                _state.value = currentState.copy(
                    enteredDigits = 0,
                    showError = true,
                )
                // Clear error state after shake animation
                delay(400)
                _state.update {
                    (it as? LockScreenState.Locked)?.copy(showError = false) ?: it
                }
            }
        }
    }

    /** Called when biometric auth succeeds (from LockScreenActivity). */
    fun onBiometricSuccess() {
        val currentState = _state.value as? LockScreenState.Locked ?: return
        onUnlock(currentState.targetPackageName)
    }
}
