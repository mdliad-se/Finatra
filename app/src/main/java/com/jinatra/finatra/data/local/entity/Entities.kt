package com.jinatra.finatra.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** Kind of account a balance is held in. */
enum class AccountType { CASH, BANK, CREDIT_CARD, MOBILE_WALLET, CRYPTO, TRADING, INVESTMENT }

/** Direction of a transaction: money in, money out, or moved between own accounts. */
enum class TransactionType { INCOME, EXPENSE, TRANSFER }

/** Budget recurrence: rolling monthly, or a fixed CUSTOM date range. */
enum class BudgetPeriod { MONTHLY, CUSTOM }

/** How often a recurring transaction fires; CUSTOM uses [RecurringTransactionEntity.intervalDays]. */
enum class RecurrenceFrequency { DAILY, WEEKLY, MONTHLY, CUSTOM }

/** SAVINGS = a target to reach; DEBT_OWED = money I owe; DEBT_LENT = money owed to me (PRD 6.9). */
enum class GoalType { SAVINGS, DEBT_OWED, DEBT_LENT }

/** A financial account (wallet, bank, card, etc.) holding a balance in a single [currency]. */
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

/** A spending or income category. Supports one level of nesting via [parentId];
 *  indexed on parentId for fast subcategory lookups. */
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

/**
 * A single money movement. Deleting the owning account cascades to its transactions.
 * For TRANSFERs, [transferToAccountId] names the destination account. Indexed on accountId,
 * categoryId, and dateTime to keep the listing and aggregation queries fast.
 */
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
    val splitGroupId: Long? = null, // shared id linking parts of one split expense (PRD 6.4)
    val createdAt: Long,
    val updatedAt: Long,
)

/** A spending limit for one category over a [period]. Indexed on categoryId. */
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

/** A template that auto-generates (or reminds about) transactions on a schedule. */
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

/** One immutable history record of an action taken on a transaction. */
@Entity(tableName = "audit_log")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val transactionId: Long,
    val action: String,             // CREATED / EDITED / DELETED
    val details: String = "",
    val timestamp: Long,
)

/** A currency conversion rate. Composite primary key is the (from, to) pair. */
@Entity(tableName = "exchange_rates", primaryKeys = ["from", "to"])
data class ExchangeRateEntity(
    val from: String,
    val to: String,
    val rate: Double,               // 1 [from] = rate [to]
    val updatedAt: Long,
)

/** Savings goal / debt tracker (PRD 6.9). For SAVINGS: saved/target. For DEBT_*: repaid/total.
 *  [monthlyTarget] + [planStartedAt] hold a contribution plan (PRD goal/savings plan): the amount
 *  to set aside each month and when the plan began, so progress can be judged on-track vs behind. */
@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: GoalType = GoalType.SAVINGS,
    val targetAmount: Double,
    val savedAmount: Double = 0.0,   // saved (SAVINGS) or repaid (DEBT_*)
    val currency: String,
    val deadline: Long? = null,
    val colorHex: Long,
    val iconKey: String = "goal",
    val createdAt: Long,
    val monthlyTarget: Double = 0.0,    // planned monthly contribution; 0 = no plan yet
    val planStartedAt: Long? = null,    // when the contribution plan began (for on-track checks)
)

/** A loan tracked as an EMI plan (PRD EMI plan). The fixed monthly payment, remaining balance and
 *  progress are derived from these terms via [com.jinatra.finatra.util.Emi]; surfaced under Budgets
 *  as a recurring monthly commitment. */
@Entity(tableName = "loans")
data class LoanEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val principal: Double,
    val annualRatePct: Double,      // annual interest rate, percent (0 for interest-free)
    val tenureMonths: Int,
    val startDate: Long,            // first payment month; drives months-paid-so-far
    val currency: String,
    val createdAt: Long,
)

/** Saved reusable transaction (PRD 6.4 templates). */
@Entity(tableName = "tx_templates")
data class TransactionTemplateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: TransactionType,
    val amount: Double,
    val categoryId: Long? = null,
    val accountId: Long? = null,
    val note: String = "",
    val tags: String = "",
    val createdAt: Long,
)

/**
 * A saved chat conversation (PRD 6.11). [kind] separates the AI Coach ("coach") from the
 * budget planner ("budget") so each surface lists only its own history. [title] is shown in
 * the session list — auto-derived from the first user message, renamable by the user.
 */
@Entity(tableName = "chat_sessions")
data class ChatSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val kind: String,            // "coach" | "budget"
    val title: String,
    val createdAt: Long,
    val updatedAt: Long,         // bumped on each new message; drives most-recent-first ordering
)

/**
 * Persistent chat message (PRD 6.11). role = "user" | "ai". Belongs to a [ChatSessionEntity]
 * via [sessionId]; deleting the session cascades to its messages.
 */
@Entity(
    tableName = "chat_messages",
    foreignKeys = [
        ForeignKey(entity = ChatSessionEntity::class, parentColumns = ["id"], childColumns = ["sessionId"], onDelete = ForeignKey.CASCADE),
    ],
    indices = [Index("sessionId")],
)
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val role: String,
    val content: String,
    val timestamp: Long,
)
