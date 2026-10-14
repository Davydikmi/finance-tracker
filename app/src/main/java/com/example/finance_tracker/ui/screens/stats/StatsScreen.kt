package com.example.finance_tracker.ui.screens.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.finance_tracker.ui.components.Formatters
import com.example.finance_tracker.ui.theme.AccentBlue
import com.example.finance_tracker.ui.theme.CardBg
import com.example.finance_tracker.ui.theme.CardSolid
import com.example.finance_tracker.ui.theme.ExpenseRed
import com.example.finance_tracker.ui.theme.IncomeGreen
import com.example.finance_tracker.ui.theme.InkPrimary
import com.example.finance_tracker.ui.theme.InkSecondary
import com.example.finance_tracker.ui.theme.OnAccent
import com.example.finance_tracker.ui.theme.ScreenBg

/**
 * Статистика: период Неделя/Месяц/Год, расходы периода со сравнением
 * с предыдущим, пончик по категориям, график по дням, топ категорий.
 */
@Composable
fun StatsScreen(viewModel: StatsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Статистика",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = InkPrimary
        )

        Spacer(Modifier.height(14.dp))
        PeriodPills(
            selected = state.period,
            onSelect = viewModel::setPeriod
        )

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

            state.totalExpenses <= 0.0 -> EmptyState(modifier = Modifier.weight(1f))

            else -> Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(Modifier.height(14.dp))
                TotalCard(state)

                Spacer(Modifier.height(12.dp))
                DonutCard(state.categories, state.totalExpenses)

                Spacer(Modifier.height(12.dp))
                DailyChartCard(state.dailyPoints)

                Spacer(Modifier.height(20.dp))
                Text(
                    text = "Топ категорий",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = InkPrimary
                )
                Spacer(Modifier.height(10.dp))
                TopCategories(state.categories)

                Spacer(Modifier.height(20.dp))
            }
        }
        Spacer(Modifier.height(4.dp))
    }
}

@Composable
private fun PeriodPills(selected: StatsPeriod, onSelect: (StatsPeriod) -> Unit) {
    Surface(shape = RoundedCornerShape(18.dp), color = CardBg) {
        Row(modifier = Modifier.padding(4.dp)) {
            StatsPeriod.entries.forEach { period ->
                val active = period == selected
                Surface(
                    onClick = { onSelect(period) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    color = if (active) InkPrimary else Color.Transparent
                ) {
                    Text(
                        text = period.label,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp,
                        fontWeight = if (active) FontWeight.SemiBold else FontWeight.Medium,
                        color = if (active) OnAccent else InkSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun TotalCard(state: StatsUiState) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = CardBg
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Расходы: ${Formatters.money(state.totalExpenses)}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = InkPrimary
                )
                Spacer(Modifier.width(8.dp))
                state.changePercent?.let { change ->
                    val increased = change > 0
                    Text(
                        text = "${if (increased) "↑" else "↓"} ${kotlin.math.abs(change)}% " +
                            "к прошлому ${state.period.previousWord()}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (increased) ExpenseRed else IncomeGreen
                    )
                }
            }
        }
    }
}

private fun StatsPeriod.previousWord(): String = when (this) {
    StatsPeriod.WEEK -> "неделе"
    StatsPeriod.MONTH -> "месяцу"
    StatsPeriod.YEAR -> "году"
}

@Composable
private fun DonutCard(categories: List<CategoryShare>, total: Double) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = CardBg
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.Center) {
                DonutChart(
                    categories = categories,
                    total = total,
                    modifier = Modifier.size(190.dp)
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = Formatters.compactMoney(total),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = InkPrimary
                    )
                    Text(text = "за период", fontSize = 11.sp, color = InkSecondary)
                }
            }

            Spacer(Modifier.height(14.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                categories.forEach { share ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(share.color?.let { Color(it) } ?: InkSecondary)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = share.name,
                            modifier = Modifier.weight(1f),
                            fontSize = 13.sp,
                            color = InkPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = Formatters.money(share.amount),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = InkPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DonutChart(
    categories: List<CategoryShare>,
    total: Double,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val stroke = 26.dp.toPx()
        val diameter = size.minDimension - stroke
        val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
        val arcSize = Size(diameter, diameter)
        var startAngle = -90f
        categories.forEach { share ->
            val sweep = (share.amount / total * 360.0).toFloat()
            drawArc(
                color = share.color?.let { Color(it) } ?: InkSecondary,
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke)
            )
            startAngle += sweep
        }
    }
}

@Composable
private fun DailyChartCard(points: List<DayPoint>) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = CardBg
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "По дням",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = InkPrimary
            )
            Spacer(Modifier.height(12.dp))
            DailyExpensesChart(
                points = points,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
            )
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                points.forEach { point ->
                    Text(
                        text = if (point.showLabel) point.label else "",
                        fontSize = 10.sp,
                        color = InkSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun DailyExpensesChart(points: List<DayPoint>, modifier: Modifier = Modifier) {
    val maxValue = points.maxOfOrNull { it.value } ?: 0.0
    if (maxValue <= 0.0) return
    Canvas(modifier = modifier.height(140.dp)) {
        val count = points.size
        if (count < 2) return@Canvas
        val stepX = size.width / (count - 1)
        val linePath = Path()
        val fillPath = Path()
        points.forEachIndexed { index, point ->
            val x = index * stepX
            val y = size.height * (1f - (point.value / maxValue).toFloat())
            if (index == 0) {
                linePath.moveTo(x, y)
                fillPath.moveTo(x, y)
            } else {
                linePath.lineTo(x, y)
                fillPath.lineTo(x, y)
            }
        }
        fillPath.lineTo(size.width, size.height)
        fillPath.lineTo(0f, size.height)
        fillPath.close()
        drawPath(fillPath, color = AccentBlue.copy(alpha = 0.12f))
        drawPath(
            linePath,
            color = AccentBlue,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

@Composable
private fun TopCategories(categories: List<CategoryShare>) {
    val maxAmount = categories.firstOrNull()?.amount ?: 0.0
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        categories.forEach { share ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(share.color?.let { Color(it) } ?: InkSecondary)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = share.name,
                    fontSize = 13.sp,
                    color = InkPrimary,
                    modifier = Modifier.width(90.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                LinearProgressIndicator(
                    progress = { (share.amount / maxAmount).toFloat().coerceIn(0f, 1f) },
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = share.color?.let { Color(it) } ?: AccentBlue,
                    trackColor = CardSolid
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = Formatters.money(share.amount),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = InkPrimary
                )
            }
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Расходов за период нет",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = InkPrimary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Добавьте операции — и здесь появится статистика",
                fontSize = 13.sp,
                color = InkSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}
