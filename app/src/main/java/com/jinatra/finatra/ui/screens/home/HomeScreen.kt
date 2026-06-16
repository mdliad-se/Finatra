package com.jinatra.finatra.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jinatra.finatra.data.repository.AccountCard
import com.jinatra.finatra.ui.screens.onboarding.label
import com.jinatra.finatra.ui.components.ExpressiveCard
import com.jinatra.finatra.ui.components.IconChip
import com.jinatra.finatra.ui.components.OverlineLabel
import com.jinatra.finatra.ui.components.RingSegment
import com.jinatra.finatra.ui.components.SectionHeader
import com.jinatra.finatra.ui.components.SpendingRing
import com.jinatra.finatra.ui.components.TransactionRow
import com.jinatra.finatra.ui.components.chartColor
import com.jinatra.finatra.ui.theme.DarkTeal
import com.jinatra.finatra.ui.theme.DeepTeal
import com.jinatra.finatra.ui.theme.FinatraTheme
import com.jinatra.finatra.ui.theme.SweetCream
import com.jinatra.finatra.util.CategoryIcons
import com.jinatra.finatra.util.Money

@Composable
fun HomeScreen(
    onAddTransaction: () -> Unit,
    onSeeAllTransactions: () -> Unit,
    onOpenAccounts: () -> Unit,
    onOpenTransaction: (Long) -> Unit,
    onAddForAccount: (Long, String) -> Unit = { _, _ -> },
    vm: HomeViewModel = hiltViewModel(),
) {
    val s by vm.state.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddTransaction,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Add") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            )
        }
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxWidth().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(
                        Icons.Filled.AccountBalanceWallet,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp),
                    )
                    Text(
                        "Finatra",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            item { BalanceHeroCard(s.netWorth, s.incomeThisMonth, s.expenseThisMonth, s.baseCurrency, onOpenAccounts) }

            s.aiInsight?.let { insight ->
                item {
                    ExpressiveCard(Modifier.fillMaxWidth(), container = MaterialTheme.colorScheme.secondaryContainer, padding = 16.dp) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            IconChip(Icons.Filled.AutoAwesome, tint = MaterialTheme.colorScheme.primary)
                            Column(Modifier.weight(1f)) {
                                OverlineLabel("AI insight")
                                Text(insight, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                            }
                            IconButton(onClick = vm::dismissInsight) {
                                Icon(Icons.Filled.Close, contentDescription = "Dismiss", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            if (s.accounts.isNotEmpty()) {
                item { AccountSection(s.accounts, onAddForAccount) }
            } else {
                item { FlowRingCard(s.incomeThisMonth, s.expenseThisMonth, s.balanceLeft, s.baseCurrency) }
            }

            if (s.upcoming.isNotEmpty()) {
                item { SectionHeader("Upcoming") }
                item {
                    ExpressiveCard(Modifier.fillMaxWidth()) {
                        s.upcoming.forEachIndexed { i, r ->
                            if (i > 0) Spacer(Modifier.height(12.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(r.note.ifBlank { "Scheduled" }, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, maxLines = 1)
                                    Text(com.jinatra.finatra.util.DateUtil.day(r.nextRun), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text(Money.format(r.amount, r.currency), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            if (s.budgets.isNotEmpty()) {
                item { SectionHeader("Budgets") }
                item {
                    ExpressiveCard(Modifier.fillMaxWidth()) {
                        s.budgets.forEachIndexed { i, bp ->
                            if (i > 0) Spacer(Modifier.height(14.dp))
                            BudgetMiniRow(bp, s.baseCurrency)
                        }
                    }
                }
            }

            item {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SectionHeader("Recent")
                    TextButton(onClick = onSeeAllTransactions) { Text("See all") }
                }
            }
            if (s.recent.isEmpty()) {
                item {
                    Text(
                        "No transactions yet. Tap Add to log your first.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 24.dp),
                    )
                }
            } else {
                item {
                    ExpressiveCard(Modifier.fillMaxWidth(), padding = 8.dp) {
                        Column(Modifier.padding(horizontal = 8.dp)) {
                            s.recent.forEach { tx -> TransactionRow(tx) { onOpenTransaction(tx.id) } }
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun BalanceHeroCard(
    netWorth: Double,
    income: Double,
    expense: Double,
    currency: String,
    onClick: () -> Unit,
) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(Brush.linearGradient(listOf(DeepTeal, DarkTeal)))
    ) {
        Column(Modifier.padding(24.dp)) {
            OverlineLabelOnTeal("Net worth")
            Text(
                Money.format(netWorth, currency),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = SweetCream,
            )
            Spacer(Modifier.height(18.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FlowPill("Income", Money.format(income, currency), Icons.Filled.ArrowDownward, Modifier.weight(1f))
                FlowPill("Expense", Money.format(expense, currency), Icons.Filled.ArrowUpward, Modifier.weight(1f))
            }
            Spacer(Modifier.height(6.dp))
            TextButton(onClick = onClick) {
                Text("View accounts", color = SweetCream.copy(alpha = 0.95f))
            }
        }
    }
}

@Composable
private fun OverlineLabelOnTeal(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Medium,
        color = SweetCream.copy(alpha = 0.75f),
    )
}

@Composable
private fun FlowPill(label: String, value: String, icon: ImageVector, modifier: Modifier) {
    Row(
        modifier.clip(RoundedCornerShape(16.dp)).background(Color.White.copy(alpha = 0.12f))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(32.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = SweetCream)
        }
        Column(Modifier.padding(start = 10.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = SweetCream.copy(alpha = 0.8f))
            Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = Color.White, maxLines = 1)
        }
    }
}

/** Swipeable account cards + a per-account pie chart that follows the selected card. */
@Composable
private fun AccountSection(cards: List<AccountCard>, onAddForAccount: (Long, String) -> Unit) {
    val pager = rememberPagerState(pageCount = { cards.size })
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        HorizontalPager(
            state = pager,
            pageSpacing = 12.dp,
            contentPadding = androidx.compose.foundation.layout.PaddingValues(end = 32.dp),
        ) { page ->
            AccountCardView(cards[page], onAddForAccount)
        }
        // Page dots
        if (cards.size > 1) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                repeat(cards.size) { i ->
                    val active = i == pager.currentPage
                    Box(
                        Modifier.padding(horizontal = 3.dp).size(if (active) 8.dp else 6.dp).clip(CircleShape)
                            .background(if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
                    )
                }
            }
        }
        AccountPie(cards[pager.currentPage.coerceIn(0, cards.lastIndex)])
    }
}

@Composable
private fun AccountCardView(card: AccountCard, onAddForAccount: (Long, String) -> Unit) {
    val base = Color(card.account.colorHex)
    val onCard = if (base.luminance() > 0.5f) Color(0xFF1A1A1A) else Color.White
    val subtle = onCard.copy(alpha = 0.7f)
    Box(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(listOf(base, base.copy(alpha = 0.78f)))),
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.size(40.dp).clip(CircleShape).background(onCard.copy(alpha = 0.18f)), contentAlignment = Alignment.Center) {
                    Icon(CategoryIcons.forAccount(card.account.type), contentDescription = null, tint = onCard)
                }
                Column(Modifier.weight(1f)) {
                    Text(card.account.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = onCard, maxLines = 1)
                    Text(card.account.type.label(), style = MaterialTheme.typography.labelMedium, color = subtle)
                }
            }
            Spacer(Modifier.height(14.dp))
            Text(Money.format(card.balance, card.account.currency), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = onCard, maxLines = 1)
            Text("Balance", style = MaterialTheme.typography.labelMedium, color = subtle)
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = { onAddForAccount(card.account.id, "INCOME") },
                    colors = ButtonDefaults.buttonColors(containerColor = onCard.copy(alpha = 0.16f), contentColor = onCard),
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Filled.ArrowDownward, contentDescription = null, modifier = Modifier.size(16.dp)); Spacer(Modifier.size(6.dp)); Text("Income")
                }
                Button(
                    onClick = { onAddForAccount(card.account.id, "EXPENSE") },
                    colors = ButtonDefaults.buttonColors(containerColor = onCard.copy(alpha = 0.16f), contentColor = onCard),
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Filled.Remove, contentDescription = null, modifier = Modifier.size(16.dp)); Spacer(Modifier.size(6.dp)); Text("Expense")
                }
            }
        }
    }
}

@Composable
private fun AccountPie(card: AccountCard) {
    val accent = Color(card.account.colorHex)
    val spent = card.categorySpend.sumOf { it.total }
    ExpressiveCard(Modifier.fillMaxWidth()) {
        Text("This month · ${card.account.name}", style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(8.dp))
        if (spent > 0) {
            SpendingRing(
                segments = card.categorySpend.mapIndexed { i, cs ->
                    RingSegment(cs.categoryName ?: "Other", cs.total, chartColor(cs.colorHex, i))
                },
                centerTop = "Spent",
                centerValue = Money.format(spent, card.account.currency),
                modifier = Modifier.fillMaxWidth().height(230.dp),
                showPercentLabels = true,
            )
        } else {
            SpendingRing(
                segments = listOf(RingSegment("Balance", card.balance.coerceAtLeast(1.0), accent)),
                centerTop = "No spending yet",
                centerValue = Money.format(card.balance, card.account.currency),
                modifier = Modifier.fillMaxWidth().height(230.dp),
                showPercentLabels = false,
            )
        }
    }
}

@Composable
private fun FlowRingCard(income: Double, expense: Double, left: Double, currency: String) {
    val hasData = income > 0 || expense > 0
    ExpressiveCard(Modifier.fillMaxWidth()) {
        Text("This month", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(8.dp))
        if (hasData) {
            SpendingRing(
                segments = listOf(
                    RingSegment("Income", income, FinatraTheme.income),
                    RingSegment("Expense", expense, FinatraTheme.expense),
                ),
                centerTop = if (left >= 0) "Left this month" else "Over budget",
                centerValue = Money.format(left, currency),
                modifier = Modifier.fillMaxWidth().height(220.dp),
                showPercentLabels = true,
            )
        } else {
            Text(
                "No activity this month yet.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 24.dp),
            )
        }
    }
}

@Composable
private fun BudgetMiniRow(bp: BudgetProgress, currency: String) {
    val fraction = if (bp.budget.amount > 0) (bp.spent / bp.budget.amount).toFloat().coerceIn(0f, 1f) else 0f
    val over = bp.spent > bp.budget.amount
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(bp.categoryName, style = MaterialTheme.typography.bodyMedium)
            Text(
                "${Money.format(bp.spent, currency)} / ${Money.format(bp.budget.amount, currency)}",
                style = MaterialTheme.typography.labelMedium,
                color = if (over) FinatraTheme.expense else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { fraction },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
            color = if (over) FinatraTheme.expense else MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    }
}
