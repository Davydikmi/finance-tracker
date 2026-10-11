package com.example.finance_tracker.domain.model

/**
 * Транзакция, обогащённая данными категории — основная read-модель
 * для списков в UI. Поля категории nullable: категория могла быть
 * удалена или не указана.
 */
data class TransactionDetail(
    val id: Long = 0L,
    val type: TransactionType,
    val amount: Double,
    val categoryId: Long,
    val date: Long,
    val description: String? = null,
    val isRecurring: Boolean = false,
    val recurringPeriod: RecurringPeriod? = null,
    val nextRecurrenceDate: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val categoryName: String? = null,
    val categoryColor: Long? = null,
    val categoryIcon: String? = null
)
