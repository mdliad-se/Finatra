package com.jinatra.finatra.ui.screens.budgets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jinatra.finatra.data.ai.AiService
import com.jinatra.finatra.data.local.entity.BudgetEntity
import com.jinatra.finatra.data.local.entity.BudgetPeriod
import com.jinatra.finatra.data.prefs.SettingsRepository
import com.jinatra.finatra.data.repository.FinanceRepository
import com.jinatra.finatra.util.DateUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** One line in the budget-planning conversation. role = "user" | "ai". */
data class ChatLine(val role: String, val text: String)

/** Chat conversations belong to the budget-planner surface. */
private const val KIND = "budget"
private const val NEW_TITLE = "Budget plan"
private const val GREETING =
    "Hi! Let's plan your monthly budgets together. Tell me about your income, " +
        "what you tend to spend on, and any savings goals — or ask me anything. " +
        "When you're happy, tap Finalize and I'll turn our chat into budgets."

/**
 * Conversational budget planner (PRD 6.6 AI suggest budgets). The user chats with the
 * AI about their finances; on Finalize the AI extracts agreed monthly limits from the
 * whole conversation, which the user then reviews and applies as budgets. The conversation
 * is persisted so it survives leaving the screen; "New plan" starts a fresh session.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class BudgetChatViewModel @Inject constructor(
    private val repo: FinanceRepository,
    private val ai: AiService,
    private val settings: SettingsRepository,
) : ViewModel() {

    val aiAvailable: Boolean get() = ai.isConfigured()

    val baseCurrency = settings.settings.map { it.baseCurrency }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "USD")

    // Active planning conversation; resumed on open, or created fresh (seeded with the greeting).
    private val _currentSessionId = MutableStateFlow<Long?>(null)

    val messages = _currentSessionId.filterNotNull()
        .flatMapLatest { repo.observeChatMessages(it) }
        .map { list -> list.map { ChatLine(it.role, it.content) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf(ChatLine("ai", GREETING)))

    private val _sending = MutableStateFlow(false)
    val sending = _sending.asStateFlow()
    private val _finalizing = MutableStateFlow(false)
    val finalizing = _finalizing.asStateFlow()
    private val _proposals = MutableStateFlow<List<BudgetSuggestion>>(emptyList())
    val proposals = _proposals.asStateFlow()

    init {
        viewModelScope.launch {
            val id = repo.latestOrNewChatSession(KIND, NEW_TITLE)
            _currentSessionId.value = id
            // Seed the greeting only for a brand-new (empty) conversation.
            if (id > 0 && repo.observeChatMessages(id).first().isEmpty()) {
                repo.addChatMessage(id, "ai", GREETING)
            }
        }
    }

    /** Discard the in-progress plan and start a fresh conversation. */
    fun newPlan() {
        _proposals.value = emptyList()
        viewModelScope.launch {
            val id = repo.createChatSession(KIND, NEW_TITLE)
            _currentSessionId.value = id
            if (id > 0) repo.addChatMessage(id, "ai", GREETING)
        }
    }

    /** Append the user's message and request a conversational AI reply, ignoring empty/in-flight input. */
    fun send(text: String) {
        val msg = text.trim()
        if (msg.isEmpty() || _sending.value || !aiAvailable) return
        val sid = _currentSessionId.value ?: return
        _sending.value = true
        viewModelScope.launch {
            repo.addChatMessage(sid, "user", msg)
            // Send the whole transcript as context; fall back to an error line if the call fails.
            val reply = ai.budgetChat(buildPrompt())
                ?: "I couldn't reach the AI service. Check your provider/API key in Settings → AI."
            repo.addChatMessage(sid, "ai", reply.trim())
            _sending.value = false
        }
    }

    /** Assemble the system instructions, the user's currency/categories and the running transcript into one prompt. */
    private suspend fun buildPrompt(): String {
        val base = settings.settings.first().baseCurrency
        val cats = repo.observeCategories().first()
            .filter { !it.isIncome }.joinToString(", ") { it.name }
        val transcript = messages.value.joinToString("\n") {
            (if (it.role == "user") "User: " else "Assistant: ") + it.text
        }
        return """
            You are Finatra's budgeting assistant. Help the user plan sensible MONTHLY budget
            limits per spending category, conversationally. Base currency: $base.
            Existing categories: $cats. Ask clarifying questions when useful, keep replies
            concise (max 70 words), and don't output JSON unless explicitly asked.
            $transcript
            Assistant:
        """.trimIndent()
    }

    /** Ask the AI to turn the conversation into concrete monthly limits for review. */
    fun finalize(onError: (String) -> Unit) {
        if (_finalizing.value || _sending.value) return
        if (!aiAvailable) { onError("Set up an AI provider in Settings → AI first."); return }
        _finalizing.value = true
        viewModelScope.launch {
            val transcript = messages.value.joinToString("\n") {
                (if (it.role == "user") "User: " else "Assistant: ") + it.text
            }
            // Ask the AI to distil the conversation into name→amount monthly limits.
            val rec = ai.extractBudgets(transcript)
            if (rec.isNullOrEmpty()) {
                _finalizing.value = false
                onError("No budgets to create yet — chat a bit more about specific category limits, then finalize.")
                return@launch
            }
            // Resolve each extracted name to a real category (creating it if needed) before proposing it.
            _proposals.value = rec.mapNotNull { (name, amt) ->
                val id = repo.findOrCreateCategory(name, isIncome = false)
                if (id > 0) BudgetSuggestion(id, name, 0.0, amt) else null
            }
            _finalizing.value = false
            if (_proposals.value.isEmpty()) onError("Couldn't match any categories from the chat.")
        }
    }

    /** Persist a single proposed budget and drop it from the review list. */
    fun applyOne(s: BudgetSuggestion) {
        viewModelScope.launch {
            repo.upsertBudget(monthly(s))
            _proposals.value = _proposals.value.filterNot { it.categoryId == s.categoryId }
        }
    }

    /** Persist all proposed budgets at once, then clear the review list and signal [onDone] with the count. */
    fun applyAll(onDone: (Int) -> Unit) {
        val all = _proposals.value
        viewModelScope.launch {
            all.forEach { repo.upsertBudget(monthly(it)) }
            _proposals.value = emptyList()
            onDone(all.size)
        }
    }

    /** Discard the current proposals without creating any budgets. */
    fun dismissProposals() { _proposals.value = emptyList() }

    /** Build a rolling monthly [BudgetEntity] (no end date) from a reviewed suggestion. */
    private fun monthly(s: BudgetSuggestion) = BudgetEntity(
        categoryId = s.categoryId, amount = s.suggested, period = BudgetPeriod.MONTHLY,
        startDate = DateUtil.startOfMonth(), endDate = null, createdAt = System.currentTimeMillis(),
    )
}
