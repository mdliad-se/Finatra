package com.jinatra.finatra.data.repository

import com.jinatra.finatra.data.local.DefaultCategories
import com.jinatra.finatra.data.local.FinatraDatabase
import com.jinatra.finatra.data.local.dao.CategorySpend
import com.jinatra.finatra.data.local.dao.TransactionWithDetails
import com.jinatra.finatra.data.local.entity.AccountEntity
import com.jinatra.finatra.data.local.entity.AuditLogEntity
import com.jinatra.finatra.data.local.entity.BudgetEntity
import com.jinatra.finatra.data.local.entity.CategoryEntity
import com.jinatra.finatra.data.local.entity.ExchangeRateEntity
import com.jinatra.finatra.data.local.entity.RecurrenceFrequency
import com.jinatra.finatra.data.local.entity.RecurringTransactionEntity
import com.jinatra.finatra.data.local.entity.TransactionEntity
import com.jinatra.finatra.data.local.entity.TransactionType
import com.jinatra.finatra.util.DateUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/** A budget whose spend has exceeded its limit (for overspend alerts). */
data class BudgetAlert(val categoryName: String, val limit: Double, val spent: Double, val currency: String)

/** Result of processing due recurring entries in a maintenance pass. */
data class RecurringOutcome(
    val autoLogged: List<RecurringTransactionEntity> = emptyList(),
    val reminders: List<RecurringTransactionEntity> = emptyList(),
)

/** Average monthly spend for one category (AI budget recommendations). */
data class CategoryAvgSpend(val categoryId: Long, val name: String, val avg: Double)

/** Per-account snapshot for the Home account carousel (balance + this-month flows + pie). */
data class AccountCard(
    val account: AccountEntity,
    val balance: Double,
    val income: Double,
    val expense: Double,
    val categorySpend: List<CategorySpend>,
)

private val CategoryPalette = listOf(
    0xFFE57373, 0xFF64B5F6, 0xFF9575CD, 0xFF4DB6AC, 0xFFFFB74D,
    0xFFF06292, 0xFF7986CB, 0xFF4FC3F7, 0xFF81C784, 0xFFFF8A65,
)

private fun colorForName(name: String): Long =
    CategoryPalette[(kotlin.math.abs(name.hashCode())) % CategoryPalette.size]

/** One month of aggregated, base-currency figures for charts & reports. */
data class MonthPoint(
    val startMillis: Long,
    val label: String,
    val income: Double,
    val expense: Double,
    val netWorthEnd: Double,
)

@Singleton
class FinanceRepository @Inject constructor(private val db: FinatraDatabase) {

    private val accounts = db.accountDao()
    private val categories = db.categoryDao()
    private val txns = db.transactionDao()
    private val budgets = db.budgetDao()
    private val recurring = db.recurringDao()
    private val audit = db.auditDao()
    private val rates = db.exchangeRateDao()

    // --- Accounts ---
    fun observeAccounts(): Flow<List<AccountEntity>> = accounts.observeAll()
    fun observeAccount(id: Long) = accounts.observeById(id)
    fun observeBalance(accountId: Long) = accounts.observeBalance(accountId)
    fun observeTotalOpening() = accounts.observeTotalOpening()
    suspend fun upsertAccount(a: AccountEntity) = accounts.upsert(a)
    suspend fun deleteAccount(a: AccountEntity) = accounts.delete(a)
    suspend fun accountById(id: Long) = accounts.byId(id)

    // --- Categories ---
    fun observeCategories(): Flow<List<CategoryEntity>> = categories.observeAll()
    suspend fun upsertCategory(c: CategoryEntity) = categories.upsert(c)
    suspend fun deleteCategory(c: CategoryEntity) = categories.delete(c)

    /** Seed predefined categories once (no-op if any exist). */
    suspend fun seedCategoriesIfEmpty() {
        if (categories.count() == 0) categories.insertAll(DefaultCategories.list)
    }

    suspend fun categoriesNow(): List<CategoryEntity> = categories.observeAll().first()

    /** Find a top-level category by name (case-insensitive) or create it. Returns its id. */
    suspend fun findOrCreateCategory(name: String, isIncome: Boolean): Long {
        val clean = name.trim()
        if (clean.isEmpty()) return -1L
        categories.byName(clean, isIncome)?.let { return it.id }
        // Fuzzy: existing whose name contains or is contained by the requested name.
        categories.allTopLevel().firstOrNull {
            it.isIncome == isIncome &&
                (it.name.contains(clean, true) || clean.contains(it.name, true))
        }?.let { return it.id }
        return categories.upsert(
            CategoryEntity(
                name = clean.replaceFirstChar { it.uppercase() },
                colorHex = colorForName(clean),
                iconKey = if (isIncome) "payments" else "category",
                isIncome = isIncome,
                isCustom = true,
            )
        )
    }

    /** Average monthly spend per expense category over the last [months] months. */
    suspend fun avgMonthlySpend(months: Int, now: Long = System.currentTimeMillis()): List<CategoryAvgSpend> {
        val cats = categories.allTopLevel().filter { !it.isIncome }
        val start = DateUtil.startOfMonth(DateUtil.plusMonths(now, -(months - 1)))
        val end = DateUtil.endOfMonth(now)
        return cats.mapNotNull { c ->
            val total = txns.spentInCategory(c.id, start, end)
            if (total > 0) CategoryAvgSpend(c.id, c.name, total / months) else null
        }.sortedByDescending { it.avg }
    }

    // --- Transactions ---
    fun observeTransactions(): Flow<List<TransactionWithDetails>> = txns.observeAll()
    fun observeRecent(limit: Int = 8): Flow<List<TransactionWithDetails>> = txns.observeRecent(limit)
    fun observeTotalByType(type: String, start: Long, end: Long) = txns.observeTotalByType(type, start, end)
    fun observeSpendByCategory(start: Long, end: Long): Flow<List<CategorySpend>> = txns.observeSpendByCategory(start, end)

    suspend fun addTransaction(t: TransactionEntity): Long {
        val id = txns.insert(t)
        audit.insert(AuditLogEntity(transactionId = id, action = "CREATED", timestamp = t.createdAt))
        return id
    }

    suspend fun updateTransaction(t: TransactionEntity) {
        txns.update(t)
        audit.insert(AuditLogEntity(transactionId = t.id, action = "EDITED", timestamp = t.updatedAt))
    }

    suspend fun deleteTransaction(t: TransactionEntity, at: Long) {
        audit.insert(AuditLogEntity(transactionId = t.id, action = "DELETED", details = t.note, timestamp = at))
        txns.delete(t)
    }

    suspend fun transactionById(id: Long) = txns.byId(id)

    // --- Budgets ---
    fun observeBudgets(): Flow<List<BudgetEntity>> = budgets.observeAll()
    suspend fun upsertBudget(b: BudgetEntity) = budgets.upsert(b)
    suspend fun deleteBudget(b: BudgetEntity) = budgets.delete(b)
    suspend fun spentInCategory(categoryId: Long, start: Long, end: Long) =
        txns.spentInCategory(categoryId, start, end)

    // --- Recurring ---
    fun observeRecurring(): Flow<List<RecurringTransactionEntity>> = recurring.observeAll()
    suspend fun upsertRecurring(r: RecurringTransactionEntity) = recurring.upsert(r)
    suspend fun deleteRecurring(r: RecurringTransactionEntity) = recurring.delete(r)
    suspend fun dueRecurring(now: Long) = recurring.due(now)

    // --- Audit ---
    fun observeAudit() = audit.observeRecent()
    fun observeAuditFor(txId: Long) = audit.observeForTx(txId)

    // --- Exchange rates / multi-currency ---
    fun observeRates(): Flow<List<ExchangeRateEntity>> = rates.observeAll()
    suspend fun upsertRate(r: ExchangeRateEntity) = rates.upsert(r)

    /** Convert amount [from] -> [to] using stored manual rate; returns amount unchanged if same or unknown. */
    suspend fun convert(amount: Double, from: String, to: String): Double {
        if (from == to) return amount
        rates.rate(from, to)?.let { return amount * it }
        rates.rate(to, from)?.let { return amount / it }
        return amount // no rate set — caller may surface a hint to add one
    }

    // --- One-shot aggregates / conversions (dashboard + workers) ---
    suspend fun accountBalance(id: Long): Double = accounts.balance(id) ?: 0.0

    /** Per-account cards for the Home carousel: balance + this-month flows + expense breakdown. */
    suspend fun accountCards(start: Long, end: Long): List<AccountCard> =
        accounts.observeAll().first().map { a ->
            AccountCard(
                account = a,
                balance = accountBalance(a.id),
                income = txns.totalByTypeForAccount(TransactionType.INCOME.name, a.id, start, end),
                expense = txns.totalByTypeForAccount(TransactionType.EXPENSE.name, a.id, start, end),
                categorySpend = txns.spendByCategoryForAccount(a.id, start, end),
            )
        }
    suspend fun totalByType(type: String, start: Long, end: Long): Double = txns.totalByType(type, start, end)

    /** Net worth across all accounts, each balance converted to [base]. */
    suspend fun convertedNetWorth(base: String): Double =
        accounts.observeAll().first().sumOf { a -> convert(accountBalance(a.id), a.currency, base) }

    /** Sum of a transaction type in a range, each currency bucket converted to [base]. */
    suspend fun convertedTotalByType(type: String, start: Long, end: Long, base: String): Double =
        txns.totalsByCurrency(type, start, end).sumOf { convert(it.total, it.currency, base) }

    /** Budgets currently over their limit (this month for MONTHLY, else their window). */
    suspend fun overspentBudgets(now: Long = System.currentTimeMillis()): List<BudgetAlert> {
        val cats = categories.observeAll().first()
        return budgets.observeAll().first().mapNotNull { b ->
            val (start, end) = if (b.period == com.jinatra.finatra.data.local.entity.BudgetPeriod.MONTHLY)
                DateUtil.startOfMonth(now) to DateUtil.endOfMonth(now)
            else b.startDate to (b.endDate ?: DateUtil.endOfMonth(now))
            val spent = txns.spentInCategory(b.categoryId, start, end)
            if (spent > b.amount && b.amount > 0) {
                val name = cats.firstOrNull { it.id == b.categoryId }?.name ?: "Category"
                BudgetAlert(name, b.amount, spent, "")
            } else null
        }
    }

    /** Active accounts whose live balance is below their configured threshold. */
    suspend fun accountsBelowThreshold(): List<Pair<AccountEntity, Double>> =
        accounts.observeAll().first()
            .filter { it.lowBalanceThreshold > 0.0 }
            .mapNotNull { a ->
                val bal = accountBalance(a.id)
                if (bal < a.lowBalanceThreshold) a to bal else null
            }

    /** Process due recurring entries: auto-log those flagged, collect the rest as reminders. */
    suspend fun processDueRecurring(now: Long = System.currentTimeMillis()): RecurringOutcome {
        val due = recurring.due(now)
        val logged = mutableListOf<RecurringTransactionEntity>()
        val reminders = mutableListOf<RecurringTransactionEntity>()
        due.forEach { r ->
            if (r.autoLog) {
                addTransaction(
                    TransactionEntity(
                        type = r.type, amount = r.amount, currency = r.currency,
                        dateTime = r.nextRun, categoryId = r.categoryId, accountId = r.accountId,
                        transferToAccountId = r.transferToAccountId, note = r.note,
                        createdAt = now, updatedAt = now,
                    )
                )
                logged += r
            } else {
                reminders += r
            }
            recurring.upsert(r.copy(nextRun = advance(r.nextRun, r.frequency, r.intervalDays)))
        }
        return RecurringOutcome(logged, reminders)
    }

    /** Last [months] months of income/expense + running net worth, all converted to [base].
     *  Net worth is anchored to the current live value and walked backwards by monthly deltas. */
    suspend fun monthlySeries(months: Int, base: String, now: Long = System.currentTimeMillis()): List<MonthPoint> {
        data class Bucket(val start: Long, val end: Long, val label: String, val income: Double, val expense: Double)
        val buckets = (months - 1 downTo 0).map { back ->
            val anchor = DateUtil.plusMonths(now, -back)
            val start = DateUtil.startOfMonth(anchor)
            val end = DateUtil.endOfMonth(anchor)
            Bucket(
                start, end, DateUtil.shortMonth(start),
                convertedTotalByType(TransactionType.INCOME.name, start, end, base),
                convertedTotalByType(TransactionType.EXPENSE.name, start, end, base),
            )
        }
        // Walk net worth backwards from the live value so the latest point is exact.
        var running = convertedNetWorth(base)
        val nets = DoubleArray(buckets.size)
        for (i in buckets.indices.reversed()) {
            nets[i] = running
            running -= (buckets[i].income - buckets[i].expense)
        }
        return buckets.mapIndexed { i, b -> MonthPoint(b.start, b.label, b.income, b.expense, nets[i]) }
    }

    private fun advance(from: Long, freq: RecurrenceFrequency, intervalDays: Int): Long = when (freq) {
        RecurrenceFrequency.DAILY -> DateUtil.plusDays(from, 1)
        RecurrenceFrequency.WEEKLY -> DateUtil.plusDays(from, 7)
        RecurrenceFrequency.MONTHLY -> DateUtil.plusMonths(from, 1)
        RecurrenceFrequency.CUSTOM -> DateUtil.plusDays(from, intervalDays.coerceAtLeast(1))
    }
}
