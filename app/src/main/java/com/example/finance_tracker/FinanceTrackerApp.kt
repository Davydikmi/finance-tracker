package com.example.finance_tracker

import android.app.Application
import com.example.finance_tracker.core.AppInitializer
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Базовый класс приложения. Обязателен для Hilt:
 * аннотация @HiltAndroidApp генерирует DI-контейнер всего приложения,
 * без неё внедрение зависимостей (@Inject) нигде не работает.
 * Зарегистрирован в AndroidManifest.xml (android:name=".FinanceTrackerApp").
 */
@HiltAndroidApp
class FinanceTrackerApp : Application() {

    @Inject
    lateinit var appInitializer: AppInitializer

    override fun onCreate() {
        super.onCreate()
        // Заполняем БД значениями по умолчанию (категории и т.д.)
        appInitializer.initialize()
    }
}
