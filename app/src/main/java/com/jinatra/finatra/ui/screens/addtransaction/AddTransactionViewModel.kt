package com.jinatra.finatra.ui.screens.addtransaction

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jinatra.finatra.data.local.entity.AccountEntity
import com.jinatra.finatra.data.local.entity.CategoryEntity
import com.jinatra.finatra.data.local.entity.TransactionEntity
import com.jinatra.finatra.data.local.entity.TransactionTemplateEntity
import com.jinatra.finatra.data.local.entity.TransactionType
import com.jinatra.finatra.data.ai.AiService
import com.jinatra.finatra.data.prefs.SettingsRepository
import com.jinatra.finatra.data.repository.FinanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/** One category portion of a split expense (PRD 6.4). */
data class SplitItem(val categoryId: Long? = null, val amount: String = "")

/**
 * Full form state for [AddTransactionScreen]. Amount/split fields are kept as strings while
 * editing. Derived members compute the categories valid for the current type, whether splitting is
 * allowed, and split totals/validity.
 */
data class AddTxState(
    val editingId: Long = -1L,
    val type: TransactionType = TransactionType.EXPENSE,
    val amount: String = "",
    val accountId: Long? = null,
    val transferToAccountId: Long? = null,
    val categoryId: Long? = null,
    val dateTime: Long = System.currentTimeMillis(),
    val note: String = "",
    val tags: String = "",
    val receiptPath: String? = null,
    val accounts: List<AccountEntity> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val baseCurrency: String = "USD",
    val aiAvailable: Boolean = false,
    val aiBusy: Boolean = false,
    val splitMode: Boolean = false,
    val splits: List<SplitItem> = emptyList(),
    val templates: List<TransactionTemplateEntity> = emptyList(),
    val duplicate: TransactionEntity? = null,
) {
    val isEditing: Boolean get() = editingId > 0
    // Top-level categories matching the current type's income/expense polarity.
    val visibleCategories: List<CategoryEntity>
        get() = categories.filter { it.isIncome == (type == TransactionType.INCOME) && it.parentId == null }
    /** Split is offered only for new expenses. */
    val canSplit: Boolean get() = !isEditing && type == TransactionType.EXPENSE
    val splitTotal: Double get() = splits.sumOf { it.amount.toDoubleOrNull() ?: 0.0 }
    // A split needs at least two rows, each with a category and a positive amount.
    val splitValid: Boolean
        get() = splits.size >= 2 && splits.all { it.categoryId != null && (it.amount.toDoubleOrNull() ?: 0.0) > 0 }
}

/**
 * Backs [AddTransactionScreen]. Initializes the form from navigation args (edit an existing
 * transaction, or preset account/type for a new one), exposes setters for every field, and handles
 * templates, split expenses, AI parsing/categorization, duplicate detection, and persistence.
 */
@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    private val repo: FinanceRepository,
    private val settings: SettingsRepository,
    private val ai: AiService,
    savedState: SavedStateHandle,
) : ViewModel() {

    // Navigation args: transaction being edited, plus optional preset account/type for new entries.
    private val txId: Long = savedState.get<Long>("txId") ?: -1L
    private val presetAccountId: Long = savedState.get<Long>("accountId") ?: -1L
    private val presetType: String = savedState.get<String>("type") ?: ""
    private val _state = MutableStateFlow(AddTxState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val accounts = repo.observeAccounts().first()
            val categories = repo.observeCategories().first()
            val base = settings.settings.first().baseCurrency
            val presetT = runCatching { TransactionType.valueOf(presetType) }.getOrNull()
            val type = presetT ?: TransactionType.EXPENSE
            _state.value = _state.value.copy(
                type = type,
                accounts = accounts,
                categories = categories,
                baseCurrency = base,
                accountId = accounts.firstOrNull { it.id == presetAccountId }?.id ?: accounts.firstOrNull()?.id,
                categoryId = categories.firstOrNull { it.isIncome == (type == TransactionType.INCOME) }?.id,
                aiAvailable = ai.isConfigured(),
            )
            if (txId > 0) loadExisting(txId)
        }
        viewModelScope.launch {
            repo.observeTemplates().collect { tpls -> _state.value = _state.value.copy(templates = tpls) }
        }
    }

    // --- Templates (PRD 6.4) ---
    fun applyTemplate(t: TransactionTemplateEntity) {
        val s = _state.value
        _state.value = s.copy(
            type = t.type,
            amount = if (t.amount > 0) (if (t.amount % 1.0 == 0.0) t.amount.toLong().toString() else t.amount.toString()) else s.amount,
            categoryId = t.categoryId ?: s.categoryId,
            accountId = t.accountId ?: s.accountId,
            note = t.note,
            tags = t.tags,
        )
    }

    fun saveAsTemplate(name: String) {
        val s = _state.value
        if (name.isBlank()) return
        viewModelScope.launch {
            repo.upsertTemplate(
                TransactionTemplateEntity(
                    name = name.trim(), type = s.type, amount = s.amount.toDoubleOrNull() ?: 0.0,
                    categoryId = if (s.type == TransactionType.TRANSFER) null else s.categoryId,
                    accountId = s.accountId, note = s.note, tags = s.tags,
                    createdAt = System.currentTimeMillis(),
                )
            )
        }
    }

    fun deleteTemplate(t: TransactionTemplateEntity) { viewModelScope.launch { repo.deleteTemplate(t) } }

    fun dismissDuplicate() { _state.value = _state.value.copy(duplicate = null) }

    private suspend fun loadExisting(id: Long) {
        repo.transactionById(id)?.let { t ->
            _state.value = _state.value.copy(
                editingId = t.id,
                type = t.type,
                amount = t.amount.toString(),
                accountId = t.accountId,
                transferToAccountId = t.transferToAccountId,
                categoryId = t.categoryId,
                dateTime = t.dateTime,
                note = t.note,
                tags = t.tags,
                receiptPath = t.receiptPath,
            )
        }
    }

    fun setType(t: TransactionType) {
        // Switching type resets the selected category to one matching the new type's polarity.
        val cat = _state.value.categories.firstOrNull { it.isIncome == (t == TransactionType.INCOME) }?.id
        _state.value = _state.value.copy(type = t, categoryId = cat)
    }
    fun setAmount(v: String) { _state.value = _state.value.copy(amount = v.filter { it.isDigit() || it == '.' }) }
    fun setAccount(id: Long) { _state.value = _state.value.copy(accountId = id) }
    fun setTransferTo(id: Long) { _state.value = _state.value.copy(transferToAccountId = id) }
    fun setCategory(id: Long) { _state.value = _state.value.copy(categoryId = id) }
    fun setDate(epoch: Long) { _state.value = _state.value.copy(dateTime = epoch) }
    fun setNote(v: String) { _state.value = _state.value.copy(note = v) }
    fun setTags(v: String) { _state.value = _state.value.copy(tags = v) }
    fun setReceipt(path: String?) { _state.value = _state.value.copy(receiptPath = path) }

    // --- Split transaction (PRD 6.4) ---
    fun toggleSplit(on: Boolean) {
        val s = _state.value
        _state.value = if (on) {
            // Seed two rows; first inherits the currently selected category.
            s.copy(splitMode = true, splits = listOf(SplitItem(s.categoryId, s.amount), SplitItem()))
        } else {
            s.copy(splitMode = false, splits = emptyList())
        }
    }
    fun addSplitRow() { _state.value = _state.value.copy(splits = _state.value.splits + SplitItem()) }
    fun removeSplitRow(index: Int) {
        _state.value = _state.value.copy(splits = _state.value.splits.filterIndexed { i, _ -> i != index })
    }
    fun setSplitCategory(index: Int, categoryId: Long) {
        _state.value = _state.value.copy(splits = _state.value.splits.mapIndexed { i, it -> if (i == index) it.copy(categoryId = categoryId) else it })
    }
    fun setSplitAmount(index: Int, amount: String) {
        val clean = amount.filter { it.isDigit() || it == '.' }
        _state.value = _state.value.copy(splits = _state.value.splits.mapIndexed { i, it -> if (i == index) it.copy(amount = clean) else it })
    }

    /** Naive on-device natural-language parse (fallback when AI unavailable). Also infers the
     *  category from keywords so quick-add picks a category even without an AI key (PRD 6.9). */
    fun quickParse(text: String) {
        val amount = Regex("""\d+(?:\.\d+)?""").find(text)?.value ?: ""
        val note = text.replace(Regex("""(spent|paid|got|received|on|for|today|yesterday)""", RegexOption.IGNORE_CASE), "")
            .replace(amount, "").trim().replace(Regex("\\s+"), " ")
        val income = Regex("(received|got|salary|income)", RegexOption.IGNORE_CASE).containsMatchIn(text)
        val type = if (income) TransactionType.INCOME else TransactionType.EXPENSE
        _state.value = _state.value.copy(
            amount = amount,
            note = note.replaceFirstChar { it.uppercase() },
            type = type,
            // Keep the existing category unless a keyword clearly maps to one.
            categoryId = inferCategoryId(text, type == TransactionType.INCOME) ?: _state.value.categoryId,
        )
    }

    /** Best-effort keyword → category match against the user's own categories, restricted to the
     *  income/expense polarity. Returns the matching category id, or null when nothing maps. */
    private fun inferCategoryId(text: String, isIncome: Boolean): Long? {
        val t = text.lowercase()
        val target = CATEGORY_KEYWORDS.firstNotNullOfOrNull { (category, keywords) ->
            category.takeIf { keywords.any { kw -> t.contains(kw) } }
        } ?: return null
        return _state.value.categories
            .firstOrNull { it.parentId == null && it.isIncome == isIncome && it.name.equals(target, true) }
            ?.id
    }

    /** AI natural-language parse (PRD 6.9). Falls back to [quickParse] if AI returns nothing. */
    fun aiParse(text: String, onError: (String) -> Unit = {}) {
        if (text.isBlank()) return
        if (!ai.isConfigured()) { quickParse(text); return }
        _state.value = _state.value.copy(aiBusy = true)
        viewModelScope.launch {
            val cats = _state.value.categories.filter { it.parentId == null }
            val parsed = ai.parseTransaction(text, cats.map { it.name })
            if (parsed == null) {
                quickParse(text)
                onError("AI unavailable — used basic parse")
            } else {
                val type = parsed.type ?: _state.value.type
                // Full smart categorization: resolve to an existing category or auto-create it.
                val catId = parsed.category
                    ?.takeIf { it.isNotBlank() && type != TransactionType.TRANSFER }
                    ?.let { repo.findOrCreateCategory(it, type == TransactionType.INCOME) }
                    ?.takeIf { it > 0 }
                if (catId != null) refreshCategories()
                _state.value = _state.value.copy(
                    amount = parsed.amount?.let { if (it % 1.0 == 0.0) it.toLong().toString() else it.toString() } ?: _state.value.amount,
                    note = parsed.note ?: _state.value.note,
                    type = type,
                    categoryId = catId ?: _state.value.categoryId,
                )
            }
            _state.value = _state.value.copy(aiBusy = false)
        }
    }

    /** Suggest a category for the current note via AI; auto-creates it if new (PRD 6.9). */
    fun suggestCategory(onError: (String) -> Unit = {}) {
        val note = _state.value.note
        if (note.isBlank() || !ai.isConfigured() || _state.value.type == TransactionType.TRANSFER) return
        _state.value = _state.value.copy(aiBusy = true)
        viewModelScope.launch {
            val isIncome = _state.value.type == TransactionType.INCOME
            val names = _state.value.categories.filter { it.parentId == null && it.isIncome == isIncome }.map { it.name }
            val suggestion = ai.suggestCategory(note, names)
            if (suggestion == null) {
                onError("Could not suggest a category")
            } else {
                val id = repo.findOrCreateCategory(suggestion, isIncome)
                if (id > 0) { refreshCategories(); _state.value = _state.value.copy(categoryId = id) }
            }
            _state.value = _state.value.copy(aiBusy = false)
        }
    }

    private suspend fun refreshCategories() {
        _state.value = _state.value.copy(categories = repo.categoriesNow())
    }

    /**
     * Persists the transaction. For new, non-split entries it first checks for a possible
     * duplicate and surfaces a confirmation dialog (re-invoked with [confirmed] = true on accept).
     * In split mode it writes each portion as a linked expense; otherwise it inserts/updates a
     * single transaction. Invokes [onDone] once persisted.
     */
    fun save(onDone: () -> Unit, confirmed: Boolean = false) {
        val s = _state.value
        val accountId = s.accountId ?: return
        val now = System.currentTimeMillis()
        val account = s.accounts.firstOrNull { it.id == accountId }

        // Duplicate detection (PRD 6.4): warn once on a matching recent entry for new, non-split txns.
        if (!confirmed && !s.isEditing && !(s.splitMode && s.canSplit)) {
            val amt = s.amount.toDoubleOrNull()
            if (amt != null && amt > 0) {
                viewModelScope.launch {
                    val dup = repo.findPossibleDuplicate(s.type, accountId, amt)
                    if (dup != null) _state.value = _state.value.copy(duplicate = dup)
                    else save(onDone, confirmed = true)
                }
                return
            }
        }

        // Split expense: persist each portion as a linked transaction (PRD 6.4).
        if (s.splitMode && s.canSplit) {
            if (!s.splitValid) return
            val parts = s.splits.map { sp ->
                TransactionEntity(
                    type = TransactionType.EXPENSE,
                    amount = sp.amount.toDouble(),
                    currency = account?.currency ?: s.baseCurrency,
                    dateTime = s.dateTime,
                    categoryId = sp.categoryId,
                    accountId = accountId,
                    note = s.note,
                    tags = s.tags,
                    receiptPath = s.receiptPath,
                    createdAt = now,
                    updatedAt = now,
                )
            }
            viewModelScope.launch { repo.addSplit(parts); onDone() }
            return
        }

        val amount = s.amount.toDoubleOrNull() ?: return
        val tx = TransactionEntity(
            id = if (s.isEditing) s.editingId else 0,
            type = s.type,
            amount = amount,
            currency = account?.currency ?: s.baseCurrency,
            dateTime = s.dateTime,
            categoryId = if (s.type == TransactionType.TRANSFER) null else s.categoryId,
            accountId = accountId,
            transferToAccountId = if (s.type == TransactionType.TRANSFER) s.transferToAccountId else null,
            note = s.note,
            tags = s.tags,
            receiptPath = s.receiptPath,
            createdAt = if (s.isEditing) now else now,
            updatedAt = now,
        )
        viewModelScope.launch {
            if (s.isEditing) repo.updateTransaction(tx) else repo.addTransaction(tx)
            onDone()
        }
    }

    companion object {
        /**
         * Offline keyword hints mapping a default category name to substrings commonly found in a
         * quick-add phrase. Used by [inferCategoryId]; matching is case-insensitive and stops at the
         * first category whose keywords appear in the text. Only categories the user actually has
         * (matched by name) are applied, so custom/renamed setups degrade gracefully.
         */
        private val CATEGORY_KEYWORDS: List<Pair<String, List<String>>> = listOf(
            "Groceries" to listOf("grocery", "groceries", "supermarket", "vegetable", "fruit"),
            "Food" to listOf("lunch", "dinner", "breakfast", "food", "restaurant", "cafe", "coffee", "snack", "meal", "pizza", "burger"),
            "Transport" to listOf("uber", "taxi", "bus", "train", "fuel", "petrol", "gas ", "transport", "fare", "parking", "metro", "ride"),
            "Housing" to listOf("rent", "mortgage", "housing"),
            "Health" to listOf("gym", "doctor", "medicine", "pharmacy", "health", "hospital", "clinic", "dentist"),
            "Entertainment" to listOf("movie", "cinema", "game", "netflix", "spotify", "concert", "entertain"),
            "Shopping" to listOf("shopping", "shirt", "clothes", "shoes", "amazon", "mall", "dress"),
            "Bills" to listOf("bill", "electricity", "water bill", "internet", "phone bill", "utility", "recharge", "subscription"),
            "Education" to listOf("school", "course", "tuition", "education", "class"),
            // Income
            "Salary" to listOf("salary", "paycheck", "payroll", "wage"),
            "Business" to listOf("business", "client", "invoice", "sale"),
            "Gifts" to listOf("gift", "present"),
            "Investments" to listOf("dividend", "interest", "investment", "stock"),
        )
    }
}
