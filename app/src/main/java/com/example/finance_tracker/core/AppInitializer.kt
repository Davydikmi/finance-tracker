package com.example.finance_tracker.core

import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.example.finance_tracker.domain.repository.CategoryRepository
import com.example.finance_tracker.domain.repository.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Первичная инициализация приложения:
 * 1) заполняет БД категориями по умолчанию, если она пуста;
 * 2) применяет настройки на уровне приложения: символ базовой валюты
 *    (см. [CurrencyConfig]) и язык интерфейса (per-app locale).
 * Вызывается из FinanceTrackerApp.onCreate().
 */
@Singleton
class AppInitializer @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val settingsRepository: SettingsRepository
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
        scope.launch {
            try {
                settingsRepository.observeSettings().collect { settings ->
                    CurrencyConfig.symbol = CurrencyConfig.symbolFor(settings.baseCurrency)
                    // AppCompatDelegate требует главный поток.
                    withContext(Dispatchers.Main) {
                        applyLanguage(settings.language)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Не удалось применить настройки", e)
            }
        }
    }

    /**
     * Per-app язык через AppCompatDelegate: на Android 13+ работает нативно,
     * на более старых устройствах сохраняется и применится при пересоздании
     * активити. Повторный вызов с тем же языком игнорируется — без цикла
     * пересозданий.
     */
    private fun applyLanguage(languageCode: String) {
        val desired = LocaleListCompat.forLanguageTags(languageCode)
        if (AppCompatDelegate.getApplicationLocales() != desired) {
            AppCompatDelegate.setApplicationLocales(desired)
        }
    }

    companion object {
        private const val TAG = "AppInitializer"
    }
}
