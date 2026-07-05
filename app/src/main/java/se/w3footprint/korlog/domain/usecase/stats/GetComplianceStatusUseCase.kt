package se.w3footprint.korlog.domain.usecase.stats

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import se.w3footprint.korlog.domain.model.ComplianceStatus
import se.w3footprint.korlog.domain.repository.SessionRepository
import java.util.Calendar
import javax.inject.Inject

class GetComplianceStatusUseCase @Inject constructor(
    private val repository: SessionRepository
) {
    operator fun invoke(): Flow<ComplianceStatus> {
        val (weekStart, weekEnd) = currentWeekRange()
        val (monthStart, monthEnd) = currentMonthRange()
        val (dayStart, dayEnd) = currentDayRange()

        return combine(
            repository.getSessionsByDateRange(weekStart, weekEnd),
            repository.getSessionsByDateRange(monthStart, monthEnd),
            repository.getSessionsByDateRange(dayStart, dayEnd)
        ) { weekSessions, monthSessions, daySessions ->
            val weeklyHours = weekSessions.sumOf { it.durationMillis } / 3_600_000.0
            val monthlyHours = monthSessions.sumOf { it.durationMillis } / 3_600_000.0
            val dailyHours = daySessions.sumOf { it.durationMillis } / 3_600_000.0
            val lastSession = weekSessions.maxByOrNull { it.endTime }
            val restHours = lastSession?.let {
                (System.currentTimeMillis() - it.endTime) / 3_600_000.0
            } ?: 24.0

            ComplianceStatus(
                weeklyHours = weeklyHours,
                monthlyHours = monthlyHours,
                dailyHours = dailyHours,
                dailyRestHours = restHours.coerceAtMost(24.0)
            )
        }
    }

    private fun currentWeekRange(): Pair<Long, Long> {
        val cal = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        val start = cal.timeInMillis
        cal.add(Calendar.WEEK_OF_YEAR, 1)
        return start to cal.timeInMillis
    }

    private fun currentMonthRange(): Pair<Long, Long> {
        val cal = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        val start = cal.timeInMillis
        cal.add(Calendar.MONTH, 1)
        return start to cal.timeInMillis
    }

    private fun currentDayRange(): Pair<Long, Long> {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        val start = cal.timeInMillis
        cal.add(Calendar.DAY_OF_YEAR, 1)
        return start to cal.timeInMillis
    }
}
