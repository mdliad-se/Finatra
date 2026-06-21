package com.jinatra.finatra.ui.screens.accounts

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jinatra.finatra.data.local.entity.AccountEntity
import com.jinatra.finatra.data.local.entity.AccountType
import com.jinatra.finatra.data.prefs.SettingsRepository
import com.jinatra.finatra.data.repository.FinanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Editable form state for [AddAccountScreen]. Numeric fields are held as strings while typing.
 * [isEditing] is true when an existing account is being edited rather than created.
 */
data class AddAccountState(
    val editingId: Long = -1L,
    val name: String = "",
    val type: AccountType = AccountType.CASH,
    val currency: String = "USD",
    val openingBalance: String = "",
    val colorHex: Long = 0xFFE05454,
    val lowBalanceThreshold: String = "",
) { val isEditing get() = editingId > 0 }

/**
 * Backs [AddAccountScreen]. Loads an existing account when an `accountId` is passed (edit mode),
 * otherwise prefills the currency from app settings, applies field-level input sanitizing, and
 * persists the result via the repository.
 */
@HiltViewModel
class AddAccountViewModel @Inject constructor(
    private val repo: FinanceRepository,
    private val settings: SettingsRepository,
    savedState: SavedStateHandle,
) : ViewModel() {

    // Account being edited, or -1 for a brand-new account.
    private val accountId: Long = savedState.get<Long>("accountId") ?: -1L
    private val _state = MutableStateFlow(AddAccountState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            // Default new accounts to the app's base currency, then overwrite with the existing
            // account's values when editing.
            val base = settings.settings.first().baseCurrency
            _state.value = _state.value.copy(currency = base)
            if (accountId > 0) repo.accountById(accountId)?.let { a ->
                _state.value = AddAccountState(
                    editingId = a.id, name = a.name, type = a.type, currency = a.currency,
                    openingBalance = a.openingBalance.toString(), colorHex = a.colorHex,
                    lowBalanceThreshold = if (a.lowBalanceThreshold > 0) a.lowBalanceThreshold.toString() else "",
                )
            }
        }
    }

    fun setName(v: String) { _state.value = _state.value.copy(name = v) }
    fun setType(v: AccountType) { _state.value = _state.value.copy(type = v) }
    fun setCurrency(v: String) { _state.value = _state.value.copy(currency = v) }
    // Opening balance may be negative (e.g. credit card debt); strip any other characters.
    fun setOpening(v: String) { _state.value = _state.value.copy(openingBalance = v.filter { it.isDigit() || it == '.' || it == '-' }) }
    fun setColor(v: Long) { _state.value = _state.value.copy(colorHex = v) }
    // Threshold is a positive amount only.
    fun setLowBalance(v: String) { _state.value = _state.value.copy(lowBalanceThreshold = v.filter { it.isDigit() || it == '.' }) }

    /** Validates a non-blank name, then upserts the account and invokes [onDone]. */
    fun save(onDone: () -> Unit) {
        val s = _state.value
        if (s.name.isBlank()) return
        viewModelScope.launch {
            repo.upsertAccount(
                AccountEntity(
                    id = if (s.isEditing) s.editingId else 0,
                    name = s.name.trim(),
                    type = s.type,
                    currency = s.currency,
                    openingBalance = s.openingBalance.toDoubleOrNull() ?: 0.0,
                    colorHex = s.colorHex,
                    lowBalanceThreshold = s.lowBalanceThreshold.toDoubleOrNull() ?: 0.0,
                    createdAt = System.currentTimeMillis(),
                )
            )
            onDone()
        }
    }
}
