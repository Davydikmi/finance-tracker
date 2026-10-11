package com.example.finance_tracker.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A per-category spending limit for one month ([monthYear] format "YYYY-MM").
 * There is at most one limit per (category, month) pair; limits are removed
 * together with their category (CASCADE).
 *
 * [notified80] / [notified100] prevent repeated threshold notifications.
 */
@Entity(
    tableName = "category_limits",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["categoryId", "monthYear"], unique = true)]
)
data class CategoryLimitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val categoryId: Long,
    val monthYear: String,
    val limit: Double,
    val notified80: Boolean = false,
    val notified100: Boolean = false
)
