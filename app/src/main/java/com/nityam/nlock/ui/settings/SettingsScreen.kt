package com.nityam.nlock.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
