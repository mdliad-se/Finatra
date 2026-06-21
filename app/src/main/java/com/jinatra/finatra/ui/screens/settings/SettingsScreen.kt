package com.jinatra.finatra.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jinatra.finatra.data.prefs.ThemeMode
import com.jinatra.finatra.ui.components.ExpressiveCard
import com.jinatra.finatra.ui.components.IconChip
import com.jinatra.finatra.ui.components.LabeledDropdown
import com.jinatra.finatra.ui.components.OverlineLabel
import com.jinatra.finatra.util.COMMON_CURRENCIES

/**
 * Top-level Settings screen.
 *
 * Presents grouped preference cards — appearance (theme, dynamic color), general
 * (currency, language) and inline notification/security toggles — and navigation rows into
 * the detail sub-screens. It also hosts the open-source licenses dialog.
 *
 * @param onOpenAccounts navigate to account management.
 * @param onOpenCategories navigate to category management.
 * @param onOpenRecurring navigate to recurring transactions.
 * @param onOpenAi navigate to AI provider / on-device model settings.
 * @param onOpenSecurity navigate to PIN / biometric / auto-lock settings.
 * @param onOpenBackup navigate to backup, restore and CSV export.
 * @param onOpenAudit navigate to the audit log.
 * @param vm settings state/actions; injected via Hilt by default.
 */
@Composable
fun SettingsScreen(
    onOpenAccounts: () -> Unit,
    onOpenCategories: () -> Unit,
    onOpenRecurring: () -> Unit,
    onOpenAi: () -> Unit,
    onOpenSecurity: () -> Unit,
    onOpenBackup: () -> Unit,
    onOpenAudit: () -> Unit,
    vm: SettingsViewModel = hiltViewModel(),
) {
    val s by vm.settings.collectAsStateWithLifecycle()
    // Controls the open-source licenses dialog.
    var showLicenses by remember { mutableStateOf(false) }

    LazyColumn(
        Modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Spacer(Modifier.height(8.dp))
            Text("Settings", style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }

        // Brand header card
        item {
            ExpressiveCard(Modifier.fillMaxWidth(), container = MaterialTheme.colorScheme.secondaryContainer) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    IconChip(Icons.Filled.AccountBalanceWallet, tint = MaterialTheme.colorScheme.primary, size = 52.dp)
                    Column {
                        Text("Finatra", style = MaterialTheme.typography.titleLarge,
                            fontFamily = com.jinatra.finatra.ui.theme.NeganFont,
                            color = MaterialTheme.colorScheme.primary)
                        Text("Your finances, your way.", style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("v1.0 · A Jinatra product", style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        item {
            OverlineLabel("Appearance")
            ExpressiveCard(Modifier.fillMaxWidth().padding(top = 8.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ThemeMode.entries.forEach { m ->
                        FilterChip(
                            selected = s.themeMode == m,
                            onClick = { vm.setTheme(m) },
                            label = { Text(m.name.lowercase().replaceFirstChar { it.uppercase() }) },
                        )
                    }
                }
                ToggleRow("Dynamic color (Material You)", s.dynamicColor, vm::setDynamic)
            }
        }

        item {
            OverlineLabel("General")
            ExpressiveCard(Modifier.fillMaxWidth().padding(top = 8.dp), padding = 0.dp) {
                Box(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    LabeledDropdown(
                        label = "Base currency",
                        options = COMMON_CURRENCIES,
                        selected = s.baseCurrency,
                        optionLabel = { it },
                        onSelect = vm::setCurrency,
                    )
                }
                Box(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    LabeledDropdown(
                        label = "Language",
                        options = listOf("en", "bn"),
                        selected = s.language,
                        optionLabel = { if (it == "bn") "বাংলা (Bengali)" else "English" },
                        onSelect = vm::setLanguage,
                    )
                }
                NavRow(Icons.Filled.AccountBalanceWallet, "Accounts", onOpenAccounts)
                NavRow(Icons.Filled.Category, "Categories", onOpenCategories)
                NavRow(Icons.Filled.Repeat, "Recurring transactions", onOpenRecurring)
            }
        }

        item {
            OverlineLabel("AI")
            ExpressiveCard(Modifier.fillMaxWidth().padding(top = 8.dp), padding = 0.dp) {
                NavRow(Icons.Filled.AutoAwesome, "AI provider & on-device models", onOpenAi)
            }
        }

        item {
            OverlineLabel("Notifications")
            ExpressiveCard(Modifier.fillMaxWidth().padding(top = 8.dp)) {
                ToggleRow("Budget overspend alerts", s.notifBudget, vm::setNotifBudget)
                ToggleRow("Recurring reminders", s.notifRecurring, vm::setNotifRecurring)
                ToggleRow("Low balance warnings", s.notifLowBalance, vm::setNotifLowBalance)
                ToggleRow("Weekly summary", s.notifWeekly, vm::setNotifWeekly)
                ToggleRow("Monthly summary", s.notifMonthly, vm::setNotifMonthly)
            }
        }

        item {
            OverlineLabel("Security")
            ExpressiveCard(Modifier.fillMaxWidth().padding(top = 8.dp), padding = 0.dp) {
                Box(Modifier.padding(horizontal = 16.dp)) {
                    ToggleRow("Block screenshots", s.screenshotPrevention, vm::setScreenshotPrevention)
                }
                NavRow(Icons.Filled.Lock, "PIN, biometric & auto-lock", onOpenSecurity)
            }
        }

        item {
            OverlineLabel("Data")
            ExpressiveCard(Modifier.fillMaxWidth().padding(top = 8.dp), padding = 0.dp) {
                NavRow(Icons.AutoMirrored.Filled.ReceiptLong, "Backup, restore, export CSV", onOpenBackup)
                NavRow(Icons.Filled.History, "Audit log", onOpenAudit)
                NavRow(Icons.Filled.Description, "Open-source licenses") { showLicenses = true }
            }
        }

        item { Spacer(Modifier.height(8.dp)) }
    }

    if (showLicenses) {
        AlertDialog(
            onDismissRequest = { showLicenses = false },
            confirmButton = { TextButton(onClick = { showLicenses = false }) { Text("Close") } },
            title = { Text("Open-source licenses") },
            text = {
                Text(
                    """
                    Finatra is built with open-source software:

                    • Jetpack Compose & Material 3 — Apache 2.0
                    • AndroidX (Room, DataStore, WorkManager, Glance, Biometric, Security) — Apache 2.0
                    • Dagger Hilt — Apache 2.0
                    • Coil — Apache 2.0
                    • OkHttp — Apache 2.0
                    • MediaPipe Tasks GenAI — Apache 2.0
                    • Kotlin & Coroutines — Apache 2.0
                    • Poppins & Inter fonts (Google Fonts) — SIL Open Font License 1.1
                    """.trimIndent(),
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
        )
    }
}

/** A clickable settings row: leading icon chip, label, and a trailing chevron. */
@Composable
private fun NavRow(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            Modifier.size(38.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        }
        Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/** A settings row pairing a label with a trailing [Switch]. */
@Composable
private fun ToggleRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = onChange)
    }
}
