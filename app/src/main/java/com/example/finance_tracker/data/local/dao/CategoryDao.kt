package com.example.finance_tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.finance_tracker.data.local.entity.CategoryEntity
import com.example.finance_tracker.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow

/**
 * DAO for the "categories" table.
 */
@Dao
interface CategoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: CategoryEntity): Long

    @Update
    suspend fun update(category: CategoryEntity)

    @Delete
    suspend fun delete(category: CategoryEntity)

    @Query("SELECT * FROM categories ORDER BY sortOrder ASC, name ASC")
    fun getAll(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE type = :type ORDER BY sortOrder ASC")
    fun getByType(type: TransactionType): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getById(id: Long): CategoryEntity?

    /** Number of transactions still pointing at this category, used to warn before deletion. */
    @Query("SELECT COUNT(*) FROM transactions WHERE categoryId = :categoryId")
    suspend fun countTransactionsInCategory(categoryId: Long): Int

    /** Общее число категорий — используется AppInitializer'ом при первичном заполнении БД. */
    @Query("SELECT COUNT(*) FROM categories")
    suspend fun getCount(): Int
}
