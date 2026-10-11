package com.example.finance_tracker.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.finance_tracker.domain.model.RecurringPeriod
import com.example.finance_tracker.domain.model.TransactionType

/**
 * A single income or expense entry.
 *
 * When a category is deleted, [categoryId] falls back to the SQL default (0),
 * which means "no category" — the transaction itself is preserved.
 * For recurring transactions, [nextRecurrenceDate] holds the timestamp of the
 * next occurrence and is compared against "now" by the recurrence worker.
 */
@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_DEFAULT
        )
    ],
    indices = [
        Index(value = ["categoryId"]),
        Index(value = ["date"]),
        Index(value = ["type", "date"])
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val type: TransactionType,
    val amount: Double,
    @ColumnInfo(defaultValue = "0")
    val categoryId: Long = 0L,
    val date: Long,
    val description: String? = null,
    @ColumnInfo(name = "isRecurring")
    val isRecurring: Boolean = false,
    val recurringPeriod: RecurringPeriod? = null,
    val nextRecurrenceDate: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)
