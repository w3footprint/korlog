package se.w3footprint.korlog.data.repository

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
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
    private val firestoreRepository: FirestoreRepository,
    private val auth: FirebaseAuth
) : SessionRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var realtimeSyncJob: Job? = null

    private val uid get() = auth.currentUser?.uid ?: ""

    override fun getAllSessions(): Flow<List<DrivingSession>> {
        val currentUid = uid
        if (currentUid.isEmpty()) return emptyFlow()
        return sessionDao.getAllSessions(currentUid).map { list -> list.map { it.toDomain() } }
    }

    override fun getSessionsByDateRange(startMillis: Long, endMillis: Long): Flow<List<DrivingSession>> {
        val currentUid = uid
        if (currentUid.isEmpty()) return emptyFlow()
        return sessionDao.getSessionsByDateRange(currentUid, startMillis, endMillis)
            .map { list -> list.map { it.toDomain() } }
    }

    override suspend fun insertSession(session: DrivingSession): Long {
        val currentUid = uid
        val id = sessionDao.insertSession(session.toEntity(currentUid))
        scope.launch { firestoreRepository.upsertSession(session.copy(id = id)) }
        return id
    }

    override suspend fun updateSession(session: DrivingSession) {
        sessionDao.updateSession(session.toEntity(uid))
        scope.launch { firestoreRepository.upsertSession(session) }
    }

    override suspend fun deleteSession(session: DrivingSession) {
        sessionDao.deleteSession(session.toEntity(uid))
        scope.launch { firestoreRepository.deleteSession(session.id) }
    }

    override suspend fun getSessionById(id: Long): DrivingSession? =
        sessionDao.getSessionById(id, uid)?.toDomain()

    override suspend fun syncFromCloud() {
        val currentUid = uid
        if (currentUid.isEmpty()) return
        try {
            val local = sessionDao.getAllSessionsOnce(currentUid)
            local.forEach { firestoreRepository.upsertSession(it.toDomain()) }
            val cloud = firestoreRepository.fetchAllSessions()
            cloud.forEach { sessionDao.insertSessionFromCloud(it) }
        } catch (_: Exception) {}
        startRealtimeSync()
    }

    override fun stopRealtimeSync() {
        realtimeSyncJob?.cancel()
        realtimeSyncJob = null
    }

    private fun startRealtimeSync() {
        realtimeSyncJob?.cancel()
        realtimeSyncJob = scope.launch {
            firestoreRepository.observeSessions().collect { sessions ->
                sessions.forEach { sessionDao.insertSessionFromCloud(it) }
                val cloudIds = sessions.map { it.id }.toSet()
                val localIds = sessionDao.getAllSessionsOnce(uid).map { it.id }.toSet()
                val deletedIds = localIds - cloudIds
                deletedIds.forEach { sessionDao.deleteSessionById(it, uid) }
            }
        }
    }
}
