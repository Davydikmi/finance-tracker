package com.example.finance_tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.finance_tracker.data.local.entity.JarMovementEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for the "jar_movements" table (deposits/withdrawals of savings jars).
 */
@Dao
interface JarMovementDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(movement: JarMovementEntity): Long

    @Query("SELECT * FROM jar_movements WHERE jarId = :jarId ORDER BY date DESC")
    fun getByJarId(jarId: Long): Flow<List<JarMovementEntity>>
}
