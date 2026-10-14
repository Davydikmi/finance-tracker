package com.example.finance_tracker.ui.screens.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.finance_tracker.ui.components.Formatters
import com.example.finance_tracker.ui.components.TransactionListItem
import com.example.finance_tracker.ui.theme.AccentBlue
import com.example.finance_tracker.ui.theme.CardBg
import com.example.finance_tracker.ui.theme.DividerColor
import com.example.finance_tracker.ui.theme.ExpenseRed
import com.example.finance_tracker.ui.theme.IncomeGreen
import com.example.finance_tracker.ui.theme.InkPrimary
import com.example.finance_tracker.ui.theme.InkSecondary
import com.example.finance_tracker.ui.theme.OnAccent
import java.time.LocalDate
import java.time.YearMonth

/**
 * История операций: переключение месяцев, поиск, фильтр по типу,
 * группировка по дням с итогом расходов за день.
 */
@Composable
fun HistoryScreen(viewModel: HistoryViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(12.dp))
        Text(
            text = "История",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = InkPrimary
        )

        Spacer(Modifier.height(14.dp))
        MonthSelector(
            month = state.month,
            onPrevious = viewModel::previousMonth,
            onNext = viewModel::nextMonth
        )

        Spacer(Modifier.height(8.dp))
        MonthSummary(
            expenses = state.monthExpenses,
            income = state.monthIncome
        )

        Spacer(Modifier.height(14.dp))
        SearchField(
            query = state.searchQuery,
            onQueryChange = viewModel::onSearchChange
        )

        Spacer(Modifier.height(10.dp))
        FilterChips(
            selected = state.typeFilter,
            onSelect = viewModel::setFilter
        )

        Spacer(Modifier.height(14.dp))
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            state.days.isEmpty() -> EmptyState(
                isFilterActive = state.isFilterActive,
                modifier = Modifier.weight(1f)
            )

            else -> TransactionList(
                days = state.days,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun MonthSelector(
    month: YearMonth,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrevious) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Предыдущий месяц",
                tint = InkPrimary
            )
        }
        Text(
            text = Formatters.monthTitle(month),
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = InkPrimary
        )
        IconButton(onClick = onNext) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Следующий месяц",
                tint = InkPrimary
            )
        }
    }
}

@Composable
private fun MonthSummary(expenses: Double, income: Double) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Расходы: ${Formatters.expense(expenses)}",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = ExpenseRed
        )
        Text(
            text = "Доходы: ${Formatters.income(income)}",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = IncomeGreen
        )
    }
}

@Composable
private fun SearchField(query: String, onQueryChange: (String) -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = CardBg
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null,
                tint = InkSecondary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(10.dp))
            Box(modifier = Modifier.weight(1f)) {
                if (query.isEmpty()) {
                    Text(
                        text = "Поиск по операциям",
                        fontSize = 15.sp,
                        color = InkSecondary
                    )
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        fontSize = 15.sp,
                        color = InkPrimary
                    ),
                    cursorBrush = SolidColor(AccentBlue),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (query.isNotEmpty()) {
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = { onQueryChange("") }, modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Очистить поиск",
                        tint = InkSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterChips(
    selected: HistoryTypeFilter,
    onSelect: (HistoryTypeFilter) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(label = "Все", isActive = selected == HistoryTypeFilter.ALL) {
            onSelect(HistoryTypeFilter.ALL)
        }
        FilterChip(label = "Расходы", isActive = selected == HistoryTypeFilter.EXPENSES) {
            onSelect(HistoryTypeFilter.EXPENSES)
        }
        FilterChip(label = "Доходы", isActive = selected == HistoryTypeFilter.INCOMES) {
            onSelect(HistoryTypeFilter.INCOMES)
        }
    }
}

@Composable
private fun FilterChip(label: String, isActive: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = if (isActive) InkPrimary else CardBg
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = if (isActive) OnAccent else InkPrimary
        )
    }
}

@Composable
private fun TransactionList(days: List<DayTransactions>, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(days, key = { it.date.toEpochDay() }) { day ->
            Column {
                DayHeader(date = day.date, dayExpense = day.dayExpense)
                Spacer(Modifier.height(8.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = CardBg
                ) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)) {
                        day.transactions.forEachIndexed { index, transaction ->
                            TransactionListItem(transaction = transaction)
                            if (index < day.transactions.lastIndex) {
                                HorizontalDivider(color = DividerColor)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DayHeader(date: LocalDate, dayExpense: Double) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = Formatters.dayHeader(date),
            modifier = Modifier.weight(1f),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = InkSecondary
        )
        if (dayExpense > 0.0) {
            Text(
                text = Formatters.expense(dayExpense),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = ExpenseRed.copy(alpha = 0.85f)
            )
        }
    }
}

@Composable
private fun EmptyState(isFilterActive: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (isFilterActive) "Ничего не найдено" else "В этом месяце операций нет",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = InkPrimary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = if (isFilterActive) {
                    "Попробуйте изменить запрос или фильтр"
                } else {
                    "Нажмите «+» на главной, чтобы добавить операцию"
                },
                fontSize = 13.sp,
                color = InkSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}
