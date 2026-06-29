package com.nityam.nlock.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import java.util.Locale

/**
 * Helps navigate OEM-specific background restriction menus (Xiaomi, Oppo, Vivo, etc.).
 */
internal object OemHelper {

    /** Returns true if the device is a known aggressive OEM. */
    fun isAggressiveOem(): Boolean {
        val manufacturer = Build.MANUFACTURER.lowercase(Locale.getDefault())
        return manufacturer in listOf("xiaomi", "redmi", "poco", "oppo", "vivo", "letv", "huawei", "honor")
    }

    /** Opens the auto-start or battery optimization settings for the OEM. */
    fun openAutoStartSettings(context: Context) {
        val intent = Intent()
        val manufacturer = Build.MANUFACTURER.lowercase(Locale.getDefault())

        when {
            manufacturer.contains("xiaomi") || manufacturer.contains("redmi") || manufacturer.contains("poco") -> {
                intent.setClassName(
                    "com.miui.securitycenter",
                    "com.miui.permcenter.autostart.AutoStartManagementActivity"
                )
            }
            manufacturer.contains("oppo") -> {
                intent.setClassName(
                    "com.coloros.safecenter",
                    "com.coloros.safecenter.permission.startup.StartupAppListActivity"
                )
            }
            manufacturer.contains("vivo") -> {
                intent.setClassName(
                    "com.vivo.permissionmanager",
                    "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"
                )
            }
            manufacturer.contains("huawei") || manufacturer.contains("honor") -> {
                intent.setClassName(
                    "com.huawei.systemmanager",
                    "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"
                )
            }
            else -> {
                // Fallback to standard application details settings
                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                intent.data = Uri.fromParts("package", context.packageName, null)
            }
        }

        try {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback
            val fallback = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            fallback.data = Uri.fromParts("package", context.packageName, null)
            fallback.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            try {
                context.startActivity(fallback)
            } catch (ignored: Exception) {}
        }
    }
}
