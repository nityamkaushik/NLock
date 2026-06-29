# NLock — Implementation Blueprint

> **Purpose**: This document is the single source of truth for building NLock. It is written so that a code-generation model can implement it file-by-file with zero guesswork, and a code-review model can verify every file against this spec.

---

## MANDATORY RULES

Every file created or modified MUST follow these rules. Violations will fail code review.

### R1 — Kotlin Style
- Every class, interface, object, and public/internal function MUST have a KDoc comment explaining **why** it exists, not just what it does.
- Every class/function/property MUST have an explicit visibility modifier (`internal`, `private`, or `public`). No implicit `public`.
- One top-level class per file. The filename must match the class name.
- `val` over `var`. `List` over `MutableList` in public APIs.
- No `@Suppress` annotations. Fix the warning instead.
- No `GlobalScope`. Use `viewModelScope`, `lifecycleScope`, or a custom `CoroutineScope(SupervisorJob() + dispatcher)`.
- No magic numbers or strings. Use named constants in a `companion object` or top-level `private const val`.
- No empty `catch` blocks. Log or propagate every exception.
- Use named arguments for functions with more than 2 parameters.

### R2 — Architecture
- **MVVM**: Screen → ViewModel → Repository → (Room + DataStore).
- ViewModels expose `StateFlow` (not `MutableStateFlow`) to the UI.
- UI state is a `sealed interface`. Every `when` branch is exhaustive — no `else` catch-all.
- Repository is the single source of truth. ViewModels never access Room or DataStore directly.

### R3 — Package Structure
- `com.nityam.nlock` — root (Application, MainActivity)
- `com.nityam.nlock.data.db` — Room entities, DAOs, database
- `com.nityam.nlock.data.preferences` — DataStore
- `com.nityam.nlock.data.repository` — repository
- `com.nityam.nlock.service` — AccessibilityService, overlay, receivers
- `com.nityam.nlock.security` — PIN hashing, biometric, keystore
- `com.nityam.nlock.worker` — WorkManager
- `com.nityam.nlock.ui.theme` — colors, theme, typography
- `com.nityam.nlock.ui.navigation` — NavHost
- `com.nityam.nlock.ui.setup` — first-launch setup
- `com.nityam.nlock.ui.lock` — lock screen overlay content
- `com.nityam.nlock.ui.applist` — app list with lock toggles
- `com.nityam.nlock.ui.settings` — settings screen
- `com.nityam.nlock.ui.vault` — vault screen
- `com.nityam.nlock.ui.decoy` — calculator decoy shell
- `com.nityam.nlock.ui.components` — shared composables
- `com.nityam.nlock.util` — OEM helper, package utils, permission checks
- `com.nityam.nlock.notification` — notification masking

### R4 — File Paths
- All Kotlin source: `app/src/main/java/com/nityam/nlock/`
- All test source: `app/src/test/java/com/nityam/nlock/`
- All XML resources: `app/src/main/res/`
- Gradle files: project root and `app/`

### R5 — Dependencies
- **Do NOT add any dependency not listed in this plan.**
- **Do NOT use Argon2, Hilt, Dagger, Koin, or any DI framework.** Manual dependency injection via `NLockApplication`.
- **Do NOT use Material 2.** Use Material 3 only.
- **Do NOT use `dynamicColorScheme`.** Use the custom Precision Vault palette defined in this plan.

---

## PROJECT CONFIGURATION

### Step 1: `gradle/libs.versions.toml`

**Action**: REPLACE entire file content with:

```toml
[versions]
agp = "9.2.1"
coreKtx = "1.19.0"
junit = "4.13.2"
junitVersion = "1.3.0"
espressoCore = "3.7.0"
lifecycleRuntimeKtx = "2.10.0"
lifecycleRuntimeCompose = "2.10.0"
activityCompose = "1.13.0"
kotlin = "2.2.10"
composeBom = "2026.02.01"
ksp = "2.2.10-2.0.2"
room = "2.8.4"
datastore = "1.2.1"
biometric = "1.4.0-alpha07"
navigationCompose = "2.9.8"
workmanager = "2.11.2"
kotlinxCoroutines = "1.10.2"

[libraries]
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "coreKtx" }
junit = { group = "junit", name = "junit", version.ref = "junit" }
androidx-junit = { group = "androidx.test.ext", name = "junit", version.ref = "junitVersion" }
androidx-espresso-core = { group = "androidx.test.espresso", name = "espresso-core", version.ref = "espressoCore" }
androidx-lifecycle-runtime-ktx = { group = "androidx.lifecycle", name = "lifecycle-runtime-ktx", version.ref = "lifecycleRuntimeKtx" }
androidx-lifecycle-runtime-compose = { group = "androidx.lifecycle", name = "lifecycle-runtime-compose", version.ref = "lifecycleRuntimeCompose" }
androidx-activity-compose = { group = "androidx.activity", name = "activity-compose", version.ref = "activityCompose" }
androidx-compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "composeBom" }
androidx-compose-ui = { group = "androidx.compose.ui", name = "ui" }
androidx-compose-ui-graphics = { group = "androidx.compose.ui", name = "ui-graphics" }
androidx-compose-ui-tooling = { group = "androidx.compose.ui", name = "ui-tooling" }
androidx-compose-ui-tooling-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
androidx-compose-ui-test-manifest = { group = "androidx.compose.ui", name = "ui-test-manifest" }
androidx-compose-ui-test-junit4 = { group = "androidx.compose.ui", name = "ui-test-junit4" }
androidx-compose-material3 = { group = "androidx.compose.material3", name = "material3" }
androidx-compose-material-icons-extended = { group = "androidx.compose.material", name = "material-icons-extended" }
androidx-room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
androidx-room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }
androidx-room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
androidx-room-testing = { group = "androidx.room", name = "room-testing", version.ref = "room" }
androidx-datastore-preferences = { group = "androidx.datastore", name = "datastore-preferences", version.ref = "datastore" }
androidx-biometric = { group = "androidx.biometric", name = "biometric", version.ref = "biometric" }
androidx-navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigationCompose" }
androidx-work-runtime-ktx = { group = "androidx.work", name = "work-runtime-ktx", version.ref = "workmanager" }
kotlinx-coroutines-android = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-android", version.ref = "kotlinxCoroutines" }
kotlinx-coroutines-test = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-test", version.ref = "kotlinxCoroutines" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
androidx-room = { id = "androidx.room", version.ref = "room" }
```

---

### Step 2: Root `build.gradle.kts`

**Path**: `d:/MyProjects/NLock/build.gradle.kts`
**Action**: REPLACE entire file content with:

```kotlin
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.androidx.room) apply false
}
```

---

### Step 3: App `build.gradle.kts`

**Path**: `d:/MyProjects/NLock/app/build.gradle.kts`
**Action**: REPLACE entire file content with:

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidx.room)
}

android {
    namespace = "com.nityam.nlock"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.nityam.nlock"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization { enable = false }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    // Navigation
    implementation(libs.androidx.navigation.compose)
    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    // DataStore
    implementation(libs.androidx.datastore.preferences)
    // Biometric
    implementation(libs.androidx.biometric)
    // WorkManager
    implementation(libs.androidx.work.runtime.ktx)
    // Coroutines
    implementation(libs.kotlinx.coroutines.android)
    // Test
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.androidx.room.testing)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
```

---

### Step 4: `AndroidManifest.xml`

**Path**: `app/src/main/AndroidManifest.xml`
**Action**: REPLACE entire file content with:

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_SPECIAL_USE" />
    <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
    <uses-permission android:name="android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS" />
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
    <uses-permission android:name="android.permission.QUERY_ALL_PACKAGES"
        tools:ignore="QueryAllPackagesPermission" />

    <application
        android:name=".NLockApplication"
        android:allowBackup="true"
        android:dataExtractionRules="@xml/data_extraction_rules"
        android:fullBackupContent="@xml/backup_rules"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.NLock">

        <!-- Main launcher activity -->
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:label="@string/app_name"
            android:theme="@style/Theme.NLock"
            android:windowSoftInputMode="adjustResize">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <!-- Disguised launcher alias (disabled by default, toggled at runtime) -->
        <activity-alias
            android:name=".DisguisedLauncherAlias"
            android:targetActivity=".MainActivity"
            android:enabled="false"
            android:icon="@drawable/ic_calculator"
            android:label="@string/decoy_label"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity-alias>

        <!-- Transparent activity to host BiometricPrompt from service context -->
        <activity
            android:name=".ui.lock.BiometricProxyActivity"
            android:exported="false"
            android:theme="@android:style/Theme.Translucent.NoTitleBar"
            android:excludeFromRecents="true"
            android:taskAffinity="" />

        <!-- Core detection service -->
        <service
            android:name=".service.AppLockAccessibilityService"
            android:exported="true"
            android:permission="android.permission.BIND_ACCESSIBILITY_SERVICE">
            <intent-filter>
                <action android:name="android.accessibilityservice.AccessibilityService" />
            </intent-filter>
            <meta-data
                android:name="android.accessibilityservice"
                android:resource="@xml/accessibility_service_config" />
        </service>

        <!-- Notification masking service -->
        <service
            android:name=".notification.AppNotificationMasker"
            android:exported="true"
            android:permission="android.permission.BIND_NOTIFICATION_LISTENER_SERVICE">
            <intent-filter>
                <action android:name="android.service.notification.NotificationListenerService" />
            </intent-filter>
        </service>

        <!-- Restart after boot or app update -->
        <receiver
            android:name=".service.BootReceiver"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.BOOT_COMPLETED" />
                <action android:name="android.intent.action.MY_PACKAGE_REPLACED" />
            </intent-filter>
        </receiver>
    </application>
</manifest>
```

---

### Step 5: `res/xml/accessibility_service_config.xml`

**Action**: CREATE new file.

```xml
<?xml version="1.0" encoding="utf-8"?>
<accessibility-service xmlns:android="http://schemas.android.com/apk/res/android"
    android:accessibilityEventTypes="typeWindowStateChanged"
    android:accessibilityFeedbackType="feedbackGeneric"
    android:accessibilityFlags="flagDefault|flagIncludeNotImportantViews"
    android:canRetrieveWindowContent="false"
    android:notificationTimeout="0"
    android:description="@string/accessibility_service_description" />
```

- `canRetrieveWindowContent="false"` — we only need the package name, not the view tree. Less overhead.
- `notificationTimeout="0"` — zero event batching delay. Events fire instantly.

---

### Step 6: `res/values/strings.xml`

**Action**: REPLACE entire file content with:

```xml
<resources>
    <string name="app_name">NLock</string>
    <string name="decoy_label">Calculator</string>
    <string name="accessibility_service_description">NLock uses this service to detect when a locked app is opened and display the lock screen. NLock does not read or collect any screen content.</string>
    <string name="notification_channel_name">NLock Service</string>
    <string name="notification_channel_description">Keeps NLock running to protect your apps</string>
    <string name="health_check_title">NLock Protection Disabled</string>
    <string name="health_check_text">Tap to re-enable app lock protection</string>
    <string name="biometric_prompt_title">Unlock App</string>
    <string name="biometric_prompt_subtitle">Use your fingerprint to unlock</string>
    <string name="biometric_prompt_negative">Use PIN</string>
</resources>
```

---

### Step 7: `res/drawable/ic_calculator.xml`

**Action**: CREATE new file. Simple calculator vector icon for the decoy alias.

```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24"
    android:tint="?attr/colorControlNormal">
    <path
        android:fillColor="@android:color/white"
        android:pathData="M19,3H5C3.9,3 3,3.9 3,5v14c0,1.1 0.9,2 2,2h14c1.1,0 2,-0.9 2,-2V5C21,3.9 20.1,3 19,3zM13.03,7.06l0.76,-0.76l0.71,0.71l0.71,-0.71l0.76,0.76l-0.71,0.71l0.71,0.71l-0.76,0.76l-0.71,-0.71l-0.71,0.71l-0.76,-0.76l0.71,-0.71L13.03,7.06zM7,7.72h3.5v1.5H7V7.72zM10.5,17.78H7v-1.5h3.5V17.78zM7,14.78v-1.5h3.5v1.5H7zM17,17.28h-1.25v1.25h-1.5v-1.25H13v-1.5h1.25v-1.25h1.5v1.25H17V17.28zM17,10.22h-4v-1.5h4V10.22z" />
</vector>
```

---

## PHASE 1 — Data Layer + Application Skeleton

> **Create in this exact order.** Each file may depend on files listed before it.

### 1.1: `LockedAppEntity.kt`

**Path**: `app/src/main/java/com/nityam/nlock/data/db/LockedAppEntity.kt`

```kotlin
package com.nityam.nlock.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents an app that the user has added to the lock list.
 *
 * Stored in Room. The [packageName] is the primary key because each app
 * can only appear once in the locked-apps list.
 */
@Entity(tableName = "locked_apps")
internal data class LockedAppEntity(
    @PrimaryKey
    @ColumnInfo(name = "package_name")
    val packageName: String,

    @ColumnInfo(name = "is_locked")
    val isLocked: Boolean = true,

    @ColumnInfo(name = "vault_display_name")
    val vaultDisplayName: String? = null,

    @ColumnInfo(name = "vault_icon_uri")
    val vaultIconUri: String? = null,

    @ColumnInfo(name = "grace_period_seconds")
    val gracePeriodSeconds: Int = 0,

    @ColumnInfo(name = "hide_notification_content")
    val hideNotificationContent: Boolean = false,
)
```

---

### 1.2: `LockedAppDao.kt`

**Path**: `app/src/main/java/com/nityam/nlock/data/db/LockedAppDao.kt`

```kotlin
package com.nityam.nlock.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data access object for the [LockedAppEntity] table.
 *
 * All observe* methods return [Flow] for reactive updates.
 * All write methods are suspend functions for structured concurrency.
 */
@Dao
internal interface LockedAppDao {

    @Query("SELECT * FROM locked_apps")
    fun observeAll(): Flow<List<LockedAppEntity>>

    @Query("SELECT * FROM locked_apps WHERE is_locked = 1")
    fun observeLockedApps(): Flow<List<LockedAppEntity>>

    @Query("SELECT package_name FROM locked_apps WHERE is_locked = 1")
    fun observeLockedPackageNames(): Flow<List<String>>

    @Query("SELECT * FROM locked_apps WHERE package_name = :packageName LIMIT 1")
    suspend fun getByPackageName(packageName: String): LockedAppEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(app: LockedAppEntity)

    @Delete
    suspend fun delete(app: LockedAppEntity)
}
```

---

### 1.3: `NLockDatabase.kt`

**Path**: `app/src/main/java/com/nityam/nlock/data/db/NLockDatabase.kt`

```kotlin
package com.nityam.nlock.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Room database for NLock.
 *
 * Contains a single table: [LockedAppEntity].
 * Schema is exported to `app/schemas/` for migration testing.
 */
@Database(
    entities = [LockedAppEntity::class],
    version = 1,
    exportSchema = true,
)
internal abstract class NLockDatabase : RoomDatabase() {
    internal abstract fun lockedAppDao(): LockedAppDao
}
```

---

### 1.4: `AppPreferences.kt`

**Path**: `app/src/main/java/com/nityam/nlock/data/preferences/AppPreferences.kt`

```kotlin
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
```

---

### 1.5: `AppLockRepository.kt`

**Path**: `app/src/main/java/com/nityam/nlock/data/repository/AppLockRepository.kt`

```kotlin
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
}
```

---

### 1.6: `PinHashManager.kt`

**Path**: `app/src/main/java/com/nityam/nlock/security/PinHashManager.kt`

```kotlin
package com.nityam.nlock.security

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Hashes and verifies PINs using PBKDF2WithHmacSHA256.
 *
 * Uses Android's built-in [SecretKeyFactory] — zero external dependencies.
 * Each PIN is hashed with a unique 16-byte random salt and [ITERATION_COUNT]
 * iterations, producing a [KEY_LENGTH_BITS]-bit derived key.
 *
 * The PIN itself is **never** stored or logged.
 */
internal class PinHashManager {

    /**
     * Hashes [pin] with a freshly generated random salt.
     *
     * @return Pair of (Base64-encoded hash, Base64-encoded salt).
     */
    internal fun hashPin(pin: String): Pair<String, String> {
        val salt = ByteArray(SALT_LENGTH_BYTES).also { SecureRandom().nextBytes(it) }
        val hash = deriveKey(pin = pin, salt = salt)
        return Pair(
            Base64.encodeToString(hash, Base64.NO_WRAP),
            Base64.encodeToString(salt, Base64.NO_WRAP),
        )
    }

    /**
     * Verifies [pin] against a previously stored hash and salt.
     *
     * Uses [MessageDigest.isEqual] for constant-time comparison
     * to prevent timing side-channel attacks.
     */
    internal fun verifyPin(
        pin: String,
        storedHashBase64: String,
        storedSaltBase64: String,
    ): Boolean {
        val storedSalt = Base64.decode(storedSaltBase64, Base64.NO_WRAP)
        val storedHash = Base64.decode(storedHashBase64, Base64.NO_WRAP)
        val candidateHash = deriveKey(pin = pin, salt = storedSalt)
        return MessageDigest.isEqual(storedHash, candidateHash)
    }

    private fun deriveKey(pin: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(
            pin.toCharArray(),
            salt,
            ITERATION_COUNT,
            KEY_LENGTH_BITS,
        )
        val factory = SecretKeyFactory.getInstance(ALGORITHM)
        return factory.generateSecret(spec).encoded
    }

    private companion object {
        const val ALGORITHM = "PBKDF2WithHmacSHA256"
        const val ITERATION_COUNT = 120_000
        const val KEY_LENGTH_BITS = 256
        const val SALT_LENGTH_BYTES = 16
    }
}
```

---

### 1.7: `NLockApplication.kt`

**Path**: `app/src/main/java/com/nityam/nlock/NLockApplication.kt`

```kotlin
package com.nityam.nlock

import android.app.Application
import androidx.room.Room
import com.nityam.nlock.data.db.NLockDatabase
import com.nityam.nlock.data.preferences.AppPreferences
import com.nityam.nlock.data.preferences.dataStore
import com.nityam.nlock.data.repository.AppLockRepository
import com.nityam.nlock.security.PinHashManager

/**
 * Application class that initializes all singletons.
 *
 * Manual dependency injection — no DI framework. Every component
 * that needs shared dependencies receives them from here.
 */
internal class NLockApplication : Application() {

    internal lateinit var database: NLockDatabase
        private set

    internal lateinit var preferences: AppPreferences
        private set

    internal lateinit var repository: AppLockRepository
        private set

    internal lateinit var pinHashManager: PinHashManager
        private set

    override fun onCreate() {
        super.onCreate()

        database = Room.databaseBuilder(
            context = applicationContext,
            klass = NLockDatabase::class.java,
            name = DATABASE_NAME,
        ).build()

        preferences = AppPreferences(dataStore = applicationContext.dataStore)

        repository = AppLockRepository(
            dao = database.lockedAppDao(),
            preferences = preferences,
        )

        pinHashManager = PinHashManager()
    }

    private companion object {
        const val DATABASE_NAME = "nlock_database"
    }
}
```

---

### 1.8: Verify Phase 1

**Run**: `./gradlew assembleDebug` from project root.

**Expected**: Build succeeds. No runtime test yet — this phase establishes the data layer and DI graph.

---

## PHASE 2 — Theme & Lock Screen UI

> Build the Precision Vault design system and the lock screen composables.

### 2.1: `Color.kt`

**Path**: `app/src/main/java/com/nityam/nlock/ui/theme/Color.kt`
**Action**: REPLACE entire file.

```kotlin
package com.nityam.nlock.ui.theme

import androidx.compose.ui.graphics.Color

// ── Dark palette (default — app is often checked in low light) ──
internal val DarkBase = Color(0xFF0E0F11)
internal val DarkSurface = Color(0xFF1A1C1F)
internal val DarkTextPrimary = Color(0xFFF2F3F5)
internal val DarkTextSecondary = Color(0xFF9AA0A6)

// ── Light palette ──
internal val LightBase = Color(0xFFF5F5F3)
internal val LightSurface = Color(0xFFFFFFFF)
internal val LightTextPrimary = Color(0xFF16181A)
internal val LightTextSecondary = Color(0xFF6B7075)

// ── Shared across both themes — keeps the app recognizably "NLock" ──
internal val NLockAccent = Color(0xFF5B8BFF)
internal val NLockWarningDark = Color(0xFFFF6B4A)
internal val NLockWarningLight = Color(0xFFE0533A)
```

---

### 2.2: `Theme.kt`

**Path**: `app/src/main/java/com/nityam/nlock/ui/theme/Theme.kt`
**Action**: REPLACE entire file.

Must implement:
- A custom `NLockColors` data class holding `base`, `surface`, `textPrimary`, `textSecondary`, `accent`, `warning`.
- A `LocalNLockColors` `staticCompositionLocalOf`.
- `NLockTheme` composable that reads `isDarkTheme` boolean (passed from ViewModel/DataStore), builds the corresponding `NLockColors`, provides it via `CompositionLocalProvider`, and wraps content in `MaterialTheme` with a mapped `colorScheme` (map `base`→`background`, `surface`→`surface`, `accent`→`primary`, `textPrimary`→`onBackground`/`onSurface`).
- `NLockTheme.colors` extension property for convenient access.
- Do NOT use `dynamicColorScheme`. Always use the Precision Vault palette.

---

### 2.3: `Type.kt`

**Path**: `app/src/main/java/com/nityam/nlock/ui/theme/Type.kt`
**Action**: REPLACE entire file.

Must implement:
- Load Inter font via `GoogleFont` provider.
- `displayLarge` / `headlineLarge`: Inter, weight 600, letterSpacing = -0.02.em — used for PIN digits and screen titles.
- `bodyLarge` / `bodyMedium`: Inter, weight 400, normal spacing — used for labels and settings text.
- `labelSmall`: Inter, weight 500 — used for captions.

---

### 2.4: `TickMarkIndicator.kt`

**Path**: `app/src/main/java/com/nityam/nlock/ui/components/TickMarkIndicator.kt`

Must implement:
- `@Composable internal fun TickMarkIndicator(pinLength: Int, filledCount: Int, modifier: Modifier)`
- Draws a `Row` of `pinLength` vertical bars (2.dp wide, 20.dp tall).
- Unfilled bars: `NLockTheme.colors.textSecondary` at 30% alpha, 1.dp stroke.
- Filled bars: solid `NLockTheme.colors.accent`, animated bottom-up via `animateFloatAsState` with `tween(durationMillis = 80)`.
- Drawn on a `Canvas` composable inside each bar's `Box`.

---

### 2.5: `PinKeypad.kt`

**Path**: `app/src/main/java/com/nityam/nlock/ui/components/PinKeypad.kt`

Must implement:
- `@Composable internal fun PinKeypad(onDigit: (Char) -> Unit, onBackspace: () -> Unit, onConfirm: () -> Unit, modifier: Modifier)`
- 4 rows × 3 columns grid.
- Row 1: 1, 2, 3. Row 2: 4, 5, 6. Row 3: 7, 8, 9. Row 4: ⌫ (backspace), 0, ✓ (confirm).
- Each key: `Box` with `Modifier.clickable` and `indication = rememberRipple()`. No background fill — text only.
- Key text: `NLockTheme.colors.textPrimary`, style `headlineLarge` for digits, `titleLarge` for icons.
- Use Material Icons `Backspace` and `Check` from `material-icons-extended`.

---

### 2.6: `LockScreenContent.kt`

**Path**: `app/src/main/java/com/nityam/nlock/ui/lock/LockScreenContent.kt`

Must implement:
- `@Composable internal fun LockScreenContent(state: LockScreenState, onDigit: (Char) -> Unit, onBackspace: () -> Unit, onConfirm: () -> Unit, onBiometric: () -> Unit, modifier: Modifier)`
- Layout (vertically centered in a `Column`):
  1. App icon — 40.dp, alpha 0.6 (loaded from `state.appIconDrawable`, converted via `rememberDrawablePainter` or similar)
  2. `TickMarkIndicator` — `pinLength = state.pinLength`, `filledCount = state.enteredDigits`
  3. `PinKeypad`
  4. If `state.biometricAvailable`: a fingerprint icon button below the keypad
- Background: `NLockTheme.colors.base` — fills entire screen.
- On wrong PIN: shake animation on the tick marks (translateX oscillation, 4 cycles, 60ms each) + briefly flash `NLockTheme.colors.warning`.
- On correct PIN: ~4.dp downward "settle" micro-motion (spring, <80ms) then dismiss.

---

### 2.7: `LockScreenState.kt` (sealed interface)

**Path**: `app/src/main/java/com/nityam/nlock/ui/lock/LockScreenState.kt`

```kotlin
package com.nityam.nlock.ui.lock

import android.graphics.drawable.Drawable

/**
 * UI state for the lock screen overlay.
 */
internal sealed interface LockScreenState {
    /** Lock screen is waiting for PIN/biometric input. */
    internal data class Locked(
        val targetPackageName: String,
        val appIconDrawable: Drawable?,
        val pinLength: Int,
        val enteredDigits: Int,
        val biometricAvailable: Boolean,
        val showError: Boolean,
    ) : LockScreenState

    /** Authentication succeeded — overlay should dismiss. */
    internal data object Unlocked : LockScreenState
}
```

---

## PHASE 3 — Service Layer

> The AccessibilityService, overlay manager, and receivers.

### 3.1: `ScreenOffReceiver.kt`

**Path**: `app/src/main/java/com/nityam/nlock/service/ScreenOffReceiver.kt`

```kotlin
package com.nityam.nlock.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Clears all per-app grace periods when the screen turns off,
 * ensuring every locked app re-locks on next open.
 *
 * Registered dynamically in [AppLockAccessibilityService] because
 * ACTION_SCREEN_OFF cannot be received by manifest-registered receivers.
 */
internal class ScreenOffReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_SCREEN_OFF) {
            AppLockAccessibilityService.instance?.clearAllGracePeriods()
        }
    }
}
```

---

### 3.2: `BootReceiver.kt`

**Path**: `app/src/main/java/com/nityam/nlock/service/BootReceiver.kt`

```kotlin
package com.nityam.nlock.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.nityam.nlock.worker.ServiceHealthCheckWorker

/**
 * Handles device boot and app-update events.
 *
 * Re-enqueues the [ServiceHealthCheckWorker] to verify the
 * AccessibilityService is still enabled after a reboot or update.
 */
internal class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                ServiceHealthCheckWorker.enqueue(context)
            }
        }
    }
}
```

---

### 3.3: `LockOverlayManager.kt`

**Path**: `app/src/main/java/com/nityam/nlock/service/LockOverlayManager.kt`

Must implement:
- Holds a `ComposeView` and a `WindowManager`.
- `preInflate()`: creates the `ComposeView`, sets up a `SavedStateRegistryOwner` / `ViewModelStoreOwner` / `LifecycleOwner` (use `ServiceLifecycleDispatcher` pattern or custom `LifecycleOwner` that dispatches `ON_CREATE`/`ON_RESUME`/`ON_DESTROY`). Sets content to `LockScreenContent`.
- `show(targetPackage: String)`: calls `windowManager.addView(overlayView, params)` if not already attached. Updates the target package state.
- `dismiss()`: calls `windowManager.removeView(overlayView)` if attached.
- `destroy()`: calls `dismiss()` and cleans up lifecycle.
- `layoutParams`: `TYPE_ACCESSIBILITY_OVERLAY`, `MATCH_PARENT × MATCH_PARENT`, flags `FLAG_NOT_TOUCH_MODAL | FLAG_LAYOUT_IN_SCREEN | FLAG_HARDWARE_ACCELERATED`, format `TRANSLUCENT`.
- The `ComposeView` lifecycle owner MUST be set up correctly or Compose will crash in a service context. This is the most critical implementation detail.

---

### 3.4: `AppLockAccessibilityService.kt`

**Path**: `app/src/main/java/com/nityam/nlock/service/AppLockAccessibilityService.kt`

Must implement exactly the logic described in Phase 1 of the original spec. Key rules:
- `lockedPackages`: `HashSet<String>`, populated from Room `Flow`, updated in a `serviceScope` coroutine.
- `gracePeriodMap`: `ConcurrentHashMap<String, Long>`, stores `packageName → lastUnlockTimestamp`.
- `onAccessibilityEvent`: check `TYPE_WINDOW_STATE_CHANGED` → `lockedPackages.contains(pkg)` → `!isWithinGracePeriod(pkg)` → `overlayManager.show(pkg)`.
- Skip own package (`pkg == packageName`).
- `recordUnlock(packageName)`: called after successful auth, stores current time in `gracePeriodMap`.
- `clearAllGracePeriods()`: called by `ScreenOffReceiver`.
- `onServiceConnected`: init `serviceScope`, `overlayManager`, register `ScreenOffReceiver`, start observing Room + DataStore.
- `onDestroy`: cancel `serviceScope`, unregister receiver, destroy overlay.
- `companion object { var instance: AppLockAccessibilityService? }` — set in `onServiceConnected`, cleared in `onDestroy`.

---

## PHASE 4 — Biometric + Keystore

### 4.1: `KeystoreManager.kt`

**Path**: `app/src/main/java/com/nityam/nlock/security/KeystoreManager.kt`

Must implement:
- `getOrCreateKey()`: loads or generates an AES key in Android Keystore with `setUserAuthenticationRequired(true)` and `setInvalidatedByBiometricEnrollment(true)`.
- `getCipher()`: returns a `Cipher` initialized in `ENCRYPT_MODE` with the key. If the key was invalidated (new biometric enrolled), catches `KeyPermanentlyInvalidatedException`, deletes the old key, generates a new one, and returns a fresh cipher.
- `isKeyValid()`: tries `getCipher()`, returns false if key is invalidated.
- Key alias: `"nlock_biometric_key"`.

### 4.2: `BiometricAuthManager.kt`

**Path**: `app/src/main/java/com/nityam/nlock/security/BiometricAuthManager.kt`

Must implement:
- `canAuthenticate(context: Context): Boolean` — checks `BiometricManager.canAuthenticate(BIOMETRIC_STRONG)`.
- `authenticate(activity: FragmentActivity, onSuccess: () -> Unit, onError: (String) -> Unit)` — creates `BiometricPrompt` with `CryptoObject(cipher)` from `KeystoreManager.getCipher()`. Title/subtitle from string resources. Negative button text "Use PIN". On `onAuthenticationSucceeded` with valid `CryptoObject`, calls `onSuccess`. On failure/error, calls `onError`.
- **CRITICAL**: Never gate unlock on a boolean alone. The `onAuthenticationSucceeded` callback MUST verify `result.cryptoObject != null` before unlocking.

### 4.3: `BiometricProxyActivity.kt`

**Path**: `app/src/main/java/com/nityam/nlock/ui/lock/BiometricProxyActivity.kt`

Must implement:
- A transparent `FragmentActivity` launched from the overlay via `Intent`.
- In `onCreate`: immediately calls `BiometricAuthManager.authenticate(this, ...)`.
- On success: sends result back to `AppLockAccessibilityService.instance` → `overlayManager.dismiss()` → `finish()`.
- On error/cancel: `finish()`.
- Has `android:excludeFromRecents="true"` and empty `taskAffinity` in manifest (already declared).

---

## PHASE 5 — Anti-Loophole + Notification Masking

### 5.1: Recents Handling

In `AppLockAccessibilityService.onAccessibilityEvent`, the existing `lockedPackages.contains(pkg)` check already handles recents: when the user taps a locked app from the recents screen, `TYPE_WINDOW_STATE_CHANGED` fires with that app's package name, which is in the locked set → overlay attaches. **No extra code needed** beyond what Phase 3 already implements.

### 5.2: Grace Period

Already implemented in Phase 3 via `isWithinGracePeriod()` and `recordUnlock()`. Make it configurable from SettingsScreen (Phase 6/7). Options: 0s (default), 15s, 30s, 60s, 300s.

### 5.3: `AppNotificationMasker.kt`

**Path**: `app/src/main/java/com/nityam/nlock/notification/AppNotificationMasker.kt`

Must implement:
- Extends `NotificationListenerService`.
- In `onNotificationPosted(sbn)`: checks if `sbn.packageName` is in the locked-apps set AND has `hideNotificationContent = true`.
- If yes: `cancelNotification(sbn.key)`.
- Reads the locked-app list from `NLockApplication.repository` using a service-scoped coroutine, cached in memory as a `Set<String>`.
- Does NOT re-post a masked notification (simpler approach — just hides it).

---

## PHASE 6 — OEM Reliability + WorkManager

### 6.1: `OemHelper.kt`

**Path**: `app/src/main/java/com/nityam/nlock/util/OemHelper.kt`

Must implement:
- `getManufacturerName(): String` — returns `Build.MANUFACTURER`.
- `needsOemGuide(): Boolean` — returns true for xiaomi, oppo, realme, vivo, samsung, oneplus, huawei, honor.
- `getAutostartIntent(): Intent?` — returns an `Intent` with `ComponentName` for each known OEM's autostart settings screen. Returns null for unknown OEMs. Each `ComponentName` must be wrapped in a `try/catch` that resolves the activity via `PackageManager` before returning.
- `getOemInstructions(): String` — returns a human-readable instruction string per OEM.

Known intents (implement all):
```
xiaomi  → com.miui.securitycenter / com.miui.permcenter.autostart.AutoStartManagementActivity
oppo    → com.coloros.safecenter / com.coloros.safecenter.startupapp.StartupAppListActivity
realme  → (same as oppo)
vivo    → com.vivo.permissionmanager / com.vivo.permissionmanager.activity.BgStartUpManagerActivity
samsung → com.samsung.android.lool / com.samsung.android.sm.devicesecurity.SmartManagerDashBoardActivity
oneplus → com.oneplus.security / com.oneplus.security.chainlaunch.view.ChainLaunchAppListActivity
huawei  → com.huawei.systemmanager / com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity
honor   → (same as huawei)
```

### 6.2: `PermissionHelper.kt`

**Path**: `app/src/main/java/com/nityam/nlock/util/PermissionHelper.kt`

Must implement:
- `isAccessibilityServiceEnabled(context: Context): Boolean` — checks `Settings.Secure.getString(ENABLED_ACCESSIBILITY_SERVICES)` for our service component name.
- `openAccessibilitySettings(context: Context)` — launches `Settings.ACTION_ACCESSIBILITY_SETTINGS`.
- `isIgnoringBatteryOptimizations(context: Context): Boolean` — checks `PowerManager.isIgnoringBatteryOptimizations`.
- `requestBatteryOptimizationExemption(context: Context)` — launches `ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` with our package URI.
- `isNotificationListenerEnabled(context: Context): Boolean` — checks `Settings.Secure.getString(ENABLED_NOTIFICATION_LISTENERS)`.

### 6.3: `PackageUtils.kt`

**Path**: `app/src/main/java/com/nityam/nlock/util/PackageUtils.kt`

Must implement:
- `getInstalledUserApps(context: Context): List<AppInfo>` — queries `PackageManager` for all apps with a launcher intent, excludes system apps (unless they have a launcher intent), excludes our own package. Returns list sorted by label.
- `data class AppInfo(val packageName: String, val label: String, val icon: Drawable)`

### 6.4: `ServiceHealthCheckWorker.kt`

**Path**: `app/src/main/java/com/nityam/nlock/worker/ServiceHealthCheckWorker.kt`

Must implement:
- Extends `CoroutineWorker`.
- `doWork()`: calls `PermissionHelper.isAccessibilityServiceEnabled(applicationContext)`. If false, posts a notification (channel: `NOTIFICATION_CHANNEL_ID = "nlock_health_check"`, title/text from string resources) that opens accessibility settings on tap. Returns `Result.success()`.
- `companion object { fun enqueue(context: Context) }` — uses `PeriodicWorkRequestBuilder<ServiceHealthCheckWorker>(repeatInterval = 15, TimeUnit.MINUTES)` with `ExistingPeriodicWorkPolicy.KEEP` and unique work name `"nlock_health_check"`.

### 6.5: `OemGuideDialog.kt`

**Path**: `app/src/main/java/com/nityam/nlock/ui/components/OemGuideDialog.kt`

Must implement:
- `@Composable internal fun OemGuideDialog(onDismiss: () -> Unit, onOpenSettings: () -> Unit)`
- Shows a dialog with the OEM name, instructions text, and a "Open Settings" button that calls `onOpenSettings`.
- Only shown if `OemHelper.needsOemGuide()` is true.

---

## PHASE 7 — UI Screens + Navigation

### 7.1: Navigation Routes

**Path**: `app/src/main/java/com/nityam/nlock/ui/navigation/NLockNavGraph.kt`

Must implement:
- `internal sealed class NLockRoute(val route: String)` with objects: `Setup`, `AppList`, `Settings`, `Vault`.
- `@Composable internal fun NLockNavGraph(navController: NavHostController, repository: AppLockRepository, pinHashManager: PinHashManager)`
- `NavHost` with `startDestination` determined by `setupComplete` from repository.
- Each `composable(route)` block creates the appropriate ViewModel and Screen.

### 7.2: `SetupScreen.kt` + `SetupViewModel.kt`

**Path**: `app/src/main/java/com/nityam/nlock/ui/setup/`

SetupScreen flow:
1. "Create a PIN" — show `PinKeypad` + `TickMarkIndicator`. User enters 4–6 digits.
2. "Confirm your PIN" — repeat entry. Must match.
3. On match: ViewModel calls `pinHashManager.hashPin(pin)` → `repository.savePinCredentials(hash, salt)`.
4. Navigate to `AppList`.

SetupViewModel state: `sealed interface SetupState { data class EnterPin(...), data class ConfirmPin(...), data class Error(val message: String), data object Complete }`

### 7.3: `AppListScreen.kt` + `AppListViewModel.kt`

**Path**: `app/src/main/java/com/nityam/nlock/ui/applist/`

- Shows all installed user apps (from `PackageUtils.getInstalledUserApps`).
- Each row: app icon, app name, toggle switch.
- Toggle calls `repository.upsertApp(LockedAppEntity(packageName, isLocked = checked))` or `repository.deleteApp(...)`.
- A search bar at the top to filter by app name.
- ViewModel state: `sealed interface AppListState { data object Loading, data class Loaded(val apps: List<AppDisplayItem>), data class Error(val message: String) }`
- `data class AppDisplayItem(val packageName: String, val label: String, val icon: Drawable, val isLocked: Boolean)`

### 7.4: `SettingsScreen.kt` + `SettingsViewModel.kt`

**Path**: `app/src/main/java/com/nityam/nlock/ui/settings/`

Settings items:
1. **Change PIN** — re-runs the setup flow (verify old PIN first).
2. **Biometric unlock** — toggle. On enable: calls `BiometricAuthManager.authenticate` to verify, then `repository.setBiometricEnabled(true)`.
3. **Grace period** — dropdown: 0s, 15s, 30s, 60s, 5m.
4. **Relock on screen off** — toggle (default on).
5. **Theme** — dropdown: System, Light, Dark.
6. **App disguise** — toggle + text field for label.
7. **OEM battery guide** — button, only visible if `OemHelper.needsOemGuide()`.
8. **Notification masking** — button to open notification listener settings.

### 7.5: `VaultScreen.kt` + `VaultViewModel.kt`

**Path**: `app/src/main/java/com/nityam/nlock/ui/vault/`

- Shows only locked apps.
- Each row: editable display name (tap to edit via dialog), editable icon (pick from gallery, stored as URI string in Room), lock indicator dot.
- Flat list — 1px hairline `Divider`, no card shadows.

### 7.6: `DecoyCalculatorScreen.kt`

**Path**: `app/src/main/java/com/nityam/nlock/ui/decoy/DecoyCalculatorScreen.kt`

Must implement:
- A simple calculator UI shell: a display text field at top showing "0", and a 4×4 grid of buttons: `C`, `±`, `%`, `÷`, `7–9`, `×`, `4–6`, `−`, `1–3`, `+`, `0`, `.`, `=`.
- Does NOT perform real arithmetic. All button presses except digits are no-ops (display stays at "0").
- Digit presses are silently appended to an internal PIN buffer.
- When the buffer matches the stored PIN hash (via `PinHashManager.verifyPin`), navigate to the main app / vault.
- If buffer exceeds `maxPinLength`, it resets silently.

### 7.7: `MainActivity.kt`

**Path**: `app/src/main/java/com/nityam/nlock/MainActivity.kt`
**Action**: REPLACE entire file.

Must implement:
- `ComponentActivity` with `enableEdgeToEdge()`.
- Reads `themeMode` and `setupComplete` from `repository` as state.
- If disguise mode is active AND setup is complete, show `DecoyCalculatorScreen` instead of `NLockNavGraph`.
- Wraps content in `NLockTheme(isDarkTheme = ...)`.
- Gets `NLockApplication` via `application as NLockApplication` for dependencies.

---

## VERIFICATION CHECKPOINTS

### After Each Phase

| Phase | Command | Pass Criteria |
|-------|---------|---------------|
| Config | `./gradlew assembleDebug` | Build succeeds, no unresolved dependencies |
| 1 | `./gradlew assembleDebug` | Build succeeds with all data layer classes |
| 2 | `./gradlew assembleDebug` | Build succeeds with theme + UI components |
| 3 | `./gradlew assembleDebug` | Build succeeds with service layer |
| 4 | `./gradlew assembleDebug` | Build succeeds with security layer |
| 5 | `./gradlew assembleDebug` | Build succeeds with notification masker |
| 6 | `./gradlew assembleDebug` | Build succeeds with workers + utils |
| 7 | `./gradlew assembleDebug` | Full build succeeds, all screens wired |

### Unit Tests (run after Phase 7)

```bash
./gradlew testDebugUnitTest
```

| Test File | Location | What It Tests |
|-----------|----------|---------------|
| `PinHashManagerTest.kt` | `test/.../security/` | `hashPin` produces non-empty hash+salt; `verifyPin` returns true for correct PIN; `verifyPin` returns false for wrong PIN; different PINs produce different hashes; same PIN with different salts produces different hashes |
| `GracePeriodLogicTest.kt` | `test/.../service/` | Grace period 0 = always relock; expired grace = relock; active grace = skip; clearAll clears all entries |
| `OemHelperTest.kt` | `test/.../util/` | Each manufacturer string maps to correct intent component; unknown manufacturer returns null |
| `AppLockRepositoryTest.kt` | `test/.../data/repository/` | Upsert/delete round-trip with in-memory Room; `observeLockedPackageNames` emits correct set after changes |

### Manual Test Matrix (device)

| Test | Steps | Expected |
|------|-------|----------|
| Lock works | Enable accessibility → lock an app → open it | Overlay appears instantly (<100ms) |
| PIN unlock | Enter correct PIN | Overlay dismisses with settle animation |
| Wrong PIN | Enter wrong PIN | Tick marks shake, flash warning color |
| Biometric | Enable biometric → open locked app → use fingerprint | Overlay dismisses |
| Screen off relock | Unlock app → screen off → screen on → reopen app | Overlay appears again |
| Grace period | Set 30s → unlock app → reopen within 30s | No overlay |
| Grace period expired | Set 30s → unlock app → wait 31s → reopen | Overlay appears |
| Recents bypass | Open locked app → go to recents → tap it | Overlay appears |
| Notification mask | Enable masking for app → receive notification | Notification hidden |
| App disguise | Enable disguise → check launcher | Shows "Calculator" icon+label |
| Decoy calculator | Open "Calculator" → enter PIN | Opens vault/app |
| Boot persistence | Reboot device | WorkManager checks service, prompts if disabled |
| OEM guide | Open settings on Xiaomi/Samsung | Correct deep-link opens |

---

## FILE CREATION ORDER (complete list)

This is the exact order to create/modify files. Each file may depend only on files listed above it.

```
 1. gradle/libs.versions.toml           (MODIFY — replace entirely)
 2. build.gradle.kts (root)              (MODIFY — replace entirely)
 3. app/build.gradle.kts                 (MODIFY — replace entirely)
 4. res/xml/accessibility_service_config.xml   (CREATE)
 5. res/values/strings.xml               (MODIFY — replace entirely)
 6. res/drawable/ic_calculator.xml       (CREATE)
 7. AndroidManifest.xml                  (MODIFY — replace entirely)
 8. data/db/LockedAppEntity.kt           (CREATE)
 9. data/db/LockedAppDao.kt              (CREATE)
10. data/db/NLockDatabase.kt             (CREATE)
11. data/preferences/AppPreferences.kt   (CREATE)
12. data/repository/AppLockRepository.kt (CREATE)
13. security/PinHashManager.kt           (CREATE)
14. NLockApplication.kt                  (CREATE)
     ── CHECKPOINT: ./gradlew assembleDebug ──
15. ui/theme/Color.kt                    (MODIFY — replace entirely)
16. ui/theme/Theme.kt                    (MODIFY — replace entirely)
17. ui/theme/Type.kt                     (MODIFY — replace entirely)
18. ui/components/TickMarkIndicator.kt   (CREATE)
19. ui/components/PinKeypad.kt           (CREATE)
20. ui/lock/LockScreenState.kt           (CREATE)
21. ui/lock/LockScreenContent.kt         (CREATE)
     ── CHECKPOINT: ./gradlew assembleDebug ──
22. service/ScreenOffReceiver.kt         (CREATE)
23. service/BootReceiver.kt              (CREATE)
24. service/LockOverlayManager.kt        (CREATE)
25. service/AppLockAccessibilityService.kt (CREATE)
     ── CHECKPOINT: ./gradlew assembleDebug ──
26. security/KeystoreManager.kt          (CREATE)
27. security/BiometricAuthManager.kt     (CREATE)
28. ui/lock/BiometricProxyActivity.kt    (CREATE)
29. ui/lock/LockScreenViewModel.kt       (CREATE)
     ── CHECKPOINT: ./gradlew assembleDebug ──
30. notification/AppNotificationMasker.kt (CREATE)
     ── CHECKPOINT: ./gradlew assembleDebug ──
31. util/OemHelper.kt                    (CREATE)
32. util/PermissionHelper.kt             (CREATE)
33. util/PackageUtils.kt                 (CREATE)
34. worker/ServiceHealthCheckWorker.kt   (CREATE)
35. ui/components/OemGuideDialog.kt      (CREATE)
     ── CHECKPOINT: ./gradlew assembleDebug ──
36. ui/navigation/NLockNavGraph.kt       (CREATE)
37. ui/setup/SetupViewModel.kt           (CREATE)
38. ui/setup/SetupScreen.kt              (CREATE)
39. ui/applist/AppListViewModel.kt       (CREATE)
40. ui/applist/AppListScreen.kt          (CREATE)
41. ui/settings/SettingsViewModel.kt     (CREATE)
42. ui/settings/SettingsScreen.kt        (CREATE)
43. ui/vault/VaultViewModel.kt           (CREATE)
44. ui/vault/VaultScreen.kt              (CREATE)
45. ui/decoy/DecoyCalculatorScreen.kt    (CREATE)
46. MainActivity.kt                      (MODIFY — replace entirely)
     ── CHECKPOINT: ./gradlew assembleDebug ──
47. test/security/PinHashManagerTest.kt  (CREATE)
48. test/service/GracePeriodLogicTest.kt (CREATE)
49. test/util/OemHelperTest.kt           (CREATE)
50. test/data/repository/AppLockRepositoryTest.kt (CREATE)
     ── FINAL: ./gradlew testDebugUnitTest ──
```

Total: **50 files** (7 modify, 43 create).
