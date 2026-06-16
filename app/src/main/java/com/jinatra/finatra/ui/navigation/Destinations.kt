package com.jinatra.finatra.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

object Routes {
    const val ONBOARDING = "onboarding"
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
    const val CATEGORIES = "categories"
    const val RECURRING = "recurring"
    const val AI_SETTINGS = "ai_settings"
    const val SECURITY = "security"
    const val AUDIT_LOG = "audit_log"
    const val BACKUP = "backup"
}

enum class TopDestination(val route: String, val label: String, val icon: ImageVector) {
    HOME(Routes.HOME, "Home", Icons.Filled.Home),
    TRANSACTIONS(Routes.TRANSACTIONS, "Transactions", Icons.AutoMirrored.Filled.ReceiptLong),
    BUDGETS(Routes.BUDGETS, "Budgets", Icons.Filled.PieChart),
    ANALYTICS(Routes.ANALYTICS, "Analytics", Icons.Filled.BarChart),
    SETTINGS(Routes.SETTINGS, "Settings", Icons.Filled.Settings),
}
