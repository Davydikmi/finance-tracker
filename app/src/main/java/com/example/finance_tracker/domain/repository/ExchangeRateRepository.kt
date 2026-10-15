package com.example.finance_tracker.domain.repository

import com.example.finance_tracker.domain.model.CurrencyRate
import kotlinx.coroutines.flow.Flow

interface ExchangeRateRepository {

    /**
     * Загружает ежедневные курсы НБ РБ и обновляет кэш в БД.
     * @return false, если сеть недоступна или вернула ошибку —
     * приложение продолжает работать на закэшированных курсах.
     */
    suspend fun refreshRates(): Boolean

    /**
     * Сколько BYN стоит 1 единица валюты [code] (например "USD").
     * null — курса нет в кэше. Для "BYN" всегда 1.0.
     */
    suspend fun getRatePerUnit(code: String): Double?

    /** Кэшированные курсы для перечня кодов (для карточек на главной). */
    fun observeRates(codes: List<String>): Flow<List<CurrencyRate>>

    /** Время последнего обновления кэша курсов; null — кэш пуст. */
    suspend fun getLastUpdateTime(): Long?
}
