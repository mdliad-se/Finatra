package com.jinatra.finatra.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jinatra.finatra.data.ai.AiService
import com.jinatra.finatra.data.repository.AccountCard
import com.jinatra.finatra.data.local.dao.TransactionWithDetails
import com.jinatra.finatra.data.local.entity.BudgetEntity
import com.jinatra.finatra.data.local.entity.RecurringTransactionEntity
import com.jinatra.finatra.data.local.entity.TransactionType
import com.jinatra.finatra.data.prefs.SettingsRepository
import com.jinatra.finatra.data.prefs.ThemeMode
import com.jinatra.finatra.data.repository.FinanceRepository
import com.jinatra.finatra.data.repository.HealthScore
import com.jinatra.finatra.util.DateUtil
import com.jinatra.finatra.util.Money
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Pairs a budget with its resolved category name and the amount spent against it
 * in the relevant period, ready for display on the dashboard.
 */
data class BudgetProgress(
    val budget: BudgetEntity,
    val categoryName: String,
    val spent: Double,
)

/**
 * Aggregated state for the home dashboard. [balanceLeft] is a derived value
 * (income minus expense this month) rather than a stored field.
 */
data class HomeUiState(
    val baseCurrency: String = "USD",
    val netWorth: Double = 0.0,
    val incomeThisMonth: Double = 0.0,
    val expenseThisMonth: Double = 0.0,
    val recent: List<TransactionWithDetails> = emptyList(),
    val budgets: List<BudgetProgress> = emptyList(),
    val upcoming: List<RecurringTransactionEntity> = emptyList(),
    val accounts: List<AccountCard> = emptyList(),
    val aiInsight: String? = null,
    val health: HealthScore? = null,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val userName: String = "User",
) {
    val balanceLeft: Double get() = incomeThisMonth - expenseThisMonth
}

/**
 * ViewModel for the home dashboard. Fans several repository streams (accounts, transactions,
 * budgets, recurring, exchange rates, settings) into a single [HomeUiState], converting all
 * money to the user's base currency. Also owns the dismissable AI insight and the theme toggle.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repo: FinanceRepository,
    private val settings: SettingsRepository,
    private val ai: AiService,
) : ViewModel() {

    // Current-month window reused across all the monthly aggregations below.
    private val monthStart = DateUtil.startOfMonth()
    private val monthEnd = DateUtil.endOfMonth()

    // One-shot AI insight; null once dismissed or before it loads.
    private val insight = MutableStateFlow<String?>(null)
    /** Dismisses the AI insight banner. */
    fun dismissInsight() { insight.value = null }

    // Joins each budget with its category name and the amount spent in its active period.
    private val budgetsFlow = combine(
        repo.observeBudgets(),
        repo.observeCategories(),
    ) { budgets, cats ->
        budgets.map { b ->
            val cat = cats.firstOrNull { it.id == b.categoryId }
            // Monthly budgets use the current month window; others use their own date range.
            val (start, end) = if (b.period.name == "MONTHLY") monthStart to monthEnd
                else b.startDate to (b.endDate ?: monthEnd)
            BudgetProgress(b, cat?.name ?: "—", repo.spentInCategory(b.categoryId, start, end))
        }
    }

    // Bundle of all currency-converted money figures derived together in [moneyFlow].
    private data class MoneyAgg(
        val base: String,
        val netWorth: Double,
        val income: Double,
        val expense: Double,
        val cards: List<AccountCard>,
        val health: HealthScore,
    )

    // Re-derives whenever accounts, transactions, rates, or base currency change.
    // All amounts are converted to the user's base currency via stored manual rates.
    private val moneyFlow = combine(
        settings.settings,
        repo.observeAccounts(),
        repo.observeTransactions(),
        repo.observeRates(),
    ) { s, _, _, _ ->
        val base = s.baseCurrency
        MoneyAgg(
            base = base,
            netWorth = repo.convertedNetWorth(base),
            income = repo.convertedTotalByType(TransactionType.INCOME.name, monthStart, monthEnd, base),
            expense = repo.convertedTotalByType(TransactionType.EXPENSE.name, monthStart, monthEnd, base),
            cards = repo.accountCards(monthStart, monthEnd),
            health = repo.financeHealth(base),
        )
    }

    val state = combine(
        moneyFlow,
        repo.observeRecent(6),
        budgetsFlow,
        insight,
        combine(repo.observeRecurring(), settings.settings) { recurring, set -> recurring to set }
    ) { m, recent, budgets, ins, recurringAndSet ->
        val (recurring, set) = recurringAndSet
        HomeUiState(
            baseCurrency = m.base,
            netWorth = m.netWorth,
            incomeThisMonth = m.income,
            expenseThisMonth = m.expense,
            recent = recent,
            budgets = budgets,
            upcoming = recurring.sortedBy { it.nextRun }.take(3),
            accounts = m.cards,
            aiInsight = ins,
            health = m.health,
            themeMode = set.themeMode,
            userName = set.userName,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())

    /** Cycles the theme: Light -> Dark -> System -> Light. */
    fun toggleTheme() {
        viewModelScope.launch {
            val current = state.value.themeMode
            val next = when (current) {
                ThemeMode.LIGHT -> ThemeMode.DARK
                ThemeMode.DARK -> ThemeMode.SYSTEM
                ThemeMode.SYSTEM -> ThemeMode.LIGHT
            }
            settings.setThemeMode(next)
        }
    }

    // On first load, if AI is configured, fetch a one-shot spending insight from a
    // summary of this month's income, expense, and top spending category.
    init {
        if (ai.isConfigured()) viewModelScope.launch {
            val m = moneyFlow.first()
            val top = repo.observeSpendByCategory(monthStart, monthEnd).first().firstOrNull()
            val summary = "income ${Money.format(m.income, m.base)}, expense ${Money.format(m.expense, m.base)}" +
                (top?.let { ", top category ${it.categoryName ?: "Other"} ${Money.format(it.total, m.base)}" } ?: "")
            insight.value = ai.spendingInsight(summary)
        }
    }
}
