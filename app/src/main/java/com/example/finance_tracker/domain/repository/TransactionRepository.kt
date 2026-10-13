package com.example.finance_tracker.domain.repository

import com.example.finance_tracker.domain.model.CategoryExpense
import com.example.finance_tracker.domain.model.Transaction
import com.example.finance_tracker.domain.model.TransactionDetail
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {

    fun getAllTransactionsWithDetails(): Flow<List<TransactionDetail>>

    fun getTransactionsByDateRange(start: Long, end: Long): Flow<List<TransactionDetail>>

    suspend fun getTransactionById(id: Long): Transaction?

    suspend fun addTransaction(transaction: Transaction): Long

    suspend fun updateTransaction(transaction: Transaction)

    suspend fun deleteTransaction(transaction: Transaction)

    /** null — если расходов в периоде не было. */
    suspend fun getTotalExpenses(start: Long, end: Long): Double?

    /** null — если доходов в периоде не было. */
    suspend fun getTotalIncome(start: Long, end: Long): Double?

    /** Расходы периода по категориям, по убыванию суммы (для статистики). */
    fun observeExpensesByCategory(start: Long, end: Long): Flow<List<CategoryExpense>>
}
