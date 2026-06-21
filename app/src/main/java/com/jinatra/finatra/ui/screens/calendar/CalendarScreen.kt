package com.jinatra.finatra.ui.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jinatra.finatra.ui.components.ExpressiveCard
import com.jinatra.finatra.ui.components.FinatraTopBar
import com.jinatra.finatra.ui.components.SectionHeader
import com.jinatra.finatra.ui.components.TransactionRow
import com.jinatra.finatra.util.DateUtil
import com.jinatra.finatra.util.Money
import java.util.Calendar

/**
 * Calendar feature screen: a month grid that highlights days with transaction
 * activity, a list of the selected day's transactions, and a preview of upcoming
 * recurring entries. Month navigation and day selection are driven by [CalendarViewModel].
 *
 * @param onBack invoked when the top bar's back affordance is tapped.
 * @param onOpenTransaction invoked with a transaction id to open its detail screen.
 */
@Composable
fun CalendarScreen(onBack: () -> Unit, onOpenTransaction: (Long) -> Unit, vm: CalendarViewModel = hiltViewModel()) {
    val s by vm.state.collectAsStateWithLifecycle()

    LazyColumn(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { FinatraTopBar("Calendar", onBack) }

        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                IconButton(onClick = vm::prevMonth) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous month")
                }
                Text(s.monthLabel, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                IconButton(onClick = vm::nextMonth) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next month")
                }
            }
        }

        item { MonthGrid(s, onSelect = vm::selectDay) }

        item { SectionHeader("Transactions · ${DateUtil.day(s.selectedDay)}") }
        if (s.dayTransactions.isEmpty()) {
            item {
                Text("No transactions on this day.", style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 8.dp))
            }
        } else {
            item {
                ExpressiveCard(Modifier.fillMaxWidth(), padding = 8.dp) {
                    Column(Modifier.padding(horizontal = 8.dp)) {
                        s.dayTransactions.forEach { tx -> TransactionRow(tx) { onOpenTransaction(tx.id) } }
                    }
                }
            }
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
                                Text(DateUtil.day(r.nextRun), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(Money.format(r.amount, r.currency), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(24.dp)) }
    }
}

/**
 * Renders the month as a 7-column grid of day cells, laid out under weekday
 * headers. Builds the cell list from the anchored month and delegates each
 * selectable day to [DayCell].
 */
@Composable
private fun MonthGrid(s: CalendarUiState, onSelect: (Long) -> Unit) {
    val dayNames = listOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa")
    val monthStart = DateUtil.startOfMonth(s.monthAnchor)
    val cal = Calendar.getInstance().apply { timeInMillis = monthStart }
    val firstDow = cal.get(Calendar.DAY_OF_WEEK) - 1   // 0 = Sunday
    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val today = DateUtil.startOfDay()

    // Cells: leading blanks, then each day; pad to full weeks.
    val cells = buildList {
        repeat(firstDow) { add(-1L) }
        for (d in 0 until daysInMonth) add(DateUtil.plusDays(monthStart, d))
        while (size % 7 != 0) add(-1L)
    }

    ExpressiveCard(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth()) {
            dayNames.forEach { n ->
                Text(n, Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(Modifier.height(6.dp))
        cells.chunked(7).forEach { week ->
            Row(Modifier.fillMaxWidth()) {
                week.forEach { epoch ->
                    Box(Modifier.weight(1f).aspectRatio(1f), contentAlignment = Alignment.Center) {
                        if (epoch >= 0) DayCell(epoch, s, today, onSelect)
                    }
                }
            }
        }
    }
}

/**
 * A single tappable day in [MonthGrid]. Highlights today and the selected day with
 * distinct background/foreground colors and shows a dot beneath days that have activity.
 */
@Composable
private fun DayCell(epoch: Long, s: CalendarUiState, today: Long, onSelect: (Long) -> Unit) {
    val isToday = epoch == today
    val isSelected = epoch == s.selectedDay
    val hasActivity = s.daysWithActivity.contains(epoch)
    val dayNum = Calendar.getInstance().apply { timeInMillis = epoch }.get(Calendar.DAY_OF_MONTH)

    val bg = when {
        isToday -> MaterialTheme.colorScheme.primary
        isSelected -> MaterialTheme.colorScheme.secondaryContainer
        else -> androidx.compose.ui.graphics.Color.Transparent
    }
    val fg = when {
        isToday -> MaterialTheme.colorScheme.onPrimary
        else -> MaterialTheme.colorScheme.onSurface
    }
    Column(
        Modifier.size(40.dp).clip(CircleShape).background(bg).clickable { onSelect(epoch) },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("$dayNum", style = MaterialTheme.typography.bodyMedium, color = fg,
            fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal)
        if (hasActivity) {
            Box(Modifier.size(4.dp).clip(CircleShape).background(if (isToday) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary))
        }
    }
}
