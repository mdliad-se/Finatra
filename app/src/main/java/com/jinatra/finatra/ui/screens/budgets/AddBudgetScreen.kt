package com.jinatra.finatra.ui.screens.budgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jinatra.finatra.data.local.entity.BudgetPeriod
import com.jinatra.finatra.ui.components.EmptyState
import com.jinatra.finatra.ui.components.ExpressiveCard
import com.jinatra.finatra.ui.components.FinatraTopBar
import com.jinatra.finatra.ui.components.LabeledDropdown

/**
 * Form for creating or editing a single budget (one spending limit per category and period).
 * Lets the user pick an expense category, enter a limit amount and choose the period
 * (Monthly or Custom). Requires at least one expense category; [onDone] is invoked after a
 * successful save or when navigating back.
 */
@Composable
fun AddBudgetScreen(
    onDone: () -> Unit,
    vm: AddBudgetViewModel = hiltViewModel(),
) {
    val s by vm.state.collectAsStateWithLifecycle()
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
        topBar = { FinatraTopBar(if (s.isEditing) "Edit budget" else "Add budget", onBack = onDone) },
    ) { padding ->
        if (s.categories.isEmpty()) {
            EmptyState("Create an expense category first.", Modifier.padding(padding))
            return@Scaffold
        }
        Column(Modifier.fillMaxWidth().padding(padding).padding(16.dp)) {
            ExpressiveCard {
                LabeledDropdown(
                    label = "Category",
                    options = s.categories,
                    selected = s.categories.firstOrNull { it.id == s.categoryId } ?: s.categories.first(),
                    optionLabel = { it.name },
                    onSelect = { vm.setCategory(it.id) },
                )
                Spacer(Modifier.height(14.dp))
                OutlinedTextField(
                    s.amount, vm::setAmount,
                    label = { Text("Limit amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BudgetPeriod.entries.forEach { p ->
                        FilterChip(
                            selected = s.period == p,
                            onClick = { vm.setPeriod(p) },
                            label = { Text(if (p == BudgetPeriod.MONTHLY) "Monthly" else "Custom") },
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                Button(onClick = { vm.save(onDone) }, enabled = s.amount.toDoubleOrNull() != null, modifier = Modifier.fillMaxWidth()) {
                    Text(if (s.isEditing) "Save" else "Add budget")
                }
            }
        }
    }
}
