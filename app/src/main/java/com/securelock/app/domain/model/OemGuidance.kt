package com.securelock.app.domain.model

import android.os.Build

data class OemGuidance(
    val manufacturerName: String,
    val isAggressiveKiller: Boolean,
    val recommendations: List<String>
) {
    companion object {
        fun getGuidance(): OemGuidance {
            val manufacturer = Build.MANUFACTURER.lowercase()
            return when {
                manufacturer.contains("xiaomi") || manufacturer.contains("redmi") || manufacturer.contains("poco") -> {
                    OemGuidance(
                        manufacturerName = "Xiaomi / HyperOS / MIUI",
                        isAggressiveKiller = true,
                        recommendations = listOf(
                            "Open Security App -> Manage Apps -> SecureLock -> Enable 'Autostart'",
                            "Set Battery Saver to 'No restrictions'",
                            "Lock SecureLock in Recent Apps (Recents screen -> tap and hold SecureLock -> tap Padlock)"
                        )
                    )
                }
                manufacturer.contains("samsung") -> {
                    OemGuidance(
                        manufacturerName = "Samsung One UI",
                        isAggressiveKiller = true,
                        recommendations = listOf(
                            "Open Settings -> Battery -> Background usage limits -> Exclude SecureLock from 'Sleeping apps' and 'Deep sleeping apps'",
                            "Open App Info -> Battery -> Select 'Unrestricted'"
                        )
                    )
                }
                manufacturer.contains("huawei") || manufacturer.contains("honor") -> {
                    OemGuidance(
                        manufacturerName = "Huawei / Honor (EMUI)",
                        isAggressiveKiller = true,
                        recommendations = listOf(
                            "Open Settings -> Battery -> App Launch -> SecureLock -> Turn off 'Manage automatically' and enable 'Auto-launch', 'Secondary launch', and 'Run in background'"
                        )
                    )
                }
                manufacturer.contains("oppo") || manufacturer.contains("realme") || manufacturer.contains("oneplus") -> {
                    OemGuidance(
                        manufacturerName = "Oppo / Realme / OnePlus (ColorOS / OxygenOS)",
                        isAggressiveKiller = true,
                        recommendations = listOf(
                            "Open App Info -> Battery -> Enable 'Allow background activity' and 'Allow auto-launch'",
                            "Lock the app in the Recent Apps carousel"
                        )
                    )
                }
                manufacturer.contains("vivo") || manufacturer.contains("iqoo") -> {
                    OemGuidance(
                        manufacturerName = "Vivo / iQOO (Funtouch OS)",
                        isAggressiveKiller = true,
                        recommendations = listOf(
                            "Open Settings -> Battery -> High background power consumption -> Allow SecureLock",
                            "Enable Autostart in iManager"
                        )
                    )
                }
                else -> {
                    OemGuidance(
                        manufacturerName = Build.MANUFACTURER.replaceFirstChar { it.uppercase() },
                        isAggressiveKiller = false,
                        recommendations = listOf(
                            "Open Settings -> Apps -> SecureLock -> Battery -> Set to 'Unrestricted'",
                            "Ensure 'Pause app activity if unused' is disabled"
                        )
                    )
                }
            }
        }
    }
}
