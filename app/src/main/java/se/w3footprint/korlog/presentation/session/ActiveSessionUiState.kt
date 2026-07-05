package se.w3footprint.korlog.presentation.session

import se.w3footprint.korlog.domain.model.Platform

data class ActiveSessionUiState(
    val isRunning: Boolean = false,
    val startTime: Long = 0L,
    val elapsedMillis: Long = 0L,
    val earningsInput: String = "",
    val distanceInput: String = "",
    val selectedPlatform: Platform = Platform.OTHER,
    val notes: String = "",
    val isSaving: Boolean = false,
    val savedSessionId: Long? = null,
    val showStopConfirm: Boolean = false
) {
    val earningsSek: Double get() = earningsInput.replace(",", ".").toDoubleOrNull() ?: 0.0
    val distanceKm: Double get() = distanceInput.replace(",", ".").toDoubleOrNull() ?: 0.0

    val elapsedHours: Int get() = (elapsedMillis / 3_600_000).toInt()
    val elapsedMinutes: Int get() = ((elapsedMillis % 3_600_000) / 60_000).toInt()
    val elapsedSeconds: Int get() = ((elapsedMillis % 60_000) / 1_000).toInt()

    val formattedTime: String get() = "%02d:%02d:%02d".format(elapsedHours, elapsedMinutes, elapsedSeconds)

    val hourlyRate: Double get() {
        val hours = elapsedMillis / 3_600_000.0
        return if (hours > 0 && earningsSek > 0) earningsSek / hours else 0.0
    }
}
