package com.jinatra.finatra.ui.screens.budgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jinatra.finatra.ui.components.ExpressiveCard
import com.jinatra.finatra.ui.components.OverlineLabel
import com.jinatra.finatra.ui.theme.SweetCream
import com.jinatra.finatra.util.Money

/** Conversational budget planner. Chat → Finalize → review AI-extracted limits → apply. */
@Composable
fun BudgetChatScreen(
    onBack: () -> Unit,
    onOpenAiSettings: () -> Unit,
    vm: BudgetChatViewModel = hiltViewModel(),
) {
    val messages by vm.messages.collectAsStateWithLifecycle()
    val sending by vm.sending.collectAsStateWithLifecycle()
    val finalizing by vm.finalizing.collectAsStateWithLifecycle()
    val proposals by vm.proposals.collectAsStateWithLifecycle()
    val baseCurrency by vm.baseCurrency.collectAsStateWithLifecycle()
    var input by remember { mutableStateOf("") }
    var notice by remember { mutableStateOf<String?>(null) }
    val listState = rememberLazyListState()

    // Auto-scroll to the latest bubble whenever a message arrives or the typing indicator toggles.
    LaunchedEffect(messages.size, sending) {
        val target = messages.size + if (sending) 1 else 0
        if (target > 0) listState.animateScrollToItem(target - 1)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Surface(color = MaterialTheme.colorScheme.primary) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = SweetCream)
                    }
                    Box(Modifier.size(40.dp).clip(CircleShape).background(SweetCream.copy(alpha = 0.18f)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = SweetCream)
                    }
                    Column(Modifier.weight(1f)) {
                        Text("Budget AI", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = SweetCream)
                        Text("Plan your budgets by chat", style = MaterialTheme.typography.labelMedium, color = SweetCream.copy(alpha = 0.75f))
                    }
                }
            }
        },
        bottomBar = {
            if (vm.aiAvailable) {
                Surface(color = MaterialTheme.colorScheme.surface, tonalElevation = 2.dp) {
                    Column(Modifier.fillMaxWidth().padding(12.dp)) {
                        notice?.let {
                            Text(it, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(8.dp))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = input,
                                onValueChange = { input = it },
                                placeholder = { Text("Tell me about your budget…") },
                                modifier = Modifier.weight(1f),
                                maxLines = 4,
                                keyboardOptions = KeyboardOptions(),
                            )
                            IconButton(onClick = { vm.send(input); input = ""; notice = null }, enabled = input.isNotBlank() && !sending) {
                                Box(Modifier.size(44.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary), contentAlignment = Alignment.Center) {
                                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = MaterialTheme.colorScheme.onPrimary)
                                }
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = { notice = null; vm.finalize { notice = it } },
                            enabled = !finalizing && !sending && messages.any { it.role == "user" },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            if (finalizing) {
                                CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                            } else {
                                Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                            }
                            Spacer(Modifier.size(8.dp))
                            Text("Finalize → create budgets")
                        }
                    }
                }
            }
        },
    ) { padding ->
        // Without a configured AI provider the planner can't run; show a setup prompt instead.
        if (!vm.aiAvailable) {
            Column(
                Modifier.fillMaxSize().padding(padding).padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp))
                Spacer(Modifier.height(16.dp))
                Text("Connect an AI provider", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Planning budgets by chat needs a live AI. Add an API key (Gemini, Claude or OpenRouter) or download a Gemma model.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(20.dp))
                Button(onClick = onOpenAiSettings, modifier = Modifier.fillMaxWidth()) { Text("Open AI settings") }
            }
            return@Scaffold
        }

        LazyColumn(
            Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item { Spacer(Modifier.height(8.dp)) }
            items(messages) { m -> ChatBubble(m) }
            if (sending) item { TypingBubble() }
            // After Finalize, the AI-extracted limits are shown for review: apply individually or all at once.
            if (proposals.isNotEmpty()) {
                item {
                    ExpressiveCard(Modifier.fillMaxWidth(), container = MaterialTheme.colorScheme.secondaryContainer) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            OverlineLabel("Proposed monthly budgets")
                            TextButton(onClick = { vm.applyAll { notice = "Budgets created." } }) { Text("Apply all") }
                        }
                        proposals.forEach { sg ->
                            Row(
                                Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(sg.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                                OutlinedButton(onClick = { vm.applyOne(sg) }) { Text(Money.format(sg.suggested, baseCurrency)) }
                            }
                        }
                        TextButton(onClick = { vm.dismissProposals() }) { Text("Dismiss") }
                    }
                }
            }
            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

/** A single chat message bubble, aligned and coloured by whether it is from the user or the AI. */
@Composable
private fun ChatBubble(m: ChatLine) {
    val isUser = m.role == "user"
    Row(Modifier.fillMaxWidth(), horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start) {
        Surface(
            color = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
            shape = RoundedCornerShape(
                topStart = if (isUser) 14.dp else 4.dp, topEnd = if (isUser) 4.dp else 14.dp,
                bottomStart = 14.dp, bottomEnd = 14.dp,
            ),
            modifier = Modifier.widthIn(max = 300.dp),
        ) {
            Text(m.text, Modifier.padding(horizontal = 14.dp, vertical = 10.dp), style = MaterialTheme.typography.bodyMedium)
        }
    }
}

/** Placeholder bubble shown while the AI reply is being generated. */
@Composable
private fun TypingBubble() {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            shape = RoundedCornerShape(topStart = 4.dp, topEnd = 14.dp, bottomStart = 14.dp, bottomEnd = 14.dp),
        ) {
            Text("…", Modifier.padding(horizontal = 16.dp, vertical = 10.dp), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
