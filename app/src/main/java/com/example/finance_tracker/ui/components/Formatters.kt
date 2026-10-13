package com.example.finance_tracker.ui.components

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Форматирование денег и дат по макетам: "1 250 ₽", "Сегодня, 14:20", "Октябрь 2026".
 * Валюта пока захардкожена (₽) — заменяется на настройку, когда появится SettingsRepository.
 */
object Formatters {

    private val symbols = DecimalFormatSymbols(Locale("ru")).apply {
        groupingSeparator = ' '
        decimalSeparator = ','
    }
    private val moneyFormat = DecimalFormat("#,##0.##").apply {
        decimalFormatSymbols = symbols
    }

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    private val shortDateFormatter = DateTimeFormatter.ofPattern("d MMM", Locale("ru"))
    private val fullDateFormatter = DateTimeFormatter.ofPattern("d MMMM", Locale("ru"))
    private val dayHeaderFormatter = DateTimeFormatter.ofPattern("d MMMM, EEEE", Locale("ru"))
    private val weekdayShortFormatter = DateTimeFormatter.ofPattern("EE", Locale("ru"))
    private val monthShortFormatter = DateTimeFormatter.ofPattern("LLL", Locale("ru"))
    private val monthFormatter = DateTimeFormatter.ofPattern("LLLL yyyy", Locale("ru"))
    private val compactFormat = DecimalFormat("0.#").apply { decimalFormatSymbols = symbols }

    fun money(amount: Double): String = moneyFormat.format(amount) + " ₽"

    fun expense(amount: Double): String = "−${moneyFormat.format(amount)} ₽"

    fun income(amount: Double): String = "+${moneyFormat.format(amount)} ₽"

    fun currentMonthTitle(): String = monthTitle(YearMonth.now())

    /** "Октябрь 2026" — заголовок выбранного месяца. */
    fun monthTitle(month: YearMonth): String =
        monthFormatter.format(month).replaceFirstChar { it.uppercase() }

    /** "12 октября, суббота" — заголовок дня в истории. */
    fun dayHeader(date: LocalDate): String = date.format(dayHeaderFormatter)

    /** "пн" / "вт" — короткий день недели. */
    fun weekdayShort(date: LocalDate): String = date.format(weekdayShortFormatter)

    /** "янв" — короткий месяц. */
    fun monthShort(month: YearMonth): String =
        month.format(monthShortFormatter).replaceFirstChar { it.lowercase() }

    /** Компактные деньги для графики: "42,3 т₽", "1,2 млн ₽". */
    fun compactMoney(amount: Double): String = when {
        amount >= 1_000_000 -> "${compactFormat.format(amount / 1_000_000)} млн ₽"
        amount >= 1_000 -> "${compactFormat.format(amount / 1_000)} т₽"
        else -> money(amount)
    }

    fun time(millis: Long): String =
        Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalTime()
            .format(timeFormatter)

    /** "12 октября" — для выбранной вручную даты. */
    fun fullDate(date: LocalDate): String = date.format(fullDateFormatter)

    /** "Сегодня" / "Вчера" / "12 окт". */
    fun dateLabel(millis: Long): String {
        val date = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
        val today = LocalDate.now()
        return when (date) {
            today -> "Сегодня"
            today.minusDays(1) -> "Вчера"
            else -> date.format(shortDateFormatter)
        }
    }
}
