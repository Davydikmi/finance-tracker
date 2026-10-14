package com.example.finance_tracker.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.finance_tracker.data.local.AppDatabase
import com.example.finance_tracker.data.local.dao.BudgetDao
import com.example.finance_tracker.data.local.dao.CategoryDao
import com.example.finance_tracker.data.local.dao.CategoryLimitDao
import com.example.finance_tracker.data.local.dao.ExchangeRateDao
import com.example.finance_tracker.data.local.dao.JarMovementDao
import com.example.finance_tracker.data.local.dao.SavingsJarDao
import com.example.finance_tracker.data.local.dao.SettingsDao
import com.example.finance_tracker.data.local.dao.TransactionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module providing the Room [AppDatabase] and its DAOs.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * Seeds the singleton settings row (id = 1) right after the database file
     * is created, so SettingsDao.getSettings() always has a row to emit.
     * The values mirror the SettingsEntity field defaults.
     */
    private val seedSettingsCallback = object : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            db.execSQL(
                """
                INSERT INTO settings (
                    id,
                    monthlyIncome,
                    hourlyRate,
                    baseCurrency,
                    monthStartDay,
                    themeMode,
                    notificationsEnabled,
                    subscriptionRemindersEnabled,
                    checkTimeHour,
                    exchangeSource,
                    language
                ) VALUES (
                    1, 0.0, 0.0, 'BYN', 1, 'system', 1, 1, 9, 'nbrb', 'ru'
                )
                """.trimIndent()
            )
        }
    }

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
            .addCallback(seedSettingsCallback)
            .addMigrations(AppDatabase.MIGRATION_1_2)
            .build()

    @Provides
    fun provideTransactionDao(database: AppDatabase): TransactionDao =
        database.transactionDao()

    @Provides
    fun provideCategoryDao(database: AppDatabase): CategoryDao =
        database.categoryDao()

    @Provides
    fun provideSavingsJarDao(database: AppDatabase): SavingsJarDao =
        database.savingsJarDao()

    @Provides
    fun provideJarMovementDao(database: AppDatabase): JarMovementDao =
        database.jarMovementDao()

    @Provides
    fun provideBudgetDao(database: AppDatabase): BudgetDao =
        database.budgetDao()

    @Provides
    fun provideCategoryLimitDao(database: AppDatabase): CategoryLimitDao =
        database.categoryLimitDao()

    @Provides
    fun provideExchangeRateDao(database: AppDatabase): ExchangeRateDao =
        database.exchangeRateDao()

    @Provides
    fun provideSettingsDao(database: AppDatabase): SettingsDao =
        database.settingsDao()
}
