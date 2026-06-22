package com.jinatra.finatra.ui.screens.goals

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jinatra.finatra.data.local.entity.GoalEntity
import com.jinatra.finatra.data.local.entity.GoalType
import com.jinatra.finatra.ui.components.ExpressiveCard
import com.jinatra.finatra.ui.components.FinatraTopBar
import com.jinatra.finatra.ui.components.OverlineLabel
import com.jinatra.finatra.ui.components.SectionHeader
import com.jinatra.finatra.ui.theme.FinatraTheme
import com.jinatra.finatra.util.DateUtil
import com.jinatra.finatra.util.Money

/**
 * Goals feature screen. Shows savings goals and tracked debts in separate sections,
 * each rendered as a progress card, plus an AI-powered "Can I afford it?" helper at
 * the top. Hosts dialogs for creating/editing a goal and for logging a contribution
 * or repayment. All persistence and AI calls are delegated to [GoalsViewModel].
 *
 * @param onBack invoked when the top bar's back affordance is tapped.
 */
@Composable
fun GoalsScreen(onBack: () -> Unit, vm: GoalsViewModel = hiltViewModel()) {
    val s by vm.state.collectAsStateWithLifecycle()
    var editing by remember { mutableStateOf<GoalEntity?>(null) }
    var creating by remember { mutableStateOf(false) }
    var contributing by remember { mutableStateOf<GoalEntity?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { FinatraTopBar("Goals", onBack) },
        floatingActionButton = {
            IconButton(onClick = { creating = true }) {
                Box(
                    Modifier.clip(CircleShape).padding(0.dp),
                    contentAlignment = Alignment.Center,
                ) { Icon(Icons.Filled.Add, contentDescription = "Add goal", tint = MaterialTheme.colorScheme.primary) }
            }
        },
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxWidth().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { AffordItCard(vm) }
            item { SavingsPlanCard(vm, s.baseCurrency) }

            item { SectionHeader("Savings Goals") }
            if (s.savings.isEmpty()) {
                item { EmptyHint("No savings goals yet. Tap + to set a target.") }
            } else {
                items(s.savings) { g ->
                    GoalCard(
                        g, s.baseCurrency, onEdit = { editing = g }, onContribute = { contributing = g },
                        onDelete = { vm.delete(g) }, onAutoPlan = { vm.autoPlan(g) }, onClearPlan = { vm.clearPlan(g) },
                    )
                }
            }

            item { Spacer(Modifier.height(4.dp)); SectionHeader("Debt Tracker") }
            if (s.debts.isEmpty()) {
                item { EmptyHint("No debts tracked. Tap + and pick a debt type.") }
            } else {
                items(s.debts) { g -> GoalCard(g, s.baseCurrency, onEdit = { editing = g }, onContribute = { contributing = g }, onDelete = { vm.delete(g) }) }
            }
            item { Spacer(Modifier.height(80.dp)) }
        }
    }

    if (creating) GoalDialog(null, s.baseCurrency, onDismiss = { creating = false }, onSave = { vm.save(it); creating = false })
    editing?.let { g -> GoalDialog(g, s.baseCurrency, onDismiss = { editing = null }, onSave = { vm.save(it); editing = null }) }
    contributing?.let { g ->
        ContributeDialog(g, onDismiss = { contributing = null }, onConfirm = { delta -> vm.contribute(g, delta); contributing = null })
    }
}

/** Human-readable label for each goal type as shown in the UI. */
private fun GoalType.label() = when (this) {
    GoalType.SAVINGS -> "Savings"
    GoalType.DEBT_OWED -> "I owe"
    GoalType.DEBT_LENT -> "Owed to me"
}

@Composable
private fun EmptyHint(text: String) {
    Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(vertical = 8.dp))
}

/**
 * Card for a single goal (savings or debt). Shows name, type, target, a progress bar,
 * saved/remaining amounts, and actions to contribute, edit, or delete. Labels adapt to
 * the goal type (e.g. "Saved" vs "Repaid", "Add funds" vs "Log payment").
 */
@Composable
private fun GoalCard(
    g: GoalEntity,
    currency: String,
    onEdit: () -> Unit,
    onContribute: () -> Unit,
    onDelete: () -> Unit,
    onAutoPlan: () -> Unit = {},
    onClearPlan: () -> Unit = {},
) {
    // Completion fraction (0..1), guarding against a zero target; remaining never goes negative.
    val pct = if (g.targetAmount > 0) (g.savedAmount / g.targetAmount).toFloat().coerceIn(0f, 1f) else 0f
    val remaining = (g.targetAmount - g.savedAmount).coerceAtLeast(0.0)
    ExpressiveCard(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text(g.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, maxLines = 1)
                Text("${g.type.label()} · target ${Money.format(g.targetAmount, g.currency.ifBlank { currency })}",
                    style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text("${(pct * 100).toInt()}%", style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
        Spacer(Modifier.height(10.dp))
        LinearProgressIndicator(
            progress = { pct },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
            color = if (pct >= 1f) FinatraTheme.income else MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("${if (g.type == GoalType.SAVINGS) "Saved" else "Repaid"} ${Money.format(g.savedAmount, g.currency.ifBlank { currency })}",
                style = MaterialTheme.typography.labelMedium, color = FinatraTheme.income)
            Text("${Money.format(remaining, g.currency.ifBlank { currency })} left",
                style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onContribute, modifier = Modifier.weight(1f)) {
                Text(if (g.type == GoalType.SAVINGS) "Add funds" else "Log payment")
            }
            OutlinedButton(onClick = onEdit) { Text("Edit") }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = FinatraTheme.expense)
            }
        }
        // Contribution plan (savings goals only): monthly target, on-track badge, projected finish.
        if (g.type == GoalType.SAVINGS) {
            val cur = g.currency.ifBlank { currency }
            val plan = GoalPlan.of(g)
            Spacer(Modifier.height(8.dp))
            if (plan == null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("No savings plan yet", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                    TextButton(onClick = onAutoPlan) { Text("Auto-plan") }
                }
            } else {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Plan: ${Money.format(plan.monthlyTarget, cur)}/mo",
                        style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                    PlanBadge(plan.onTrack)
                }
                plan.projectedFinish?.let { finish ->
                    Text("On pace to finish ${DateUtil.month(finish)}",
                        style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onAutoPlan) { Text("Re-plan") }
                    TextButton(onClick = onClearPlan) { Text("Clear plan") }
                }
            }
        }
    }
}

/** On-track / behind pill for a savings plan. */
@Composable
private fun PlanBadge(onTrack: Boolean) {
    val color = if (onTrack) FinatraTheme.income else FinatraTheme.expense
    Surface(color = color.copy(alpha = 0.15f), shape = RoundedCornerShape(50)) {
        Text(
            if (onTrack) "On track" else "Behind",
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = color,
        )
    }
}

/**
 * "Savings plan" card: builds a proposed split of the projected monthly surplus across unfinished
 * savings goals, shown for review and applied as per-goal monthly targets.
 */
@Composable
private fun SavingsPlanCard(vm: GoalsViewModel, currency: String) {
    val plan by vm.savingsPlan.collectAsStateWithLifecycle()
    val building by vm.buildingPlan.collectAsStateWithLifecycle()
    var notice by remember { mutableStateOf<String?>(null) }
    ExpressiveCard(Modifier.fillMaxWidth(), container = MaterialTheme.colorScheme.secondaryContainer) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text("Savings plan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            if (building) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
        }
        Spacer(Modifier.height(6.dp))
        Text("Split your projected monthly surplus across your savings goals.",
            style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        notice?.let { Spacer(Modifier.height(6.dp)); Text(it, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        if (plan.isEmpty()) {
            Spacer(Modifier.height(10.dp))
            Button(onClick = { notice = null; vm.buildSavingsPlan { notice = it } }, enabled = !building, modifier = Modifier.fillMaxWidth()) {
                Text("Build savings plan")
            }
        } else {
            Spacer(Modifier.height(8.dp))
            plan.forEach { item ->
                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(item.name, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                    Text("${Money.format(item.monthly, currency)}/mo", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { vm.applySavingsPlan() }, modifier = Modifier.weight(1f)) { Text("Apply plan") }
                OutlinedButton(onClick = { vm.dismissSavingsPlan() }) { Text("Dismiss") }
            }
        }
    }
}

/**
 * "Can I afford it?" helper card. Takes an amount and asks the ViewModel for a verdict,
 * showing a spinner while the (AI-backed or heuristic) check runs and then the result text.
 */
@Composable
private fun AffordItCard(vm: GoalsViewModel) {
    val loading by vm.affordLoading.collectAsStateWithLifecycle()
    val result by vm.affordResult.collectAsStateWithLifecycle()
    var amount by remember { mutableStateOf("") }
    ExpressiveCard(Modifier.fillMaxWidth(), container = MaterialTheme.colorScheme.secondaryContainer) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text("Can I afford it?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it.filter { c -> c.isDigit() || c == '.' } },
                placeholder = { Text("Enter amount") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f),
            )
            Button(onClick = { vm.checkAfford(amount.toDoubleOrNull() ?: 0.0) }, enabled = !loading) {
                Text("Ask AI")
            }
        }
        if (loading) {
            Spacer(Modifier.height(10.dp))
            CircularProgressIndicator(modifier = Modifier.height(22.dp))
        }
        result?.let {
            Spacer(Modifier.height(10.dp))
            Text(it, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

/**
 * Dialog for creating a new goal or editing an existing one. When [existing] is null it
 * builds a fresh [GoalEntity]; otherwise it copies the edited fields onto it. Save is
 * enabled only once a name and a positive target are provided.
 */
@Composable
private fun GoalDialog(existing: GoalEntity?, baseCurrency: String, onDismiss: () -> Unit, onSave: (GoalEntity) -> Unit) {
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var target by remember { mutableStateOf(existing?.targetAmount?.let { if (it == 0.0) "" else it.toString() } ?: "") }
    var type by remember { mutableStateOf(existing?.type ?: GoalType.SAVINGS) }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank() && (target.toDoubleOrNull() ?: 0.0) > 0,
                onClick = {
                    onSave(
                        (existing ?: GoalEntity(
                            name = "", type = type, targetAmount = 0.0, currency = baseCurrency,
                            colorHex = 0xFFE05454, createdAt = System.currentTimeMillis(),
                        )).copy(name = name.trim(), targetAmount = target.toDouble(), type = type)
                    )
                },
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        title = { Text(if (existing == null) "New goal" else "Edit goal") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("Name") }, singleLine = true)
                OutlinedTextField(
                    target, { target = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Target amount") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                )
                OverlineLabel("Type")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GoalType.entries.forEach { t ->
                        FilterChip(selected = type == t, onClick = { type = t }, label = { Text(t.label()) })
                    }
                }
            }
        },
    )
}

/**
 * Dialog for adding a contribution (savings) or logging a payment (debt) against [g].
 * Confirms with the entered positive amount; the title adapts to the goal type.
 */
@Composable
private fun ContributeDialog(g: GoalEntity, onDismiss: () -> Unit, onConfirm: (Double) -> Unit) {
    var amount by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                enabled = (amount.toDoubleOrNull() ?: 0.0) > 0,
                onClick = { onConfirm(amount.toDouble()) },
            ) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        title = { Text(if (g.type == GoalType.SAVINGS) "Add funds" else "Log payment") },
        text = {
            OutlinedTextField(
                amount, { amount = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Amount") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            )
        },
    )
}
