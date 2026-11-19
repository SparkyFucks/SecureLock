package com.securelock.app.domain.model

data class AppItem(
    val packageName: String,
    val appName: String,
    val isProtected: Boolean = false,
    val isSystemApp: Boolean = false,
    val isPlaceholder: Boolean = false
)
