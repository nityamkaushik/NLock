package com.nityam.nlock.ui.lock

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.fragment.app.FragmentActivity
import com.nityam.nlock.NLockApplication
import com.nityam.nlock.security.BiometricAuthManager
import com.nityam.nlock.service.AppLockAccessibilityService
import com.nityam.nlock.ui.theme.NLockTheme

/**
 * Full-screen lock screen activity — works like a native Android lock screen.
 *
 * Flow:
 * 1. Appears instantly when a locked app is detected (dark background matches blocker overlay).
 * 2. If biometrics are available, the system fingerprint dialog is shown immediately.
 * 3. User can authenticate via fingerprint OR tap "Use PIN" to fall back to the PIN keypad.
 * 4. Back button sends the user home — they can never bypass the lock.
 *
 * Launched by [com.nityam.nlock.service.LockOverlayManager] and communicates
 * unlock events back to [AppLockAccessibilityService].
 */
internal class LockScreenActivity : FragmentActivity() {

    private lateinit var viewModel: LockScreenViewModel
    private var targetPackage: String = ""
    private var biometricTriggered = false

    override fun onCreate(savedInstanceState: Bundle?) {
        @Suppress("DEPRECATION")
        overridePendingTransition(0, 0)
        super.onCreate(savedInstanceState)

        // Set window background to match the blocker overlay for a seamless transition
        window.decorView.setBackgroundColor(android.graphics.Color.parseColor("#111318"))
        @Suppress("DEPRECATION")
        window.statusBarColor = android.graphics.Color.parseColor("#111318")
        @Suppress("DEPRECATION")
        window.navigationBarColor = android.graphics.Color.parseColor("#111318")

        currentInstance = this
        isInForeground = true

        targetPackage = intent?.getStringExtra(EXTRA_TARGET_PACKAGE) ?: run {
            finish()
            return
        }

        val app = application as NLockApplication
        viewModel = LockScreenViewModel(
            repository = app.repository,
            pinHashManager = app.pinHashManager,
            onUnlock = { pkg ->
                val svc = AppLockAccessibilityService.instance
                svc?.recordUnlock(pkg)
                svc?.overlayManager?.dismiss()
                viewModel.reset()
            }
        )

        setupLockScreen(targetPackage)

        setContent {
            NLockTheme {
                val state by viewModel.state.collectAsState()
                LockScreenContent(
                    state = state,
                    onDigit = viewModel::onDigit,
                    onBackspace = viewModel::onBackspace,
                    onConfirm = viewModel::onConfirm,
                    onBiometric = ::promptBiometric,
                )
            }
        }

        // Reveal the activity behind the blocker (same color, so seamless)
        window.decorView.post {
            AppLockAccessibilityService.instance?.overlayManager?.hideBlocker()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val newTarget = intent.getStringExtra(EXTRA_TARGET_PACKAGE) ?: return
        if (newTarget != targetPackage) {
            targetPackage = newTarget
            biometricTriggered = false
            setupLockScreen(newTarget)
        }
    }

    private fun setupLockScreen(pkg: String) {
        val icon = try {
            packageManager.getApplicationIcon(pkg)
        } catch (_: Exception) { null }

        val svc = AppLockAccessibilityService.instance
        val biometricEnabled = svc?.isBiometricEnabled == true
        val biometricAvailable = biometricEnabled && BiometricAuthManager.canAuthenticate(this)

        viewModel.prepare(
            packageName = pkg,
            icon = icon,
            biometricAvailable = biometricAvailable,
        )

        // Auto-prompt biometrics on first show
        if (biometricAvailable && !biometricTriggered) {
            biometricTriggered = true
            viewModel.setBiometricPrompting(true)
            // Small delay to let the activity render first, then show biometric dialog on top
            window.decorView.postDelayed(::promptBiometric, 100)
        }
    }

    private fun promptBiometric() {
        if (isFinishing || isDestroyed) return
        viewModel.setBiometricPrompting(true)
        BiometricAuthManager.authenticate(
            activity = this,
            onSuccess = { viewModel.onBiometricSuccess() },
            onError = { viewModel.setBiometricPrompting(false) },
            onUsePin = { viewModel.setBiometricPrompting(false) },
        )
    }

    override fun onResume() {
        super.onResume()
        isInForeground = true
    }

    override fun onPause() {
        super.onPause()
        isInForeground = false
    }

    @Deprecated("Deprecated in Java")
    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        // Send user home — never let them go back to the locked app
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(homeIntent)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (currentInstance == this) {
            currentInstance = null
        }
        isInForeground = false
    }

    override fun finish() {
        super.finish()
        @Suppress("DEPRECATION")
        overridePendingTransition(0, 0)
    }

    companion object {
        const val EXTRA_TARGET_PACKAGE = "extra_target_package"

        /** Direct reference for [LockOverlayManager] to finish this activity. */
        @Volatile var currentInstance: LockScreenActivity? = null
            private set

        /** True while the activity is between onResume and onPause. */
        @Volatile var isInForeground = false
            private set
    }
}
