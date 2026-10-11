package com.example.finance_tracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.finance_tracker.data.local.dao.BudgetDao
import com.example.finance_tracker.data.local.dao.CategoryDao
import com.example.finance_tracker.data.local.dao.CategoryLimitDao
import com.example.finance_tracker.data.local.dao.ExchangeRateDao
import com.example.finance_tracker.data.local.dao.JarMovementDao
import com.example.finance_tracker.data.local.dao.SavingsJarDao
import com.example.finance_tracker.data.local.dao.SettingsDao
import com.example.finance_tracker.data.local.dao.TransactionDao
import com.example.finance_tracker.data.local.entity.BudgetEntity
import com.example.finance_tracker.data.local.entity.CategoryEntity
import com.example.finance_tracker.data.local.entity.CategoryLimitEntity
import com.example.finance_tracker.data.local.entity.ExchangeRateEntity
import com.example.finance_tracker.data.local.entity.JarMovementEntity
import com.example.finance_tracker.data.local.entity.SavingsJarEntity
import com.example.finance_tracker.data.local.entity.SettingsEntity
import com.example.finance_tracker.data.local.entity.TransactionEntity

/**
 * Room database of the finance tracker.
 *
 * Schemas are exported (see the room.schemaLocation KSP argument in
 * app/build.gradle.kts) so future versions can be migrated with tested
 * Migration objects instead of destructive downgrade/recreate.
 */
@Database(
    entities = [
        TransactionEntity::class,
        CategoryEntity::class,
        SavingsJarEntity::class,
        JarMovementEntity::class,
        BudgetEntity::class,
        CategoryLimitEntity::class,
        ExchangeRateEntity::class,
        SettingsEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao

    abstract fun categoryDao(): CategoryDao

    abstract fun savingsJarDao(): SavingsJarDao

    abstract fun jarMovementDao(): JarMovementDao

    abstract fun budgetDao(): BudgetDao

    abstract fun categoryLimitDao(): CategoryLimitDao

    abstract fun exchangeRateDao(): ExchangeRateDao

    abstract fun settingsDao(): SettingsDao

    companion object {
        const val DATABASE_NAME = "finance_tracker.db"
    }
}
