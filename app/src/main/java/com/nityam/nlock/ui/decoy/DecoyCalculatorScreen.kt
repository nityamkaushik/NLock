package com.nityam.nlock.ui.decoy

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * A dummy calculator UI that serves as the disguise.
 * In a real implementation, this would look exactly like a calculator
 * and unlock only when a specific PIN is entered and '=' is pressed.
 */
@Composable
internal fun DecoyCalculatorScreen(
    onUnlockVault: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Button(onClick = onUnlockVault) {
            Text("Unlock Hidden AppLock")
        }
    }
}
