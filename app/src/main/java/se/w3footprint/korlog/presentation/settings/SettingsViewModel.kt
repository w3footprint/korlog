package se.w3footprint.korlog.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import se.w3footprint.korlog.data.auth.AuthRepository
import se.w3footprint.korlog.data.local.store.UserPreferencesStore
import se.w3footprint.korlog.domain.repository.SessionRepository
import se.w3footprint.korlog.worker.BreakReminderScheduler
import javax.inject.Inject

data class SettingsUiState(
    val isSigningOut: Boolean = false,
    val signedOut: Boolean = false,
    val weeklyLimitHours: Int = 60,
    val monthlyLimitHours: Int = 192,
    val notificationsEnabled: Boolean = true
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionRepository: SessionRepository,
    private val prefs: UserPreferencesStore,
    private val reminderScheduler: BreakReminderScheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                prefs.weeklyLimitHours,
                prefs.monthlyLimitHours,
                prefs.notificationsEnabled
            ) { weekly, monthly, notifications ->
                Triple(weekly, monthly, notifications)
            }.collect { (weekly, monthly, notifications) ->
                _uiState.update {
                    it.copy(
                        weeklyLimitHours = weekly,
                        monthlyLimitHours = monthly,
                        notificationsEnabled = notifications
                    )
                }
            }
        }
    }

    fun setWeeklyLimit(hours: Int) {
        viewModelScope.launch { prefs.setWeeklyLimitHours(hours) }
    }

    fun setMonthlyLimit(hours: Int) {
        viewModelScope.launch { prefs.setMonthlyLimitHours(hours) }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            prefs.setNotificationsEnabled(enabled)
            if (!enabled) reminderScheduler.cancel()
        }
    }

    fun signOut() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSigningOut = true) }
            sessionRepository.stopRealtimeSync()
            authRepository.signOut()
            _uiState.update { it.copy(signedOut = true) }
        }
    }
}
