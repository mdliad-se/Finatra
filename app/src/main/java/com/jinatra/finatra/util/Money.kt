package com.jinatra.finatra.util

import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

object Money {
    fun format(amount: Double, currencyCode: String): String = try {
        NumberFormat.getCurrencyInstance(Locale.getDefault()).apply {
            currency = Currency.getInstance(currencyCode)
            maximumFractionDigits = 2
        }.format(amount)
    } catch (e: Exception) {
        // Unknown ISO code (e.g. custom) — fall back to plain number + code.
        "%,.2f %s".format(amount, currencyCode)
    }

    fun formatSigned(amount: Double, currencyCode: String, isExpense: Boolean): String {
        val sign = if (isExpense) "-" else "+"
        return "$sign${format(kotlin.math.abs(amount), currencyCode)}"
    }
}

/** Common currency codes for pickers. */
val COMMON_CURRENCIES = listOf(
    "USD", "EUR", "GBP", "BDT", "INR", "JPY", "CNY", "AUD", "CAD", "AED", "SAR", "PKR"
)
