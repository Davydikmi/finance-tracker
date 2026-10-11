package com.example.finance_tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.finance_tracker.data.local.entity.CategoryLimitEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for the "category_limits" table (per-category monthly spending limits).
 */
@Dao
interface CategoryLimitDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(limit: CategoryLimitEntity): Long

    @Update
    suspend fun update(limit: CategoryLimitEntity)

    @Delete
    suspend fun delete(limit: CategoryLimitEntity)

    @Query("SELECT * FROM category_limits WHERE monthYear = :monthYear")
    fun getByMonth(monthYear: String): Flow<List<CategoryLimitEntity>>

    @Query("SELECT * FROM category_limits WHERE categoryId = :categoryId AND monthYear = :monthYear")
    suspend fun getByCategoryAndMonth(categoryId: Long, monthYear: String): CategoryLimitEntity?

    /** Flags the 80% threshold notification as already shown for this limit. */
    @Query("UPDATE category_limits SET notified80 = 1 WHERE id = :id")
    suspend fun markNotified80(id: Long)

    /** Flags the 100% threshold notification as already shown for this limit. */
    @Query("UPDATE category_limits SET notified100 = 1 WHERE id = :id")
    suspend fun markNotified100(id: Long)
}
