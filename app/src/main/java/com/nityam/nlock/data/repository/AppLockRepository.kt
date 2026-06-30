package com.nityam.nlock.data.repository

import com.nityam.nlock.data.db.LockedAppDao
import com.nityam.nlock.data.db.LockedAppEntity
import com.nityam.nlock.data.preferences.AppPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Single source of truth for all app-lock data.
 *
 * Combines Room (locked apps, vault aliases) and DataStore (PIN, settings).
 * ViewModels and the AccessibilityService read from this class — never
 * from Room or DataStore directly.
 */
internal class AppLockRepository(
    private val dao: LockedAppDao,
    private val preferences: AppPreferences,
) {
    // ── Locked apps (Room) ──

    fun observeAllApps(): Flow<List<LockedAppEntity>> = dao.observeAll()
    fun observeLockedApps(): Flow<List<LockedAppEntity>> = dao.observeLockedApps()
    fun observeLockedPackageNames(): Flow<List<String>> = dao.observeLockedPackageNames()
    suspend fun getApp(packageName: String): LockedAppEntity? = dao.getByPackageName(packageName)
    suspend fun upsertApp(app: LockedAppEntity) = dao.upsert(app)
    suspend fun deleteApp(app: LockedAppEntity) = dao.delete(app)

    // ── Preferences (DataStore) ──

    val pinHash: Flow<String?> = preferences.pinHash
    val pinSalt: Flow<String?> = preferences.pinSalt
    val biometricEnabled: Flow<Boolean> = preferences.biometricEnabled
    val screenOffRelock: Flow<Boolean> = preferences.screenOffRelock
    val themeMode: Flow<String> = preferences.themeMode
    val setupComplete: Flow<Boolean> = preferences.setupComplete
    val appDisguiseEnabled: Flow<Boolean> = preferences.appDisguiseEnabled
    val appDisguiseLabel: Flow<String> = preferences.appDisguiseLabel
    val showSystemApps: Flow<Boolean> = preferences.showSystemApps
    val uninstallProtection: Flow<Boolean> = preferences.uninstallProtection
    val requirePasswordForNlock: Flow<Boolean> = preferences.requirePasswordForNlock

    /** Grace period as milliseconds for the service hot path. */
    fun observeGracePeriodMs(): Flow<Long> =
        preferences.gracePeriodSeconds.map { it * 1000L }

    suspend fun savePinCredentials(hash: String, salt: String) =
        preferences.savePinCredentials(hash = hash, salt = salt)

    suspend fun setBiometricEnabled(enabled: Boolean) =
        preferences.setBiometricEnabled(enabled)

    suspend fun setScreenOffRelock(enabled: Boolean) =
        preferences.setScreenOffRelock(enabled)

    suspend fun setGracePeriodSeconds(seconds: Int) =
        preferences.setGracePeriodSeconds(seconds)

    suspend fun setThemeMode(mode: String) =
        preferences.setThemeMode(mode)

    suspend fun setAppDisguise(enabled: Boolean, label: String) =
        preferences.setAppDisguise(enabled = enabled, label = label)

    suspend fun setShowSystemApps(enabled: Boolean) =
        preferences.setShowSystemApps(enabled)

    suspend fun setUninstallProtection(enabled: Boolean) =
        preferences.setUninstallProtection(enabled)

    suspend fun setRequirePasswordForNlock(enabled: Boolean) =
        preferences.setRequirePasswordForNlock(enabled)
}
