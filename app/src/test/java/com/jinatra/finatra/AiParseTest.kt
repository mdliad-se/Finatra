package com.jinatra.finatra

import com.jinatra.finatra.data.ai.AiService
import com.jinatra.finatra.data.local.entity.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Verifies the AI reply -> ParsedTx mapping, tolerant of surrounding prose. */
class AiParseTest {
    /** A well-formed JSON object maps to a ParsedTx with all fields populated. */
    @Test fun parsesCleanJson() {
        val p = AiService.parseTxJson("""{"amount": 500, "note": "Lunch", "type": "EXPENSE", "category": "Food"}""")!!
        assertEquals(500.0, p.amount!!, 0.001)
        assertEquals("Lunch", p.note)
        assertEquals(TransactionType.EXPENSE, p.type)
        assertEquals("Food", p.category)
    }

    /** JSON embedded in conversational prose is still extracted and parsed. */
    @Test fun extractsJsonFromProse() {
        val p = AiService.parseTxJson("""Sure! Here you go: {"amount": 4200, "type":"INCOME"} hope that helps""")!!
        assertEquals(4200.0, p.amount!!, 0.001)
        assertEquals(TransactionType.INCOME, p.type)
    }

    /** Input containing no JSON returns null instead of a partial/blank transaction. */
    @Test fun garbageReturnsNull() {
        assertNull(AiService.parseTxJson("no json here"))
    }

    /** Budget recommendations parse into a category->amount map, with zero-valued entries dropped. */
    @Test fun parsesBudgetRecommendations() {
        val m = AiService.parseBudgetJson("""Here you go: {"Food": 300, "Transport": 120.5, "Junk": 0} done""")!!
        assertEquals(300.0, m["Food"]!!, 0.001)
        assertEquals(120.5, m["Transport"]!!, 0.001)
        assertNull(m["Junk"])  // zero filtered out
    }

    /** Budget input with no parseable JSON returns null. */
    @Test fun budgetGarbageReturnsNull() {
        assertNull(AiService.parseBudgetJson("nope"))
    }
}
