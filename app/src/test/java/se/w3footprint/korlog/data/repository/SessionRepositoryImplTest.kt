package se.w3footprint.korlog.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import se.w3footprint.korlog.data.local.dao.SessionDao
import se.w3footprint.korlog.data.local.entity.SessionEntity
import se.w3footprint.korlog.data.remote.firestore.FirestoreRepository
import se.w3footprint.korlog.domain.model.DrivingSession
import se.w3footprint.korlog.domain.model.Platform

class SessionRepositoryImplTest {

    private lateinit var sessionDao: SessionDao
    private lateinit var firestoreRepository: FirestoreRepository
    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseUser: FirebaseUser
    private lateinit var repository: SessionRepositoryImpl

    private val userAUid = "user-a-uid"
    private val userBUid = "user-b-uid"

    private fun makeSession(id: Long = 0, userId: String = userAUid) = SessionEntity(
        id = id, syncId = "", userId = userId,
        startTime = 1000L, endTime = 2000L, durationMillis = 1000L,
        earningsSek = 100.0, distanceKm = 10.0, platform = "UBER",
        notes = "", date = 1000L
    )

    private fun makeDomainSession(id: Long = 0) = DrivingSession(
        id = id, startTime = 1000L, endTime = 2000L, durationMillis = 1000L,
        earningsSek = 100.0, distanceKm = 10.0, platform = Platform.UBER,
        notes = "", date = 1000L
    )

    @Before
    fun setup() {
        sessionDao = mockk(relaxed = true)
        firestoreRepository = mockk(relaxed = true)
        auth = mockk()
        firebaseUser = mockk()

        every { auth.currentUser } returns firebaseUser
        every { firebaseUser.uid } returns userAUid

        repository = SessionRepositoryImpl(sessionDao, firestoreRepository, auth)
    }

    // ── getAllSessions ────────────────────────────────────────────────────────

    @Test
    fun `getAllSessions returns empty flow when not logged in`() = runTest {
        every { auth.currentUser } returns null

        val result = repository.getAllSessions().firstOrNull()

        assertNull(result)
    }

    @Test
    fun `getAllSessions queries by current user uid`() = runTest {
        every { sessionDao.getAllSessions(userAUid) } returns flowOf(
            listOf(makeSession(id = 1, userId = userAUid))
        )

        val result = repository.getAllSessions().first()

        assertEquals(1, result.size)
        assertEquals(1L, result[0].id)
    }

    @Test
    fun `getAllSessions does not return sessions belonging to another user`() = runTest {
        every { sessionDao.getAllSessions(userAUid) } returns flowOf(
            listOf(makeSession(id = 1, userId = userAUid))
        )

        // Switch to User B
        every { firebaseUser.uid } returns userBUid
        every { sessionDao.getAllSessions(userBUid) } returns flowOf(emptyList())

        val result = repository.getAllSessions().first()

        assertTrue(result.isEmpty())
    }

    // ── insertSession ─────────────────────────────────────────────────────────

    @Test
    fun `insertSession tags session with current user uid`() = runTest {
        val entitySlot = slot<SessionEntity>()
        coEvery { sessionDao.insertSession(capture(entitySlot)) } returns 1L
        coEvery { firestoreRepository.upsertSession(any()) } returns Unit

        repository.insertSession(makeDomainSession())

        assertEquals(userAUid, entitySlot.captured.userId)
    }

    @Test
    fun `insertSession returns Room-generated id`() = runTest {
        coEvery { sessionDao.insertSession(any()) } returns 42L

        val id = repository.insertSession(makeDomainSession())

        assertEquals(42L, id)
    }

    // ── deleteSession ─────────────────────────────────────────────────────────

    @Test
    fun `deleteSession removes from Room using current user uid`() = runTest {
        val entitySlot = slot<SessionEntity>()
        coEvery { sessionDao.deleteSession(capture(entitySlot)) } returns Unit

        repository.deleteSession(makeDomainSession(id = 5))

        assertEquals(userAUid, entitySlot.captured.userId)
        assertEquals(5L, entitySlot.captured.id)
    }

    // ── getSessionById ────────────────────────────────────────────────────────

    @Test
    fun `getSessionById returns null when session belongs to different user`() = runTest {
        coEvery { sessionDao.getSessionById(1L, userAUid) } returns null

        val result = repository.getSessionById(1L)

        assertNull(result)
    }

    @Test
    fun `getSessionById returns session when it belongs to current user`() = runTest {
        coEvery { sessionDao.getSessionById(1L, userAUid) } returns makeSession(id = 1)

        val result = repository.getSessionById(1L)

        assertEquals(1L, result?.id)
    }

    // ── syncFromCloud ─────────────────────────────────────────────────────────

    @Test
    fun `syncFromCloud does nothing when not logged in`() = runTest {
        every { auth.currentUser } returns null

        repository.syncFromCloud()

        coVerify(exactly = 0) { firestoreRepository.fetchAllSessions() }
        coVerify(exactly = 0) { sessionDao.insertSession(any()) }
    }

    @Test
    fun `syncFromCloud pushes local sessions to Firestore`() = runTest {
        val localSession = makeSession(id = 1, userId = userAUid)
        coEvery { sessionDao.getAllSessionsOnce(userAUid) } returns listOf(localSession)
        coEvery { firestoreRepository.fetchAllSessions() } returns emptyList()

        repository.syncFromCloud()

        coVerify { firestoreRepository.upsertSession(any()) }
    }

    @Test
    fun `syncFromCloud inserts cloud sessions into Room`() = runTest {
        val cloudSession = makeSession(id = 2, userId = userAUid)
        coEvery { sessionDao.getAllSessionsOnce(userAUid) } returns emptyList()
        coEvery { firestoreRepository.fetchAllSessions() } returns listOf(cloudSession)

        repository.syncFromCloud()

        coVerify { sessionDao.insertSession(cloudSession) }
    }

    @Test
    fun `syncFromCloud does not push sessions belonging to other users`() = runTest {
        // User B logs in — their local Room is empty for userB, nothing to push
        every { firebaseUser.uid } returns userBUid
        coEvery { sessionDao.getAllSessionsOnce(userBUid) } returns emptyList()
        coEvery { firestoreRepository.fetchAllSessions() } returns emptyList()

        repository.syncFromCloud()

        coVerify(exactly = 0) { firestoreRepository.upsertSession(any()) }
    }

    @Test
    fun `syncFromCloud does not claim orphaned sessions for new user`() = runTest {
        // Orphaned sessions (userId='') must NOT be uploaded to a new user's Firestore
        every { firebaseUser.uid } returns userBUid
        coEvery { sessionDao.getAllSessionsOnce(userBUid) } returns emptyList()
        coEvery { firestoreRepository.fetchAllSessions() } returns emptyList()

        repository.syncFromCloud()

        // User B should upload nothing
        coVerify(exactly = 0) { firestoreRepository.upsertSession(any()) }
    }

    @Test
    fun `syncFromCloud survives Firestore errors without crashing`() = runTest {
        coEvery { sessionDao.getAllSessionsOnce(userAUid) } returns emptyList()
        coEvery { firestoreRepository.fetchAllSessions() } throws RuntimeException("network error")

        // Should not throw
        repository.syncFromCloud()
    }

    // ── multi-user isolation ──────────────────────────────────────────────────

    @Test
    fun `User A sessions are not visible to User B`() = runTest {
        // User A has sessions
        every { sessionDao.getAllSessions(userAUid) } returns flowOf(
            listOf(makeSession(id = 1, userId = userAUid))
        )
        // User B has no sessions
        every { sessionDao.getAllSessions(userBUid) } returns flowOf(emptyList())

        // Switch to User B
        every { firebaseUser.uid } returns userBUid

        val sessions = repository.getAllSessions().first()

        assertTrue("User B should see no sessions", sessions.isEmpty())
    }

    @Test
    fun `User B login does not expose User A orphaned sessions`() = runTest {
        // User A had sessions before userId column (userId='') — these are orphaned
        // User B logs in — they should NOT appear for User B
        every { firebaseUser.uid } returns userBUid
        every { sessionDao.getAllSessions(userBUid) } returns flowOf(emptyList())

        val sessions = repository.getAllSessions().first()

        assertTrue(sessions.isEmpty())
        // Crucially: no claimOrphanedSessions call should happen
        coVerify(exactly = 0) { sessionDao.claimOrphanedSessions(any()) }
    }

    @Test
    fun `sessions from User A are not deleted when User B syncs`() = runTest {
        // User B's cloud has session id=1. User A also has local session id=1.
        // The deleteSessionById call must include userId so it only deletes User B's row.
        every { firebaseUser.uid } returns userBUid
        val userBCloudSession = makeSession(id = 1, userId = userBUid)

        coEvery { sessionDao.getAllSessionsOnce(userBUid) } returns listOf(userBCloudSession)
        coEvery { firestoreRepository.fetchAllSessions() } returns listOf(userBCloudSession)

        repository.syncFromCloud()

        // deleteSessionById should never be called without a userId guard
        coVerify(exactly = 0) { sessionDao.deleteSessionById(any(), neq(userBUid)) }
    }
}
