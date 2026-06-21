package com.jinatra.finatra.ui.screens.analytics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Alignment
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.HorizontalDivider
import com.jinatra.finatra.data.repository.MonthPoint
import com.jinatra.finatra.data.repository.MonthlyForecast
import com.jinatra.finatra.ui.components.BarGroup
import com.jinatra.finatra.ui.components.CategoryStat
import com.jinatra.finatra.ui.components.CategoryStatGrid
import com.jinatra.finatra.ui.components.EmptyState
import com.jinatra.finatra.ui.components.ExpressiveCard
import com.jinatra.finatra.ui.components.IncomeExpenseBars
import com.jinatra.finatra.ui.components.LabeledDropdown
import com.jinatra.finatra.ui.components.NetWorthLine
import com.jinatra.finatra.ui.components.OverlineLabel
import com.jinatra.finatra.ui.components.RingSegment
import com.jinatra.finatra.ui.components.SectionHeader
import com.jinatra.finatra.ui.components.SpendingRing
import com.jinatra.finatra.ui.components.chartColor
import com.jinatra.finatra.ui.theme.FinatraTheme
import com.jinatra.finatra.util.Money
import kotlin.math.roundToInt

/**
 * Top-level analytics dashboard for the current month.
 *
 * Presents financial insights across four tabbed sections (Overview, Categories,
 * Trends, Reports): income/expense/savings stats, a savings-rate gauge, spending
 * breakdown rings, net-worth trend, end-of-month forecast, a what-if savings
 * simulator and a multi-month summary report. All data is collected from
 * [AnalyticsViewModel] as lifecycle-aware state.
 */
@Composable
fun AnalyticsScreen(vm: AnalyticsViewModel = hiltViewModel()) {
    val s by vm.state.collectAsStateWithLifecycle()
    val whatIf by vm.whatIf.collectAsStateWithLifecycle()
    // Sum of category spend, reused for the ring center value and per-category percentages.
    val totalSpend = s.byCategory.sumOf { it.total }
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Overview", "Categories", "Trends", "Reports")

    LazyColumn(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Spacer(Modifier.height(8.dp))
            Text(
                "Analytics",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                s.monthLabel,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // Horizontal custom segmented control / tab chips
        item {
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(MaterialTheme.colorScheme.surfaceContainerLow).padding(6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                tabs.forEachIndexed { index, title ->
                    val active = selectedTab == index
                    val container = if (active) MaterialTheme.colorScheme.primary else Color.Transparent
                    val content = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    
                    Box(
                        Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(container)
                            .clickable { selectedTab = index }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = content
                        )
                    }
                }
            }
        }

        when (selectedTab) {
            0 -> { // OVERVIEW
                item { StatStrip(s.income, s.expense, s.savings, s.baseCurrency) }
                
                // Savings Rate circular gauge
                item {
                    // Savings as a percentage of income, clamped to 0–100 (guards against zero income).
                    val savingsRate = if (s.income > 0) ((s.savings / s.income) * 100).coerceIn(0.0, 100.0).roundToInt() else 0
                    ExpressiveCard(Modifier.fillMaxWidth()) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                            OverlineLabel("Savings Rate")
                            Spacer(Modifier.height(16.dp))
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(100.dp)) {
                                CircularProgressIndicator(
                                    progress = { savingsRate / 100f },
                                    color = Color(0xFF5AB8A8),
                                    strokeWidth = 8.dp,
                                    modifier = Modifier.fillMaxSize(),
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                                Text("$savingsRate%", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.height(12.dp))
                            Text("Savings Rate", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                if (s.series.size >= 2) {
                    item {
                        ExpressiveCard(Modifier.fillMaxWidth()) {
                            OverlineLabel("Income vs expense")
                            Spacer(Modifier.height(16.dp))
                            IncomeExpenseBars(
                                groups = s.series.map { BarGroup(it.label, it.income, it.expense) },
                                incomeColor = Color(0xFF5AB8A8), // teal
                                expenseColor = Color(0xFFE05454), // red
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }

                if (s.payees.isNotEmpty()) {
                    item {
                        ExpressiveCard(Modifier.fillMaxWidth()) {
                            OverlineLabel("Top payees")
                            Spacer(Modifier.height(8.dp))
                            s.payees.forEachIndexed { i, p ->
                                if (i > 0) Spacer(Modifier.height(8.dp))
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Column(Modifier.weight(1f)) {
                                        Text(p.payee, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, maxLines = 1)
                                        Text("${p.count}×", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Text(Money.format(p.total, s.baseCurrency), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            }
            1 -> { // CATEGORIES
                item {
                    ExpressiveCard(Modifier.fillMaxWidth()) {
                        OverlineLabel("Top spending categories")
                        Spacer(Modifier.height(12.dp))
                        if (s.byCategory.isEmpty()) {
                            EmptyState("No spending this month yet.")
                        } else {
                            val segments = s.byCategory.mapIndexed { i, cs ->
                                RingSegment(cs.categoryName ?: "Other", cs.total, chartColor(cs.colorHex, i))
                            }
                            SpendingRing(
                                segments = segments,
                                centerTop = "Spent this month",
                                centerValue = Money.format(totalSpend, s.baseCurrency),
                                modifier = Modifier.fillMaxWidth().height(200.dp),
                            )
                        }
                    }
                }

                if (s.byCategory.isNotEmpty()) {
                    item { SectionHeader("Spending categories") }
                    item {
                        // Convert each category's spend into a labelled stat with its share of total spend.
                        val stats = s.byCategory.mapIndexed { i, cs ->
                            val pct = if (totalSpend > 0) (cs.total / totalSpend * 100).roundToInt() else 0
                            CategoryStat(
                                name = cs.categoryName ?: "Uncategorized",
                                amount = Money.format(cs.total, s.baseCurrency),
                                percent = pct,
                                color = chartColor(cs.colorHex, i),
                            )
                        }
                        CategoryStatGrid(stats, Modifier.fillMaxWidth())
                    }
                }
            }
            2 -> { // TRENDS
                if (s.series.size >= 2) {
                    item {
                        ExpressiveCard(Modifier.fillMaxWidth()) {
                            OverlineLabel("Net worth trend")
                            Spacer(Modifier.height(16.dp))
                            NetWorthLine(
                                values = s.series.map { it.netWorthEnd },
                                labels = s.series.map { it.label },
                                lineColor = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.fillMaxWidth().height(180.dp),
                            )
                        }
                    }
                }

                s.forecast?.let { f -> item { ForecastCard(f, s.baseCurrency) } }

                item { WhatIfCard(whatIf, s.baseCurrency, vm) }
            }
            3 -> { // REPORTS
                item {
                    ExpressiveCard(Modifier.fillMaxWidth(), padding = 0.dp) {
                        val reportsList = listOf("Monthly Summary", "Annual Report", "Custom Date Range", "Category Report", "Merchant Report")
                        Column {
                            reportsList.forEachIndexed { i, r ->
                                Row(
                                    Modifier.fillMaxWidth().clickable {}.padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(r, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                                    Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
                                }
                                if (i < reportsList.lastIndex) {
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                                }
                            }
                        }
                    }
                }

                if (s.series.size >= 2) {
                    item { ReportSummary(s.series, s.baseCurrency) }
                }
            }
        }
        item { Spacer(Modifier.height(24.dp)) }
    }
}

/** Card projecting the end-of-month balance, income, expense and savings from spend so far. */
@Composable
private fun ForecastCard(f: MonthlyForecast, currency: String) {
    ExpressiveCard(Modifier.fillMaxWidth(), container = MaterialTheme.colorScheme.secondaryContainer) {
        OverlineLabel("Monthly forecast")
        Spacer(Modifier.height(4.dp))
        Text("Projected end-of-month balance", style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            Money.format(f.projectedEndBalance, currency),
            style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Proj. income ${Money.format(f.projectedIncome, currency)}",
                style = MaterialTheme.typography.labelMedium, color = FinatraTheme.income)
            Text("Proj. expense ${Money.format(f.projectedExpense, currency)}",
                style = MaterialTheme.typography.labelMedium, color = FinatraTheme.expense)
        }
        Spacer(Modifier.height(4.dp))
        Text("Based on ${f.daysElapsed}/${f.daysInMonth} days · proj. savings ${Money.format(f.projectedSavings, currency)}",
            style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/**
 * Interactive "what-if" savings simulator: pick a category, choose a percentage to cut
 * and a number of months, and see the projected total/monthly saving. Slider changes are
 * pushed back to [AnalyticsViewModel], which recomputes the projection.
 */
@Composable
private fun WhatIfCard(w: WhatIfState, currency: String, vm: AnalyticsViewModel) {
    ExpressiveCard(Modifier.fillMaxWidth()) {
        OverlineLabel("What-if simulator")
        Spacer(Modifier.height(8.dp))
        if (w.categories.isEmpty()) {
            Text("Not enough spending history yet.", style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            return@ExpressiveCard
        }
        LabeledDropdown(
            label = "If I cut",
            options = w.categories,
            selected = w.selected ?: w.categories.first(),
            optionLabel = { it.name },
            onSelect = { vm.setWhatIfCategory(it.categoryId) },
        )
        Spacer(Modifier.height(12.dp))
        Text("By ${w.cutPercent}%", style = MaterialTheme.typography.bodyMedium)
        Slider(
            value = w.cutPercent.toFloat(),
            onValueChange = { vm.setWhatIfPercent(it.roundToInt()) },
            valueRange = 5f..100f,
        )
        Text("Over ${w.months} months", style = MaterialTheme.typography.bodyMedium)
        Slider(
            value = w.months.toFloat(),
            onValueChange = { vm.setWhatIfMonths(it.roundToInt()) },
            valueRange = 1f..24f,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "You'd save ${Money.format(w.totalSaving, currency)}",
            style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold,
            color = FinatraTheme.income,
        )
        Text("≈ ${Money.format(w.monthlySaving, currency)} / month",
            style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/** Row of three mini stat cards: income, expense and savings (tinted by sign). */
@Composable
private fun StatStrip(income: Double, expense: Double, savings: Double, currency: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        MiniStat("Income", Money.format(income, currency), FinatraTheme.income, Modifier.weight(1f))
        MiniStat("Expense", Money.format(expense, currency), FinatraTheme.expense, Modifier.weight(1f))
        MiniStat(
            "Savings", Money.format(savings, currency),
            if (savings >= 0) FinatraTheme.income else FinatraTheme.expense, Modifier.weight(1f),
        )
    }
}

/** Single labelled value tile used inside [StatStrip]. */
@Composable
private fun MiniStat(label: String, value: String, accent: Color, modifier: Modifier) {
    ExpressiveCard(modifier, container = MaterialTheme.colorScheme.surfaceContainer, padding = 14.dp) {
        OverlineLabel(label)
        Spacer(Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.titleMedium, color = accent, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}

/** Aggregated multi-month report: totals, net saved, average monthly savings and best month. */
@Composable
private fun ReportSummary(series: List<MonthPoint>, currency: String) {
    // Aggregate the month series into totals and derived averages for the report rows.
    val income = series.sumOf { it.income }
    val expense = series.sumOf { it.expense }
    val avgSavings = (income - expense) / series.size
    val best = series.maxByOrNull { it.income - it.expense }
    ExpressiveCard(Modifier.fillMaxWidth(), container = MaterialTheme.colorScheme.surfaceContainer) {
        OverlineLabel("${series.size}-month report")
        Spacer(Modifier.height(10.dp))
        ReportRow("Total income", Money.format(income, currency))
        ReportRow("Total expense", Money.format(expense, currency))
        ReportRow("Net saved", Money.format(income - expense, currency))
        ReportRow("Avg monthly savings", Money.format(avgSavings, currency))
        if (best != null) ReportRow("Best month", "${best.label} (${Money.format(best.income - best.expense, currency)})")
    }
}

/** A single label/value line within [ReportSummary]. */
@Composable
private fun ReportRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}
