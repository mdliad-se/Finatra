package com.jinatra.finatra.ui.screens.addtransaction

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jinatra.finatra.data.local.entity.AccountEntity
import com.jinatra.finatra.data.local.entity.CategoryEntity
import com.jinatra.finatra.data.local.entity.TransactionEntity
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
) {
    val isEditing: Boolean get() = editingId > 0
    val visibleCategories: List<CategoryEntity>
        get() = categories.filter { it.isIncome == (type == TransactionType.INCOME) && it.parentId == null }
}

@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    private val repo: FinanceRepository,
    private val settings: SettingsRepository,
    private val ai: AiService,
    savedState: SavedStateHandle,
) : ViewModel() {

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
    }

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

    /** Naive on-device natural-language parse (fallback when AI unavailable). */
    fun quickParse(text: String) {
        val amount = Regex("""\d+(?:\.\d+)?""").find(text)?.value ?: ""
        val note = text.replace(Regex("""(spent|paid|got|received|on|for|today|yesterday)""", RegexOption.IGNORE_CASE), "")
            .replace(amount, "").trim().replace(Regex("\\s+"), " ")
        val income = Regex("(received|got|salary|income)", RegexOption.IGNORE_CASE).containsMatchIn(text)
        _state.value = _state.value.copy(
            amount = amount,
            note = note.replaceFirstChar { it.uppercase() },
            type = if (income) TransactionType.INCOME else TransactionType.EXPENSE,
        )
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

    fun save(onDone: () -> Unit) {
        val s = _state.value
        val amount = s.amount.toDoubleOrNull() ?: return
        val accountId = s.accountId ?: return
        val now = System.currentTimeMillis()
        val account = s.accounts.firstOrNull { it.id == accountId }
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
}
