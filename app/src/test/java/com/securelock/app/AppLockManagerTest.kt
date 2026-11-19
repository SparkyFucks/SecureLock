package com.securelock.app

import com.securelock.app.domain.model.LockTiming
import com.securelock.app.service.AppLockManager
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AppLockManagerTest {

    @Before
    fun setup() {
        AppLockManager.lockAll()
    }

    @Test
    fun appIsLockedInitially() {
        val unlocked = AppLockManager.isAppUnlocked("com.example.test", LockTiming.IMMEDIATELY)
        assertFalse(unlocked)
    }

    @Test
    fun markAppUnlocked_makesAppUnlocked() {
        val pkg = "com.example.test"
        AppLockManager.markAppUnlocked(pkg)

        assertTrue(AppLockManager.isAppUnlocked(pkg, LockTiming.IMMEDIATELY))
        assertTrue(AppLockManager.isAppUnlocked(pkg, LockTiming.AFTER_ONE_MINUTE))
    }

    @Test
    fun immediateTiming_locksWhenAppExits() {
        val pkg = "com.example.test"
        AppLockManager.markAppUnlocked(pkg)
        assertTrue(AppLockManager.isAppUnlocked(pkg, LockTiming.IMMEDIATELY))

        // User switches to launcher
        AppLockManager.onForegroundPackageChanged(
            newPackage = "com.android.launcher",
            previousPackage = pkg,
            timing = LockTiming.IMMEDIATELY
        )

        assertFalse(AppLockManager.isAppUnlocked(pkg, LockTiming.IMMEDIATELY))
    }

    @Test
    fun lockAll_clearsAllUnlockedSessions() {
        val pkg1 = "com.example.app1"
        val pkg2 = "com.example.app2"
        AppLockManager.markAppUnlocked(pkg1)
        AppLockManager.markAppUnlocked(pkg2)

        assertTrue(AppLockManager.isAppUnlocked(pkg1, LockTiming.AFTER_ONE_MINUTE))
        assertTrue(AppLockManager.isAppUnlocked(pkg2, LockTiming.AFTER_ONE_MINUTE))

        AppLockManager.lockAll()

        assertFalse(AppLockManager.isAppUnlocked(pkg1, LockTiming.AFTER_ONE_MINUTE))
        assertFalse(AppLockManager.isAppUnlocked(pkg2, LockTiming.AFTER_ONE_MINUTE))
    }
}
