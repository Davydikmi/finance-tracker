package com.example.finance_tracker.core

import android.util.Log
import com.example.finance_tracker.data.local.dao.CategoryDao
import com.example.finance_tracker.data.local.entity.CategoryEntity
import com.example.finance_tracker.domain.model.TransactionType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Первичная инициализация приложения.
 * Заполняет БД значениями по умолчанию (категории), если она пуста.
 * Вызывается из FinanceTrackerApp.onCreate().
 */
@Singleton
class AppInitializer @Inject constructor(
    private val categoryDao: CategoryDao
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun initialize() {
        scope.launch {
            try {
                seedDefaultCategories()
            } catch (e: Exception) {
                Log.e(TAG, "Не удалось заполнить БД категориями по умолчанию", e)
            }
        }
    }

    private suspend fun seedDefaultCategories() {
        if (categoryDao.getCount() > 0) return
        Log.d(TAG, "БД пуста — заполняю категории по умолчанию")
        DEFAULT_EXPENSE_CATEGORIES.forEach { categoryDao.insert(it) }
        DEFAULT_INCOME_CATEGORIES.forEach { categoryDao.insert(it) }
        Log.d(TAG, "Категории по умолчанию созданы")
    }

    companion object {
        private const val TAG = "AppInitializer"

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
