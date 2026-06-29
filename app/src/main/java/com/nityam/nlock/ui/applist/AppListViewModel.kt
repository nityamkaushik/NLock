package com.nityam.nlock.ui.applist

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nityam.nlock.data.db.LockedAppEntity
import com.nityam.nlock.data.repository.AppLockRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

internal data class AppListItem(
    val packageName: String,
    val label: String,
    val isLocked: Boolean
)

internal class AppListViewModel(
    private val repository: AppLockRepository,
    private val packageManager: PackageManager
) : ViewModel() {

    private val _apps = MutableStateFlow<List<AppListItem>>(emptyList())
    val apps: StateFlow<List<AppListItem>> = _apps.asStateFlow()

    init {
        loadApps()
    }

    private fun loadApps() {
        viewModelScope.launch {
            repository.observeAllApps().collectLatest { lockedEntities ->
                val lockedMap = lockedEntities.associateBy { it.packageName }
                
                val installedPackages = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
                val appList = installedPackages.mapNotNull { appInfo ->
                    val intent = packageManager.getLaunchIntentForPackage(appInfo.packageName)
                    if (intent != null && appInfo.packageName != "com.nityam.nlock") {
                        val label = packageManager.getApplicationLabel(appInfo).toString()
                        val isLocked = lockedMap[appInfo.packageName]?.isLocked == true
                        AppListItem(appInfo.packageName, label, isLocked)
                    } else {
                        null
                    }
                }.sortedBy { it.label }
                
                _apps.value = appList
            }
        }
    }

    fun toggleAppLock(packageName: String, isLocked: Boolean) {
        viewModelScope.launch {
            if (isLocked) {
                repository.upsertApp(LockedAppEntity(packageName = packageName, isLocked = true))
            } else {
                repository.deleteApp(LockedAppEntity(packageName = packageName))
            }
        }
    }
}
