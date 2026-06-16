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

@HiltViewModel
class TransactionsViewModel @Inject constructor(
    private val repo: FinanceRepository,
) : ViewModel() {

    val query = MutableStateFlow("")

    val transactions = combine(repo.observeTransactions(), query) { list, q ->
        if (q.isBlank()) list
        else list.filter {
            it.note.contains(q, true) ||
                (it.categoryName?.contains(q, true) == true) ||
                (it.accountName?.contains(q, true) == true) ||
                it.tags.contains(q, true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setQuery(q: String) { query.value = q }

    fun delete(tx: TransactionWithDetails) {
        viewModelScope.launch {
            repo.transactionById(tx.id)?.let { repo.deleteTransaction(it, System.currentTimeMillis()) }
        }
    }
}
