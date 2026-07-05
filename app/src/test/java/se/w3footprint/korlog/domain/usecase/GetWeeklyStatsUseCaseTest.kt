package se.w3footprint.korlog.domain.usecase

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import se.w3footprint.korlog.domain.model.DrivingSession
import se.w3footprint.korlog.domain.model.Platform
import se.w3footprint.korlog.domain.repository.SessionRepository
import se.w3footprint.korlog.domain.usecase.stats.GetWeeklyStatsUseCase

class GetWeeklyStatsUseCaseTest {

    private val repository: SessionRepository = mockk()
    private val useCase = GetWeeklyStatsUseCase(repository)

    @Test
    fun `returns zero stats when no sessions exist`() = runTest {
        every { repository.getSessionsByDateRange(any(), any()) } returns flowOf(emptyList())

        val result = useCase().first()

        assertEquals(0L, result.totalDurationMillis)
        assertEquals(0.0, result.totalEarningsSek, 0.001)
        assertEquals(0, result.sessionCount)
    }

    @Test
    fun `sums earnings from multiple sessions correctly`() = runTest {
        val sessions = listOf(
            makeSession(earningsSek = 450.0, durationMillis = 3_600_000L),
            makeSession(earningsSek = 320.0, durationMillis = 1_800_000L),
            makeSession(earningsSek = 180.0, durationMillis = 900_000L)
        )
        every { repository.getSessionsByDateRange(any(), any()) } returns flowOf(sessions)

        val result = useCase().first()

        assertEquals(950.0, result.totalEarningsSek, 0.001)
        assertEquals(3, result.sessionCount)
    }

    @Test
    fun `sums duration from multiple sessions correctly`() = runTest {
        val sessions = listOf(
            makeSession(durationMillis = 3_600_000L),   // 1 hour
            makeSession(durationMillis = 7_200_000L),   // 2 hours
            makeSession(durationMillis = 1_800_000L)    // 0.5 hours
        )
        every { repository.getSessionsByDateRange(any(), any()) } returns flowOf(sessions)

        val result = useCase().first()

        assertEquals(12_600_000L, result.totalDurationMillis)
        assertEquals(3.5, result.totalHours, 0.001)
    }

    @Test
    fun `calculates average hourly rate correctly`() = runTest {
        val sessions = listOf(
            makeSession(earningsSek = 360.0, durationMillis = 3_600_000L)  // 360 kr in 1h = 360 kr/h
        )
        every { repository.getSessionsByDateRange(any(), any()) } returns flowOf(sessions)

        val result = useCase().first()

        assertEquals(360.0, result.averageHourlyRate, 0.1)
    }

    @Test
    fun `returns zero hourly rate when no earnings logged`() = runTest {
        val sessions = listOf(makeSession(earningsSek = 0.0, durationMillis = 3_600_000L))
        every { repository.getSessionsByDateRange(any(), any()) } returns flowOf(sessions)

        val result = useCase().first()

        assertEquals(0.0, result.averageHourlyRate, 0.001)
    }

    private fun makeSession(
        earningsSek: Double = 0.0,
        durationMillis: Long = 0L,
        distanceKm: Double = 0.0
    ) = DrivingSession(
        startTime = System.currentTimeMillis(),
        endTime = System.currentTimeMillis() + durationMillis,
        durationMillis = durationMillis,
        earningsSek = earningsSek,
        distanceKm = distanceKm,
        platform = Platform.OTHER,
        date = System.currentTimeMillis()
    )
}
