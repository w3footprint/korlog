package se.w3footprint.korlog.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class WorkStatsTest {

    @Test
    fun `totalHours converts milliseconds correctly`() {
        val stats = WorkStats(
            totalDurationMillis = 3_600_000L,  // exactly 1 hour
            totalEarningsSek = 0.0,
            totalDistanceKm = 0.0,
            sessionCount = 1
        )
        assertEquals(1.0, stats.totalHours, 0.001)
    }

    @Test
    fun `totalHours handles fractional hours`() {
        val stats = WorkStats(
            totalDurationMillis = 5_400_000L,  // 1.5 hours
            totalEarningsSek = 0.0,
            totalDistanceKm = 0.0,
            sessionCount = 1
        )
        assertEquals(1.5, stats.totalHours, 0.001)
    }

    @Test
    fun `averageHourlyRate is correct`() {
        val stats = WorkStats(
            totalDurationMillis = 7_200_000L,  // 2 hours
            totalEarningsSek = 800.0,
            totalDistanceKm = 0.0,
            sessionCount = 2
        )
        assertEquals(400.0, stats.averageHourlyRate, 0.001)
    }

    @Test
    fun `averageHourlyRate is zero when no duration`() {
        val stats = WorkStats(
            totalDurationMillis = 0L,
            totalEarningsSek = 500.0,
            totalDistanceKm = 0.0,
            sessionCount = 0
        )
        assertEquals(0.0, stats.averageHourlyRate, 0.001)
    }

    @Test
    fun `totalHours is zero for empty stats`() {
        val stats = WorkStats(0L, 0.0, 0.0, 0)
        assertEquals(0.0, stats.totalHours, 0.001)
    }
}
