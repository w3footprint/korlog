package se.w3footprint.korlog.presentation.stats

import se.w3footprint.korlog.domain.model.Platform

enum class StatsPeriod { WEEK, MONTH }

data class DayBar(val label: String, val hours: Float, val earnings: Double)
data class PlatformSlice(val platform: Platform, val earnings: Double, val fraction: Float)

data class StatsUiState(
    val period: StatsPeriod = StatsPeriod.WEEK,
    val totalHours: Double = 0.0,
    val totalEarnings: Double = 0.0,
    val totalSessions: Int = 0,
    val hourlyRate: Double = 0.0,
    val dayBars: List<DayBar> = emptyList(),
    val platformSlices: List<PlatformSlice> = emptyList(),
    val isLoading: Boolean = true
)
