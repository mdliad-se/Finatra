package com.jinatra.finatra.ui.screens.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jinatra.finatra.data.local.dao.CategorySpend
import com.jinatra.finatra.data.local.dao.PayeeSpend
import com.jinatra.finatra.data.local.entity.TransactionType
import com.jinatra.finatra.data.prefs.SettingsRepository
import com.jinatra.finatra.data.repository.CategoryAvgSpend
import com.jinatra.finatra.data.repository.FinanceRepository
import com.jinatra.finatra.data.repository.MonthPoint
import com.jinatra.finatra.data.repository.MonthlyForecast
import com.jinatra.finatra.util.DateUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Immutable UI state for the analytics dashboard: the current month's income, expense and
 * per-category spend, a multi-month series, an end-of-month forecast and top payees, all
 * expressed in [baseCurrency].
 */
data class AnalyticsUiState(
    val baseCurrency: String = "USD",
    val monthLabel: String = "",
    val income: Double = 0.0,
    val expense: Double = 0.0,
    val byCategory: List<CategorySpend> = emptyList(),
    val series: List<MonthPoint> = emptyList(),
    val forecast: MonthlyForecast? = null,
    val payees: List<PayeeSpend> = emptyList(),
) {
    /** Net savings for the month: income minus expense (can be negative). */
    val savings: Double get() = income - expense
}

/**
 * What-if simulator inputs + projected saving (PRD 6.9).
 *
 * Models "if I cut [selected] by [cutPercent]% over [months] months, how much would I save?",
 * driving the projection off each category's historical monthly average.
 */
data class WhatIfState(
    val categories: List<CategoryAvgSpend> = emptyList(),
    val selectedCategoryId: Long? = null,
    val cutPercent: Int = 30,
    val months: Int = 6,
) {
    /** The currently chosen category, resolved from [selectedCategoryId]. */
    val selected: CategoryAvgSpend? get() = categories.firstOrNull { it.categoryId == selectedCategoryId }
    /** Projected saving per month: the cut percentage applied to the category's average spend. */
    val monthlySaving: Double get() = (selected?.avg ?: 0.0) * cutPercent / 100.0
    /** Projected total saving over the chosen horizon. */
    val totalSaving: Double get() = monthlySaving * months
}

/**
 * Drives [AnalyticsScreen]. Combines settings, transactions and FX rates into a single
 * [AnalyticsUiState], and owns the independent [whatIf] simulator state. All monetary
 * figures are converted to the user's base currency by the repository.
 */
@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val repo: FinanceRepository,
    settings: SettingsRepository,
) : ViewModel() {

    private val start = DateUtil.startOfMonth()
    private val end = DateUtil.endOfMonth()

    private data class Derived(
        val series: List<MonthPoint>,
        val forecast: MonthlyForecast,
        val payees: List<PayeeSpend>,
    )

    // 6-month series + month-end forecast + top payees, converted to base; recomputes on data changes.
    private val derivedFlow = combine(
        settings.settings,
        repo.observeTransactions(),
        repo.observeRates(),
    ) { s, _, _ ->
        Derived(
            repo.monthlySeries(6, s.baseCurrency),
            repo.monthlyForecast(s.baseCurrency),
            repo.payeeSpend(start, end),
        )
    }

    val state = combine(
        settings.settings,
        repo.observeTotalByType(TransactionType.INCOME.name, start, end),
        repo.observeTotalByType(TransactionType.EXPENSE.name, start, end),
        repo.observeSpendByCategory(start, end),
        derivedFlow,
    ) { s, income, expense, byCat, derived ->
        AnalyticsUiState(
            baseCurrency = s.baseCurrency,
            monthLabel = DateUtil.month(start),
            income = income,
            expense = expense,
            byCategory = byCat,
            series = derived.series,
            forecast = derived.forecast,
            payees = derived.payees,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AnalyticsUiState())

    // --- What-if simulator ---
    private val _whatIf = MutableStateFlow(WhatIfState())
    val whatIf = _whatIf.asStateFlow()

    init {
        // Seed the simulator with each category's 3-month average spend, defaulting to the first.
        viewModelScope.launch {
            val cats = repo.avgMonthlySpend(3)
            _whatIf.value = _whatIf.value.copy(categories = cats, selectedCategoryId = cats.firstOrNull()?.categoryId)
        }
    }

    fun setWhatIfCategory(id: Long) { _whatIf.value = _whatIf.value.copy(selectedCategoryId = id) }
    fun setWhatIfPercent(pct: Int) { _whatIf.value = _whatIf.value.copy(cutPercent = pct.coerceIn(5, 100)) }
    fun setWhatIfMonths(m: Int) { _whatIf.value = _whatIf.value.copy(months = m.coerceIn(1, 24)) }
}
