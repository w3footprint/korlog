package se.w3footprint.korlog.domain.usecase.session

import se.w3footprint.korlog.domain.model.DrivingSession
import se.w3footprint.korlog.domain.model.Platform
import se.w3footprint.korlog.domain.repository.SessionRepository
import javax.inject.Inject

class SaveSessionUseCase @Inject constructor(
    private val repository: SessionRepository
) {
    suspend operator fun invoke(
        startTime: Long,
        endTime: Long,
        breakDurationMillis: Long = 0L,
        earningsSek: Double,
        distanceKm: Double,
        platform: Platform,
        notes: String
    ): Long {
        val session = DrivingSession(
            startTime = startTime,
            endTime = endTime,
            durationMillis = endTime - startTime,
            breakDurationMillis = breakDurationMillis,
            earningsSek = earningsSek,
            distanceKm = distanceKm,
            platform = platform,
            notes = notes,
            date = startTime
        )
        return repository.insertSession(session)
    }
}
