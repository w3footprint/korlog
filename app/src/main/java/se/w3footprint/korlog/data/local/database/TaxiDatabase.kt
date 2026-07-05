package se.w3footprint.korlog.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import se.w3footprint.korlog.data.local.dao.SessionDao
import se.w3footprint.korlog.data.local.entity.SessionEntity

@Database(
    entities = [SessionEntity::class],
    version = 2,
    exportSchema = false
)
abstract class TaxiDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao

    companion object {
        const val DATABASE_NAME = "korlog.db"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE sessions ADD COLUMN breakDurationMillis INTEGER NOT NULL DEFAULT 0"
                )
            }
        }
    }
}
