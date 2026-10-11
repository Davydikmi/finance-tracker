package com.example.finance_tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.finance_tracker.data.local.entity.ExchangeRateEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for the "exchange_rates" cache table.
 */
@Dao
interface ExchangeRateDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(rates: List<ExchangeRateEntity>)

    @Query("SELECT * FROM exchange_rates WHERE baseCurrency = :base")
    fun getRatesForBase(base: String): Flow<List<ExchangeRateEntity>>

    /** Returns null when this currency pair is not cached yet. */
    @Query("SELECT rate FROM exchange_rates WHERE baseCurrency = :base AND targetCurrency = :target")
    suspend fun getRate(base: String, target: String): Double?

    /** Timestamp of the most recent refresh across all cached rates, null when the table is empty. */
    @Query("SELECT MAX(updatedAt) FROM exchange_rates")
    suspend fun getLastUpdateTime(): Long?
}
