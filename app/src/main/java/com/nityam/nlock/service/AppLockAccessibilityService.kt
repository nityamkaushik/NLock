package com.nityam.nlock.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.content.IntentFilter
import android.view.accessibility.AccessibilityEvent
import com.nityam.nlock.NLockApplication
import com.nityam.nlock.ui.lock.LockScreenActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

/**
 * Core app-lock detection service.
 *
 * Listens for [AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED] and checks the
 * foreground package against an in-memory [HashSet] of locked packages.
 * The set is kept in sync with Room via a [Flow] — no database I/O on the hot path.
 */
internal class AppLockAccessibilityService : AccessibilityService() {

    private val lockedPackages = HashSet<String>()
    private val gracePeriodMap = ConcurrentHashMap<String, Long>()
    private var currentGracePeriodMs: Long = 0L
    private var currentForegroundPackage: String? = null
    private var activelyUnlockedPackage: String? = null
    internal var isBiometricEnabled: Boolean = false
        private set
    private var isSetupComplete: Boolean = false
    private var uninstallProtectionEnabled: Boolean = true
    private var requirePasswordForNlockEnabled: Boolean = true

    internal lateinit var overlayManager: LockOverlayManager
        private set
    private lateinit var screenOffReceiver: ScreenOffReceiver
    private lateinit var serviceScope: CoroutineScope

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        overlayManager = LockOverlayManager(service = this)
        overlayManager.preInflate()
        registerScreenOffReceiver()
        observeLockedPackages()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val pkg = event.packageName?.toString() ?: return
        val className = event.className?.toString() ?: ""

        if (pkg == "com.android.systemui") return
        if (pkg == packageName) {
            // Do not intercept if our lock screen is currently in the foreground (prevents infinite loops)
            if (LockScreenActivity.currentInstance != null) return
            // If self-lock is disabled or setup isn't complete, ignore our own package
            if (!requirePasswordForNlockEnabled || !isSetupComplete) return
        }

        if (className.contains("InputMethod") || className.contains("SoftInput") || pkg.contains("inputmethod") || pkg.contains("keyboard")) return

        if (pkg != currentForegroundPackage) {
            if (activelyUnlockedPackage != null && activelyUnlockedPackage != pkg) {
                val isUninstallTransition = uninstallProtectionEnabled &&
                        (activelyUnlockedPackage == "com.android.settings" || activelyUnlockedPackage == "com.google.android.packageinstaller") &&
                        (pkg == "com.android.settings" || pkg == "com.google.android.packageinstaller")

                if (isUninstallTransition) {
                    // Treat Settings and PackageInstaller as the same logical app to prevent double prompts
                    val prevUnlockTime = gracePeriodMap[activelyUnlockedPackage!!] ?: System.currentTimeMillis()
                    gracePeriodMap[pkg] = prevUnlockTime
                    activelyUnlockedPackage = pkg
                } else {
                    if (currentGracePeriodMs <= 0L) {
                        gracePeriodMap.remove(activelyUnlockedPackage!!)
                    }
                    activelyUnlockedPackage = null
                }
            }
            currentForegroundPackage = pkg

            if (overlayManager.isAttached && overlayManager.currentTargetPackage != pkg) {
                // Only dismiss if the lock screen is not actively in the foreground
                // (biometric dialogs from OEM packages could falsely trigger this)
                if (!LockScreenActivity.isInForeground) {
                    overlayManager.dismiss()
                }
            }
        }

        val isUninstallProtectionApp = uninstallProtectionEnabled && 
                (pkg == "com.android.settings" || pkg == "com.google.android.packageinstaller")
        val isSelfLockApp = requirePasswordForNlockEnabled && pkg == packageName && isSetupComplete

        if (pkg in lockedPackages || isUninstallProtectionApp || isSelfLockApp) {
            if (pkg == activelyUnlockedPackage) return

            val bypassGracePeriod = (pkg == packageName && requirePasswordForNlockEnabled)

            if (bypassGracePeriod || !isWithinGracePeriod(pkg)) {
                overlayManager.show(targetPackage = pkg)
            }
        }
    }

    override fun onInterrupt() { /* Required override — no action needed */ }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        serviceScope.cancel()
        unregisterReceiver(screenOffReceiver)
        overlayManager.destroy()
    }

    // ── Grace period ──

    internal fun recordUnlock(packageName: String) {
        gracePeriodMap[packageName] = System.currentTimeMillis()
        activelyUnlockedPackage = packageName
    }

    internal fun clearAllGracePeriods() {
        gracePeriodMap.clear()
        activelyUnlockedPackage = null
        currentForegroundPackage = null
    }

    private fun isWithinGracePeriod(packageName: String): Boolean {
        if (currentGracePeriodMs <= 0L) return false
        val lastUnlocked = gracePeriodMap[packageName] ?: return false
        return System.currentTimeMillis() - lastUnlocked < currentGracePeriodMs
    }

    // ── Setup ──

    private fun registerScreenOffReceiver() {
        screenOffReceiver = ScreenOffReceiver()
        val filter = IntentFilter(Intent.ACTION_SCREEN_OFF)
        registerReceiver(screenOffReceiver, filter)
    }

    private fun observeLockedPackages() {
        val app = application as NLockApplication
        serviceScope.launch {
            app.repository.observeLockedPackageNames().collect { packages ->
                lockedPackages.clear()
                lockedPackages.addAll(packages)
            }
        }
        serviceScope.launch {
            app.repository.observeGracePeriodMs().collect { ms ->
                currentGracePeriodMs = ms
            }
        }
        serviceScope.launch {
            app.repository.biometricEnabled.collect { enabled ->
                isBiometricEnabled = enabled
            }
        }
        serviceScope.launch {
            app.repository.setupComplete.collect { complete ->
                isSetupComplete = complete
            }
        }
        serviceScope.launch {
            app.repository.uninstallProtection.collect { enabled ->
                uninstallProtectionEnabled = enabled
            }
        }
        serviceScope.launch {
            app.repository.requirePasswordForNlock.collect { enabled ->
                requirePasswordForNlockEnabled = enabled
            }
        }
    }

    internal companion object {
        /** Weak reference for [ScreenOffReceiver] to call back into. */
        @Volatile
        internal var instance: AppLockAccessibilityService? = null
            private set
    }
}
