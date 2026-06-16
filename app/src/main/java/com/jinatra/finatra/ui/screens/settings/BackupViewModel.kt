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

@HiltViewModel
class BackupViewModel @Inject constructor(
    private val repo: FinanceRepository,
) : ViewModel() {

    /** CSV of all transactions (PRD 6.12). */
    suspend fun buildTransactionsCsv(): String = withContext(Dispatchers.IO) {
        val rows = repo.observeTransactions().first()
        val sb = StringBuilder("date,type,amount,currency,category,account,note,tags\n")
        rows.forEach { t ->
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
                val accByName = repo.observeAccounts().first().associateBy { it.name }
                val catByName = repo.observeCategories().first().associateBy { it.name }
                val lines = text.split("\n").map { it.trimEnd('\r') }.filter { it.isNotBlank() }
                lines.drop(1).forEach { line ->
                    val f = com.jinatra.finatra.util.CsvUtil.splitLine(line)
                    if (f.size < 6) { skipped++; return@forEach }
                    val acc = accByName[f[5]]
                    if (acc == null) { skipped++; return@forEach }
                    val type = runCatching { TransactionType.valueOf(f[1].trim()) }.getOrDefault(TransactionType.EXPENSE)
                    val amount = f[2].trim().toDoubleOrNull() ?: run { skipped++; return@forEach }
                    val dateTime = DateUtil.parseFull(f[0]) ?: now
                    repo.addTransaction(
                        TransactionEntity(
                            type = type,
                            amount = amount,
                            currency = f.getOrElse(3) { acc.currency }.ifBlank { acc.currency },
                            dateTime = dateTime,
                            categoryId = catByName[f.getOrElse(4) { "" }]?.id,
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

    fun restoreFromJson(text: String, onDone: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val ok = runCatching {
                val root = JSONObject(text)
                val now = System.currentTimeMillis()
                val accArr = root.optJSONArray("accounts") ?: JSONArray()
                val nameToId = mutableMapOf<String, Long>()
                for (i in 0 until accArr.length()) {
                    val o = accArr.getJSONObject(i)
                    val id = repo.upsertAccount(
                        AccountEntity(
                            name = o.getString("name"),
                            type = runCatching { AccountType.valueOf(o.getString("type")) }.getOrDefault(AccountType.CASH),
                            currency = o.optString("currency", "USD"),
                            openingBalance = o.optDouble("openingBalance", 0.0),
                            colorHex = o.optLong("colorHex", 0xFF0A756C),
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
