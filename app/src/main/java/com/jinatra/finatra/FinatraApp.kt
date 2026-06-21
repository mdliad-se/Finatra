package com.jinatra.finatra

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import com.jinatra.finatra.data.repository.FinanceRepository
import com.jinatra.finatra.work.FinanceScheduler
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Application entry point and Hilt dependency-injection root.
 *
 * On startup it registers the app's notification channels, schedules the recurring
 * background maintenance work, and seeds the default categories on a background scope
 * if the database is empty.
 */
@HiltAndroidApp
class FinatraApp : Application() {

    @Inject lateinit var repository: FinanceRepository

    // Long-lived IO scope; SupervisorJob keeps startup work isolated so one failure
    // does not cancel the others.
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        FinanceScheduler.schedule(this)
        appScope.launch { repository.seedCategoriesIfEmpty() }
    }

    /** Registers the three notification channels (budget, recurring, summaries) at their respective importance levels. */
    private fun createNotificationChannels() {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        listOf(
            NotificationChannel(CH_BUDGET, "Budget alerts", NotificationManager.IMPORTANCE_HIGH),
            NotificationChannel(CH_RECURRING, "Recurring reminders", NotificationManager.IMPORTANCE_DEFAULT),
            NotificationChannel(CH_SUMMARY, "Summaries", NotificationManager.IMPORTANCE_LOW),
        ).forEach(nm::createNotificationChannel)
    }

    /** Stable notification-channel ids referenced when posting notifications. */
    companion object {
        const val CH_BUDGET = "budget_alerts"
        const val CH_RECURRING = "recurring_reminders"
        const val CH_SUMMARY = "summaries"
    }
}
