package com.astra.launcher.core.design

import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.astra.launcher.core.storage.AstraAccentSource
import com.astra.launcher.core.storage.AstraThemeMode
import com.astra.launcher.core.storage.AstraThemePreset
import com.astra.launcher.core.storage.AstraWallpaperId
import com.astra.launcher.core.storage.ThemeSettings
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

/**
 * Resolved Semantic Palette for Astra Launcher (Sections 16, 17, 43).
 */
@Immutable
data class AstraPalette(
    val isLight: Boolean,
    val obsidian0: Color,
    val elevatedSurface: Color,
    val glassSurface: Color,
    val glassStroke: Color,
    val hairlineBorder: Color,
    val scrimOverlay: Color,
    val primaryText: Color,
    val secondaryText: Color,
    val mutedText: Color,
    val primaryAccent: Color,
    val secondaryAccent: Color,
    val successTone: Color,
    val warningTone: Color,
    val dangerTone: Color
)

/**
 * WCAG Contrast & Dynamic Wallpaper Palette Engine (Section 43).
 */
object AstraColorEngine {

    fun relativeLuminance(color: Color): Float {
        fun channel(c: Float): Float =
            if (c <= 0.03928f) c / 12.92f else ((c + 0.055f) / 1.055f).pow(2.4f)
        return 0.2126f * channel(color.red) + 0.7152f * channel(color.green) + 0.0722f * channel(color.blue)
    }

    fun contrastRatio(foreground: Color, background: Color): Float {
        val l1 = relativeLuminance(foreground)
        val l2 = relativeLuminance(background)
        val lighter = max(l1, l2)
        val darker = min(l1, l2)
        return (lighter + 0.05f) / (darker + 0.05f)
    }

    fun ensureAccessibleAccent(candidate: Color, background: Color): Color {
        val ratio = contrastRatio(candidate, background)
        if (ratio >= 3.2f) return candidate
        return Color(
            red = (candidate.red + (1f - candidate.red) * 0.45f).coerceIn(0f, 1f),
            green = (candidate.green + (1f - candidate.green) * 0.45f).coerceIn(0f, 1f),
            blue = (candidate.blue + (1f - candidate.blue) * 0.55f).coerceIn(0f, 1f),
            alpha = 1f
        )
    }

    fun resolvePalette(
        wallpaper: AstraWallpaperId,
        themeMode: AstraThemeMode,
        accentSource: AstraAccentSource,
        preset: AstraThemePreset,
        customAccentHex: Long,
        highContrast: Boolean
    ): AstraPalette {
        val baseDark = Color(0xFF0A0B0E)
        val rawAccentHex = when (accentSource) {
            AstraAccentSource.WALLPAPER -> wallpaper.recommendedAccentHex
            AstraAccentSource.THEME_PRESET -> preset.primaryAccentHex
            AstraAccentSource.CUSTOM -> customAccentHex
        }

        val primaryAccent = ensureAccessibleAccent(Color(rawAccentHex), baseDark)
        val secondaryAccent = ensureAccessibleAccent(Color(wallpaper.secondaryAtmosphereHex), baseDark)

        // Note: Even in light/auto wallpaper modes, launcher workspace overlays and text use
        // high-legibility dark smoked-glass containers so white/cloud text always passes WCAG AA >= 4.5:1
        return AstraPalette(
            isLight = themeMode == AstraThemeMode.LIGHT,
            obsidian0 = baseDark,
            elevatedSurface = Color(0xFF151821),
            glassSurface = Color(0xCC171B24),
            glassStroke = if (highContrast) Color(0x99F5F6F8) else Color(0x33F5F6F8),
            hairlineBorder = if (highContrast) Color(0x80F5F6F8) else Color(0x24F5F6F8),
            scrimOverlay = Color(0xDB090B10),
            primaryText = Color(0xFFF5F6F8),
            secondaryText = if (highContrast) Color(0xFFF5F6F8) else Color(0xFFD0D5DF),
            mutedText = Color(0xFF8E96A8),
            primaryAccent = primaryAccent,
            secondaryAccent = secondaryAccent,
            successTone = Color(0xFF34D399),
            warningTone = Color(0xFFFBBF24),
            dangerTone = Color(0xFFF87171)
        )
    }

    fun buildSemanticColors(settings: ThemeSettings): AstraPalette =
        resolvePalette(
            wallpaper = settings.wallpaperId,
            themeMode = settings.themeMode,
            accentSource = settings.accentSource,
            preset = settings.themePreset,
            customAccentHex = settings.customAccentHex,
            highContrast = settings.highContrastMode
        )
}

/**
 * Corner & Container Shape Tokens (Section 18).
 */
object AstraShapes {
    val IconSquircle = RoundedCornerShape(16.dp)
    val CardMedium = RoundedCornerShape(18.dp)
    val CardLarge = RoundedCornerShape(24.dp)
    val ModalSheet = RoundedCornerShape(28.dp)
    val DockPill = RoundedCornerShape(28.dp)
    val SearchPill = RoundedCornerShape(26.dp)
    val ChipPill = CircleShape
}

/**
 * Typography Hierarchy (Section 18).
 */
object AstraTypography {
    private val sans = FontFamily.SansSerif
    private val mono = FontFamily.Monospace

    val ClockHero = TextStyle(
        fontFamily = sans,
        fontWeight = FontWeight.Light,
        fontSize = 56.sp,
        lineHeight = 60.sp,
        letterSpacing = (-1.2).sp
    )

    val DisplayM = TextStyle(
        fontFamily = sans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 34.sp
    )

    val TitleL = TextStyle(
        fontFamily = sans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp
    )

    val SectionHeader = TextStyle(
        fontFamily = sans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 20.sp
    )

    val BodyL = TextStyle(
        fontFamily = sans,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 22.sp
    )

    val BodyM = TextStyle(
        fontFamily = sans,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    )

    val Caption = TextStyle(
        fontFamily = sans,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp
    )

    val IconLabel = TextStyle(
        fontFamily = sans,
        fontWeight = FontWeight.Medium,
        fontSize = 11.5.sp,
        lineHeight = 14.sp
    )

    val MonoMetric = TextStyle(
        fontFamily = mono,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp
    )
}

/**
 * Motion Physics Tokens (Section 30).
 */
object AstraMotion {
    val TapSpring: SpringSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMediumLow
    )
    val CalmEasing: Easing = CubicBezierEasing(0.22f, 1.0f, 0.36f, 1.0f)
}

enum class AstraHapticToken {
    LIGHT_TICK,
    LONG_PRESS_ANCHOR,
    REJECT_WARNING
}

object AstraHaptics {
    fun perform(view: View?, token: AstraHapticToken, enabled: Boolean = true) {
        if (!enabled || view == null) return
        try {
            val constant = when (token) {
                AstraHapticToken.LIGHT_TICK -> HapticFeedbackConstants.CLOCK_TICK
                AstraHapticToken.LONG_PRESS_ANCHOR -> HapticFeedbackConstants.LONG_PRESS
                AstraHapticToken.REJECT_WARNING -> HapticFeedbackConstants.REJECT
            }
            view.performHapticFeedback(constant)
        } catch (_: Throwable) {
        }
    }
}
