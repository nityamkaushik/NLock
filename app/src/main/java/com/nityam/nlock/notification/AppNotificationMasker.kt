package com.nityam.nlock.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat
import com.nityam.nlock.NLockApplication
import com.nityam.nlock.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Intercepts notifications to prevent reading messages from locked apps
 * via the notification shade.
 */
internal class AppNotificationMasker : NotificationListenerService() {

    private lateinit var serviceScope: CoroutineScope
    private val lockedPackages = HashSet<String>()

    override fun onCreate() {
        super.onCreate()
        serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        observeLockedPackages()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return
        
        // Ignore our own notifications
        if (sbn.packageName == packageName) return

        if (lockedPackages.contains(sbn.packageName)) {
            serviceScope.launch {
                val app = (application as NLockApplication).repository.getApp(sbn.packageName)
                if (app?.hideNotificationContent == true) {
                    cancelNotification(sbn.key)
                    // Optionally post a masked notification here
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    private fun observeLockedPackages() {
        val app = application as NLockApplication
        serviceScope.launch {
            app.repository.observeLockedPackageNames().collect { packages ->
                lockedPackages.clear()
                lockedPackages.addAll(packages)
            }
        }
    }
}
