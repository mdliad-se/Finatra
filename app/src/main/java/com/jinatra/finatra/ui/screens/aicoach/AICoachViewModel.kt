package com.jinatra.finatra.ui.screens.aicoach

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jinatra.finatra.data.ai.AiResult
import com.jinatra.finatra.data.ai.AiService
import com.jinatra.finatra.data.local.entity.ChatMessageEntity
import com.jinatra.finatra.data.local.entity.ChatSessionEntity
import com.jinatra.finatra.data.prefs.SettingsRepository
import com.jinatra.finatra.data.repository.FinanceRepository
import com.jinatra.finatra.util.Money
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Chat conversations belong to the AI Coach surface. */
private const val KIND = "coach"
/** Title given to a freshly started conversation before the first user message renames it. */
private const val NEW_TITLE = "New chat"

/**
 * Backs [AICoachScreen]. Manages multiple persisted conversations: the list of past sessions,
 * the active session and its streamed message history, and the in-flight send state. Sends user
 * questions to the AI together with a summary of the user's finances and the running transcript.
 * When AI is not configured it replies with setup guidance instead of contacting any service.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AICoachViewModel @Inject constructor(
    private val repo: FinanceRepository,
    private val ai: AiService,
    private val settings: SettingsRepository,
) : ViewModel() {

    // The conversation currently shown; resolved to the most recent (or a new) session on first use.
    private val _currentSessionId = MutableStateFlow<Long?>(null)
    val currentSessionId = _currentSessionId.asStateFlow()

    // All coach conversations, most-recently-updated first, for the history list.
    val sessions = repo.observeChatSessions(KIND)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList<ChatSessionEntity>())

    // Messages of the active conversation, switching whenever the active session changes.
    val messages = _currentSessionId.filterNotNull()
        .flatMapLatest { repo.observeChatMessages(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList<ChatMessageEntity>())

    // True while awaiting an AI reply; drives the typing indicator and disables send.
    private val _sending = MutableStateFlow(false)
    val sending = _sending.asStateFlow()

    val aiAvailable: Boolean get() = ai.isConfigured()

    init {
        // Resume the latest conversation on open, or start a fresh one for first-run users.
        viewModelScope.launch {
            _currentSessionId.value = repo.latestOrNewChatSession(KIND, NEW_TITLE)
        }
    }

    /** Start a new, empty conversation and switch to it. */
    fun newChat() {
        viewModelScope.launch { _currentSessionId.value = repo.createChatSession(KIND, NEW_TITLE) }
    }

    /** Switch the visible conversation to [id]. */
    fun switchSession(id: Long) { _currentSessionId.value = id }

    /** Rename a conversation. */
    fun renameSession(id: Long, title: String) {
        val clean = title.trim().ifBlank { NEW_TITLE }
        viewModelScope.launch { repo.renameChatSession(id, clean) }
    }

    /** Delete a conversation; if it was the active one, fall back to the latest remaining or a new chat. */
    fun deleteSession(id: Long) {
        viewModelScope.launch {
            repo.deleteChatSession(id)
            if (_currentSessionId.value == id) {
                _currentSessionId.value = repo.latestOrNewChatSession(KIND, NEW_TITLE)
            }
        }
    }

    /** Persists the user's message, then appends either the AI reply or a setup-guidance fallback. */
    fun send(text: String) {
        val msg = text.trim()
        // Ignore blank input and re-entrant sends while a reply is pending.
        if (msg.isEmpty() || _sending.value) return
        _sending.value = true
        viewModelScope.launch {
            val sid = _currentSessionId.value ?: repo.createChatSession(KIND, NEW_TITLE).also { _currentSessionId.value = it }
            // Snapshot the prior turns before persisting the new message so the transcript fed to
            // the model is the conversation *so far* (enables real multi-turn follow-ups).
            val prior = messages.value
            val history = transcript(prior)
            // Name the conversation from its first user message.
            if (prior.none { it.role == "user" }) repo.renameChatSession(sid, msg.take(40))
            repo.addChatMessage(sid, "user", msg)
            val base = settings.settings.first().baseCurrency
            val context = buildContext(base)
            val reply = if (aiAvailable) {
                val convo = if (history.isBlank()) "" else "\n\nConversation so far:\n$history"
                val result = ai.chatResult("$context$convo\n\nUser: $msg\n\nReply as a concise, friendly finance coach (max 60 words).")
                result.textOrNull ?: when (result) {
                    is AiResult.Err -> result.error.message
                    else -> "I couldn't reach the AI service. Check Settings → AI."
                }
            } else {
                "AI isn't set up yet. Add a provider & API key (or download a Gemma model) in Settings → AI to chat with your coach."
            }
            repo.addChatMessage(sid, "ai", reply.trim())
            _sending.value = false
        }
    }

    /** Render the most recent [limit] turns as a compact "Role: text" transcript for prompt context. */
    private fun transcript(history: List<ChatMessageEntity>, limit: Int = 6): String =
        history.takeLast(limit).joinToString("\n") {
            "${if (it.role == "user") "User" else "Coach"}: ${it.content}"
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
