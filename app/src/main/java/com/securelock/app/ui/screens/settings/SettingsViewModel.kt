package com.securelock.app.ui.screens.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.securelock.app.SecureLockApp
import com.securelock.app.domain.model.LockTiming
import com.securelock.app.domain.model.SecurityStatus
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val prefsManager = (application as SecureLockApp).preferencesManager

    val securityStatus: StateFlow<SecurityStatus> = prefsManager.securityStatusFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SecurityStatus()
        )

    fun setLockTiming(timing: LockTiming) {
        viewModelScope.launch {
            prefsManager.setLockTiming(timing)
        }
    }

    fun setBiometricsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            prefsManager.setBiometricsEnabled(enabled)
        }
    }

    fun resetSecurityConfiguration(onComplete: () -> Unit) {
        viewModelScope.launch {
            prefsManager.resetAllSecurityData()
            onComplete()
        }
    }
}
