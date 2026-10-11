package com.example.finance_tracker.domain.model

/**
 * Копилка (конверт накоплений). targetAmount == null — просто фонд без цели.
 */
data class SavingsJar(
    val id: Long = 0L,
    val name: String,
    val targetAmount: Double? = null,
    val savedAmount: Double = 0.0,
    val icon: String,
    val color: Long,
    val createdAt: Long = System.currentTimeMillis(),
    val isArchived: Boolean = false
)
