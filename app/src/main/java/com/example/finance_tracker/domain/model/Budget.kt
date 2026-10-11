package com.example.finance_tracker.domain.model

/**
 * Общий месячный лимит расходов. monthYear — формат "YYYY-MM" (например "2026-10").
 * Служебные флаги уведомлений (notified80/100) — внутренняя кухня data-слоя
 * и в домен не выносятся.
 */
data class Budget(
    val id: Long = 0L,
    val monthYear: String,
    val totalLimit: Double
)
