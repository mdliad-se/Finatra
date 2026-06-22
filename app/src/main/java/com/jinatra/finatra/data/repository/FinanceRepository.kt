package com.jinatra.finatra.data.repository

import com.jinatra.finatra.data.local.DefaultCategories
import com.jinatra.finatra.data.local.FinatraDatabase
import com.jinatra.finatra.data.local.dao.CategorySpend
import com.jinatra.finatra.data.local.dao.PayeeSpend
import com.jinatra.finatra.data.local.dao.TransactionWithDetails
import com.jinatra.finatra.data.local.entity.AccountEntity
import com.jinatra.finatra.data.local.entity.AuditLogEntity
import com.jinatra.finatra.data.local.entity.BudgetEntity
import com.jinatra.finatra.data.local.entity.CategoryEntity
import com.jinatra.finatra.data.local.entity.ChatMessageEntity
import com.jinatra.finatra.data.local.entity.ChatSessionEntity
import com.jinatra.finatra.data.local.entity.ExchangeRateEntity
import com.jinatra.finatra.data.local.entity.GoalEntity
import com.jinatra.finatra.data.local.entity.RecurrenceFrequency
import com.jinatra.finatra.data.local.entity.RecurringTransactionEntity
import com.jinatra.finatra.data.local.entity.TransactionEntity
import com.jinatra.finatra.data.local.entity.TransactionTemplateEntity
import com.jinatra.finatra.data.local.entity.TransactionType
import com.jinatra.finatra.data.SessionManager
import com.jinatra.finatra.util.DateUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
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

// Fixed palette of ARGB colors auto-assigned to categories that have no explicit color.
private val CategoryPalette = listOf(
    0xFFE57373, 0xFF64B5F6, 0xFF9575CD, 0xFF4DB6AC, 0xFFFFB74D,
    0xFFF06292, 0xFF7986CB, 0xFF4FC3F7, 0xFF81C784, 0xFFFF8A65,
)

// Deterministic palette pick: same name always maps to the same color (abs() guards negative hashCode).
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

/** End-of-month projection from the current spending/earning rate (PRD 6.9 monthly forecast). */
data class MonthlyForecast(
    val projectedIncome: Double,
    val projectedExpense: Double,
    val projectedEndBalance: Double,
    val daysElapsed: Int,
    val daysInMonth: Int,
) {
    val projectedSavings: Double get() = projectedIncome - projectedExpense
}

/** Financial health score 1–100 with its component breakdown (PRD 6.16). */
data class HealthScore(
    val score: Int,
    val savingsRate: Double,     // 0..1 fraction of income saved this month
    val budgetAdherence: Double, // 0..1 (1 = no budget overspent)
) {
    /** Color band per PRD: red <40, amber 40–70, green >70. */
    val status: String get() = when {
        score >= 71 -> "Healthy"
        score >= 40 -> "Fair"
        else -> "Needs attention"
    }
}

/**
 * Central repository over the Room database for all financial data: accounts, categories,
 * transactions, budgets, recurring items, goals, exchange rates, audit log and AI chat history.
 *
 * It also computes derived figures (net worth, monthly series, forecasts, health score) and applies
 * multi-currency conversion via stored manual rates ([convert]).
 *
 * Decoy mode (PRD 6.13): when [SessionManager] reports decoy, reads return empty/zero values (see
 * [gated]) and writes are silently skipped, so the alternate PIN reveals a clean, empty app without
 * touching real data.
 */
@Singleton
class FinanceRepository @Inject constructor(
    private val db: FinatraDatabase,
    private val session: SessionManager,
) {

    private val accounts = db.accountDao()
    private val categories = db.categoryDao()
    private val txns = db.transactionDao()
    private val budgets = db.budgetDao()
    private val recurring = db.recurringDao()
    private val audit = db.auditDao()
    private val rates = db.exchangeRateDao()
    private val goals = db.goalDao()
    private val chat = db.chatDao()
    private val templates = db.templateDao()

    /** Gate a flow behind decoy mode (PRD 6.13): emit [empty] instead of real data when decoy is on. */
    private fun <T> gated(flow: Flow<T>, empty: T): Flow<T> =
        combine(flow, session.decoy) { v, d -> if (d) empty else v }

    // --- Accounts ---
    fun observeAccounts(): Flow<List<AccountEntity>> = gated(accounts.observeAll(), emptyList())
    fun observeAccount(id: Long) = gated(accounts.observeById(id), null)
    fun observeBalance(accountId: Long) = gated(accounts.observeBalance(accountId), 0.0)
    fun observeTotalOpening() = gated(accounts.observeTotalOpening(), 0.0)
    suspend fun upsertAccount(a: AccountEntity) = if (session.isDecoy) -1L else accounts.upsert(a)
    suspend fun deleteAccount(a: AccountEntity) { if (!session.isDecoy) accounts.delete(a) }
    suspend fun accountById(id: Long) = if (session.isDecoy) null else accounts.byId(id)

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
        if (session.isDecoy) return emptyList()
        val cats = categories.allTopLevel().filter { !it.isIncome }
        val start = DateUtil.startOfMonth(DateUtil.plusMonths(now, -(months - 1)))
        val end = DateUtil.endOfMonth(now)
        return cats.mapNotNull { c ->
            val total = txns.spentInCategory(c.id, start, end)
            if (total > 0) CategoryAvgSpend(c.id, c.name, total / months) else null
        }.sortedByDescending { it.avg }
    }

    // --- Transactions ---
    fun observeTransactions(): Flow<List<TransactionWithDetails>> = gated(txns.observeAll(), emptyList())
    fun observeRecent(limit: Int = 8): Flow<List<TransactionWithDetails>> = gated(txns.observeRecent(limit), emptyList())
    fun observeTotalByType(type: String, start: Long, end: Long) = gated(txns.observeTotalByType(type, start, end), 0.0)
    fun observeSpendByCategory(start: Long, end: Long): Flow<List<CategorySpend>> = gated(txns.observeSpendByCategory(start, end), emptyList())

    suspend fun addTransaction(t: TransactionEntity): Long {
        if (session.isDecoy) return -1L
        val id = txns.insert(t)
        audit.insert(AuditLogEntity(transactionId = id, action = "CREATED", timestamp = t.createdAt))
        return id
    }

    suspend fun updateTransaction(t: TransactionEntity) {
        if (session.isDecoy) return
        txns.update(t)
        audit.insert(AuditLogEntity(transactionId = t.id, action = "EDITED", timestamp = t.updatedAt))
    }

    suspend fun deleteTransaction(t: TransactionEntity, at: Long) {
        if (session.isDecoy) return
        audit.insert(AuditLogEntity(transactionId = t.id, action = "DELETED", details = t.note, timestamp = at))
        txns.delete(t)
    }

    suspend fun transactionById(id: Long) = if (session.isDecoy) null else txns.byId(id)

    /** Save one expense split across several categories as linked parts (PRD 6.4). */
    suspend fun addSplit(parts: List<TransactionEntity>): Boolean {
        if (session.isDecoy || parts.isEmpty()) return false
        val groupId = parts.first().createdAt
        parts.forEach { addTransaction(it.copy(splitGroupId = groupId)) }
        return true
    }

    /** A recent transaction matching amount+account+type within [withinHours] — a likely duplicate (PRD 6.4). */
    suspend fun findPossibleDuplicate(
        type: TransactionType, accountId: Long, amount: Double, withinHours: Int = 36,
        now: Long = System.currentTimeMillis(),
    ): TransactionEntity? {
        if (session.isDecoy) return null
        // 3_600_000 ms = 1 hour; look back [withinHours] for a same type/account/amount entry.
        return txns.matchingSince(type.name, accountId, amount, now - withinHours * 3_600_000L)
    }

    // --- Transaction templates (PRD 6.4) ---
    fun observeTemplates(): Flow<List<TransactionTemplateEntity>> = gated(templates.observeAll(), emptyList())
    suspend fun upsertTemplate(t: TransactionTemplateEntity) = if (session.isDecoy) -1L else templates.upsert(t)
    suspend fun deleteTemplate(t: TransactionTemplateEntity) { if (!session.isDecoy) templates.delete(t) }

    /** Top payees/merchants by expense total in a range (PRD 6.8). */
    suspend fun payeeSpend(start: Long, end: Long, limit: Int = 8): List<PayeeSpend> =
        if (session.isDecoy) emptyList() else txns.spendByPayee(start, end, limit)

    // --- Budgets ---
    fun observeBudgets(): Flow<List<BudgetEntity>> = gated(budgets.observeAll(), emptyList())
    suspend fun upsertBudget(b: BudgetEntity) = if (session.isDecoy) -1L else budgets.upsert(b)
    suspend fun deleteBudget(b: BudgetEntity) { if (!session.isDecoy) budgets.delete(b) }
    suspend fun spentInCategory(categoryId: Long, start: Long, end: Long) =
        if (session.isDecoy) 0.0 else txns.spentInCategory(categoryId, start, end)

    /** Category expense over a range, each currency bucket converted to [base] before summing, so a
     *  budget limit (held in the base currency) is compared against correctly across mixed-currency
     *  accounts. */
    suspend fun convertedSpentInCategory(categoryId: Long, start: Long, end: Long, base: String): Double =
        if (session.isDecoy) 0.0
        else txns.spentInCategoryByCurrency(categoryId, start, end).sumOf { convert(it.total, it.currency, base) }

    // --- Recurring ---
    fun observeRecurring(): Flow<List<RecurringTransactionEntity>> = gated(recurring.observeAll(), emptyList())
    suspend fun upsertRecurring(r: RecurringTransactionEntity) = if (session.isDecoy) -1L else recurring.upsert(r)
    suspend fun deleteRecurring(r: RecurringTransactionEntity) { if (!session.isDecoy) recurring.delete(r) }
    suspend fun dueRecurring(now: Long) = recurring.due(now)

    // --- Audit ---
    fun observeAudit() = gated(audit.observeRecent(), emptyList())
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
    suspend fun accountBalance(id: Long): Double = if (session.isDecoy) 0.0 else accounts.balance(id) ?: 0.0

    /** Per-account cards for the Home carousel: balance + this-month flows + expense breakdown. */
    suspend fun accountCards(start: Long, end: Long): List<AccountCard> {
        if (session.isDecoy) return emptyList()
        return accounts.observeAll().first().map { a ->
            AccountCard(
                account = a,
                balance = accountBalance(a.id),
                income = txns.totalByTypeForAccount(TransactionType.INCOME.name, a.id, start, end),
                expense = txns.totalByTypeForAccount(TransactionType.EXPENSE.name, a.id, start, end),
                categorySpend = txns.spendByCategoryForAccount(a.id, start, end),
            )
        }
    }
    suspend fun totalByType(type: String, start: Long, end: Long): Double =
        if (session.isDecoy) 0.0 else txns.totalByType(type, start, end)

    /** Net worth across all accounts, each balance converted to [base]. */
    suspend fun convertedNetWorth(base: String): Double {
        if (session.isDecoy) return 0.0
        return accounts.observeAll().first().sumOf { a -> convert(accountBalance(a.id), a.currency, base) }
    }

    /** Sum of a transaction type in a range, each currency bucket converted to [base]. */
    suspend fun convertedTotalByType(type: String, start: Long, end: Long, base: String): Double {
        if (session.isDecoy) return 0.0
        return txns.totalsByCurrency(type, start, end).sumOf { convert(it.total, it.currency, base) }
    }

    /** Budgets currently over their limit (this month for MONTHLY, else their window). */
    suspend fun overspentBudgets(now: Long = System.currentTimeMillis()): List<BudgetAlert> {
        if (session.isDecoy) return emptyList()
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
        if (session.isDecoy) emptyList() else accounts.observeAll().first()
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

    // --- Goals / debt tracker (PRD 6.9) ---
    fun observeGoals(): Flow<List<GoalEntity>> = gated(goals.observeAll(), emptyList())
    suspend fun upsertGoal(g: GoalEntity) = if (session.isDecoy) -1L else goals.upsert(g)
    suspend fun deleteGoal(g: GoalEntity) { if (!session.isDecoy) goals.delete(g) }
    suspend fun goalById(id: Long) = if (session.isDecoy) null else goals.byId(id)
    suspend fun goalCount() = goals.count()

    // --- Chat sessions + history (PRD 6.11) ---
    /** Conversations of one [kind] ("coach" / "budget"), most-recently-updated first. */
    fun observeChatSessions(kind: String): Flow<List<ChatSessionEntity>> = gated(chat.observeSessions(kind), emptyList())
    /** Ordered messages of one conversation. */
    fun observeChatMessages(sessionId: Long): Flow<List<ChatMessageEntity>> = gated(chat.observeBySession(sessionId), emptyList())

    suspend fun createChatSession(kind: String, title: String, at: Long = System.currentTimeMillis()): Long =
        if (session.isDecoy) -1L
        else chat.insertSession(ChatSessionEntity(kind = kind, title = title, createdAt = at, updatedAt = at))

    /** Resume the most recent [kind] session, or create a fresh one titled [newTitle]. */
    suspend fun latestOrNewChatSession(kind: String, newTitle: String): Long {
        if (session.isDecoy) return -1L
        return chat.latestSession(kind)?.id ?: createChatSession(kind, newTitle)
    }

    /** Append a message to [sessionId] and bump the session's updatedAt for ordering. */
    suspend fun addChatMessage(sessionId: Long, role: String, content: String, at: Long = System.currentTimeMillis()): Long {
        if (session.isDecoy || sessionId <= 0) return -1L
        val id = chat.insert(ChatMessageEntity(sessionId = sessionId, role = role, content = content, timestamp = at))
        chat.touchSession(sessionId, at)
        return id
    }

    suspend fun renameChatSession(id: Long, title: String, at: Long = System.currentTimeMillis()) {
        if (!session.isDecoy && id > 0) chat.renameSession(id, title, at)
    }
    suspend fun deleteChatSession(id: Long) { if (!session.isDecoy && id > 0) chat.deleteSession(id) }
    suspend fun clearChatSession(id: Long) { if (!session.isDecoy && id > 0) chat.clearSession(id) }

    /** Financial health score (PRD 6.16): savings rate + budget adherence, base currency. */
    suspend fun financeHealth(base: String, now: Long = System.currentTimeMillis()): HealthScore {
        if (session.isDecoy) return HealthScore(0, 0.0, 0.0)
        val start = DateUtil.startOfMonth(now)
        val end = DateUtil.endOfMonth(now)
        val income = convertedTotalByType(TransactionType.INCOME.name, start, end, base)
        val expense = convertedTotalByType(TransactionType.EXPENSE.name, start, end, base)
        val savingsRate = if (income > 0) ((income - expense) / income).coerceIn(0.0, 1.0) else 0.0

        val allBudgets = budgets.observeAll().first()
        val adherence = if (allBudgets.isEmpty()) 0.7 else {
            val over = overspentBudgets(now).size
            (1.0 - over.toDouble() / allBudgets.size).coerceIn(0.0, 1.0)
        }
        val score = (savingsRate * 50 + adherence * 50).toInt().coerceIn(1, 100)
        return HealthScore(score, savingsRate, adherence)
    }

    /** Project end-of-month income/expense/balance by extrapolating the month-to-date rate (PRD 6.9). */
    suspend fun monthlyForecast(base: String, now: Long = System.currentTimeMillis()): MonthlyForecast {
        val start = DateUtil.startOfMonth(now)
        val end = DateUtil.endOfMonth(now)
        val cal = java.util.Calendar.getInstance().apply { timeInMillis = now }
        val daysInMonth = cal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
        val daysElapsed = cal.get(java.util.Calendar.DAY_OF_MONTH).coerceAtLeast(1)
        if (session.isDecoy) return MonthlyForecast(0.0, 0.0, 0.0, daysElapsed, daysInMonth)

        val incomeSoFar = convertedTotalByType(TransactionType.INCOME.name, start, end, base)
        val expenseSoFar = convertedTotalByType(TransactionType.EXPENSE.name, start, end, base)
        val factor = daysInMonth.toDouble() / daysElapsed
        val projIncome = incomeSoFar * factor
        val projExpense = expenseSoFar * factor
        val netWorth = convertedNetWorth(base)
        // Net worth already reflects month-to-date flows; add only the remaining projected delta.
        val remaining = (projIncome - incomeSoFar) - (projExpense - expenseSoFar)
        return MonthlyForecast(projIncome, projExpense, netWorth + remaining, daysElapsed, daysInMonth)
    }

    /** Distinct start-of-day epochs that have at least one transaction (PRD 6.15 calendar dots). */
    suspend fun transactionDays(start: Long, end: Long): Set<Long> =
        if (session.isDecoy) emptySet() else txns.observeAll().first()
            .filter { it.dateTime in start..end }
            .map { DateUtil.startOfDay(it.dateTime) }
            .toSet()

    /** Next run time after [from] for the given [freq]; CUSTOM steps by [intervalDays] (min 1 day). */
    private fun advance(from: Long, freq: RecurrenceFrequency, intervalDays: Int): Long = when (freq) {
        RecurrenceFrequency.DAILY -> DateUtil.plusDays(from, 1)
        RecurrenceFrequency.WEEKLY -> DateUtil.plusDays(from, 7)
        RecurrenceFrequency.MONTHLY -> DateUtil.plusMonths(from, 1)
        RecurrenceFrequency.CUSTOM -> DateUtil.plusDays(from, intervalDays.coerceAtLeast(1))
    }
}
