package se.w3footprint.taxiworktracker.domain.repository

import kotlinx.coroutines.flow.Flow
import se.w3footprint.taxiworktracker.domain.model.DrivingSession

interface SessionRepository {
    fun getAllSessions(): Flow<List<DrivingSession>>
    fun getSessionsByDateRange(startMillis: Long, endMillis: Long): Flow<List<DrivingSession>>
    suspend fun insertSession(session: DrivingSession): Long
    suspend fun updateSession(session: DrivingSession)
    suspend fun deleteSession(session: DrivingSession)
    suspend fun getSessionById(id: Long): DrivingSession?
}
