package com.jinatra.finatra.ui.screens.achievements

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jinatra.finatra.ui.components.ExpressiveCard
import com.jinatra.finatra.ui.components.FinatraTopBar
import com.jinatra.finatra.ui.components.SectionHeader
import com.jinatra.finatra.ui.theme.WarmRed
import com.jinatra.finatra.ui.theme.SweetCream

/**
 * Gamification screen showing the user's logging streak, the current weekly challenge, financial
 * milestones, and an unlockable badge grid. Laid out as a 3-column grid where header/banner rows
 * span the full width via [fullRow].
 *
 * @param onBack navigates back.
 * @param vm derives all streak/challenge/badge/milestone state from the user's finance data.
 */
@Composable
fun AchievementsScreen(onBack: () -> Unit, vm: AchievementsViewModel = hiltViewModel()) {
    val s by vm.state.collectAsStateWithLifecycle()

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        fullRow { FinatraTopBar("Achievements", onBack) }

        fullRow {
            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(WarmRed).padding(24.dp)) {
                Column {
                    Text("🔥", fontSize = 36.sp)
                    Text("${s.streak}", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold, color = SweetCream)
                    Text("Day logging streak", style = MaterialTheme.typography.bodyMedium, color = SweetCream.copy(alpha = 0.75f))
                }
            }
        }

        fullRow { SectionHeader("This week's challenge") }
        fullRow {
            ExpressiveCard(Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                    Column(Modifier.weight(1f)) {
                        Text(s.challenge.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(s.challenge.detail, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (s.challenge.done) Badge { Text("Done") }
                }
                Spacer(Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { s.challenge.fraction },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            }
        }

        if (s.milestones.isNotEmpty()) {
            fullRow { SectionHeader("Milestones") }
            fullRow {
                ExpressiveCard(Modifier.fillMaxWidth()) {
                    s.milestones.forEachIndexed { i, m ->
                        if (i > 0) Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(if (m.reached) "🏅" else "⏳", fontSize = 18.sp)
                            Text(
                                m.label,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (m.reached) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (m.reached) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }

        fullRow { SectionHeader("Badges") }
        items(s.badges) { b -> BadgeTile(b) }
        fullRow { Spacer(Modifier.height(24.dp)) }
    }
}

/** Span a single item across all 3 grid columns. */
private fun androidx.compose.foundation.lazy.grid.LazyGridScope.fullRow(content: @Composable () -> Unit) {
    item(span = { GridItemSpan(maxLineSpan) }) { content() }
}

/** A single badge cell; dimmed to 40% opacity when still locked. */
@Composable
private fun BadgeTile(b: com.jinatra.finatra.ui.screens.achievements.Badge) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth().aspectRatio(0.95f).alpha(if (b.unlocked) 1f else 0.4f),
    ) {
        Column(
            Modifier.padding(10.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(b.emoji, fontSize = 26.sp)
            Spacer(Modifier.height(6.dp))
            Text(b.name, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center, maxLines = 2)
        }
    }
}
