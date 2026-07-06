package se.w3footprint.korlog.presentation.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import se.w3footprint.korlog.data.local.store.ActiveSessionStore
import se.w3footprint.korlog.data.local.store.PersistedSessionState
import se.w3footprint.korlog.domain.model.Platform
import se.w3footprint.korlog.domain.usecase.session.SaveSessionUseCase
import se.w3footprint.korlog.worker.BreakReminderScheduler
import javax.inject.Inject

@HiltViewModel
class ActiveSessionViewModel @Inject constructor(
    private val saveSession: SaveSessionUseCase,
    private val breakReminderScheduler: BreakReminderScheduler,
    private val sessionStore: ActiveSessionStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActiveSessionUiState())
    val uiState: StateFlow<ActiveSessionUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    init {
        viewModelScope.launch {
            val persisted = sessionStore.state.first()
            if (persisted.isRunning) {
                val platform = runCatching { Platform.valueOf(persisted.platform) }
                    .getOrDefault(Platform.OTHER)
                val now = System.currentTimeMillis()
                val elapsed = if (persisted.isOnBreak) {
                    persisted.startTime.let { now - it - persisted.totalBreakMillis - (now - persisted.currentBreakStartMillis) }
                        .coerceAtLeast(0L)
                } else {
                    (now - persisted.startTime - persisted.totalBreakMillis).coerceAtLeast(0L)
                }
                _uiState.update {
                    it.copy(
                        isRunning = true,
                        isOnBreak = persisted.isOnBreak,
                        startTime = persisted.startTime,
                        elapsedMillis = elapsed,
                        totalBreakMillis = persisted.totalBreakMillis,
                        currentBreakStartMillis = persisted.currentBreakStartMillis,
                        earningsInput = persisted.earningsInput,
                        distanceInput = persisted.distanceInput,
                        selectedPlatform = platform,
                        notes = persisted.notes
                    )
                }
                if (!persisted.isOnBreak) startTicking()
            }
        }
    }

    fun startSession() {
        if (_uiState.value.isRunning) return
        val now = System.currentTimeMillis()
        _uiState.update { it.copy(isRunning = true, startTime = now, elapsedMillis = 0L) }
        persist()
        startTicking()
        breakReminderScheduler.schedule(driveTimeHours = 6L)
    }

    fun takeBreak() {
        timerJob?.cancel()
        _uiState.update { it.copy(isOnBreak = true, currentBreakStartMillis = System.currentTimeMillis()) }
        persist()
        breakReminderScheduler.cancel()
    }

    fun resumeFromBreak() {
        val now = System.currentTimeMillis()
        _uiState.update { state ->
            val breakDuration = now - state.currentBreakStartMillis
            state.copy(
                isOnBreak = false,
                totalBreakMillis = state.totalBreakMillis + breakDuration,
                currentBreakStartMillis = 0L
            )
        }
        persist()
        startTicking()
        breakReminderScheduler.schedule(driveTimeHours = 6L)
    }

    private fun startTicking() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1_000L)
                val state = _uiState.value
                val now = System.currentTimeMillis()
                val elapsed = (now - state.startTime - state.totalBreakMillis).coerceAtLeast(0L)
                _uiState.update { it.copy(elapsedMillis = elapsed) }
            }
        }
    }

    fun onEarningsChanged(value: String) {
        if (value.isEmpty() || value.matches(Regex("^\\d{0,6}([.,]\\d{0,2})?\$"))) {
            _uiState.update { it.copy(earningsInput = value) }
            persist()
        }
    }

    fun onDistanceChanged(value: String) {
        if (value.isEmpty() || value.matches(Regex("^\\d{0,5}([.,]\\d{0,1})?\$"))) {
            _uiState.update { it.copy(distanceInput = value) }
            persist()
        }
    }

    fun onPlatformSelected(platform: Platform) {
        _uiState.update { it.copy(selectedPlatform = platform) }
        persist()
    }

    fun onNotesChanged(value: String) {
        _uiState.update { it.copy(notes = value) }
        persist()
    }

    fun onStopRequested() {
        _uiState.update { it.copy(showStopConfirm = true) }
    }

    fun onStopConfirmDismissed() {
        _uiState.update { it.copy(showStopConfirm = false) }
    }

    fun confirmStop() {
        breakReminderScheduler.cancel()
        val now = System.currentTimeMillis()
        _uiState.update { state ->
            val extraBreak = if (state.isOnBreak) now - state.currentBreakStartMillis else 0L
            state.copy(
                showStopConfirm = false,
                isSaving = true,
                isOnBreak = false,
                totalBreakMillis = state.totalBreakMillis + extraBreak
            )
        }
        timerJob?.cancel()

        val state = _uiState.value
        viewModelScope.launch {
            sessionStore.clear()
            val id = saveSession(
                startTime = state.startTime,
                endTime = now,
                breakDurationMillis = state.totalBreakMillis,
                earningsSek = state.earningsSek,
                distanceKm = state.distanceKm,
                platform = state.selectedPlatform,
                notes = state.notes
            )
            _uiState.update { it.copy(isSaving = false, savedSessionId = id) }
        }
    }

    private fun persist() {
        viewModelScope.launch {
            val s = _uiState.value
            sessionStore.save(
                PersistedSessionState(
                    isRunning = s.isRunning,
                    isOnBreak = s.isOnBreak,
                    startTime = s.startTime,
                    totalBreakMillis = s.totalBreakMillis,
                    currentBreakStartMillis = s.currentBreakStartMillis,
                    earningsInput = s.earningsInput,
                    distanceInput = s.distanceInput,
                    platform = s.selectedPlatform.name,
                    notes = s.notes
                )
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
