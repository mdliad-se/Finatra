package com.jinatra.finatra.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.jinatra.finatra.data.local.dao.AccountDao
import com.jinatra.finatra.data.local.dao.AuditDao
import com.jinatra.finatra.data.local.dao.BudgetDao
import com.jinatra.finatra.data.local.dao.CategoryDao
import com.jinatra.finatra.data.local.dao.ChatDao
import com.jinatra.finatra.data.local.dao.ExchangeRateDao
import com.jinatra.finatra.data.local.dao.GoalDao
import com.jinatra.finatra.data.local.dao.LoanDao
import com.jinatra.finatra.data.local.dao.RecurringDao
import com.jinatra.finatra.data.local.dao.TemplateDao
import com.jinatra.finatra.data.local.dao.TransactionDao
import com.jinatra.finatra.data.local.entity.AccountEntity
import com.jinatra.finatra.data.local.entity.AuditLogEntity
import com.jinatra.finatra.data.local.entity.BudgetEntity
import com.jinatra.finatra.data.local.entity.CategoryEntity
import com.jinatra.finatra.data.local.entity.ChatMessageEntity
import com.jinatra.finatra.data.local.entity.ChatSessionEntity
import com.jinatra.finatra.data.local.entity.ExchangeRateEntity
import com.jinatra.finatra.data.local.entity.GoalEntity
import com.jinatra.finatra.data.local.entity.LoanEntity
import com.jinatra.finatra.data.local.entity.RecurringTransactionEntity
import com.jinatra.finatra.data.local.entity.TransactionEntity
import com.jinatra.finatra.data.local.entity.TransactionTemplateEntity

/**
 * The app's Room database (PRD: fully local, offline-first storage).
 * Current schema version is 7; incremental upgrades are defined in [ALL_MIGRATIONS] and enum
 * columns are handled by [Converters]. `exportSchema = true` writes JSON schemas for migration tests.
 */
@Database(
    entities = [
        AccountEntity::class,
        CategoryEntity::class,
        TransactionEntity::class,
        BudgetEntity::class,
        RecurringTransactionEntity::class,
        AuditLogEntity::class,
        ExchangeRateEntity::class,
        GoalEntity::class,
        LoanEntity::class,
        ChatSessionEntity::class,
        ChatMessageEntity::class,
        TransactionTemplateEntity::class,
    ],
    version = 7,
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
    abstract fun goalDao(): GoalDao
    abstract fun loanDao(): LoanDao
    abstract fun chatDao(): ChatDao
    abstract fun templateDao(): TemplateDao
}
