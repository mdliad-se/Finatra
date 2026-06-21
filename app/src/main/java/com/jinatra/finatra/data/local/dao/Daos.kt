package com.jinatra.finatra.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.jinatra.finatra.data.local.entity.AccountEntity
import com.jinatra.finatra.data.local.entity.AuditLogEntity
import com.jinatra.finatra.data.local.entity.BudgetEntity
import com.jinatra.finatra.data.local.entity.CategoryEntity
import com.jinatra.finatra.data.local.entity.ChatMessageEntity
import com.jinatra.finatra.data.local.entity.ExchangeRateEntity
import com.jinatra.finatra.data.local.entity.GoalEntity
import com.jinatra.finatra.data.local.entity.RecurringTransactionEntity
import com.jinatra.finatra.data.local.entity.TransactionEntity
import com.jinatra.finatra.data.local.entity.TransactionTemplateEntity
import kotlinx.coroutines.flow.Flow

/** Projection: a transaction joined with its category + account display fields. */
data class TransactionWithDetails(
    val id: Long,
    val type: String,
    val amount: Double,
    val currency: String,
    val dateTime: Long,
    val note: String,
    val tags: String,
    val accountId: Long,
    val accountName: String?,
    val transferToAccountId: Long?,
    val categoryId: Long?,
    val categoryName: String?,
    val categoryColor: Long?,
    val categoryIcon: String?,
    val receiptPath: String?,
)

/** Projection: total expense per category over a period (for analytics/pie charts). */
data class CategorySpend(
    val categoryId: Long?,
    val categoryName: String?,
    val colorHex: Long?,
    val total: Double,
)

/** Projection: summed amount grouped by currency, used before converting to a base currency. */
data class CurrencySum(
    val currency: String,
    val total: Double,
)

/** Aggregated spend for one payee/merchant (transaction note), for payee tracking (PRD 6.8). */
data class PayeeSpend(
    val payee: String,
    val total: Double,
    val count: Int,
)

/** CRUD and balance queries for financial accounts. */
@Dao
interface AccountDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(a: AccountEntity): Long
    @Update suspend fun update(a: AccountEntity)
    @Delete suspend fun delete(a: AccountEntity)
    @Query("SELECT * FROM accounts WHERE archived = 0 ORDER BY id") fun observeAll(): Flow<List<AccountEntity>>
    @Query("SELECT * FROM accounts WHERE id = :id") suspend fun byId(id: Long): AccountEntity?
    @Query("SELECT * FROM accounts WHERE id = :id") fun observeById(id: Long): Flow<AccountEntity?>

    /** Current balance = opening + income - expense - transfers out + transfers in. */
    @Query("""
        SELECT a.openingBalance
          + COALESCE((SELECT SUM(amount) FROM transactions WHERE accountId = a.id AND type = 'INCOME'), 0)
          - COALESCE((SELECT SUM(amount) FROM transactions WHERE accountId = a.id AND type = 'EXPENSE'), 0)
          - COALESCE((SELECT SUM(amount) FROM transactions WHERE accountId = a.id AND type = 'TRANSFER'), 0)
          + COALESCE((SELECT SUM(amount) FROM transactions WHERE transferToAccountId = a.id AND type = 'TRANSFER'), 0)
        FROM accounts a WHERE a.id = :id
    """)
    fun observeBalance(id: Long): Flow<Double?>

    @Query("SELECT COALESCE(SUM(openingBalance),0) FROM accounts WHERE archived = 0")
    fun observeTotalOpening(): Flow<Double>

    /** One-shot balance (same formula as observeBalance). */
    @Query("""
        SELECT a.openingBalance
          + COALESCE((SELECT SUM(amount) FROM transactions WHERE accountId = a.id AND type = 'INCOME'), 0)
          - COALESCE((SELECT SUM(amount) FROM transactions WHERE accountId = a.id AND type = 'EXPENSE'), 0)
          - COALESCE((SELECT SUM(amount) FROM transactions WHERE accountId = a.id AND type = 'TRANSFER'), 0)
          + COALESCE((SELECT SUM(amount) FROM transactions WHERE transferToAccountId = a.id AND type = 'TRANSFER'), 0)
        FROM accounts a WHERE a.id = :id
    """)
    suspend fun balance(id: Long): Double?
}

/** CRUD and lookups for spending/income categories, including the parent-child hierarchy. */
@Dao
interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(c: CategoryEntity): Long
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insertAll(c: List<CategoryEntity>)
    @Update suspend fun update(c: CategoryEntity)
    @Delete suspend fun delete(c: CategoryEntity)
    @Query("SELECT * FROM categories ORDER BY isIncome, name") fun observeAll(): Flow<List<CategoryEntity>>
    @Query("SELECT * FROM categories WHERE parentId IS NULL ORDER BY name") fun observeTopLevel(): Flow<List<CategoryEntity>>
    @Query("SELECT COUNT(*) FROM categories") suspend fun count(): Int
    @Query("SELECT * FROM categories WHERE id = :id") suspend fun byId(id: Long): CategoryEntity?
    @Query("SELECT * FROM categories WHERE name = :name COLLATE NOCASE AND isIncome = :income LIMIT 1")
    suspend fun byName(name: String, income: Boolean): CategoryEntity?
    @Query("SELECT * FROM categories WHERE parentId IS NULL") suspend fun allTopLevel(): List<CategoryEntity>
}

/** CRUD plus the analytics/aggregation queries over transactions (totals, spend breakdowns, payees). */
@Dao
interface TransactionDao {
    @Insert suspend fun insert(t: TransactionEntity): Long
    @Update suspend fun update(t: TransactionEntity)
    @Delete suspend fun delete(t: TransactionEntity)
    @Query("SELECT * FROM transactions WHERE id = :id") suspend fun byId(id: Long): TransactionEntity?

    @Query("""
        SELECT t.id, t.type, t.amount, t.currency, t.dateTime, t.note, t.tags,
               t.accountId, a.name AS accountName, t.transferToAccountId,
               t.categoryId, c.name AS categoryName, c.colorHex AS categoryColor, c.iconKey AS categoryIcon,
               t.receiptPath
        FROM transactions t
        LEFT JOIN accounts a ON a.id = t.accountId
        LEFT JOIN categories c ON c.id = t.categoryId
        ORDER BY t.dateTime DESC
    """)
    fun observeAll(): Flow<List<TransactionWithDetails>>

    @Query("""
        SELECT t.id, t.type, t.amount, t.currency, t.dateTime, t.note, t.tags,
               t.accountId, a.name AS accountName, t.transferToAccountId,
               t.categoryId, c.name AS categoryName, c.colorHex AS categoryColor, c.iconKey AS categoryIcon,
               t.receiptPath
        FROM transactions t
        LEFT JOIN accounts a ON a.id = t.accountId
        LEFT JOIN categories c ON c.id = t.categoryId
        ORDER BY t.dateTime DESC LIMIT :limit
    """)
    fun observeRecent(limit: Int): Flow<List<TransactionWithDetails>>

    @Query("SELECT COALESCE(SUM(amount),0) FROM transactions WHERE type = :type AND dateTime BETWEEN :start AND :end")
    fun observeTotalByType(type: String, start: Long, end: Long): Flow<Double>

    @Query("""
        SELECT t.categoryId AS categoryId, c.name AS categoryName, c.colorHex AS colorHex, SUM(t.amount) AS total
        FROM transactions t LEFT JOIN categories c ON c.id = t.categoryId
        WHERE t.type = 'EXPENSE' AND t.dateTime BETWEEN :start AND :end
        GROUP BY t.categoryId ORDER BY total DESC
    """)
    fun observeSpendByCategory(start: Long, end: Long): Flow<List<CategorySpend>>

    @Query("SELECT COALESCE(SUM(amount),0) FROM transactions WHERE type='EXPENSE' AND categoryId=:categoryId AND dateTime BETWEEN :start AND :end")
    suspend fun spentInCategory(categoryId: Long, start: Long, end: Long): Double

    /** Category expense summed per currency, so mixed-currency spend can be converted to a base
     *  currency before comparing against a budget limit (see FinanceRepository.convertedSpentInCategory). */
    @Query("SELECT currency, COALESCE(SUM(amount),0) AS total FROM transactions WHERE type='EXPENSE' AND categoryId=:categoryId AND dateTime BETWEEN :start AND :end GROUP BY currency")
    suspend fun spentInCategoryByCurrency(categoryId: Long, start: Long, end: Long): List<CurrencySum>

    @Query("SELECT COALESCE(SUM(amount),0) FROM transactions WHERE type = :type AND accountId = :acc AND dateTime BETWEEN :start AND :end")
    suspend fun totalByTypeForAccount(type: String, acc: Long, start: Long, end: Long): Double

    @Query("""
        SELECT t.categoryId AS categoryId, c.name AS categoryName, c.colorHex AS colorHex, SUM(t.amount) AS total
        FROM transactions t LEFT JOIN categories c ON c.id = t.categoryId
        WHERE t.type = 'EXPENSE' AND t.accountId = :acc AND t.dateTime BETWEEN :start AND :end
        GROUP BY t.categoryId ORDER BY total DESC
    """)
    suspend fun spendByCategoryForAccount(acc: Long, start: Long, end: Long): List<CategorySpend>

    @Query("SELECT COALESCE(SUM(amount),0) FROM transactions WHERE type = :type AND dateTime BETWEEN :start AND :end")
    suspend fun totalByType(type: String, start: Long, end: Long): Double

    /** Per-currency sums for a type in a range — caller converts each to base. */
    @Query("""
        SELECT currency AS currency, SUM(amount) AS total
        FROM transactions WHERE type = :type AND dateTime BETWEEN :start AND :end
        GROUP BY currency
    """)
    suspend fun totalsByCurrency(type: String, start: Long, end: Long): List<CurrencySum>

    /** Top payees (by note) by expense total in a range (PRD 6.8 merchant tracking). */
    @Query("""
        SELECT note AS payee, SUM(amount) AS total, COUNT(*) AS count
        FROM transactions
        WHERE type = 'EXPENSE' AND note != '' AND dateTime BETWEEN :start AND :end
        GROUP BY note COLLATE NOCASE ORDER BY total DESC LIMIT :limit
    """)
    suspend fun spendByPayee(start: Long, end: Long, limit: Int): List<PayeeSpend>

    /** Recent transactions matching amount+account+type within [since] (duplicate detection, PRD 6.4). */
    @Query("""
        SELECT * FROM transactions
        WHERE type = :type AND accountId = :accountId AND amount = :amount AND dateTime >= :since
        ORDER BY dateTime DESC LIMIT 1
    """)
    suspend fun matchingSince(type: String, accountId: Long, amount: Double, since: Long): TransactionEntity?
}

/** CRUD for per-category budget limits. */
@Dao
interface BudgetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(b: BudgetEntity): Long
    @Delete suspend fun delete(b: BudgetEntity)
    @Query("SELECT * FROM budgets") fun observeAll(): Flow<List<BudgetEntity>>
    @Query("SELECT * FROM budgets WHERE id = :id") suspend fun byId(id: Long): BudgetEntity?
}

/** CRUD for recurring transactions and lookup of those due to run. */
@Dao
interface RecurringDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(r: RecurringTransactionEntity): Long
    @Delete suspend fun delete(r: RecurringTransactionEntity)
    @Query("SELECT * FROM recurring_transactions WHERE active = 1 ORDER BY nextRun") fun observeAll(): Flow<List<RecurringTransactionEntity>>
    @Query("SELECT * FROM recurring_transactions WHERE active = 1 AND nextRun <= :now") suspend fun due(now: Long): List<RecurringTransactionEntity>
}

/** Append-only audit log of transaction create/edit/delete actions. */
@Dao
interface AuditDao {
    @Insert suspend fun insert(a: AuditLogEntity)
    @Query("SELECT * FROM audit_log WHERE transactionId = :txId ORDER BY timestamp DESC") fun observeForTx(txId: Long): Flow<List<AuditLogEntity>>
    @Query("SELECT * FROM audit_log ORDER BY timestamp DESC LIMIT 200") fun observeRecent(): Flow<List<AuditLogEntity>>
}

/** Stored currency conversion rates. Column names `from`/`to` are SQL keywords, hence the backticks. */
@Dao
interface ExchangeRateDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(r: ExchangeRateEntity)
    @Query("SELECT * FROM exchange_rates") fun observeAll(): Flow<List<ExchangeRateEntity>>
    @Query("SELECT rate FROM exchange_rates WHERE `from` = :from AND `to` = :to") suspend fun rate(from: String, to: String): Double?
}

/** CRUD for savings goals / debt trackers. */
@Dao
interface GoalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(g: GoalEntity): Long
    @Delete suspend fun delete(g: GoalEntity)
    @Query("SELECT * FROM goals ORDER BY createdAt DESC") fun observeAll(): Flow<List<GoalEntity>>
    @Query("SELECT * FROM goals WHERE id = :id") suspend fun byId(id: Long): GoalEntity?
    @Query("SELECT COUNT(*) FROM goals") suspend fun count(): Int
}

/** Persisted AI Coach chat history (insert, observe in order, clear). */
@Dao
interface ChatDao {
    @Insert suspend fun insert(m: ChatMessageEntity): Long
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC, id ASC") fun observeAll(): Flow<List<ChatMessageEntity>>
    @Query("DELETE FROM chat_messages") suspend fun clear()
}

/** CRUD for reusable transaction templates. */
@Dao
interface TemplateDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(t: TransactionTemplateEntity): Long
    @Delete suspend fun delete(t: TransactionTemplateEntity)
    @Query("SELECT * FROM tx_templates ORDER BY name") fun observeAll(): Flow<List<TransactionTemplateEntity>>
}
