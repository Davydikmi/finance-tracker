package com.example.finance_tracker.data.repository

import com.example.finance_tracker.data.local.dao.SettingsDao
import com.example.finance_tracker.data.local.entity.SettingsEntity
import com.example.finance_tracker.domain.model.AppSettings
import com.example.finance_tracker.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val settingsDao: SettingsDao
) : SettingsRepository {

    override fun observeSettings(): Flow<AppSettings> =
        settingsDao.getSettings().map { it.toAppSettings() }

    override suspend fun currentBaseCurrency(): String =
        settingsDao.getSettingsOnce()?.baseCurrency ?: AppSettings.DEFAULT_CURRENCY

    override suspend fun setThemeMode(mode: String) = update { it.copy(themeMode = mode) }

    override suspend fun setBaseCurrency(currencyCode: String) =
        update { it.copy(baseCurrency = currencyCode) }

    override suspend fun setLanguage(languageCode: String) =
        update { it.copy(language = languageCode) }

    override suspend fun setNotificationsEnabled(enabled: Boolean) =
        update { it.copy(notificationsEnabled = enabled) }

    /** Строка настроек ровно одна — читаем, преобразуем, перезаписываем. */
    private suspend fun update(transform: (SettingsEntity) -> SettingsEntity) {
        val current = settingsDao.getSettingsOnce() ?: return
        settingsDao.update(transform(current))
    }

    private fun SettingsEntity.toAppSettings() = AppSettings(
        themeMode = themeMode,
        baseCurrency = baseCurrency,
        language = language,
        notificationsEnabled = notificationsEnabled
    )
}
