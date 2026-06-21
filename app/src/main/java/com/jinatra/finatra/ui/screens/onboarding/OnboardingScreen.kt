package com.jinatra.finatra.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.hilt.navigation.compose.hiltViewModel
import com.jinatra.finatra.data.local.entity.AccountType
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.jinatra.finatra.ui.components.ColorPickerRow
import com.jinatra.finatra.ui.components.ExpressiveCard
import com.jinatra.finatra.ui.components.LabeledDropdown
import com.jinatra.finatra.ui.components.OverlineLabel
import com.jinatra.finatra.util.COMMON_CURRENCIES

/**
 * First-run onboarding screen. Collects the user's name, base currency, first account
 * (name, type, opening balance, color), and optional app PIN and AI provider/key. Both
 * "Get started" and "Skip for now" call the same [finish] handler — skipping simply leaves
 * fields at their defaults — which persists the setup via [OnboardingViewModel] and then
 * invokes [onDone] to leave onboarding.
 */
@Composable
fun OnboardingScreen(
    onDone: () -> Unit,
    vm: OnboardingViewModel = hiltViewModel(),
) {
    var userName by remember { mutableStateOf("") }
    var currency by remember { mutableStateOf("USD") }
    var accountName by remember { mutableStateOf("Cash") }
    var accountType by remember { mutableStateOf(AccountType.CASH) }
    var balanceText by remember { mutableStateOf("") }
    var color by remember { mutableLongStateOf(0xFFE05454) }
    var pin by remember { mutableStateOf("") }
    var aiProvider by remember { mutableStateOf("Gemini") }
    var aiApiKey by remember { mutableStateOf("") }

    // Single submit path shared by both the primary and "skip" buttons; unset fields fall
    // back to their defaults and the ViewModel decides what is actually persisted.
    val finish = {
        vm.finish(
            userName = userName,
            baseCurrency = currency,
            accountName = accountName,
            accountType = accountType,
            openingBalance = balanceText.toDoubleOrNull() ?: 0.0,
            colorHex = color,
            pin = pin,
            aiProvider = aiProvider,
            aiApiKey = aiApiKey,
            onComplete = onDone,
        )
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Spacer(Modifier.height(24.dp))
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(if (isSystemInDarkTheme()) Color(0xFF222222) else Color(0xFFFFEACF)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = com.jinatra.finatra.R.drawable.ic_launcher_foreground),
                contentDescription = "Finatra Logo",
                tint = Color.Unspecified,
                modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            "Finatra",
            style = MaterialTheme.typography.displayLarge,
            fontFamily = com.jinatra.finatra.ui.theme.NeganFont,
            color = MaterialTheme.colorScheme.primary,
        )
        Text("Your finances, your way.", style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            "All data stays on this device. No cloud, no tracking. Let's set up your profile and first account.",
            style = MaterialTheme.typography.bodyMedium,
        )

        ExpressiveCard(modifier = Modifier.fillMaxWidth()) {
            OverlineLabel("Your profile")
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = userName,
                onValueChange = { userName = it },
                label = { Text("Your name") },
                placeholder = { Text("e.g. Alex") },
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        ExpressiveCard(modifier = Modifier.fillMaxWidth()) {
            OverlineLabel("Base currency")
            Spacer(Modifier.height(12.dp))
            LabeledDropdown(
                label = "Currency",
                options = COMMON_CURRENCIES,
                selected = currency,
                optionLabel = { it },
                onSelect = { currency = it },
            )
        }

        ExpressiveCard(modifier = Modifier.fillMaxWidth()) {
            OverlineLabel("First account")
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = accountName,
                onValueChange = { accountName = it },
                label = { Text("Account name") },
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))
            LabeledDropdown(
                label = "Type",
                options = AccountType.entries,
                selected = accountType,
                optionLabel = { it.label() },
                onSelect = { accountType = it },
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = balanceText,
                onValueChange = { balanceText = it.filter { c -> c.isDigit() || c == '.' || c == '-' } },
                label = { Text("Opening balance") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))
            OverlineLabel("Card color")
            Spacer(Modifier.height(8.dp))
            ColorPickerRow(selected = color, onSelect = { color = it })
        }

        ExpressiveCard(modifier = Modifier.fillMaxWidth()) {
            OverlineLabel("Security & AI (optional)")
            Spacer(Modifier.height(4.dp))
            Text(
                "Set an app PIN and connect an AI provider. You can skip this and do it later.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = pin,
                onValueChange = { pin = it.filter { c -> c.isDigit() }.take(6) },
                label = { Text("App PIN (4-6 digits)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))
            LabeledDropdown(
                label = "AI provider",
                options = listOf("Gemini", "Claude", "OpenRouter"),
                selected = aiProvider,
                optionLabel = { it },
                onSelect = { aiProvider = it },
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = aiApiKey,
                onValueChange = { aiApiKey = it },
                label = { Text("API key") },
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = finish,
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Get started") }
        TextButton(
            onClick = finish,
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Skip for now") }
    }
}

/** Human-readable label for each account type, used in dropdowns and account cards. */
fun AccountType.label(): String = when (this) {
    AccountType.CASH -> "Cash"
    AccountType.BANK -> "Bank Account"
    AccountType.CREDIT_CARD -> "Credit Card"
    AccountType.MOBILE_WALLET -> "Mobile Wallet"
    AccountType.CRYPTO -> "Crypto"
    AccountType.TRADING -> "Trading / Brokerage"
    AccountType.INVESTMENT -> "Investment / Savings"
}
