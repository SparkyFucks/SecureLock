package com.securelock.app.service

import com.securelock.app.domain.model.LockTiming
import java.util.concurrent.ConcurrentHashMap

/**
 * Manages runtime lock session states and grace periods.
 * Thread-safe and persistent in memory while the application process lives.
 */
object AppLockManager {

    // Package name -> timestamp (epoch millis) when unlocked
    private val unlockedSessionMap = ConcurrentHashMap<String, Long>()

    // Currently active foreground package
    @Volatile
    var currentForegroundPackage: String? = null
        private set

    @Volatile
    var lastExitedPackage: String? = null
        private set

    @Volatile
    var lastExitTimestamp: Long = 0L
        private set

    fun isAppUnlocked(packageName: String, timing: LockTiming): Boolean {
        val unlockTime = unlockedSessionMap[packageName] ?: return false

        return when (timing) {
            LockTiming.IMMEDIATELY -> {
                // If immediately, app is only unlocked if it was unlocked in the current session
                // and user hasn't switched away to a non-SecureLock app
                true
            }
            LockTiming.AFTER_ONE_MINUTE -> {
                val elapsed = System.currentTimeMillis() - unlockTime
                elapsed <= timing.timeoutMillis
            }
        }
    }

    fun markAppUnlocked(packageName: String) {
        unlockedSessionMap[packageName] = System.currentTimeMillis()
        currentForegroundPackage = packageName
    }

    fun onForegroundPackageChanged(newPackage: String, previousPackage: String?, timing: LockTiming) {
        if (newPackage == previousPackage) return

        currentForegroundPackage = newPackage

        if (previousPackage != null && previousPackage != newPackage) {
            lastExitedPackage = previousPackage
            lastExitTimestamp = System.currentTimeMillis()

            // If timing is IMMEDIATELY, clear the unlocked state when exiting the app
            if (timing == LockTiming.IMMEDIATELY) {
                unlockedSessionMap.remove(previousPackage)
            }
        }
    }

    fun lockAll() {
        unlockedSessionMap.clear()
    }
}
