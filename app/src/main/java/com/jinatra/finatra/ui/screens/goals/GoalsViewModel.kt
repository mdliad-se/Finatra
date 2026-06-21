package com.jinatra.finatra.ui.screens.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jinatra.finatra.data.ai.AiService
import com.jinatra.finatra.data.local.entity.GoalEntity
import com.jinatra.finatra.data.local.entity.GoalType
import com.jinatra.finatra.data.prefs.SettingsRepository
import com.jinatra.finatra.data.repository.FinanceRepository
import com.jinatra.finatra.util.Money
import dagger.hilt.android.lifecycle.HiltViewModel
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
