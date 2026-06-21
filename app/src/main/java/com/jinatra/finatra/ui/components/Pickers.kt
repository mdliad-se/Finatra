package com.jinatra.finatra.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** Brand warm palette for account/category colors (PRD §5.5 accountPalette). */
val AccountColors: List<Long> = listOf(
    // Red
    0xFFE05454, 0xFF8A4A4A,
    // Green
    0xFF3D7A5C, 0xFF2A5E43,
    // Blue
    0xFF4A7A8A, 0xFF2B4D66,
    // Orange
    0xFFC97B4B, 0xFFE68E65,
    // Yellow
    0xFFD9A036, 0xFF7A6E3D,
    // Pink
    0xFFD46A8D, 0xFF6B4C8A,
)

/**
 * Horizontal swatch picker over [AccountColors]. The [selected] color (an ARGB Long) gets a white
 * ring and check mark; tapping a swatch reports it via [onSelect].
 */
@Composable
fun ColorPickerRow(selected: Long, onSelect: (Long) -> Unit, modifier: Modifier = Modifier) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(vertical = 4.dp),
    ) {
        items(AccountColors) { c ->
            Box(
                Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(c))
                    .then(if (c == selected) Modifier.border(3.dp, Color.White, CircleShape) else Modifier)
                    .clickable { onSelect(c) },
                contentAlignment = Alignment.Center,
            ) {
                if (c == selected) Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White)
            }
        }
    }
}

/**
 * Generic read-only exposed dropdown over [options] of any type [T].
 *
 * @param label floating label for the field.
 * @param selected currently chosen option, rendered via [optionLabel].
 * @param optionLabel maps an option to its display string (used for the field and each menu item).
 * @param onSelect invoked with the chosen option when a menu item is tapped.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> LabeledDropdown(
    label: String,
    options: List<T>,
    selected: T,
    optionLabel: (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Local open/closed state for the menu, hoisted into the ExposedDropdownMenuBox.
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }, modifier = modifier) {
        OutlinedTextField(
            value = optionLabel(selected),
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                .fillMaxWidth(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { opt ->
                DropdownMenuItem(
                    text = { Text(optionLabel(opt)) },
                    onClick = { onSelect(opt); expanded = false },
                )
            }
        }
    }
}
