package com.securelock.app.ui.screens.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.securelock.app.SecureLockApp
import com.securelock.app.domain.model.SecurityStatus
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val prefsManager = (application as SecureLockApp).preferencesManager

    val securityStatus: StateFlow<SecurityStatus> = prefsManager.securityStatusFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SecurityStatus()
        )
}
