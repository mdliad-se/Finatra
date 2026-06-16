package com.jinatra.finatra.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class AccountType { CASH, BANK, CREDIT_CARD, MOBILE_WALLET, CRYPTO, TRADING, INVESTMENT }
enum class TransactionType { INCOME, EXPENSE, TRANSFER }
enum class BudgetPeriod { MONTHLY, CUSTOM }
enum class RecurrenceFrequency { DAILY, WEEKLY, MONTHLY, CUSTOM }

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: AccountType,
    val currency: String,
    val openingBalance: Double,
    val colorHex: Long,        // ARGB packed
    val iconKey: String = "wallet",
    val archived: Boolean = false,
    val lowBalanceThreshold: Double = 0.0,   // 0 = off; warn when balance drops below
    val createdAt: Long,
)

@Entity(
    tableName = "categories",
    indices = [Index("parentId")]
)
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val parentId: Long? = null,   // null = top-level; set = subcategory
    val colorHex: Long,
    val iconKey: String,
    val isIncome: Boolean = false,
    val isCustom: Boolean = false,
)

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(entity = AccountEntity::class, parentColumns = ["id"], childColumns = ["accountId"], onDelete = ForeignKey.CASCADE),
    ],
    indices = [Index("accountId"), Index("categoryId"), Index("dateTime")]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: TransactionType,
    val amount: Double,             // positive magnitude, in account currency
    val currency: String,
    val dateTime: Long,             // epoch millis
    val categoryId: Long? = null,
    val accountId: Long,
    val transferToAccountId: Long? = null,
    val note: String = "",
    val tags: String = "",          // comma-separated
    val receiptPath: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
)

@Entity(
    tableName = "budgets",
    indices = [Index("categoryId")]
)
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val categoryId: Long,
    val amount: Double,
    val period: BudgetPeriod,
    val startDate: Long,
    val endDate: Long? = null,      // null for rolling monthly
    val createdAt: Long,
)

@Entity(tableName = "recurring_transactions")
data class RecurringTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: TransactionType,
    val amount: Double,
    val currency: String,
    val accountId: Long,
    val categoryId: Long? = null,
    val transferToAccountId: Long? = null,
    val note: String = "",
    val frequency: RecurrenceFrequency,
    val intervalDays: Int = 0,      // used when frequency == CUSTOM
    val nextRun: Long,
    val autoLog: Boolean = true,    // false = remind to confirm
    val active: Boolean = true,
)

@Entity(tableName = "audit_log")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val transactionId: Long,
    val action: String,             // CREATED / EDITED / DELETED
    val details: String = "",
    val timestamp: Long,
)

@Entity(tableName = "exchange_rates", primaryKeys = ["from", "to"])
data class ExchangeRateEntity(
    val from: String,
    val to: String,
    val rate: Double,               // 1 [from] = rate [to]
    val updatedAt: Long,
)
