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
import com.jinatra.finatra.data.repository.FinanceRepository
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

data class BudgetProgress(
    val budget: BudgetEntity,
    val categoryName: String,
    val spent: Double,
)

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
) {
    val balanceLeft: Double get() = incomeThisMonth - expenseThisMonth
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repo: FinanceRepository,
    settings: SettingsRepository,
    private val ai: AiService,
) : ViewModel() {

    private val monthStart = DateUtil.startOfMonth()
    private val monthEnd = DateUtil.endOfMonth()

    private val insight = MutableStateFlow<String?>(null)
    fun dismissInsight() { insight.value = null }

    private val budgetsFlow = combine(
        repo.observeBudgets(),
        repo.observeCategories(),
    ) { budgets, cats ->
        budgets.map { b ->
            val cat = cats.firstOrNull { it.id == b.categoryId }
            val (start, end) = if (b.period.name == "MONTHLY") monthStart to monthEnd
                else b.startDate to (b.endDate ?: monthEnd)
            BudgetProgress(b, cat?.name ?: "—", repo.spentInCategory(b.categoryId, start, end))
        }
    }

    private data class MoneyAgg(
        val base: String,
        val netWorth: Double,
        val income: Double,
        val expense: Double,
        val cards: List<AccountCard>,
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
        )
    }

    val state = combine(
        moneyFlow,
        repo.observeRecent(6),
        budgetsFlow,
        insight,
        repo.observeRecurring(),
    ) { m, recent, budgets, ins, recurring ->
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
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())

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
