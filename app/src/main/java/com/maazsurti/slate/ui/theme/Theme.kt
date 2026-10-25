package com.maazsurti.slate.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Warm paper, ink, one hairline. Colour carries meaning only.
val Canvas = Color(0xFFF6F4EF)
val Paper = Color(0xFFFFFDF8)
val Ink = Color(0xFF17150F)
val Muted = Color(0xFF79736A)
val Hairline = Color(0xFFE4DFD4)
val Green = Color(0xFF2F5D45)
val GreenWash = Color(0xFFEDF3EC)
val Clay = Color(0xFF956400)
val ClayWash = Color(0xFFFBF3DB)
val Rust = Color(0xFF9F2F2D)
val RustWash = Color(0xFFFDEBEC)

private val LightColors = lightColorScheme(
    primary = Ink,
    onPrimary = Paper,
    primaryContainer = Ink,
    onPrimaryContainer = Paper,
    secondary = Green,
    onSecondary = Paper,
    secondaryContainer = GreenWash,
    onSecondaryContainer = Green,
    background = Canvas,
    onBackground = Ink,
    surface = Paper,
    onSurface = Ink,
    surfaceVariant = Canvas,
    onSurfaceVariant = Muted,
    surfaceContainer = Canvas,
    surfaceContainerHigh = Paper,
    surfaceContainerHighest = Canvas,
    outline = Hairline,
    outlineVariant = Hairline,
    error = Rust,
    onError = Paper,
    errorContainer = RustWash,
    onErrorContainer = Rust,
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFF3EFE6),
    onPrimary = Color(0xFF14120E),
    secondary = Color(0xFF9CC5AC),
    onSecondary = Color(0xFF14120E),
    secondaryContainer = Color(0xFF1E2A22),
    onSecondaryContainer = Color(0xFFC9DECf),
    background = Color(0xFF14120E),
    onBackground = Color(0xFFF3EFE6),
    surface = Color(0xFF1D1A15),
    onSurface = Color(0xFFF3EFE6),
    surfaceVariant = Color(0xFF14120E),
    onSurfaceVariant = Color(0xFFA29A8C),
    surfaceContainer = Color(0xFF14120E),
    outline = Color(0xFF2E2A23),
    outlineVariant = Color(0xFF2E2A23),
    error = Color(0xFFE8A39F),
    errorContainer = Color(0xFF3A1D1C),
)

private val Serif = FontFamily.Serif

val SlateTypography = Typography(
    displayLarge = TextStyle(fontFamily = Serif, fontSize = 46.sp, lineHeight = 50.sp, letterSpacing = (-1).sp),
    displaySmall = TextStyle(fontFamily = Serif, fontSize = 34.sp, lineHeight = 40.sp, letterSpacing = (-0.6).sp),
    headlineMedium = TextStyle(fontFamily = Serif, fontSize = 27.sp, lineHeight = 32.sp, letterSpacing = (-0.4).sp),
    headlineSmall = TextStyle(fontFamily = Serif, fontSize = 22.sp, lineHeight = 28.sp),
    titleLarge = TextStyle(fontSize = 19.sp, lineHeight = 24.sp, fontWeight = FontWeight.Medium),
    titleMedium = TextStyle(fontSize = 15.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium),
    bodyLarge = TextStyle(fontSize = 15.sp, lineHeight = 22.sp),
    bodyMedium = TextStyle(fontSize = 13.5.sp, lineHeight = 19.sp),
    bodySmall = TextStyle(fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge = TextStyle(fontSize = 13.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium),
    labelMedium = TextStyle(fontSize = 11.sp, lineHeight = 14.sp, letterSpacing = 0.4.sp),
    labelSmall = TextStyle(fontSize = 10.sp, lineHeight = 12.sp, letterSpacing = 1.2.sp),
)

/** Figures line up in a column, like a ledger. */
val MoneyStyle = TextStyle(
    fontFamily = FontFamily.Monospace,
    fontSize = 14.sp,
    lineHeight = 18.sp,
    letterSpacing = (-0.2).sp,
    fontFeatureSettings = "tnum",
)

@Composable
fun SlateTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = SlateTypography,
        content = content,
    )
}
