package se.w3footprint.korlog.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import se.w3footprint.korlog.data.auth.AuthRepository
import se.w3footprint.korlog.data.auth.AuthResult
import javax.inject.Inject

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val resetEmailSent: Boolean = false
) {
    val emailValid: Boolean get() = email.contains("@") && email.contains(".")
    val passwordValid: Boolean get() = password.length >= 6
    val canSubmitLogin: Boolean get() = emailValid && passwordValid && !isLoading
    val canSubmitRegister: Boolean get() = emailValid && passwordValid
        && password == confirmPassword && !isLoading
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun onEmailChanged(value: String) = _uiState.update { it.copy(email = value.trim(), error = null) }
    fun onPasswordChanged(value: String) = _uiState.update { it.copy(password = value, error = null) }
    fun onConfirmPasswordChanged(value: String) = _uiState.update { it.copy(confirmPassword = value, error = null) }

    fun signIn() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = authRepository.signIn(_uiState.value.email, _uiState.value.password)
            _uiState.update {
                when (result) {
                    is AuthResult.Success -> it.copy(isLoading = false, isSuccess = true)
                    is AuthResult.Error -> it.copy(isLoading = false, error = result.message)
                }
            }
        }
    }

    fun register() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = authRepository.register(_uiState.value.email, _uiState.value.password)
            _uiState.update {
                when (result) {
                    is AuthResult.Success -> it.copy(isLoading = false, isSuccess = true)
                    is AuthResult.Error -> it.copy(isLoading = false, error = result.message)
                }
            }
        }
    }

    fun sendPasswordReset() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = authRepository.sendPasswordReset(_uiState.value.email)
            _uiState.update {
                when (result) {
                    is AuthResult.Success -> it.copy(isLoading = false, resetEmailSent = true)
                    is AuthResult.Error -> it.copy(isLoading = false, error = result.message)
                }
            }
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
}
