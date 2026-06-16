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

@HiltAndroidApp
class FinatraApp : Application() {

    @Inject lateinit var repository: FinanceRepository

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        FinanceScheduler.schedule(this)
        appScope.launch { repository.seedCategoriesIfEmpty() }
    }

    private fun createNotificationChannels() {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        listOf(
            NotificationChannel(CH_BUDGET, "Budget alerts", NotificationManager.IMPORTANCE_HIGH),
            NotificationChannel(CH_RECURRING, "Recurring reminders", NotificationManager.IMPORTANCE_DEFAULT),
            NotificationChannel(CH_SUMMARY, "Summaries", NotificationManager.IMPORTANCE_LOW),
        ).forEach(nm::createNotificationChannel)
    }

    companion object {
        const val CH_BUDGET = "budget_alerts"
        const val CH_RECURRING = "recurring_reminders"
        const val CH_SUMMARY = "summaries"
    }
}
