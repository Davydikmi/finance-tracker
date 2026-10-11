package com.example.finance_tracker.domain.model

/**
 * Доменная модель транзакции для записи (create/update/delete).
 * Чтение списков для UI идёт через [TransactionDetail].
 */
data class Transaction(
    val id: Long = 0L,
    val type: TransactionType,
    val amount: Double,
    val categoryId: Long = 0L,
    val date: Long,
    val description: String? = null,
    val isRecurring: Boolean = false,
    val recurringPeriod: RecurringPeriod? = null,
    val nextRecurrenceDate: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)
