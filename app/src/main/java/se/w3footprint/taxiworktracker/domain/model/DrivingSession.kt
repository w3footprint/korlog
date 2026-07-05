package se.w3footprint.taxiworktracker.domain.model

data class DrivingSession(
    val id: Long = 0,
    val startTime: Long,
    val endTime: Long,
    val durationMillis: Long,
    val earningsSek: Double,
    val distanceKm: Double = 0.0,
    val platform: Platform = Platform.OTHER,
    val notes: String = "",
    val date: Long
)

enum class Platform {
    UBER, BOLT, CABONLINE, TAXIKURIR, SVERIGETAXI, OTHER
}
