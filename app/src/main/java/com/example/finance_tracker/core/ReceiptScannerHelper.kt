package com.example.finance_tracker.core

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Распознавание чеков: ML Kit Text Recognition (локально, офлайн)
 * + полуавтоматический парсинг суммы и даты. Пользователь подтверждает
 * результат перед сохранением.
 */
@Singleton
class ReceiptScannerHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val recognizer by lazy {
        // Латинская модель ML Kit v2: цифры (суммы, даты) распознаёт надёжно;
        // кириллические названия позиций могут пропасть — парсингу они не нужны.
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    suspend fun scan(uri: Uri): ReceiptScan {
        val image = InputImage.fromFilePath(context, uri)
        val text = recognizer.process(image).await().text
        return ReceiptScan(amount = parseAmount(text), date = parseDate(text), text = text)
    }

    /**
     * Сумма: приоритет строкам с «итог/сумма/всего/total», иначе —
     * максимальное число с копейками на чеке.
     */
    private fun parseAmount(text: String): Double? {
        val lines = text.lines()
        val keywordLine = lines.firstOrNull { line ->
            KEYWORDS.any { keyword -> line.lowercase().contains(keyword) }
        }
        val fromKeyword = keywordLine?.let { firstNumber(it) }
        if (fromKeyword != null) return fromKeyword
        return lines.mapNotNull { firstNumber(it) }.maxOrNull()
    }

    private fun firstNumber(line: String): Double? =
        NUMBER_REGEX.findAll(line)
            .map { normalizeNumber(it.value) }
            .firstOrNull { it != null && it > 0.0 }

    private fun normalizeNumber(raw: String): Double? =
        raw.replace(" ", "").replace("\u00A0", "").replace(',', '.').toDoubleOrNull()

    private fun parseDate(text: String): LocalDate? {
        val match = DATE_REGEX.find(text) ?: return null
        val day = match.groupValues[1].toInt()
        val month = match.groupValues[2].toInt()
        var year = match.groupValues[3].toInt()
        if (year < 100) year += 2000
        return try {
            LocalDate.of(year, month, day)
        } catch (e: Exception) {
            null
        }
    }

    private companion object {
        val NUMBER_REGEX = Regex("\\d+(?:[ \u00A0]\\d{3})*(?:[.,]\\d{1,2})?")
        val DATE_REGEX = Regex("(\\d{1,2})[./-](\\d{1,2})[./-](\\d{2,4})")
        val KEYWORDS = listOf("итог", "сумм", "всего", "total", "оплат", "к оплате")
    }
}

/** Результат сканирования: текст чека + найденные кандидаты полей. */
data class ReceiptScan(
    val amount: Double?,
    val date: LocalDate?,
    val text: String
)
