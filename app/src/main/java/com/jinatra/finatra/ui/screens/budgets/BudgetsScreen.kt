package com.jinatra.finatra.ui.screens.budgets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jinatra.finatra.ui.components.ExpressiveCard
import com.jinatra.finatra.ui.components.OverlineLabel
import com.jinatra.finatra.ui.theme.FinatraTheme
import com.jinatra.finatra.util.Money

@Composable
fun BudgetsScreen(
    onAdd: () -> Unit,
    onEdit: (Long) -> Unit,
    vm: BudgetsViewModel = hiltViewModel(),
) {
    val s by vm.state.collectAsStateWithLifecycle()
    val suggestions by vm.suggestions.collectAsStateWithLifecycle()
    val suggesting by vm.suggesting.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAdd,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) { Icon(Icons.Filled.Add, contentDescription = "Add budget") }
        }
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Budgets", style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    if (vm.aiAvailable) {
                        TextButton(onClick = { vm.recommend() }, enabled = !suggesting) {
                            if (suggesting) {
                                CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                            Spacer(Modifier.size(6.dp))
                            Text("AI suggest")
                        }
                    }
                }
            }

            if (suggestions.isNotEmpty()) {
                item {
                    ExpressiveCard(Modifier.fillMaxWidth(), container = MaterialTheme.colorScheme.secondaryContainer) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            OverlineLabel("AI recommended limits")
                            TextButton(onClick = { vm.applyAll() }) { Text("Add all") }
                        }
                        suggestions.forEach { sg ->
                            Row(
                                Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(sg.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                                    Text("avg ${Money.format(sg.currentAvg, s.baseCurrency)}/mo",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                OutlinedButton(onClick = { vm.applySuggestion(sg) }) {
                                    Text(Money.format(sg.suggested, s.baseCurrency))
                                }
                            }
                        }
                        TextButton(onClick = { vm.dismissSuggestions() }) { Text("Dismiss") }
                    }
                }
            }

            if (s.rows.isEmpty()) {
                item {
                    Text(
                        "No budgets yet. Tap + to set a monthly limit per category" +
                            (if (vm.aiAvailable) ", or use AI suggest." else "."),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 24.dp),
                    )
                }
            } else {
                items(s.rows, key = { it.budget.id }) { row ->
                    BudgetCard(row, s.baseCurrency) { onEdit(row.budget.id) }
                }
            }
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun BudgetCard(row: BudgetRow, currency: String, onClick: () -> Unit) {
    ExpressiveCard(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(row.categoryName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            val status = when {
                row.isOver -> "Over"
                row.isWarning -> "Warning"
                else -> "On track"
            }
            Text(status, style = MaterialTheme.typography.labelMedium,
                color = if (row.isOver) FinatraTheme.expense else MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(10.dp))
        LinearProgressIndicator(
            progress = { row.fraction },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
            color = if (row.isOver) FinatraTheme.expense else MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "${Money.format(row.spent, currency)} of ${Money.format(row.budget.amount, currency)}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
