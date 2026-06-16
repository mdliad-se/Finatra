package com.jinatra.finatra.ui.screens.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jinatra.finatra.data.local.dao.CategorySpend
import com.jinatra.finatra.data.local.entity.TransactionType
import com.jinatra.finatra.data.prefs.SettingsRepository
import com.jinatra.finatra.data.repository.FinanceRepository
import com.jinatra.finatra.data.repository.MonthPoint
import com.jinatra.finatra.util.DateUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class AnalyticsUiState(
    val baseCurrency: String = "USD",
    val monthLabel: String = "",
    val income: Double = 0.0,
    val expense: Double = 0.0,
    val byCategory: List<CategorySpend> = emptyList(),
    val series: List<MonthPoint> = emptyList(),
) {
    val savings: Double get() = income - expense
}

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    repo: FinanceRepository,
    settings: SettingsRepository,
) : ViewModel() {

    private val start = DateUtil.startOfMonth()
    private val end = DateUtil.endOfMonth()

    // 6-month series (income/expense/net worth) converted to base; recomputes on data changes.
    private val seriesFlow = combine(
        settings.settings,
        repo.observeTransactions(),
        repo.observeRates(),
    ) { s, _, _ -> repo.monthlySeries(6, s.baseCurrency) }

    val state = combine(
        settings.settings,
        repo.observeTotalByType(TransactionType.INCOME.name, start, end),
        repo.observeTotalByType(TransactionType.EXPENSE.name, start, end),
        repo.observeSpendByCategory(start, end),
        seriesFlow,
    ) { s, income, expense, byCat, series ->
        AnalyticsUiState(
            baseCurrency = s.baseCurrency,
            monthLabel = DateUtil.month(start),
            income = income,
            expense = expense,
            byCategory = byCat,
            series = series,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AnalyticsUiState())
}
