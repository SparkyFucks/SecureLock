package com.securelock.app.ui.navigation

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Home : Screen("home")
    data object AuthSetup : Screen("auth_setup")
    data object BiometricSetup : Screen("biometric_setup")
    data object AppSelection : Screen("app_selection")
    data object LockTiming : Screen("lock_timing")
    data object SecuritySettings : Screen("security_settings")
    data object Permissions : Screen("permissions")
    data object OemGuidance : Screen("oem_guidance")
}
