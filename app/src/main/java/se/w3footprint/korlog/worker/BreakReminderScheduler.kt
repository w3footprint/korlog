package se.w3footprint.korlog.worker

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import se.w3footprint.korlog.data.local.store.UserPreferencesStore
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BreakReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val prefs: UserPreferencesStore
) {
    private val workManager = WorkManager.getInstance(context)

    fun schedule(driveTimeHours: Long = 6L) {
        val enabled = runBlocking { prefs.notificationsEnabled.first() }
        if (!enabled) return

        val request = OneTimeWorkRequestBuilder<BreakReminderWorker>()
            .setInitialDelay(driveTimeHours, TimeUnit.HOURS)
            .build()

        workManager.enqueueUniqueWork(
            BreakReminderWorker.WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    fun cancel() {
        workManager.cancelUniqueWork(BreakReminderWorker.WORK_NAME)
    }
}
