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

data class SessionDetailUiState(
    val session: DrivingSession? = null,
    val isLoading: Boolean = true,
    val deleted: Boolean = false,
    val isEditing: Boolean = false,
    val editEarnings: String = "",
    val editDistance: String = "",
    val editPlatform: Platform = Platform.OTHER,
    val editNotes: String = ""
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

    fun startEditing() {
        val s = _uiState.value.session ?: return
        _uiState.update {
            it.copy(
                isEditing = true,
                editEarnings = if (s.earningsSek > 0) s.earningsSek.toInt().toString() else "",
                editDistance = if (s.distanceKm > 0) s.distanceKm.toInt().toString() else "",
                editPlatform = s.platform,
                editNotes = s.notes
            )
        }
    }

    fun onEditEarningsChanged(v: String) = _uiState.update { it.copy(editEarnings = v) }
    fun onEditDistanceChanged(v: String) = _uiState.update { it.copy(editDistance = v) }
    fun onEditPlatformChanged(v: Platform) = _uiState.update { it.copy(editPlatform = v) }
    fun onEditNotesChanged(v: String) = _uiState.update { it.copy(editNotes = v) }
    fun cancelEditing() = _uiState.update { it.copy(isEditing = false) }

    fun saveEdit() {
        val session = _uiState.value.session ?: return
        val state = _uiState.value
        val updated = session.copy(
            earningsSek = state.editEarnings.toDoubleOrNull() ?: session.earningsSek,
            distanceKm = state.editDistance.toDoubleOrNull() ?: session.distanceKm,
            platform = state.editPlatform,
            notes = state.editNotes
        )
        viewModelScope.launch {
            repository.updateSession(updated)
            _uiState.update { it.copy(session = updated, isEditing = false) }
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
