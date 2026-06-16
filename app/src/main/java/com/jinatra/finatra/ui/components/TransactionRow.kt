package com.jinatra.finatra.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jinatra.finatra.data.local.dao.TransactionWithDetails
import com.jinatra.finatra.data.local.entity.TransactionType
import com.jinatra.finatra.ui.theme.FinatraTheme
import com.jinatra.finatra.util.CategoryIcons
import com.jinatra.finatra.util.DateUtil
import com.jinatra.finatra.util.Money

@Composable
fun TransactionRow(tx: TransactionWithDetails, onClick: () -> Unit) {
    val type = runCatching { TransactionType.valueOf(tx.type) }.getOrNull()
    val isExpense = type == TransactionType.EXPENSE
    val isTransfer = type == TransactionType.TRANSFER
    val amountColor = when {
        isTransfer -> MaterialTheme.colorScheme.onSurface
        isExpense -> FinatraTheme.expense
        else -> FinatraTheme.income
    }
    val iconBg = tx.categoryColor?.let { Color(it) } ?: MaterialTheme.colorScheme.primary

    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(42.dp).clip(CircleShape).background(iconBg.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (isTransfer) Icons.AutoMirrored.Filled.CompareArrows
                else CategoryIcons.forKey(tx.categoryIcon ?: ""),
                contentDescription = null,
                tint = iconBg,
            )
        }
        Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
            Text(
                text = tx.note.ifBlank { tx.categoryName ?: if (isTransfer) "Transfer" else "Transaction" },
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
            )
            Text(
                text = "${tx.categoryName ?: tx.accountName ?: ""} · ${DateUtil.day(tx.dateTime)}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = if (isTransfer) Money.format(tx.amount, tx.currency)
            else Money.formatSigned(tx.amount, tx.currency, isExpense),
            style = MaterialTheme.typography.titleMedium,
            color = amountColor,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
