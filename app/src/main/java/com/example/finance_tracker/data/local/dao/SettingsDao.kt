package com.example.finance_tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.finance_tracker.data.local.entity.SettingsEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for the "settings" table, which always holds exactly one row
 * ([SettingsEntity.SINGLETON_ID]). The row is seeded when the database is
 * first created, so [getSettings] can assume it exists.
 */
@Dao
interface SettingsDao {

    @Query("SELECT * FROM settings WHERE id = 1")
    fun getSettings(): Flow<SettingsEntity>

    @Query("SELECT * FROM settings WHERE id = 1")
    suspend fun getSettingsOnce(): SettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(settings: SettingsEntity)

    @Update
    suspend fun update(settings: SettingsEntity)
}
