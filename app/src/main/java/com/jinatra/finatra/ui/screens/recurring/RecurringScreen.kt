package com.jinatra.finatra.ui.screens.recurring

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jinatra.finatra.data.local.entity.RecurrenceFrequency
import com.jinatra.finatra.ui.components.EmptyState
import com.jinatra.finatra.ui.components.ExpressiveCard
import com.jinatra.finatra.ui.components.FinatraTopBar
import com.jinatra.finatra.ui.components.LabeledDropdown
import com.jinatra.finatra.util.DateUtil
import com.jinatra.finatra.util.Money

/**
 * Screen listing the user's recurring transaction templates.
 *
 * Shows each template (note, formatted amount, cadence and next-run date) with a delete
 * action, and a floating action button that opens a dialog to create a new one. The FAB
 * and add dialog are only available once at least one account exists, since a recurring
 * transaction must be tied to an account.
 *
 * @param onBack invoked when the user navigates back.
 * @param vm screen state/actions; injected via Hilt by default.
 */
@Composable
fun RecurringScreen(onBack: () -> Unit, vm: RecurringViewModel = hiltViewModel()) {
    val s by vm.state.collectAsStateWithLifecycle()
    // Controls visibility of the "new recurring" dialog.
    var showAdd by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { FinatraTopBar("Recurring", onBack = onBack) },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            if (s.accounts.isNotEmpty()) {
                FloatingActionButton(
                    onClick = { showAdd = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ) { Icon(Icons.Filled.Add, contentDescription = "Add") }
            }
        },
    ) { padding ->
        if (s.items.isEmpty()) {
            EmptyState("No recurring transactions.", Modifier.padding(padding))
        } else {
            LazyColumn(Modifier.fillMaxSize().padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(s.items, key = { it.id }) { r ->
                    ExpressiveCard(modifier = Modifier.fillMaxWidth(), padding = 16.dp) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(r.note.ifBlank { "Recurring" }, style = MaterialTheme.typography.titleMedium)
                                Text("${Money.format(r.amount, r.currency)} · ${r.frequency.name.lowercase()} · next ${DateUtil.day(r.nextRun)}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton(onClick = { vm.delete(r) }) { Icon(Icons.Filled.Delete, contentDescription = "Delete") }
                        }
                    }
                }
            }
        }
    }

    // New-recurring dialog: collects amount, note, account, cadence and the auto-log/remind
    // choice, then delegates creation to the ViewModel. Local form state lives here so it
    // resets each time the dialog is reopened.
    if (showAdd && s.accounts.isNotEmpty()) {
        var amount by remember { mutableStateOf("") }
        var note by remember { mutableStateOf("") }
        var account by remember { mutableStateOf(s.accounts.first()) }
        var freq by remember { mutableStateOf(RecurrenceFrequency.MONTHLY) }
        // true = auto-log the transaction when due; false = only remind.
        var autoLog by remember { mutableStateOf(true) }
        AlertDialog(
            onDismissRequest = { showAdd = false },
            title = { Text("New recurring") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(amount, { amount = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Amount") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                    OutlinedTextField(note, { note = it }, label = { Text("Note") })
                    LabeledDropdown("Account", s.accounts, account, { it.name }, { account = it })
                    LabeledDropdown("Frequency", RecurrenceFrequency.entries, freq,
                        { it.name.lowercase().replaceFirstChar { c -> c.uppercase() } }, { freq = it })
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        FilterChip(selected = autoLog, onClick = { autoLog = true }, label = { Text("Auto-log") })
                        Text("  ")
                        FilterChip(selected = !autoLog, onClick = { autoLog = false }, label = { Text("Remind") })
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    // Only create if the amount parses to a number; otherwise keep the dialog open.
                    val a = amount.toDoubleOrNull()
                    if (a != null) { vm.add(a, account.id, account.currency, note, freq, autoLog); showAdd = false }
                }) { Text("Add") }
            },
            dismissButton = { TextButton(onClick = { showAdd = false }) { Text("Cancel") } },
        )
    }
}
