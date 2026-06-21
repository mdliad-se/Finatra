package com.jinatra.finatra.data.local

import androidx.room.TypeConverter
import com.jinatra.finatra.data.local.entity.AccountType
import com.jinatra.finatra.data.local.entity.BudgetPeriod
import com.jinatra.finatra.data.local.entity.GoalType
import com.jinatra.finatra.data.local.entity.RecurrenceFrequency
import com.jinatra.finatra.data.local.entity.TransactionType

/**
 * Room [TypeConverter]s for the app's enums. Each enum is stored as its [Enum.name] string
 * column; the `to*` methods read it back via `valueOf`. Registered on [FinatraDatabase] via
 * `@TypeConverters`. Note: renaming or removing an enum constant would break reads of existing rows.
 */
class Converters {
    @TypeConverter fun toAccountType(v: String) = AccountType.valueOf(v)
    @TypeConverter fun fromAccountType(v: AccountType) = v.name

    @TypeConverter fun toGoalType(v: String) = GoalType.valueOf(v)
    @TypeConverter fun fromGoalType(v: GoalType) = v.name

    @TypeConverter fun toTransactionType(v: String) = TransactionType.valueOf(v)
    @TypeConverter fun fromTransactionType(v: TransactionType) = v.name

    @TypeConverter fun toBudgetPeriod(v: String) = BudgetPeriod.valueOf(v)
    @TypeConverter fun fromBudgetPeriod(v: BudgetPeriod) = v.name

    @TypeConverter fun toFrequency(v: String) = RecurrenceFrequency.valueOf(v)
    @TypeConverter fun fromFrequency(v: RecurrenceFrequency) = v.name
}
