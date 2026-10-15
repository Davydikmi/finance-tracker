package com.example.finance_tracker.data.repository

import com.example.finance_tracker.data.local.dao.BudgetDao
import com.example.finance_tracker.data.local.dao.CategoryLimitDao
import com.example.finance_tracker.data.local.dao.TransactionDao
import com.example.finance_tracker.data.local.entity.BudgetEntity
import com.example.finance_tracker.data.local.entity.CategoryLimitEntity
import com.example.finance_tracker.data.mapper.toDomain
import com.example.finance_tracker.data.mapper.toEntity
import com.example.finance_tracker.domain.model.Budget
import com.example.finance_tracker.domain.model.CategoryLimit
import com.example.finance_tracker.domain.model.TransactionType
import com.example.finance_tracker.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BudgetRepositoryImpl @Inject constructor(
    private val budgetDao: BudgetDao,
    private val categoryLimitDao: CategoryLimitDao,
    private val transactionDao: TransactionDao
) : BudgetRepository {

    override fun observeMonthlyBudget(monthYear: String): Flow<Budget?> =
        budgetDao.observeByMonth(monthYear).map { it?.toDomain() }

    override fun getDailySpendingLimit(): Flow<Double?> {
        val month = YearMonth.now()
        val zone = ZoneId.systemDefault()
        val start = month.atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val end = month.atEndOfMonth().plusDays(1).atStartOfDay(zone)
            .toInstant().toEpochMilli() - 1
        return combine(
            budgetDao.observeByMonth(month.toString()),
            transactionDao.getByDateRange(start, end)
        ) { budget, transactions ->
            val limit = budget?.takeIf { it.totalLimit > 0.0 } ?: return@combine null
            val spent = transactions
                .filter { it.type == TransactionType.EXPENSE }
                .sumOf { it.amount }
            val daysLeft = month.lengthOfMonth() - LocalDate.now().dayOfMonth + 1
            ((limit.totalLimit - spent) / daysLeft).coerceAtLeast(0.0)
        }
    }

    override suspend fun setMonthlyBudget(monthYear: String, totalLimit: Double) {
        require(totalLimit > 0) { "Лимит бюджета должен быть положительным, получено: $totalLimit" }
        val existing = budgetDao.getByMonth(monthYear)
        budgetDao.insert(
            existing?.copy(totalLimit = totalLimit)
                ?: BudgetEntity(monthYear = monthYear, totalLimit = totalLimit)
        )
    }

    override fun observeCategoryLimits(monthYear: String): Flow<List<CategoryLimit>> =
        categoryLimitDao.getByMonth(monthYear).map { list -> list.map { it.toDomain() } }

    override suspend fun getCategoryLimit(categoryId: Long, monthYear: String): CategoryLimit? =
        categoryLimitDao.getByCategoryAndMonth(categoryId, monthYear)?.toDomain()

    override suspend fun setCategoryLimit(categoryId: Long, monthYear: String, limit: Double) {
        require(limit > 0) { "Лимит категории должен быть положительным, получено: $limit" }
        val existing = categoryLimitDao.getByCategoryAndMonth(categoryId, monthYear)
        categoryLimitDao.insert(
            existing?.copy(limit = limit)
                ?: CategoryLimitEntity(categoryId = categoryId, monthYear = monthYear, limit = limit)
        )
    }

    override suspend fun deleteCategoryLimit(limit: CategoryLimit) =
        categoryLimitDao.delete(limit.toEntity())

    override suspend fun markBudgetNotified80(budgetId: Long) =
        budgetDao.markNotified80(budgetId)

    override suspend fun markBudgetNotified100(budgetId: Long) =
        budgetDao.markNotified100(budgetId)

    override suspend fun markLimitNotified80(limitId: Long) =
        categoryLimitDao.markNotified80(limitId)

    override suspend fun markLimitNotified100(limitId: Long) =
        categoryLimitDao.markNotified100(limitId)
}
