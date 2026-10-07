package com.astra.launcher.core.design

import android.media.AudioManager
import android.media.ToneGenerator
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.astra.launcher.core.performance.AstraVisualBudget
import com.astra.launcher.core.storage.AstraAccentSource
import com.astra.launcher.core.storage.AstraThemeMode
import com.astra.launcher.core.storage.ThemeSettings
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

/**
 * Base Palette & Semantic Color Roles (Section 5).
 */
object AstraPalette {
    val AstraBlack = Color(0xFF0A0B0D)
    val Graphite1 = Color(0xFF111318)
    val Graphite2 = Color(0xFF171A20)
    val Graphite3 = Color(0xFF20242C)
    val Cloud = Color(0xFFF5F6F8)
    val Mist = Color(0xFFD8DBE1)
    val Ink = Color(0xFF171A20)

    val AstralCyan = Color(0xFF7DD3FC)
    val AstralBlue = Color(0xFF38BDF8)
    val AstralViolet = Color(0xFFA78BFA)
    val AstralIndigo = Color(0xFF818CF8)

    val SemanticSuccess = Color(0xFF34D399)
    val SemanticWarning = Color(0xFFFBBF24)
    val SemanticError = Color(0xFFF87171)
    val SemanticInfo = Color(0xFF60A5FA)
}

@Immutable
data class AstraSemanticColors(
    val isLight: Boolean,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val textDisabled: Color,
    val iconPrimary: Color,
    val iconSecondary: Color,
    val surfaceBase: Color,
    val surfaceRaised: Color,
    val surfaceFloating: Color,
    val surfaceGlass: Color,
    val surfaceGlassStrong: Color,
    val borderSubtle: Color,
    val focusRing: Color,
    val accentPrimary: Color,
    val accentSecondary: Color,
    val success: Color,
    val warning: Color,
    val error: Color,
    val info: Color
)

/**
 * Contrast & Dynamic Palette Envelope Validator (Section 5).
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

    fun ensureAccessibleAccent(candidate: Color, isLightSurface: Boolean): Color {
        val bg = if (isLightSurface) AstraPalette.Cloud else AstraPalette.Graphite1
        val ratio = contrastRatio(candidate, bg)
        if (ratio >= 3.5f) return candidate
        return if (isLightSurface) {
            Color(
                red = (candidate.red * 0.62f).coerceIn(0f, 1f),
                green = (candidate.green * 0.62f).coerceIn(0f, 1f),
                blue = (candidate.blue * 0.75f).coerceIn(0f, 1f),
                alpha = 1f
            )
        } else {
            Color(
                red = (candidate.red + (1f - candidate.red) * 0.45f).coerceIn(0f, 1f),
                green = (candidate.green + (1f - candidate.green) * 0.45f).coerceIn(0f, 1f),
                blue = (candidate.blue + (1f - candidate.blue) * 0.55f).coerceIn(0f, 1f),
                alpha = 1f
            )
        }
    }

    fun buildSemanticColors(settings: ThemeSettings): AstraSemanticColors {
        val isLight = when (settings.themeMode) {
            AstraThemeMode.LIGHT -> true
            AstraThemeMode.DARK -> false
            AstraThemeMode.AUTO_WALLPAPER -> settings.wallpaperId.upperRegionLuminance > 0.65f
        }

        val rawAccentHex = when (settings.accentSource) {
            AstraAccentSource.WALLPAPER -> settings.wallpaperId.recommendedAccentHex
            AstraAccentSource.THEME_PRESET -> settings.themePreset.primaryAccentHex
            AstraAccentSource.CUSTOM -> settings.customAccentHex
        }

        val primaryAccent = ensureAccessibleAccent(Color(rawAccentHex), isLight)
        val secondaryAccent = ensureAccessibleAccent(Color(settings.wallpaperId.secondaryAtmosphereHex), isLight)
        val highContrast = settings.highContrastMode

        return if (isLight) {
            AstraSemanticColors(
                isLight = true,
                textPrimary = AstraPalette.Ink,
                textSecondary = if (highContrast) Color(0xFF1F242D) else Color(0xFF3E4656),
                textTertiary = Color(0xFF646E82),
                textDisabled = Color(0xFF9AA3B5),
                iconPrimary = AstraPalette.Ink,
                iconSecondary = Color(0xFF475063),
                surfaceBase = AstraPalette.Cloud,
                surfaceRaised = Color(0xFFFFFFFF),
                surfaceFloating = Color(0xFFFDFEFF),
                surfaceGlass = Color(0xD9F5F6F8),
                surfaceGlassStrong = Color(0xF2F5F6F8),
                borderSubtle = if (highContrast) Color(0x66171A20) else Color(0x22171A20),
                focusRing = primaryAccent,
                accentPrimary = primaryAccent,
                accentSecondary = secondaryAccent,
                success = Color(0xFF059669),
                warning = Color(0xFFD97706),
                error = Color(0xFFDC2626),
                info = Color(0xFF0284C7)
            )
        } else {
            AstraSemanticColors(
                isLight = false,
                textPrimary = AstraPalette.Cloud,
                textSecondary = if (highContrast) AstraPalette.Cloud else AstraPalette.Mist,
                textTertiary = Color(0xFF949BA8),
                textDisabled = Color(0xFF565D6B),
                iconPrimary = AstraPalette.Cloud,
                iconSecondary = AstraPalette.Mist,
                surfaceBase = AstraPalette.AstraBlack,
                surfaceRaised = AstraPalette.Graphite1,
                surfaceFloating = AstraPalette.Graphite2,
                surfaceGlass = Color(0xB8171A20),
                surfaceGlassStrong = Color(0xE0111318),
                borderSubtle = if (highContrast) Color(0x88F5F6F8) else Color(0x26F5F6F8),
                focusRing = primaryAccent,
                accentPrimary = primaryAccent,
                accentSecondary = secondaryAccent,
                success = AstraPalette.SemanticSuccess,
                warning = AstraPalette.SemanticWarning,
                error = AstraPalette.SemanticError,
                info = AstraPalette.SemanticInfo
            )
        }
    }
}

/**
 * Corner Language (Section 6.4).
 */
@Immutable
data class AstraShapes(
    val smallControl: Shape = RoundedCornerShape(12.dp),
    val compactCard: Shape = RoundedCornerShape(16.dp),
    val standardCard: Shape = RoundedCornerShape(20.dp),
    val elevatedSheet: Shape = RoundedCornerShape(26.dp),
    val heroPanel: Shape = RoundedCornerShape(32.dp),
    val pill: Shape = CircleShape
)

/**
 * Spacing + Grid Rhythm (Section 8).
 */
@Immutable
data class AstraSpacing(
    val xxs: Dp = 4.dp,
    val xs: Dp = 8.dp,
    val sm: Dp = 12.dp,
    val md: Dp = 16.dp,
    val lg: Dp = 20.dp,
    val xl: Dp = 24.dp,
    val xxl: Dp = 28.dp,
    val xxxl: Dp = 32.dp,
    val section: Dp = 40.dp,
    val hero: Dp = 48.dp,
    val monumental: Dp = 64.dp,
    val compactMargin: Dp = 18.dp,
    val standardMargin: Dp = 22.dp,
    val largeMargin: Dp = 28.dp
)

/**
 * Typography Hierarchy (Section 7).
 * Supports dynamic scaling for Accessibility Large-Text Variant (Frame 40).
 */
@Immutable
data class AstraTypography(
    val displayXl: TextStyle,
    val displayL: TextStyle,
    val headlineXl: TextStyle,
    val headlineL: TextStyle,
    val headlineM: TextStyle,
    val titleL: TextStyle,
    val titleM: TextStyle,
    val bodyL: TextStyle,
    val bodyM: TextStyle,
    val bodyS: TextStyle,
    val labelL: TextStyle,
    val labelS: TextStyle
) {
    companion object {
        fun create(scale: Float = 1.0f): AstraTypography {
            val s = scale.coerceIn(0.85f, 1.45f)
            val sans = FontFamily.SansSerif
            return AstraTypography(
                displayXl = TextStyle(
                    fontFamily = sans,
                    fontWeight = FontWeight.Light,
                    fontSize = (72f * s).sp,
                    lineHeight = (78f * s).sp,
                    letterSpacing = (-1.5).sp
                ),
                displayL = TextStyle(
                    fontFamily = sans,
                    fontWeight = FontWeight.Light,
                    fontSize = (54f * s).sp,
                    lineHeight = (60f * s).sp,
                    letterSpacing = (-1.0).sp
                ),
                headlineXl = TextStyle(
                    fontFamily = sans,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = (34f * s).sp,
                    lineHeight = (40f * s).sp,
                    letterSpacing = (-0.5).sp
                ),
                headlineL = TextStyle(
                    fontFamily = sans,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = (28f * s).sp,
                    lineHeight = (34f * s).sp,
                    letterSpacing = (-0.3).sp
                ),
                headlineM = TextStyle(
                    fontFamily = sans,
                    fontWeight = FontWeight.Medium,
                    fontSize = (24f * s).sp,
                    lineHeight = (30f * s).sp
                ),
                titleL = TextStyle(
                    fontFamily = sans,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = (20f * s).sp,
                    lineHeight = (26f * s).sp
                ),
                titleM = TextStyle(
                    fontFamily = sans,
                    fontWeight = FontWeight.Medium,
                    fontSize = (17f * s).sp,
                    lineHeight = (23f * s).sp
                ),
                bodyL = TextStyle(
                    fontFamily = sans,
                    fontWeight = FontWeight.Normal,
                    fontSize = (16f * s).sp,
                    lineHeight = (24f * s).sp
                ),
                bodyM = TextStyle(
                    fontFamily = sans,
                    fontWeight = FontWeight.Normal,
                    fontSize = (14f * s).sp,
                    lineHeight = (20f * s).sp
                ),
                bodyS = TextStyle(
                    fontFamily = sans,
                    fontWeight = FontWeight.Normal,
                    fontSize = (12f * s).sp,
                    lineHeight = (17f * s).sp
                ),
                labelL = TextStyle(
                    fontFamily = sans,
                    fontWeight = FontWeight.Medium,
                    fontSize = (13f * s).sp,
                    lineHeight = (18f * s).sp,
                    letterSpacing = 0.2.sp
                ),
                labelS = TextStyle(
                    fontFamily = sans,
                    fontWeight = FontWeight.Medium,
                    fontSize = (11f * s).sp,
                    lineHeight = (15f * s).sp,
                    letterSpacing = 0.4.sp
                )
            )
        }
    }
}

/**
 * Motion System (Section 30).
 */
object AstraMotion {
    const val INSTANT_MS = 100
    const val QUICK_MS = 160
    const val STANDARD_MS = 220
    const val EMPHASIZED_MS = 320
    const val SPATIAL_MS = 420

    val CalmEasing: Easing = CubicBezierEasing(0.22f, 1.0f, 0.36f, 1.0f)
    val SpatialEasing: Easing = CubicBezierEasing(0.16f, 1.0f, 0.30f, 1.0f)

    fun <T> spec(baseDurationMs: Int, scale: Float = 1f): TweenSpec<T> {
        val effective = (baseDurationMs * scale).toInt().coerceAtLeast(0)
        return tween(durationMillis = effective, easing = CalmEasing)
    }
}

/**
 * Haptic Vocabulary (Section 31).
 */
enum class AstraHapticCue {
    LIGHT,
    MEDIUM,
    HEAVY,
    WARNING
}

object AstraHaptics {
    fun perform(view: View?, cue: AstraHapticCue, enabled: Boolean = true) {
        if (!enabled || view == null) return
        try {
            val constant = when (cue) {
                AstraHapticCue.LIGHT -> HapticFeedbackConstants.CLOCK_TICK
                AstraHapticCue.MEDIUM -> HapticFeedbackConstants.CONTEXT_CLICK
                AstraHapticCue.HEAVY -> HapticFeedbackConstants.LONG_PRESS
                AstraHapticCue.WARNING -> HapticFeedbackConstants.REJECT
            }
            view.performHapticFeedback(constant)
        } catch (_: Throwable) {
            // Safe no-op
        }
    }
}

/**
 * Sound Language (Section 32).
 * Conservative, short, respectful of system sound settings.
 */
enum class AstraSoundCue {
    UNLOCK,
    NAVIGATION,
    CHARGING_CONFIRM,
    ERROR
}

object AstraSoundEngine {
    fun playCue(cue: AstraSoundCue, enabled: Boolean = true) {
        if (!enabled) return
        try {
            val tone = when (cue) {
                AstraSoundCue.UNLOCK -> ToneGenerator.TONE_PROP_ACK
                AstraSoundCue.NAVIGATION -> ToneGenerator.TONE_PROP_BEEP
                AstraSoundCue.CHARGING_CONFIRM -> ToneGenerator.TONE_PROP_PROMPT
                AstraSoundCue.ERROR -> ToneGenerator.TONE_PROP_NACK
            }
            val generator = ToneGenerator(AudioManager.STREAM_SYSTEM, 25)
            generator.startTone(tone, 45)
        } catch (_: Throwable) {
            // Safe no-op in headless/muted environments
        }
    }
}

val LocalAstraColors = staticCompositionLocalOf { AstraColorEngine.buildSemanticColors(ThemeSettings()) }
val LocalAstraTypography = staticCompositionLocalOf { AstraTypography.create(1.0f) }
val LocalAstraShapes = staticCompositionLocalOf { AstraShapes() }
val LocalAstraSpacing = staticCompositionLocalOf { AstraSpacing() }
val LocalAstraVisualBudget = staticCompositionLocalOf {
    AstraVisualBudget(
        effectiveBlurRadiusDp = 20f,
        enableAtmosphericShaderOverlay = true,
        enableElevationShadows = true,
        motionDurationScale = 1.0f,
        isLowEndModeActive = false,
        totalRamMb = 6144L,
        availableRamMb = 3200L,
        coldStartDurationMs = 95L
    )
}

object AstraTheme {
    val colors: AstraSemanticColors
        @Composable
        @ReadOnlyComposable
        get() = LocalAstraColors.current

    val typography: AstraTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalAstraTypography.current

    val shapes: AstraShapes
        @Composable
        @ReadOnlyComposable
        get() = LocalAstraShapes.current

    val spacing: AstraSpacing
        @Composable
        @ReadOnlyComposable
        get() = LocalAstraSpacing.current

    val visualBudget: AstraVisualBudget
        @Composable
        @ReadOnlyComposable
        get() = LocalAstraVisualBudget.current
}

@Composable
fun AstraThemeProvider(
    themeSettings: ThemeSettings,
    visualBudget: AstraVisualBudget = LocalAstraVisualBudget.current,
    content: @Composable () -> Unit
) {
    val semanticColors = AstraColorEngine.buildSemanticColors(themeSettings)
    val typography = AstraTypography.create(themeSettings.textScaleMultiplier)
    val shapes = AstraShapes()
    val spacing = AstraSpacing()

    CompositionLocalProvider(
        LocalAstraColors provides semanticColors,
        LocalAstraTypography provides typography,
        LocalAstraShapes provides shapes,
        LocalAstraSpacing provides spacing,
        LocalAstraVisualBudget provides visualBudget,
        content = content
    )
}
