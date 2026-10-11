package com.example.finance_tracker.data.repository

import com.example.finance_tracker.data.local.dao.CategoryDao
import com.example.finance_tracker.data.local.entity.CategoryEntity
import com.example.finance_tracker.data.mapper.toDomain
import com.example.finance_tracker.data.mapper.toEntity
import com.example.finance_tracker.domain.model.Category
import com.example.finance_tracker.domain.model.TransactionType
import com.example.finance_tracker.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepositoryImpl @Inject constructor(
    private val categoryDao: CategoryDao
) : CategoryRepository {

    override fun getAllCategories(): Flow<List<Category>> =
        categoryDao.getAll().map { categories -> categories.map { it.toDomain() } }

    override fun getCategoriesByType(type: TransactionType): Flow<List<Category>> =
        categoryDao.getByType(type).map { categories -> categories.map { it.toDomain() } }

    override suspend fun getCategoryById(id: Long): Category? =
        categoryDao.getById(id)?.toDomain()

    override suspend fun insertCategory(category: Category): Long =
        categoryDao.insert(category.toEntity())

    override suspend fun updateCategory(category: Category) =
        categoryDao.update(category.toEntity())

    override suspend fun deleteCategory(category: Category) =
        categoryDao.delete(category.toEntity())

    override suspend fun countTransactionsInCategory(categoryId: Long): Int =
        categoryDao.countTransactionsInCategory(categoryId)

    override suspend fun initializeDefaultCategories() {
        if (categoryDao.getCount() > 0) return
        DEFAULT_EXPENSE_CATEGORIES.forEach { categoryDao.insert(it) }
        DEFAULT_INCOME_CATEGORIES.forEach { categoryDao.insert(it) }
    }

    companion object {
        private val DEFAULT_EXPENSE_CATEGORIES = listOf(
            CategoryEntity(name = "Еда", icon = "restaurant", color = 0xFFEF5350, type = TransactionType.EXPENSE, isDefault = true, sortOrder = 1),
            CategoryEntity(name = "Транспорт", icon = "directions_bus", color = 0xFF42A5F5, type = TransactionType.EXPENSE, isDefault = true, sortOrder = 2),
            CategoryEntity(name = "Жильё", icon = "home", color = 0xFF8D6E63, type = TransactionType.EXPENSE, isDefault = true, sortOrder = 3),
            CategoryEntity(name = "Развлечения", icon = "movie", color = 0xFFAB47BC, type = TransactionType.EXPENSE, isDefault = true, sortOrder = 4),
            CategoryEntity(name = "Здоровье", icon = "medical_services", color = 0xFF66BB6A, type = TransactionType.EXPENSE, isDefault = true, sortOrder = 5),
            CategoryEntity(name = "Покупки", icon = "shopping_bag", color = 0xFFFFB300, type = TransactionType.EXPENSE, isDefault = true, sortOrder = 6),
            CategoryEntity(name = "Связь", icon = "smartphone", color = 0xFF26C6DA, type = TransactionType.EXPENSE, isDefault = true, sortOrder = 7),
            CategoryEntity(name = "Прочее", icon = "category", color = 0xFF9E9E9E, type = TransactionType.EXPENSE, isDefault = true, sortOrder = 8)
        )

        private val DEFAULT_INCOME_CATEGORIES = listOf(
            CategoryEntity(name = "Зарплата", icon = "payments", color = 0xFF66BB6A, type = TransactionType.INCOME, isDefault = true, sortOrder = 1),
            CategoryEntity(name = "Подарки", icon = "card_giftcard", color = 0xFFFFB74D, type = TransactionType.INCOME, isDefault = true, sortOrder = 2),
            CategoryEntity(name = "Прочий доход", icon = "attach_money", color = 0xFF9E9E9E, type = TransactionType.INCOME, isDefault = true, sortOrder = 3)
        )
    }
}
