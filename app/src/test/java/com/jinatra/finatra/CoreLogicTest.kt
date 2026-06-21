package com.jinatra.finatra

import com.jinatra.finatra.util.CsvUtil
import com.jinatra.finatra.util.DateUtil
import com.jinatra.finatra.util.Money
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for the core utility logic: CSV line parsing ([CsvUtil]), date round-tripping
 * ([DateUtil]), and currency formatting ([Money]).
 */
class CoreLogicTest {

    /**
     * CSV parsing handles quoted fields that themselves contain commas and escaped (doubled)
     * quotes, splitting into the correct number of fields with quotes unescaped.
     */
    @Test fun csv_splitsQuotedFieldsWithCommasAndEscapedQuotes() {
        val line = "\"1 Jan\",\"EXPENSE\",\"12.50\",\"USD\",\"Food\",\"Cash\",\"Lunch, with \"\"friends\"\"\",\"tag1\""
        val f = CsvUtil.splitLine(line)
        assertEquals(8, f.size)
        assertEquals("Lunch, with \"friends\"", f[6])
        assertEquals("12.50", f[2])
    }

    /** A simple unquoted comma-separated line splits into its plain fields. */
    @Test fun csv_plainLine() {
        assertEquals(listOf("a", "b", "c"), CsvUtil.splitLine("a,b,c"))
    }

    /** Formatting a timestamp and parsing it back recovers the same instant at minute precision. */
    @Test fun date_parseFull_roundTrips() {
        val now = 1_700_000_000_000L
        val parsed = DateUtil.parseFull(DateUtil.full(now))
        assertTrue(parsed != null)
        // minute precision round-trip
        assertEquals(now / 60000, parsed!! / 60000)
    }

    /**
     * Money formats a known currency with grouped digits, and for an unrecognized currency code
     * falls back to showing the raw code.
     */
    @Test fun money_formatsAndFallsBackForUnknownCode() {
        assertTrue(Money.format(1234.5, "USD").contains("1,234.5"))
        assertTrue(Money.format(10.0, "XYZ").contains("XYZ"))
    }

    /** Signed formatting prefixes expenses with "-" and non-expenses with "+". */
    @Test fun money_signed() {
        assertTrue(Money.formatSigned(5.0, "USD", isExpense = true).startsWith("-"))
        assertTrue(Money.formatSigned(5.0, "USD", isExpense = false).startsWith("+"))
    }
}
