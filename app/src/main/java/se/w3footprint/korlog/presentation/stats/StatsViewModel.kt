package se.w3footprint.korlog.presentation.stats

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
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val repository: SessionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    init {
        loadStats(StatsPeriod.WEEK)
    }

    fun onPeriodSelected(period: StatsPeriod) {
        loadStats(period)
    }

    private fun loadStats(period: StatsPeriod) {
        _uiState.update { it.copy(period = period, isLoading = it.isInitialLoad) }
        val (start, end) = rangeFor(period)
        viewModelScope.launch {
            repository.getSessionsByDateRange(start, end).collect { sessions ->
                _uiState.update { buildState(period, sessions) }
            }
        }
    }

    private fun buildState(period: StatsPeriod, sessions: List<DrivingSession>): StatsUiState {
        val totalMillis = sessions.sumOf { it.drivingDurationMillis }
        val totalHours = totalMillis / 3_600_000.0
        val totalEarnings = sessions.sumOf { it.earningsSek }
        val hourlyRate = if (totalHours > 0) totalEarnings / totalHours else 0.0

        val dayBars = buildDayBars(period, sessions)
        val platformSlices = buildPlatformSlices(sessions, totalEarnings)

        return StatsUiState(
            period = period,
            totalHours = totalHours,
            totalEarnings = totalEarnings,
            totalSessions = sessions.size,
            hourlyRate = hourlyRate,
            dayBars = dayBars,
            platformSlices = platformSlices,
            isLoading = false,
            isInitialLoad = false
        )
    }

    private fun buildDayBars(period: StatsPeriod, sessions: List<DrivingSession>): List<DayBar> {
        val cal = Calendar.getInstance()
        return when (period) {
            StatsPeriod.WEEK -> {
                val weekDays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                weekDays.mapIndexed { index, label ->
                    val dayOfWeek = (index + 2).let { if (it > 7) it - 7 else it }
                    val daySessions = sessions.filter {
                        cal.timeInMillis = it.date
                        cal.get(Calendar.DAY_OF_WEEK) == dayOfWeek
                    }
                    DayBar(
                        label = label,
                        hours = (daySessions.sumOf { it.drivingDurationMillis } / 3_600_000.0).toFloat(),
                        earnings = daySessions.sumOf { it.earningsSek }
                    )
                }
            }
            StatsPeriod.MONTH -> {
                (1..4).map { week ->
                    val weekSessions = sessions.filter {
                        cal.timeInMillis = it.date
                        cal.get(Calendar.WEEK_OF_MONTH) == week
                    }
                    DayBar(
                        label = "W$week",
                        hours = (weekSessions.sumOf { it.drivingDurationMillis } / 3_600_000.0).toFloat(),
                        earnings = weekSessions.sumOf { it.earningsSek }
                    )
                }
            }
        }
    }

    private fun buildPlatformSlices(
        sessions: List<DrivingSession>,
        totalEarnings: Double
    ): List<PlatformSlice> {
        if (totalEarnings == 0.0) return emptyList()
        return Platform.entries
            .map { platform ->
                val earnings = sessions.filter { it.platform == platform }.sumOf { it.earningsSek }
                PlatformSlice(
                    platform = platform,
                    earnings = earnings,
                    fraction = (earnings / totalEarnings).toFloat()
                )
            }
            .filter { it.earnings > 0 }
            .sortedByDescending { it.earnings }
    }

    private fun rangeFor(period: StatsPeriod): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        return when (period) {
            StatsPeriod.WEEK -> {
                cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis
                cal.add(Calendar.WEEK_OF_YEAR, 1)
                start to cal.timeInMillis
            }
            StatsPeriod.MONTH -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis
                cal.add(Calendar.MONTH, 1)
                start to cal.timeInMillis
            }
        }
    }
}
