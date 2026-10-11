package com.example.finance_tracker

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Базовый класс приложения. Обязателен для Hilt:
 * аннотация @HiltAndroidApp генерирует DI-контейнер всего приложения,
 * без неё внедрение зависимостей (@Inject) нигде не работает.
 * Зарегистрирован в AndroidManifest.xml (android:name=".FinanceTrackerApp").
 */
@HiltAndroidApp
class FinanceTrackerApp : Application()
