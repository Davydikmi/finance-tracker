package com.example.finance_tracker.domain.model

/**
 * Курс валюты к BYN: сколько BYN стоит 1 единица [code].
 */
data class CurrencyRate(
    val code: String,
    val ratePerUnit: Double,
    val updatedAt: Long
)
