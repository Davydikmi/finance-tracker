package com.example.finance_tracker.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.finance_tracker.domain.model.TransactionDetail
import com.example.finance_tracker.domain.model.TransactionType
import com.example.finance_tracker.ui.theme.ExpenseRed
import com.example.finance_tracker.ui.theme.IncomeGreen
import com.example.finance_tracker.ui.theme.InkPrimary
import com.example.finance_tracker.ui.theme.InkSecondary

/**
 * Строка транзакции: точка категории, заголовок с подстрокой
 * «дата, время · категория» и сумма (красная расход / зелёная доход).
 * Используется на главной и в истории.
 */
@Composable
fun TransactionListItem(
    transaction: TransactionDetail,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CategoryDot(color = transaction.categoryColor)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            val title = transaction.description
                ?: transaction.categoryName
                ?: "Без категории"
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = InkPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            val categoryPart = transaction.categoryName?.let { " · $it" }.orEmpty()
            Text(
                text = "${Formatters.dateLabel(transaction.date)}, ${Formatters.time(transaction.date)}$categoryPart",
                fontSize = 12.sp,
                color = InkSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(Modifier.width(8.dp))
        val isExpense = transaction.type == TransactionType.EXPENSE
        Text(
            text = if (isExpense) {
                Formatters.expense(transaction.amount)
            } else {
                Formatters.income(transaction.amount)
            },
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isExpense) ExpenseRed else IncomeGreen
        )
    }
}
