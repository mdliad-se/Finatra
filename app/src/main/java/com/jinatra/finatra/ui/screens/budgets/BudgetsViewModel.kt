package com.jinatra.finatra.ui.screens.budgets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jinatra.finatra.data.ai.AiService
import com.jinatra.finatra.data.local.entity.BudgetEntity
import com.jinatra.finatra.data.local.entity.BudgetPeriod
import com.jinatra.finatra.data.prefs.SettingsRepository
import com.jinatra.finatra.data.repository.FinanceRepository
import com.jinatra.finatra.util.DateUtil
import com.jinatra.finatra.util.Money
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BudgetRow(
    val budget: BudgetEntity,
    val categoryName: String,
    val categoryColor: Long,
    val spent: Double,
) {
    val fraction: Float get() = if (budget.amount > 0) (spent / budget.amount).toFloat().coerceIn(0f, 1f) else 0f
    val isOver: Boolean get() = spent > budget.amount
    val isWarning: Boolean get() = !isOver && fraction >= 0.8f
}

data class BudgetsUiState(
    val rows: List<BudgetRow> = emptyList(),
    val baseCurrency: String = "USD",
)

/** An AI-recommended monthly limit for a category (PRD 6.9). */
data class BudgetSuggestion(
    val categoryId: Long,
    val name: String,
    val currentAvg: Double,
    val suggested: Double,
)

@HiltViewModel
class BudgetsViewModel @Inject constructor(
    private val repo: FinanceRepository,
    private val ai: AiService,
    settings: SettingsRepository,
) : ViewModel() {

    val state = combine(
        repo.observeBudgets(),
        repo.observeCategories(),
        settings.settings,
    ) { budgets, cats, s ->
        val rows = budgets.map { b ->
            val cat = cats.firstOrNull { it.id == b.categoryId }
            val (start, end) = if (b.period == BudgetPeriod.MONTHLY) DateUtil.startOfMonth() to DateUtil.endOfMonth()
                else b.startDate to (b.endDate ?: DateUtil.endOfMonth())
            BudgetRow(b, cat?.name ?: "—", cat?.colorHex ?: 0xFF0A756C, repo.spentInCategory(b.categoryId, start, end))
        }
        BudgetsUiState(rows, s.baseCurrency)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BudgetsUiState())

    val aiAvailable: Boolean get() = ai.isConfigured()

    private val _suggesting = MutableStateFlow(false)
    val suggesting = _suggesting.asStateFlow()
    private val _suggestions = MutableStateFlow<List<BudgetSuggestion>>(emptyList())
    val suggestions = _suggestions.asStateFlow()

    fun delete(b: BudgetEntity) { viewModelScope.launch { repo.deleteBudget(b) } }

    /** Ask AI for monthly limits based on the last 3 months of spend (PRD 6.9). */
    fun recommend(onError: (String) -> Unit = {}) {
        _suggesting.value = true
        viewModelScope.launch {
            val avg = repo.avgMonthlySpend(3)
            if (avg.isEmpty()) { _suggesting.value = false; onError("Not enough spending history yet"); return@launch }
            val currency = state.value.baseCurrency
            val summary = avg.joinToString(", ") { "${it.name} ${Money.format(it.avg, currency)}" }
            val rec = ai.recommendBudgets(summary)
            _suggestions.value = if (rec == null) {
                // Fallback: suggest 90% of average spend per category.
                avg.map { BudgetSuggestion(it.categoryId, it.name, it.avg, (it.avg * 0.9)) }
            } else {
                avg.mapNotNull { a ->
                    val match = rec.entries.firstOrNull { it.key.equals(a.name, true) || a.name.contains(it.key, true) }
                    BudgetSuggestion(a.categoryId, a.name, a.avg, match?.value ?: (a.avg * 0.9))
                }
            }
            _suggesting.value = false
        }
    }

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

    fun dismissSuggestions() { _suggestions.value = emptyList() }
}
