package se.w3footprint.korlog.domain.usecase.stats

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import se.w3footprint.korlog.domain.model.WorkStats
import se.w3footprint.korlog.domain.repository.SessionRepository
import java.util.Calendar
import javax.inject.Inject

class GetWeeklyStatsUseCase @Inject constructor(
    private val repository: SessionRepository
) {
    operator fun invoke(): Flow<WorkStats> {
        val (start, end) = currentWeekRange()
        return repository.getSessionsByDateRange(start, end).map { sessions ->
            WorkStats(
                totalDurationMillis = sessions.sumOf { it.drivingDurationMillis },
                totalEarningsSek = sessions.sumOf { it.earningsSek },
                totalDistanceKm = sessions.sumOf { it.distanceKm },
                sessionCount = sessions.size
            )
        }
    }

    private fun currentWeekRange(): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        cal.add(Calendar.WEEK_OF_YEAR, 1)
        val end = cal.timeInMillis
        return start to end
    }
}
