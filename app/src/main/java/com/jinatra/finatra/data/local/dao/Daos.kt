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
import com.jinatra.finatra.data.local.entity.ExchangeRateEntity
import com.jinatra.finatra.data.local.entity.RecurringTransactionEntity
import com.jinatra.finatra.data.local.entity.TransactionEntity
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

data class CategorySpend(
    val categoryId: Long?,
    val categoryName: String?,
    val colorHex: Long?,
    val total: Double,
)

data class CurrencySum(
    val currency: String,
    val total: Double,
)

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
}

@Dao
interface BudgetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(b: BudgetEntity): Long
    @Delete suspend fun delete(b: BudgetEntity)
    @Query("SELECT * FROM budgets") fun observeAll(): Flow<List<BudgetEntity>>
    @Query("SELECT * FROM budgets WHERE id = :id") suspend fun byId(id: Long): BudgetEntity?
}

@Dao
interface RecurringDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(r: RecurringTransactionEntity): Long
    @Delete suspend fun delete(r: RecurringTransactionEntity)
    @Query("SELECT * FROM recurring_transactions WHERE active = 1 ORDER BY nextRun") fun observeAll(): Flow<List<RecurringTransactionEntity>>
    @Query("SELECT * FROM recurring_transactions WHERE active = 1 AND nextRun <= :now") suspend fun due(now: Long): List<RecurringTransactionEntity>
}

@Dao
interface AuditDao {
    @Insert suspend fun insert(a: AuditLogEntity)
    @Query("SELECT * FROM audit_log WHERE transactionId = :txId ORDER BY timestamp DESC") fun observeForTx(txId: Long): Flow<List<AuditLogEntity>>
    @Query("SELECT * FROM audit_log ORDER BY timestamp DESC LIMIT 200") fun observeRecent(): Flow<List<AuditLogEntity>>
}

@Dao
interface ExchangeRateDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(r: ExchangeRateEntity)
    @Query("SELECT * FROM exchange_rates") fun observeAll(): Flow<List<ExchangeRateEntity>>
    @Query("SELECT rate FROM exchange_rates WHERE `from` = :from AND `to` = :to") suspend fun rate(from: String, to: String): Double?
}
