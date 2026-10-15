package com.example.finance_tracker.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.finance_tracker.domain.model.CurrencyRate
import com.example.finance_tracker.domain.model.SavingsJar
import com.example.finance_tracker.ui.components.Formatters
import com.example.finance_tracker.ui.components.TransactionListItem
import com.example.finance_tracker.ui.theme.AccentBlue
import com.example.finance_tracker.ui.theme.CardBg
import com.example.finance_tracker.ui.theme.CardBgLight
import com.example.finance_tracker.ui.theme.CardSolid
import com.example.finance_tracker.ui.theme.DividerColor
import com.example.finance_tracker.ui.theme.ExpenseRed
import com.example.finance_tracker.ui.theme.IncomeGreen
import com.example.finance_tracker.ui.theme.InkPrimary
import com.example.finance_tracker.ui.theme.InkSecondary
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Главный экран: баланс месяца, карточки «можно тратить в день» и бюджета,
 * копилки, последние операции. Данные — из [HomeViewModel].
 */
@Composable
fun HomeScreen(
    onOpenConverter: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    HomeContent(state, onOpenConverter)
}

@Composable
private fun HomeContent(state: HomeUiState, onOpenConverter: () -> Unit) {
    if (state.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(12.dp))
        Text(
            text = Formatters.currentMonthTitle(),
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = InkPrimary
        )

        Spacer(Modifier.height(20.dp))
        Text(text = "Свободных средств", fontSize = 13.sp, color = InkSecondary)
        Spacer(Modifier.height(2.dp))
        Text(
            text = Formatters.money(state.balance),
            fontSize = 38.sp,
            fontWeight = FontWeight.Bold,
            color = InkPrimary
        )

        Spacer(Modifier.height(18.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AllowanceCard(state.dailyAllowance, Modifier.weight(1f).fillMaxHeight())
            BudgetCard(state.budgetPercentLeft, Modifier.weight(1f).fillMaxHeight())
        }

        if (state.rates.isNotEmpty()) {
            Spacer(Modifier.height(18.dp))
            RatesRow(
                rates = state.rates,
                ratesUpdatedAt = state.ratesUpdatedAt,
                onClick = onOpenConverter
            )
        }

        Spacer(Modifier.height(26.dp))
        SectionHeader("Копилки")
        Spacer(Modifier.height(10.dp))
        if (state.jars.isEmpty()) {
            EmptyCard("Копилок пока нет — создайте первую в разделе «Копилки»")
        } else {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(state.jars, key = { it.id }) { jar ->
                    JarCard(jar = jar)
                }
            }
        }

        Spacer(Modifier.height(26.dp))
        SectionHeader("Последние операции")
        Spacer(Modifier.height(10.dp))
        if (state.recentTransactions.isEmpty()) {
            EmptyCard("Операций пока нет. Нажмите «+», чтобы добавить первую")
        } else {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = CardBg
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
                ) {
                    state.recentTransactions.forEachIndexed { index, transaction ->
                        TransactionListItem(transaction = transaction)
                        if (index < state.recentTransactions.lastIndex) {
                            HorizontalDivider(color = DividerColor)
                        }
                    }
                }
            }
        }

        // Запас под FAB и нижнее меню
        Spacer(Modifier.height(110.dp))
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        fontSize = 17.sp,
        fontWeight = FontWeight.SemiBold,
        color = InkPrimary
    )
}

@Composable
private fun EmptyCard(text: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = CardBgLight
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(16.dp),
            fontSize = 13.sp,
            lineHeight = 18.sp,
            color = InkSecondary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun AllowanceCard(dailyAllowance: Double?, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = CardBg
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(shape = CircleShape, color = CardSolid) {
                Icon(
                    imageVector = Icons.Outlined.Schedule,
                    contentDescription = null,
                    tint = InkPrimary,
                    modifier = Modifier
                        .padding(9.dp)
                        .size(20.dp)
                )
            }
            Spacer(Modifier.width(10.dp))
            Column {
                Text(
                    text = dailyAllowance?.let { Formatters.money(it) } ?: "—",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = InkPrimary
                )
                Spacer(Modifier.height(2.dp))
                Text(text = "Можно тратить в день", fontSize = 11.sp, color = InkSecondary)
            }
        }
    }
}

@Composable
private fun BudgetCard(percentLeft: Int?, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = CardBg
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = "Бюджет", fontSize = 11.sp, color = InkSecondary)
            Spacer(Modifier.height(4.dp))
            Text(
                text = if (percentLeft != null) "$percentLeft% осталось" else "не задан",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = InkPrimary
            )
            percentLeft?.let { left ->
                val usage = (100 - left).coerceIn(0, 100)
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { usage / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = budgetUsageColor(usage),
                    trackColor = CardSolid
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "израсходовано $usage%",
                    fontSize = 10.sp,
                    color = InkSecondary
                )
            }
        }
    }
}

/** Зелёный — израсходовано меньше половины, жёлтый — до 80%, красный — больше. */
private fun budgetUsageColor(usagePercent: Int): Color = when {
    usagePercent > 80 -> ExpenseRed
    usagePercent >= 50 -> Color(0xFFF5A623)
    else -> IncomeGreen
}

/** Карточки курсов НБ РБ: USD / EUR / RUB к BYN; тап — конвертер. */
@Composable
private fun RatesRow(
    rates: List<CurrencyRate>,
    ratesUpdatedAt: Long?,
    onClick: () -> Unit
) {
    Column {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            rates.forEach { rate ->
                Surface(
                    onClick = onClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    color = CardBg
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = rate.code,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = InkSecondary
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = DecimalFormat("#,##0.00", DecimalFormatSymbols(Locale("ru")))
                                .format(rate.ratePerUnit),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = InkPrimary
                        )
                    }
                }
            }
        }
        ratesUpdatedAt?.let { updatedAt ->
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Курс НБ РБ на " + Instant.ofEpochMilli(updatedAt)
                    .atZone(ZoneId.systemDefault()).toLocalDate()
                    .format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) +
                    " · нажмите для конвертера",
                fontSize = 11.sp,
                color = InkSecondary
            )
        }
    }
}

@Composable
private fun JarCard(jar: SavingsJar) {
    Surface(
        modifier = Modifier.width(216.dp),
        shape = RoundedCornerShape(20.dp),
        color = CardBg
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = jar.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = InkPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = Formatters.money(jar.savedAmount),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = InkPrimary
            )
            Spacer(Modifier.height(12.dp))
            val target = jar.targetAmount
            if (target != null && target > 0.0) {
                LinearProgressIndicator(
                    progress = { (jar.savedAmount / target).toFloat().coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = AccentBlue,
                    trackColor = CardSolid.copy(alpha = 0.6f)
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "цель: ${Formatters.money(target)}",
                    fontSize = 11.sp,
                    color = InkSecondary
                )
            } else {
                Text(text = "без цели", fontSize = 11.sp, color = InkSecondary)
            }
        }
    }
}
