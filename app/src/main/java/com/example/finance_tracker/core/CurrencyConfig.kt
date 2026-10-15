package com.example.finance_tracker.core

/**
 * Валюта приложения: код из настроек → символ и курс для отображения.
 * Суммы в БД всегда хранятся в BYN; [ratePerUnit] — сколько BYN стоит
 * 1 единица выбранной валюты (по курсу НБ РБ, из ExchangeRateRepository).
 * Отображаемая сумма = хранимая / [ratePerUnit]: 3.5 Br / 3.5 = 1 $.
 * Обновляется AppInitializer'ом при изменении настроек и после загрузки курсов.
 */
object CurrencyConfig {

    /** Символ текущей базовой валюты (по умолчанию — белорусский рубль). */
    @Volatile
    var symbol: String = symbolFor(AppDefaultCurrency)

    /**
     * Курс «BYN за 1 единицу отображаемой валюты». 1.0 — пока курсы
     * не загружены или валюта BYN (без конвертации).
     */
    @Volatile
    var ratePerUnit: Double = 1.0

    const val AppDefaultCurrency: String = "BYN"

    /** Поддерживаемые валюты для выбора в настройках. */
    val supported: List<String> = listOf("BYN", "RUB", "USD", "EUR", "PLN", "UAH")

    fun symbolFor(code: String): String = when (code) {
        "BYN" -> "Br"
        "RUB" -> "₽"
        "USD" -> "$"
        "EUR" -> "€"
        "PLN" -> "zł"
        "UAH" -> "₴"
        else -> code
    }

    /** Человекочитаемое название для диалога выбора. */
    fun displayName(code: String): String = when (code) {
        "BYN" -> "Белорусский рубль"
        "RUB" -> "Российский рубль"
        "USD" -> "Доллар США"
        "EUR" -> "Евро"
        "PLN" -> "Польский злотый"
        "UAH" -> "Украинская гривна"
        else -> code
    }
}
