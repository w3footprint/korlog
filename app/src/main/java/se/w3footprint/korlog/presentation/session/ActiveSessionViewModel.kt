package se.w3footprint.korlog.presentation.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import se.w3footprint.korlog.domain.model.Platform
import se.w3footprint.korlog.domain.session.ActiveSessionManager
import se.w3footprint.korlog.domain.session.ActiveSessionState
import se.w3footprint.korlog.domain.usecase.session.SaveSessionUseCase
import se.w3footprint.korlog.worker.BreakReminderScheduler
import javax.inject.Inject

private fun ActiveSessionState.toUiState() = ActiveSessionUiState(
    isRunning = isRunning,
    isOnBreak = isOnBreak,
    startTime = startTime,
    elapsedMillis = elapsedMillis,
    totalBreakMillis = totalBreakMillis,
    currentBreakStartMillis = currentBreakStartMillis,
    earningsInput = earningsInput,
    distanceInput = distanceInput,
    selectedPlatform = selectedPlatform,
    notes = notes
)

@HiltViewModel
class ActiveSessionViewModel @Inject constructor(
    private val sessionManager: ActiveSessionManager,
    private val saveSession: SaveSessionUseCase,
    private val breakReminderScheduler: BreakReminderScheduler
) : ViewModel() {

    val sessionState = sessionManager.state

    private val _uiState = MutableStateFlow(sessionManager.state.value.toUiState())
    val uiState: StateFlow<ActiveSessionUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            sessionManager.state.collect { s ->
                _uiState.update {
                    it.copy(
                        isRunning = s.isRunning,
                        isOnBreak = s.isOnBreak,
                        startTime = s.startTime,
                        elapsedMillis = s.elapsedMillis,
                        totalBreakMillis = s.totalBreakMillis,
                        currentBreakStartMillis = s.currentBreakStartMillis,
                        earningsInput = s.earningsInput,
                        distanceInput = s.distanceInput,
                        selectedPlatform = s.selectedPlatform,
                        notes = s.notes
                    )
                }
            }
        }
    }

    fun startSession() {
        sessionManager.startSession()
        breakReminderScheduler.schedule(driveTimeHours = 6L)
    }

    fun takeBreak() {
        sessionManager.takeBreak()
        breakReminderScheduler.cancel()
    }

    fun resumeFromBreak() {
        sessionManager.resumeFromBreak()
        breakReminderScheduler.schedule(driveTimeHours = 6L)
    }

    fun onEarningsChanged(value: String) {
        if (value.isEmpty() || value.matches(Regex("^\\d{0,6}([.,]\\d{0,2})?\$"))) {
            sessionManager.updateEarnings(value)
        }
    }

    fun onDistanceChanged(value: String) {
        if (value.isEmpty() || value.matches(Regex("^\\d{0,5}([.,]\\d{0,1})?\$"))) {
            sessionManager.updateDistance(value)
        }
    }

    fun onPlatformSelected(platform: Platform) = sessionManager.updatePlatform(platform)
    fun onNotesChanged(value: String) = sessionManager.updateNotes(value)

    fun onStopRequested() = _uiState.update { it.copy(showStopConfirm = true) }
    fun onStopConfirmDismissed() = _uiState.update { it.copy(showStopConfirm = false) }

    fun confirmStop() {
        breakReminderScheduler.cancel()
        val ui = _uiState.value
        _uiState.update { it.copy(showStopConfirm = false, isSaving = true) }
        val now = System.currentTimeMillis()
        val s = sessionManager.stopAndClear()
        val totalBreak = if (s.isOnBreak) s.totalBreakMillis + (now - s.currentBreakStartMillis)
                         else s.totalBreakMillis
        viewModelScope.launch {
            val id = saveSession(
                startTime = s.startTime,
                endTime = now,
                breakDurationMillis = totalBreak,
                earningsSek = ui.earningsSek,
                distanceKm = ui.distanceKm,
                platform = s.selectedPlatform,
                notes = s.notes
            )
            _uiState.update { it.copy(isSaving = false, savedSessionId = id) }
        }
    }
}
