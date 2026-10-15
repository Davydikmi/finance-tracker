package com.example.finance_tracker.data.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.finance_tracker.core.BudgetAlertChecker
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Ежедневная проверка бюджета в 9:00 (см. FinanceTrackerApp.scheduleWorkers).
 * Дублирует проверку после вставки транзакции — на случай, если порог
 * достигнут, а приложение не открывали.
 */
@HiltWorker
class BudgetCheckWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val budgetAlertChecker: BudgetAlertChecker
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        budgetAlertChecker.checkAndNotify()
        return Result.success()
    }
}
