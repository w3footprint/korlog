package se.w3footprint.korlog.data.remote.firestore

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import se.w3footprint.korlog.data.local.entity.SessionEntity
import se.w3footprint.korlog.domain.model.DrivingSession
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private fun sessionsCollection(uid: String) =
        firestore.collection("users").document(uid).collection("sessions")

    suspend fun upsertSession(session: DrivingSession) {
        val uid = auth.currentUser?.uid ?: return
        if (session.syncId.isEmpty()) return
        val data = mapOf(
            "syncId" to session.syncId,
            "startTime" to session.startTime,
            "endTime" to session.endTime,
            "durationMillis" to session.durationMillis,
            "breakDurationMillis" to session.breakDurationMillis,
            "earningsSek" to session.earningsSek,
            "distanceKm" to session.distanceKm,
            "platform" to session.platform.name,
            "notes" to session.notes,
            "date" to session.date
        )
        sessionsCollection(uid).document(session.syncId).set(data).await()
    }

    suspend fun deleteSession(syncId: String) {
        val uid = auth.currentUser?.uid ?: return
        if (syncId.isEmpty()) return
        sessionsCollection(uid).document(syncId).delete().await()
    }

    suspend fun fetchAllSessions(): List<SessionEntity> {
        val uid = auth.currentUser?.uid ?: return emptyList()
        val snapshot = sessionsCollection(uid).get().await()
        return snapshot.documents.mapNotNull { doc -> doc.toEntity(uid) }
    }

    fun observeSessions(): Flow<List<SessionEntity>> = callbackFlow {
        val uid = auth.currentUser?.uid ?: run { close(); return@callbackFlow }
        val listener = sessionsCollection(uid).addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) return@addSnapshotListener
            trySend(snapshot.documents.mapNotNull { doc -> doc.toEntity(uid) })
        }
        awaitClose { listener.remove() }
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toEntity(uid: String): SessionEntity? {
        return try {
            SessionEntity(
                syncId = id,
                userId = uid,
                startTime = getLong("startTime") ?: 0L,
                endTime = getLong("endTime") ?: 0L,
                durationMillis = getLong("durationMillis") ?: 0L,
                breakDurationMillis = getLong("breakDurationMillis") ?: 0L,
                earningsSek = getDouble("earningsSek") ?: 0.0,
                distanceKm = getDouble("distanceKm") ?: 0.0,
                platform = getString("platform") ?: "OTHER",
                notes = getString("notes") ?: "",
                date = getLong("date") ?: 0L
            )
        } catch (e: Exception) {
            null
        }
    }
}
