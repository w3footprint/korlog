package se.w3footprint.korlog.presentation.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import se.w3footprint.korlog.domain.model.DrivingSession
import se.w3footprint.korlog.domain.repository.SessionRepository
import javax.inject.Inject

data class SessionSummaryUiState(
    val session: DrivingSession? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class SessionSummaryViewModel @Inject constructor(
    private val repository: SessionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SessionSummaryUiState())
    val uiState: StateFlow<SessionSummaryUiState> = _uiState.asStateFlow()

    fun loadSession(id: Long) {
        viewModelScope.launch {
            val session = repository.getSessionById(id)
            _uiState.update { it.copy(session = session, isLoading = false) }
        }
    }
}
