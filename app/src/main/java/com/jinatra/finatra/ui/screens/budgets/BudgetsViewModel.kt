package com.jinatra.finatra.ui.screens.budgets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jinatra.finatra.data.ai.AiService
import com.jinatra.finatra.data.local.entity.BudgetEntity
import com.jinatra.finatra.data.local.entity.BudgetPeriod
import com.jinatra.finatra.data.local.entity.LoanEntity
import com.jinatra.finatra.data.prefs.SettingsRepository
import com.jinatra.finatra.data.repository.FinanceRepository
import com.jinatra.finatra.util.DateUtil
import com.jinatra.finatra.util.Emi
import com.jinatra.finatra.util.Money
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * A budget paired with its resolved category and the amount spent against it this period —
 * the unit rendered by each budget list item.
 */
data class BudgetRow(
    val budget: BudgetEntity,
    val categoryName: String,
    val categoryColor: Long,
    val spent: Double,
) {
    /** Spend as a fraction of the limit, clamped to 0..1 for progress display. */
    val fraction: Float get() = if (budget.amount > 0) (spent / budget.amount).toFloat().coerceIn(0f, 1f) else 0f
    /** True once spend has exceeded the limit. */
    val isOver: Boolean get() = spent > budget.amount
    /** True when nearing the limit (>= 80%) but not yet over. */
    val isWarning: Boolean get() = !isOver && fraction >= 0.8f
}

/** UI state for [BudgetsScreen]: all budget rows for the period and the user's base currency. */
data class BudgetsUiState(
    val rows: List<BudgetRow> = emptyList(),
    val baseCurrency: String = "USD",
)

/** A loan paired with its current EMI schedule (payment, remaining balance, progress). */
data class LoanRow(
    val loan: LoanEntity,
    val schedule: Emi.Schedule,
)

/** An AI-recommended monthly limit for a category (PRD 6.9). */
data class BudgetSuggestion(
    val categoryId: Long,
    val name: String,
    val currentAvg: Double,
    val suggested: Double,
)

/**
 * Backs [BudgetsScreen]. Builds the per-category budget rows (joining budgets with their
 * categories and current-period spend) and provides one-tap AI budget recommendations based
 * on recent spending, with a deterministic fallback when AI is unavailable.
 */
@HiltViewModel
class BudgetsViewModel @Inject constructor(
    private val repo: FinanceRepository,
    private val ai: AiService,
    settings: SettingsRepository,
) : ViewModel() {

    // Join budgets with their category and the spend for the budget's active period into display rows.
    val state = combine(
        repo.observeBudgets(),
        repo.observeCategories(),
        settings.settings,
    ) { budgets, cats, s ->
        val rows = budgets.map { b ->
            val cat = cats.firstOrNull { it.id == b.categoryId }
            // Monthly budgets use the current month; custom budgets use their own date range.
            val (start, end) = if (b.period == BudgetPeriod.MONTHLY) DateUtil.startOfMonth() to DateUtil.endOfMonth()
                else b.startDate to (b.endDate ?: DateUtil.endOfMonth())
            BudgetRow(b, cat?.name ?: "—", cat?.colorHex ?: 0xFFE05454, repo.convertedSpentInCategory(b.categoryId, start, end, s.baseCurrency))
        }
        BudgetsUiState(rows, s.baseCurrency)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BudgetsUiState())

    // Tracked loans with their EMI schedule computed from months elapsed since the start date.
    val loans = repo.observeLoans()
        .map { list ->
            val now = System.currentTimeMillis()
            list.map { l ->
                LoanRow(l, Emi.schedule(l.principal, l.annualRatePct, l.tenureMonths, DateUtil.monthsUntil(now, l.startDate)))
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val aiAvailable: Boolean get() = ai.isConfigured()

    /** Add a loan / EMI plan; its first payment month is now. */
    fun addLoan(name: String, principal: Double, annualRatePct: Double, tenureMonths: Int, currency: String) {
        if (name.isBlank() || principal <= 0 || tenureMonths <= 0) return
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            repo.upsertLoan(
                LoanEntity(
                    name = name.trim(), principal = principal, annualRatePct = annualRatePct,
                    tenureMonths = tenureMonths, startDate = now, currency = currency, createdAt = now,
                )
            )
        }
    }

    /** Remove a loan. */
    fun deleteLoan(l: LoanEntity) { viewModelScope.launch { repo.deleteLoan(l) } }

    private val _suggesting = MutableStateFlow(false)
    val suggesting = _suggesting.asStateFlow()
    private val _suggestions = MutableStateFlow<List<BudgetSuggestion>>(emptyList())
    val suggestions = _suggestions.asStateFlow()

    /** Remove a budget. */
    fun delete(b: BudgetEntity) { viewModelScope.launch { repo.deleteBudget(b) } }

    /** Ask AI for monthly limits based on the last 3 months of spend (PRD 6.9). */
    fun recommend(onError: (String) -> Unit = {}) {
        _suggesting.value = true
        viewModelScope.launch {
            val avg = repo.avgMonthlySpend(3)
            if (avg.isEmpty()) { _suggesting.value = false; onError("Not enough spending history yet"); return@launch }
            val currency = state.value.baseCurrency
            // Feed the AI a per-category spend summary and ask for recommended limits.
            val summary = avg.joinToString(", ") { "${it.name} ${Money.format(it.avg, currency)}" }
            val rec = ai.recommendBudgets(summary)
            _suggestions.value = if (rec == null) {
                // Fallback: suggest 90% of average spend per category.
                avg.map { BudgetSuggestion(it.categoryId, it.name, it.avg, (it.avg * 0.9)) }
            } else {
                // Match AI limits back to known categories by name; fall back to 90% of average if unmatched.
                avg.mapNotNull { a ->
                    val match = rec.entries.firstOrNull { it.key.equals(a.name, true) || a.name.contains(it.key, true) }
                    BudgetSuggestion(a.categoryId, a.name, a.avg, match?.value ?: (a.avg * 0.9))
                }
            }
            _suggesting.value = false
        }
    }

    /** Create a monthly budget from a single recommendation and drop it from the suggestion list. */
    fun applySuggestion(s: BudgetSuggestion) {
        viewModelScope.launch {
            repo.upsertBudget(
                BudgetEntity(
                    categoryId = s.categoryId, amount = s.suggested, period = BudgetPeriod.MONTHLY,
                    startDate = DateUtil.startOfMonth(), endDate = null, createdAt = System.currentTimeMillis(),
                )
            )
            _suggestions.value = _suggestions.value.filterNot { it.categoryId == s.categoryId }
        }
    }

    /** Create monthly budgets from all current recommendations, then clear the list. */
    fun applyAll() {
        val all = _suggestions.value
        viewModelScope.launch {
            all.forEach { s ->
                repo.upsertBudget(
                    BudgetEntity(
                        categoryId = s.categoryId, amount = s.suggested, period = BudgetPeriod.MONTHLY,
                        startDate = DateUtil.startOfMonth(), endDate = null, createdAt = System.currentTimeMillis(),
                    )
                )
            }
            _suggestions.value = emptyList()
        }
    }

    /** Discard the current recommendations without creating budgets. */
    fun dismissSuggestions() { _suggestions.value = emptyList() }
}
