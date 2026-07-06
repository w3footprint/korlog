package se.w3footprint.korlog.presentation.session

import se.w3footprint.korlog.domain.model.Platform

data class ActiveSessionUiState(
    val isRunning: Boolean = false,
    val isOnBreak: Boolean = false,
    val startTime: Long = 0L,
    val elapsedMillis: Long = 0L,
    val totalBreakMillis: Long = 0L,
    val currentBreakStartMillis: Long = 0L,
    val earningsInput: String = "",
    val distanceInput: String = "",
    val selectedPlatform: Platform = Platform.OTHER,
    val notes: String = "",
    val isSaving: Boolean = false,
    val savedSessionId: Long? = null,
    val showStopConfirm: Boolean = false,
    val stoppedAtMillis: Long = 0L
) {
    val earningsSek: Double get() = earningsInput.replace(",", ".").toDoubleOrNull() ?: 0.0
    val distanceKm: Double get() = distanceInput.replace(",", ".").toDoubleOrNull() ?: 0.0

    val drivingMillis: Long get() = elapsedMillis - totalBreakMillis

    val drivingHours: Int get() = (drivingMillis / 3_600_000).toInt()
    val drivingMinutes: Int get() = ((drivingMillis % 3_600_000) / 60_000).toInt()
    val drivingSeconds: Int get() = ((drivingMillis % 60_000) / 1_000).toInt()

    val formattedDrivingTime: String get() =
        "%02d:%02d:%02d".format(drivingHours, drivingMinutes, drivingSeconds)

    val breakHours: Int get() = (totalBreakMillis / 3_600_000).toInt()
    val breakMinutes: Int get() = ((totalBreakMillis % 3_600_000) / 60_000).toInt()
    val formattedBreakTime: String get() = "%02d:%02d".format(breakHours, breakMinutes)

    val hourlyRate: Double get() {
        val hours = drivingMillis / 3_600_000.0
        return if (hours > 0 && earningsSek > 0) earningsSek / hours else 0.0
    }
}
