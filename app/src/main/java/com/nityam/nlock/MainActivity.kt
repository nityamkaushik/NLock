package com.nityam.nlock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.nityam.nlock.ui.components.OemGuideDialog
import com.nityam.nlock.ui.navigation.NLockNavGraph
import com.nityam.nlock.ui.theme.NLockTheme
import com.nityam.nlock.util.PermissionHelper

internal class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as NLockApplication
        val repository = app.repository

        setContent {
            NLockTheme {
                val isSetupComplete by repository.setupComplete.collectAsState(initial = false)
                val appDisguiseEnabled by repository.appDisguiseEnabled.collectAsState(initial = false)

                NLockNavGraph(
                    isDisguised = appDisguiseEnabled,
                    isSetupComplete = isSetupComplete
                )

                // Show OEM guide only once per app session, dismissible
                var showOemDialog by rememberSaveable { mutableStateOf(true) }
                if (showOemDialog) {
                    OemGuideDialog(onDismiss = { showOemDialog = false })
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!PermissionHelper.isAccessibilityServiceEnabled(
                this,
                com.nityam.nlock.service.AppLockAccessibilityService::class.java
            )
        ) {
            PermissionHelper.openAccessibilitySettings(this)
        }
    }
}