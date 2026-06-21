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

/**
 * Immutable UI state for the Recurring transactions screen.
 *
 * @property items the user's configured recurring transactions.
 * @property accounts available accounts, used to populate the "new recurring" form
 *   (and to decide whether the add action can be offered at all).
 */
data class RecurringUiState(
    val items: List<RecurringTransactionEntity> = emptyList(),
    val accounts: List<AccountEntity> = emptyList(),
)

/**
 * ViewModel backing [com.jinatra.finatra.ui.screens.recurring.RecurringScreen].
 *
 * Exposes the list of recurring transaction templates and the accounts they can be
 * attached to, and provides actions to create and delete those templates. The actual
 * firing of due recurrences (auto-logging / reminders) is handled elsewhere; this screen
 * only manages the definitions.
 */
@HiltViewModel
class RecurringViewModel @Inject constructor(
    private val repo: FinanceRepository,
) : ViewModel() {

    // Merge recurring templates and accounts into a single state stream, kept hot
    // for 5s after the last collector unsubscribes to survive config changes.
    val state = combine(repo.observeRecurring(), repo.observeAccounts()) { items, accounts ->
        RecurringUiState(items, accounts)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), RecurringUiState())

    /**
     * Creates a new recurring expense template.
     *
     * @param autoLog when true the recurrence posts the transaction automatically on its
     *   due date; when false it only raises a reminder.
     */
    fun add(amount: Double, accountId: Long, currency: String, note: String, freq: RecurrenceFrequency, autoLog: Boolean) {
        val now = System.currentTimeMillis()
        // Compute the first fire time from the chosen cadence (calendar-approximate:
        // a month is treated as 30 days; CUSTOM defaults to a daily step here).
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

    /** Permanently removes a recurring template. */
    fun delete(r: RecurringTransactionEntity) { viewModelScope.launch { repo.deleteRecurring(r) } }
}
