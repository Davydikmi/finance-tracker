package com.example.finance_tracker.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finance_tracker.domain.model.TransactionDetail
import com.example.finance_tracker.domain.model.TransactionType
import com.example.finance_tracker.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject

enum class HistoryTypeFilter {
    ALL,
    EXPENSES,
    INCOMES
}

/** Операции одного дня; итог дня считается по расходам. */
data class DayTransactions(
    val date: LocalDate,
    val transactions: List<TransactionDetail>
) {
    val dayExpense: Double
        get() = transactions
            .filter { it.type == TransactionType.EXPENSE }
            .sumOf { it.amount }
}

data class HistoryUiState(
    val month: YearMonth = YearMonth.now(),
    val searchQuery: String = "",
    val typeFilter: HistoryTypeFilter = HistoryTypeFilter.ALL,
    val monthExpenses: Double = 0.0,
    val monthIncome: Double = 0.0,
    val days: List<DayTransactions> = emptyList(),
    val isLoading: Boolean = true
) {
    val isFilterActive: Boolean
        get() = searchQuery.isNotBlank() || typeFilter != HistoryTypeFilter.ALL
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HistoryViewModel @Inject constructor(
    transactionRepository: TransactionRepository
) : ViewModel() {

    private val selectedMonth = MutableStateFlow(YearMonth.now())
    private val searchQuery = MutableStateFlow("")
    private val typeFilter = MutableStateFlow(HistoryTypeFilter.ALL)

    val uiState: StateFlow<HistoryUiState> = combine(
        selectedMonth.flatMapLatest { month ->
            transactionRepository.getTransactionsByDateRange(month.startMillis(), month.endMillis())
        },
        selectedMonth,
        searchQuery,
        typeFilter
    ) { monthTransactions, month, query, filter ->
        val filtered = monthTransactions.filter { transaction ->
            val typeOk = when (filter) {
                HistoryTypeFilter.ALL -> true
                HistoryTypeFilter.EXPENSES -> transaction.type == TransactionType.EXPENSE
                HistoryTypeFilter.INCOMES -> transaction.type == TransactionType.INCOME
            }
            val searchOk = query.isBlank() ||
                transaction.description?.contains(query.trim(), ignoreCase = true) == true ||
                transaction.categoryName?.contains(query.trim(), ignoreCase = true) == true
            typeOk && searchOk
        }

        val zone = ZoneId.systemDefault()
        val days = filtered
            .groupBy { Instant.ofEpochMilli(it.date).atZone(zone).toLocalDate() }
            .toSortedMap(compareByDescending<LocalDate> { it })
            .map { (date, list) -> DayTransactions(date, list) }

        HistoryUiState(
            month = month,
            searchQuery = query,
            typeFilter = filter,
            monthExpenses = monthTransactions
                .filter { it.type == TransactionType.EXPENSE }
                .sumOf { it.amount },
            monthIncome = monthTransactions
                .filter { it.type == TransactionType.INCOME }
                .sumOf { it.amount },
            days = days,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HistoryUiState()
    )

    fun previousMonth() {
        selectedMonth.update { it.minusMonths(1) }
    }

    fun nextMonth() {
        selectedMonth.update { it.plusMonths(1) }
    }

    fun onSearchChange(query: String) {
        searchQuery.update { query }
    }

    fun setFilter(filter: HistoryTypeFilter) {
        typeFilter.update { filter }
    }

    private fun YearMonth.startMillis(): Long =
        atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

    private fun YearMonth.endMillis(): Long =
        atEndOfMonth().plusDays(1)
            .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1
}
