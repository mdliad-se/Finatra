package com.jinatra.finatra.data.local

import com.jinatra.finatra.data.local.entity.CategoryEntity

/** Predefined categories seeded on first launch. iconKey maps to Material icons in CategoryIcons. */
object DefaultCategories {
    val list: List<CategoryEntity> = listOf(
        cat("Food", 0xFFE57373, "restaurant"),
        cat("Transport", 0xFF64B5F6, "directions_car"),
        cat("Housing", 0xFF9575CD, "home"),
        cat("Health", 0xFF4DB6AC, "favorite"),
        cat("Entertainment", 0xFFFFB74D, "movie"),
        cat("Shopping", 0xFFF06292, "shopping_bag"),
        cat("Bills", 0xFF7986CB, "receipt_long"),
        cat("Education", 0xFF4FC3F7, "school"),
        cat("Groceries", 0xFF81C784, "local_grocery_store"),
        cat("Other", 0xFF90A4AE, "category"),
        // income
        cat("Salary", 0xFF66BB6A, "payments", income = true),
        cat("Business", 0xFF26A69A, "storefront", income = true),
        cat("Gifts", 0xFFFF8A65, "card_giftcard", income = true),
        cat("Investments", 0xFF42A5F5, "trending_up", income = true),
    )

    private fun cat(name: String, color: Long, icon: String, income: Boolean = false) =
        CategoryEntity(name = name, colorHex = color, iconKey = icon, isIncome = income, isCustom = false)
}
