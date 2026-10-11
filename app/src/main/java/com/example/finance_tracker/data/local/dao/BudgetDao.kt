package com.example.finance_tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.finance_tracker.data.local.entity.BudgetEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for the "budgets" table (monthly total spending limits).
 */
@Dao
interface BudgetDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(budget: BudgetEntity): Long

    @Query("SELECT * FROM budgets WHERE monthYear = :monthYear")
    suspend fun getByMonth(monthYear: String): BudgetEntity?

    /** Реактивное наблюдение за бюджетом месяца; null, пока бюджет не задан. */
    @Query("SELECT * FROM budgets WHERE monthYear = :monthYear")
    fun observeByMonth(monthYear: String): Flow<BudgetEntity?>

    /** Flags the 80% threshold notification as already shown for this budget. */
    @Query("UPDATE budgets SET notified80 = 1 WHERE id = :id")
    suspend fun markNotified80(id: Long)

    /** Flags the 100% threshold notification as already shown for this budget. */
    @Query("UPDATE budgets SET notified100 = 1 WHERE id = :id")
    suspend fun markNotified100(id: Long)
}
