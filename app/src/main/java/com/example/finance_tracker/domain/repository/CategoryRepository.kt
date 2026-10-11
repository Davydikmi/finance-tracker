package com.example.finance_tracker.domain.repository

import com.example.finance_tracker.domain.model.Category
import com.example.finance_tracker.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {

    /** Все категории, отсортированные по sortOrder, затем по имени. */
    fun getAllCategories(): Flow<List<Category>>

    /** Категории только заданного типа (для экрана создания транзакции). */
    fun getCategoriesByType(type: TransactionType): Flow<List<Category>>

    suspend fun getCategoryById(id: Long): Category?

    suspend fun insertCategory(category: Category): Long

    suspend fun updateCategory(category: Category)

    /**
     * Удаление категории. Транзакции этой категории НЕ удаляются —
     * их categoryId сбрасывается в 0 ("без категории", FK SET_DEFAULT).
     */
    suspend fun deleteCategory(category: Category)

    /** Число транзакций в категории — для предупреждения перед удалением. */
    suspend fun countTransactionsInCategory(categoryId: Long): Int

    /** Заполняет БД категориями по умолчанию, если она пуста. Идемпотентно. */
    suspend fun initializeDefaultCategories()
}
