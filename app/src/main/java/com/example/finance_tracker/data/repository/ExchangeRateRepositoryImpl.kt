package com.example.finance_tracker.data.repository

import com.example.finance_tracker.data.local.dao.ExchangeRateDao
import com.example.finance_tracker.data.local.entity.ExchangeRateEntity
import com.example.finance_tracker.data.remote.NbrbApi
import com.example.finance_tracker.domain.model.CurrencyRate
import com.example.finance_tracker.domain.repository.ExchangeRateRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Курсы валют: суммы в БД всегда хранятся в BYN (базовая валюта),
 * кэш хранит нормализованный курс «BYN за 1 единицу валюты».
 */
@Singleton
class ExchangeRateRepositoryImpl @Inject constructor(
    private val nbrbApi: NbrbApi,
    private val exchangeRateDao: ExchangeRateDao
) : ExchangeRateRepository {

    override suspend fun refreshRates(): Boolean = withContext(Dispatchers.IO) {
        try {
            val rates = nbrbApi.getDailyRates()
            val entities = rates
                .filter { it.officialRate != null && it.scale > 0 }
                .map { dto ->
                    ExchangeRateEntity(
                        baseCurrency = BASE_CURRENCY,
                        targetCurrency = dto.abbreviation,
                        // Нормализация: НБ РБ даёт курс за Cur_Scale единиц.
                        rate = dto.officialRate!! / dto.scale,
                        updatedAt = System.currentTimeMillis()
                    )
                }
            exchangeRateDao.insertAll(entities)
            true
        } catch (e: UnknownHostException) {
            false
        } catch (e: SocketTimeoutException) {
            false
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getRatePerUnit(code: String): Double? {
        if (code == BASE_CURRENCY) return 1.0
        return exchangeRateDao.getRate(BASE_CURRENCY, code)
    }

    override fun observeRates(codes: List<String>): Flow<List<CurrencyRate>> =
        exchangeRateDao.getRatesForBase(BASE_CURRENCY).map { entities ->
            entities
                .filter { it.targetCurrency in codes }
                .map { CurrencyRate(it.targetCurrency, it.rate, it.updatedAt) }
        }

    override suspend fun getLastUpdateTime(): Long? =
        exchangeRateDao.getLastUpdateTime()

    private companion object {
        const val BASE_CURRENCY = "BYN"
    }
}
