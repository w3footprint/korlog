package se.w3footprint.korlog.data.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import se.w3footprint.korlog.data.local.dao.SessionDao
import se.w3footprint.korlog.data.local.entity.toDomain
import se.w3footprint.korlog.data.local.entity.toEntity
import se.w3footprint.korlog.data.remote.firestore.FirestoreRepository
import se.w3footprint.korlog.domain.model.DrivingSession
import se.w3footprint.korlog.domain.repository.SessionRepository
import javax.inject.Inject

class SessionRepositoryImpl @Inject constructor(
    private val sessionDao: SessionDao,
    private val firestoreRepository: FirestoreRepository
) : SessionRepository {

    private val syncScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun getAllSessions(): Flow<List<DrivingSession>> =
        sessionDao.getAllSessions().map { list -> list.map { it.toDomain() } }

    override fun getSessionsByDateRange(
        startMillis: Long,
        endMillis: Long
    ): Flow<List<DrivingSession>> =
        sessionDao.getSessionsByDateRange(startMillis, endMillis)
            .map { list -> list.map { it.toDomain() } }

    override suspend fun insertSession(session: DrivingSession): Long {
        val id = sessionDao.insertSession(session.toEntity())
        syncScope.launch { firestoreRepository.upsertSession(session.copy(id = id)) }
        return id
    }

    override suspend fun updateSession(session: DrivingSession) {
        sessionDao.updateSession(session.toEntity())
        syncScope.launch { firestoreRepository.upsertSession(session) }
    }

    override suspend fun deleteSession(session: DrivingSession) {
        sessionDao.deleteSession(session.toEntity())
        syncScope.launch { firestoreRepository.deleteSession(session.id) }
    }

    override suspend fun getSessionById(id: Long): DrivingSession? =
        sessionDao.getSessionById(id)?.toDomain()

    override suspend fun syncFromCloud() {
        val sessions = firestoreRepository.fetchAllSessions()
        sessions.forEach { sessionDao.insertSession(it) }
    }
}
