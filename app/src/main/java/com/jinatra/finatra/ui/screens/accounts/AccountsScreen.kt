package com.jinatra.finatra.ui.screens.accounts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jinatra.finatra.ui.components.EmptyState
import com.jinatra.finatra.ui.components.ExpressiveCard
import com.jinatra.finatra.ui.components.FinatraTopBar
import com.jinatra.finatra.ui.screens.onboarding.label
import com.jinatra.finatra.util.CategoryIcons
import com.jinatra.finatra.util.Money

@Composable
fun AccountsScreen(
    onAdd: () -> Unit,
    onEdit: (Long) -> Unit,
    onBack: () -> Unit,
    vm: AccountsViewModel = hiltViewModel(),
) {
    val accounts by vm.accounts.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { FinatraTopBar("Accounts", onBack = onBack) },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAdd,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) { Icon(Icons.Filled.Add, contentDescription = "Add account") }
        },
    ) { padding ->
        if (accounts.isEmpty()) {
            EmptyState("No accounts yet. Add cash, bank, card, or mobile wallet.", Modifier.padding(padding))
        } else {
            LazyColumn(
                Modifier.fillMaxSize().padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(accounts, key = { it.account.id }) { awb ->
                    AccountCard(awb) { onEdit(awb.account.id) }
                }
            }
        }
    }
}

@Composable
private fun AccountCard(awb: AccountWithBalance, onClick: () -> Unit) {
    val bg = Color(awb.account.colorHex)
    val onColor = if (bg.luminance() > 0.5f) Color.Black else Color.White
    ExpressiveCard(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        container = bg,
        contentColor = onColor,
        border = false,
        padding = 20.dp,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(44.dp).clip(CircleShape).background(onColor.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(CategoryIcons.forAccount(awb.account.type), contentDescription = null, tint = onColor)
            }
            Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
                Text(awb.account.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(awb.account.type.label(), style = MaterialTheme.typography.labelMedium)
            }
            Text(
                Money.format(awb.balance, awb.account.currency),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}
