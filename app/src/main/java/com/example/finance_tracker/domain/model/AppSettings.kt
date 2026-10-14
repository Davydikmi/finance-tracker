package com.example.finance_tracker.domain.model

/**
 * Настройки приложения (подмножество, используемое UI).
 * themeMode: [THEME_LIGHT] / [THEME_DARK] / [THEME_SYSTEM];
 * language: [LANG_RU] / [LANG_EN]; baseCurrency — ISO-код (BYN, RUB, …).
 */
data class AppSettings(
    val themeMode: String = THEME_SYSTEM,
    val baseCurrency: String = DEFAULT_CURRENCY,
    val language: String = LANG_RU
) {
    companion object {
        const val THEME_LIGHT = "light"
        const val THEME_DARK = "dark"
        const val THEME_SYSTEM = "system"

        const val LANG_RU = "ru"
        const val LANG_EN = "en"

        const val DEFAULT_CURRENCY = "BYN"
    }
}
