package com.securelock.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.securelock.app.MainActivity
import com.securelock.app.R
import com.securelock.app.SecureLockApp
import com.securelock.app.data.permissions.PermissionManager
import com.securelock.app.domain.model.LockTiming
import com.securelock.app.ui.screens.lock.LockScreenActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class AppLockForegroundService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())
    private var isMonitoring = false
    private var launcherPackages: Set<String> = emptySet()

    companion object {
        const val NOTIFICATION_ID = 1001
        const val CHANNEL_ID = "secure_lock_protection_channel"
        const val CHANNEL_NAME = "SecureLock Active Protection"

        @Volatile
        var isServiceRunning = false
            private set

        fun start(context: Context) {
            val intent = Intent(context, AppLockForegroundService::class.java)
            ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, AppLockForegroundService::class.java)
            context.stopService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        isServiceRunning = true
        createNotificationChannel()
        launcherPackages = getLauncherPackageNames()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = buildForegroundNotification()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ServiceCompat.startForeground(
                this,
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        if (!isMonitoring) {
            startMonitoringLoop()
        }

        return START_STICKY
    }

    private fun startMonitoringLoop() {
        isMonitoring = true
        val app = application as SecureLockApp
        val prefs = app.preferencesManager
        val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager

        serviceScope.launch {
            var previousForegroundPackage: String? = null

            while (isActive) {
                if (PermissionManager.hasUsageStatsPermission(this@AppLockForegroundService) &&
                    PermissionManager.hasOverlayPermission(this@AppLockForegroundService)
                ) {
                    val currentForeground = getForegroundPackageName(usageStatsManager)

                    if (currentForeground != null &&
                        currentForeground != packageName &&
                        !launcherPackages.contains(currentForeground) &&
                        currentForeground != "com.android.systemui"
                    ) {
                        val protectedSet = prefs.protectedPackagesFlow.first()
                        val securityStatus = prefs.securityStatusFlow.first()

                        // Detect package switch
                        if (currentForeground != previousForegroundPackage) {
                            AppLockManager.onForegroundPackageChanged(
                                newPackage = currentForeground,
                                previousPackage = previousForegroundPackage,
                                timing = securityStatus.lockTiming
                            )
                        }

                        // Check if current app is protected and needs locking
                        if (protectedSet.contains(currentForeground)) {
                            val isUnlocked = AppLockManager.isAppUnlocked(
                                packageName = currentForeground,
                                timing = securityStatus.lockTiming
                            )

                            if (!isUnlocked) {
                                launchLockOverlay(currentForeground)
                            }
                        }

                        previousForegroundPackage = currentForeground
                    } else if (currentForeground != null && launcherPackages.contains(currentForeground)) {
                        // User went to home launcher
                        val securityStatus = prefs.securityStatusFlow.first()
                        AppLockManager.onForegroundPackageChanged(
                            newPackage = currentForeground,
                            previousPackage = previousForegroundPackage,
                            timing = securityStatus.lockTiming
                        )
                        previousForegroundPackage = currentForeground
                    }
                }

                // Battery-friendly polling interval: 350ms
                delay(350)
            }
        }
    }

    private fun getForegroundPackageName(usageStatsManager: UsageStatsManager?): String? {
        if (usageStatsManager == null) return null

        val endTime = System.currentTimeMillis()
        val beginTime = endTime - 2500
        val events = usageStatsManager.queryEvents(beginTime, endTime)
        val event = UsageEvents.Event()

        var latestResumedPackage: String? = null
        var latestTimestamp = 0L

        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED && event.timeStamp > latestTimestamp) {
                latestResumedPackage = event.packageName
                latestTimestamp = event.timeStamp
            }
        }

        return latestResumedPackage
    }

    private fun launchLockOverlay(targetPackage: String) {
        val intent = Intent(this, LockScreenActivity::class.java).apply {
            putExtra(LockScreenActivity.EXTRA_PACKAGE_NAME, targetPackage)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        startActivity(intent)
    }

    private fun getLauncherPackageNames(): Set<String> {
        val launchers = mutableSetOf<String>()
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        val resolveInfos = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
        for (info in resolveInfos) {
            launchers.add(info.activityInfo.packageName)
        }
        return launchers
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows persistent protection status for SecureLock"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildForegroundNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("SecureLock Active")
            .setContentText("Protected applications are secured.")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        isServiceRunning = false
        isMonitoring = false
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
