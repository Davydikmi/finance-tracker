package com.example.finance_tracker.domain.model

/**
 * Лимит расходов по категории на месяц. monthYear — формат "YYYY-MM".
 */
data class CategoryLimit(
    val id: Long = 0L,
    val categoryId: Long,
    val monthYear: String,
    val limit: Double
)
