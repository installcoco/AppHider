package com.apphider.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.apphider.ui.admin.AdminActivationScreen
import com.apphider.ui.applist.AppListScreen
import com.apphider.ui.calculator.CalculatorScreen
import com.apphider.ui.hidden.HiddenSpaceScreen
import com.apphider.ui.settings.SettingsScreen
import com.apphider.ui.setup.SetupScreen

/**
 * Navigation route constants.
 */
object Routes {
    const val SETUP = "setup"
    const val ADMIN_ACTIVATION = "admin_activation"
    const val CALCULATOR = "calculator"
    const val HIDDEN_SPACE = "hidden_space"
    const val APP_LIST = "app_list"
    const val SETTINGS = "settings"
}

/**
 * Main navigation graph for the application.
 * Uses single Activity with Compose Navigation.
 */
@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Routes.SETUP) {
            SetupScreen(
                onSetupComplete = {
                    navController.navigate(Routes.ADMIN_ACTIVATION) {
                        popUpTo(Routes.SETUP) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.ADMIN_ACTIVATION) {
            AdminActivationScreen(
                onActivated = {
                    navController.navigate(Routes.CALCULATOR) {
                        popUpTo(Routes.ADMIN_ACTIVATION) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.CALCULATOR) {
            CalculatorScreen(
                onEnterHiddenSpace = {
                    navController.navigate(Routes.HIDDEN_SPACE)
                },
                onNavigateToSettings = {
                    navController.navigate(Routes.SETTINGS)
                }
            )
        }

        composable(Routes.HIDDEN_SPACE) {
            HiddenSpaceScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToAppList = {
                    navController.navigate(Routes.APP_LIST)
                },
                onNavigateToSettings = {
                    navController.navigate(Routes.SETTINGS)
                }
            )
        }

        composable(Routes.APP_LIST) {
            AppListScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}