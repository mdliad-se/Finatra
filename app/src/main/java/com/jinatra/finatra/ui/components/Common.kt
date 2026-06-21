package com.jinatra.finatra.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Standard screen top app bar in brand colors. Shows a back arrow only when [onBack] is provided,
 * so top-level tabs can omit it while pushed detail screens get a navigate-up button.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinatraTopBar(title: String, onBack: (() -> Unit)? = null) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ),
    )
}

/** Section title within a screen — title-medium weight in the brand primary color. */
@Composable
fun SectionHeader(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(vertical = 8.dp),
    )
}

/** Small uppercase, letter-spaced caption — the template's `label-md uppercase tracking-widest`. */
@Composable
fun OverlineLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.2.sp),
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier,
    )
}

/**
 * Signature bento card: rounded-xl surface with a hairline outline-variant border.
 * [container] defaults to `surfaceContainerLow`; pass `secondaryContainer` etc. for accent tiles.
 */
@Composable
fun ExpressiveCard(
    modifier: Modifier = Modifier,
    container: Color = MaterialTheme.colorScheme.surfaceContainerLow,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    shape: androidx.compose.ui.graphics.Shape = MaterialTheme.shapes.medium,
    border: Boolean = true,
    padding: Dp = 20.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = container,
        contentColor = contentColor,
    ) {
        val borderMod = if (border) {
            Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), shape)
        } else Modifier
        Column(Modifier.then(borderMod).padding(padding), content = content)
    }
}

/** Circular icon chip with a tinted background — used across cards and insights. */
@Composable
fun IconChip(
    icon: ImageVector,
    tint: Color = MaterialTheme.colorScheme.primary,
    size: Dp = 48.dp,
    background: Color = tint.copy(alpha = 0.16f),
) {
    Box(
        Modifier.size(size).clip(CircleShape).background(background),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = tint)
    }
}

/** Insight / quick-report tile: icon chip + small overline + body line. */
@Composable
fun InsightCard(
    icon: ImageVector,
    label: String,
    message: String,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.primary,
) {
    ExpressiveCard(modifier = modifier, container = MaterialTheme.colorScheme.surfaceContainer, padding = 16.dp) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            IconChip(icon, tint = tint)
            Column {
                OverlineLabel(label)
                Text(
                    message,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

/** Pill segmented toggle (Week / Month style). [selectedIndex] is the active option. */
@Composable
fun SegmentedToggle(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(4.dp)
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        options.forEachIndexed { i, label ->
            val active = i == selectedIndex
            Box(
                Modifier
                    .clip(CircleShape)
                    .background(if (active) MaterialTheme.colorScheme.primary else Color.Transparent)
                    .clickable { onSelect(i) }
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (active) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/** Centered placeholder shown when a list/screen has no content, displaying [message]. */
@Composable
fun EmptyState(message: String, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}
