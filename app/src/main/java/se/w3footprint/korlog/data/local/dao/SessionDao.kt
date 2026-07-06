package se.w3footprint.korlog.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import se.w3footprint.korlog.data.local.entity.SessionEntity

@Dao
interface SessionDao {

    @Query("SELECT * FROM sessions WHERE userId = :userId ORDER BY date DESC")
    fun getAllSessions(userId: String): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions WHERE userId = :userId AND date BETWEEN :startMillis AND :endMillis ORDER BY date DESC")
    fun getSessionsByDateRange(userId: String, startMillis: Long, endMillis: Long): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions WHERE id = :id AND userId = :userId")
    suspend fun getSessionById(id: Long, userId: String): SessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SessionEntity): Long

    @Update
    suspend fun updateSession(session: SessionEntity)

    @Delete
    suspend fun deleteSession(session: SessionEntity)

    @Query("UPDATE sessions SET userId = :userId WHERE userId = ''")
    suspend fun claimOrphanedSessions(userId: String)

    @Query("SELECT * FROM sessions WHERE userId = :userId")
    suspend fun getAllSessionsOnce(userId: String): List<SessionEntity>

    @Query("DELETE FROM sessions WHERE id = :id AND userId = :userId")
    suspend fun deleteSessionById(id: Long, userId: String)
}
