package se.w3footprint.korlog.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import se.w3footprint.korlog.domain.model.DrivingSession
import se.w3footprint.korlog.domain.model.Platform

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startTime: Long,
    val endTime: Long,
    val durationMillis: Long,
    val breakDurationMillis: Long = 0L,
    val earningsSek: Double,
    val distanceKm: Double,
    val platform: String,
    val notes: String,
    val date: Long
)

fun SessionEntity.toDomain() = DrivingSession(
    id = id,
    startTime = startTime,
    endTime = endTime,
    durationMillis = durationMillis,
    breakDurationMillis = breakDurationMillis,
    earningsSek = earningsSek,
    distanceKm = distanceKm,
    platform = Platform.entries.find { it.name == platform } ?: Platform.OTHER,
    notes = notes,
    date = date
)

fun DrivingSession.toEntity() = SessionEntity(
    id = id,
    startTime = startTime,
    endTime = endTime,
    durationMillis = durationMillis,
    breakDurationMillis = breakDurationMillis,
    earningsSek = earningsSek,
    distanceKm = distanceKm,
    platform = platform.name,
    notes = notes,
    date = date
)
