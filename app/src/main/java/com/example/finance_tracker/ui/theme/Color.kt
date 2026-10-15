package com.example.finance_tracker.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * M3-схемы поверх токенов из Palette.kt: нейтральные, под макет
 * (белый фон, чёрный акцент, серые карточки). Полноценные темы
 * (light/dark/system из настроек) появятся вместе со SettingsRepository.
 */
val LightColorScheme = lightColorScheme(
    primary = InkPrimary,
    onPrimary = Color.White,
    background = ScreenBg,
    onBackground = InkPrimary,
    surface = Color.White,
    onSurface = InkPrimary,
    surfaceVariant = CardBg,
    onSurfaceVariant = InkSecondary,
    error = ExpenseRed,
    tertiary = IncomeGreen,
    outlineVariant = DividerColor
)

val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFF2F2F4),
    onPrimary = Color(0xFF1F1F23),
    background = Color(0xFF161618),
    onBackground = Color(0xFFF2F2F4),
    surface = Color(0xFF1F1F23),
    onSurface = Color(0xFFF2F2F4),
    surfaceVariant = Color(0xFF2A2A2E),
    onSurfaceVariant = Color(0xFF9E9EA6),
    error = ExpenseRed,
    tertiary = IncomeGreen
)
