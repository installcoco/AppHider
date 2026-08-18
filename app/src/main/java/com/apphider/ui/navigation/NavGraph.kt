package com.apphider.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
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
        startDestination = startDestination,
        enterTransition = { fadeIn(animationSpec = tween(300)) },
        exitTransition = { fadeOut(animationSpec = tween(300)) }
    ) {
        composable(Routes.SETUP) {
            SetupScreen(
                onSetupComplete = {
                    navController.navigate(Routes.CALCULATOR) {
                        popUpTo(Routes.SETUP) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.CALCULATOR) {
            CalculatorScreen(
                onEnterHiddenSpace = {
                    navController.navigate(Routes.HIDDEN_SPACE) {
                        enterTransition = {
                            (fadeIn(tween(300)) + scaleIn(
                                initialScale = 0.9f,
                                animationSpec = tween(300)
                            ))
                        }
                        exitTransition = {
                            fadeOut(tween(300))
                        }
                    }
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