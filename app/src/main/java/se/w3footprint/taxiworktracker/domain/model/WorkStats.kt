package se.w3footprint.taxiworktracker.domain.model

data class WorkStats(
    val totalDurationMillis: Long,
    val totalEarningsSek: Double,
    val totalDistanceKm: Double,
    val sessionCount: Int
) {
    val totalHours: Double get() = totalDurationMillis / 1000.0 / 3600.0
    val averageHourlyRate: Double get() = if (totalHours > 0) totalEarningsSek / totalHours else 0.0
}

data class ComplianceStatus(
    val weeklyHours: Double,
    val weeklyHardLimitHours: Double = 60.0,
    val weeklyAverageLimitHours: Double = 48.0,
    val monthlyHours: Double,
    val monthlyRecommendedLimitHours: Double = 192.0,
    val dailyHours: Double,
    val dailyRestHours: Double
) {
    val isWeeklyHardLimitExceeded: Boolean get() = weeklyHours >= weeklyHardLimitHours
    val isWeeklyAverageLimitExceeded: Boolean get() = weeklyHours >= weeklyAverageLimitHours
    val isRestRecommended: Boolean get() = dailyHours >= 6.0
    val weeklyProgress: Float get() = (weeklyHours / weeklyHardLimitHours).coerceIn(0.0, 1.0).toFloat()
}
