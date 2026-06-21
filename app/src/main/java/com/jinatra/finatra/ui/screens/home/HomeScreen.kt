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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Savings
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.width
import java.util.Calendar
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import com.jinatra.finatra.data.prefs.ThemeMode
import kotlin.math.roundToInt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButtonDefaults
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
import com.jinatra.finatra.data.repository.HealthScore
import com.jinatra.finatra.ui.screens.onboarding.label
import com.jinatra.finatra.ui.components.ExpressiveCard
import com.jinatra.finatra.ui.components.IconChip
import com.jinatra.finatra.ui.components.OverlineLabel
import com.jinatra.finatra.ui.components.RingSegment
import com.jinatra.finatra.ui.components.SectionHeader
import com.jinatra.finatra.ui.components.SpendingRing
import com.jinatra.finatra.ui.components.TransactionRow
import com.jinatra.finatra.ui.components.chartColor
import com.jinatra.finatra.ui.theme.DeepWarmRed
import com.jinatra.finatra.ui.theme.WarmRed
import com.jinatra.finatra.ui.theme.FinatraTheme
import com.jinatra.finatra.ui.theme.SweetCream
import com.jinatra.finatra.util.CategoryIcons
import com.jinatra.finatra.util.DateUtil
import com.jinatra.finatra.util.Money

/**
 * Home dashboard — the app's primary landing screen. Aggregates the user's financial
 * snapshot into one scroll: greeting, net-worth hero card, quick income/expense actions,
 * shortcuts row, financial health score, an optional AI insight, the account carousel (or
 * a money-flow ring when there are no accounts), monthly budget progress, upcoming bills,
 * and recent transactions. All data comes from [HomeViewModel]; the lambdas wire up
 * navigation to the corresponding feature screens.
 */
@Composable
fun HomeScreen(
    onAddTransaction: () -> Unit,
    onSeeAllTransactions: () -> Unit,
    onOpenAccounts: () -> Unit,
    onOpenTransaction: (Long) -> Unit,
    onAddForAccount: (Long, String) -> Unit = { _, _ -> },
    onOpenAiCoach: () -> Unit = {},
    onOpenGoals: () -> Unit = {},
    onOpenCalendar: () -> Unit = {},
    onOpenAchievements: () -> Unit = {},
    onOpenBudgets: () -> Unit = {},
    vm: HomeViewModel = hiltViewModel(),
) {
    val s by vm.state.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddTransaction,
                shape = RoundedCornerShape(24.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp),
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
            }
        }
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxWidth().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Spacer(Modifier.height(8.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Finatra",
                            style = MaterialTheme.typography.headlineMedium,
                            fontFamily = com.jinatra.finatra.ui.theme.NeganFont,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Box(
                            Modifier
                                .padding(start = 2.dp, top = 8.dp)
                                .size(6.dp)
                                .align(Alignment.Top)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }
                    IconButton(onClick = vm::toggleTheme) {
                        val isDark = s.themeMode == ThemeMode.DARK
                        Icon(
                            imageVector = if (isDark) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                            contentDescription = "Toggle Theme",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                // Greeting varies by time of day.
                val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                val greeting = when (hour) {
                    in 0..11 -> "Good morning"
                    in 12..16 -> "Good afternoon"
                    else -> "Good evening"
                }
                Column(Modifier.padding(vertical = 4.dp)) {
                    Text(
                        buildAnnotatedString {
                            append("$greeting, ")
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)) {
                                append(s.userName)
                            }
                        },
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Light),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        "Your finances look healthy today.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item { BalanceHeroCard(s.netWorth, s.balanceLeft, s.baseCurrency, onOpenAccounts) }

            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = { onAddForAccount(-1L, "INCOME") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.AddCircle, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Add Income")
                    }
                    Button(
                        onClick = { onAddForAccount(-1L, "EXPENSE") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.RemoveCircle, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Add Expense")
                    }
                }
            }

            item { QuickAccessRow(onOpenAiCoach, onOpenGoals, onOpenCalendar, onOpenAchievements) }

            s.health?.let { h -> item { HealthScoreCard(h) } }

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

            // Show the swipeable account carousel when accounts exist, otherwise fall back
            // to a single income/expense flow ring summarizing this month's activity.
            if (s.accounts.isNotEmpty()) {
                item { AccountSection(s.accounts, onAddForAccount) }
            } else {
                item { FlowRingCard(s.incomeThisMonth, s.expenseThisMonth, s.balanceLeft, s.baseCurrency) }
            }

            // Aggregate all category budgets into one monthly figure; only show the card when budgeted.
            val totalBudget = s.budgets.sumOf { it.budget.amount }
            val totalSpent = s.budgets.sumOf { it.spent }
            if (totalBudget > 0.0) {
                item {
                    // Days remaining in the current calendar month.
                    val cal = Calendar.getInstance()
                    val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
                    val currentDay = cal.get(Calendar.DAY_OF_MONTH)
                    val daysLeft = (maxDays - currentDay).coerceAtLeast(0)
                    MonthlyBudgetCard(totalSpent, totalBudget, daysLeft, s.baseCurrency, onOpenBudgets)
                }
            }

            if (s.upcoming.isNotEmpty()) {
                item { SectionHeader("Upcoming Bills") }
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(s.upcoming) { bill ->
                            // A scheduled bill whose next run is already in the past is flagged overdue.
                            val isOverdue = bill.nextRun < System.currentTimeMillis()
                            val accentBorderColor = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            val tintColor = if (isOverdue) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceContainerHigh

                            ExpressiveCard(
                                modifier = Modifier
                                    .width(160.dp)
                                    .border(
                                        width = 1.dp,
                                        color = accentBorderColor,
                                        shape = RoundedCornerShape(16.dp)
                                    ),
                                container = tintColor,
                                padding = 16.dp
                            ) {
                                Column {
                                    Text(
                                        bill.note.ifBlank { "Scheduled" },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        Money.format(bill.amount, bill.currency),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        if (isOverdue) "Overdue" else DateUtil.day(bill.nextRun),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
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

/**
 * Gradient hero card showing total net worth and the month's change, with an up/down
 * trend pill colored by direction. Tapping the card navigates to the accounts screen.
 */
@Composable
private fun BalanceHeroCard(
    netWorth: Double,
    netWorthChange: Double,
    currency: String,
    onClick: () -> Unit,
) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(listOf(WarmRed, DeepWarmRed)))
            .clickable(onClick = onClick)
    ) {
        Box(
            Modifier
                .matchParentSize()
                .background(Brush.radialGradient(
                    colors = listOf(Color.White.copy(alpha = 0.15f), Color.Transparent),
                    center = Offset.Zero,
                    radius = 400f
                ))
        )
        Column(Modifier.padding(24.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "TOTAL NET WORTH",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = SweetCream.copy(alpha = 0.9f),
                    letterSpacing = 1.sp
                )
                Icon(
                    imageVector = Icons.Filled.AccountBalance,
                    contentDescription = null,
                    tint = SweetCream.copy(alpha = 0.8f)
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                Money.format(netWorth, currency),
                style = MaterialTheme.typography.headlineLarge.copy(fontSize = 36.sp),
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
            Spacer(Modifier.height(12.dp))
            
            val isPositive = netWorthChange >= 0.0
            val trendText = if (isPositive) {
                "+" + Money.format(netWorthChange, currency) + " this month"
            } else {
                Money.format(netWorthChange, currency) + " this month"
            }
            val trendColor = if (isPositive) Color(0xFF7ABE5A) else MaterialTheme.colorScheme.error
            val trendIcon = if (isPositive) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color.Black.copy(alpha = 0.12f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = trendIcon,
                    contentDescription = null,
                    tint = trendColor,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = trendText,
                    color = trendColor,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
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
        // Pie below the carousel tracks whichever card is currently paged into view.
        AccountPie(cards[pager.currentPage.coerceIn(0, cards.lastIndex)])
    }
}

/**
 * A single account card in the carousel: name, type, balance, and quick add-income/
 * add-expense buttons, on a gradient derived from the account's color.
 */
@Composable
private fun AccountCardView(card: AccountCard, onAddForAccount: (Long, String) -> Unit) {
    val base = Color(card.account.colorHex)
    // Pick dark or light foreground based on the card color's luminance for contrast.
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

/**
 * Spending breakdown ring for one account's current month. Shows category segments when
 * there is spending; otherwise shows the account balance as a single placeholder segment.
 */
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

/**
 * Income-vs-expense ring shown on the dashboard when the user has no accounts yet.
 * The center reports the amount left this month (or an over-budget label).
 */
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

/** Horizontal row of shortcut pills linking to AI Coach, Goals, Calendar, and Achievements. */
@Composable
private fun QuickAccessRow(
    onAiCoach: () -> Unit,
    onGoals: () -> Unit,
    onCalendar: () -> Unit,
    onAchievements: () -> Unit,
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        item { QuickPill(Icons.Filled.AutoAwesome, "AI Coach", primary = true, onClick = onAiCoach) }
        item { QuickPill(Icons.Filled.Savings, "Goals", onClick = onGoals) }
        item { QuickPill(Icons.Filled.CalendarMonth, "Calendar", onClick = onCalendar) }
        item { QuickPill(Icons.Filled.EmojiEvents, "Achievements", onClick = onAchievements) }
    }
}

@Composable
private fun QuickPill(icon: ImageVector, label: String, primary: Boolean = false, onClick: () -> Unit) {
    val container = if (primary) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh
    val content = if (primary) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    Row(
        Modifier.clip(RoundedCornerShape(14.dp)).background(container).clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(icon, contentDescription = null, tint = content, modifier = Modifier.size(18.dp))
        Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium, color = content)
    }
}

/**
 * Financial health summary card: status text and 0-100 score, with the accent color
 * banded green/amber/red by score range, plus a progress bar of the same color.
 */
@Composable
private fun HealthScoreCard(h: HealthScore) {
    // Color band by score: strong (>=71) green, fair (>=40) tertiary, weak red.
    val band = when {
        h.score >= 71 -> FinatraTheme.income
        h.score >= 40 -> MaterialTheme.colorScheme.tertiary
        else -> FinatraTheme.expense
    }
    ExpressiveCard(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("FINANCIAL HEALTH", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(h.status, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = band)
            }
            Text("${h.score}", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold, color = band)
        }
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { h.score / 100f },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
            color = band,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
        Spacer(Modifier.height(6.dp))
        Text("Budget adherence · savings rate · spending trends",
            style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/**
 * Aggregate monthly budget card: total spent vs total budgeted, days left in the month,
 * utilization percent, and remaining amount. Tapping opens the budgets screen.
 */
@Composable
private fun MonthlyBudgetCard(
    spent: Double,
    total: Double,
    daysLeft: Int,
    currency: String,
    onClick: () -> Unit
) {
    val fraction = if (total > 0) (spent / total).toFloat().coerceIn(0f, 1f) else 0f
    val remaining = (total - spent).coerceAtLeast(0.0)
    val percent = (fraction * 100).roundToInt()

    // Progress color escalates green -> amber -> red as the budget fills up.
    val progressColor = when {
        fraction < 0.7f -> Color(0xFF7ABE5A)
        fraction < 0.9f -> Color(0xFFE8B85A)
        else -> Color(0xFFE05454)
    }

    ExpressiveCard(
        Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("October Budget", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(2.dp))
                Text("${Money.format(spent, currency)} spent of ${Money.format(total, currency)}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Box(
                Modifier
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    "$daysLeft days left",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        LinearProgressIndicator(
            progress = { fraction },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
            color = progressColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
        Spacer(Modifier.height(8.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("$percent% utilized", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("${Money.format(remaining, currency)} remaining", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
