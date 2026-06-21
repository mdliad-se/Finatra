package com.jinatra.finatra.ui.screens.audit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jinatra.finatra.data.repository.FinanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Backs [AuditLogScreen] by exposing the repository's audit-log stream as lifecycle-aware
 * state. Read-only: it observes entries but never mutates them.
 */
@HiltViewModel
class AuditLogViewModel @Inject constructor(repo: FinanceRepository) : ViewModel() {
    val entries = repo.observeAudit()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
