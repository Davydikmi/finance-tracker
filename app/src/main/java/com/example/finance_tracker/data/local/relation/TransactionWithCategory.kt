package com.example.finance_tracker.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.finance_tracker.data.local.entity.CategoryEntity
import com.example.finance_tracker.data.local.entity.TransactionEntity

/**
 * Результат запроса транзакции вместе с её категорией (Room @Relation).
 *
 * category == null, если категория не была указана или удалена
 * (тогда categoryId у транзакции сбрасывается в 0 и совпадений нет).
 */
data class TransactionWithCategory(
    @Embedded
    val transaction: TransactionEntity,
    @Relation(parentColumn = "categoryId", entityColumn = "id")
    val category: CategoryEntity?
)
