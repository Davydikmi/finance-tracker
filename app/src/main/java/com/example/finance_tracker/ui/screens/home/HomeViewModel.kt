package com.example.finance_tracker.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finance_tracker.domain.model.SavingsJar
import com.example.finance_tracker.domain.model.TransactionDetail
import com.example.finance_tracker.domain.model.TransactionType
import com.example.finance_tracker.domain.repository.BudgetRepository
import com.example.finance_tracker.domain.repository.SavingsJarRepository
import com.example.finance_tracker.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject

data class HomeUiState(
    val balance: Double = 0.0,
    val monthExpenses: Double = 0.0,
    val dailyAllowance: Double? = null,
    val budgetPercentLeft: Int? = null,
    val jars: List<SavingsJar> = emptyList(),
    val recentTransactions: List<TransactionDetail> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    transactionRepository: TransactionRepository,
    savingsJarRepository: SavingsJarRepository,
    budgetRepository: BudgetRepository
) : ViewModel() {

    private val today: LocalDate = LocalDate.now()
    private val month: YearMonth = YearMonth.from(today)
    private val monthStart: Long = month.atDay(1)
        .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    private val monthEnd: Long = month.atEndOfMonth().plusDays(1)
        .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1
    private val daysLeftInMonth: Int = month.lengthOfMonth() - today.dayOfMonth + 1

    val uiState: StateFlow<HomeUiState> = combine(
        transactionRepository.getAllTransactionsWithDetails(),
        savingsJarRepository.getActiveJars(),
        budgetRepository.observeMonthlyBudget(month.toString())
    ) { transactions, jars, budget ->
        val balance = transactions.sumOf {
            if (it.type == TransactionType.INCOME) it.amount else -it.amount
        }
        val monthExpenses = transactions
            .filter { it.type == TransactionType.EXPENSE && it.date in monthStart..monthEnd }
            .sumOf { it.amount }

        val budgetLimit = budget?.totalLimit
        val budgetRemaining = budgetLimit
            ?.let { limit -> (limit - monthExpenses).coerceAtLeast(0.0) }

        HomeUiState(
            balance = balance,
            monthExpenses = monthExpenses,
            dailyAllowance = budgetRemaining?.div(daysLeftInMonth),
            budgetPercentLeft = if (budgetLimit != null && budgetLimit > 0.0 && budgetRemaining != null) {
                (budgetRemaining / budgetLimit * 100).toInt().coerceIn(0, 100)
            } else {
                null
            },
            jars = jars,
            recentTransactions = transactions.take(RECENT_TRANSACTIONS_COUNT),
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState()
    )

    private companion object {
        const val RECENT_TRANSACTIONS_COUNT = 5
    }
}
