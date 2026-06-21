package com.jinatra.finatra.ui.screens.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jinatra.finatra.data.local.dao.TransactionWithDetails
import com.jinatra.finatra.data.repository.FinanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the transactions list screen.
 *
 * Exposes the live transaction history filtered by a free-text search [query], and supports
 * deleting a transaction. The search matches the note, category name, account name, and tags.
 */
@HiltViewModel
class TransactionsViewModel @Inject constructor(
    private val repo: FinanceRepository,
) : ViewModel() {

    /** Current search text driving [transactions]; updated via [setQuery]. */
    val query = MutableStateFlow("")

    /**
     * Transactions from the repository, re-filtered whenever the source list or [query] changes.
     * A blank query passes everything through; otherwise rows are kept when the query is a
     * case-insensitive substring of the note, category name, account name, or tags.
     */
    val transactions = combine(repo.observeTransactions(), query) { list, q ->
        if (q.isBlank()) list
        else list.filter {
            it.note.contains(q, true) ||
                (it.categoryName?.contains(q, true) == true) ||
                (it.accountName?.contains(q, true) == true) ||
                it.tags.contains(q, true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Updates the active search [query]. */
    fun setQuery(q: String) { query.value = q }

    /**
     * Deletes the given transaction. Re-loads the full entity by id (the list item is a
     * read-only projection) before delegating to the repository with the current timestamp.
     */
    fun delete(tx: TransactionWithDetails) {
        viewModelScope.launch {
            // Resolve the projection back to its full entity, then delete it (timestamped for sync).
            repo.transactionById(tx.id)?.let { repo.deleteTransaction(it, System.currentTimeMillis()) }
        }
    }
}
