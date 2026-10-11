package com.example.finance_tracker.domain.repository

import com.example.finance_tracker.domain.model.SavingsJar
import kotlinx.coroutines.flow.Flow

interface SavingsJarRepository {

    fun getActiveJars(): Flow<List<SavingsJar>>

    suspend fun getJarById(id: Long): SavingsJar?

    suspend fun createJar(jar: SavingsJar): Long

    suspend fun updateJar(jar: SavingsJar)

    suspend fun deleteJar(jar: SavingsJar)

    /**
     * Положить средства в копилку: создаёт движение DEPOSIT и
     * увеличивает баланс. Атомарно (Room-транзакция).
     * @throws IllegalArgumentException если amount <= 0 или копилка не найдена
     */
    suspend fun addFunds(jarId: Long, amount: Double)

    /**
     * Снять средства из копилки: создаёт движение WITHDRAW и
     * уменьшает баланс. Атомарно (Room-транзакция).
     * @throws IllegalArgumentException если amount <= 0, копилка не найдена
     * или средств недостаточно
     */
    suspend fun withdrawFunds(jarId: Long, amount: Double)
}
