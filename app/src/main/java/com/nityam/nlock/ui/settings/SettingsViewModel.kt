package com.nityam.nlock.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nityam.nlock.data.repository.AppLockRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
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

    val showSystemApps: StateFlow<Boolean> = repository.showSystemApps
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val gracePeriodSeconds: StateFlow<Int> = repository.observeGracePeriodMs()
        .map { (it / 1000L).toInt() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

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

    fun setShowSystemApps(enabled: Boolean) {
        viewModelScope.launch {
            repository.setShowSystemApps(enabled)
        }
    }

    fun setGracePeriodSeconds(seconds: Int) {
        viewModelScope.launch {
            repository.setGracePeriodSeconds(seconds)
        }
    }
}
