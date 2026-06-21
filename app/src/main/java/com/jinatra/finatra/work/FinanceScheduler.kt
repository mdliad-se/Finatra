package com.jinatra.finatra.work

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/** Schedules the maintenance pass (notifications + recurring auto-log). */
object FinanceScheduler {
    private const val WORK_NAME = "finatra_daily_maintenance"
    private const val LAUNCH_WORK = "finatra_maintenance_now"

    /**
     * Enqueues a daily periodic [MaintenanceWorker] (kept unique, updated if it already
     * exists) plus a one-time run on launch so time-sensitive checks fire immediately.
     *
     * @param context any context; used to obtain the [WorkManager] instance.
     */
    fun schedule(context: Context) {
        val wm = WorkManager.getInstance(context)
        // Daily background pass.
        wm.enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            PeriodicWorkRequestBuilder<MaintenanceWorker>(1, TimeUnit.DAYS).build(),
        )
        // Also run a pass on app launch so budget/low-balance/recurring checks are timely.
        wm.enqueueUniqueWork(
            LAUNCH_WORK,
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<MaintenanceWorker>().build(),
        )
    }
}
