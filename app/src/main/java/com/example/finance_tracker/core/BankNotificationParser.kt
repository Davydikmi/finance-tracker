package com.example.finance_tracker.core

/**
 * Парсинг текста банковских push/SMS-уведомлений (Альфа, Беларусбанк,
 * «Оплати» и др.): сумма, дата, подсказка категории по ключевым словам.
 * Форматы банков различаются, поэтому парсинг «мягкий»: что не нашли —
 * пользователь введёт сам.
 */
object BankNotificationParser {

    data class Parsed(
        val amount: Double?,
        val dateMillis: Long?,
        /** Корень названия категории из дефолтного справочника ("еда", "транспорт"…). */
        val categoryHint: String?
    )

    private val NUMBER_REGEX =
        Regex("\\d{1,3}(?:[ \u00A0]\\d{3})+(?:[.,]\\d{2})?|\\d+[.,]\\d{2}|\\d+")
    private val DATE_REGEX = Regex("(\\d{1,2})[./](\\d{1,2})[./](\\d{2,4})")

    /** Ключевые слова из текста → корень названия категории. */
    private val CATEGORY_KEYWORDS: List<Pair<String, String>> = listOf(
        "супермаркет" to "еда",
        "продукт" to "еда",
        "кафе" to "еда",
        "ресторан" to "еда",
        "кофе" to "еда",
        "пицц" to "еда",
        "транспорт" to "транспорт",
        "такси" to "транспорт",
        "метро" to "транспорт",
        "автобус" to "транспорт",
        "бензин" to "транспорт",
        "азс" to "транспорт",
        "заправк" to "транспорт",
        "аптек" to "здоровье",
        "клиник" to "здоровье",
        "врач" to "здоровье",
        "медиц" to "здоровье",
        "магазин" to "покупки",
        "покупк" to "покупки",
        "кино" to "развлечения",
        "театр" to "развлечения",
        "развлеч" to "развлечения",
        "связь" to "связь",
        "интернет" to "связь",
        "телеком" to "связь",
        "аренд" to "жильё",
        "коммунал" to "жильё",
        "жкх" to "жильё",
        "квартплат" to "жильё"
    )

    fun parse(text: String): Parsed {
        val lower = text.lowercase()
        return Parsed(
            amount = parseAmount(lower),
            dateMillis = parseDate(text),
            categoryHint = CATEGORY_KEYWORDS
                .firstOrNull { (keyword, _) -> lower.contains(keyword) }
                ?.second
        )
    }

    /**
     * Сумма: приоритет строкам с «сумма/итого/спишет/оплат…»,
     * иначе — максимальное число в тексте (обычно это сумма операции).
     */
    private fun parseAmount(lowerText: String): Double? {
        val keywordLine = lowerText.lines().firstOrNull { line ->
            listOf("сумм", "итог", "спишет", "списание", "оплат", "покупк", "total")
                .any { line.contains(it) }
        }
        val numbers = NUMBER_REGEX.findAll(keywordLine ?: lowerText)
            .mapNotNull { normalize(it.value) }
            .filter { it > 0.0 }
            .toList()
        return numbers.maxOrNull()
    }

    private fun normalize(raw: String): Double? =
        raw.replace(" ", "").replace("\u00A0", "").replace(',', '.').toDoubleOrNull()

    private fun parseDate(text: String): Long? {
        val match = DATE_REGEX.find(text) ?: return null
        val day = match.groupValues[1].toIntOrNull() ?: return null
        val month = match.groupValues[2].toIntOrNull() ?: return null
        var year = match.groupValues[3].toIntOrNull() ?: return null
        if (year < 100) year += 2000
        return try {
            java.time.LocalDate.of(year, month, day)
                .atStartOfDay(java.time.ZoneId.systemDefault())
                .toInstant().toEpochMilli()
        } catch (e: Exception) {
            null
        }
    }
}
