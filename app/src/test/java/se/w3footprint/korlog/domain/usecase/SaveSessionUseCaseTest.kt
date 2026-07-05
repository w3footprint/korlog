package se.w3footprint.korlog.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import se.w3footprint.korlog.domain.model.DrivingSession
import se.w3footprint.korlog.domain.model.Platform
import se.w3footprint.korlog.domain.repository.SessionRepository
import se.w3footprint.korlog.domain.usecase.session.SaveSessionUseCase

class SaveSessionUseCaseTest {

    private val repository: SessionRepository = mockk()
    private val useCase = SaveSessionUseCase(repository)

    @Test
    fun `inserts session with correct duration computed from start and end time`() = runTest {
        val start = 0L
        val end = 3_600_000L  // exactly 1 hour
        val slot = slot<DrivingSession>()
        coEvery { repository.insertSession(capture(slot)) } returns 1L

        useCase(
            startTime = start,
            endTime = end,
            breakDurationMillis = 0L,
            earningsSek = 400.0,
            distanceKm = 50.0,
            platform = Platform.UBER,
            notes = ""
        )

        assertEquals(3_600_000L, slot.captured.durationMillis)
    }

    @Test
    fun `inserts session with correct earnings and platform`() = runTest {
        val slot = slot<DrivingSession>()
        coEvery { repository.insertSession(capture(slot)) } returns 2L

        useCase(
            startTime = 0L,
            endTime = 3_600_000L,
            earningsSek = 575.50,
            distanceKm = 120.0,
            platform = Platform.BOLT,
            notes = "Good evening"
        )

        assertEquals(575.50, slot.captured.earningsSek, 0.001)
        assertEquals(Platform.BOLT, slot.captured.platform)
        assertEquals("Good evening", slot.captured.notes)
        assertEquals(120.0, slot.captured.distanceKm, 0.001)
    }

    @Test
    fun `returns the inserted session id from repository`() = runTest {
        coEvery { repository.insertSession(any()) } returns 42L

        val id = useCase(
            startTime = 0L, endTime = 1_800_000L,
            earningsSek = 200.0, distanceKm = 0.0,
            platform = Platform.OTHER, notes = ""
        )

        assertEquals(42L, id)
    }

    @Test
    fun `calls repository insertSession exactly once`() = runTest {
        coEvery { repository.insertSession(any()) } returns 1L

        useCase(
            startTime = 0L, endTime = 3_600_000L, breakDurationMillis = 0L,
            earningsSek = 300.0, distanceKm = 0.0, platform = Platform.CABONLINE, notes = ""
        )

        coVerify(exactly = 1) { repository.insertSession(any()) }
    }
}
