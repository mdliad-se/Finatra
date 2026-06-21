package com.jinatra.finatra.ui.screens.addtransaction

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.jinatra.finatra.data.local.entity.TransactionType
import com.jinatra.finatra.ui.components.ExpressiveCard
import com.jinatra.finatra.ui.components.FinatraTopBar
import com.jinatra.finatra.ui.components.LabeledDropdown
import com.jinatra.finatra.ui.components.OverlineLabel
import com.jinatra.finatra.ui.components.SectionHeader
import com.jinatra.finatra.ui.components.SegmentedToggle
import com.jinatra.finatra.util.DateUtil
import com.jinatra.finatra.util.Money
import java.io.File

/**
 * The main transaction entry form, used for both adding and editing. Supports income/expense/
 * transfer types, account and category selection, split-across-categories expenses, quick-add
 * templates, AI natural-language parsing and category suggestion, receipt photo capture, and a
 * duplicate-transaction warning dialog.
 *
 * @param onDone invoked after a save (or as the back action).
 * @param vm owns the form state and all save/validation/AI logic.
 */
@Composable
fun AddTransactionScreen(
    onDone: () -> Unit,
    vm: AddTransactionViewModel = hiltViewModel(),
) {
    val s by vm.state.collectAsStateWithLifecycle()
    // Local-only text for the natural-language quick-add field (not part of persisted state).
    var quick by remember { mutableStateOf("") }
    var showSaveTemplate by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Receipt capture: write to app-private files/receipts, exposed via FileProvider.
    var pendingPath by remember { mutableStateOf<String?>(null) }
    val takePicture = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        if (ok) vm.setReceipt(pendingPath)
    }
    val pickImage = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            val dir = File(context.filesDir, "receipts").apply { mkdirs() }
            val dest = File(dir, "rcpt_${System.currentTimeMillis()}.jpg")
            runCatching {
                context.contentResolver.openInputStream(uri)?.use { input ->
                    dest.outputStream().use { input.copyTo(it) }
                }
            }
            vm.setReceipt(dest.absolutePath)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
        topBar = {
            FinatraTopBar(if (s.isEditing) "Edit transaction" else "Add transaction", onBack = onDone)
        },
    ) { padding ->
        Column(
            Modifier.fillMaxWidth().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // AI quick-parse (natural language entry)
            ExpressiveCard(modifier = Modifier.fillMaxWidth()) {
                OverlineLabel("Quick add")
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = quick,
                    onValueChange = { quick = it },
                    label = { Text("Quick add — e.g. \"spent 500 on lunch\"") },
                    shape = MaterialTheme.shapes.medium,
                    trailingIcon = {
                        if (s.aiBusy) {
                            CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
                        } else {
                            IconButton(onClick = { if (quick.isNotBlank()) vm.aiParse(quick) }) {
                                Icon(Icons.Filled.AutoAwesome, contentDescription = "Parse")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    if (s.aiAvailable) "Powered by your AI provider" else "Basic parser — add an AI key in Settings for smarter parsing",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (!s.isEditing && s.templates.isNotEmpty()) {
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    s.templates.forEach { t ->
                        AssistChip(onClick = { vm.applyTemplate(t) }, label = { Text(t.name) })
                    }
                }
            }

            ExpressiveCard(modifier = Modifier.fillMaxWidth()) {
                OverlineLabel("Type")
                Spacer(Modifier.height(8.dp))
                SegmentedToggle(
                    options = TransactionType.entries.map { it.label() },
                    selectedIndex = TransactionType.entries.indexOf(s.type),
                    onSelect = { vm.setType(TransactionType.entries[it]) },
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(14.dp))
                OutlinedTextField(
                    value = s.amount,
                    onValueChange = vm::setAmount,
                    label = { Text("Amount") },
                    shape = MaterialTheme.shapes.medium,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            ExpressiveCard(modifier = Modifier.fillMaxWidth()) {
            if (s.accounts.isNotEmpty()) {
                LabeledDropdown(
                    label = if (s.type == TransactionType.TRANSFER) "From account" else "Account",
                    options = s.accounts,
                    selected = s.accounts.firstOrNull { it.id == s.accountId } ?: s.accounts.first(),
                    optionLabel = { it.name },
                    onSelect = { vm.setAccount(it.id) },
                )
            } else {
                Text("Create an account first (Settings → Accounts).")
            }

            if (s.type == TransactionType.TRANSFER) {
                if (s.accounts.size > 1) {
                    LabeledDropdown(
                        label = "To account",
                        options = s.accounts.filter { it.id != s.accountId },
                        selected = s.accounts.firstOrNull { it.id == s.transferToAccountId }
                            ?: s.accounts.first { it.id != s.accountId },
                        optionLabel = { it.name },
                        onSelect = { vm.setTransferTo(it.id) },
                    )
                }
            } else if (s.visibleCategories.isNotEmpty()) {
                if (s.canSplit) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("Split across categories", Modifier.weight(1f))
                        androidx.compose.material3.Switch(checked = s.splitMode, onCheckedChange = vm::toggleSplit)
                    }
                }
                if (s.splitMode && s.canSplit) {
                    Spacer(Modifier.height(8.dp))
                    s.splits.forEachIndexed { i, sp ->
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(Modifier.weight(1f)) {
                                LabeledDropdown(
                                    label = "Category",
                                    options = s.visibleCategories,
                                    selected = s.visibleCategories.firstOrNull { it.id == sp.categoryId } ?: s.visibleCategories.first(),
                                    optionLabel = { it.name },
                                    onSelect = { vm.setSplitCategory(i, it.id) },
                                )
                            }
                            OutlinedTextField(
                                value = sp.amount,
                                onValueChange = { vm.setSplitAmount(i, it) },
                                label = { Text("Amt") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.width(110.dp),
                            )
                            IconButton(onClick = { vm.removeSplitRow(i) }, enabled = s.splits.size > 2) {
                                Icon(Icons.Filled.Close, contentDescription = "Remove split")
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                    }
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = { vm.addSplitRow() }) {
                            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.size(6.dp)); Text("Add split")
                        }
                        Spacer(Modifier.weight(1f))
                        Text("Total: ${com.jinatra.finatra.util.Money.format(s.splitTotal, s.accounts.firstOrNull { it.id == s.accountId }?.currency ?: s.baseCurrency)}",
                            style = MaterialTheme.typography.labelLarge)
                    }
                } else {
                    LabeledDropdown(
                        label = "Category",
                        options = s.visibleCategories,
                        selected = s.visibleCategories.firstOrNull { it.id == s.categoryId } ?: s.visibleCategories.first(),
                        optionLabel = { it.name },
                        onSelect = { vm.setCategory(it.id) },
                    )
                    if (s.aiAvailable && s.note.isNotBlank()) {
                        TextButton(onClick = { vm.suggestCategory() }, enabled = !s.aiBusy) {
                            Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.size(6.dp))
                            Text("Suggest category (AI)")
                        }
                    }
                }
            }
            }

            ExpressiveCard(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = s.note,
                    onValueChange = vm::setNote,
                    label = { Text("Note") },
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(14.dp))
                OutlinedTextField(
                    value = s.tags,
                    onValueChange = vm::setTags,
                    label = { Text("Tags (comma separated)") },
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(14.dp))
                SectionHeader("Date: ${DateUtil.full(s.dateTime)}")
            }

            // Receipt photo (PRD 6.3)
            ExpressiveCard(modifier = Modifier.fillMaxWidth()) {
                OverlineLabel("Receipt")
                Spacer(Modifier.height(8.dp))
                if (s.receiptPath != null) {
                    AsyncImage(
                        model = s.receiptPath,
                        contentDescription = "Receipt",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(12.dp)),
                    )
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = { vm.setReceipt(null) }) { Text("Remove receipt") }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(
                            onClick = {
                                val dir = File(context.filesDir, "receipts").apply { mkdirs() }
                                val dest = File(dir, "rcpt_${System.currentTimeMillis()}.jpg")
                                val uri = FileProvider.getUriForFile(
                                    context, "${context.packageName}.fileprovider", dest,
                                )
                                pendingPath = dest.absolutePath
                                takePicture.launch(uri)
                            },
                            modifier = Modifier.weight(1f),
                        ) {
                            Icon(Icons.Filled.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.size(6.dp))
                            Text("Camera")
                        }
                        OutlinedButton(onClick = { pickImage.launch("image/*") }, modifier = Modifier.weight(1f)) {
                            Text("Gallery")
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            // Enabled once an account is chosen and either the split rows are valid (split mode)
            // or the amount parses as a number.
            Button(
                onClick = { vm.save(onDone) },
                enabled = s.accountId != null &&
                    (if (s.splitMode && s.canSplit) s.splitValid else s.amount.toDoubleOrNull() != null),
                modifier = Modifier.fillMaxWidth(),
            ) { Text(if (s.isEditing) "Save changes" else if (s.splitMode && s.canSplit) "Add split" else "Add transaction") }

            if (!s.isEditing && !(s.splitMode && s.canSplit) && s.amount.toDoubleOrNull() != null) {
                TextButton(onClick = { showSaveTemplate = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("Save as template")
                }
            }
        }
    }

    // Duplicate-transaction warning (PRD 6.4).
    s.duplicate?.let { dup ->
        AlertDialog(
            onDismissRequest = { vm.dismissDuplicate() },
            title = { Text("Possible duplicate") },
            text = {
                Text("A ${Money.format(dup.amount, dup.currency)} entry on this account already exists at " +
                    "${DateUtil.full(dup.dateTime)}. Save anyway?")
            },
            confirmButton = {
                TextButton(onClick = { vm.dismissDuplicate(); vm.save(onDone, confirmed = true) }) { Text("Save anyway") }
            },
            dismissButton = { TextButton(onClick = { vm.dismissDuplicate() }) { Text("Cancel") } },
        )
    }

    if (showSaveTemplate) {
        var tname by remember { mutableStateOf(s.note.ifBlank { "" }) }
        AlertDialog(
            onDismissRequest = { showSaveTemplate = false },
            title = { Text("Save as template") },
            text = {
                OutlinedTextField(tname, { tname = it }, label = { Text("Template name") }, singleLine = true)
            },
            confirmButton = {
                TextButton(enabled = tname.isNotBlank(), onClick = { vm.saveAsTemplate(tname); showSaveTemplate = false }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { showSaveTemplate = false }) { Text("Cancel") } },
        )
    }
}

private fun TransactionType.label() = when (this) {
    TransactionType.INCOME -> "Income"
    TransactionType.EXPENSE -> "Expense"
    TransactionType.TRANSFER -> "Transfer"
}
