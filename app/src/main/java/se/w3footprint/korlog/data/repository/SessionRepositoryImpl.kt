package se.w3footprint.korlog.data.repository

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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

    private val syncScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

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
        syncScope.launch { firestoreRepository.upsertSession(session.copy(id = id)) }
        return id
    }

    override suspend fun updateSession(session: DrivingSession) {
        sessionDao.updateSession(session.toEntity(uid))
        syncScope.launch { firestoreRepository.upsertSession(session) }
    }

    override suspend fun deleteSession(session: DrivingSession) {
        sessionDao.deleteSession(session.toEntity(uid))
        syncScope.launch { firestoreRepository.deleteSession(session.id) }
    }

    override suspend fun getSessionById(id: Long): DrivingSession? =
        sessionDao.getSessionById(id, uid)?.toDomain()

    override suspend fun syncFromCloud() {
        val currentUid = uid
        if (currentUid.isEmpty()) return
        sessionDao.claimOrphanedSessions(currentUid)
        try {
            val local = sessionDao.getAllSessionsOnce(currentUid)
            local.forEach { firestoreRepository.upsertSession(it.toDomain()) }
            val cloud = firestoreRepository.fetchAllSessions()
            cloud.forEach { sessionDao.insertSession(it) }
        } catch (_: Exception) {}
    }
}
