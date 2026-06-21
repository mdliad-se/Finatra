package com.jinatra.finatra.ui.screens.achievements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jinatra.finatra.data.ai.AiService
import com.jinatra.finatra.data.local.entity.BudgetPeriod
import com.jinatra.finatra.data.local.entity.GoalType
import com.jinatra.finatra.data.prefs.SettingsRepository
import com.jinatra.finatra.data.repository.FinanceRepository
import com.jinatra.finatra.util.DateUtil
import com.jinatra.finatra.util.Money
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/** An achievement badge with its display glyph and unlocked status. */
data class Badge(val emoji: String, val name: String, val unlocked: Boolean)

/** The active weekly challenge with progress [fraction] in 0f..1f. */
data class Challenge(val title: String, val detail: String, val fraction: Float, val done: Boolean)

/** A reached or upcoming financial milestone (PRD 6.14). */
data class Milestone(val label: String, val reached: Boolean)

/** Aggregate UI state rendered by [AchievementsScreen]. */
data class AchievementsUiState(
    val streak: Int = 0,
    val challenge: Challenge = Challenge("Log every day this week", "Build a 7-day logging streak", 0f, false),
    val badges: List<Badge> = emptyList(),
    val milestones: List<Milestone> = emptyList(),
)

/** Net-worth thresholds that, once crossed, surface as milestones. */
private val NET_WORTH_TIERS = listOf(1_000.0, 5_000.0, 10_000.0, 25_000.0, 50_000.0, 100_000.0)

/**
 * Backs [AchievementsScreen]. Reactively derives gamification state from the user's transactions,
 * budgets, goals, and settings: the consecutive-day logging streak, the weekly challenge progress,
 * earned badges, and net-worth/goal milestones.
 */
@HiltViewModel
class AchievementsViewModel @Inject constructor(
    repo: FinanceRepository,
    settings: SettingsRepository,
    ai: AiService,
) : ViewModel() {

    // Recompute the full achievements snapshot whenever any underlying finance data changes.
    val state = combine(
        repo.observeTransactions(),
        repo.observeBudgets(),
        repo.observeGoals(),
        settings.settings,
    ) { txns, budgets, goals, s ->
        val today = DateUtil.startOfDay()
        val activeDays = txns.map { DateUtil.startOfDay(it.dateTime) }.toSet()

        // Streak: consecutive days ending today (or yesterday) with at least one transaction.
        var streak = 0
        var cursor = if (activeDays.contains(today)) today else DateUtil.plusDays(today, -1)
        while (activeDays.contains(cursor)) {
            streak++
            cursor = DateUtil.plusDays(cursor, -1)
        }

        // This-month spend per category (for budget badges).
        val mStart = DateUtil.startOfMonth()
        val mEnd = DateUtil.endOfMonth()
        val spentByCat = txns.filter { it.type == "EXPENSE" && it.dateTime in mStart..mEnd && it.categoryId != null }
            .groupBy { it.categoryId!! }
            .mapValues { e -> e.value.sumOf { it.amount } }
        val monthlyBudgets = budgets.filter { it.period == BudgetPeriod.MONTHLY }
        // True if any monthly budget is overspent — used to award the "Perfect Month" badge.
        val anyOver = monthlyBudgets.any { (spentByCat[it.categoryId] ?: 0.0) > it.amount && it.amount > 0 }

        val savings = goals.filter { it.type == GoalType.SAVINGS }
        val owed = goals.filter { it.type == GoalType.DEBT_OWED }
        val distinctCats = txns.mapNotNull { it.categoryId }.toSet().size

        val weekFraction = (streak.coerceAtMost(7) / 7f)
        val challenge = Challenge(
            title = "Log every day this week",
            detail = "Keep your logging streak going — $streak day${if (streak == 1) "" else "s"} so far",
            fraction = weekFraction,
            done = weekFraction >= 1f,
        )

        val badges = listOf(
            Badge("🎯", "First Budget", budgets.isNotEmpty()),
            Badge("💰", "First Saving", savings.any { it.savedAmount > 0 }),
            Badge("🔥", "7-Day Streak", streak >= 7),
            Badge("⚡", "30-Day Streak", streak >= 30),
            Badge("🏆", "100-Day Streak", streak >= 100),
            Badge("👑", "Goal Crusher", goals.any { it.targetAmount > 0 && it.savedAmount >= it.targetAmount }),
            Badge("✅", "Zero Debt", owed.isNotEmpty() && owed.all { it.savedAmount >= it.targetAmount }),
            Badge("⭐", "Perfect Month", monthlyBudgets.isNotEmpty() && !anyOver),
            Badge("📊", "Analyst", distinctCats >= 3),
            Badge("🤖", "AI Explorer", ai.isConfigured()),
        )

        // Milestones (PRD 6.14): net-worth tiers reached + next target, plus completed goals.
        val netWorth = repo.convertedNetWorth(s.baseCurrency)
        val tierMilestones = buildList {
            val nextTier = NET_WORTH_TIERS.firstOrNull { it > netWorth }
            NET_WORTH_TIERS.filter { it <= netWorth }.forEach {
                add(Milestone("Net worth ${Money.format(it, s.baseCurrency)}", true))
            }
            if (nextTier != null) add(Milestone("Next: ${Money.format(nextTier, s.baseCurrency)}", false))
        }
        val goalMilestones = goals
            .filter { it.targetAmount > 0 && it.savedAmount >= it.targetAmount }
            .map { Milestone("Goal reached: ${it.name}", true) }
        val milestones = goalMilestones + tierMilestones

        AchievementsUiState(streak, challenge, badges, milestones)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AchievementsUiState())
}
