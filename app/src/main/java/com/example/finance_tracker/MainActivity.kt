package com.example.finance_tracker

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.finance_tracker.domain.model.AppSettings
import com.example.finance_tracker.ui.MainViewModel
import com.example.finance_tracker.ui.navigation.FinanceApp
import com.example.finance_tracker.ui.theme.FinancetrackerTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * AppCompatActivity (а не ComponentActivity): только AppCompatActivity
 * применяет per-app локали AppCompatDelegate на устройствах ниже Android 13.
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settings by mainViewModel.settings.collectAsStateWithLifecycle()
            val darkTheme = when (settings?.themeMode) {
                AppSettings.THEME_DARK -> true
                AppSettings.THEME_LIGHT -> false
                else -> isSystemInDarkTheme()
            }
            FinancetrackerTheme(darkTheme = darkTheme) {
                FinanceApp()
            }
        }
    }
}
