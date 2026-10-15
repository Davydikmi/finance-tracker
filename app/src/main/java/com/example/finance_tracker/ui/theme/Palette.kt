package com.example.finance_tracker.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

/**
 * Дизайн-токены приложения. Изменяемые (MutableState), чтобы тема
 * light/dark переключалась на лету: значения присваиваются в
 * FinancetrackerTheme, все экраны читают их напрямую.
 */
var ScreenBg by mutableStateOf(Color(0xFFFAFAFA))
    private set
var CardBg by mutableStateOf(Color(0xFFF2F2F2))
    private set
var CardBgLight by mutableStateOf(Color(0xFFF7F7F7))
    private set

/** Поверхность контраста: белая карточка/поле в светлой теме, приподнятая в тёмной. */
var CardSolid by mutableStateOf(Color.White)
    private set

/** Основной текст/акцент: почти чёрный в светлой теме, почти белый в тёмной. */
var InkPrimary by mutableStateOf(Color(0xFF1A1A1A))
    private set
var InkSecondary by mutableStateOf(Color(0xFF9E9E9E))
    private set

/** Текст/иконка поверх тёмного акцента (FAB, активные пилюли, кнопки). */
var OnAccent by mutableStateOf(Color.White)
    private set

var AccentBlue by mutableStateOf(Color(0xFF2F80ED))
    private set
var ExpenseRed by mutableStateOf(Color(0xFFE5484D))
    private set
var IncomeGreen by mutableStateOf(Color(0xFF30A46C))
    private set
var FabDark by mutableStateOf(Color(0xFF262626))
    private set
var DividerColor by mutableStateOf(Color.Black.copy(alpha = 0.06f))
    private set

internal fun applyPalette(darkTheme: Boolean) {
    if (darkTheme) {
        ScreenBg = Color(0xFF161618)
        CardBg = Color(0xFF232327)
        CardBgLight = Color(0xFF232327)
        CardSolid = Color(0xFF2A2A2E)
        InkPrimary = Color(0xFFF2F2F4)
        InkSecondary = Color(0xFF9E9EA6)
        OnAccent = Color(0xFF1F1F23)
        AccentBlue = Color(0xFF5C9DFF)
        ExpenseRed = Color(0xFFFF6369)
        IncomeGreen = Color(0xFF3DD68C)
        FabDark = Color(0xFF3A3A3E)
        DividerColor = Color.White.copy(alpha = 0.08f)
    } else {
        ScreenBg = Color(0xFFFAFAFA)
        CardBg = Color(0xFFF2F2F2)
        CardBgLight = Color(0xFFF7F7F7)
        CardSolid = Color.White
        InkPrimary = Color(0xFF1A1A1A)
        InkSecondary = Color(0xFF9E9E9E)
        OnAccent = Color.White
        AccentBlue = Color(0xFF2F80ED)
        ExpenseRed = Color(0xFFE5484D)
        IncomeGreen = Color(0xFF30A46C)
        FabDark = Color(0xFF262626)
        DividerColor = Color.Black.copy(alpha = 0.06f)
    }
}
