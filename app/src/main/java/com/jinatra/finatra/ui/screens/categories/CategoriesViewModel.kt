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

/**
 * ViewModel backing [CategoriesScreen]. Exposes the live list of categories and
 * handles creating custom categories and deleting them via [FinanceRepository].
 */
@HiltViewModel
class CategoriesViewModel @Inject constructor(
    private val repo: FinanceRepository,
) : ViewModel() {

    /** Live stream of all categories, surfaced as state for the UI. */
    val categories = repo.observeCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Creates a new custom category; ignored when [name] is blank. */
    fun add(name: String, isIncome: Boolean, colorHex: Long) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repo.upsertCategory(
                CategoryEntity(name = name.trim(), colorHex = colorHex, iconKey = "category", isIncome = isIncome, isCustom = true)
            )
        }
    }

    /** Deletes the given category. */
    fun delete(c: CategoryEntity) { viewModelScope.launch { repo.deleteCategory(c) } }
}
