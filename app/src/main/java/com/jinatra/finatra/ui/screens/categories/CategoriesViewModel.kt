package com.jinatra.finatra.ui.screens.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jinatra.finatra.data.local.entity.CategoryEntity
import com.jinatra.finatra.data.repository.FinanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoriesViewModel @Inject constructor(
    private val repo: FinanceRepository,
) : ViewModel() {

    val categories = repo.observeCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun add(name: String, isIncome: Boolean, colorHex: Long) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repo.upsertCategory(
                CategoryEntity(name = name.trim(), colorHex = colorHex, iconKey = "category", isIncome = isIncome, isCustom = true)
            )
        }
    }

    fun delete(c: CategoryEntity) { viewModelScope.launch { repo.deleteCategory(c) } }
}
