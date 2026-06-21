package com.jinatra.finatra.ui.screens.audit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jinatra.finatra.ui.components.EmptyState
import com.jinatra.finatra.ui.components.ExpressiveCard
import com.jinatra.finatra.ui.components.FinatraTopBar
import com.jinatra.finatra.util.DateUtil

/**
 * Read-only audit trail screen. Lists recorded transaction history events (action, affected
 * transaction id, timestamp and optional details) newest-first, or an empty state when none
 * exist. [onBack] navigates up.
 */
@Composable
fun AuditLogScreen(onBack: () -> Unit, vm: AuditLogViewModel = hiltViewModel()) {
    val entries by vm.entries.collectAsStateWithLifecycle()
    Scaffold(
        topBar = { FinatraTopBar("Audit log", onBack = onBack) },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
    ) { padding ->
        if (entries.isEmpty()) {
            EmptyState("No history yet.", Modifier.padding(padding))
        } else {
            LazyColumn(
                Modifier.fillMaxSize().padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(entries, key = { it.id }) { e ->
                    ExpressiveCard(modifier = Modifier.fillMaxWidth(), padding = 16.dp) {
                        Text("${e.action} · tx #${e.transactionId}", style = MaterialTheme.typography.bodyLarge)
                        Text(DateUtil.full(e.timestamp) + (if (e.details.isNotBlank()) " · ${e.details}" else ""),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
