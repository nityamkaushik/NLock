package com.nityam.nlock.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nityam.nlock.NLockApplication
import com.nityam.nlock.util.PackageUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val viewModel = viewModel<SettingsViewModel>(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val app = context.applicationContext as NLockApplication
                return SettingsViewModel(app.repository) as T
            }
        }
    )

    val biometricEnabled by viewModel.biometricEnabled.collectAsState()
    val appDisguiseEnabled by viewModel.appDisguiseEnabled.collectAsState()
    val showSystemApps by viewModel.showSystemApps.collectAsState()
    val gracePeriodSeconds by viewModel.gracePeriodSeconds.collectAsState()

    var showGracePeriodDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            SettingRow(
                title = "Use Biometrics",
                checked = biometricEnabled,
                onCheckedChange = { viewModel.setBiometricEnabled(it) }
            )
            SettingRow(
                title = "Disguise App as Calculator",
                checked = appDisguiseEnabled,
                onCheckedChange = {
                    viewModel.toggleAppDisguise(it)
                    PackageUtils.toggleAppDisguise(context, it)
                }
            )
            SettingRow(
                title = "Show System Apps",
                checked = showSystemApps,
                onCheckedChange = { viewModel.setShowSystemApps(it) }
            )
            ClickableSettingRow(
                title = "Auto-lock Interval",
                subtitle = formatGracePeriod(gracePeriodSeconds),
                onClick = { showGracePeriodDialog = true }
            )
        }

        if (showGracePeriodDialog) {
            GracePeriodDialog(
                currentSeconds = gracePeriodSeconds,
                onDismiss = { showGracePeriodDialog = false },
                onSelect = { 
                    viewModel.setGracePeriodSeconds(it)
                    showGracePeriodDialog = false
                }
            )
        }
    }
}

@Composable
private fun SettingRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, modifier = Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun ClickableSettingRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(text = title)
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun GracePeriodDialog(
    currentSeconds: Int,
    onDismiss: () -> Unit,
    onSelect: (Int) -> Unit
) {
    val options = listOf(
        0 to "Immediately",
        15 to "15 seconds",
        30 to "30 seconds",
        60 to "1 minute",
        120 to "2 minutes",
        300 to "5 minutes"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Auto-lock Interval") },
        text = {
            Column {
                options.forEach { (seconds, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(seconds) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentSeconds == seconds,
                            onClick = { onSelect(seconds) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = label)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun formatGracePeriod(seconds: Int): String {
    return when (seconds) {
        0 -> "Immediately"
        15 -> "15 seconds"
        30 -> "30 seconds"
        60 -> "1 minute"
        120 -> "2 minutes"
        300 -> "5 minutes"
        else -> "$seconds seconds"
    }
}
