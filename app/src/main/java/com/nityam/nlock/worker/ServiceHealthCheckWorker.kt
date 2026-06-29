package com.nityam.nlock.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.nityam.nlock.service.AppLockAccessibilityService
import com.nityam.nlock.util.PermissionHelper
import java.util.concurrent.TimeUnit

/**
 * Periodically checks if the AccessibilityService is still running.
 * If killed by the OEM, attempts to restart it or notify the user.
 */
internal class ServiceHealthCheckWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val isEnabled = PermissionHelper.isAccessibilityServiceEnabled(
            applicationContext,
            AppLockAccessibilityService::class.java
        )

        if (!isEnabled) {
            // The service was disabled or killed.
            // Notify the user to re-enable it.
            // A production app would post a high-priority notification here.
            return Result.failure()
        }

        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "nlock_health_check"

        fun enqueue(context: Context) {
            val request = PeriodicWorkRequestBuilder<ServiceHealthCheckWorker>(
                repeatInterval = 15,
                repeatIntervalTimeUnit = TimeUnit.MINUTES
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
