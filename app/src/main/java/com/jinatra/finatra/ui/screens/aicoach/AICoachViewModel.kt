package com.jinatra.finatra.ui.screens.aicoach

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jinatra.finatra.data.ai.AiService
import com.jinatra.finatra.data.local.entity.ChatMessageEntity
import com.jinatra.finatra.data.prefs.SettingsRepository
import com.jinatra.finatra.data.repository.FinanceRepository
import com.jinatra.finatra.util.Money
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Backs [AICoachScreen]. Streams the persisted chat history, tracks the in-flight send state, and
 * sends user questions to the AI together with a summary of the user's finances. When AI is not
 * configured it replies with setup guidance instead of contacting any service.
 */
@HiltViewModel
class AICoachViewModel @Inject constructor(
    private val repo: FinanceRepository,
    private val ai: AiService,
    private val settings: SettingsRepository,
) : ViewModel() {

    // Persisted conversation history, observed from the repository.
    val messages = repo.observeChat()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList<ChatMessageEntity>())

    // True while awaiting an AI reply; drives the typing indicator and disables send.
    private val _sending = MutableStateFlow(false)
    val sending = _sending.asStateFlow()

    val aiAvailable: Boolean get() = ai.isConfigured()

    fun clear() { viewModelScope.launch { repo.clearChat() } }

    /** Persists the user's message, then appends either the AI reply or a setup-guidance fallback. */
    fun send(text: String) {
        val msg = text.trim()
        // Ignore blank input and re-entrant sends while a reply is pending.
        if (msg.isEmpty() || _sending.value) return
        _sending.value = true
        viewModelScope.launch {
            repo.addChatMessage("user", msg)
            val base = settings.settings.first().baseCurrency
            val context = buildContext(base)
            val reply = if (aiAvailable) {
                ai.chat("$context\n\nUser: $msg\n\nReply as a concise, friendly finance coach (max 60 words).")
                    ?: "I couldn't reach the AI service. Check your provider/API key in Settings → AI."
            } else {
                "AI isn't set up yet. Add a provider & API key (or download a Gemma model) in Settings → AI to chat with your coach."
            }
            repo.addChatMessage("ai", reply.trim())
            _sending.value = false
        }
    }

    /** Builds the system context fed to the AI: net worth, savings rate, health score, and the
     *  user's spending personality (when known) to tailor the advice. */
    private suspend fun buildContext(base: String): String {
        val netWorth = repo.convertedNetWorth(base)
        val health = repo.financeHealth(base)
        val personality = settings.settings.first().personality
        val styleLine = if (personality.name != "UNKNOWN")
            " Their spending personality is ${personality.name.lowercase()} — tailor advice to it." else ""
        return "You are Finatra AI, a personal finance coach. The user's net worth is " +
            "${Money.format(netWorth, base)}, savings rate ${(health.savingsRate * 100).toInt()}%, " +
            "health score ${health.score}/100 (${health.status}).$styleLine"
    }
}
