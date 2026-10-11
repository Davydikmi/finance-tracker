package com.example.finance_tracker.domain.model

/**
 * Категория транзакции (доход/расход).
 * icon — имя Material-иконки (например "restaurant"), color — ARGB-цвет.
 */
data class Category(
    val id: Long = 0L,
    val name: String,
    val icon: String,
    val color: Long,
    val type: TransactionType,
    val isDefault: Boolean = false,
    val sortOrder: Int = 0
)
