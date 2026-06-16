package com.jinatra.finatra.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalSize
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.LinearProgressIndicator
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jinatra.finatra.data.local.entity.TransactionType
import com.jinatra.finatra.data.repository.FinanceRepository
import com.jinatra.finatra.util.DateUtil
import com.jinatra.finatra.util.Money
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun repository(): FinanceRepository
}

class BalanceWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: androidx.glance.GlanceId) {
        val repo = EntryPointAccessors.fromApplication(context, WidgetEntryPoint::class.java).repository()

        val opening = repo.observeTotalOpening().first()
        val income = repo.observeTotalByType(TransactionType.INCOME.name, 0, Long.MAX_VALUE).first()
        val expense = repo.observeTotalByType(TransactionType.EXPENSE.name, 0, Long.MAX_VALUE).first()
        val dayStart = DateUtil.startOfDay()
        val todaySpend = repo.observeTotalByType(TransactionType.EXPENSE.name, dayStart, System.currentTimeMillis()).first()
        val netWorth = opening + income - expense

        val monthStart = DateUtil.startOfMonth()
        val monthEnd = DateUtil.endOfMonth()
        val budgetList = repo.observeBudgets().first()
        val totalBudget = budgetList.sumOf { it.amount }
        val totalSpent = budgetList.sumOf { repo.spentInCategory(it.categoryId, monthStart, monthEnd) }
        val budgetFraction = if (totalBudget > 0) (totalSpent / totalBudget).toFloat().coerceIn(0f, 1f) else -1f

        provideContent {
            GlanceTheme {
                WidgetContent(netWorth, todaySpend, budgetFraction)
            }
        }
    }

    @Composable
    private fun WidgetContent(netWorth: Double, todaySpend: Double, budgetFraction: Float) {
        val showBudget = LocalSize.current.height >= MEDIUM_HEIGHT
        // Jinatra brand warmth as base; Material You dynamic color via GlanceTheme on API 31+.
        Column(
            modifier = GlanceModifier.fillMaxSize()
                .background(Color(0xFFFFEACF))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.Start,
        ) {
            Text("Finatra", style = TextStyle(color = ColorProvider(Color(0xFF0A756C)), fontSize = 12.sp, fontWeight = FontWeight.Medium))
            Text(
                Money.format(netWorth, "USD"),
                style = TextStyle(color = ColorProvider(Color(0xFF1A1A1A)), fontSize = 22.sp, fontWeight = FontWeight.Bold),
            )
            Text(
                "Today: ${Money.format(todaySpend, "USD")}",
                style = TextStyle(color = ColorProvider(Color(0xFF0A756C)), fontSize = 12.sp),
            )
            if (showBudget) {
                Spacer(GlanceModifier.height(8.dp))
                if (budgetFraction < 0f) {
                    Text("No budgets", style = TextStyle(color = ColorProvider(Color(0xFF0A756C)), fontSize = 12.sp))
                } else {
                    Text(
                        "Budgets ${(budgetFraction * 100).toInt()}%",
                        style = TextStyle(color = ColorProvider(Color(0xFF0A756C)), fontSize = 12.sp, fontWeight = FontWeight.Medium),
                    )
                    Spacer(GlanceModifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = budgetFraction,
                        modifier = GlanceModifier.fillMaxWidth(),
                        color = ColorProvider(Color(0xFF0A756C)),
                        backgroundColor = ColorProvider(Color(0x330A756C)),
                    )
                }
            }
        }
    }

    companion object {
        private val MEDIUM_HEIGHT = 110.dp
    }
}

class BalanceWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = BalanceWidget()
}
