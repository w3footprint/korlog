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
import se.w3footprint.korlog.domain.repository.SessionRepository
import javax.inject.Inject

data class SessionDetailUiState(
    val session: DrivingSession? = null,
    val isLoading: Boolean = true,
    val deleted: Boolean = false
)

@HiltViewModel
class SessionDetailViewModel @Inject constructor(
    private val repository: SessionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SessionDetailUiState())
    val uiState: StateFlow<SessionDetailUiState> = _uiState.asStateFlow()

    fun loadSession(id: Long) {
        viewModelScope.launch {
            val session = repository.getSessionById(id)
            _uiState.update { it.copy(session = session, isLoading = false) }
        }
    }

    fun deleteSession() {
        val session = _uiState.value.session ?: return
        viewModelScope.launch {
            repository.deleteSession(session)
            _uiState.update { it.copy(deleted = true) }
        }
    }
}
