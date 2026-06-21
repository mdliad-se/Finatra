package com.jinatra.finatra.util

import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

/**
 * Currency formatting helpers used across the UI.
 *
 * Amounts are plain [Double] values (the app stores monetary amounts as doubles, not minor units).
 * Formatting uses two decimal places and the active locale's grouping/decimal convention and digits
 * (e.g. Bengali shows ৳১,২৩৪.৫৬), so output follows the user's selected language; the currency symbol
 * is resolved separately by ISO code via [getSymbol].
 */
object Money {
    // Preferred display symbols by ISO 4217 code; overrides the JDK symbol, which can differ by locale.
    private val CurrencySymbols = mapOf(
        "USD" to "$",
        "EUR" to "€",
        "GBP" to "£",
        "BDT" to "৳",
        "INR" to "₹",
        "JPY" to "¥",
        "CNY" to "¥",
        "AUD" to "A$",
        "CAD" to "C$",
        "AED" to "د.إ",
        "SAR" to "ر.س",
        "PKR" to "₨"
    )

    /**
     * Display symbol for an ISO 4217 [currencyCode]. Falls back to the JDK's locale symbol, and to
     * the raw code itself if the code is unknown/invalid (e.g. a typo), so this never throws.
     */
    fun getSymbol(currencyCode: String): String {
        return CurrencySymbols[currencyCode] ?: try {
            java.util.Currency.getInstance(currencyCode).getSymbol(Locale.getDefault())
        } catch (e: Exception) {
            currencyCode
        }
    }

    /**
     * Format [amount] as "<symbol><grouped number>", e.g. `$1,234.56`. Always two decimals, no space
     * between symbol and number, using the active locale's separators and digits. The sign is
     * preserved as-is (a negative amount yields e.g. `$-5.00`); use [formatSigned] for an explicit
     * leading +/- on the absolute value.
     */
    fun format(amount: Double, currencyCode: String): String {
        val symbol = getSymbol(currencyCode)
        val formattedNumber = "%,.2f".format(Locale.getDefault(), amount)
        return "$symbol$formattedNumber"
    }

    /**
     * Format the magnitude of [amount] with an explicit leading sign: `-` when [isExpense] is true,
     * otherwise `+` (e.g. `-$5.00` / `+$5.00`). The input sign of [amount] is ignored; only [isExpense]
     * decides the displayed sign.
     */
    fun formatSigned(amount: Double, currencyCode: String, isExpense: Boolean): String {
        val sign = if (isExpense) "-" else "+"
        return "$sign${format(kotlin.math.abs(amount), currencyCode)}"
    }
}

/** Common currency codes for pickers. */
val COMMON_CURRENCIES = listOf(
    "USD", "EUR", "GBP", "BDT", "INR", "JPY", "CNY", "AUD", "CAD", "AED", "SAR", "PKR"
)
