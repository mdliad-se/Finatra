package com.jinatra.finatra.ui.screens.budgets

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jinatra.finatra.data.local.entity.BudgetEntity
import com.jinatra.finatra.data.local.entity.BudgetPeriod
import com.jinatra.finatra.data.local.entity.CategoryEntity
import com.jinatra.finatra.data.repository.FinanceRepository
import com.jinatra.finatra.util.DateUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddBudgetState(
    val editingId: Long = -1L,
    val categories: List<CategoryEntity> = emptyList(),
    val categoryId: Long? = null,
    val amount: String = "",
    val period: BudgetPeriod = BudgetPeriod.MONTHLY,
) { val isEditing get() = editingId > 0 }

@HiltViewModel
class AddBudgetViewModel @Inject constructor(
    private val repo: FinanceRepository,
    savedState: SavedStateHandle,
) : ViewModel() {

    private val budgetId: Long = savedState.get<Long>("budgetId") ?: -1L
    private val _state = MutableStateFlow(AddBudgetState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val cats = repo.observeCategories().first().filter { !it.isIncome && it.parentId == null }
            _state.value = _state.value.copy(categories = cats, categoryId = cats.firstOrNull()?.id)
            if (budgetId > 0) repo.observeBudgets().first().firstOrNull { it.id == budgetId }?.let { b ->
                _state.value = _state.value.copy(
                    editingId = b.id, categoryId = b.categoryId,
                    amount = b.amount.toString(), period = b.period,
                )
            }
        }
    }

    fun setCategory(id: Long) { _state.value = _state.value.copy(categoryId = id) }
    fun setAmount(v: String) { _state.value = _state.value.copy(amount = v.filter { it.isDigit() || it == '.' }) }
    fun setPeriod(p: BudgetPeriod) { _state.value = _state.value.copy(period = p) }

    fun save(onDone: () -> Unit) {
        val s = _state.value
        val amount = s.amount.toDoubleOrNull() ?: return
        val catId = s.categoryId ?: return
        viewModelScope.launch {
            repo.upsertBudget(
                BudgetEntity(
                    id = if (s.isEditing) s.editingId else 0,
                    categoryId = catId,
                    amount = amount,
                    period = s.period,
                    startDate = DateUtil.startOfMonth(),
                    endDate = if (s.period == BudgetPeriod.MONTHLY) null else DateUtil.endOfMonth(),
                    createdAt = System.currentTimeMillis(),
                )
            )
            onDone()
        }
    }
}
