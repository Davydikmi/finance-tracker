package com.example.finance_tracker.data.repository

import androidx.room.withTransaction
import com.example.finance_tracker.data.local.AppDatabase
import com.example.finance_tracker.data.local.dao.CategoryDao
import com.example.finance_tracker.data.local.dao.JarMovementDao
import com.example.finance_tracker.data.local.dao.SavingsJarDao
import com.example.finance_tracker.data.local.dao.TransactionDao
import com.example.finance_tracker.data.local.entity.CategoryEntity
import com.example.finance_tracker.data.local.entity.JarMovementEntity
import com.example.finance_tracker.data.local.entity.TransactionEntity
import com.example.finance_tracker.data.mapper.toDomain
import com.example.finance_tracker.data.mapper.toEntity
import com.example.finance_tracker.domain.model.JarMovementType
import com.example.finance_tracker.domain.model.SavingsJar
import com.example.finance_tracker.domain.model.TransactionType
import com.example.finance_tracker.domain.repository.SavingsJarRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SavingsJarRepositoryImpl @Inject constructor(
    private val database: AppDatabase,
    private val savingsJarDao: SavingsJarDao,
    private val jarMovementDao: JarMovementDao,
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao
) : SavingsJarRepository {

    override fun getActiveJars(): Flow<List<SavingsJar>> =
        savingsJarDao.getActive().map { jars -> jars.map { it.toDomain() } }

    override suspend fun getJarById(id: Long): SavingsJar? =
        savingsJarDao.getById(id)?.toDomain()

    override suspend fun createJar(jar: SavingsJar): Long =
        savingsJarDao.insert(jar.toEntity())

    override suspend fun updateJar(jar: SavingsJar) =
        savingsJarDao.update(jar.toEntity())

    override suspend fun deleteJar(jar: SavingsJar) =
        savingsJarDao.delete(jar.toEntity())

    override suspend fun addFunds(jarId: Long, amount: Double) {
        require(amount > 0) { "Сумма пополнения должна быть положительной, получено: $amount" }
        val jar = savingsJarDao.getById(jarId)
            ?: throw IllegalArgumentException("Копилка с id=$jarId не найдена")
        val newAmount = jar.savedAmount + amount
        database.withTransaction {
            jarMovementDao.insert(
                JarMovementEntity(
                    jarId = jarId,
                    amount = amount,
                    type = JarMovementType.DEPOSIT,
                    date = System.currentTimeMillis()
                )
            )
            savingsJarDao.updateSavedAmount(jarId, newAmount)
            // Деньги ушли со счёта в копилку — отражаем как расход.
            transactionDao.insert(
                buildJarTransaction(
                    jarName = jar.name,
                    type = TransactionType.EXPENSE,
                    amount = amount,
                    description = "Отложено в «${jar.name}»"
                )
            )
        }
    }

    override suspend fun withdrawFunds(jarId: Long, amount: Double) {
        require(amount > 0) { "Сумма снятия должна быть положительной, получено: $amount" }
        val jar = savingsJarDao.getById(jarId)
            ?: throw IllegalArgumentException("Копилка с id=$jarId не найдена")
        require(jar.savedAmount >= amount) {
            "Недостаточно средств в копилке: доступно ${jar.savedAmount}, запрошено $amount"
        }
        database.withTransaction {
            jarMovementDao.insert(
                JarMovementEntity(
                    jarId = jarId,
                    amount = amount,
                    type = JarMovementType.WITHDRAW,
                    date = System.currentTimeMillis()
                )
            )
            savingsJarDao.updateSavedAmount(jarId, jar.savedAmount - amount)
            // Деньги вернулись на счёт — отражаем как доход.
            transactionDao.insert(
                buildJarTransaction(
                    jarName = jar.name,
                    type = TransactionType.INCOME,
                    amount = amount,
                    description = "Снято из «${jar.name}»"
                )
            )
        }
    }

    /**
     * Операция движения копилки в общем списке транзакций: пополнение —
     * расход, снятие — доход. Категория «Копилка» создаётся при первом
     * использовании отдельно для каждого типа (категории типизированы).
     */
    private suspend fun buildJarTransaction(
        jarName: String,
        type: TransactionType,
        amount: Double,
        description: String
    ): TransactionEntity {
        val categoryId = categoryDao.getByNameAndType(JAR_CATEGORY_NAME, type)?.id
            ?: categoryDao.insert(
                CategoryEntity(
                    name = JAR_CATEGORY_NAME,
                    icon = "savings",
                    color = 0xFF2F80ED,
                    type = type,
                    isDefault = true,
                    sortOrder = 100
                )
            )
        return TransactionEntity(
            type = type,
            amount = amount,
            categoryId = categoryId,
            date = System.currentTimeMillis(),
            description = description
        )
    }

    private companion object {
        const val JAR_CATEGORY_NAME = "Копилка"
    }
}
