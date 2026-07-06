package se.w3footprint.korlog.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import se.w3footprint.korlog.data.auth.AuthRepository
import javax.inject.Inject

data class SettingsUiState(
    val isSigningOut: Boolean = false,
    val signedOut: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun signOut() {
        viewModelScope.launch {
            _uiState.value = SettingsUiState(isSigningOut = true)
            authRepository.signOut()
            _uiState.value = SettingsUiState(signedOut = true)
        }
    }
}
