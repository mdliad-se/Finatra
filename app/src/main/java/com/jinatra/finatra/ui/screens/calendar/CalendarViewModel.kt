package com.jinatra.finatra.ui.screens.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jinatra.finatra.data.local.dao.TransactionWithDetails
import com.jinatra.finatra.data.local.entity.RecurringTransactionEntity
import com.jinatra.finatra.data.prefs.SettingsRepository
import com.jinatra.finatra.data.repository.FinanceRepository
import com.jinatra.finatra.util.DateUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Immutable UI state for the calendar screen.
 *
 * @property monthAnchor any epoch within the month currently being displayed.
 * @property monthLabel formatted "Month Year" label for the visible month.
 * @property daysWithActivity start-of-day epochs (within the visible month) that have transactions.
 * @property selectedDay start-of-day epoch of the day the user has tapped.
 * @property dayTransactions transactions occurring on [selectedDay], newest first.
 * @property upcoming the next few recurring transactions by their next run date.
 * @property baseCurrency the user's base currency code.
 */
data class CalendarUiState(
    val monthAnchor: Long = System.currentTimeMillis(),
    val monthLabel: String = "",
    val daysWithActivity: Set<Long> = emptySet(),   // start-of-day epochs in this month
    val selectedDay: Long = DateUtil.startOfDay(),
    val dayTransactions: List<TransactionWithDetails> = emptyList(),
    val upcoming: List<RecurringTransactionEntity> = emptyList(),
    val baseCurrency: String = "USD",
)

/**
 * ViewModel backing [CalendarScreen]. Holds the visible-month anchor and the selected
 * day, and combines those with the live transaction/recurring/settings streams to derive
 * [CalendarUiState]. Recomputes the month's activity days, the selected day's transactions,
 * and the upcoming recurring list whenever any input changes.
 */
@HiltViewModel
class CalendarViewModel @Inject constructor(
    repo: FinanceRepository,
    settings: SettingsRepository,
) : ViewModel() {

    // User-controlled inputs: the month being viewed and the day tapped within it.
    private val anchor = MutableStateFlow(System.currentTimeMillis())
    private val selected = MutableStateFlow(DateUtil.startOfDay())

    val state = combine(
        repo.observeTransactions(),
        repo.observeRecurring(),
        settings.settings,
        anchor,
        selected,
    ) { txns, recurring, s, monthAnchor, selectedDay ->
        val start = DateUtil.startOfMonth(monthAnchor)
        val end = DateUtil.endOfMonth(monthAnchor)
        // Transactions that fall within the visible month; used to mark active days in the grid.
        val inMonth = txns.filter { it.dateTime in start..end }
        CalendarUiState(
            monthAnchor = monthAnchor,
            monthLabel = DateUtil.month(monthAnchor),
            daysWithActivity = inMonth.map { DateUtil.startOfDay(it.dateTime) }.toSet(),
            selectedDay = selectedDay,
            dayTransactions = txns.filter { DateUtil.startOfDay(it.dateTime) == selectedDay }.sortedByDescending { it.dateTime },
            upcoming = recurring.sortedBy { it.nextRun }.take(5),
            baseCurrency = s.baseCurrency,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CalendarUiState())

    fun prevMonth() { anchor.value = DateUtil.plusMonths(anchor.value, -1) }
    fun nextMonth() { anchor.value = DateUtil.plusMonths(anchor.value, 1) }
    fun selectDay(epoch: Long) { selected.value = DateUtil.startOfDay(epoch) }
}
