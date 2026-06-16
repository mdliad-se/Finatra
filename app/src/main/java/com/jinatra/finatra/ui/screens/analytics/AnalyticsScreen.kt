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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jinatra.finatra.data.repository.MonthPoint
import com.jinatra.finatra.ui.components.BarGroup
import com.jinatra.finatra.ui.components.CategoryStat
import com.jinatra.finatra.ui.components.CategoryStatGrid
import com.jinatra.finatra.ui.components.EmptyState
import com.jinatra.finatra.ui.components.ExpressiveCard
import com.jinatra.finatra.ui.components.IncomeExpenseBars
import com.jinatra.finatra.ui.components.NetWorthLine
import com.jinatra.finatra.ui.components.OverlineLabel
import com.jinatra.finatra.ui.components.RingSegment
import com.jinatra.finatra.ui.components.SectionHeader
import com.jinatra.finatra.ui.components.SpendingRing
import com.jinatra.finatra.ui.components.chartColor
import com.jinatra.finatra.ui.theme.FinatraTheme
import com.jinatra.finatra.util.Money
import kotlin.math.roundToInt

@Composable
fun AnalyticsScreen(vm: AnalyticsViewModel = hiltViewModel()) {
    val s by vm.state.collectAsStateWithLifecycle()
    val totalSpend = s.byCategory.sumOf { it.total }

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

        item {
            ExpressiveCard(Modifier.fillMaxWidth()) {
                OverlineLabel("Top categories")
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
                        modifier = Modifier.fillMaxWidth().height(260.dp),
                    )
                }
            }
        }

        item { StatStrip(s.income, s.expense, s.savings, s.baseCurrency) }

        if (s.series.size >= 2) {
            item {
                ExpressiveCard(Modifier.fillMaxWidth()) {
                    OverlineLabel("Income vs expense")
                    Spacer(Modifier.height(16.dp))
                    IncomeExpenseBars(
                        groups = s.series.map { BarGroup(it.label, it.income, it.expense) },
                        incomeColor = FinatraTheme.income,
                        expenseColor = FinatraTheme.expense,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(20.dp))
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
            item { ReportSummary(s.series, s.baseCurrency) }
        }

        if (s.byCategory.isNotEmpty()) {
            item { SectionHeader("Spending categories") }
            item {
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
        item { Spacer(Modifier.height(24.dp)) }
    }
}

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

@Composable
private fun MiniStat(label: String, value: String, accent: Color, modifier: Modifier) {
    ExpressiveCard(modifier, container = MaterialTheme.colorScheme.surfaceContainer, padding = 14.dp) {
        OverlineLabel(label)
        Spacer(Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.titleMedium, color = accent, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}

@Composable
private fun ReportSummary(series: List<MonthPoint>, currency: String) {
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

@Composable
private fun ReportRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}
