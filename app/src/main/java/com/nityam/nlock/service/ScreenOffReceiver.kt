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
