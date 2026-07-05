package se.w3footprint.korlog.presentation.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import se.w3footprint.korlog.domain.model.Platform
import se.w3footprint.korlog.domain.usecase.session.SaveSessionUseCase
import javax.inject.Inject

@HiltViewModel
class ActiveSessionViewModel @Inject constructor(
    private val saveSession: SaveSessionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActiveSessionUiState())
    val uiState: StateFlow<ActiveSessionUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    fun startSession() {
        val now = System.currentTimeMillis()
        _uiState.update { it.copy(isRunning = true, startTime = now, elapsedMillis = 0L) }
        startTimer()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1_000L)
                _uiState.update { state ->
                    state.copy(elapsedMillis = System.currentTimeMillis() - state.startTime)
                }
            }
        }
    }

    fun onEarningsChanged(value: String) {
        if (value.isEmpty() || value.matches(Regex("^\\d{0,6}([.,]\\d{0,2})?\$"))) {
            _uiState.update { it.copy(earningsInput = value) }
        }
    }

    fun onDistanceChanged(value: String) {
        if (value.isEmpty() || value.matches(Regex("^\\d{0,5}([.,]\\d{0,1})?\$"))) {
            _uiState.update { it.copy(distanceInput = value) }
        }
    }

    fun onPlatformSelected(platform: Platform) {
        _uiState.update { it.copy(selectedPlatform = platform) }
    }

    fun onNotesChanged(value: String) {
        _uiState.update { it.copy(notes = value) }
    }

    fun onStopRequested() {
        _uiState.update { it.copy(showStopConfirm = true) }
    }

    fun onStopConfirmDismissed() {
        _uiState.update { it.copy(showStopConfirm = false) }
    }

    fun confirmStop() {
        timerJob?.cancel()
        _uiState.update { it.copy(showStopConfirm = false, isSaving = true) }
        val state = _uiState.value
        val endTime = System.currentTimeMillis()
        viewModelScope.launch {
            val id = saveSession(
                startTime = state.startTime,
                endTime = endTime,
                earningsSek = state.earningsSek,
                distanceKm = state.distanceKm,
                platform = state.selectedPlatform,
                notes = state.notes
            )
            _uiState.update { it.copy(isSaving = false, savedSessionId = id) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
