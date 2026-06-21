package com.jinatra.finatra.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jinatra.finatra.data.local.entity.AccountEntity
import com.jinatra.finatra.data.local.entity.AccountType
import com.jinatra.finatra.data.local.entity.TransactionEntity
import com.jinatra.finatra.data.local.entity.TransactionType
import com.jinatra.finatra.data.repository.FinanceRepository
import com.jinatra.finatra.util.DateUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

/**
 * ViewModel for the Backup & data screen.
 *
 * Provides fully on-device (no network) data portability:
 *  - CSV export of all transactions and a blank import template.
 *  - A versioned JSON backup of accounts + transactions.
 *  - CSV import and JSON restore, which reconcile rows against existing accounts/categories.
 *
 * Heavy work (DB reads, parsing, writes) runs on [Dispatchers.IO]; completion callbacks
 * are marshalled back to the main thread for the caller's UI feedback.
 */
@HiltViewModel
class BackupViewModel @Inject constructor(
    private val repo: FinanceRepository,
) : ViewModel() {

    /** Blank import template: header + one example row (PRD 6.18). */
    fun csvTemplate(): String =
        "date,type,amount,currency,category,account,note,tags\n" +
            "\"1 Jan 2026, 9:00 AM\",\"EXPENSE\",\"500\",\"BDT\",\"Food\",\"Cash\",\"Lunch\",\"work\"\n"

    /** CSV of all transactions (PRD 6.12). */
    suspend fun buildTransactionsCsv(): String = withContext(Dispatchers.IO) {
        val rows = repo.observeTransactions().first()
        val sb = StringBuilder("date,type,amount,currency,category,account,note,tags\n")
        rows.forEach { t ->
            // Each field is double-quoted and embedded quotes are doubled ("" ) per RFC 4180.
            sb.append(
                listOf(
                    DateUtil.full(t.dateTime), t.type, t.amount.toString(), t.currency,
                    t.categoryName ?: "", t.accountName ?: "", t.note, t.tags,
                ).joinToString(",") { "\"${it.replace("\"", "\"\"")}\"" }
            ).append("\n")
        }
        sb.toString()
    }

    /** Full local JSON backup of accounts + transactions. */
    suspend fun buildBackupJson(): String = withContext(Dispatchers.IO) {
        val accounts = repo.observeAccounts().first()
        val txns = repo.observeTransactions().first()
        val root = JSONObject()
        // "version" lets restore detect/upgrade older backup formats in future.
        root.put("version", 1)
        root.put("accounts", JSONArray().apply {
            accounts.forEach { a ->
                put(JSONObject().apply {
                    put("name", a.name); put("type", a.type.name); put("currency", a.currency)
                    put("openingBalance", a.openingBalance); put("colorHex", a.colorHex)
                })
            }
        })
        root.put("transactions", JSONArray().apply {
            txns.forEach { t ->
                put(JSONObject().apply {
                    put("type", t.type); put("amount", t.amount); put("currency", t.currency)
                    put("dateTime", t.dateTime); put("note", t.note); put("tags", t.tags)
                    put("account", t.accountName ?: "")
                })
            }
        })
        root.toString(2)
    }

    /** Import transactions from a CSV matching the export header (PRD 6.12).
     *  Columns: date,type,amount,currency,category,account,note,tags.
     *  Rows whose account name doesn't match an existing account are skipped. */
    fun importCsv(text: String, onDone: (imported: Int, skipped: Int) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            var imported = 0
            var skipped = 0
            runCatching {
                val now = System.currentTimeMillis()
                val accByName = repo.observeAccounts().first().associateBy { it.name.lowercase() }
                val lines = text.split("\n").map { it.trimEnd('\r') }.filter { it.isNotBlank() }
                lines.drop(1).forEach { line ->
                    val f = com.jinatra.finatra.util.CsvUtil.splitLine(line)
                    if (f.size < 6) { skipped++; return@forEach }
                    val acc = accByName[f[5].trim().lowercase()]
                    if (acc == null) { skipped++; return@forEach }
                    val type = runCatching { TransactionType.valueOf(f[1].trim().uppercase()) }.getOrDefault(TransactionType.EXPENSE)
                    val amount = f[2].trim().toDoubleOrNull() ?: run { skipped++; return@forEach }
                    val dateTime = DateUtil.parseFull(f[0]) ?: now
                    // Resolve category by name, creating it if new (unless transfer).
                    val catName = f.getOrElse(4) { "" }.trim()
                    val categoryId = if (catName.isNotBlank() && type != TransactionType.TRANSFER)
                        repo.findOrCreateCategory(catName, type == TransactionType.INCOME).takeIf { it > 0 } else null
                    repo.addTransaction(
                        TransactionEntity(
                            type = type,
                            amount = amount,
                            currency = f.getOrElse(3) { acc.currency }.ifBlank { acc.currency },
                            dateTime = dateTime,
                            categoryId = categoryId,
                            accountId = acc.id,
                            note = f.getOrElse(6) { "" },
                            tags = f.getOrElse(7) { "" },
                            createdAt = now, updatedAt = now,
                        )
                    )
                    imported++
                }
            }
            withContext(Dispatchers.Main) { onDone(imported, skipped) }
        }
    }

    /**
     * Restores accounts and transactions from a JSON backup produced by [buildBackupJson].
     *
     * Accounts are upserted first and their generated ids tracked by name; transactions are
     * then re-linked to those ids by account name. Transactions referencing an unknown
     * account are skipped. The whole operation is best-effort: any parse/DB failure yields
     * `onDone(false)`.
     */
    fun restoreFromJson(text: String, onDone: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val ok = runCatching {
                val root = JSONObject(text)
                val now = System.currentTimeMillis()
                val accArr = root.optJSONArray("accounts") ?: JSONArray()
                // Maps each restored account name to its new row id, used to re-link transactions.
                val nameToId = mutableMapOf<String, Long>()
                for (i in 0 until accArr.length()) {
                    val o = accArr.getJSONObject(i)
                    val id = repo.upsertAccount(
                        AccountEntity(
                            name = o.getString("name"),
                            type = runCatching { AccountType.valueOf(o.getString("type")) }.getOrDefault(AccountType.CASH),
                            currency = o.optString("currency", "USD"),
                            openingBalance = o.optDouble("openingBalance", 0.0),
                            colorHex = o.optLong("colorHex", 0xFFE05454),
                            createdAt = now,
                        )
                    )
                    nameToId[o.getString("name")] = id
                }
                val txArr = root.optJSONArray("transactions") ?: JSONArray()
                for (i in 0 until txArr.length()) {
                    val o = txArr.getJSONObject(i)
                    val accId = nameToId[o.optString("account")] ?: continue
                    repo.addTransaction(
                        TransactionEntity(
                            type = runCatching { TransactionType.valueOf(o.getString("type")) }.getOrDefault(TransactionType.EXPENSE),
                            amount = o.optDouble("amount", 0.0),
                            currency = o.optString("currency", "USD"),
                            dateTime = o.optLong("dateTime", now),
                            accountId = accId,
                            note = o.optString("note", ""),
                            tags = o.optString("tags", ""),
                            createdAt = now, updatedAt = now,
                        )
                    )
                }
                true
            }.getOrDefault(false)
            withContext(Dispatchers.Main) { onDone(ok) }
        }
    }
}
