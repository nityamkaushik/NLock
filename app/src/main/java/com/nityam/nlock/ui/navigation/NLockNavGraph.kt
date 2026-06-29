package com.nityam.nlock.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.nityam.nlock.ui.applist.AppListScreen
import com.nityam.nlock.ui.decoy.DecoyCalculatorScreen
import com.nityam.nlock.ui.settings.SettingsScreen
import com.nityam.nlock.ui.setup.SetupScreen
import com.nityam.nlock.ui.vault.VaultScreen

internal object Routes {
    const val SETUP = "setup"
    const val APP_LIST = "app_list"
    const val SETTINGS = "settings"
    const val VAULT = "vault"
    const val DECOY = "decoy"
}

@Composable
internal fun NLockNavGraph(
    isDisguised: Boolean,
    isSetupComplete: Boolean,
    navController: NavHostController = rememberNavController()
) {
    val startDestination = when {
        isDisguised -> Routes.DECOY
        !isSetupComplete -> Routes.SETUP
        else -> Routes.APP_LIST
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.DECOY) {
            DecoyCalculatorScreen(
                onUnlockVault = {
                    navController.navigate(Routes.APP_LIST) {
                        popUpTo(Routes.DECOY) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.SETUP) {
            SetupScreen(
                onSetupComplete = {
                    navController.navigate(Routes.APP_LIST) {
                        popUpTo(Routes.SETUP) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.APP_LIST) {
            AppListScreen(
                onNavigateToSettings = { navController.navigate(Routes.SETTINGS) },
                onNavigateToVault = { navController.navigate(Routes.VAULT) }
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Routes.VAULT) {
            VaultScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
