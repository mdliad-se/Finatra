package com.jinatra.finatra.ui.screens.aicoach

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
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.jinatra.finatra.data.local.entity.ChatMessageEntity
import com.jinatra.finatra.ui.theme.WarmRed
import com.jinatra.finatra.ui.theme.SweetCream

/**
 * Conversational AI finance coach: a chat UI over the persisted message history, with an input
 * bar to ask questions and a clear-chat action. Shows an empty-state prompt before any messages
 * and a typing indicator while a reply is in flight.
 *
 * @param onBack navigates back.
 * @param vm exposes the chat history and send/clear actions, building context from finance data.
 */
@Composable
fun AICoachScreen(onBack: () -> Unit, vm: AICoachViewModel = hiltViewModel()) {
    val messages by vm.messages.collectAsStateWithLifecycle()
    val sending by vm.sending.collectAsStateWithLifecycle()
    var input by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Auto-scroll to the latest message (or the typing bubble) as the conversation grows.
    LaunchedEffect(messages.size, sending) {
        val target = messages.size + if (sending) 1 else 0
        if (target > 0) listState.animateScrollToItem(target - 1)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Surface(color = WarmRed) {
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
                        Text("Finatra AI", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = SweetCream)
                        Text("Your personal finance coach", style = MaterialTheme.typography.labelMedium, color = SweetCream.copy(alpha = 0.75f))
                    }
                    if (messages.isNotEmpty()) {
                        IconButton(onClick = vm::clear) {
                            Icon(Icons.Filled.DeleteSweep, contentDescription = "Clear chat", tint = SweetCream)
                        }
                    }
                }
            }
        },
        bottomBar = {
            Surface(color = MaterialTheme.colorScheme.surface, tonalElevation = 2.dp) {
                Row(
                    Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedTextField(
                        value = input,
                        onValueChange = { input = it },
                        placeholder = { Text("Ask anything about your finances…") },
                        modifier = Modifier.weight(1f),
                        maxLines = 4,
                        keyboardOptions = KeyboardOptions(),
                    )
                    IconButton(
                        onClick = { vm.send(input); input = "" },
                        enabled = input.isNotBlank() && !sending,
                    ) {
                        Box(Modifier.size(44.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary), contentAlignment = Alignment.Center) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                }
            }
        },
    ) { padding ->
        if (messages.isEmpty() && !sending) {
            Box(Modifier.fillMaxSize().padding(padding).padding(32.dp), contentAlignment = Alignment.Center) {
                Text(
                    "Ask me to analyse your spending, suggest a budget, or check if you can afford something.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(
                Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                item { Spacer(Modifier.height(8.dp)) }
                items(messages) { m -> ChatBubble(m) }
                if (sending) item { TypingBubble() }
                item { Spacer(Modifier.height(8.dp)) }
            }
        }
    }
}

/** A single chat bubble, styled and aligned by whether the message is from the user or the AI. */
@Composable
private fun ChatBubble(m: ChatMessageEntity) {
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
            Text(m.content, Modifier.padding(horizontal = 14.dp, vertical = 10.dp), style = MaterialTheme.typography.bodyMedium)
        }
    }
}

/** Placeholder bubble shown while awaiting the AI's reply. */
@Composable
private fun TypingBubble() {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            shape = RoundedCornerShape(topStart = 4.dp, topEnd = 14.dp, bottomStart = 14.dp, bottomEnd = 14.dp),
        ) {
            Text("…", Modifier.padding(horizontal = 16.dp, vertical = 10.dp), style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
