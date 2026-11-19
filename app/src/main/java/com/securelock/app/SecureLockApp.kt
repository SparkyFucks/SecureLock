package com.securelock.app

import android.app.Application
import com.securelock.app.data.datastore.PreferencesManager
import com.securelock.app.data.repository.InstalledAppsRepository
import com.securelock.app.data.security.KeystoreManager

class SecureLockApp : Application() {

    lateinit var preferencesManager: PreferencesManager
        private set

    lateinit var installedAppsRepository: InstalledAppsRepository
        private set

    lateinit var keystoreManager: KeystoreManager
        private set

    override fun onCreate() {
        super.onCreate()
        preferencesManager = PreferencesManager(applicationContext)
        installedAppsRepository = InstalledAppsRepository(applicationContext)
        keystoreManager = KeystoreManager()
    }
}
