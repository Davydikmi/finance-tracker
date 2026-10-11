package com.example.finance_tracker.data.mapper

import com.example.finance_tracker.data.local.entity.TransactionEntity
import com.example.finance_tracker.data.local.relation.TransactionWithCategory
import com.example.finance_tracker.domain.model.Transaction
import com.example.finance_tracker.domain.model.TransactionDetail

/** Relation из Room -> read-модель для UI. */
fun TransactionWithCategory.toDomain(): TransactionDetail = TransactionDetail(
    id = transaction.id,
    type = transaction.type,
    amount = transaction.amount,
    categoryId = transaction.categoryId,
    date = transaction.date,
    description = transaction.description,
    isRecurring = transaction.isRecurring,
    recurringPeriod = transaction.recurringPeriod,
    nextRecurrenceDate = transaction.nextRecurrenceDate,
    createdAt = transaction.createdAt,
    categoryName = category?.name,
    categoryColor = category?.color,
    categoryIcon = category?.icon
)

/** Доменная модель записи -> Room entity. */
fun Transaction.toEntity(): TransactionEntity = TransactionEntity(
    id = id,
    type = type,
    amount = amount,
    categoryId = categoryId,
    date = date,
    description = description,
    isRecurring = isRecurring,
    recurringPeriod = recurringPeriod,
    nextRecurrenceDate = nextRecurrenceDate,
    createdAt = createdAt
)

/** Room entity -> доменная модель (точечные чтения). */
fun TransactionEntity.toDomain(): Transaction = Transaction(
    id = id,
    type = type,
    amount = amount,
    categoryId = categoryId,
    date = date,
    description = description,
    isRecurring = isRecurring,
    recurringPeriod = recurringPeriod,
    nextRecurrenceDate = nextRecurrenceDate,
    createdAt = createdAt
)
