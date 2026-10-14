package com.example.finance_tracker.domain.repository

import com.example.finance_tracker.domain.model.AppSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {

    /** Единственная строка настроек; создаётся сидом при первом запуске. */
    fun observeSettings(): Flow<AppSettings>

    suspend fun setThemeMode(mode: String)

    suspend fun setBaseCurrency(currencyCode: String)

    suspend fun setLanguage(languageCode: String)
}
