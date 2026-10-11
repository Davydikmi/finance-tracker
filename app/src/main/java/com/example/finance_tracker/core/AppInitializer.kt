package com.example.finance_tracker.core

import android.util.Log
import com.example.finance_tracker.domain.repository.CategoryRepository
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
    private val categoryRepository: CategoryRepository
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun initialize() {
        scope.launch {
            try {
                categoryRepository.initializeDefaultCategories()
            } catch (e: Exception) {
                Log.e(TAG, "Не удалось заполнить БД категориями по умолчанию", e)
            }
        }
    }

    companion object {
        private const val TAG = "AppInitializer"
    }
}
