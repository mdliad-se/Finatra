package com.jinatra.finatra.ui.screens.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jinatra.finatra.data.local.entity.AccountEntity
import com.jinatra.finatra.data.local.entity.TransactionType
import com.jinatra.finatra.data.repository.FinanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** An account paired with its running balance derived from all transactions. */
data class AccountWithBalance(val account: AccountEntity, val balance: Double)

/**
 * Backs [AccountsScreen]. Exposes every account with a live balance computed from its opening
 * balance plus the effect of all transactions, and supports deleting an account.
 */
@HiltViewModel
class AccountsViewModel @Inject constructor(
    private val repo: FinanceRepository,
) : ViewModel() {

    // Recompute each account's balance whenever accounts or transactions change.
    val accounts = combine(
        repo.observeAccounts(),
        repo.observeTransactions(),
    ) { accounts, txns ->
        accounts.map { acc ->
            var bal = acc.openingBalance
            txns.forEach { t ->
                // Apply each transaction's signed effect; transfers move money between two accounts.
                when (runCatching { TransactionType.valueOf(t.type) }.getOrNull()) {
                    TransactionType.INCOME -> if (t.accountId == acc.id) bal += t.amount
                    TransactionType.EXPENSE -> if (t.accountId == acc.id) bal -= t.amount
                    TransactionType.TRANSFER -> {
                        if (t.accountId == acc.id) bal -= t.amount
                        if (t.transferToAccountId == acc.id) bal += t.amount
                    }
                    null -> {}
                }
            }
            AccountWithBalance(acc, bal)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun delete(account: AccountEntity) {
        viewModelScope.launch { repo.deleteAccount(account) }
    }
}
