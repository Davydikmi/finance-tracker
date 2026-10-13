package com.example.finance_tracker.ui.screens.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finance_tracker.domain.model.CategoryExpense
import com.example.finance_tracker.domain.model.TransactionType
import com.example.finance_tracker.domain.repository.TransactionRepository
import com.example.finance_tracker.ui.components.Formatters
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject
import kotlin.math.roundToInt

enum class StatsPeriod(val label: String) {
    WEEK("Неделя"),
    MONTH("Месяц"),
    YEAR("Год")
}

/** Доля категории в расходах периода — элемент пончика и топа. */
data class CategoryShare(
    val name: String,
    val color: Long?,
    val amount: Double,
    val percent: Int
)

/** Точка графика «по дням»: подпись и сумма расходов за день/месяц. */
data class DayPoint(
    val label: String,
    val value: Double,
    /** false — подпись скрыта (на оси месяца показываем не каждый день). */
    val showLabel: Boolean = true
)

data class StatsUiState(
    val period: StatsPeriod = StatsPeriod.MONTH,
    val totalExpenses: Double = 0.0,
    /** Изменение к предыдущему периоду в процентах; null — сравнивать не с чем. */
    val changePercent: Int? = null,
    val categories: List<CategoryShare> = emptyList(),
    val dailyPoints: List<DayPoint> = emptyList(),
    val isLoading: Boolean = true
)

private data class PeriodRange(val start: LocalDate, val end: LocalDate) {
    val startMillis: Long
        get() = start.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    val endMillis: Long
        get() = end.plusDays(1).atStartOfDay(ZoneId.systemDefault())
            .toInstant().toEpochMilli() - 1
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class StatsViewModel @Inject constructor(
    transactionRepository: TransactionRepository
) : ViewModel() {

    private val period = MutableStateFlow(StatsPeriod.MONTH)

    val uiState: StateFlow<StatsUiState> = period.flatMapLatest { selected ->
        val today = LocalDate.now()
        val current = selected.currentRange(today)
        val previous = selected.previousRange(today)

        combine(
            transactionRepository.getTransactionsByDateRange(current.startMillis, current.endMillis),
            transactionRepository.observeExpensesByCategory(current.startMillis, current.endMillis),
            flow { emit(transactionRepository.getTotalExpenses(previous.startMillis, previous.endMillis)) }
        ) { transactions, categoryExpenses, prevTotal ->
            buildState(selected, current, transactions, categoryExpenses, prevTotal)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = StatsUiState()
    )

    fun setPeriod(newPeriod: StatsPeriod) {
        period.update { newPeriod }
    }

    private fun buildState(
        selected: StatsPeriod,
        range: PeriodRange,
        transactions: List<com.example.finance_tracker.domain.model.TransactionDetail>,
        categoryExpenses: List<CategoryExpense>,
        prevTotal: Double?
    ): StatsUiState {
        val expenses = transactions.filter { it.type == TransactionType.EXPENSE }
        val total = expenses.sumOf { it.amount }
        val zone = ZoneId.systemDefault()

        // Расходы по календарным дням/месяцам периода
        val byDay = expenses.groupBy {
            java.time.Instant.ofEpochMilli(it.date).atZone(zone).toLocalDate()
        }.mapValues { (_, list) -> list.sumOf { it.amount } }

        val dailyPoints = when (selected) {
            StatsPeriod.WEEK -> (0..6).map { offset ->
                val day = range.start.plusDays(offset.toLong())
                DayPoint(Formatters.weekdayShort(day), byDay[day] ?: 0.0)
            }

            StatsPeriod.MONTH -> (1..range.start.lengthOfMonth()).map { dayOfMonth ->
                val day = range.start.withDayOfMonth(dayOfMonth)
                DayPoint(
                    label = dayOfMonth.toString(),
                    value = byDay[day] ?: 0.0,
                    showLabel = dayOfMonth == 1 ||
                        dayOfMonth % 5 == 0 ||
                        dayOfMonth == range.start.lengthOfMonth()
                )
            }

            StatsPeriod.YEAR -> (0..11).map { offset ->
                val month = YearMonth.from(range.start).plusMonths(offset.toLong())
                val monthTotal = byDay.entries
                    .filter { YearMonth.from(it.key) == month }
                    .sumOf { it.value }
                DayPoint(Formatters.monthShort(month), monthTotal)
            }
        }

        val categories = categoryExpenses
            .filter { it.amount > 0.0 }
            .map { expense ->
                CategoryShare(
                    name = expense.categoryName ?: "Без категории",
                    color = expense.categoryColor,
                    amount = expense.amount,
                    percent = if (total > 0.0) (expense.amount / total * 100).roundToInt() else 0
                )
            }

        val change = prevTotal
            ?.takeIf { it > 0.0 && total > 0.0 }
            ?.let { ((total - it) / it * 100).roundToInt() }

        return StatsUiState(
            period = selected,
            totalExpenses = total,
            changePercent = change,
            categories = categories,
            dailyPoints = dailyPoints,
            isLoading = false
        )
    }

    private fun StatsPeriod.currentRange(today: LocalDate): PeriodRange = when (this) {
        StatsPeriod.WEEK -> PeriodRange(
            start = today.with(DayOfWeek.MONDAY),
            end = today.with(DayOfWeek.SUNDAY)
        )

        StatsPeriod.MONTH -> PeriodRange(
            start = today.withDayOfMonth(1),
            end = today.withDayOfMonth(today.lengthOfMonth())
        )

        StatsPeriod.YEAR -> PeriodRange(
            start = today.withDayOfYear(1),
            end = today.withDayOfYear(today.lengthOfYear())
        )
    }

    private fun StatsPeriod.previousRange(today: LocalDate): PeriodRange = when (this) {
        StatsPeriod.WEEK -> PeriodRange(
            start = today.minusWeeks(1).with(DayOfWeek.MONDAY),
            end = today.minusWeeks(1).with(DayOfWeek.SUNDAY)
        )

        StatsPeriod.MONTH -> {
            val prev = today.minusMonths(1)
            PeriodRange(prev.withDayOfMonth(1), prev.withDayOfMonth(prev.lengthOfMonth()))
        }

        StatsPeriod.YEAR -> {
            val prev = today.minusYears(1)
            PeriodRange(prev.withDayOfYear(1), prev.withDayOfYear(prev.lengthOfYear()))
        }
    }
}
