package com.nityam.nlock.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.nityam.nlock.util.OemHelper

@Composable
internal fun OemGuideDialog(
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val isAggressive = OemHelper.isAggressiveOem()

    if (!isAggressive) {
        onDismiss()
        return
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Background Protection Needed") },
        text = { Text("Your device aggressively kills background apps. Please enable Auto-Start and remove Battery Restrictions for NLock to work reliably.") },
        confirmButton = {
            TextButton(onClick = {
                OemHelper.openAutoStartSettings(context)
                onDismiss()
            }) {
                Text("Open Settings")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Later")
            }
        }
    )
}
