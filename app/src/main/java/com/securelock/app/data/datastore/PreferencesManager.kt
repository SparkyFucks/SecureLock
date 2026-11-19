package com.securelock.app.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.securelock.app.data.security.HashUtils
import com.securelock.app.domain.model.AuthMethod
import com.securelock.app.domain.model.LockTiming
import com.securelock.app.domain.model.SecurityStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "secure_lock_prefs")

class PreferencesManager(private val context: Context) {

    companion object {
        val KEY_SETUP_COMPLETE = booleanPreferencesKey("is_setup_complete")
        val KEY_AUTH_METHOD = stringPreferencesKey("auth_method")
        val KEY_HASHED_CREDENTIAL = stringPreferencesKey("credential_hash")
        val KEY_CREDENTIAL_SALT = stringPreferencesKey("credential_salt")
        val KEY_BIOMETRICS_ENABLED = booleanPreferencesKey("biometrics_enabled")
        val KEY_LOCK_TIMING = stringPreferencesKey("lock_timing")
        val KEY_PROTECTED_PACKAGES = stringSetPreferencesKey("protected_packages")
    }

    val securityStatusFlow: Flow<SecurityStatus> = context.dataStore.data.map { prefs ->
        val isSetupComplete = prefs[KEY_SETUP_COMPLETE] ?: false
        val authMethodStr = prefs[KEY_AUTH_METHOD]
        val authMethod = AuthMethod.fromId(authMethodStr)
        val biometrics = prefs[KEY_BIOMETRICS_ENABLED] ?: false
        val protectedCount = prefs[KEY_PROTECTED_PACKAGES]?.size ?: 0
        val timing = LockTiming.fromId(prefs[KEY_LOCK_TIMING])

        SecurityStatus(
            isSetupComplete = isSetupComplete,
            authMethod = authMethod,
            isBiometricsEnabled = biometrics,
            protectedAppsCount = protectedCount,
            lockTiming = timing
        )
    }

    val protectedPackagesFlow: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        prefs[KEY_PROTECTED_PACKAGES] ?: emptySet()
    }

    suspend fun saveCredential(authMethod: AuthMethod, rawCredential: String) {
        val salt = HashUtils.generateSalt()
        val hash = HashUtils.hashCredential(rawCredential, salt)

        context.dataStore.edit { prefs ->
            prefs[KEY_AUTH_METHOD] = authMethod.id
            prefs[KEY_CREDENTIAL_SALT] = HashUtils.toBase64(salt)
            prefs[KEY_HASHED_CREDENTIAL] = HashUtils.toBase64(hash)
            prefs[KEY_SETUP_COMPLETE] = true
        }
    }

    suspend fun verifyCredential(candidate: String): Boolean {
        var isValid = false
        context.dataStore.edit { prefs ->
            val saltStr = prefs[KEY_CREDENTIAL_SALT]
            val hashStr = prefs[KEY_HASHED_CREDENTIAL]
            if (saltStr != null && hashStr != null) {
                val salt = HashUtils.fromBase64(saltStr)
                val expectedHash = HashUtils.fromBase64(hashStr)
                isValid = HashUtils.verifyCredential(candidate, salt, expectedHash)
            }
        }
        return isValid
    }

    suspend fun setBiometricsEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_BIOMETRICS_ENABLED] = enabled
        }
    }

    suspend fun setLockTiming(timing: LockTiming) {
        context.dataStore.edit { prefs ->
            prefs[KEY_LOCK_TIMING] = timing.id
        }
    }

    suspend fun setAppProtected(packageName: String, isProtected: Boolean) {
        context.dataStore.edit { prefs ->
            val current = (prefs[KEY_PROTECTED_PACKAGES] ?: emptySet()).toMutableSet()
            if (isProtected) {
                current.add(packageName)
            } else {
                current.remove(packageName)
            }
            prefs[KEY_PROTECTED_PACKAGES] = current
        }
    }

    suspend fun resetAllSecurityData() {
        context.dataStore.edit { it.clear() }
    }
}
