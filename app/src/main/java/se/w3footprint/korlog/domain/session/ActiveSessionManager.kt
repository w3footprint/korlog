package se.w3footprint.korlog.domain.session

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
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
import javax.inject.Inject
import javax.inject.Singleton

data class ActiveSessionState(
    val isRunning: Boolean = false,
    val isOnBreak: Boolean = false,
    val startTime: Long = 0L,
    val elapsedMillis: Long = 0L,
    val totalBreakMillis: Long = 0L,
    val currentBreakStartMillis: Long = 0L,
    val earningsInput: String = "",
    val distanceInput: String = "",
    val selectedPlatform: Platform = Platform.OTHER,
    val notes: String = "",
    val isRestored: Boolean = false
) {
    val drivingMillis: Long get() = elapsedMillis
    val formattedElapsed: String get() {
        val driving = (elapsedMillis - totalBreakMillis).coerceAtLeast(0L)
        val h = driving / 3_600_000
        val m = (driving % 3_600_000) / 60_000
        val s = (driving % 60_000) / 1_000
        return "%02d:%02d:%02d".format(h, m, s)
    }
}

@Singleton
class ActiveSessionManager @Inject constructor(
    private val store: ActiveSessionStore
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var timerJob: Job? = null

    private val _state = MutableStateFlow(ActiveSessionState())
    val state: StateFlow<ActiveSessionState> = _state.asStateFlow()

    init {
        scope.launch {
            val persisted = store.state.first()
            if (persisted.isRunning) {
                val platform = runCatching { Platform.valueOf(persisted.platform) }
                    .getOrDefault(Platform.OTHER)
                val now = System.currentTimeMillis()
                // Wall-clock elapsed; UiState subtracts totalBreakMillis to get driving time
                val elapsed = (now - persisted.startTime).coerceAtLeast(0L)
                _state.update {
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
                        notes = persisted.notes,
                        isRestored = true
                    )
                }
                if (!persisted.isOnBreak) startTicking()
            } else {
                _state.update { it.copy(isRestored = true) }
            }
        }
    }

    fun startSession() {
        if (_state.value.isRunning) return
        val now = System.currentTimeMillis()
        _state.update {
            it.copy(isRunning = true, startTime = now, elapsedMillis = 0L, isRestored = true)
        }
        persist()
        startTicking()
    }

    fun takeBreak() {
        timerJob?.cancel()
        _state.update { it.copy(isOnBreak = true, currentBreakStartMillis = System.currentTimeMillis()) }
        persist()
    }

    fun resumeFromBreak() {
        val now = System.currentTimeMillis()
        _state.update { s ->
            val breakDuration = now - s.currentBreakStartMillis
            s.copy(
                isOnBreak = false,
                totalBreakMillis = s.totalBreakMillis + breakDuration,
                currentBreakStartMillis = 0L
            )
        }
        persist()
        startTicking()
    }

    fun updateEarnings(value: String) {
        _state.update { it.copy(earningsInput = value) }
        persist()
    }

    fun updateDistance(value: String) {
        _state.update { it.copy(distanceInput = value) }
        persist()
    }

    fun updatePlatform(platform: Platform) {
        _state.update { it.copy(selectedPlatform = platform) }
        persist()
    }

    fun updateNotes(value: String) {
        _state.update { it.copy(notes = value) }
        persist()
    }

    fun stopAndClear(): ActiveSessionState {
        timerJob?.cancel()
        val final = _state.value
        _state.update { ActiveSessionState(isRestored = true) }
        scope.launch { store.clear() }
        return final
    }

    private fun startTicking() {
        timerJob?.cancel()
        timerJob = scope.launch {
            while (true) {
                delay(1_000L)
                val s = _state.value
                if (!s.isRunning || s.isOnBreak) break
                val now = System.currentTimeMillis()
                // Store raw wall-clock elapsed; UiState subtracts totalBreakMillis to get driving time
                val elapsed = (now - s.startTime).coerceAtLeast(0L)
                _state.update { it.copy(elapsedMillis = elapsed) }
            }
        }
    }

    private fun persist() {
        val s = _state.value
        scope.launch {
            store.save(
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
}
