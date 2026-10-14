package com.example.finance_tracker.core

/**
 * Валюта приложения: код из настроек → символ для отображения.
 * Держится в памяти и обновляется AppInitializer'ом при изменении настроек;
 * [symbol] читается форматтерами при каждой отрисовке.
 *
 * Конвертация сумм по курсу НБ РБ — отложенная функция (запланировано
 * вместе со слоем курсов валют); сейчас выбор валюты меняет только символ.
 */
object CurrencyConfig {

    /** Символ текущей базовой валюты (по умолчанию — белорусский рубль). */
    @Volatile
    var symbol: String = symbolFor(AppDefaultCurrency)

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
