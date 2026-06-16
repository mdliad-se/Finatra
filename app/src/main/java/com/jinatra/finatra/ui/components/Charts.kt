package com.jinatra.finatra.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/** Vibrant fallback palette for segments without an explicit category color. */
val ChartColors = listOf(
    Color(0xFF3B82F6), // blue
    Color(0xFFF59E0B), // amber
    Color(0xFF22C55E), // green
    Color(0xFF8B5CF6), // purple
    Color(0xFFEF4444), // red
    Color(0xFF06B6D4), // cyan
    Color(0xFFEC4899), // pink
    Color(0xFF14B8A6), // teal
)

fun chartColor(hex: Long?, index: Int): Color =
    if (hex != null) Color(hex) else ChartColors[index % ChartColors.size]

data class RingSegment(val label: String, val value: Double, val color: Color)

/**
 * Donut ring with rounded caps, a center label, and percentage labels on each arc.
 * Pass [centerTop] (small caption) and [centerValue] (big number) for the hole.
 */
@Composable
fun SpendingRing(
    segments: List<RingSegment>,
    centerTop: String,
    centerValue: String,
    modifier: Modifier = Modifier,
    ringThickness: Dp = 26.dp,
    showPercentLabels: Boolean = true,
) {
    val total = segments.sumOf { it.value }
    val measurer = rememberTextMeasurer()
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val track = MaterialTheme.colorScheme.surfaceVariant

    Box(modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val strokePx = ringThickness.toPx()
            val labelPad = if (showPercentLabels) 30.dp.toPx() else strokePx / 2f
            val diameter = minOf(size.width, size.height) - strokePx - labelPad * 2
            val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
            val arcSize = Size(diameter, diameter)
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = diameter / 2f

            // Track behind segments
            drawArc(
                color = track.copy(alpha = 0.4f),
                startAngle = -90f, sweepAngle = 360f, useCenter = false,
                topLeft = topLeft, size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round),
            )
            if (total <= 0.0) return@Canvas

            val gap = 5f
            var start = -90f
            segments.forEach { seg ->
                val sweep = (seg.value / total * 360f).toFloat()
                if (sweep <= 0f) return@forEach
                drawArc(
                    color = seg.color,
                    startAngle = start + gap / 2f,
                    sweepAngle = (sweep - gap).coerceAtLeast(0.5f),
                    useCenter = false,
                    topLeft = topLeft, size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round),
                )
                if (showPercentLabels && sweep > 16f) {
                    val mid = Math.toRadians((start + sweep / 2f).toDouble())
                    val lr = radius + strokePx / 2f + 14.dp.toPx()
                    val lx = center.x + cos(mid).toFloat() * lr
                    val ly = center.y + sin(mid).toFloat() * lr
                    val pct = (seg.value / total * 100).roundToInt()
                    val layout = measurer.measure(
                        "$pct%",
                        style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = onVariant),
                    )
                    drawText(
                        layout,
                        topLeft = Offset(lx - layout.size.width / 2f, ly - layout.size.height / 2f),
                    )
                }
                start += sweep
            }
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth(0.52f),
        ) {
            Text(
                centerTop,
                style = MaterialTheme.typography.labelMedium,
                color = onVariant,
                maxLines = 1,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
            Text(
                centerValue,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = onSurface,
                maxLines = 1,
                softWrap = false,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }
    }
}

/** Reference-style spending tile: colored chip, amount, name, percentage badge. */
@Composable
fun CategoryStatCard(
    name: String,
    amount: String,
    percent: Int,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(36.dp).clip(CircleShape).background(color.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(Modifier.size(14.dp).clip(CircleShape).background(color))
                }
                Box(
                    Modifier.clip(RoundedCornerShape(50)).background(color.copy(alpha = 0.14f))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                ) {
                    Text("$percent%", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = color)
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(amount, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(name, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** Two-column grid of [CategoryStatCard]s, laid out without a nested lazy grid. */
@Composable
fun CategoryStatGrid(items: List<CategoryStat>, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items.chunked(2).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { stat ->
                    CategoryStatCard(stat.name, stat.amount, stat.percent, stat.color, Modifier.weight(1f))
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

data class CategoryStat(
    val name: String,
    val amount: String,
    val percent: Int,
    val color: Color,
)

data class BarGroup(val label: String, val income: Double, val expense: Double)

/** Grouped income/expense bars per period (PRD 6.7). */
@Composable
fun IncomeExpenseBars(
    groups: List<BarGroup>,
    incomeColor: Color,
    expenseColor: Color,
    modifier: Modifier = Modifier,
) {
    val max = (groups.maxOfOrNull { maxOf(it.income, it.expense) } ?: 0.0).coerceAtLeast(1.0)
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        groups.forEach { g ->
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    Modifier.fillMaxWidth().height(140.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Bar((g.income / max).toFloat(), incomeColor, Modifier.weight(1f))
                    Bar((g.expense / max).toFloat(), expenseColor, Modifier.weight(1f))
                }
                Spacer(Modifier.height(6.dp))
                Text(g.label, style = MaterialTheme.typography.labelSmall, color = labelColor)
            }
        }
    }
}

@Composable
private fun Bar(fraction: Float, color: Color, modifier: Modifier) {
    Box(
        modifier
            .fillMaxHeight(fraction.coerceIn(0.02f, 1f))
            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
            .background(color),
    )
}

/** Net-worth-over-time line with a soft area fill (PRD 6.7). */
@Composable
fun NetWorthLine(
    values: List<Double>,
    labels: List<String>,
    lineColor: Color,
    modifier: Modifier = Modifier,
) {
    if (values.size < 2) return
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val measurer = rememberTextMeasurer()
    val min = values.min()
    val max = values.max()
    val span = (max - min).takeIf { it != 0.0 } ?: 1.0
    Canvas(modifier) {
        val pad = 28.dp.toPx()
        val w = size.width
        val h = size.height - pad
        val stepX = w / (values.size - 1)
        fun y(v: Double) = (h - ((v - min) / span * (h - 10.dp.toPx())).toFloat()).toFloat()
        val pts = values.mapIndexed { i, v -> Offset(i * stepX, y(v)) }

        val area = Path().apply {
            moveTo(pts.first().x, h)
            pts.forEach { lineTo(it.x, it.y) }
            lineTo(pts.last().x, h)
            close()
        }
        drawPath(area, brush = Brush.verticalGradient(listOf(lineColor.copy(alpha = 0.25f), lineColor.copy(alpha = 0f))))

        val line = Path().apply {
            moveTo(pts.first().x, pts.first().y)
            pts.drop(1).forEach { lineTo(it.x, it.y) }
        }
        drawPath(line, color = lineColor, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
        pts.lastOrNull()?.let { drawCircle(lineColor, radius = 5.dp.toPx(), center = it) }

        labels.forEachIndexed { i, lbl ->
            val layout = measurer.measure(lbl, style = TextStyle(fontSize = 10.sp, color = labelColor))
            drawText(layout, topLeft = Offset(i * stepX - layout.size.width / 2f, h + 6.dp.toPx()))
        }
    }
}
