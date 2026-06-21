package com.jinatra.finatra.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.LinearProgressIndicator
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
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
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jinatra.finatra.MainActivity
import com.jinatra.finatra.data.local.entity.TransactionType
import com.jinatra.finatra.data.prefs.SettingsRepository
import com.jinatra.finatra.data.repository.FinanceRepository
import com.jinatra.finatra.util.DateUtil
import com.jinatra.finatra.util.Money
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first

/**
 * Hilt entry point that exposes singleton-scoped dependencies to the widget.
 *
 * Glance widgets are not constructed by Hilt, so they pull the [FinanceRepository] and
 * [SettingsRepository] from the application graph at render time via [EntryPointAccessors].
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun repository(): FinanceRepository
    fun settings(): SettingsRepository
}

/**
 * Home-screen Glance app widget showing net worth, today's spend, and (when large enough) a
 * monthly budget progress bar. Tapping the widget opens [MainActivity].
 *
 * Uses a responsive size mode: the SMALL layout shows balance + today's spend, while MEDIUM
 * additionally renders the budget bar (PRD 6.19).
 */
class BalanceWidget : GlanceAppWidget() {

    // Small = balance + today; medium = also budget bar (PRD 6.19).
    override val sizeMode = SizeMode.Responsive(setOf(SMALL, MEDIUM))

    /**
     * Loads the latest finance figures and renders the widget content. Called by Glance whenever
     * the widget needs to (re)draw, so each call reads a fresh snapshot from the repository flows.
     */
    override suspend fun provideGlance(context: Context, id: androidx.glance.GlanceId) {
        // Glance can't be Hilt-injected, so resolve dependencies from the application entry point.
        val entry = EntryPointAccessors.fromApplication(context, WidgetEntryPoint::class.java)
        val repo = entry.repository()
        val currency = entry.settings().settings.first().baseCurrency

        // Snapshot the current totals (first() takes one emission from each flow).
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
        // Fraction of total budget spent this month, clamped to 0..1; -1 signals "no budgets set".
        val budgetFraction = if (totalBudget > 0) (totalSpent / totalBudget).toFloat().coerceIn(0f, 1f) else -1f

        provideContent {
            GlanceTheme {
                WidgetContent(netWorth, todaySpend, budgetFraction, currency)
            }
        }
    }

    /**
     * Renders the widget body. The budget section is shown only when the widget is at least
     * [MEDIUM_HEIGHT] tall; [budgetFraction] < 0 means no budgets are configured.
     */
    @Composable
    private fun WidgetContent(netWorth: Double, todaySpend: Double, budgetFraction: Float, currency: String) {
        // Only the taller (MEDIUM) layout has room for the budget progress section.
        val showBudget = LocalSize.current.height >= MEDIUM_HEIGHT
        val ctx = LocalContext.current
        // Jinatra brand warmth as base; Material You dynamic color via GlanceTheme on API 31+.
        Column(
            modifier = GlanceModifier.fillMaxSize()
                .background(Color(0xFFFFEACF))
                .padding(12.dp)
                .clickable(actionStartActivity(Intent(ctx, MainActivity::class.java))),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.Start,
        ) {
            Text("Finatra", style = TextStyle(color = ColorProvider(Color(0xFF0A756C)), fontSize = 12.sp, fontWeight = FontWeight.Medium))
            Text(
                Money.format(netWorth, currency),
                style = TextStyle(color = ColorProvider(Color(0xFF1A1A1A)), fontSize = 22.sp, fontWeight = FontWeight.Bold),
            )
            Text(
                "Today: ${Money.format(todaySpend, currency)}",
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
        private val SMALL = DpSize(110.dp, 40.dp)
        private val MEDIUM = DpSize(180.dp, 120.dp)
    }
}

/** Broadcast receiver that binds [BalanceWidget] to the Android app-widget framework. */
class BalanceWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = BalanceWidget()
}
