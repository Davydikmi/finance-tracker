package com.example.finance_tracker.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A savings jar — a separate envelope of money the user is putting aside.
 *
 * A null [targetAmount] means the jar is just a fund with no savings goal.
 * Archived jars are kept in the database but hidden from the active list.
 */
@Entity(tableName = "savings_jars")
data class SavingsJarEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val targetAmount: Double? = null,
    val savedAmount: Double = 0.0,
    val icon: String,
    val color: Long,
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "isArchived")
    val isArchived: Boolean = false
)
