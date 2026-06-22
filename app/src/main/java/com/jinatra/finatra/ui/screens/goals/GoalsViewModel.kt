package com.jinatra.finatra.ui.screens.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jinatra.finatra.data.ai.AiService
import com.jinatra.finatra.data.local.entity.GoalEntity
import com.jinatra.finatra.data.local.entity.GoalType
import com.jinatra.finatra.data.prefs.SettingsRepository
import com.jinatra.finatra.data.repository.FinanceRepository
import com.jinatra.finatra.util.DateUtil
import com.jinatra.finatra.util.Money
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlin.math.ceil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for the goals screen, with goals split by kind.
 *
 * @property savings goals of type [GoalType.SAVINGS].
 * @property debts goals of any non-savings type (owed / lent).
 * @property baseCurrency the user's base currency code.
 */
data class GoalsUiState(
    val savings: List<GoalEntity> = emptyList(),
    val debts: List<GoalEntity> = emptyList(),
    val baseCurrency: String = "USD",
)

/**
 * Derived view of a goal's contribution plan (PRD goal/savings plan): the monthly target, how far
 * the saver *should* be by now, whether they're keeping up, and when the goal finishes at this pace.
 */
data class GoalPlan(
    val monthlyTarget: Double,
    val expectedSaved: Double,
    val onTrack: Boolean,
    val projectedFinish: Long?,
) {
    companion object {
        /** Build the plan for [g], or null when no plan is set (monthlyTarget <= 0). */
        fun of(g: GoalEntity, now: Long = System.currentTimeMillis()): GoalPlan? {
            if (g.monthlyTarget <= 0.0) return null
            val started = g.planStartedAt ?: g.createdAt
            val monthsElapsed = DateUtil.monthsUntil(now, started)
            val expected = (g.monthlyTarget * monthsElapsed).coerceAtMost(g.targetAmount)
            val remaining = (g.targetAmount - g.savedAmount).coerceAtLeast(0.0)
            val finish = if (remaining <= 0.0) now
                else DateUtil.plusMonths(now, ceil(remaining / g.monthlyTarget).toInt().coerceAtLeast(1))
            // Small tolerance so being a hair behind doesn't flip the badge to "behind".
            return GoalPlan(g.monthlyTarget, expected, g.savedAmount >= expected * 0.95, finish)
        }
    }
}

/** One goal's slice of an AI/heuristic savings plan, pending review. */
data class SavingsPlanItem(val goalId: Long, val name: String, val monthly: Double)

/**
 * ViewModel backing [GoalsScreen]. Splits the live goals stream into savings and debts
 * for the UI, performs goal CRUD and contributions via [FinanceRepository], and powers
 * the "Can I afford it?" feature — using the AI service when configured and falling back
 * to a net-worth heuristic otherwise.
 */
@HiltViewModel
class GoalsViewModel @Inject constructor(
    private val repo: FinanceRepository,
    private val ai: AiService,
    private val settings: SettingsRepository,
) : ViewModel() {

    val state = combine(repo.observeGoals(), settings.settings) { goals, s ->
        GoalsUiState(
            savings = goals.filter { it.type == GoalType.SAVINGS },
            debts = goals.filter { it.type != GoalType.SAVINGS },
            baseCurrency = s.baseCurrency,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), GoalsUiState())

    /** Whether an AI provider is configured; gates the AI path of the affordability check. */
    val aiAvailable: Boolean get() = ai.isConfigured()

    // --- Can I afford it? (PRD 6.9) ---
    private val _affordLoading = MutableStateFlow(false)
    val affordLoading = _affordLoading.asStateFlow()
    private val _affordResult = MutableStateFlow<String?>(null)
    val affordResult = _affordResult.asStateFlow()

    /** Creates or updates a goal. */
    fun save(g: GoalEntity) { viewModelScope.launch { repo.upsertGoal(g) } }
    /** Removes a goal. */
    fun delete(g: GoalEntity) { viewModelScope.launch { repo.deleteGoal(g) } }

    /** Add [delta] to a goal's saved/repaid amount (contribution or payment). */
    fun contribute(g: GoalEntity, delta: Double) {
        viewModelScope.launch {
            repo.upsertGoal(g.copy(savedAmount = (g.savedAmount + delta).coerceAtLeast(0.0)))
        }
    }

    // --- Goal contribution plan (PRD goal plan) ---
    private val _planning = MutableStateFlow(false)
    val planning = _planning.asStateFlow()

    /**
     * Sets a monthly contribution plan for [g]. Uses the deadline to pace it when one is set,
     * otherwise asks the AI for a sensible monthly amount (falling back to clearing the balance
     * over a year). Persists [GoalEntity.monthlyTarget] + [GoalEntity.planStartedAt].
     */
    fun autoPlan(g: GoalEntity) {
        if (_planning.value) return
        _planning.value = true
        viewModelScope.launch {
            val remaining = (g.targetAmount - g.savedAmount).coerceAtLeast(0.0)
            val base = state.value.baseCurrency
            val monthly = when {
                remaining <= 0.0 -> 0.0
                // Pace to the deadline when there is one.
                g.deadline != null && g.deadline > System.currentTimeMillis() ->
                    remaining / DateUtil.monthsUntil(g.deadline).coerceAtLeast(1)
                // No deadline: let the AI propose a monthly amount from the user's finances.
                aiAvailable -> aiMonthly(g, remaining, base) ?: (remaining / 12.0)
                else -> remaining / 12.0
            }
            repo.upsertGoal(g.copy(monthlyTarget = monthly, planStartedAt = System.currentTimeMillis()))
            _planning.value = false
        }
    }

    /** Ask the AI for a single monthly contribution figure; null if it can't be parsed. */
    private suspend fun aiMonthly(g: GoalEntity, remaining: Double, base: String): Double? {
        val netWorth = repo.convertedNetWorth(base)
        val health = repo.financeHealth(base)
        val prompt = """
            Suggest a realistic MONTHLY savings contribution to reach a goal.
            Goal: ${g.name}. Remaining to save: ${Money.format(remaining, base)}.
            User net worth ${Money.format(netWorth, base)}, savings rate ${(health.savingsRate * 100).toInt()}%.
            Reply with ONLY a number (the monthly amount in $base), no currency symbol, no prose.
        """.trimIndent()
        val raw = ai.chat(prompt)?.trim() ?: return null
        return Regex("[0-9]+(\\.[0-9]+)?").find(raw.replace(",", ""))?.value?.toDoubleOrNull()?.takeIf { it > 0 }
    }

    /** Clear a goal's plan. */
    fun clearPlan(g: GoalEntity) {
        viewModelScope.launch { repo.upsertGoal(g.copy(monthlyTarget = 0.0, planStartedAt = null)) }
    }

    // --- Savings plan: allocate the monthly surplus across goals (PRD savings plan) ---
    private val _savingsPlan = MutableStateFlow<List<SavingsPlanItem>>(emptyList())
    val savingsPlan = _savingsPlan.asStateFlow()
    private val _buildingPlan = MutableStateFlow(false)
    val buildingPlan = _buildingPlan.asStateFlow()

    /**
     * Proposes how to split the projected monthly surplus across unfinished savings goals,
     * proportional to how much each still needs. Falls back to clearing each balance over a year
     * when there's no surplus. Results are staged in [savingsPlan] for review.
     */
    fun buildSavingsPlan(onError: (String) -> Unit) {
        if (_buildingPlan.value) return
        val goals = state.value.savings.filter { it.savedAmount < it.targetAmount }
        if (goals.isEmpty()) { onError("Add a savings goal first."); return }
        _buildingPlan.value = true
        viewModelScope.launch {
            val base = state.value.baseCurrency
            val surplus = repo.monthlyForecast(base).projectedSavings.coerceAtLeast(0.0)
            val totalRemaining = goals.sumOf { (it.targetAmount - it.savedAmount).coerceAtLeast(0.0) }
            _savingsPlan.value = goals.map { g ->
                val remaining = (g.targetAmount - g.savedAmount).coerceAtLeast(0.0)
                val monthly = if (surplus > 0.0 && totalRemaining > 0.0) surplus * (remaining / totalRemaining)
                    else remaining / 12.0
                SavingsPlanItem(g.id, g.name, monthly)
            }
            _buildingPlan.value = false
        }
    }

    /** Apply the staged savings plan: set each goal's monthly target and start it now. */
    fun applySavingsPlan() {
        val items = _savingsPlan.value
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            items.forEach { item ->
                repo.goalById(item.goalId)?.let { g ->
                    repo.upsertGoal(g.copy(monthlyTarget = item.monthly, planStartedAt = now))
                }
            }
            _savingsPlan.value = emptyList()
        }
    }

    /** Discard the staged savings plan. */
    fun dismissSavingsPlan() { _savingsPlan.value = emptyList() }

    /** Clears the affordability result. */
    fun dismissAfford() { _affordResult.value = null }

    /**
     * Evaluates whether the user can afford to spend [amount]. Ignores non-positive amounts.
     * When no AI is configured, returns a heuristic verdict based on the amount's share of net
     * worth; otherwise prompts the AI with net worth, savings rate, and health score and shows
     * its reply (falling back to a heuristic message if the AI call fails).
     */
    fun checkAfford(amount: Double) {
        if (amount <= 0) return
        _affordLoading.value = true
        _affordResult.value = null
        viewModelScope.launch {
            val base = state.value.baseCurrency
            val netWorth = repo.convertedNetWorth(base)
            val health = repo.financeHealth(base)
            // Offline heuristic: classify by how big the spend is relative to net worth.
            if (!aiAvailable) {
                val verdict = if (amount <= netWorth * 0.1)
                    "Likely yes — that's a small share of your ${Money.format(netWorth, base)} net worth."
                else if (amount <= netWorth * 0.3)
                    "Maybe — it's a noticeable chunk of your net worth. Check your goals first."
                else
                    "Caution — that's a large share of your ${Money.format(netWorth, base)} net worth."
                _affordResult.value = verdict
                _affordLoading.value = false
                return@launch
            }
            val prompt = """
                A user asks if they can afford to spend ${Money.format(amount, base)}.
                Their net worth is ${Money.format(netWorth, base)}, savings rate ${(health.savingsRate * 100).toInt()}%,
                financial health score ${health.score}/100.
                Give a short (max 35 words), friendly yes/maybe/no recommendation with one reason. No preamble.
            """.trimIndent()
            _affordResult.value = ai.chat(prompt)?.trim()
                ?: "Couldn't reach AI. Based on your net worth of ${Money.format(netWorth, base)}, decide carefully."
            _affordLoading.value = false
        }
    }
}
