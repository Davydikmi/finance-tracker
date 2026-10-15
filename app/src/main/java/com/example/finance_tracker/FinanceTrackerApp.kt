package com.example.finance_tracker

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.finance_tracker.core.AppInitializer
import com.example.finance_tracker.core.NotificationHelper
import com.example.finance_tracker.data.work.BudgetCheckWorker
import com.example.finance_tracker.data.work.ExchangeRateWorker
import dagger.hilt.android.HiltAndroidApp
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Базовый класс приложения. @HiltAndroidApp генерирует DI-контейнер;
 * Configuration.Provider подключает HiltWorkerFactory для @HiltWorker.
 */
@HiltAndroidApp
class FinanceTrackerApp : Application(), Configuration.Provider {

    @Inject
    lateinit var appInitializer: AppInitializer

    @Inject
    lateinit var notificationHelper: NotificationHelper

    @Inject
    lateinit var hiltWorkerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(hiltWorkerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        // Заполняем БД значениями по умолчанию (категории, настройки, курсы).
        appInitializer.initialize()
        notificationHelper.ensureChannels()
        scheduleWorkers()
    }

    /**
     * Две фоновые задачи, ежедневно в 9:00 (KEEP — расписание не дублируется).
     */
    private fun scheduleWorkers() {
        val workManager = WorkManager.getInstance(this)
        val initialDelay = Duration.between(LocalDateTime.now(), nextNineAm())

        workManager.enqueueUniquePeriodicWork(
            "budget_check",
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<BudgetCheckWorker>(24, TimeUnit.HOURS)
                .setInitialDelay(initialDelay.toMillis(), TimeUnit.MILLISECONDS)
                .build()
        )
        workManager.enqueueUniquePeriodicWork(
            "exchange_rates",
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<ExchangeRateWorker>(24, TimeUnit.HOURS)
                .setInitialDelay(initialDelay.toMillis(), TimeUnit.MILLISECONDS)
                .build()
        )
    }

    private fun nextNineAm(): LocalDateTime {
        val now = LocalDateTime.now()
        var next = now.toLocalDate().atTime(9, 0)
        if (!next.isAfter(now)) {
            next = next.plusDays(1)
        }
        return next
    }
}
