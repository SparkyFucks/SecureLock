package com.securelock.app.ui.screens.setup

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Password
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.securelock.app.domain.model.AuthMethod
import com.securelock.app.ui.screens.setup.components.PasswordInputView
import com.securelock.app.ui.screens.setup.components.PatternLockView
import com.securelock.app.ui.screens.setup.components.PinInputView
import com.securelock.app.ui.theme.StatusError

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthSetupScreen(
    viewModel: AuthSetupViewModel,
    onNavigateBack: () -> Unit,
    onAdvanceToBiometrics: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (state.currentStep) {
                            SetupStep.SELECT_METHOD -> "Choose Lock Method"
                            SetupStep.CREATE_CREDENTIAL -> "Create ${state.selectedMethod.title}"
                            SetupStep.CONFIRM_CREDENTIAL -> "Confirm ${state.selectedMethod.title}"
                            SetupStep.BIOMETRIC_OPTION -> "Biometric Security"
                        },
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (state.currentStep == SetupStep.SELECT_METHOD) {
                                onNavigateBack()
                            } else {
                                viewModel.goBackStep()
                            }
                        }
                    ) {
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
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Error banner if any
            state.errorMessage?.let { msg ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.errorContainer)
                        .padding(12.dp)
                ) {
                    Text(
                        text = msg,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            AnimatedContent(
                targetState = state.currentStep,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "setup_step_transition"
            ) { step ->
                when (step) {
                    SetupStep.SELECT_METHOD -> {
                        SelectMethodStep(
                            onSelect = { method -> viewModel.selectMethod(method) }
                        )
                    }
                    SetupStep.CREATE_CREDENTIAL -> {
                        CreateCredentialStep(
                            method = state.selectedMethod,
                            onComplete = { cred -> viewModel.onInitialCredentialEntered(cred) }
                        )
                    }
                    SetupStep.CONFIRM_CREDENTIAL -> {
                        ConfirmCredentialStep(
                            method = state.selectedMethod,
                            onComplete = { cred -> viewModel.onConfirmCredentialEntered(cred, onAdvanceToBiometrics) }
                        )
                    }
                    SetupStep.BIOMETRIC_OPTION -> {
                        // Handled by separate destination or inline
                        Box(modifier = Modifier.fillMaxSize())
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectMethodStep(onSelect: (AuthMethod) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Select how you would like to unlock your protected applications. You can change this later in settings.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        AuthMethodCard(
            title = AuthMethod.PIN.title,
            description = AuthMethod.PIN.subtitle,
            icon = Icons.Filled.Dialpad,
            onClick = { onSelect(AuthMethod.PIN) }
        )

        AuthMethodCard(
            title = AuthMethod.PASSWORD.title,
            description = AuthMethod.PASSWORD.subtitle,
            icon = Icons.Filled.Password,
            onClick = { onSelect(AuthMethod.PASSWORD) }
        )

        AuthMethodCard(
            title = AuthMethod.PATTERN.title,
            description = AuthMethod.PATTERN.subtitle,
            icon = Icons.Filled.GridOn,
            onClick = { onSelect(AuthMethod.PATTERN) }
        )
    }
}

@Composable
private fun AuthMethodCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun CreateCredentialStep(
    method: AuthMethod,
    onComplete: (String) -> Unit
) {
    var pinString by remember { mutableStateOf("") }
    var passwordString by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (method) {
            AuthMethod.PIN -> {
                Text(
                    text = "Enter a 4 to 8 digit numerical PIN",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                PinInputView(
                    pinLength = 4,
                    currentLength = pinString.length,
                    onDigitClick = { digit ->
                        if (pinString.length < 8) {
                            pinString += digit
                            if (pinString.length == 4) {
                                onComplete(pinString)
                            }
                        }
                    },
                    onDeleteClick = {
                        if (pinString.isNotEmpty()) {
                            pinString = pinString.dropLast(1)
                        }
                    }
                )
            }
            AuthMethod.PASSWORD -> {
                PasswordInputView(
                    value = passwordString,
                    onValueChange = { passwordString = it },
                    label = "Create Master Password",
                    onDone = {
                        if (passwordString.length >= 6) {
                            onComplete(passwordString)
                        }
                    }
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { onComplete(passwordString) },
                    enabled = passwordString.length >= 6 && passwordString.any { it.isLetter() } && passwordString.any { it.isDigit() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Continue")
                }
            }
            AuthMethod.PATTERN -> {
                PatternLockView(
                    onPatternComplete = { pattern ->
                        onComplete(pattern)
                    }
                )
            }
        }
    }
}

@Composable
private fun ConfirmCredentialStep(
    method: AuthMethod,
    onComplete: (String) -> Unit
) {
    var pinString by remember { mutableStateOf("") }
    var passwordString by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (method) {
            AuthMethod.PIN -> {
                Text(
                    text = "Re-enter your PIN to confirm",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                PinInputView(
                    pinLength = 4,
                    currentLength = pinString.length,
                    onDigitClick = { digit ->
                        if (pinString.length < 8) {
                            pinString += digit
                            if (pinString.length == 4) {
                                onComplete(pinString)
                            }
                        }
                    },
                    onDeleteClick = {
                        if (pinString.isNotEmpty()) {
                            pinString = pinString.dropLast(1)
                        }
                    }
                )
            }
            AuthMethod.PASSWORD -> {
                PasswordInputView(
                    value = passwordString,
                    onValueChange = { passwordString = it },
                    label = "Confirm Master Password",
                    onDone = {
                        onComplete(passwordString)
                    }
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { onComplete(passwordString) },
                    enabled = passwordString.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Confirm & Save")
                }
            }
            AuthMethod.PATTERN -> {
                PatternLockView(
                    onPatternComplete = { pattern ->
                        onComplete(pattern)
                    }
                )
            }
        }
    }
}
