package com.nityam.nlock.util

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager

/**
 * Handles package-level operations, including the app disguise toggle.
 */
internal object PackageUtils {

    /**
     * Toggles between the standard MainActivity and the DisguisedLauncherAlias.
     */
    fun toggleAppDisguise(context: Context, enableDisguise: Boolean) {
        val pm = context.packageManager
        
        val mainActivity = ComponentName(context, "com.nityam.nlock.MainActivity")
        val aliasActivity = ComponentName(context, "com.nityam.nlock.DisguisedLauncherAlias")

        if (enableDisguise) {
            pm.setComponentEnabledSetting(
                aliasActivity,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )
            pm.setComponentEnabledSetting(
                mainActivity,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
            )
        } else {
            pm.setComponentEnabledSetting(
                mainActivity,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )
            pm.setComponentEnabledSetting(
                aliasActivity,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
            )
        }
    }
}
