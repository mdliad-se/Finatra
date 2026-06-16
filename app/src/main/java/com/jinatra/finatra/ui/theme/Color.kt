package com.jinatra.finatra.ui.theme

import androidx.compose.ui.graphics.Color

// ─────────────────────────────────────────────────────────────────────────────
// Jinatra brand anchors (from logo SVGs)
//   Lite logo: cream field (#FFEACF) + teal mark (#0A756C)
//   Dark logo: teal field (#0A756C) + cream mark (#FFEACF)
// Light theme leans on the lite logo, dark theme on the dark logo.
// ─────────────────────────────────────────────────────────────────────────────
val DeepTeal = Color(0xFF0A756C)   // logo teal — dark-mode primaryContainer
val SweetCream = Color(0xFFFFEACF) // logo cream — accent / highlight
val MistTeal = Color(0xFFD6E6E4)
val WarmWhite = Color(0xFFFFF8F2)
val DeepInk = Color(0xFF0F1F1E)
val DarkTeal = Color(0xFF1A3330)
val Ink = Color(0xFF1E1B18)

// ── Light scheme tokens (Material 3 Expressive, from template config) ──────────
val LightPrimary = Color(0xFF005B53)
val LightOnPrimary = Color(0xFFFFFFFF)
val LightPrimaryContainer = Color(0xFF0A756C)
val LightOnPrimaryContainer = Color(0xFFA1F8EC)
val LightInversePrimary = Color(0xFF7FD6CA)
val LightSecondary = Color(0xFF526160)
val LightOnSecondary = Color(0xFFFFFFFF)
val LightSecondaryContainer = Color(0xFFD6E6E4)
val LightOnSecondaryContainer = Color(0xFF3B4A48)
val LightTertiary = Color(0xFF5C4E3A)
val LightOnTertiary = Color(0xFFFFFFFF)
val LightTertiaryContainer = Color(0xFFF4DFC5)
val LightOnTertiaryContainer = Color(0xFF524531)
val LightBackground = Color(0xFFFFF8F2)
val LightOnBackground = Color(0xFF1E1B18)
val LightSurface = Color(0xFFFFF8F2)
val LightOnSurface = Color(0xFF1E1B18)
val LightSurfaceVariant = Color(0xFFE8E1DC)
val LightOnSurfaceVariant = Color(0xFF3E4947)
val LightSurfaceDim = Color(0xFFDFD9D3)
val LightSurfaceBright = Color(0xFFFFF8F2)
val LightSurfaceContainerLowest = Color(0xFFFFFFFF)
val LightSurfaceContainerLow = Color(0xFFF9F2EC)
val LightSurfaceContainer = Color(0xFFF3EDE7)
val LightSurfaceContainerHigh = Color(0xFFEEE7E1)
val LightSurfaceContainerHighest = Color(0xFFE8E1DC)
val LightOutline = Color(0xFF6E7977)
val LightOutlineVariant = Color(0xFFBDC9C6)
val LightError = Color(0xFFBA1A1A)
val LightOnError = Color(0xFFFFFFFF)
val LightErrorContainer = Color(0xFFFFDAD6)
val LightOnErrorContainer = Color(0xFF93000A)
val LightInverseSurface = Color(0xFF33302C)
val LightInverseOnSurface = Color(0xFFF6F0EA)
val LightScrim = Color(0xFF000000)

// ── Dark scheme tokens (deep-ink + teal surfaces, cream accents, bright mint) ──
val DarkPrimary = Color(0xFF7FD6CA)            // bright mint (inverse-primary)
val DarkOnPrimary = Color(0xFF003731)
val DarkPrimaryContainer = Color(0xFF0A756C)   // logo teal — brand link in dark
val DarkOnPrimaryContainer = Color(0xFF9BF2E6)
val DarkInversePrimary = Color(0xFF005B53)
val DarkSecondary = Color(0xFFBACAC8)          // secondary-fixed-dim
val DarkOnSecondary = Color(0xFF233230)
val DarkSecondaryContainer = Color(0xFF3B4A48)
val DarkOnSecondaryContainer = Color(0xFFD6E6E4)
val DarkTertiary = Color(0xFFD7C4AA)           // warm tan (tertiary-fixed-dim)
val DarkOnTertiary = Color(0xFF392C16)
val DarkTertiaryContainer = Color(0xFF524531)
val DarkOnTertiaryContainer = Color(0xFFF4DFC5)
val DarkBackground = Color(0xFF0F1F1E)         // deep-ink-bg
val DarkOnBackground = Color(0xFFEDE3D6)       // warm cream-white (logo-derived)
val DarkSurface = Color(0xFF0F1F1E)
val DarkOnSurface = Color(0xFFEDE3D6)
val DarkSurfaceVariant = Color(0xFF1A3330)     // dark-teal-surface
val DarkOnSurfaceVariant = Color(0xFFB8C8C5)
val DarkSurfaceDim = Color(0xFF0F1F1E)
val DarkSurfaceBright = Color(0xFF354443)
val DarkSurfaceContainerLowest = Color(0xFF0A1413)
val DarkSurfaceContainerLow = Color(0xFF14211F)
val DarkSurfaceContainer = Color(0xFF1A3330)   // card surface (teal-tinted)
val DarkSurfaceContainerHigh = Color(0xFF233E3A)
val DarkSurfaceContainerHighest = Color(0xFF2D4844)
val DarkOutline = Color(0xFF87938F)
val DarkOutlineVariant = Color(0xFF3D4A47)
val DarkError = Color(0xFFFFB4AB)
val DarkOnError = Color(0xFF690005)
val DarkErrorContainer = Color(0xFF93000A)
val DarkOnErrorContainer = Color(0xFFFFDAD6)
val DarkInverseSurface = Color(0xFFEDE3D6)
val DarkInverseOnSurface = Color(0xFF1A3330)
val DarkScrim = Color(0xFF000000)

// ── Semantic transaction colors (theme-aware via FinatraExtraColors) ───────────
val IncomeGreenLight = Color(0xFF2E7D5B)
val ExpenseRedLight = Color(0xFFB0413E)
val IncomeGreenDark = Color(0xFF7FD6A8)
val ExpenseRedDark = Color(0xFFFFB3A3)

// Back-compat aliases (kept for existing call sites; prefer LocalFinatraColors).
val IncomeGreen = IncomeGreenLight
val ExpenseRed = ExpenseRedLight
