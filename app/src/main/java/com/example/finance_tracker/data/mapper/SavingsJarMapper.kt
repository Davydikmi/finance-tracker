package com.example.finance_tracker.data.mapper

import com.example.finance_tracker.data.local.entity.SavingsJarEntity
import com.example.finance_tracker.domain.model.SavingsJar

fun SavingsJarEntity.toDomain(): SavingsJar = SavingsJar(
    id = id,
    name = name,
    targetAmount = targetAmount,
    savedAmount = savedAmount,
    icon = icon,
    color = color,
    createdAt = createdAt,
    isArchived = isArchived
)

fun SavingsJar.toEntity(): SavingsJarEntity = SavingsJarEntity(
    id = id,
    name = name,
    targetAmount = targetAmount,
    savedAmount = savedAmount,
    icon = icon,
    color = color,
    createdAt = createdAt,
    isArchived = isArchived
)
