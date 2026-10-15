package com.example.finance_tracker.presentation

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.example.finance_tracker.MainActivity
import com.example.finance_tracker.core.BankNotificationParser

/** Предзаполнение экрана операции из внешнего «Поделиться». */
data class PrefillData(
    val amount: Double? = null,
    val dateMillis: Long? = null,
    val keyword: String? = null
)

fun Intent.toPrefillData(): PrefillData? {
    val has = hasExtra(ShareReceiverActivity.EXTRA_AMOUNT) ||
        hasExtra(ShareReceiverActivity.EXTRA_DATE) ||
        hasExtra(ShareReceiverActivity.EXTRA_KEYWORD)
    if (!has) return null
    return PrefillData(
        amount = getDoubleExtra(ShareReceiverActivity.EXTRA_AMOUNT, Double.NaN)
            .takeIf { !it.isNaN() },
        dateMillis = getLongExtra(ShareReceiverActivity.EXTRA_DATE, -1L).takeIf { it > 0 },
        keyword = getStringExtra(ShareReceiverActivity.EXTRA_KEYWORD)
    )
}

/**
 * Приёмник «Поделиться» из банковских приложений (ACTION_SEND, text/plain).
 * Парсит текст уведомления и открывает экран добавления операции
 * с предзаполненными суммой, датой и подсказкой категории.
 */
class ShareReceiverActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val text = intent?.getStringExtra(Intent.EXTRA_TEXT).orEmpty()
        val parsed = BankNotificationParser.parse(text)

        val openApp = Intent(this, MainActivity::class.java).apply {
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
            )
            parsed.amount?.let { putExtra(EXTRA_AMOUNT, it) }
            parsed.dateMillis?.let { putExtra(EXTRA_DATE, it) }
            parsed.categoryHint?.let { putExtra(EXTRA_KEYWORD, it) }
        }
        startActivity(openApp)
        finish()
    }

    companion object {
        const val EXTRA_AMOUNT = "prefill_amount"
        const val EXTRA_DATE = "prefill_date"
        const val EXTRA_KEYWORD = "prefill_keyword"
    }
}
