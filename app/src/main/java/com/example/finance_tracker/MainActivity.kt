package com.example.finance_tracker

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.finance_tracker.domain.model.AppSettings
import com.example.finance_tracker.presentation.PrefillData
import com.example.finance_tracker.presentation.toPrefillData
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

    /** Предзаполнение из share-intента, ждёт старта навигации. */
    private var pendingPrefill: PrefillData? = null

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pendingPrefill = intent?.toPrefillData()
        requestNotificationPermissionIfNeeded()
        enableEdgeToEdge()
        setContent {
            val settings by mainViewModel.settings.collectAsStateWithLifecycle()
            val darkTheme = when (settings?.themeMode) {
                AppSettings.THEME_DARK -> true
                AppSettings.THEME_LIGHT -> false
                else -> isSystemInDarkTheme()
            }
            FinancetrackerTheme(darkTheme = darkTheme) {
                FinanceApp(consumePendingPrefill = ::takePendingPrefill)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        pendingPrefill = intent.toPrefillData()
    }

    private fun takePendingPrefill(): PrefillData? {
        val data = pendingPrefill
        pendingPrefill = null
        return data
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
