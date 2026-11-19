package com.securelock.app.data.repository

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.provider.Settings
import com.securelock.app.domain.model.AppItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class InstalledAppsRepository(private val context: Context) {

    /**
     * Retrieves launchable applications plus critical security packages (Settings and Google Play Store).
     * Complies with Android 11+ package visibility via <queries> in AndroidManifest.xml.
     */
    suspend fun getInstalledApplications(protectedPackages: Set<String>): List<AppItem> = withContext(Dispatchers.IO) {
        val packageManager = context.packageManager
        val ownPackage = context.packageName

        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos = packageManager.queryIntentActivities(mainIntent, 0)
        val appList = mutableListOf<AppItem>()
        val seenPackages = mutableSetOf<String>()

        for (resolveInfo in resolveInfos) {
            val pkg = resolveInfo.activityInfo.packageName
            if (pkg == ownPackage || seenPackages.contains(pkg)) {
                continue
            }
            seenPackages.add(pkg)

            val label = try {
                resolveInfo.loadLabel(packageManager).toString()
            } catch (e: Exception) {
                pkg
            }

            val appInfo = resolveInfo.activityInfo.applicationInfo
            val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0

            appList.add(
                AppItem(
                    packageName = pkg,
                    appName = label,
                    isProtected = protectedPackages.contains(pkg),
                    isSystemApp = isSystem,
                    isPlaceholder = false
                )
            )
        }

        // Ensure System Settings is present even if omitted by launcher query
        val settingsPkg = "com.android.settings"
        if (!seenPackages.contains(settingsPkg)) {
            try {
                val appInfo = packageManager.getApplicationInfo(settingsPkg, 0)
                val label = packageManager.getApplicationLabel(appInfo).toString()
                appList.add(
                    AppItem(
                        packageName = settingsPkg,
                        appName = label,
                        isProtected = protectedPackages.contains(settingsPkg),
                        isSystemApp = true,
                        isPlaceholder = false
                    )
                )
                seenPackages.add(settingsPkg)
            } catch (e: Exception) {
                // Settings package not found or restricted
            }
        }

        // If running in an emulator or minimal environment with no queryable apps, provide transparent placeholders
        if (appList.isEmpty()) {
            return@withContext getPlaceholderApps(protectedPackages)
        }

        appList.sortedWith(
            compareByDescending<AppItem> { it.isProtected }
                .thenBy { it.appName.lowercase() }
        )
    }

    private fun getPlaceholderApps(protectedPackages: Set<String>): List<AppItem> {
        val samplePackages = listOf(
            Pair("com.android.settings", "System Settings (Anti-Uninstall)"),
            Pair("com.android.chrome", "Google Chrome"),
            Pair("com.google.android.gm", "Gmail"),
            Pair("com.whatsapp", "WhatsApp"),
            Pair("com.instagram.android", "Instagram"),
            Pair("com.google.android.apps.photos", "Google Photos"),
            Pair("com.android.vending", "Google Play Store")
        )

        return samplePackages.map { (pkg, name) ->
            AppItem(
                packageName = pkg,
                appName = name,
                isProtected = protectedPackages.contains(pkg),
                isSystemApp = pkg == "com.android.settings",
                isPlaceholder = true
            )
        }
    }
}
