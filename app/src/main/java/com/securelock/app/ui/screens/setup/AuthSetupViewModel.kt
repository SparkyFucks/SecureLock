package com.securelock.app.ui.screens.setup

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.securelock.app.SecureLockApp
import com.securelock.app.domain.model.AuthMethod
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class SetupStep {
    SELECT_METHOD,
    CREATE_CREDENTIAL,
    CONFIRM_CREDENTIAL,
    BIOMETRIC_OPTION
}

data class AuthSetupUiState(
    val currentStep: SetupStep = SetupStep.SELECT_METHOD,
    val selectedMethod: AuthMethod = AuthMethod.PIN,
    val initialCredential: String = "",
    val confirmCredential: String = "",
    val errorMessage: String? = null,
    val isComplete: Boolean = false
)

class AuthSetupViewModel(application: Application) : AndroidViewModel(application) {
    private val prefsManager = (application as SecureLockApp).preferencesManager

    private val _uiState = MutableStateFlow(AuthSetupUiState())
    val uiState: StateFlow<AuthSetupUiState> = _uiState.asStateFlow()

    fun selectMethod(method: AuthMethod) {
        _uiState.value = _uiState.value.copy(
            selectedMethod = method,
            currentStep = SetupStep.CREATE_CREDENTIAL,
            initialCredential = "",
            confirmCredential = "",
            errorMessage = null
        )
    }

    fun onInitialCredentialEntered(credential: String) {
        // Validate credential format
        val error = validateCredentialFormat(_uiState.value.selectedMethod, credential)
        if (error != null) {
            _uiState.value = _uiState.value.copy(errorMessage = error)
            return
        }

        _uiState.value = _uiState.value.copy(
            initialCredential = credential,
            currentStep = SetupStep.CONFIRM_CREDENTIAL,
            confirmCredential = "",
            errorMessage = null
        )
    }

    fun onConfirmCredentialEntered(credential: String, onAdvanceToBiometrics: () -> Unit) {
        if (credential != _uiState.value.initialCredential) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Credentials do not match. Please try again.",
                confirmCredential = ""
            )
            return
        }

        // Credentials match! Save to DataStore with cryptographic salt + PBKDF2 hash
        viewModelScope.launch {
            prefsManager.saveCredential(_uiState.value.selectedMethod, credential)
            _uiState.value = _uiState.value.copy(
                currentStep = SetupStep.BIOMETRIC_OPTION,
                errorMessage = null
            )
            onAdvanceToBiometrics()
        }
    }

    fun goBackStep() {
        when (_uiState.value.currentStep) {
            SetupStep.SELECT_METHOD -> { /* Root */ }
            SetupStep.CREATE_CREDENTIAL -> {
                _uiState.value = _uiState.value.copy(
                    currentStep = SetupStep.SELECT_METHOD,
                    initialCredential = "",
                    errorMessage = null
                )
            }
            SetupStep.CONFIRM_CREDENTIAL -> {
                _uiState.value = _uiState.value.copy(
                    currentStep = SetupStep.CREATE_CREDENTIAL,
                    confirmCredential = "",
                    errorMessage = null
                )
            }
            SetupStep.BIOMETRIC_OPTION -> {
                _uiState.value = _uiState.value.copy(currentStep = SetupStep.CONFIRM_CREDENTIAL)
            }
        }
    }

    private fun validateCredentialFormat(method: AuthMethod, credential: String): String? {
        return when (method) {
            AuthMethod.PIN -> {
                if (credential.length < 4 || credential.length > 8) {
                    "PIN must be between 4 and 8 digits"
                } else null
            }
            AuthMethod.PASSWORD -> {
                if (credential.length < 6) {
                    "Password must be at least 6 characters"
                } else if (!credential.any { it.isLetter() } || !credential.any { it.isDigit() }) {
                    "Password must include both letters and numbers"
                } else null
            }
            AuthMethod.PATTERN -> {
                val dots = credential.split("-")
                if (dots.size < 4) {
                    "Pattern must connect at least 4 dots"
                } else null
            }
        }
    }
}
