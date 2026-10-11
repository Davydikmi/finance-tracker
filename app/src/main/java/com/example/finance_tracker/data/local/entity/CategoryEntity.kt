package com.example.finance_tracker.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.finance_tracker.domain.model.TransactionType

/**
 * A category a transaction can belong to (e.g. "Food", "Salary").
 *
 * [icon] holds a Material Icons name such as "restaurant" and [color]
 * holds the ARGB color the UI should render this category with.
 */
@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val icon: String,
    val color: Long,
    val type: TransactionType,
    @ColumnInfo(name = "isDefault")
    val isDefault: Boolean = false,
    val sortOrder: Int = 0
)
