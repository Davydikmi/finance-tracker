package com.example.finance_tracker.data.local

import androidx.room.TypeConverter
import com.example.finance_tracker.domain.model.JarMovementType
import com.example.finance_tracker.domain.model.RecurringPeriod
import com.example.finance_tracker.domain.model.TransactionType

/**
 * Room type converters: enums are persisted by their [Enum.name] string.
 * All converters are null-safe so nullable enum columns
 * (e.g. TransactionEntity.recurringPeriod) round-trip correctly.
 */
class Converters {

    @TypeConverter
    fun transactionTypeToString(value: TransactionType?): String? = value?.name

    @TypeConverter
    fun stringToTransactionType(value: String?): TransactionType? =
        value?.let { TransactionType.valueOf(it) }

    @TypeConverter
    fun jarMovementTypeToString(value: JarMovementType?): String? = value?.name

    @TypeConverter
    fun stringToJarMovementType(value: String?): JarMovementType? =
        value?.let { JarMovementType.valueOf(it) }

    @TypeConverter
    fun recurringPeriodToString(value: RecurringPeriod?): String? = value?.name

    @TypeConverter
    fun stringToRecurringPeriod(value: String?): RecurringPeriod? =
        value?.let { RecurringPeriod.valueOf(it) }
}
