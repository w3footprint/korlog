package se.w3footprint.korlog.presentation.session

import org.junit.Assert.assertEquals
import org.junit.Test

class ActiveSessionUiStateTest {

    @Test
    fun `formattedTime shows zeroes when no time elapsed`() {
        val state = ActiveSessionUiState(elapsedMillis = 0L)
        assertEquals("00:00:00", state.formattedTime)
    }

    @Test
    fun `formattedTime formats one hour correctly`() {
        val state = ActiveSessionUiState(elapsedMillis = 3_600_000L)
        assertEquals("01:00:00", state.formattedTime)
    }

    @Test
    fun `formattedTime formats mixed hours minutes seconds`() {
        val state = ActiveSessionUiState(elapsedMillis = 3_723_000L) // 1h 2m 3s
        assertEquals("01:02:03", state.formattedTime)
    }

    @Test
    fun `earningsSek parses dot decimal correctly`() {
        val state = ActiveSessionUiState(earningsInput = "450.50")
        assertEquals(450.50, state.earningsSek, 0.001)
    }

    @Test
    fun `earningsSek parses comma decimal correctly`() {
        val state = ActiveSessionUiState(earningsInput = "450,50")
        assertEquals(450.50, state.earningsSek, 0.001)
    }

    @Test
    fun `earningsSek returns zero for empty input`() {
        val state = ActiveSessionUiState(earningsInput = "")
        assertEquals(0.0, state.earningsSek, 0.001)
    }

    @Test
    fun `hourlyRate is zero when no earnings entered`() {
        val state = ActiveSessionUiState(elapsedMillis = 3_600_000L, earningsInput = "")
        assertEquals(0.0, state.hourlyRate, 0.001)
    }

    @Test
    fun `hourlyRate calculates correctly with earnings and time`() {
        val state = ActiveSessionUiState(
            elapsedMillis = 3_600_000L,  // 1 hour
            earningsInput = "360"
        )
        assertEquals(360.0, state.hourlyRate, 0.1)
    }

    @Test
    fun `hourlyRate is zero when elapsed time is zero`() {
        val state = ActiveSessionUiState(elapsedMillis = 0L, earningsInput = "200")
        assertEquals(0.0, state.hourlyRate, 0.001)
    }
}
