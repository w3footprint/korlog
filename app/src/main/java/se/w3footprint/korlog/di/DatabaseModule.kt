package se.w3footprint.korlog.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import se.w3footprint.korlog.data.local.dao.SessionDao
import se.w3footprint.korlog.data.local.database.TaxiDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): TaxiDatabase {
        return Room.databaseBuilder(
            context,
            TaxiDatabase::class.java,
            TaxiDatabase.DATABASE_NAME
        )
            .addMigrations(TaxiDatabase.MIGRATION_1_2)
            .build()
    }

    @Provides
    @Singleton
    fun provideSessionDao(database: TaxiDatabase): SessionDao {
        return database.sessionDao()
    }
}
