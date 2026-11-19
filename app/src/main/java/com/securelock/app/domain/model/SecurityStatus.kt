package com.securelock.app.domain.model

data class SecurityStatus(
    val isSetupComplete: Boolean = false,
    val authMethod: AuthMethod? = null,
    val isBiometricsEnabled: Boolean = false,
    val protectedAppsCount: Int = 0,
    val lockTiming: LockTiming = LockTiming.IMMEDIATELY,
    val hasSecurityQuestion: Boolean = false,
    val isUninstallProtected: Boolean = false
)
