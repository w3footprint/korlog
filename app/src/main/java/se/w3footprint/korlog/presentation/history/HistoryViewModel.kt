package se.w3footprint.korlog.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import se.w3footprint.korlog.domain.model.DrivingSession
import se.w3footprint.korlog.domain.model.Platform
import se.w3footprint.korlog.domain.repository.SessionRepository
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: SessionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAllSessions().collect { sessions ->
                _uiState.update { state ->
                    state.copy(
                        sessions = sessions,
                        filteredSessions = applyFilters(sessions, state.searchQuery, state.selectedPlatform),
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onSearchChanged(query: String) {
        _uiState.update { state ->
            state.copy(
                searchQuery = query,
                filteredSessions = applyFilters(state.sessions, query, state.selectedPlatform)
            )
        }
    }

    fun onPlatformFilterChanged(platform: Platform?) {
        _uiState.update { state ->
            state.copy(
                selectedPlatform = platform,
                filteredSessions = applyFilters(state.sessions, state.searchQuery, platform)
            )
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            repository.syncFromCloud()
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    fun deleteSession(session: DrivingSession) {
        viewModelScope.launch {
            repository.deleteSession(session)
        }
    }

    private fun applyFilters(
        sessions: List<DrivingSession>,
        query: String,
        platform: Platform?
    ): List<DrivingSession> {
        return sessions
            .filter { platform == null || it.platform == platform }
            .filter {
                query.isBlank() ||
                    it.notes.contains(query, ignoreCase = true) ||
                    it.platform.name.contains(query, ignoreCase = true)
            }
    }
}
