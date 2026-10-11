package com.example.finance_tracker.di

import com.example.finance_tracker.data.repository.BudgetRepositoryImpl
import com.example.finance_tracker.data.repository.SavingsJarRepositoryImpl
import com.example.finance_tracker.data.repository.TransactionRepositoryImpl
import com.example.finance_tracker.domain.repository.BudgetRepository
import com.example.finance_tracker.domain.repository.SavingsJarRepository
import com.example.finance_tracker.domain.repository.TransactionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Привязка реализаций репозиториев к их доменным интерфейсам.
 * @Binds (а не @Provides) — реализации сами создаются через @Inject constructor.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindTransactionRepository(impl: TransactionRepositoryImpl): TransactionRepository

    @Binds
    @Singleton
    abstract fun bindSavingsJarRepository(impl: SavingsJarRepositoryImpl): SavingsJarRepository

    @Binds
    @Singleton
    abstract fun bindBudgetRepository(impl: BudgetRepositoryImpl): BudgetRepository
}
