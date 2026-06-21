package com.jinatra.finatra.ui.theme

import androidx.compose.ui.graphics.Color

// ─────────────────────────────────────────────────────────────────────────────
// Finatra brand: two-tone "red ink on warm field" — matches the logo SVGs.
//   Lite logo: cream field (#FFEACF) + red mark (#E05454)
//   Dark logo: dark field (#222222) + red mark (#E05454)
// Light theme = cream background, red foreground. Dark theme = #222 background,
// red foreground. Cards are flat/border-only (PRD §5.7), so red is the ink for
// text, icons, headings, borders and filled accents on a warm/dark canvas.
// ─────────────────────────────────────────────────────────────────────────────
val WarmRed = Color(0xFFE05454)        // primary accent — the brand red
val DeepWarmRed = Color(0xFFC44040)    // pressed / active
val RedTintLight = Color(0xFFFDF0F0)   // red surface (light)
val RedTintBorderLight = Color(0xFFF0C8C8)
val RedTintDark = Color(0xFF2C1A1A)    // red surface (dark)
val RedTintBorderDark = Color(0xFF3A2020)
val WarmCream = Color(0xFFFFEACF)      // light background (#FFEACF)
val WarmVoid = Color(0xFF222222)       // dark background

// Logo anchors (used by the launcher/in-app logo, not the UI scheme).
val SweetCream = Color(0xFFFFEACF)
val WarmAmber = Color(0xFFE8B85A)      // warning / tertiary warmth (#E8B85A)

// ── Light scheme tokens — cream canvas, red ink ────────────────────────────────
val LightPrimary = WarmRed
val LightOnPrimary = Color(0xFFFFFFFF)
val LightPrimaryContainer = Color(0xFFFADADC)
val LightOnPrimaryContainer = Color(0xFF410005)
val LightInversePrimary = RedTintBorderLight
val LightSecondary = DeepWarmRed
val LightOnSecondary = Color(0xFFFFFFFF)
val LightSecondaryContainer = Color(0xFFFFF5E6)
val LightOnSecondaryContainer = Color(0xFF2C2C2C)
val LightTertiary = WarmAmber
val LightOnTertiary = Color(0xFFFFFFFF)
val LightTertiaryContainer = Color(0xFFFDF0D5)
val LightOnTertiaryContainer = Color(0xFF5C4E3A)
val LightBackground = WarmCream
val LightOnBackground = Color(0xFF1A1A1A)
val LightOnSurface = Color(0xFF2C2C2C)
val LightSurface = Color(0xFFFFF5E6)
val LightSurfaceVariant = Color(0xFFF5DEB3)
val LightOnSurfaceVariant = Color(0xFF6B5C4E)
val LightSurfaceDim = Color(0xFFF4EAD8)
val LightSurfaceBright = Color(0xFFFFFDF9)
val LightSurfaceContainerLowest = Color(0xFFFFFFFF)
val LightSurfaceContainerLow = Color(0xFFFFF9F0)
val LightSurfaceContainer = Color(0xFFFFF5E6)
val LightSurfaceContainerHigh = Color(0xFFFBF0DF)
val LightSurfaceContainerHighest = Color(0xFFF5EAC8)
val LightOutline = Color(0xFFD4B896)
val LightOutlineVariant = Color(0xFFF5DEB3)
val LightError = Color(0xFFC0392B)
val LightOnError = Color(0xFFFFFFFF)
val LightErrorContainer = Color(0xFFFFDAD6)
val LightOnErrorContainer = Color(0xFF93000A)
val LightInverseSurface = Color(0xFF2C2C2C)
val LightInverseOnSurface = Color(0xFFFFF5E6)
val LightScrim = Color(0xFF000000)

// ── Dark scheme tokens — #222 canvas, red ink ──────────────────────────────────
val DarkPrimary = WarmRed
val DarkOnPrimary = Color(0xFFFFFFFF)
val DarkPrimaryContainer = Color(0xFF5C1F1F)
val DarkOnPrimaryContainer = Color(0xFFFFDAD6)
val DarkInversePrimary = DeepWarmRed
val DarkSecondary = WarmRed
val DarkOnSecondary = Color(0xFFFFFFFF)
val DarkSecondaryContainer = Color(0xFF2C2C2C)
val DarkOnSecondaryContainer = Color(0xFFE0D8D0)
val DarkTertiary = WarmAmber
val DarkOnTertiary = Color(0xFF392C16)
val DarkTertiaryContainer = Color(0xFF524531)
val DarkOnTertiaryContainer = Color(0xFFF4DFC5)
val DarkBackground = WarmVoid
val DarkOnBackground = Color(0xFFF5F0EB)
val DarkSurface = Color(0xFF2C2C2C)
val DarkOnSurface = Color(0xFFE0D8D0)
val DarkSurfaceVariant = Color(0xFF333333)
val DarkOnSurfaceVariant = Color(0xFF9E9E9E)
val DarkSurfaceDim = Color(0xFF242424)
val DarkSurfaceBright = Color(0xFF363636)
val DarkSurfaceContainerLowest = Color(0xFF1C1C1C)
val DarkSurfaceContainerLow = Color(0xFF242424)
val DarkSurfaceContainer = Color(0xFF2C2C2C)
val DarkSurfaceContainerHigh = Color(0xFF333333)
val DarkSurfaceContainerHighest = Color(0xFF3B3B3B)
val DarkOutline = Color(0xFF444444)
val DarkOutlineVariant = Color(0xFF333333)
val DarkError = Color(0xFFFF6B6B)
val DarkOnError = Color(0xFF690005)
val DarkErrorContainer = Color(0xFF93000A)
val DarkOnErrorContainer = Color(0xFFFFDAD6)
val DarkInverseSurface = Color(0xFFF5F0EB)
val DarkInverseOnSurface = WarmVoid
val DarkScrim = Color(0xFF000000)

// ── Semantic transaction colors (theme-aware via FinatraExtraColors) ───────────
// Kept distinct from brand red so income/expense amounts stay legible.
val IncomeGreenLight = Color(0xFF3D7A5C)
val ExpenseRedLight = Color(0xFFE05454)
val IncomeGreenDark = Color(0xFF7FD6A8)
val ExpenseRedDark = Color(0xFFFFB3A3)

// Back-compat aliases (kept for existing call sites; prefer LocalFinatraColors).
val IncomeGreen = IncomeGreenLight
val ExpenseRed = ExpenseRedLight
