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
}
