package com.example.finance_tracker.data.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.finance_tracker.domain.repository.ExchangeRateRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Ежедневное обновление курсов НБ РБ (в 9:00). Ошибка сети — retry
 * с экспоненциальной задержкой; успех — следующий запуск завтра.
 */
@HiltWorker
class ExchangeRateWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val exchangeRateRepository: ExchangeRateRepository
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result =
        if (exchangeRateRepository.refreshRates()) {
            Result.success()
        } else {
            Result.retry()
        }
}
