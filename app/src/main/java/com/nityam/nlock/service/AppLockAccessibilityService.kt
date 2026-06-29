package com.nityam.nlock.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.content.IntentFilter
import android.view.accessibility.AccessibilityEvent
import com.nityam.nlock.NLockApplication
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
        if (pkg == packageName) return

        if (pkg in lockedPackages && !isWithinGracePeriod(pkg)) {
            overlayManager.show(targetPackage = pkg)
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
    }

    internal fun clearAllGracePeriods() {
        gracePeriodMap.clear()
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
    }

    internal companion object {
        /** Weak reference for [ScreenOffReceiver] to call back into. */
        @Volatile
        internal var instance: AppLockAccessibilityService? = null
            private set
    }
}
