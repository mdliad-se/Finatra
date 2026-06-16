package com.jinatra.finatra.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.jinatra.finatra.FinatraApp
import com.jinatra.finatra.data.prefs.SettingsRepository
import com.jinatra.finatra.data.repository.FinanceRepository
import com.jinatra.finatra.data.local.entity.TransactionType
import com.jinatra.finatra.util.DateUtil
import com.jinatra.finatra.util.Money
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import java.util.Calendar

/** Hilt access for non-injectable WorkManager workers. */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface WorkerEntryPoint {
    fun financeRepository(): FinanceRepository
    fun settingsRepository(): SettingsRepository
}

/**
 * Single daily maintenance pass (PRD 6.10 + recurring auto-log).
 * Fans out to: due recurring, budget overspend, low balance, and weekly/monthly
 * summaries gated by day-of-week / day-of-month. All steps respect user toggles.
 */
class MaintenanceWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = runCatching {
        val ep = EntryPointAccessors.fromApplication(applicationContext, WorkerEntryPoint::class.java)
        val repo = ep.financeRepository()
        val settings = ep.settingsRepository().settings.first()
        val now = System.currentTimeMillis()
        val base = settings.baseCurrency

        // 1. Recurring: auto-log + reminders
        val outcome = repo.processDueRecurring(now)
        if (settings.notifRecurring) {
            outcome.reminders.forEach { r ->
                Notifier.post(
                    applicationContext, FinatraApp.CH_RECURRING, ("rec" + r.id).hashCode(),
                    "Recurring due", "${r.note.ifBlank { "Scheduled entry" }} — ${Money.format(r.amount, r.currency)}",
                )
            }
            if (outcome.autoLogged.isNotEmpty()) {
                Notifier.post(
                    applicationContext, FinatraApp.CH_RECURRING, "recauto".hashCode(),
                    "Recurring logged", "${outcome.autoLogged.size} scheduled transaction(s) added automatically.",
                )
            }
        }

        // 2. Budget overspend
        if (settings.notifBudget) {
            repo.overspentBudgets(now).forEach { b ->
                Notifier.post(
                    applicationContext, FinatraApp.CH_BUDGET, ("bud" + b.categoryName).hashCode(),
                    "Budget exceeded: ${b.categoryName}",
                    "Spent ${Money.format(b.spent, base)} of ${Money.format(b.limit, base)} limit.",
                )
            }
        }

        // 3. Low balance
        if (settings.notifLowBalance) {
            repo.accountsBelowThreshold().forEach { (a, bal) ->
                Notifier.post(
                    applicationContext, FinatraApp.CH_BUDGET, ("low" + a.id).hashCode(),
                    "Low balance: ${a.name}",
                    "Balance ${Money.format(bal, a.currency)} below ${Money.format(a.lowBalanceThreshold, a.currency)}.",
                )
            }
        }

        // 4. Weekly summary (Mondays)
        val cal = Calendar.getInstance().apply { timeInMillis = now }
        if (settings.notifWeekly && cal.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {
            val weekStart = DateUtil.plusDays(DateUtil.startOfDay(now), -7)
            val income = repo.convertedTotalByType(TransactionType.INCOME.name, weekStart, now, base)
            val expense = repo.convertedTotalByType(TransactionType.EXPENSE.name, weekStart, now, base)
            Notifier.post(
                applicationContext, FinatraApp.CH_SUMMARY, "weekly".hashCode(),
                "Weekly summary",
                "Last 7 days — income ${Money.format(income, base)}, spent ${Money.format(expense, base)}.",
            )
        }

        // 5. Monthly summary (1st of month → recap previous month)
        if (settings.notifMonthly && cal.get(Calendar.DAY_OF_MONTH) == 1) {
            val thisMonthStart = DateUtil.startOfMonth(now)
            val prevMonthStart = DateUtil.plusMonths(thisMonthStart, -1)
            val income = repo.convertedTotalByType(TransactionType.INCOME.name, prevMonthStart, thisMonthStart - 1, base)
            val expense = repo.convertedTotalByType(TransactionType.EXPENSE.name, prevMonthStart, thisMonthStart - 1, base)
            Notifier.post(
                applicationContext, FinatraApp.CH_SUMMARY, "monthly".hashCode(),
                "Monthly recap",
                "Last month — income ${Money.format(income, base)}, spent ${Money.format(expense, base)}, saved ${Money.format(income - expense, base)}.",
            )
        }
        Result.success()
    }.getOrElse { Result.retry() }
}
