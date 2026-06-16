package com.jinatra.finatra.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.jinatra.finatra.data.local.dao.AccountDao
import com.jinatra.finatra.data.local.dao.AuditDao
import com.jinatra.finatra.data.local.dao.BudgetDao
import com.jinatra.finatra.data.local.dao.CategoryDao
import com.jinatra.finatra.data.local.dao.ExchangeRateDao
import com.jinatra.finatra.data.local.dao.RecurringDao
import com.jinatra.finatra.data.local.dao.TransactionDao
import com.jinatra.finatra.data.local.entity.AccountEntity
import com.jinatra.finatra.data.local.entity.AuditLogEntity
import com.jinatra.finatra.data.local.entity.BudgetEntity
import com.jinatra.finatra.data.local.entity.CategoryEntity
import com.jinatra.finatra.data.local.entity.ExchangeRateEntity
import com.jinatra.finatra.data.local.entity.RecurringTransactionEntity
import com.jinatra.finatra.data.local.entity.TransactionEntity

@Database(
    entities = [
        AccountEntity::class,
        CategoryEntity::class,
        TransactionEntity::class,
        BudgetEntity::class,
        RecurringTransactionEntity::class,
        AuditLogEntity::class,
        ExchangeRateEntity::class,
    ],
    version = 2,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class FinatraDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao
    abstract fun recurringDao(): RecurringDao
    abstract fun auditDao(): AuditDao
    abstract fun exchangeRateDao(): ExchangeRateDao
}
