package com.jinatra.finatra.data.ai

import com.jinatra.finatra.data.local.entity.TransactionType
import com.jinatra.finatra.data.prefs.SecurePrefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Structured result of parsing a free-text transaction description (see [AiService.parseTransaction]).
 * Every field is nullable — the model may omit or fail to determine any of them, and callers
 * treat each as an optional pre-fill for the add-transaction form.
 */
data class ParsedTx(
    val amount: Double? = null,
    val note: String? = null,
    val type: TransactionType? = null,
    val category: String? = null,
)

/** Provider endpoints — overridable in tests (MockWebServer), real defaults in prod. */
data class AiEndpoints(
    // gemini-flash-latest: stable alias (1.5-flash was retired → 404). Verified live 2026-06.
    val gemini: String = "https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent",
    val claude: String = "https://api.anthropic.com/v1/messages",
    val openRouter: String = "https://openrouter.ai/api/v1/chat/completions",
)

/**
 * Raw HTTP layer for the three cloud providers. Pure + injectable so it can be tested
 * end-to-end against a MockWebServer without a real key. Returns null on any failure.
 */
@Singleton
class AiHttp @Inject constructor(
    private val client: OkHttpClient,
    private val endpoints: AiEndpoints,
) {
    private val json = "application/json".toMediaType()

    /**
     * Send [prompt] to the named [provider] ("Claude", "OpenRouter", or anything else → Gemini)
     * using the user's [key], returning the model's text reply. Runs on the IO dispatcher and
     * swallows every exception, returning null so callers can fall back gracefully.
     */
    suspend fun complete(provider: String, prompt: String, key: String): String? = withContext(Dispatchers.IO) {
        runCatching {
            when (provider) {
                "Claude" -> claude(prompt, key)
                "OpenRouter" -> openRouter(prompt, key)
                else -> gemini(prompt, key)
            }
        }.getOrNull()
    }

    // Google Gemini: key is passed as a query parameter; reply text is dug out of
    // candidates[0].content.parts[0].text.
    private fun gemini(prompt: String, key: String): String? {
        val body = JSONObject().put(
            "contents",
            JSONArray().put(JSONObject().put("parts", JSONArray().put(JSONObject().put("text", prompt)))),
        ).toString().toRequestBody(json)
        val req = Request.Builder().url("${endpoints.gemini}?key=$key").post(body).build()
        client.newCall(req).execute().use { resp ->
            val txt = resp.body?.string() ?: return null
            if (!resp.isSuccessful) return null
            return JSONObject(txt).optJSONArray("candidates")?.optJSONObject(0)
                ?.optJSONObject("content")?.optJSONArray("parts")?.optJSONObject(0)?.optString("text")
        }
    }

    // Anthropic Claude Messages API: key in x-api-key, fixed api-version header;
    // reply text is content[0].text.
    private fun claude(prompt: String, key: String): String? {
        val body = JSONObject()
            .put("model", "claude-3-5-haiku-latest")
            .put("max_tokens", 512)
            .put("messages", JSONArray().put(JSONObject().put("role", "user").put("content", prompt)))
            .toString().toRequestBody(json)
        val req = Request.Builder().url(endpoints.claude)
            .addHeader("x-api-key", key)
            .addHeader("anthropic-version", "2023-06-01")
            .post(body).build()
        client.newCall(req).execute().use { resp ->
            val txt = resp.body?.string() ?: return null
            if (!resp.isSuccessful) return null
            return JSONObject(txt).optJSONArray("content")?.optJSONObject(0)?.optString("text")
        }
    }

    // OpenRouter (OpenAI-compatible chat-completions): key as Bearer token;
    // reply text is choices[0].message.content.
    private fun openRouter(prompt: String, key: String): String? {
        val body = JSONObject()
            .put("model", "openai/gpt-4o-mini")
            .put("messages", JSONArray().put(JSONObject().put("role", "user").put("content", prompt)))
            .toString().toRequestBody(json)
        val req = Request.Builder().url(endpoints.openRouter)
            .addHeader("Authorization", "Bearer $key")
            .post(body).build()
        client.newCall(req).execute().use { resp ->
            val txt = resp.body?.string() ?: return null
            if (!resp.isSuccessful) return null
            return JSONObject(txt).optJSONArray("choices")?.optJSONObject(0)
                ?.optJSONObject("message")?.optString("content")
        }
    }
}

/**
 * Cloud + on-device AI client (PRD 6.9). Uses user-supplied key/provider from [SecurePrefs];
 * prefers on-device Gemma when a model is present. Key never leaves the device except in the
 * direct request to the chosen provider. All calls return null on failure — callers fall back.
 */
@Singleton
class AiService @Inject constructor(
    private val secure: SecurePrefs,
    private val gemma: GemmaService,
    private val http: AiHttp,
) {
    /** True when AI features can run at all: either an on-device Gemma model is present
     *  or the user has saved a cloud API key. */
    fun isConfigured(): Boolean = gemma.isAvailable() || !secure.aiApiKey.isNullOrBlank()

    /** Low-level single-turn chat. Prefers on-device Gemma, falls back to cloud. Null on failure. */
    suspend fun chat(prompt: String): String? {
        if (gemma.isAvailable()) {
            gemma.generate(prompt)?.let { return it }
        }
        val key = secure.aiApiKey?.takeIf { it.isNotBlank() } ?: return null
        val provider = secure.aiProvider ?: "Gemini"
        return http.complete(provider, prompt, key)
    }

    /** Parse a free-text entry into transaction fields (PRD 6.9 NL entry). */
    suspend fun parseTransaction(text: String, categories: List<String>): ParsedTx? {
        val cats = categories.joinToString(", ")
        val prompt = """
            Extract a personal-finance transaction from this text: "$text".
            Available categories: $cats.
            Reply ONLY with compact JSON: {"amount": number, "note": string, "type": "INCOME"|"EXPENSE"|"TRANSFER", "category": string}.
            Pick the closest category from the list, or "" if none fits. No prose.
        """.trimIndent()
        val raw = chat(prompt) ?: return null
        return parseTxJson(raw)
    }

    /** Suggest the best category name for a free-text note (PRD 6.9 smart categorization). */
    suspend fun suggestCategory(note: String, categories: List<String>): String? {
        if (note.isBlank()) return null
        val prompt = "Pick the single best spending category for: \"$note\". " +
            "Choose from: ${categories.joinToString(", ")}. " +
            "If none fits, invent a short (1-2 word) category name. Reply with ONLY the category name, nothing else."
        return chat(prompt)?.trim()?.lines()?.firstOrNull()?.trim()?.trim('"', '.', ',')?.takeIf { it.isNotBlank() && it.length < 30 }
    }

    /** Recommend monthly budget limits from spend history (PRD 6.9). Map of category name -> limit. */
    suspend fun recommendBudgets(spendSummary: String): Map<String, Double>? {
        val prompt = """
            A user's average monthly spending by category: $spendSummary.
            Recommend a sensible monthly budget limit per category (slightly below or near current spend to encourage saving).
            Reply ONLY with compact JSON mapping category name to number, e.g. {"Food": 300, "Transport": 120}. No prose.
        """.trimIndent()
        val raw = chat(prompt) ?: return null
        return parseBudgetJson(raw)
    }

    /** Conversational reply for the budget-planning chat. Null on failure. */
    suspend fun budgetChat(prompt: String): String? = chat(prompt)?.trim()?.takeIf { it.isNotBlank() }

    /** Extract the final agreed monthly budget limits from a planning conversation.
     *  Map of category name -> limit. Null if nothing concrete could be parsed. */
    suspend fun extractBudgets(conversation: String): Map<String, Double>? {
        val prompt = """
            Below is a conversation between a user and a budgeting assistant.
            Extract the MOST RECENT set of MONTHLY budget limits per spending category
            discussed in the conversation (use the latest concrete numbers proposed or
            agreed — prefer the user's adjustments over the assistant's defaults).
            Exclude non-budget lines like total income or leftover savings.
            Conversation:
            $conversation
            Reply ONLY with compact JSON mapping category name to number, e.g. {"Food": 300, "Transport": 120}. No prose. If truly no amounts were discussed, reply {}.
        """.trimIndent()
        val raw = chat(prompt) ?: return null
        return parseBudgetJson(raw)
    }

    /** One-line spending insight for the dashboard (PRD 6.9). */
    suspend fun spendingInsight(summary: String): String? {
        val prompt = "You are a concise personal-finance assistant. Given this month summary: $summary. " +
            "Give ONE short, specific, friendly insight (max 20 words). No preamble."
        return chat(prompt)?.trim()?.takeIf { it.isNotBlank() }
    }

    /** Pure parsing helpers, kept in the companion so they can be unit-tested without an [AiService] instance. */
    companion object {
        /** Parse the model's JSON reply into [ParsedTx]; tolerant of surrounding prose. */
        fun parseTxJson(raw: String): ParsedTx? {
            val obj = runCatching { JSONObject(extractJson(raw)) }.getOrNull() ?: return null
            return ParsedTx(
                amount = obj.optDouble("amount").takeIf { !it.isNaN() && it != 0.0 },
                note = obj.optString("note").ifBlank { null },
                type = runCatching { TransactionType.valueOf(obj.optString("type")) }.getOrNull(),
                category = obj.optString("category").ifBlank { null },
            )
        }

        /** Slice out the first `{...}` object from [s] so a model that wraps JSON in prose
         *  still parses; returns the input unchanged when no braces are found. */
        fun extractJson(s: String): String {
            val start = s.indexOf('{'); val end = s.lastIndexOf('}')
            return if (start >= 0 && end > start) s.substring(start, end + 1) else s
        }

        /** Parse a {category: amount} JSON reply into a map. */
        fun parseBudgetJson(raw: String): Map<String, Double>? {
            val obj = runCatching { JSONObject(extractJson(raw)) }.getOrNull() ?: return null
            val out = LinkedHashMap<String, Double>()
            obj.keys().forEach { k ->
                val v = obj.optDouble(k)
                if (!v.isNaN() && v > 0) out[k] = v
            }
            return out.ifEmpty { null }
        }
    }
}
