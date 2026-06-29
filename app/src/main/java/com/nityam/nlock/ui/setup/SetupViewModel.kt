package com.nityam.nlock.ui.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nityam.nlock.data.repository.AppLockRepository
import com.nityam.nlock.security.PinHashManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class SetupViewModel(
    private val repository: AppLockRepository,
    private val pinHashManager: PinHashManager
) : ViewModel() {

    private val _step = MutableStateFlow(SetupStep.ENTER_PIN)
    val step: StateFlow<SetupStep> = _step.asStateFlow()

    private var firstPin = ""

    fun submitPin(pin: String) {
        if (_step.value == SetupStep.ENTER_PIN) {
            firstPin = pin
            _step.value = SetupStep.CONFIRM_PIN
        } else if (_step.value == SetupStep.CONFIRM_PIN) {
            if (pin == firstPin) {
                savePinAndComplete(pin)
            } else {
                _step.value = SetupStep.ERROR_MISMATCH
            }
        }
    }

    fun reset() {
        firstPin = ""
        _step.value = SetupStep.ENTER_PIN
    }

    private fun savePinAndComplete(pin: String) {
        viewModelScope.launch {
            val (hash, salt) = pinHashManager.hashPin(pin)
            repository.savePinCredentials(hash, salt)
            _step.value = SetupStep.DONE
        }
    }
}

internal enum class SetupStep {
    ENTER_PIN, CONFIRM_PIN, ERROR_MISMATCH, DONE
}
