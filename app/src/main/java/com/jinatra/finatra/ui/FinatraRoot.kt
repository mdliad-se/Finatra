package com.jinatra.finatra.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.jinatra.finatra.ui.navigation.Routes
import com.jinatra.finatra.ui.navigation.TopDestination
import com.jinatra.finatra.ui.screens.accounts.AccountsScreen
import com.jinatra.finatra.ui.screens.accounts.AddAccountScreen
import com.jinatra.finatra.ui.screens.addtransaction.AddTransactionScreen
import com.jinatra.finatra.ui.screens.analytics.AnalyticsScreen
import com.jinatra.finatra.ui.screens.audit.AuditLogScreen
import com.jinatra.finatra.ui.screens.budgets.AddBudgetScreen
import com.jinatra.finatra.ui.screens.budgets.BudgetsScreen
import com.jinatra.finatra.ui.screens.categories.CategoriesScreen
import com.jinatra.finatra.ui.screens.home.HomeScreen
import com.jinatra.finatra.ui.screens.onboarding.OnboardingScreen
import com.jinatra.finatra.ui.screens.recurring.RecurringScreen
import com.jinatra.finatra.ui.screens.settings.AiSettingsScreen
import com.jinatra.finatra.ui.screens.settings.BackupScreen
import com.jinatra.finatra.ui.screens.settings.SecurityScreen
import com.jinatra.finatra.ui.screens.settings.SettingsScreen
import com.jinatra.finatra.ui.screens.transactions.TransactionsScreen

@Composable
fun FinatraRoot(onboardingDone: Boolean) {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val showBottomBar = TopDestination.entries.any { it.route == currentRoute }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ) {
                    TopDestination.entries.forEach { dest ->
                        NavigationBarItem(
                            selected = currentRoute == dest.route,
                            onClick = {
                                navController.navigate(dest.route) {
                                    popUpTo(Routes.HOME) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(dest.icon, contentDescription = dest.label) },
                            label = {
                                Text(
                                    dest.label,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = if (onboardingDone) Routes.HOME else Routes.ONBOARDING,
            modifier = Modifier.padding(padding),
        ) {
            composable(Routes.ONBOARDING) {
                OnboardingScreen(onDone = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                })
            }
            composable(Routes.HOME) {
                HomeScreen(
                    onAddTransaction = { navController.navigate(Routes.ADD_TRANSACTION) },
                    onSeeAllTransactions = { navController.navigate(Routes.TRANSACTIONS) },
                    onOpenAccounts = { navController.navigate(Routes.ACCOUNTS) },
                    onOpenTransaction = { id -> navController.navigate("${Routes.ADD_TRANSACTION}?txId=$id") },
                    onAddForAccount = { accId, type ->
                        navController.navigate("${Routes.ADD_TRANSACTION}?accountId=$accId&type=$type")
                    },
                )
            }
            composable(Routes.TRANSACTIONS) {
                TransactionsScreen(
                    onAdd = { navController.navigate(Routes.ADD_TRANSACTION) },
                    onEdit = { id -> navController.navigate("${Routes.ADD_TRANSACTION}?txId=$id") },
                )
            }
            composable(Routes.BUDGETS) {
                BudgetsScreen(
                    onAdd = { navController.navigate(Routes.ADD_BUDGET) },
                    onEdit = { id -> navController.navigate("${Routes.ADD_BUDGET}?budgetId=$id") },
                )
            }
            composable(Routes.ANALYTICS) { AnalyticsScreen() }
            composable(Routes.SETTINGS) {
                SettingsScreen(
                    onOpenAccounts = { navController.navigate(Routes.ACCOUNTS) },
                    onOpenCategories = { navController.navigate(Routes.CATEGORIES) },
                    onOpenRecurring = { navController.navigate(Routes.RECURRING) },
                    onOpenAi = { navController.navigate(Routes.AI_SETTINGS) },
                    onOpenSecurity = { navController.navigate(Routes.SECURITY) },
                    onOpenBackup = { navController.navigate(Routes.BACKUP) },
                    onOpenAudit = { navController.navigate(Routes.AUDIT_LOG) },
                )
            }

            composable(
                "${Routes.ADD_TRANSACTION}?txId={txId}&accountId={accountId}&type={type}",
                arguments = listOf(
                    navArgument("txId") { type = NavType.LongType; defaultValue = -1L },
                    navArgument("accountId") { type = NavType.LongType; defaultValue = -1L },
                    navArgument("type") { type = NavType.StringType; defaultValue = "" },
                ),
            ) { AddTransactionScreen(onDone = { navController.popBackStack() }) }

            composable(Routes.ACCOUNTS) {
                AccountsScreen(
                    onAdd = { navController.navigate(Routes.ADD_ACCOUNT) },
                    onEdit = { id -> navController.navigate("${Routes.ADD_ACCOUNT}?accountId=$id") },
                    onBack = { navController.popBackStack() },
                )
            }
            composable(
                "${Routes.ADD_ACCOUNT}?accountId={accountId}",
                arguments = listOf(navArgument("accountId") { type = NavType.LongType; defaultValue = -1L }),
            ) { AddAccountScreen(onDone = { navController.popBackStack() }) }

            composable(
                "${Routes.ADD_BUDGET}?budgetId={budgetId}",
                arguments = listOf(navArgument("budgetId") { type = NavType.LongType; defaultValue = -1L }),
            ) { AddBudgetScreen(onDone = { navController.popBackStack() }) }

            composable(Routes.CATEGORIES) { CategoriesScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.RECURRING) { RecurringScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.AI_SETTINGS) { AiSettingsScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.SECURITY) { SecurityScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.BACKUP) { BackupScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.AUDIT_LOG) { AuditLogScreen(onBack = { navController.popBackStack() }) }
        }
    }
}
