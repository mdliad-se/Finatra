package com.jinatra.finatra.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Date/time helpers operating on epoch milliseconds.
 *
 * Formatting uses the device's default [Locale] and time zone, so displayed strings are localized.
 * "Start/end of month/day" computations use the default-time-zone [Calendar]; end-of-period values
 * are inclusive to the last millisecond (e.g. [endOfMonth] is the month start + 1 month − 1 ms).
 */
object DateUtil {
    private val dayFmt = SimpleDateFormat("EEE, d MMM", Locale.getDefault())
    private val fullFmt = SimpleDateFormat("d MMM yyyy, h:mm a", Locale.getDefault())
    private val monthFmt = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    private val shortMonthFmt = SimpleDateFormat("MMM", Locale.getDefault())
    private val groupFmt = SimpleDateFormat("EEEE, d MMM", Locale.getDefault())

    /** Abbreviated month name only, e.g. "Oct" (chart axis labels). */
    fun shortMonth(epoch: Long): String = shortMonthFmt.format(Date(epoch))

    /** Short day label without year, e.g. "Tue, 12 Oct". */
    fun day(epoch: Long): String = dayFmt.format(Date(epoch))
    /** Full date and time, e.g. "12 Oct 2025, 3:30 PM"; round-trips via [parseFull]. */
    fun full(epoch: Long): String = fullFmt.format(Date(epoch))
    /** Full month and year, e.g. "October 2025". */
    fun month(epoch: Long): String = monthFmt.format(Date(epoch))

    /** Parse a string previously produced by [full]; null if unparseable. */
    fun parseFull(s: String): Long? = runCatching { fullFmt.parse(s)?.time }.getOrNull()

    /** Relative date-group header: Today / Yesterday / "Tuesday, 12 Oct". */
    fun groupLabel(epoch: Long): String {
        val today = startOfDay()
        return when (startOfDay(epoch)) {
            today -> "Today"
            plusDays(today, -1) -> "Yesterday"
            else -> groupFmt.format(Date(epoch))
        }
    }

    /** Epoch ms of the first day of [now]'s month at 00:00:00.000 (default time zone). */
    fun startOfMonth(now: Long = System.currentTimeMillis()): Long = Calendar.getInstance().apply {
        timeInMillis = now
        set(Calendar.DAY_OF_MONTH, 1); set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    /** Last instant of [now]'s month: month start + 1 month − 1 ms (inclusive upper bound). */
    fun endOfMonth(now: Long = System.currentTimeMillis()): Long = Calendar.getInstance().apply {
        timeInMillis = startOfMonth(now)
        add(Calendar.MONTH, 1); add(Calendar.MILLISECOND, -1)
    }.timeInMillis

    /** Epoch ms of [now]'s day at 00:00:00.000 (default time zone); used as a day bucket key. */
    fun startOfDay(now: Long = System.currentTimeMillis()): Long = Calendar.getInstance().apply {
        timeInMillis = now
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    /** [epoch] shifted by [days] (negative to go back); calendar-aware so it spans month/year ends. */
    fun plusDays(epoch: Long, days: Int): Long = Calendar.getInstance().apply {
        timeInMillis = epoch; add(Calendar.DAY_OF_YEAR, days)
    }.timeInMillis

    /** [epoch] shifted by [months] (negative to go back); [Calendar] clamps overflowing day-of-month. */
    fun plusMonths(epoch: Long, months: Int): Long = Calendar.getInstance().apply {
        timeInMillis = epoch; add(Calendar.MONTH, months)
    }.timeInMillis
}
