package com.nityam.nlock.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Singleton DataStore instance, scoped to the application [Context]. */
internal val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "nlock_prefs")

/**
 * Typed wrapper around DataStore preferences.
 *
 * Provides read ([Flow]) and write (suspend) access to all lightweight
 * settings. Heavy relational data lives in Room instead.
 */
internal class AppPreferences(private val dataStore: DataStore<Preferences>) {

    // ── Keys ──

    private companion object {
        val PIN_HASH = stringPreferencesKey("pin_hash")
        val PIN_SALT = stringPreferencesKey("pin_salt")
        val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
        val SCREEN_OFF_RELOCK = booleanPreferencesKey("screen_off_relock")
        val GRACE_PERIOD_SECONDS = intPreferencesKey("grace_period_seconds")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val APP_DISGUISE_ENABLED = booleanPreferencesKey("app_disguise_enabled")
        val APP_DISGUISE_LABEL = stringPreferencesKey("app_disguise_label")
        val SETUP_COMPLETE = booleanPreferencesKey("setup_complete")
    }

    // ── Reads (Flow) ──

    val pinHash: Flow<String?> = dataStore.data.map { it[PIN_HASH] }
    val pinSalt: Flow<String?> = dataStore.data.map { it[PIN_SALT] }
    val biometricEnabled: Flow<Boolean> = dataStore.data.map { it[BIOMETRIC_ENABLED] ?: false }
    val screenOffRelock: Flow<Boolean> = dataStore.data.map { it[SCREEN_OFF_RELOCK] ?: true }
    val gracePeriodSeconds: Flow<Int> = dataStore.data.map { it[GRACE_PERIOD_SECONDS] ?: 0 }
    val themeMode: Flow<String> = dataStore.data.map { it[THEME_MODE] ?: "system" }
    val appDisguiseEnabled: Flow<Boolean> = dataStore.data.map { it[APP_DISGUISE_ENABLED] ?: false }
    val appDisguiseLabel: Flow<String> = dataStore.data.map { it[APP_DISGUISE_LABEL] ?: "Calculator" }
    val setupComplete: Flow<Boolean> = dataStore.data.map { it[SETUP_COMPLETE] ?: false }

    // ── Writes (suspend) ──

    internal suspend fun savePinCredentials(hash: String, salt: String) {
        dataStore.edit { prefs ->
            prefs[PIN_HASH] = hash
            prefs[PIN_SALT] = salt
            prefs[SETUP_COMPLETE] = true
        }
    }

    internal suspend fun setBiometricEnabled(enabled: Boolean) {
        dataStore.edit { it[BIOMETRIC_ENABLED] = enabled }
    }

    internal suspend fun setScreenOffRelock(enabled: Boolean) {
        dataStore.edit { it[SCREEN_OFF_RELOCK] = enabled }
    }

    internal suspend fun setGracePeriodSeconds(seconds: Int) {
        dataStore.edit { it[GRACE_PERIOD_SECONDS] = seconds }
    }

    internal suspend fun setThemeMode(mode: String) {
        dataStore.edit { it[THEME_MODE] = mode }
    }

    internal suspend fun setAppDisguise(enabled: Boolean, label: String) {
        dataStore.edit { prefs ->
            prefs[APP_DISGUISE_ENABLED] = enabled
            prefs[APP_DISGUISE_LABEL] = label
        }
    }
}
