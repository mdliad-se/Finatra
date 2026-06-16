package com.jinatra.finatra.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtil {
    private val dayFmt = SimpleDateFormat("EEE, d MMM", Locale.getDefault())
    private val fullFmt = SimpleDateFormat("d MMM yyyy, h:mm a", Locale.getDefault())
    private val monthFmt = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    private val shortMonthFmt = SimpleDateFormat("MMM", Locale.getDefault())
    private val groupFmt = SimpleDateFormat("EEEE, d MMM", Locale.getDefault())

    fun shortMonth(epoch: Long): String = shortMonthFmt.format(Date(epoch))

    fun day(epoch: Long): String = dayFmt.format(Date(epoch))
    fun full(epoch: Long): String = fullFmt.format(Date(epoch))
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

    fun startOfMonth(now: Long = System.currentTimeMillis()): Long = Calendar.getInstance().apply {
        timeInMillis = now
        set(Calendar.DAY_OF_MONTH, 1); set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    fun endOfMonth(now: Long = System.currentTimeMillis()): Long = Calendar.getInstance().apply {
        timeInMillis = startOfMonth(now)
        add(Calendar.MONTH, 1); add(Calendar.MILLISECOND, -1)
    }.timeInMillis

    fun startOfDay(now: Long = System.currentTimeMillis()): Long = Calendar.getInstance().apply {
        timeInMillis = now
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    fun plusDays(epoch: Long, days: Int): Long = Calendar.getInstance().apply {
        timeInMillis = epoch; add(Calendar.DAY_OF_YEAR, days)
    }.timeInMillis

    fun plusMonths(epoch: Long, months: Int): Long = Calendar.getInstance().apply {
        timeInMillis = epoch; add(Calendar.MONTH, months)
    }.timeInMillis
}
