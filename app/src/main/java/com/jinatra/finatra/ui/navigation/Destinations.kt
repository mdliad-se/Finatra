package com.jinatra.finatra.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.jinatra.finatra.R

/**
 * Central registry of navigation route strings used by the [com.jinatra.finatra.ui.FinatraRoot]
 * NavHost. Routes annotated with a query/path comment accept arguments (e.g. `?txId={id}`).
 */
object Routes {
    const val ONBOARDING = "onboarding"
    const val QUIZ = "quiz"
    const val HOME = "home"
    const val TRANSACTIONS = "transactions"
    const val BUDGETS = "budgets"
    const val ANALYTICS = "analytics"
    const val SETTINGS = "settings"
    const val ADD_TRANSACTION = "add_transaction"          // ?txId={id}
    const val ACCOUNTS = "accounts"
    const val ADD_ACCOUNT = "add_account"                  // ?accountId={id}
    const val ACCOUNT_DETAIL = "account_detail"            // /{accountId}
    const val ADD_BUDGET = "add_budget"                    // ?budgetId={id}
    const val BUDGET_CHAT = "budget_chat"
    const val CATEGORIES = "categories"
    const val RECURRING = "recurring"
    const val AI_SETTINGS = "ai_settings"
    const val SECURITY = "security"
    const val AUDIT_LOG = "audit_log"
    const val BACKUP = "backup"
    const val GOALS = "goals"
    const val AI_COACH = "ai_coach"
    const val CALENDAR = "calendar"
    const val ACHIEVEMENTS = "achievements"
}

/**
 * The five top-level tabs shown in the bottom [androidx.compose.material3.NavigationBar].
 * Each entry pairs a [route] with its localized [labelRes] and bottom-bar [icon].
 */
enum class TopDestination(val route: String, val labelRes: Int, val icon: ImageVector) {
    HOME(Routes.HOME, R.string.nav_home, Icons.Filled.Home),
    TRANSACTIONS(Routes.TRANSACTIONS, R.string.nav_transactions, Icons.AutoMirrored.Filled.ReceiptLong),
    BUDGETS(Routes.BUDGETS, R.string.nav_budgets, Icons.Filled.PieChart),
    ANALYTICS(Routes.ANALYTICS, R.string.nav_analytics, Icons.Filled.BarChart),
    SETTINGS(Routes.SETTINGS, R.string.nav_settings, Icons.Filled.Settings),
}
