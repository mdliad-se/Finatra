package com.jinatra.finatra.ui.screens.recurring

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jinatra.finatra.data.local.entity.AccountEntity
import com.jinatra.finatra.data.local.entity.RecurrenceFrequency
import com.jinatra.finatra.data.local.entity.RecurringTransactionEntity
import com.jinatra.finatra.data.local.entity.TransactionType
import com.jinatra.finatra.data.repository.FinanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RecurringUiState(
    val items: List<RecurringTransactionEntity> = emptyList(),
    val accounts: List<AccountEntity> = emptyList(),
)

@HiltViewModel
class RecurringViewModel @Inject constructor(
    private val repo: FinanceRepository,
) : ViewModel() {

    val state = combine(repo.observeRecurring(), repo.observeAccounts()) { items, accounts ->
        RecurringUiState(items, accounts)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), RecurringUiState())

    fun add(amount: Double, accountId: Long, currency: String, note: String, freq: RecurrenceFrequency, autoLog: Boolean) {
        val now = System.currentTimeMillis()
        val next = when (freq) {
            RecurrenceFrequency.DAILY -> now + 86_400_000L
            RecurrenceFrequency.WEEKLY -> now + 7 * 86_400_000L
            RecurrenceFrequency.MONTHLY -> now + 30 * 86_400_000L
            RecurrenceFrequency.CUSTOM -> now + 86_400_000L
        }
        viewModelScope.launch {
            repo.upsertRecurring(
                RecurringTransactionEntity(
                    type = TransactionType.EXPENSE, amount = amount, currency = currency,
                    accountId = accountId, note = note, frequency = freq, nextRun = next, autoLog = autoLog,
                )
            )
        }
    }

    fun delete(r: RecurringTransactionEntity) { viewModelScope.launch { repo.deleteRecurring(r) } }
}
