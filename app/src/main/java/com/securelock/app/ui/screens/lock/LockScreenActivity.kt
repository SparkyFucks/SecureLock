package com.securelock.app.ui.screens.lock

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.securelock.app.SecureLockApp
import com.securelock.app.domain.model.AuthMethod
import com.securelock.app.domain.model.SecurityStatus
import com.securelock.app.service.AppLockManager
import com.securelock.app.ui.screens.setup.components.PasswordInputView
import com.securelock.app.ui.screens.setup.components.PatternLockView
import com.securelock.app.ui.screens.setup.components.PinInputView
import com.securelock.app.ui.theme.SecureLockTheme
import com.securelock.app.ui.theme.StatusError
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class LockScreenActivity : FragmentActivity() {

    companion object {
        const val EXTRA_PACKAGE_NAME = "extra_package_name"
    }

    private var targetPackage: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Prevent screenshots, recents exposure, and screen capture
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        targetPackage = intent.getStringExtra(EXTRA_PACKAGE_NAME) ?: ""

        val appName = try {
            val appInfo = packageManager.getApplicationInfo(targetPackage, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            "Protected Application"
        }

        enableEdgeToEdge()

        onBackPressedDispatcher.addCallback(this) {
            exitToHomeScreen()
        }

        setContent {
            SecureLockTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LockScreenUI(
                        targetAppName = appName,
                        targetPackage = targetPackage,
                        onUnlockSuccess = {
                            AppLockManager.markAppUnlocked(targetPackage)
                            finish()
                            @Suppress("DEPRECATION") overridePendingTransition(0, 0)
                        },
                        onExitToHome = {
                            exitToHomeScreen()
                        },
                        onTriggerBiometric = { promptBiometrics {
                            AppLockManager.markAppUnlocked(targetPackage)
                            finish()
                            @Suppress("DEPRECATION") overridePendingTransition(0, 0)
                        } }
                    )
                }
            }
        }
    }

    private fun exitToHomeScreen() {
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(homeIntent)
        finish()
        @Suppress("DEPRECATION") overridePendingTransition(0, 0)
    }



    private fun promptBiometrics(onSuccess: () -> Unit) {
        val executor = ContextCompat.getMainExecutor(this)
        val prompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess()
            }
        })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock Application")
            .setSubtitle("Confirm your identity to open this app")
            .setNegativeButtonText("Use Master Credential")
            .build()

        prompt.authenticate(promptInfo)
    }
}

@Composable
fun LockScreenUI(
    targetAppName: String,
    targetPackage: String,
    onUnlockSuccess: () -> Unit,
    onExitToHome: () -> Unit,
    onTriggerBiometric: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val prefsManager = (context.applicationContext as SecureLockApp).preferencesManager

    var securityStatus by remember { mutableStateOf(SecurityStatus()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var enteredPin by remember { mutableStateOf("") }
    var enteredPassword by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val status = prefsManager.securityStatusFlow.first()
        securityStatus = status

        // Auto trigger biometrics if enabled
        if (status.isBiometricsEnabled) {
            val biometricManager = BiometricManager.from(context)
            if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK) == BiometricManager.BIOMETRIC_SUCCESS) {
                onTriggerBiometric()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Header
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = targetAppName,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "Protected by SecureLock",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            AnimatedVisibility(
                visible = errorMessage != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                errorMessage?.let { msg ->
                    Text(
                        text = msg,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }

        // Credential Input View
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            when (securityStatus.authMethod) {
                AuthMethod.PIN -> {
                    PinInputView(
                        pinLength = 4,
                        currentLength = enteredPin.length,
                        onDigitClick = { digit ->
                            if (enteredPin.length < 8) {
                                enteredPin += digit
                                if (enteredPin.length >= 4) {
                                    coroutineScope.launch {
                                        val valid = prefsManager.verifyCredential(enteredPin)
                                        if (valid) {
                                            onUnlockSuccess()
                                        } else if (enteredPin.length == 4) {
                                            errorMessage = "Incorrect PIN. Try again."
                                            enteredPin = ""
                                        }
                                    }
                                }
                            }
                        },
                        onDeleteClick = {
                            if (enteredPin.isNotEmpty()) {
                                enteredPin = enteredPin.dropLast(1)
                                errorMessage = null
                            }
                        }
                    )
                }
                AuthMethod.PASSWORD -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        PasswordInputView(
                            value = enteredPassword,
                            onValueChange = {
                                enteredPassword = it
                                errorMessage = null
                            },
                            label = "Master Password",
                            onDone = {
                                coroutineScope.launch {
                                    val valid = prefsManager.verifyCredential(enteredPassword)
                                    if (valid) {
                                        onUnlockSuccess()
                                    } else {
                                        errorMessage = "Incorrect password"
                                    }
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    val valid = prefsManager.verifyCredential(enteredPassword)
                                    if (valid) {
                                        onUnlockSuccess()
                                    } else {
                                        errorMessage = "Incorrect password"
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Unlock")
                        }
                    }
                }
                AuthMethod.PATTERN -> {
                    PatternLockView(
                        onPatternComplete = { pattern ->
                            coroutineScope.launch {
                                val valid = prefsManager.verifyCredential(pattern)
                                if (valid) {
                                    onUnlockSuccess()
                                } else {
                                    errorMessage = "Incorrect pattern"
                                }
                            }
                        }
                    )
                }
                null -> {
                    // Fallback
                    Button(onClick = onUnlockSuccess) {
                        Text("Unlock")
                    }
                }
            }
        }

        // Bottom Controls (Biometrics + Cancel)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(onClick = onExitToHome) {
                Text("Exit to Home")
            }

            if (securityStatus.isBiometricsEnabled) {
                FilledTonalButton(onClick = onTriggerBiometric) {
                    Icon(
                        imageVector = Icons.Outlined.Fingerprint,
                        contentDescription = "Fingerprint",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Biometric")
                }
            }
        }
    }
}
