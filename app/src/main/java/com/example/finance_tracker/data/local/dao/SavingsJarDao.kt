package com.example.finance_tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.finance_tracker.data.local.entity.SavingsJarEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for the "savings_jars" table.
 */
@Dao
interface SavingsJarDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(jar: SavingsJarEntity): Long

    @Update
    suspend fun update(jar: SavingsJarEntity)

    @Delete
    suspend fun delete(jar: SavingsJarEntity)

    @Query("SELECT * FROM savings_jars WHERE isArchived = 0 ORDER BY createdAt DESC")
    fun getActive(): Flow<List<SavingsJarEntity>>

    @Query("SELECT * FROM savings_jars WHERE id = :id")
    suspend fun getById(id: Long): SavingsJarEntity?

    /** Overwrites the jar balance with [amount] without touching other columns. */
    @Query("UPDATE savings_jars SET savedAmount = :amount WHERE id = :id")
    suspend fun updateSavedAmount(id: Long, amount: Double)
}
