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
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import java.util.Calendar
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.jinatra.finatra.ui.components.SpendingRing
import com.jinatra.finatra.ui.components.RingSegment
import com.jinatra.finatra.ui.theme.WarmRed
import com.jinatra.finatra.util.DateUtil
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.ExpandMore as ExpandMoreIcon
import androidx.compose.material3.IconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape

/**
 * Budgets overview for the current month. Shows an overall spend-vs-budget donut summary,
 * an entry point to the AI budget planner, a per-category list with progress bars, and a
 * cash-flow forecast card. [onAdd]/[onEdit] navigate to the add/edit form and [onAiSuggest]
 * opens the conversational budget planner.
 */
@Composable
fun BudgetsScreen(
    onAdd: () -> Unit,
    onEdit: (Long) -> Unit,
    onAiSuggest: () -> Unit,
    vm: BudgetsViewModel = hiltViewModel(),
) {
    val s by vm.state.collectAsStateWithLifecycle()
    // Non-null while a delete confirmation is showing for that budget row.
    var pendingDelete by remember { mutableStateOf<BudgetRow?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAdd,
                shape = RoundedCornerShape(24.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) { Icon(Icons.Filled.Add, contentDescription = "Add budget") }
        }
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Spacer(Modifier.height(8.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${DateUtil.month(System.currentTimeMillis()).split(" ").first()} Budgets",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = onAdd) {
                        Icon(Icons.Filled.Add, contentDescription = "Add Budget")
                    }
                }
            }

            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    var periodExpanded by remember { mutableStateOf(false) }
                    var selectedPeriod by remember { mutableStateOf("This Month") }
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(50))
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                            .clickable { periodExpanded = !periodExpanded }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                selectedPeriod,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(Modifier.width(4.dp))
                            Icon(Icons.Filled.ExpandMore, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // Overall Summary Card with Donut Chart
            // Aggregate every budget row into headline totals and the share of budget used so far.
            val totalBudget = s.rows.sumOf { it.budget.amount }
            val totalSpent = s.rows.sumOf { it.spent }
            val percentUsed = if (totalBudget > 0) ((totalSpent / totalBudget) * 100).toInt() else 0
            // Days left in the calendar month, used as a rough "time remaining in period" hint.
            val cal = Calendar.getInstance()
            val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
            val currentDay = cal.get(Calendar.DAY_OF_MONTH)
            val daysRemaining = (maxDays - currentDay).coerceAtLeast(0)

            item {
                ExpressiveCard(Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (totalBudget > 0.0) {
                            // Two-segment donut: spent vs. remaining (clamped so overspend doesn't go negative).
                            SpendingRing(
                                segments = listOf(
                                    RingSegment("Spent", totalSpent, MaterialTheme.colorScheme.primary),
                                    RingSegment("Remaining", (totalBudget - totalSpent).coerceAtLeast(0.0), MaterialTheme.colorScheme.surfaceVariant)
                                ),
                                centerTop = "Used",
                                centerValue = "$percentUsed%",
                                modifier = Modifier.fillMaxWidth().height(160.dp),
                                showPercentLabels = false
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${Money.format(totalSpent, s.baseCurrency)} of ${Money.format(totalBudget, s.baseCurrency)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "$daysRemaining days left in period",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            Text("No active budgets yet.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // Full-width "AI suggest budgets"
            item {
                Button(
                    onClick = onAiSuggest,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(8.dp))
                    Text("AI suggest budgets")
                }
            }

            if (s.rows.isEmpty()) {
                item {
                    Text(
                        "No budgets yet. Tap + to set a monthly limit per category, or use AI suggest budgets.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 24.dp),
                    )
                }
            } else {
                items(s.rows, key = { it.budget.id }) { row ->
                    BudgetCard(row, s.baseCurrency, onClick = { onEdit(row.budget.id) }, onDelete = { pendingDelete = row })
                }
            }

            // Cash Flow Forecast Card
            item {
                // Placeholder projected balance heuristic until a real forecast is wired in.
                val expectedBalance = if (totalBudget > 0.0) totalBudget * 1.5 else 3240.0
                ForecastCard(expectedBalance, s.baseCurrency)
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }

    // Confirm before removing a budget — deletion is immediate and not undoable from here.
    pendingDelete?.let { row ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete budget?") },
            text = { Text("Remove the ${row.categoryName} budget? Your transactions are not affected.") },
            confirmButton = {
                TextButton(onClick = { vm.delete(row.budget); pendingDelete = null }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text("Cancel") } },
        )
    }
}

/**
 * Single budget list item: category name, spent-of-limit text, days remaining and a progress
 * bar whose colour escalates green → amber → red as spend approaches the limit. Tapping it
 * triggers [onClick] (edit).
 */
@Composable
private fun BudgetCard(row: BudgetRow, currency: String, onClick: () -> Unit, onDelete: () -> Unit) {
    val fraction = row.fraction
    // Traffic-light progress colour by how much of the budget is consumed.
    val progressColor = when {
        fraction < 0.7f -> Color(0xFF7ABE5A)
        fraction < 0.9f -> Color(0xFFE8B85A)
        else -> Color(0xFFE05454)
    }

    val cal = Calendar.getInstance()
    val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val currentDay = cal.get(Calendar.DAY_OF_MONTH)
    val daysRemaining = (maxDays - currentDay).coerceAtLeast(0)

    ExpressiveCard(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text(row.categoryName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(2.dp))
                Text(
                    "${Money.format(row.spent, currency)} / ${Money.format(row.budget.amount, currency)} spent",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "$daysRemaining days left",
                    style = MaterialTheme.typography.labelSmall,
                    color = progressColor,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(
                        Icons.Filled.DeleteOutline,
                        contentDescription = "Delete ${row.categoryName} budget",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        LinearProgressIndicator(
            progress = { fraction },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
            color = progressColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    }
}

/** 30-day cash-flow forecast card: a decorative trend curve plus the expected end-of-month balance. */
@Composable
private fun ForecastCard(expectedBalance: Double, currency: String) {
    ExpressiveCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(8.dp)) {
            Text("30-Day Forecast", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))

            androidx.compose.foundation.Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
            ) {
                val width = size.width
                val height = size.height
                val path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(0f, height * 0.8f)
                    cubicTo(
                        width * 0.25f, height * 0.75f,
                        width * 0.5f, height * 0.6f,
                        width * 0.75f, height * 0.4f
                    )
                    cubicTo(
                        width * 0.85f, height * 0.5f,
                        width * 0.95f, height * 0.2f,
                        width, height * 0.15f
                    )
                }
                drawPath(
                    path = path,
                    color = WarmRed,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                )
                val fillPath = androidx.compose.ui.graphics.Path().apply {
                    addPath(path)
                    lineTo(width, height)
                    lineTo(0f, height)
                    close()
                }
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(WarmRed.copy(alpha = 0.15f), Color.Transparent)
                    )
                )
                drawCircle(
                    color = WarmRed,
                    radius = 5.dp.toPx(),
                    center = Offset(width, height * 0.15f)
                )
            }
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(Modifier.height(8.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val currentMonth = DateUtil.month(System.currentTimeMillis()).split(" ").first()
                Text("Expected balance $currentMonth 31", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(Money.format(expectedBalance, currency), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
