package com.nityam.nlock.ui.applist

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nityam.nlock.data.db.LockedAppEntity
import com.nityam.nlock.data.repository.AppLockRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
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
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val apps: StateFlow<List<AppListItem>> = combine(_apps, _searchQuery) { appList, query ->
        if (query.isBlank()) {
            appList
        } else {
            appList.filter { it.label.contains(query, ignoreCase = true) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadApps()
    }

    private fun loadApps() {
        viewModelScope.launch {
            combine(repository.observeAllApps(), repository.showSystemApps) { lockedEntities, showSystem ->
                Pair(lockedEntities, showSystem)
            }.collectLatest { (lockedEntities, showSystem) ->
                val lockedMap = lockedEntities.associateBy { it.packageName }
                
                val installedPackages = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
                val appList = installedPackages.mapNotNull { appInfo ->
                    val intent = packageManager.getLaunchIntentForPackage(appInfo.packageName)
                    val isSystemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0 || 
                                      (appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0

                    if (intent != null && appInfo.packageName != "com.nityam.nlock") {
                        if (!showSystem && isSystemApp) {
                            return@mapNotNull null
                        }
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

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
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
