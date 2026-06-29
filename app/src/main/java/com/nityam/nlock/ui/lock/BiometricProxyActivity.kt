package com.nityam.nlock.ui.lock

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.nityam.nlock.security.BiometricAuthManager
import com.nityam.nlock.service.AppLockAccessibilityService

/**
 * Transparent [FragmentActivity] to host the biometric prompt from the service overlay.
 *
 * When auth completes successfully, communicates back to [AppLockAccessibilityService]
 * to trigger the unlock flow through the ViewModel, then finishes itself.
 */
internal class BiometricProxyActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        BiometricAuthManager.authenticate(
            activity = this,
            onSuccess = {
                val service = AppLockAccessibilityService.instance
                if (service != null) {
                    // Let the ViewModel handle state transition → overlay auto-dismisses
                    service.overlayManager.onBiometricSuccess()
                }
                finish()
            },
            onError = { _ ->
                // Dismiss the transparent activity and go back to PIN entry
                finish()
            }
        )
    }

    companion object {
        const val EXTRA_TARGET_PACKAGE = "extra_target_package"
    }
}
