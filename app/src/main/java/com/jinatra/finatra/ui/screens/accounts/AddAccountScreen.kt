package com.jinatra.finatra.ui.screens.accounts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jinatra.finatra.data.local.entity.AccountType
import com.jinatra.finatra.ui.components.ColorPickerRow
import com.jinatra.finatra.ui.components.FinatraTopBar
import com.jinatra.finatra.ui.components.LabeledDropdown
import com.jinatra.finatra.ui.components.OverlineLabel
import com.jinatra.finatra.ui.screens.onboarding.label
import com.jinatra.finatra.util.COMMON_CURRENCIES

@Composable
fun AddAccountScreen(
    onDone: () -> Unit,
    vm: AddAccountViewModel = hiltViewModel(),
) {
    val s by vm.state.collectAsStateWithLifecycle()
    Scaffold(
        topBar = { FinatraTopBar(if (s.isEditing) "Edit account" else "Add account", onBack = onDone) },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
    ) { padding ->
        Column(
            Modifier.fillMaxWidth().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            OutlinedTextField(s.name, vm::setName, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
            LabeledDropdown(
                label = "Type",
                options = AccountType.entries,
                selected = s.type,
                optionLabel = { it.label() },
                onSelect = vm::setType,
            )
            LabeledDropdown(
                label = "Currency",
                options = COMMON_CURRENCIES,
                selected = s.currency,
                optionLabel = { it },
                onSelect = vm::setCurrency,
            )
            OutlinedTextField(
                s.openingBalance, vm::setOpening,
                label = { Text("Opening balance") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                s.lowBalanceThreshold, vm::setLowBalance,
                label = { Text("Low-balance alert (0 = off)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
            )
            OverlineLabel("Card color")
            ColorPickerRow(selected = s.colorHex, onSelect = vm::setColor)
            Spacer(Modifier.height(8.dp))
            Button(onClick = { vm.save(onDone) }, enabled = s.name.isNotBlank(), modifier = Modifier.fillMaxWidth()) {
                Text(if (s.isEditing) "Save" else "Add account")
            }
        }
    }
}
