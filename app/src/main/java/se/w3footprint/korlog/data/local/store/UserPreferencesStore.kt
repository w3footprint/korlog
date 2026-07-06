package se.w3footprint.korlog.data.local.store

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.userPrefsDataStore: DataStore<Preferences> by preferencesDataStore("user_preferences")

@Singleton
class UserPreferencesStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val WEEKLY_LIMIT_HOURS = intPreferencesKey("weekly_limit_hours")
        val MONTHLY_LIMIT_HOURS = intPreferencesKey("monthly_limit_hours")
    }

    val weeklyLimitHours: Flow<Int> = context.userPrefsDataStore.data.map { prefs ->
        prefs[Keys.WEEKLY_LIMIT_HOURS] ?: 60
    }

    val monthlyLimitHours: Flow<Int> = context.userPrefsDataStore.data.map { prefs ->
        prefs[Keys.MONTHLY_LIMIT_HOURS] ?: 192
    }

    suspend fun setWeeklyLimitHours(hours: Int) {
        context.userPrefsDataStore.edit { it[Keys.WEEKLY_LIMIT_HOURS] = hours }
    }

    suspend fun setMonthlyLimitHours(hours: Int) {
        context.userPrefsDataStore.edit { it[Keys.MONTHLY_LIMIT_HOURS] = hours }
    }
}
