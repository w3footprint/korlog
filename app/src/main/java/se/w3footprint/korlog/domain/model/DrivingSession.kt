package se.w3footprint.korlog.domain.model

data class DrivingSession(
    val id: Long = 0,
    val syncId: String = "",
    val startTime: Long,
    val endTime: Long,
    val durationMillis: Long,
    val breakDurationMillis: Long = 0L,
    val earningsSek: Double,
    val distanceKm: Double = 0.0,
    val platform: Platform = Platform.OTHER,
    val notes: String = "",
    val date: Long
) {
    val drivingDurationMillis: Long get() = durationMillis - breakDurationMillis
}

enum class Platform {
    UBER, BOLT, CABONLINE, TAXIKURIR, SVERIGETAXI, OTHER
}
