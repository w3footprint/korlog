package se.w3footprint.korlog.data.local.store

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.sessionDataStore: DataStore<Preferences> by preferencesDataStore("active_session")

data class PersistedSessionState(
    val isRunning: Boolean = false,
    val isOnBreak: Boolean = false,
    val startTime: Long = 0L,
    val totalBreakMillis: Long = 0L,
    val currentBreakStartMillis: Long = 0L,
    val earningsInput: String = "",
    val distanceInput: String = "",
    val platform: String = "OTHER",
    val notes: String = ""
)

@Singleton
class ActiveSessionStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val IS_RUNNING = booleanPreferencesKey("is_running")
        val IS_ON_BREAK = booleanPreferencesKey("is_on_break")
        val START_TIME = longPreferencesKey("start_time")
        val TOTAL_BREAK_MILLIS = longPreferencesKey("total_break_millis")
        val CURRENT_BREAK_START = longPreferencesKey("current_break_start")
        val EARNINGS_INPUT = stringPreferencesKey("earnings_input")
        val DISTANCE_INPUT = stringPreferencesKey("distance_input")
        val PLATFORM = stringPreferencesKey("platform")
        val NOTES = stringPreferencesKey("notes")
    }

    val state: Flow<PersistedSessionState> = context.sessionDataStore.data.map { prefs ->
        PersistedSessionState(
            isRunning = prefs[Keys.IS_RUNNING] ?: false,
            isOnBreak = prefs[Keys.IS_ON_BREAK] ?: false,
            startTime = prefs[Keys.START_TIME] ?: 0L,
            totalBreakMillis = prefs[Keys.TOTAL_BREAK_MILLIS] ?: 0L,
            currentBreakStartMillis = prefs[Keys.CURRENT_BREAK_START] ?: 0L,
            earningsInput = prefs[Keys.EARNINGS_INPUT] ?: "",
            distanceInput = prefs[Keys.DISTANCE_INPUT] ?: "",
            platform = prefs[Keys.PLATFORM] ?: "OTHER",
            notes = prefs[Keys.NOTES] ?: ""
        )
    }

    suspend fun save(s: PersistedSessionState) {
        context.sessionDataStore.edit { prefs ->
            prefs[Keys.IS_RUNNING] = s.isRunning
            prefs[Keys.IS_ON_BREAK] = s.isOnBreak
            prefs[Keys.START_TIME] = s.startTime
            prefs[Keys.TOTAL_BREAK_MILLIS] = s.totalBreakMillis
            prefs[Keys.CURRENT_BREAK_START] = s.currentBreakStartMillis
            prefs[Keys.EARNINGS_INPUT] = s.earningsInput
            prefs[Keys.DISTANCE_INPUT] = s.distanceInput
            prefs[Keys.PLATFORM] = s.platform
            prefs[Keys.NOTES] = s.notes
        }
    }

    suspend fun clear() {
        context.sessionDataStore.edit { it.clear() }
    }
}
