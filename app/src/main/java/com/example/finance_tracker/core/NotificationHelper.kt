package com.example.finance_tracker.core

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.finance_tracker.MainActivity
import com.example.finance_tracker.R
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Каналы и показ локальных уведомлений (алерты бюджета).
 * Вызов [ensureChannels] — в Application.onCreate().
 */
@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun ensureChannels() {
        val channel = android.app.NotificationChannel(
            CHANNEL_BUDGET,
            "Оповещения о бюджете",
            android.app.NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Предупреждения при достижении 80% и 100% месячного бюджета"
        }
        context.getSystemService(android.app.NotificationManager::class.java)
            .createNotificationChannel(channel)
    }

    /**
     * Алерт о достижении порога бюджета. Молча выходит, если разрешение
     * POST_NOTIFICATIONS не выдано (Android 13+).
     */
    fun showBudgetAlert(percent: Int, spent: Double, limit: Double) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU &&
            androidx.core.content.ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.POST_NOTIFICATIONS
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        val moneyFormat = DecimalFormat("#,##0.##", DecimalFormatSymbols(Locale("ru")))
        val openApp = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_BUDGET)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Бюджет: израсходовано $percent%")
            .setContentText(
                "Потрачено ${moneyFormat.format(spent)} из ${moneyFormat.format(limit)} " +
                    CurrencyConfig.symbol
            )
            .setAutoCancel(true)
            .setContentIntent(openApp)
            .build()
        // id = percent: 80% и 100% — разные уведомления.
        NotificationManagerCompat.from(context).notify(percent, notification)
    }

    companion object {
        const val CHANNEL_BUDGET = "budget_alerts"
    }
}
