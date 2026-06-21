package com.jinatra.finatra.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CandlestickChart
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.CurrencyBitcoin
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalGroceryStore
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.ui.graphics.vector.ImageVector
import com.jinatra.finatra.data.local.entity.AccountType

/** Maps stored category/account icon identifiers to Material [ImageVector] icons for display. */
object CategoryIcons {
    /** Resolve a category's persisted icon [key] to its icon; unknown keys fall back to a generic Category icon. */
    fun forKey(key: String): ImageVector = when (key) {
        "restaurant" -> Icons.Filled.Restaurant
        "directions_car" -> Icons.Filled.DirectionsCar
        "home" -> Icons.Filled.Home
        "favorite" -> Icons.Filled.Favorite
        "movie" -> Icons.Filled.Movie
        "shopping_bag" -> Icons.Filled.ShoppingBag
        "receipt_long" -> Icons.Filled.ReceiptLong
        "school" -> Icons.Filled.School
        "local_grocery_store" -> Icons.Filled.LocalGroceryStore
        "payments" -> Icons.Filled.Payments
        "storefront" -> Icons.Filled.Storefront
        "card_giftcard" -> Icons.Filled.CardGiftcard
        "trending_up" -> Icons.Filled.TrendingUp
        else -> Icons.Filled.Category
    }

    /** Icon representing an account's [type] (cash, bank, card, wallet, crypto, etc.). */
    fun forAccount(type: AccountType): ImageVector = when (type) {
        AccountType.CASH -> Icons.Filled.AccountBalanceWallet
        AccountType.BANK -> Icons.Filled.AccountBalance
        AccountType.CREDIT_CARD -> Icons.Filled.CreditCard
        AccountType.MOBILE_WALLET -> Icons.Filled.PhoneAndroid
        AccountType.CRYPTO -> Icons.Filled.CurrencyBitcoin
        AccountType.TRADING -> Icons.Filled.CandlestickChart
        AccountType.INVESTMENT -> Icons.Filled.Savings
    }
}
