package com.jinatra.finatra.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.jinatra.finatra.R

// Brand typography (PRD 5.5): Poppins for headings, Inter for body.
// Loaded on demand via the Google Fonts downloadable-font provider — no bundled binaries.
private val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs,
)

private val poppins = GoogleFont("Poppins")
private val inter = GoogleFont("Inter")

/** Negan — logo/wordmark display font only (PRD §5.2). Bundled OTF in res/font; not used by the M3 scale. */
val NeganFont = FontFamily(androidx.compose.ui.text.font.Font(R.font.negan))

private val Display = FontFamily(
    Font(googleFont = poppins, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = poppins, fontProvider = provider, weight = FontWeight.Bold),
)
private val BodyFont = FontFamily(
    Font(googleFont = inter, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = inter, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = inter, fontProvider = provider, weight = FontWeight.SemiBold),
)

/**
 * Material 3 type scale for the app. Display/headline slots use Poppins (the `Display` family);
 * titles, body, and labels use Inter (the `BodyFont` family), per PRD §5.5.
 */
val FinatraTypography = Typography(
    displayLarge = TextStyle(fontFamily = Display, fontWeight = FontWeight.Bold, fontSize = 48.sp, lineHeight = 56.sp),
    displaySmall = TextStyle(fontFamily = Display, fontWeight = FontWeight.Bold, fontSize = 36.sp, lineHeight = 44.sp),
    headlineLarge = TextStyle(fontFamily = Display, fontWeight = FontWeight.Bold, fontSize = 32.sp, lineHeight = 40.sp),
    headlineMedium = TextStyle(fontFamily = Display, fontWeight = FontWeight.SemiBold, fontSize = 28.sp, lineHeight = 36.sp),
    titleLarge = TextStyle(fontFamily = BodyFont, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 26.sp),
    titleMedium = TextStyle(fontFamily = BodyFont, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, lineHeight = 24.sp),
    titleSmall = TextStyle(fontFamily = BodyFont, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp),
    bodyLarge = TextStyle(fontFamily = BodyFont, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = BodyFont, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    labelMedium = TextStyle(fontFamily = BodyFont, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp),
    labelSmall = TextStyle(fontFamily = BodyFont, fontWeight = FontWeight.Normal, fontSize = 11.sp, lineHeight = 14.sp),
)
