package com.example.finance_tracker.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A cached exchange rate from [baseCurrency] to [targetCurrency]
 * (ISO 4217 codes, e.g. "EUR" -> "USD"). Rates are refreshed periodically
 * from the configured source and served offline from this cache.
 */
@Entity(
    tableName = "exchange_rates",
    indices = [Index(value = ["baseCurrency", "targetCurrency"], unique = true)]
)
data class ExchangeRateEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val baseCurrency: String,
    val targetCurrency: String,
    val rate: Double,
    val updatedAt: Long
)
