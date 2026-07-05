package se.w3footprint.korlog.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import se.w3footprint.korlog.data.local.dao.SessionDao
import se.w3footprint.korlog.data.local.entity.toDomain
import se.w3footprint.korlog.data.local.entity.toEntity
import se.w3footprint.korlog.domain.model.DrivingSession
import se.w3footprint.korlog.domain.repository.SessionRepository
import javax.inject.Inject

class SessionRepositoryImpl @Inject constructor(
    private val sessionDao: SessionDao
) : SessionRepository {

    override fun getAllSessions(): Flow<List<DrivingSession>> =
        sessionDao.getAllSessions().map { list -> list.map { it.toDomain() } }

    override fun getSessionsByDateRange(
        startMillis: Long,
        endMillis: Long
    ): Flow<List<DrivingSession>> =
        sessionDao.getSessionsByDateRange(startMillis, endMillis)
            .map { list -> list.map { it.toDomain() } }

    override suspend fun insertSession(session: DrivingSession): Long =
        sessionDao.insertSession(session.toEntity())

    override suspend fun updateSession(session: DrivingSession) =
        sessionDao.updateSession(session.toEntity())

    override suspend fun deleteSession(session: DrivingSession) =
        sessionDao.deleteSession(session.toEntity())

    override suspend fun getSessionById(id: Long): DrivingSession? =
        sessionDao.getSessionById(id)?.toDomain()
}
