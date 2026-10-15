package com.example.finance_tracker.domain.model

/**
 * Сумма расходов по одной категории за период — элемент статистики.
 * categoryName/categoryColor могут быть null, если категория удалена
 * (categoryId = 0). Room маппит результат SQL-запроса прямо в эту модель
 * по именам колонок (см. TransactionDao.observeExpensesByCategory).
 */
data class CategoryExpense(
    val categoryId: Long,
    val categoryName: String?,
    val categoryColor: Long?,
    val amount: Double
)
