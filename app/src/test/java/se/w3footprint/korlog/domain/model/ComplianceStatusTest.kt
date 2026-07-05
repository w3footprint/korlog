package se.w3footprint.korlog.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ComplianceStatusTest {

    private fun makeStatus(
        weeklyHours: Double,
        dailyHours: Double = 0.0,
        dailyRestHours: Double = 12.0
    ) = ComplianceStatus(
        weeklyHours = weeklyHours,
        monthlyHours = weeklyHours,
        dailyHours = dailyHours,
        dailyRestHours = dailyRestHours
    )

    // Weekly hard limit (60h)

    @Test
    fun `hard limit not exceeded below 60 hours`() {
        assertFalse(makeStatus(59.9).isWeeklyHardLimitExceeded)
    }

    @Test
    fun `hard limit exceeded at exactly 60 hours`() {
        assertTrue(makeStatus(60.0).isWeeklyHardLimitExceeded)
    }

    @Test
    fun `hard limit exceeded above 60 hours`() {
        assertTrue(makeStatus(61.0).isWeeklyHardLimitExceeded)
    }

    // Weekly average limit (48h)

    @Test
    fun `average limit not exceeded below 48 hours`() {
        assertFalse(makeStatus(47.9).isWeeklyAverageLimitExceeded)
    }

    @Test
    fun `average limit exceeded at exactly 48 hours`() {
        assertTrue(makeStatus(48.0).isWeeklyAverageLimitExceeded)
    }

    // Break recommendation (daily 6h threshold)

    @Test
    fun `break not recommended below 6 daily hours`() {
        assertFalse(makeStatus(0.0, dailyHours = 5.9).isRestRecommended)
    }

    @Test
    fun `break recommended at exactly 6 daily hours`() {
        assertTrue(makeStatus(0.0, dailyHours = 6.0).isRestRecommended)
    }

    // Progress clamping

    @Test
    fun `weekly progress is zero when no hours logged`() {
        assertEquals(0.0f, makeStatus(0.0).weeklyProgress, 0.001f)
    }

    @Test
    fun `weekly progress is 0_5 at 30 hours`() {
        assertEquals(0.5f, makeStatus(30.0).weeklyProgress, 0.001f)
    }

    @Test
    fun `weekly progress is clamped to 1_0 above 60 hours`() {
        assertEquals(1.0f, makeStatus(75.0).weeklyProgress, 0.001f)
    }

    @Test
    fun `weekly progress is 1_0 at exactly 60 hours`() {
        assertEquals(1.0f, makeStatus(60.0).weeklyProgress, 0.001f)
    }
}
