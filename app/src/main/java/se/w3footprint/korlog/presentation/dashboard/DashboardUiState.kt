package se.w3footprint.korlog.presentation.dashboard

import se.w3footprint.korlog.domain.model.ComplianceStatus
import se.w3footprint.korlog.domain.model.DrivingSession
import se.w3footprint.korlog.domain.model.WorkStats

data class DashboardUiState(
    val weeklyStats: WorkStats = WorkStats(0L, 0.0, 0.0, 0),
    val monthlyStats: WorkStats = WorkStats(0L, 0.0, 0.0, 0),
    val compliance: ComplianceStatus = ComplianceStatus(
        weeklyHours = 0.0,
        monthlyHours = 0.0,
        dailyHours = 0.0,
        dailyRestHours = 24.0
    ),
    val recentSessions: List<DrivingSession> = emptyList(),
    val isLoading: Boolean = true,
    val hasActiveSession: Boolean = false
)
