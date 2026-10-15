package com.example.finance_tracker.core.export

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.finance_tracker.core.CurrencyConfig
import com.example.finance_tracker.domain.model.TransactionDetail
import com.example.finance_tracker.domain.model.TransactionType
import com.example.finance_tracker.domain.repository.TransactionRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Экспорт операций за месяц в PDF (A4): итоги периода и таблица операций
 * с разбивкой по страницам. Файл кладётся в cache/export и отдаётся
 * наружу через FileProvider.
 */
@Singleton
class TransactionPdfExporter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val transactionRepository: TransactionRepository
) {

    suspend fun exportMonth(month: YearMonth): File = withContext(Dispatchers.IO) {
        val zone = ZoneId.systemDefault()
        val start = month.atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val end = month.atEndOfMonth().plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1
        val transactions = transactionRepository.getTransactionsByDateRange(start, end).first()

        val currency = " " + CurrencyConfig.symbol
        val title = monthTitleFormatter.format(month).replaceFirstChar { it.uppercase() }

        val titlePaint = paint(size = 18f, bold = true)
        val totalsPaint = paint(size = 12f, bold = true)
        val headerPaint = paint(size = 11f, bold = true, color = Color.GRAY)
        val textPaint = paint(size = 11f)
        val amountPaint = paint(size = 11f, bold = true)

        val pdf = PdfDocument()
        var pageNumber = 1
        var page = pdf.startPage(pageInfo(pageNumber))
        var canvas = page.canvas
        var y = MARGIN + 18f

        fun finishPage() {
            canvas.drawText(
                "— $pageNumber —",
                PAGE_WIDTH / 2f,
                PAGE_HEIGHT - MARGIN / 2f,
                headerPaint
            )
            pdf.finishPage(page)
        }

        fun nextPage() {
            finishPage()
            pageNumber++
            page = pdf.startPage(pageInfo(pageNumber))
            canvas = page.canvas
            y = MARGIN
        }

        // Шапка отчёта (только первая страница). Суммы в отчёте — в валюте
        // приложения (конвертация из BYN по курсу НБ РБ).
        canvas.drawText("Финансовый трекер — отчёт за $title", MARGIN, y, titlePaint)
        y += 26f
        val expenses = transactions
            .filter { it.type == TransactionType.EXPENSE }
            .sumOf { it.amount }
        val income = transactions
            .filter { it.type == TransactionType.INCOME }
            .sumOf { it.amount }
        canvas.drawText("Расходы: −${money(expenses)}$currency", MARGIN, y, totalsPaint)
        y += LINE_HEIGHT
        canvas.drawText("Доходы: +${money(income)}$currency", MARGIN, y, totalsPaint)
        y += LINE_HEIGHT
        canvas.drawText("Баланс: ${money(income - expenses)}$currency", MARGIN, y, totalsPaint)
        y += LINE_HEIGHT * 1.5f

        // Заголовок таблицы
        canvas.drawText("Дата", MARGIN, y, headerPaint)
        canvas.drawText("Описание", MARGIN + 55f, y, headerPaint)
        canvas.drawText("Категория", MARGIN + 260f, y, headerPaint)
        val sumHeader = "Сумма"
        canvas.drawText(sumHeader, PAGE_WIDTH - MARGIN - headerPaint.measureText(sumHeader), y, headerPaint)
        y += LINE_HEIGHT

        if (transactions.isEmpty()) {
            canvas.drawText("Операций за период нет", MARGIN, y, textPaint)
        }

        transactions.forEach { transaction ->
            if (y > PAGE_HEIGHT - MARGIN * 2f) nextPage()
            val date = Instant.ofEpochMilli(transaction.date).atZone(zone).toLocalDate()
            canvas.drawText(date.format(rowDateFormatter), MARGIN, y, textPaint)
            canvas.drawText(
                ellipsize(transaction.description ?: "—", textPaint, 190f),
                MARGIN + 55f,
                y,
                textPaint
            )
            canvas.drawText(
                ellipsize(transaction.categoryName ?: "Без категории", textPaint, 130f),
                MARGIN + 260f,
                y,
                textPaint
            )
            val sign = if (transaction.type == TransactionType.EXPENSE) "−" else "+"
            val amountText = "$sign${money(transaction.amount)}$currency"
            canvas.drawText(
                amountText,
                PAGE_WIDTH - MARGIN - amountPaint.measureText(amountText),
                y,
                amountPaint
            )
            y += LINE_HEIGHT
        }
        finishPage()

        val dir = File(context.cacheDir, "export").apply { mkdirs() }
        val file = File(dir, "finance_report_${month.year}-${"%02d".format(month.monthValue)}.pdf")
        file.outputStream().use { pdf.writeTo(it) }
        pdf.close()
        file
    }

    private fun pageInfo(number: Int) =
        PdfDocument.PageInfo.Builder(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), number).create()

    private fun paint(size: Float, bold: Boolean = false, color: Int = Color.BLACK) =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = size
            this.color = color
            typeface = if (bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
        }

    /** Обрезает текст с «…», чтобы влезал в заданную ширину. */
    private fun ellipsize(text: String, paint: Paint, maxWidth: Float): String {
        if (paint.measureText(text) <= maxWidth) return text
        var result = text
        while (result.isNotEmpty() && paint.measureText("$result…") > maxWidth) {
            result = result.dropLast(1)
        }
        return "$result…"
    }

    /** Хранимая сумма (BYN) → форматированная сумма в валюте приложения. */
    private fun money(amount: Double): String =
        moneyFormat.format(amount / CurrencyConfig.ratePerUnit)

    private companion object {
        const val PAGE_WIDTH = 595f
        const val PAGE_HEIGHT = 842f
        const val MARGIN = 40f
        const val LINE_HEIGHT = 16f

        val moneyFormat = java.text.DecimalFormat(
            "#,##0.##",
            java.text.DecimalFormatSymbols(Locale("ru"))
        )
        val monthTitleFormatter = DateTimeFormatter.ofPattern("LLLL yyyy", Locale("ru"))
        val rowDateFormatter = DateTimeFormatter.ofPattern("dd.MM", Locale("ru"))
    }
}
