package com.example.finance_tracker.data.mapper

import com.example.finance_tracker.data.local.entity.CategoryEntity
import com.example.finance_tracker.domain.model.Category

fun CategoryEntity.toDomain(): Category = Category(
    id = id,
    name = name,
    icon = icon,
    color = color,
    type = type,
    isDefault = isDefault,
    sortOrder = sortOrder
)

fun Category.toEntity(): CategoryEntity = CategoryEntity(
    id = id,
    name = name,
    icon = icon,
    color = color,
    type = type,
    isDefault = isDefault,
    sortOrder = sortOrder
)
