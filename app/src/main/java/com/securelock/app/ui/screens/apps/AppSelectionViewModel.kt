package com.securelock.app.ui.screens.apps

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.securelock.app.SecureLockApp
import com.securelock.app.domain.model.AppItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppFilter {
    ALL,
    PROTECTED,
    UNPROTECTED
}

data class AppSelectionUiState(
    val apps: List<AppItem> = emptyList(),
    val searchQuery: String = "",
    val activeFilter: AppFilter = AppFilter.ALL,
    val isLoading: Boolean = true
)

class AppSelectionViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as SecureLockApp
    private val prefsManager = app.preferencesManager
    private val appsRepo = app.installedAppsRepository

    private val _searchQuery = MutableStateFlow("")
    private val _activeFilter = MutableStateFlow(AppFilter.ALL)
    private val _rawApps = MutableStateFlow<List<AppItem>>(emptyList())
    private val _isLoading = MutableStateFlow(true)

    val uiState: StateFlow<AppSelectionUiState> = combine(
        _rawApps,
        _searchQuery,
        _activeFilter,
        _isLoading
    ) { apps, query, filter, loading ->
        val filtered = apps.filter { item ->
            val matchesQuery = item.appName.contains(query, ignoreCase = true) ||
                    item.packageName.contains(query, ignoreCase = true)
            val matchesFilter = when (filter) {
                AppFilter.ALL -> true
                AppFilter.PROTECTED -> item.isProtected
                AppFilter.UNPROTECTED -> !item.isProtected
            }
            matchesQuery && matchesFilter
        }
        AppSelectionUiState(
            apps = filtered,
            searchQuery = query,
            activeFilter = filter,
            isLoading = loading
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AppSelectionUiState()
    )

    init {
        loadInstalledApps()
    }

    fun loadInstalledApps() {
        viewModelScope.launch {
            _isLoading.value = true
            prefsManager.protectedPackagesFlow.collect { protectedSet ->
                val list = appsRepo.getInstalledApplications(protectedSet)
                _rawApps.value = list
                _isLoading.value = false
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun setFilter(filter: AppFilter) {
        _activeFilter.value = filter
    }

    fun toggleAppProtection(appItem: AppItem) {
        viewModelScope.launch {
            prefsManager.setAppProtected(appItem.packageName, !appItem.isProtected)
        }
    }
}
