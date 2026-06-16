package com.jinatra.finatra.ui.screens.settings

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jinatra.finatra.ui.components.FinatraTopBar
import com.jinatra.finatra.ui.components.SectionHeader
import kotlinx.coroutines.launch

@Composable
fun BackupScreen(onBack: () -> Unit, vm: BackupViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    fun writeTo(uri: android.net.Uri?, content: String, doneMsg: String) {
        if (uri == null) return
        runCatching {
            context.contentResolver.openOutputStream(uri)?.use { it.write(content.toByteArray()) }
        }
        Toast.makeText(context, doneMsg, Toast.LENGTH_SHORT).show()
    }

    val exportCsv = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        scope.launch { writeTo(uri, vm.buildTransactionsCsv(), "Exported CSV") }
    }
    val exportJson = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        scope.launch { writeTo(uri, vm.buildBackupJson(), "Backup saved") }
    }
    val restore = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            val text = runCatching { context.contentResolver.openInputStream(uri)?.bufferedReader()?.readText() }.getOrNull()
            if (text != null) vm.restoreFromJson(text) { ok ->
                Toast.makeText(context, if (ok) "Restored" else "Restore failed", Toast.LENGTH_SHORT).show()
            }
        }
    }
    val importCsv = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            val text = runCatching { context.contentResolver.openInputStream(uri)?.bufferedReader()?.readText() }.getOrNull()
            if (text != null) vm.importCsv(text) { imported, skipped ->
                Toast.makeText(context, "Imported $imported, skipped $skipped", Toast.LENGTH_LONG).show()
            }
        }
    }

    Scaffold(
        topBar = { FinatraTopBar("Backup & data", onBack = onBack) },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
    ) { padding ->
        Column(Modifier.fillMaxWidth().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("All files stay on your device. No cloud upload.",
                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

            SectionHeader("Export")
            Button(onClick = { exportCsv.launch("finatra_transactions.csv") }, modifier = Modifier.fillMaxWidth()) {
                Text("Export transactions to CSV")
            }
            Button(onClick = { exportJson.launch("finatra_backup.json") }, modifier = Modifier.fillMaxWidth()) {
                Text("Full backup (JSON)")
            }

            SectionHeader("Import & restore")
            OutlinedButton(onClick = { importCsv.launch(arrayOf("text/csv", "text/comma-separated-values", "text/plain")) }, modifier = Modifier.fillMaxWidth()) {
                Text("Import transactions from CSV")
            }
            OutlinedButton(onClick = { restore.launch(arrayOf("application/json")) }, modifier = Modifier.fillMaxWidth()) {
                Text("Restore from backup file")
            }
        }
    }
}
