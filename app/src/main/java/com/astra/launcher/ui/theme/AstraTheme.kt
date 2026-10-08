package com.astra.launcher.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Astra theme — Material 3 wired to Astra tokens.
 * Type scale: docs/01_design_system.md §4 (Space Grotesk display / Inter text;
 * wire res/font families in a full build).
 */
private val AstraTypography = Typography(
    displayLarge = TextStyle(fontWeight = FontWeight.Light, fontSize = 57.sp, lineHeight = 62.sp),
    headlineLarge = TextStyle(fontWeight = FontWeight.Medium, fontSize = 32.sp, lineHeight = 38.sp),
    headlineMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 24.sp, lineHeight = 30.sp),
    titleLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp),
    bodyLarge = TextStyle(fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 22.sp),
    bodyMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 21.sp),
    labelSmall = TextStyle(fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 14.sp),
)

@Composable
fun AstraTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            background = AstraColors.InkBackground,
            surface = AstraColors.Surface,
            surfaceVariant = AstraColors.SurfaceRaised,
            primary = AstraColors.AuroraTeal,
            onPrimary = AstraColors.InkBackground,
            secondary = AstraColors.Violet,
            onBackground = AstraColors.Starlight,
            onSurface = AstraColors.Starlight,
            onSurfaceVariant = AstraColors.Muted,
            error = AstraColors.Error,
        )
    } else {
        lightColorScheme(
            background = AstraColors.Paper,
            surface = AstraColors.WhiteSurface,
            surfaceVariant = AstraColors.RaisedLight,
            primary = AstraColors.DeepTeal,
            onPrimary = AstraColors.WhiteSurface,
            secondary = AstraColors.DeepViolet,
            onBackground = AstraColors.Ink,
            onSurface = AstraColors.Ink,
            onSurfaceVariant = AstraColors.InkMuted,
        )
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = AstraTypography,
        content = content,
    )
}
