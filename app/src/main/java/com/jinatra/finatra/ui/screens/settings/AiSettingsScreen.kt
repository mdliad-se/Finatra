package com.jinatra.finatra.ui.screens.settings

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jinatra.finatra.ui.components.ExpressiveCard
import com.jinatra.finatra.ui.components.FinatraTopBar
import com.jinatra.finatra.ui.components.LabeledDropdown
import com.jinatra.finatra.ui.components.SectionHeader

@Composable
fun AiSettingsScreen(onBack: () -> Unit, vm: AiSettingsViewModel = hiltViewModel()) {
    val context = LocalContext.current
    var provider by remember { mutableStateOf(vm.providers.firstOrNull { it.name == vm.savedProvider() } ?: vm.providers.first()) }
    var key by remember { mutableStateOf(vm.savedKey()) }
    var saved by remember { mutableStateOf(false) }
    var gemmaReady by remember { mutableStateOf(vm.gemmaAvailable()) }
    var modelUrl by remember { mutableStateOf(vm.defaultModelUrl) }
    val dl by vm.download.collectAsStateWithLifecycle()
    LaunchedEffect(dl) { if (dl is DownloadState.Done) gemmaReady = vm.gemmaAvailable() }
    val importGemma = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            val ok = runCatching {
                context.contentResolver.openInputStream(uri)?.let { vm.importGemma(it) } ?: false
            }.getOrDefault(false)
            gemmaReady = ok && vm.gemmaAvailable()
            android.widget.Toast.makeText(
                context, if (gemmaReady) "Model imported" else "Import failed",
                android.widget.Toast.LENGTH_SHORT,
            ).show()
        }
    }

    Scaffold(
        topBar = { FinatraTopBar("AI", onBack = onBack) },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
    ) { padding ->
        Column(Modifier.fillMaxWidth().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)) {

            SectionHeader("Cloud AI (your API key)")
            Text("Key is encrypted on-device and never sent anywhere except the provider you choose.",
                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

            LabeledDropdown("Provider", vm.providers, provider, { it.name }, { provider = it; saved = false })
            OutlinedTextField(
                value = key,
                onValueChange = { key = it; saved = false },
                label = { Text("API key") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(provider.keyUrl)))
                }) { Text("Get ${provider.name} key") }
                Button(onClick = { vm.save(provider.name, key); saved = true }) { Text("Save") }
            }
            if (saved) Text("Saved ✓", color = MaterialTheme.colorScheme.primary)

            Spacer(Modifier.height(8.dp))
            SectionHeader("On-device AI (Gemma)")
            ExpressiveCard(Modifier.fillMaxWidth()) {
                Text("Private, offline insights", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))
                Text("Run Gemma locally via MediaPipe LLM Inference. Zero network calls. Import a .task model (e.g. one downloaded with Google AI Edge Gallery). When present, on-device runs before any cloud provider.",
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(10.dp))
                Text(
                    if (gemmaReady) "Status: model installed ✓" else "Status: no model installed",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (gemmaReady) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = {
                        importGemma.launch(arrayOf("application/octet-stream", "*/*"))
                    }) { Text(if (gemmaReady) "Replace model" else "Import model") }
                    if (gemmaReady) {
                        OutlinedButton(onClick = { vm.deleteGemma(); gemmaReady = false }) { Text("Remove") }
                    }
                }

                Spacer(Modifier.height(12.dp))
                Text("Or download a model", style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedTextField(
                    value = modelUrl,
                    onValueChange = { modelUrl = it },
                    label = { Text("Model .task URL") },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth(),
                )
                when (val d = dl) {
                    is DownloadState.Running -> {
                        Spacer(Modifier.height(8.dp))
                        if (d.progress >= 0f) {
                            LinearProgressIndicator(progress = { d.progress }, modifier = Modifier.fillMaxWidth())
                            Text("${(d.progress * 100).toInt()}%", style = MaterialTheme.typography.labelMedium)
                        } else {
                            LinearProgressIndicator(Modifier.fillMaxWidth())
                            Text("Downloading…", style = MaterialTheme.typography.labelMedium)
                        }
                        OutlinedButton(onClick = { vm.cancelDownload() }) { Text("Cancel") }
                    }
                    is DownloadState.Error -> {
                        Text(d.message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelMedium)
                        Button(onClick = { vm.downloadGemma(modelUrl) }) { Text("Download model") }
                    }
                    DownloadState.Done -> {
                        Text("Downloaded ✓", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
                    }
                    DownloadState.Idle -> {
                        Button(onClick = { vm.downloadGemma(modelUrl) }) { Text("Download model") }
                    }
                }
            }
        }
    }
}
