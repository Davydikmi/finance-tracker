package com.example.finance_tracker.domain.model

/**
 * Type of a money movement: money coming in or money going out.
 */
enum class TransactionType {
    INCOME,
    EXPENSE
}

/**
 * Type of a savings jar movement: putting money in or taking money out.
 */
enum class JarMovementType {
    DEPOSIT,
    WITHDRAW
}

/**
 * Period at which a recurring transaction repeats.
 */
enum class RecurringPeriod {
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY
}
