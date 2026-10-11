package com.example.finance_tracker.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * The total spending limit for one month ([monthYear] format "YYYY-MM").
 * There is at most one row per month.
 *
 * [notified80] / [notified100] track whether the corresponding
 * "80% / 100% of budget spent" notification has already been shown,
 * so the worker fires each alert at most once per month.
 */
@Entity(
    tableName = "budgets",
    indices = [Index(value = ["monthYear"], unique = true)]
)
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val monthYear: String,
    val totalLimit: Double,
    val notified80: Boolean = false,
    val notified100: Boolean = false
)
