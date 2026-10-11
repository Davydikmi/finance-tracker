package com.example.finance_tracker.data.mapper

import com.example.finance_tracker.data.local.entity.BudgetEntity
import com.example.finance_tracker.data.local.entity.CategoryLimitEntity
import com.example.finance_tracker.domain.model.Budget
import com.example.finance_tracker.domain.model.CategoryLimit

fun BudgetEntity.toDomain(): Budget = Budget(
    id = id,
    monthYear = monthYear,
    totalLimit = totalLimit
)

fun CategoryLimitEntity.toDomain(): CategoryLimit = CategoryLimit(
    id = id,
    categoryId = categoryId,
    monthYear = monthYear,
    limit = limit
)

fun CategoryLimit.toEntity(): CategoryLimitEntity = CategoryLimitEntity(
    id = id,
    categoryId = categoryId,
    monthYear = monthYear,
    limit = limit
)
