package com.securelock.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.securelock.app.SecureLockApp
import com.securelock.app.data.permissions.PermissionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            val app = context.applicationContext as SecureLockApp
            val prefs = app.preferencesManager

            CoroutineScope(Dispatchers.Default).launch {
                val status = prefs.securityStatusFlow.first()
                if (status.isSetupComplete && PermissionManager.hasAllRequiredPermissions(context)) {
                    AppLockForegroundService.start(context)
                }
            }
        }
    }
}
