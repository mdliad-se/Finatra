package com.jinatra.finatra.ui.screens.categories

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jinatra.finatra.ui.components.ColorPickerRow
import com.jinatra.finatra.ui.components.ExpressiveCard
import com.jinatra.finatra.ui.components.FinatraTopBar
import com.jinatra.finatra.ui.components.IconChip
import com.jinatra.finatra.ui.theme.FinatraTheme
import com.jinatra.finatra.util.CategoryIcons

/**
 * Categories management screen. Lists every category with its icon, color, and
 * income/expense flag, lets the user delete custom categories, and exposes a FAB
 * that opens a dialog for creating a new one (name, type, and color).
 *
 * @param onBack invoked when the top bar's back affordance is tapped.
 */
@Composable
fun CategoriesScreen(onBack: () -> Unit, vm: CategoriesViewModel = hiltViewModel()) {
    val cats by vm.categories.collectAsStateWithLifecycle()
    var showAdd by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
        topBar = { FinatraTopBar("Categories", onBack = onBack) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAdd = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) { Icon(Icons.Filled.Add, contentDescription = "Add") }
        },
    ) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(cats, key = { it.id }) { c ->
                ExpressiveCard(modifier = Modifier.fillMaxWidth(), padding = 0.dp) {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        IconChip(
                            icon = CategoryIcons.forKey(c.iconKey),
                            tint = Color(c.colorHex),
                            size = 40.dp,
                            background = Color(c.colorHex).copy(alpha = 0.2f),
                        )
                        Column(Modifier.weight(1f).padding(start = 12.dp)) {
                            Text(c.name, style = MaterialTheme.typography.bodyLarge)
                            Text(if (c.isIncome) "Income" else "Expense",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (c.isIncome) FinatraTheme.income else FinatraTheme.expense)
                        }
                        // Only user-created categories can be removed; built-in ones are protected.
                        if (c.isCustom) {
                            IconButton(onClick = { vm.delete(c) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAdd) {
        var name by remember { mutableStateOf("") }
        var income by remember { mutableStateOf(false) }
        var color by remember { mutableLongStateOf(0xFFE05454) }
        AlertDialog(
            onDismissRequest = { showAdd = false },
            title = { Text("New category") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(name, { name = it }, label = { Text("Name") }, shape = MaterialTheme.shapes.medium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = !income, onClick = { income = false }, label = { Text("Expense") })
                        FilterChip(selected = income, onClick = { income = true }, label = { Text("Income") })
                    }
                    ColorPickerRow(selected = color, onSelect = { color = it })
                }
            },
            confirmButton = {
                TextButton(onClick = { vm.add(name, income, color); showAdd = false }) { Text("Add") }
            },
            dismissButton = { TextButton(onClick = { showAdd = false }) { Text("Cancel") } },
        )
    }
}
