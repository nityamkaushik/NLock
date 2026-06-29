package com.nityam.nlock.ui.applist

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AppListScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToVault: () -> Unit
) {
    val context = LocalContext.current
    val viewModel = viewModel<AppListViewModel>(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val app = context.applicationContext as NLockApplication
                return AppListViewModel(app.repository, context.packageManager) as T
            }
        }
    )

    val apps by viewModel.apps.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("App Lock") },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            items(apps) { app ->
                AppListItemRow(
                    item = app,
                    onToggle = { isLocked -> viewModel.toggleAppLock(app.packageName, isLocked) }
                )
            }
        }
    }
}

@Composable
private fun AppListItemRow(
    item: AppListItem,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = item.label, modifier = Modifier.weight(1f))
        Switch(
            checked = item.isLocked,
            onCheckedChange = onToggle
        )
    }
}
