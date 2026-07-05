package se.w3footprint.korlog.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import se.w3footprint.korlog.data.local.dao.SessionDao
import se.w3footprint.korlog.data.local.entity.SessionEntity

@Database(
    entities = [SessionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class TaxiDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao

    companion object {
        const val DATABASE_NAME = "korlog.db"
    }
}
