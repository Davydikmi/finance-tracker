package com.example.finance_tracker.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * App-wide settings stored as a single row with [id] == [SINGLETON_ID].
 * The row is seeded by the database when it is first created (see
 * DatabaseModule), so [com.example.finance_tracker.data.local.dao.SettingsDao.getSettings]
 * can always assume a row exists.
 */
@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey
    val id: Int = SINGLETON_ID,
    val monthlyIncome: Double = 0.0,
    val hourlyRate: Double = 0.0,
    val baseCurrency: String = "BYN",
    val monthStartDay: Int = 1,
    val themeMode: String = THEME_SYSTEM,
    val notificationsEnabled: Boolean = true,
    val subscriptionRemindersEnabled: Boolean = true,
    val checkTimeHour: Int = 9,
    val exchangeSource: String = "nbrb",
    @ColumnInfo(defaultValue = "ru")
    val language: String = LANG_RU
) {
    companion object {
        const val SINGLETON_ID: Int = 1

        const val THEME_LIGHT: String = "light"
        const val THEME_DARK: String = "dark"
        const val THEME_SYSTEM: String = "system"

        const val LANG_RU: String = "ru"
        const val LANG_EN: String = "en"
    }
}
