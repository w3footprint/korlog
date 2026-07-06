package se.w3footprint.korlog.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import se.w3footprint.korlog.data.local.store.ActiveSessionStore
import se.w3footprint.korlog.domain.repository.SessionRepository
import se.w3footprint.korlog.domain.usecase.stats.GetComplianceStatusUseCase
import se.w3footprint.korlog.domain.usecase.stats.GetMonthlyStatsUseCase
import se.w3footprint.korlog.domain.usecase.stats.GetWeeklyStatsUseCase
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getWeeklyStats: GetWeeklyStatsUseCase,
    private val getMonthlyStats: GetMonthlyStatsUseCase,
    private val getComplianceStatus: GetComplianceStatusUseCase,
    private val repository: SessionRepository,
    private val sessionStore: ActiveSessionStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboard()
    }

    private fun loadDashboard() {
        viewModelScope.launch {
            combine(
                combine(getWeeklyStats(), getMonthlyStats(), getComplianceStatus()) { w, m, c -> Triple(w, m, c) },
                combine(repository.getAllSessions(), sessionStore.state) { sessions, store -> Pair(sessions, store) }
            ) { (weekly, monthly, compliance), (allSessions, sessionState) ->
                DashboardUiState(
                    weeklyStats = weekly,
                    monthlyStats = monthly,
                    compliance = compliance,
                    recentSessions = allSessions.sortedByDescending { it.date }.take(5),
                    isLoading = false,
                    hasActiveSession = sessionState.isRunning
                )
            }.collect { state ->
                _uiState.update { state }
            }
        }
    }
}
