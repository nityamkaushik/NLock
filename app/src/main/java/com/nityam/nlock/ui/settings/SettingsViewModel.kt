package com.nityam.nlock.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nityam.nlock.data.repository.AppLockRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

internal class SettingsViewModel(
    private val repository: AppLockRepository
) : ViewModel() {

    val biometricEnabled: StateFlow<Boolean> = repository.biometricEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val screenOffRelock: StateFlow<Boolean> = repository.screenOffRelock
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val appDisguiseEnabled: StateFlow<Boolean> = repository.appDisguiseEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun setBiometricEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setBiometricEnabled(enabled)
        }
    }

    fun setScreenOffRelock(enabled: Boolean) {
        viewModelScope.launch {
            repository.setScreenOffRelock(enabled)
        }
    }

    fun toggleAppDisguise(enabled: Boolean) {
        viewModelScope.launch {
            repository.setAppDisguise(enabled, "Calculator")
        }
    }
}
