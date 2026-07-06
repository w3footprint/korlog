package se.w3footprint.korlog.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import se.w3footprint.korlog.data.local.dao.SessionDao
import se.w3footprint.korlog.data.local.entity.SessionEntity

@Database(
    entities = [SessionEntity::class],
    version = 4,
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

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE sessions ADD COLUMN userId TEXT NOT NULL DEFAULT ''"
                )
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE sessions ADD COLUMN syncId TEXT NOT NULL DEFAULT ''")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_sessions_syncId ON sessions(syncId)")
            }
        }
    }
}
