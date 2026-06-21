package com.jinatra.finatra.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jinatra.finatra.ui.components.ExpressiveCard
import com.jinatra.finatra.ui.components.FinatraTopBar
import com.jinatra.finatra.ui.components.LabeledDropdown
import com.jinatra.finatra.ui.components.OverlineLabel

/**
 * Security settings screen.
 *
 * Lets the user manage the app lock: set/change/remove the PIN, toggle biometric unlock,
 * configure the optional decoy PIN (shown only once a real PIN exists), choose an auto-lock
 * delay, and define a usage-lock schedule (shown only when a lock method is active). PIN
 * entry uses masked, numeric-password fields and validates 4- or 6-digit length.
 *
 * @param onBack invoked when navigating back.
 * @param vm screen state/actions; injected via Hilt by default.
 */
@Composable
fun SecurityScreen(onBack: () -> Unit, vm: SecurityViewModel = hiltViewModel()) {
    val s by vm.state.collectAsStateWithLifecycle()
    var showPin by remember { mutableStateOf(false) }
    var showDecoy by remember { mutableStateOf(false) }
    // Local mirrors of PIN existence so the UI reflects set/remove without re-querying state.
    var hasPin by remember { mutableStateOf(vm.hasPin()) }
    var hasDecoy by remember { mutableStateOf(vm.hasDecoyPin()) }

    // Auto-lock delay choices in minutes; 0 = immediately, -1 = never.
    val lockOptions = listOf(0, 1, 5, 15, 30, -1)

    Scaffold(
        topBar = { FinatraTopBar("Security", onBack = onBack) },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
    ) { padding ->
        Column(Modifier.fillMaxWidth().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)) {

            ExpressiveCard(Modifier.fillMaxWidth()) {
                OverlineLabel("App lock")
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(if (hasPin) "PIN is set" else "No PIN set", Modifier.weight(1f))
                    if (hasPin) OutlinedButton(onClick = { vm.clearPin(); hasPin = false }) { Text("Remove") }
                    Button(onClick = { showPin = true }) { Text(if (hasPin) "Change" else "Set PIN") }
                }

                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Biometric unlock", Modifier.weight(1f))
                    Switch(checked = s.biometricEnabled, onCheckedChange = { vm.setBiometric(it) })
                }
            }

            if (hasPin) {
                ExpressiveCard(Modifier.fillMaxWidth()) {
                    OverlineLabel("Decoy PIN")
                    Text(
                        "A second PIN that opens the app to a clean, empty state. Use it under pressure.",
                        style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Row(Modifier.fillMaxWidth().padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(if (hasDecoy) "Decoy PIN is set" else "No decoy PIN", Modifier.weight(1f))
                        if (hasDecoy) OutlinedButton(onClick = { vm.clearDecoyPin(); hasDecoy = false }) { Text("Remove") }
                        Button(onClick = { showDecoy = true }) { Text(if (hasDecoy) "Change" else "Set") }
                    }
                }
            }

            ExpressiveCard(Modifier.fillMaxWidth()) {
                OverlineLabel("Auto-lock")
                LabeledDropdown(
                    label = "Lock after",
                    options = lockOptions,
                    selected = s.autoLockMinutes,
                    optionLabel = {
                        when (it) {
                            0 -> "Immediately"
                            -1 -> "Never"
                            else -> "$it min"
                        }
                    },
                    onSelect = { vm.setAutoLock(it) },
                )
            }

            if (hasPin || s.biometricEnabled) {
                ExpressiveCard(Modifier.fillMaxWidth()) {
                    OverlineLabel("Usage lock schedule")
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("Lock during set hours", Modifier.weight(1f))
                        Switch(checked = s.usageLockEnabled, onCheckedChange = { vm.setUsageLock(it) })
                    }
                    if (s.usageLockEnabled) {
                        LabeledDropdown(
                            label = "From",
                            options = (0..23).toList(),
                            selected = s.usageLockStartHour,
                            optionLabel = { "%02d:00".format(it) },
                            onSelect = { vm.setUsageLockStart(it) },
                        )
                        LabeledDropdown(
                            label = "Until",
                            options = (0..23).toList(),
                            selected = s.usageLockEndHour,
                            optionLabel = { "%02d:00".format(it) },
                            onSelect = { vm.setUsageLockEnd(it) },
                        )
                        Text("App stays locked between these hours.",
                            style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Text("Screenshot blocking is toggled on the main Settings screen.",
                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }

    if (showPin) {
        var pin by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showPin = false },
            title = { Text("Set PIN") },
            text = {
                OutlinedTextField(
                    value = pin,
                    onValueChange = { if (it.length <= 6 && it.all(Char::isDigit)) pin = it },
                    label = { Text("4 or 6 digit PIN") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = PasswordVisualTransformation(),
                )
            },
            confirmButton = {
                TextButton(
                    enabled = pin.length == 4 || pin.length == 6,
                    onClick = { vm.setPin(pin); hasPin = true; showPin = false },
                ) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { showPin = false }) { Text("Cancel") } },
        )
    }

    // Decoy-PIN dialog: identical to the PIN dialog but additionally rejects a value that
    // equals the real PIN (a decoy must be distinct to be useful).
    if (showDecoy) {
        var pin by remember { mutableStateOf("") }
        // Only flag a clash once the entry is a complete 4-/6-digit PIN.
        val clash = (pin.length == 4 || pin.length == 6) && vm.decoyMatchesReal(pin)
        AlertDialog(
            onDismissRequest = { showDecoy = false },
            title = { Text("Set decoy PIN") },
            text = {
                Column {
                    OutlinedTextField(
                        value = pin,
                        onValueChange = { if (it.length <= 6 && it.all(Char::isDigit)) pin = it },
                        label = { Text("4 or 6 digit PIN") },
                        isError = clash,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation(),
                    )
                    if (clash) Text("Must differ from your real PIN",
                        color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelMedium)
                }
            },
            confirmButton = {
                TextButton(
                    enabled = (pin.length == 4 || pin.length == 6) && !clash,
                    onClick = { vm.setDecoyPin(pin); hasDecoy = true; showDecoy = false },
                ) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { showDecoy = false }) { Text("Cancel") } },
        )
    }
}
