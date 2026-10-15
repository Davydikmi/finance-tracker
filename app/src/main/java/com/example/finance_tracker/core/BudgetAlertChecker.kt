package com.example.finance_tracker.core

import com.example.finance_tracker.data.local.dao.BudgetDao
import com.example.finance_tracker.data.local.dao.TransactionDao
import com.example.finance_tracker.domain.model.TransactionType
import com.example.finance_tracker.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.first
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

/**
 * Проверка порогов бюджета: 80% и 100%. Вызывается после каждой вставки
 * транзакции и из BudgetCheckWorker. Флаги notified80/100 в строке бюджета
 * не дают показать одно и то же уведомление дважды; новый месяц — новая
 * строка бюджета с флагами false, т.е. «сброс» происходит автоматически.
 */
@Singleton
class BudgetAlertChecker @Inject constructor(
    private val budgetDao: BudgetDao,
    private val transactionDao: TransactionDao,
    private val notificationHelper: NotificationHelper,
    private val settingsRepository: SettingsRepository
) {

    suspend fun checkAndNotify() {
        if (!settingsRepository.observeSettings().first().notificationsEnabled) return

        val month = YearMonth.now()
        val zone = ZoneId.systemDefault()
        val start = month.atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val end = month.atEndOfMonth().plusDays(1).atStartOfDay(zone)
            .toInstant().toEpochMilli() - 1

        val budget = budgetDao.getByMonth(month.toString()) ?: return
        if (budget.totalLimit <= 0.0) return

        val spent = transactionDao.getTotalExpenses(start, end) ?: 0.0
        val percent = (spent / budget.totalLimit * 100).roundToInt()

        when {
            percent >= 100 && !budget.notified100 -> {
                notificationHelper.showBudgetAlert(100, spent, budget.totalLimit)
                budgetDao.markNotified100(budget.id)
            }

            percent >= 80 && !budget.notified80 -> {
                notificationHelper.showBudgetAlert(80, spent, budget.totalLimit)
                budgetDao.markNotified80(budget.id)
            }
        }
    }
}
