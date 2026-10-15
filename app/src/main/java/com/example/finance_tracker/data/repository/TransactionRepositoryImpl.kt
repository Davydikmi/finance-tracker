package com.example.finance_tracker.data.repository

import com.example.finance_tracker.core.BudgetAlertChecker
import com.example.finance_tracker.data.local.dao.TransactionDao
import com.example.finance_tracker.data.mapper.toDomain
import com.example.finance_tracker.data.mapper.toEntity
import com.example.finance_tracker.domain.model.CategoryExpense
import com.example.finance_tracker.domain.model.Transaction
import com.example.finance_tracker.domain.model.TransactionDetail
import com.example.finance_tracker.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao,
    private val budgetAlertChecker: BudgetAlertChecker
) : TransactionRepository {

    override fun getAllTransactionsWithDetails(): Flow<List<TransactionDetail>> =
        transactionDao.getAllWithCategory().map { list -> list.map { it.toDomain() } }

    override fun getTransactionsByDateRange(start: Long, end: Long): Flow<List<TransactionDetail>> =
        transactionDao.getByDateRangeWithCategory(start, end).map { list -> list.map { it.toDomain() } }

    override suspend fun getTransactionById(id: Long): Transaction? =
        transactionDao.getById(id)?.toDomain()

    override suspend fun addTransaction(transaction: Transaction): Long {
        val id = transactionDao.insert(transaction.toEntity())
        // Порог 80/100% бюджета проверяем сразу после расхода/дохода.
        budgetAlertChecker.checkAndNotify()
        return id
    }

    override suspend fun updateTransaction(transaction: Transaction) =
        transactionDao.update(transaction.toEntity())

    override suspend fun deleteTransaction(transaction: Transaction) =
        transactionDao.delete(transaction.toEntity())

    override suspend fun getTotalExpenses(start: Long, end: Long): Double? =
        transactionDao.getTotalExpenses(start, end)

    override suspend fun getTotalIncome(start: Long, end: Long): Double? =
        transactionDao.getTotalIncome(start, end)

    override fun observeExpensesByCategory(start: Long, end: Long): Flow<List<CategoryExpense>> =
        transactionDao.observeExpensesByCategory(start, end)
}
