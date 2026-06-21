package com.jinatra.finatra.ui.screens.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jinatra.finatra.ui.components.EmptyState
import com.jinatra.finatra.ui.components.ExpressiveCard
import com.jinatra.finatra.ui.components.OverlineLabel
import com.jinatra.finatra.ui.components.TransactionRow
import com.jinatra.finatra.util.DateUtil

/**
 * Transactions list screen.
 *
 * Renders the searchable, filterable transaction history grouped by day. The search query is
 * owned by [TransactionsViewModel]; the quick filter ("All" / "This Month" / "Income" /
 * "Expenses") is local UI state applied on top of the already-searched list. Each row can be
 * swiped end-to-start to delete it.
 *
 * @param onAdd invoked when the floating action button is tapped to create a new transaction.
 * @param onEdit invoked with a transaction id when a row is tapped to edit it.
 * @param vm the screen's [TransactionsViewModel], provided by Hilt.
 */
@Composable
fun TransactionsScreen(
    onAdd: () -> Unit,
    onEdit: (Long) -> Unit,
    vm: TransactionsViewModel = hiltViewModel(),
) {
    val txns by vm.transactions.collectAsStateWithLifecycle()
    val query by vm.query.collectAsStateWithLifecycle()
    var selectedFilter by remember { mutableStateOf("All") }

    // Apply the active quick filter to the (already search-filtered) list from the ViewModel.
    val filteredTxns = remember(txns, selectedFilter) {
        when (selectedFilter) {
            "Income" -> txns.filter { it.type == "INCOME" }
            "Expenses" -> txns.filter { it.type == "EXPENSE" }
            "This Month" -> {
                val start = DateUtil.startOfMonth()
                val end = DateUtil.endOfMonth()
                txns.filter { it.dateTime in start..end }
            }
            else -> txns
        }
    }

    // Group the visible transactions into day buckets, newest day first, for section headers.
    val groups = remember(filteredTxns) {
        filteredTxns.groupBy { DateUtil.startOfDay(it.dateTime) }
            .toList()
            .sortedByDescending { it.first }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAdd,
                shape = RoundedCornerShape(24.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) { Icon(Icons.Filled.Add, contentDescription = "Add") }
        }
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Transactions",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = vm::setQuery,
                    placeholder = { Text("Search transactions...") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                        unfocusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    val filters = listOf("All", "This Month", "Income", "Expenses")
                    items(filters) { filter ->
                        val active = selectedFilter == filter
                        val containerColor = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                        val contentColor = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        
                        Box(
                            Modifier
                                .clip(RoundedCornerShape(50))
                                .background(containerColor)
                                .clickable { selectedFilter = filter }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = filter,
                                color = contentColor,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            if (filteredTxns.isEmpty()) {
                item { EmptyState("No transactions match.") }
            } else {
                groups.forEach { (dayStart, dayTxns) ->
                    item(key = "h-$dayStart") {
                        OverlineLabel(
                            DateUtil.groupLabel(dayStart),
                            modifier = Modifier.padding(top = 8.dp, start = 4.dp, bottom = 2.dp),
                        )
                    }
                    item(key = "g-$dayStart") {
                        ExpressiveCard(Modifier.fillMaxWidth(), padding = 0.dp) {
                            dayTxns.forEachIndexed { i, tx ->
                                // Only a right-to-left (EndToStart) swipe deletes; confirm it so the row dismisses.
                                val dismissState = rememberSwipeToDismissBoxState(
                                    confirmValueChange = {
                                        if (it == SwipeToDismissBoxValue.EndToStart) { vm.delete(tx); true } else false
                                    }
                                )
                                SwipeToDismissBox(
                                    state = dismissState,
                                    enableDismissFromStartToEnd = false,
                                    backgroundContent = {
                                        Box(
                                            Modifier.fillMaxSize().background(MaterialTheme.colorScheme.errorContainer)
                                                .padding(horizontal = 20.dp),
                                            contentAlignment = Alignment.CenterEnd,
                                        ) {
                                            Icon(
                                                Icons.Filled.Delete, contentDescription = "Delete",
                                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                            )
                                        }
                                    },
                                ) {
                                    Box(
                                        Modifier.background(MaterialTheme.colorScheme.surfaceContainerLow)
                                            .padding(horizontal = 12.dp),
                                    ) {
                                        TransactionRow(tx) { onEdit(tx.id) }
                                    }
                                }
                                if (i < dayTxns.lastIndex) {
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                                }
                            }
                        }
                    }
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}
