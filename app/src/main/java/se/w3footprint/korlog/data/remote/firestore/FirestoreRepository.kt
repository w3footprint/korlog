package se.w3footprint.korlog.data.remote.firestore

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
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
        val data = mapOf(
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
        sessionsCollection(uid).document(session.id.toString()).set(data).await()
    }

    suspend fun deleteSession(sessionId: Long) {
        val uid = auth.currentUser?.uid ?: return
        sessionsCollection(uid).document(sessionId.toString()).delete().await()
    }

    suspend fun fetchAllSessions(): List<se.w3footprint.korlog.data.local.entity.SessionEntity> {
        val uid = auth.currentUser?.uid ?: return emptyList()
        val snapshot = sessionsCollection(uid).get().await()
        return snapshot.documents.mapNotNull { doc ->
            try {
                se.w3footprint.korlog.data.local.entity.SessionEntity(
                    id = doc.id.toLong(),
                    startTime = doc.getLong("startTime") ?: 0L,
                    endTime = doc.getLong("endTime") ?: 0L,
                    durationMillis = doc.getLong("durationMillis") ?: 0L,
                    breakDurationMillis = doc.getLong("breakDurationMillis") ?: 0L,
                    earningsSek = doc.getDouble("earningsSek") ?: 0.0,
                    distanceKm = doc.getDouble("distanceKm") ?: 0.0,
                    platform = doc.getString("platform") ?: "OTHER",
                    notes = doc.getString("notes") ?: "",
                    date = doc.getLong("date") ?: 0L
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}
