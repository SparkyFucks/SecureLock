package com.securelock.app.ui.screens.permissions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.BatterySaver
import androidx.compose.material.icons.outlined.DataUsage
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.securelock.app.data.permissions.PermissionManager
import com.securelock.app.service.AppLockForegroundService
import com.securelock.app.ui.theme.ShieldGlow
import com.securelock.app.ui.theme.StatusWarning

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionsScreen(
    onNavigateBack: () -> Unit,
    onProtectionStarted: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasUsageStats by remember { mutableStateOf(PermissionManager.hasUsageStatsPermission(context)) }
    var hasOverlay by remember { mutableStateOf(PermissionManager.hasOverlayPermission(context)) }
    var hasNotification by remember { mutableStateOf(PermissionManager.hasNotificationPermission(context)) }
    var isIgnoringBattery by remember { mutableStateOf(PermissionManager.isIgnoringBatteryOptimizations(context)) }

    // Re-check permissions when returning from system settings
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasUsageStats = PermissionManager.hasUsageStatsPermission(context)
                hasOverlay = PermissionManager.hasOverlayPermission(context)
                hasNotification = PermissionManager.hasNotificationPermission(context)
                isIgnoringBattery = PermissionManager.isIgnoringBatteryOptimizations(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val allRequiredGranted = hasUsageStats && hasOverlay

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Required Permissions", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "SecureLock needs explicit Android system permissions to reliably detect protected apps and present the lock screen.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // 1. Usage Stats Permission
            PermissionCard(
                title = "Usage Access",
                description = "Required to detect when a protected application is opened in the foreground.",
                icon = Icons.Outlined.DataUsage,
                isGranted = hasUsageStats,
                isRequired = true,
                onGrantClick = { PermissionManager.openUsageStatsSettings(context) }
            )

            // 2. Overlay Permission
            PermissionCard(
                title = "Display Over Other Apps",
                description = "Required to display the PIN or Pattern lock screen over protected apps.",
                icon = Icons.Outlined.Layers,
                isGranted = hasOverlay,
                isRequired = true,
                onGrantClick = { PermissionManager.openOverlaySettings(context) }
            )

            // 3. Notification Permission
            PermissionCard(
                title = "Foreground Notifications",
                description = "Keeps the protection service running continuously without being killed by Android.",
                icon = Icons.Outlined.Notifications,
                isGranted = hasNotification,
                isRequired = false,
                onGrantClick = { PermissionManager.openAppSettings(context) }
            )

            // 4. Battery Optimization
            PermissionCard(
                title = "Battery Optimization Exemption",
                description = "Prevents Android battery saver from freezing the background protection service.",
                icon = Icons.Outlined.BatterySaver,
                isGranted = isIgnoringBattery,
                isRequired = false,
                onGrantClick = { PermissionManager.openBatteryOptimizationSettings(context) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Action Button
            if (allRequiredGranted) {
                Button(
                    onClick = {
                        AppLockForegroundService.start(context)
                        onProtectionStarted()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Start Live Protection Service")
                }
            } else {
                Text(
                    text = "Please grant all required permissions above to activate real-time app locking.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun PermissionCard(
    title: String,
    description: String,
    icon: ImageVector,
    isGranted: Boolean,
    isRequired: Boolean,
    onGrantClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            if (isGranted) ShieldGlow.copy(alpha = 0.15f)
                            else MaterialTheme.colorScheme.primaryContainer
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isGranted) ShieldGlow else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (isRequired) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "(Required)",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Text(
                        text = if (isGranted) "Permission Granted" else "Action Required",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = if (isGranted) ShieldGlow else StatusWarning,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }

                Icon(
                    imageVector = if (isGranted) Icons.Filled.CheckCircle else Icons.Filled.Warning,
                    contentDescription = null,
                    tint = if (isGranted) ShieldGlow else StatusWarning,
                    modifier = Modifier.size(22.dp)
                )
            }

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (!isGranted) {
                FilledTonalButton(
                    onClick = onGrantClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Grant Permission")
                }
            }
        }
    }
}
