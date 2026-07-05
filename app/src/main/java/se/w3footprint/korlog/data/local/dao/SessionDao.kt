package se.w3footprint.korlog.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import se.w3footprint.taxiworktracker.data.local.entity.SessionEntity

@Dao
interface SessionDao {

    @Query("SELECT * FROM sessions ORDER BY date DESC")
    fun getAllSessions(): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions WHERE date BETWEEN :startMillis AND :endMillis ORDER BY date DESC")
    fun getSessionsByDateRange(startMillis: Long, endMillis: Long): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions WHERE id = :id")
    suspend fun getSessionById(id: Long): SessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SessionEntity): Long

    @Update
    suspend fun updateSession(session: SessionEntity)

    @Delete
    suspend fun deleteSession(session: SessionEntity)
}
