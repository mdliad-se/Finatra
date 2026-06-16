package com.jinatra.finatra

import com.jinatra.finatra.util.CsvUtil
import com.jinatra.finatra.util.DateUtil
import com.jinatra.finatra.util.Money
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CoreLogicTest {

    @Test fun csv_splitsQuotedFieldsWithCommasAndEscapedQuotes() {
        val line = "\"1 Jan\",\"EXPENSE\",\"12.50\",\"USD\",\"Food\",\"Cash\",\"Lunch, with \"\"friends\"\"\",\"tag1\""
        val f = CsvUtil.splitLine(line)
        assertEquals(8, f.size)
        assertEquals("Lunch, with \"friends\"", f[6])
        assertEquals("12.50", f[2])
    }

    @Test fun csv_plainLine() {
        assertEquals(listOf("a", "b", "c"), CsvUtil.splitLine("a,b,c"))
    }

    @Test fun date_parseFull_roundTrips() {
        val now = 1_700_000_000_000L
        val parsed = DateUtil.parseFull(DateUtil.full(now))
        assertTrue(parsed != null)
        // minute precision round-trip
        assertEquals(now / 60000, parsed!! / 60000)
    }

    @Test fun money_formatsAndFallsBackForUnknownCode() {
        assertTrue(Money.format(1234.5, "USD").contains("1,234.5"))
        assertTrue(Money.format(10.0, "XYZ").contains("XYZ"))
    }

    @Test fun money_signed() {
        assertTrue(Money.formatSigned(5.0, "USD", isExpense = true).startsWith("-"))
        assertTrue(Money.formatSigned(5.0, "USD", isExpense = false).startsWith("+"))
    }
}
