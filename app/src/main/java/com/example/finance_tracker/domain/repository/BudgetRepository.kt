package com.example.finance_tracker.domain.repository

import com.example.finance_tracker.domain.model.Budget
import com.example.finance_tracker.domain.model.CategoryLimit
import kotlinx.coroutines.flow.Flow

interface BudgetRepository {

    /** Бюджет месяца; null, пока не задан. monthYear — "YYYY-MM". */
    fun observeMonthlyBudget(monthYear: String): Flow<Budget?>

    /**
     * Лимит трат на сегодня: (бюджет месяца − потрачено) / осталось дней.
     * null — бюджет не задан; никогда не отрицательный.
     */
    fun getDailySpendingLimit(): Flow<Double?>

    /** Создаёт или обновляет бюджет месяца, сохраняя флаги уведомлений. */
    suspend fun setMonthlyBudget(monthYear: String, totalLimit: Double)

    fun observeCategoryLimits(monthYear: String): Flow<List<CategoryLimit>>

    suspend fun getCategoryLimit(categoryId: Long, monthYear: String): CategoryLimit?

    /** Создаёт или обновляет лимит категории, сохраняя флаги уведомлений. */
    suspend fun setCategoryLimit(categoryId: Long, monthYear: String, limit: Double)

    suspend fun deleteCategoryLimit(limit: CategoryLimit)

    /** Пометить, что уведомление о 80% бюджета уже показано. */
    suspend fun markBudgetNotified80(budgetId: Long)

    /** Пометить, что уведомление о 100% бюджета уже показано. */
    suspend fun markBudgetNotified100(budgetId: Long)

    /** Пометить, что уведомление о 80% лимита категории уже показано. */
    suspend fun markLimitNotified80(limitId: Long)

    /** Пометить, что уведомление о 100% лимита категории уже показано. */
    suspend fun markLimitNotified100(limitId: Long)
}
