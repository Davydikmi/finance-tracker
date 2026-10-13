package com.example.finance_tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.finance_tracker.data.local.entity.TransactionEntity
import com.example.finance_tracker.data.local.relation.TransactionWithCategory
import com.example.finance_tracker.domain.model.CategoryExpense
import com.example.finance_tracker.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow

/**
 * DAO for the "transactions" table.
 *
 * The 'EXPENSE' / 'INCOME' literals in the SUM queries below match the
 * string form [com.example.finance_tracker.data.local.Converters] persists
 * for [TransactionType] (the enum name).
 */
@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity): Long

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Delete
    suspend fun delete(transaction: TransactionEntity)

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAll(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: Long): TransactionEntity?

    @Query("SELECT * FROM transactions WHERE date BETWEEN :start AND :end ORDER BY date DESC")
    fun getByDateRange(start: Long, end: Long): Flow<List<TransactionEntity>>

    /** Все транзакции, сразу с категорией (JOIN через @Relation). */
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllWithCategory(): Flow<List<TransactionWithCategory>>

    /** Транзакции периода, сразу с категорией (JOIN через @Relation). */
    @Query("SELECT * FROM transactions WHERE date BETWEEN :start AND :end ORDER BY date DESC")
    fun getByDateRangeWithCategory(start: Long, end: Long): Flow<List<TransactionWithCategory>>

    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY date DESC")
    fun getByType(type: TransactionType): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE description LIKE '%' || :query || '%' ORDER BY date DESC")
    fun search(query: String): Flow<List<TransactionEntity>>

    /** Returns null when there are no matching rows (SUM over an empty set). */
    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'EXPENSE' AND date BETWEEN :start AND :end")
    suspend fun getTotalExpenses(start: Long, end: Long): Double?

    /** Returns null when there are no matching rows (SUM over an empty set). */
    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'INCOME' AND date BETWEEN :start AND :end")
    suspend fun getTotalIncome(start: Long, end: Long): Double?

    /** Returns null when there are no matching rows (SUM over an empty set). */
    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'EXPENSE' AND categoryId = :categoryId AND date BETWEEN :start AND :end")
    suspend fun getExpensesByCategory(categoryId: Long, start: Long, end: Long): Double?

    /**
     * Recurring transactions whose next occurrence is due at or before [now].
     * Rows with a NULL nextRecurrenceDate never match (NULL comparison), so
     * non-recurring entries are excluded even without checking isRecurring.
     * Synchronous list (no Flow) — called from the WorkManager recurrence job.
     */
    @Query("SELECT * FROM transactions WHERE isRecurring = 1 AND nextRecurrenceDate <= :now")
    suspend fun getDueRecurring(now: Long): List<TransactionEntity>

    @Query("SELECT * FROM transactions ORDER BY date DESC LIMIT 5")
    fun getLast5(): Flow<List<TransactionEntity>>

    /**
     * Расходы периода, сгруппированные по категориям, по убыванию суммы.
     * Категории с id = 0 ("без категории") дают categoryName = null.
     * Room маппит колонки запроса в доменную модель CategoryExpense по именам.
     */
    @Query(
        """
        SELECT t.categoryId AS categoryId,
               c.name AS categoryName,
               c.color AS categoryColor,
               SUM(t.amount) AS amount
        FROM transactions t
        LEFT JOIN categories c ON c.id = t.categoryId
        WHERE t.type = 'EXPENSE' AND t.date BETWEEN :start AND :end
        GROUP BY t.categoryId
        ORDER BY amount DESC
        """
    )
    fun observeExpensesByCategory(start: Long, end: Long): Flow<List<CategoryExpense>>
}
