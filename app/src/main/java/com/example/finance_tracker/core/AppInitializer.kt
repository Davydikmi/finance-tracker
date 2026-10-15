package com.example.finance_tracker.core

import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.example.finance_tracker.domain.repository.CategoryRepository
import com.example.finance_tracker.domain.repository.ExchangeRateRepository
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
 * 2) применяет настройки: символ валюты + курс конвертации
 *    (см. [CurrencyConfig]) и язык интерфейса (per-app locale);
 * 3) периодически обновляет курсы НБ РБ в фоне.
 * Вызывается из FinanceTrackerApp.onCreate().
 */
@Singleton
class AppInitializer @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val settingsRepository: SettingsRepository,
    private val exchangeRateRepository: ExchangeRateRepository
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
                    applyCurrency(settings.baseCurrency)
                    // AppCompatDelegate требует главный поток.
                    withContext(Dispatchers.Main) {
                        applyLanguage(settings.language)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Не удалось применить настройки", e)
            }
        }
        refreshRatesPeriodically()
    }

    /**
     * Обновляет курсы при каждом старте (курсы НБ РБ меняются раз в день)
     * и затем раз в 6 часов, пока приложение живо. Офлайн — работаем
     * на закэшированных курсах.
     */
    private fun refreshRatesPeriodically() {
        scope.launch {
            while (true) {
                val refreshed = try {
                    exchangeRateRepository.refreshRates()
                } catch (e: Exception) {
                    Log.w(TAG, "Курсы не обновлены", e)
                    false
                }
                if (refreshed) {
                    Log.d(TAG, "Курсы НБ РБ обновлены")
                    // Перез применяем текущую валюту: курс мог измениться.
                    applyCurrency(settingsRepository.currentBaseCurrency())
                }
                kotlinx.coroutines.delay(RATES_REFRESH_INTERVAL_MS)
            }
        }
    }

    private suspend fun applyCurrency(currencyCode: String) {
        CurrencyConfig.symbol = CurrencyConfig.symbolFor(currencyCode)
        exchangeRateRepository.getRatePerUnit(currencyCode)?.let { rate ->
            CurrencyConfig.ratePerUnit = rate
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
        private const val RATES_REFRESH_INTERVAL_MS = 6 * 60 * 60 * 1000L
    }
}
