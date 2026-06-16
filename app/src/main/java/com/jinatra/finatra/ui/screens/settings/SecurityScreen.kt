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

@Composable
fun SecurityScreen(onBack: () -> Unit, vm: SecurityViewModel = hiltViewModel()) {
    val s by vm.state.collectAsStateWithLifecycle()
    var showPin by remember { mutableStateOf(false) }
    var hasPin by remember { mutableStateOf(vm.hasPin()) }

    val lockOptions = listOf(1, 5, 15, 30, 0)

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

            ExpressiveCard(Modifier.fillMaxWidth()) {
                OverlineLabel("Auto-lock")
                LabeledDropdown(
                    label = "Lock after",
                    options = lockOptions,
                    selected = s.autoLockMinutes,
                    optionLabel = { if (it == 0) "Never" else "$it min" },
                    onSelect = { vm.setAutoLock(it) },
                )
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
}
