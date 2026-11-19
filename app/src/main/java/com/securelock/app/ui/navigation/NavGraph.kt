package com.securelock.app.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.securelock.app.ui.screens.apps.AppSelectionScreen
import com.securelock.app.ui.screens.apps.AppSelectionViewModel
import com.securelock.app.ui.screens.home.HomeScreen
import com.securelock.app.ui.screens.home.HomeViewModel
import com.securelock.app.ui.screens.permissions.PermissionsScreen
import com.securelock.app.ui.screens.settings.LockTimingScreen
import com.securelock.app.ui.screens.settings.OemGuidanceScreen
import com.securelock.app.ui.screens.settings.SecuritySettingsScreen
import com.securelock.app.ui.screens.settings.SettingsViewModel
import com.securelock.app.ui.screens.setup.AuthSetupScreen
import com.securelock.app.ui.screens.setup.AuthSetupViewModel
import com.securelock.app.ui.screens.setup.BiometricSetupScreen
import com.securelock.app.ui.screens.splash.SplashScreen

@Composable
fun SecureLockNavGraph(
    navController: NavHostController,
    homeViewModel: HomeViewModel = viewModel()
) {
    val securityStatus by homeViewModel.securityStatus.collectAsState()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        enterTransition = { fadeIn(animationSpec = tween(250)) + slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(250)) },
        exitTransition = { fadeOut(animationSpec = tween(250)) },
        popEnterTransition = { fadeIn(animationSpec = tween(250)) },
        popExitTransition = { fadeOut(animationSpec = tween(250)) + slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(250)) }
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                securityStatus = securityStatus,
                onNavigateNext = { isConfigured ->
                    val destination = if (isConfigured) Screen.Home.route else Screen.AuthSetup.route
                    navController.navigate(destination) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = homeViewModel,
                onNavigateToApps = { navController.navigate(Screen.AppSelection.route) },
                onNavigateToTiming = { navController.navigate(Screen.LockTiming.route) },
                onNavigateToSettings = { navController.navigate(Screen.SecuritySettings.route) },
                onNavigateToSetup = { navController.navigate(Screen.AuthSetup.route) },
                onNavigateToPermissions = { navController.navigate(Screen.Permissions.route) },
                onNavigateToOemGuide = { navController.navigate(Screen.OemGuidance.route) }
            )
        }

        composable(Screen.AuthSetup.route) {
            val setupVm: AuthSetupViewModel = viewModel()
            AuthSetupScreen(
                viewModel = setupVm,
                onNavigateBack = {
                    if (navController.previousBackStackEntry != null) {
                        navController.popBackStack()
                    } else {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.AuthSetup.route) { inclusive = true }
                        }
                    }
                },
                onAdvanceToBiometrics = {
                    navController.navigate(Screen.BiometricSetup.route)
                }
            )
        }

        composable(Screen.BiometricSetup.route) {
            BiometricSetupScreen(
                onFinished = {
                    navController.navigate(Screen.Permissions.route) {
                        popUpTo(Screen.AuthSetup.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Permissions.route) {
            PermissionsScreen(
                onNavigateBack = { navController.popBackStack() },
                onProtectionStarted = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Permissions.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.OemGuidance.route) {
            OemGuidanceScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.AppSelection.route) {
            val appsVm: AppSelectionViewModel = viewModel()
            AppSelectionScreen(
                viewModel = appsVm,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.LockTiming.route) {
            val settingsVm: SettingsViewModel = viewModel()
            LockTimingScreen(
                viewModel = settingsVm,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.SecuritySettings.route) {
            val settingsVm: SettingsViewModel = viewModel()
            SecuritySettingsScreen(
                viewModel = settingsVm,
                onNavigateBack = { navController.popBackStack() },
                onChangeMethodClick = { navController.navigate(Screen.AuthSetup.route) },
                onNavigateToPermissions = { navController.navigate(Screen.Permissions.route) },
                onNavigateToOemGuide = { navController.navigate(Screen.OemGuidance.route) }
            )
        }
    }
}
