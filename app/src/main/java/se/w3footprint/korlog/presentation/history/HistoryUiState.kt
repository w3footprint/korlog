package se.w3footprint.korlog.presentation.history

import se.w3footprint.korlog.domain.model.DrivingSession
import se.w3footprint.korlog.domain.model.Platform

data class HistoryUiState(
    val sessions: List<DrivingSession> = emptyList(),
    val filteredSessions: List<DrivingSession> = emptyList(),
    val searchQuery: String = "",
    val selectedPlatform: Platform? = null,
    val isLoading: Boolean = true
)
