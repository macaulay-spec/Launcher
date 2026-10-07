package com.astra.launcher.core.design

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Astra Brand System (Section 4).
 * Built around orbital geometry, a small star point, a controlled arc, and an abstract A/orbit hybrid.
 * Clean at 16.dp and elegant at 128.dp.
 */
@Composable
fun AstraBrandMark(
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    luminousMoment: Boolean = false,
    tint: Color = AstraTheme.colors.textPrimary,
    accent: Color = AstraTheme.colors.accentPrimary
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val stroke = (w * 0.072f).coerceAtLeast(1.8f)

        if (luminousMoment) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(accent.copy(alpha = 0.34f), Color.Transparent),
                    center = Offset(w * 0.5f, h * 0.48f),
                    radius = w * 0.55f
                ),
                radius = w * 0.55f
            )
        }

        // Controlled elliptical orbital arc
        drawArc(
            color = accent,
            startAngle = 195f,
            sweepAngle = 235f,
            useCenter = false,
            topLeft = Offset(w * 0.10f, h * 0.24f),
            size = Size(w * 0.80f, h * 0.54f),
            style = Stroke(width = stroke * 0.9f, cap = StrokeCap.Round)
        )

        // Abstract 'A' apex & structural legs
        val apexPath = Path().apply {
            moveTo(w * 0.28f, h * 0.78f)
            lineTo(w * 0.50f, h * 0.18f)
            lineTo(w * 0.72f, h * 0.78f)
        }
        drawPath(
            path = apexPath,
            color = tint,
            style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Small precision star point at orbital intersection
        val starCenter = Offset(w * 0.74f, h * 0.30f)
        drawCircle(
            color = accent,
            radius = stroke * 0.85f,
            center = starCenter
        )
    }
}

/**
 * Coherent Astra Icon Family (Section 9).
 * Consistent optical stroke, rounded geometric structure, active/inactive variants.
 */
enum class AstraGlyph {
    SEARCH,
    HOME,
    APPS,
    SETTINGS,
    WIFI,
    BLUETOOTH,
    CELLULAR,
    AIRPLANE,
    HOTSPOT,
    FLASHLIGHT,
    BRIGHTNESS,
    VOLUME,
    BATTERY,
    LOCK,
    UNLOCK,
    NOTIFICATIONS,
    CONTROLS,
    WALLPAPER,
    WIDGETS,
    CLOCK,
    CALENDAR,
    CAMERA,
    PHONE,
    MESSAGES,
    BROWSER,
    PLAY,
    PAUSE,
    SKIP_NEXT,
    CHECK,
    WARNING,
    SHIELD,
    SPARK,
    EDIT,
    FOLDER,
    CLOSE,
    CHEVRON_RIGHT
}

@Composable
fun AstraVectorIcon(
    glyph: AstraGlyph,
    modifier: Modifier = Modifier,
    size: Dp = 22.dp,
    tint: Color = AstraTheme.colors.iconPrimary,
    active: Boolean = false
) {
    val accent = AstraTheme.colors.accentPrimary
    val drawColor = if (active) accent else tint

    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val strokeWidth = (w * 0.088f).coerceAtLeast(1.6f)
        val stroke = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)

        when (glyph) {
            AstraGlyph.SEARCH -> {
                drawCircle(
                    color = drawColor,
                    radius = w * 0.30f,
                    center = Offset(w * 0.44f, h * 0.44f),
                    style = stroke
                )
                drawLine(
                    color = drawColor,
                    start = Offset(w * 0.66f, h * 0.66f),
                    end = Offset(w * 0.86f, h * 0.86f),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )
            }
            AstraGlyph.HOME -> {
                val p = Path().apply {
                    moveTo(w * 0.16f, h * 0.46f)
                    lineTo(w * 0.50f, h * 0.16f)
                    lineTo(w * 0.84f, h * 0.46f)
                    lineTo(w * 0.84f, h * 0.84f)
                    lineTo(w * 0.16f, h * 0.84f)
                    close()
                }
                drawPath(p, color = drawColor, style = stroke)
            }
            AstraGlyph.APPS -> {
                val r = w * 0.12f
                val coords = listOf(0.28f to 0.28f, 0.72f to 0.28f, 0.28f to 0.72f, 0.72f to 0.72f)
                coords.forEach { (cx, cy) ->
                    drawCircle(
                        color = drawColor,
                        radius = r,
                        center = Offset(w * cx, h * cy),
                        style = if (active) Stroke(strokeWidth * 1.2f) else stroke
                    )
                }
            }
            AstraGlyph.SETTINGS -> {
                drawCircle(
                    color = drawColor,
                    radius = w * 0.22f,
                    center = Offset(w * 0.5f, h * 0.5f),
                    style = stroke
                )
                drawCircle(
                    color = drawColor,
                    radius = w * 0.38f,
                    center = Offset(w * 0.5f, h * 0.5f),
                    style = Stroke(width = strokeWidth * 0.75f, cap = StrokeCap.Round)
                )
            }
            AstraGlyph.WIFI -> {
                drawArc(
                    color = drawColor,
                    startAngle = 215f,
                    sweepAngle = 110f,
                    useCenter = false,
                    topLeft = Offset(w * 0.12f, h * 0.22f),
                    size = Size(w * 0.76f, h * 0.76f),
                    style = stroke
                )
                drawArc(
                    color = drawColor,
                    startAngle = 220f,
                    sweepAngle = 100f,
                    useCenter = false,
                    topLeft = Offset(w * 0.26f, h * 0.40f),
                    size = Size(w * 0.48f, h * 0.48f),
                    style = stroke
                )
                drawCircle(color = drawColor, radius = strokeWidth * 0.8f, center = Offset(w * 0.5f, h * 0.78f))
            }
            AstraGlyph.BLUETOOTH -> {
                val p = Path().apply {
                    moveTo(w * 0.32f, h * 0.28f)
                    lineTo(w * 0.68f, h * 0.68f)
                    lineTo(w * 0.50f, h * 0.86f)
                    lineTo(w * 0.50f, h * 0.14f)
                    lineTo(w * 0.68f, h * 0.32f)
                    lineTo(w * 0.32f, h * 0.72f)
                }
                drawPath(p, color = drawColor, style = stroke)
            }
            AstraGlyph.CELLULAR -> {
                val bars = listOf(0.22f to 0.70f, 0.42f to 0.54f, 0.62f to 0.38f, 0.82f to 0.20f)
                bars.forEach { (x, topY) ->
                    drawLine(
                        color = drawColor,
                        start = Offset(w * x, h * 0.82f),
                        end = Offset(w * x, h * topY),
                        strokeWidth = strokeWidth * 1.15f,
                        cap = StrokeCap.Round
                    )
                }
            }
            AstraGlyph.AIRPLANE -> {
                drawLine(
                    color = drawColor,
                    start = Offset(w * 0.5f, h * 0.15f),
                    end = Offset(w * 0.5f, h * 0.85f),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = drawColor,
                    start = Offset(w * 0.16f, h * 0.52f),
                    end = Offset(w * 0.84f, h * 0.52f),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )
            }
            AstraGlyph.HOTSPOT -> {
                drawCircle(color = drawColor, radius = strokeWidth * 0.9f, center = Offset(w * 0.5f, h * 0.5f))
                drawCircle(color = drawColor, radius = w * 0.26f, center = Offset(w * 0.5f, h * 0.5f), style = stroke)
                drawArc(
                    color = drawColor,
                    startAngle = 140f,
                    sweepAngle = 260f,
                    useCenter = false,
                    topLeft = Offset(w * 0.12f, h * 0.12f),
                    size = Size(w * 0.76f, h * 0.76f),
                    style = stroke
                )
            }
            AstraGlyph.FLASHLIGHT -> {
                drawRoundRect(
                    color = drawColor,
                    topLeft = Offset(w * 0.34f, h * 0.22f),
                    size = Size(w * 0.32f, h * 0.58f),
                    cornerRadius = CornerRadius(w * 0.08f, w * 0.08f),
                    style = stroke
                )
                drawLine(
                    color = drawColor,
                    start = Offset(w * 0.30f, h * 0.38f),
                    end = Offset(w * 0.70f, h * 0.38f),
                    strokeWidth = strokeWidth
                )
            }
            AstraGlyph.BRIGHTNESS -> {
                drawCircle(
                    color = drawColor,
                    radius = w * 0.20f,
                    center = Offset(w * 0.5f, h * 0.5f),
                    style = stroke
                )
                val rays = listOf(
                    Offset(0.5f, 0.12f) to Offset(0.5f, 0.20f),
                    Offset(0.5f, 0.80f) to Offset(0.5f, 0.88f),
                    Offset(0.12f, 0.5f) to Offset(0.20f, 0.5f),
                    Offset(0.80f, 0.5f) to Offset(0.88f, 0.5f)
                )
                rays.forEach { (s, e) ->
                    drawLine(
                        color = drawColor,
                        start = Offset(w * s.x, h * s.y),
                        end = Offset(w * e.x, h * e.y),
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Round
                    )
                }
            }
            AstraGlyph.VOLUME -> {
                val speaker = Path().apply {
                    moveTo(w * 0.18f, h * 0.40f)
                    lineTo(w * 0.34f, h * 0.40f)
                    lineTo(w * 0.54f, h * 0.24f)
                    lineTo(w * 0.54f, h * 0.76f)
                    lineTo(w * 0.34f, h * 0.60f)
                    lineTo(w * 0.18f, h * 0.60f)
                    close()
                }
                drawPath(speaker, color = drawColor, style = stroke)
                drawArc(
                    color = drawColor,
                    startAngle = -45f,
                    sweepAngle = 90f,
                    useCenter = false,
                    topLeft = Offset(w * 0.48f, h * 0.30f),
                    size = Size(w * 0.32f, h * 0.40f),
                    style = stroke
                )
            }
            AstraGlyph.BATTERY -> {
                drawRoundRect(
                    color = drawColor,
                    topLeft = Offset(w * 0.14f, h * 0.28f),
                    size = Size(w * 0.64f, h * 0.44f),
                    cornerRadius = CornerRadius(w * 0.10f, w * 0.10f),
                    style = stroke
                )
                drawLine(
                    color = drawColor,
                    start = Offset(w * 0.84f, h * 0.40f),
                    end = Offset(w * 0.84f, h * 0.60f),
                    strokeWidth = strokeWidth * 1.2f,
                    cap = StrokeCap.Round
                )
            }
            AstraGlyph.LOCK, AstraGlyph.UNLOCK -> {
                drawRoundRect(
                    color = drawColor,
                    topLeft = Offset(w * 0.22f, h * 0.44f),
                    size = Size(w * 0.56f, h * 0.40f),
                    cornerRadius = CornerRadius(w * 0.12f, w * 0.12f),
                    style = stroke
                )
                drawArc(
                    color = drawColor,
                    startAngle = 180f,
                    sweepAngle = if (glyph == AstraGlyph.LOCK) 180f else 130f,
                    useCenter = false,
                    topLeft = Offset(w * 0.30f, h * 0.18f),
                    size = Size(w * 0.40f, h * 0.44f),
                    style = stroke
                )
            }
            AstraGlyph.NOTIFICATIONS -> {
                drawArc(
                    color = drawColor,
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(w * 0.22f, h * 0.18f),
                    size = Size(w * 0.56f, h * 0.56f),
                    style = stroke
                )
                drawLine(
                    color = drawColor,
                    start = Offset(w * 0.18f, h * 0.70f),
                    end = Offset(w * 0.82f, h * 0.70f),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )
                drawCircle(color = drawColor, radius = strokeWidth * 0.75f, center = Offset(w * 0.5f, h * 0.84f))
            }
            AstraGlyph.CONTROLS -> {
                drawLine(drawColor, Offset(w * 0.20f, h * 0.34f), Offset(w * 0.80f, h * 0.34f), strokeWidth, StrokeCap.Round)
                drawCircle(drawColor, radius = w * 0.10f, center = Offset(w * 0.38f, h * 0.34f))
                drawLine(drawColor, Offset(w * 0.20f, h * 0.66f), Offset(w * 0.80f, h * 0.66f), strokeWidth, StrokeCap.Round)
                drawCircle(drawColor, radius = w * 0.10f, center = Offset(w * 0.64f, h * 0.66f))
            }
            AstraGlyph.WALLPAPER -> {
                drawRoundRect(
                    color = drawColor,
                    topLeft = Offset(w * 0.16f, h * 0.18f),
                    size = Size(w * 0.68f, h * 0.64f),
                    cornerRadius = CornerRadius(w * 0.12f, w * 0.12f),
                    style = stroke
                )
                drawArc(
                    color = drawColor,
                    startAngle = 200f,
                    sweepAngle = 140f,
                    useCenter = false,
                    topLeft = Offset(w * 0.24f, h * 0.38f),
                    size = Size(w * 0.52f, h * 0.40f),
                    style = stroke
                )
            }
            AstraGlyph.WIDGETS -> {
                drawRoundRect(drawColor, Offset(w * 0.16f, h * 0.16f), Size(w * 0.30f, h * 0.30f), CornerRadius(6f, 6f), stroke)
                drawRoundRect(drawColor, Offset(w * 0.54f, h * 0.16f), Size(w * 0.30f, h * 0.30f), CornerRadius(6f, 6f), stroke)
                drawRoundRect(drawColor, Offset(w * 0.16f, h * 0.54f), Size(w * 0.68f, h * 0.30f), CornerRadius(6f, 6f), stroke)
            }
            AstraGlyph.CLOCK -> {
                drawCircle(drawColor, radius = w * 0.36f, center = Offset(w * 0.5f, h * 0.5f), style = stroke)
                drawLine(drawColor, Offset(w * 0.5f, h * 0.5f), Offset(w * 0.5f, h * 0.30f), strokeWidth, StrokeCap.Round)
                drawLine(drawColor, Offset(w * 0.5f, h * 0.5f), Offset(w * 0.66f, h * 0.56f), strokeWidth, StrokeCap.Round)
            }
            AstraGlyph.CALENDAR -> {
                drawRoundRect(drawColor, Offset(w * 0.18f, h * 0.24f), Size(w * 0.64f, h * 0.58f), CornerRadius(8f, 8f), stroke)
                drawLine(drawColor, Offset(w * 0.18f, h * 0.42f), Offset(w * 0.82f, h * 0.42f), strokeWidth)
            }
            AstraGlyph.CAMERA -> {
                drawRoundRect(drawColor, Offset(w * 0.16f, h * 0.28f), Size(w * 0.68f, h * 0.50f), CornerRadius(10f, 10f), stroke)
                drawCircle(drawColor, radius = w * 0.14f, center = Offset(w * 0.5f, h * 0.53f), style = stroke)
            }
            AstraGlyph.PHONE -> {
                drawArc(
                    color = drawColor,
                    startAngle = 110f,
                    sweepAngle = 140f,
                    useCenter = false,
                    topLeft = Offset(w * 0.22f, h * 0.18f),
                    size = Size(w * 0.56f, h * 0.64f),
                    style = Stroke(width = strokeWidth * 1.25f, cap = StrokeCap.Round)
                )
            }
            AstraGlyph.MESSAGES -> {
                drawRoundRect(drawColor, Offset(w * 0.16f, h * 0.22f), Size(w * 0.68f, h * 0.50f), CornerRadius(12f, 12f), stroke)
                drawLine(drawColor, Offset(w * 0.32f, h * 0.44f), Offset(w * 0.68f, h * 0.44f), strokeWidth, StrokeCap.Round)
            }
            AstraGlyph.BROWSER -> {
                drawCircle(drawColor, radius = w * 0.36f, center = Offset(w * 0.5f, h * 0.5f), style = stroke)
                drawLine(drawColor, Offset(w * 0.16f, h * 0.5f), Offset(w * 0.84f, h * 0.5f), strokeWidth)
            }
            AstraGlyph.PLAY -> {
                val p = Path().apply {
                    moveTo(w * 0.34f, h * 0.24f)
                    lineTo(w * 0.74f, h * 0.50f)
                    lineTo(w * 0.34f, h * 0.76f)
                    close()
                }
                drawPath(p, color = drawColor)
            }
            AstraGlyph.PAUSE -> {
                drawLine(drawColor, Offset(w * 0.36f, h * 0.24f), Offset(w * 0.36f, h * 0.76f), strokeWidth * 1.4f, StrokeCap.Round)
                drawLine(drawColor, Offset(w * 0.64f, h * 0.24f), Offset(w * 0.64f, h * 0.76f), strokeWidth * 1.4f, StrokeCap.Round)
            }
            AstraGlyph.SKIP_NEXT -> {
                val p = Path().apply {
                    moveTo(w * 0.26f, h * 0.26f)
                    lineTo(w * 0.62f, h * 0.50f)
                    lineTo(w * 0.26f, h * 0.74f)
                    close()
                }
                drawPath(p, color = drawColor)
                drawLine(drawColor, Offset(w * 0.72f, h * 0.26f), Offset(w * 0.72f, h * 0.74f), strokeWidth * 1.2f, StrokeCap.Round)
            }
            AstraGlyph.CHECK -> {
                val p = Path().apply {
                    moveTo(w * 0.22f, h * 0.52f)
                    lineTo(w * 0.42f, h * 0.72f)
                    lineTo(w * 0.78f, h * 0.30f)
                }
                drawPath(p, color = drawColor, style = stroke)
            }
            AstraGlyph.WARNING -> {
                val p = Path().apply {
                    moveTo(w * 0.50f, h * 0.18f)
                    lineTo(w * 0.84f, h * 0.78f)
                    lineTo(w * 0.16f, h * 0.78f)
                    close()
                }
                drawPath(p, color = drawColor, style = stroke)
                drawLine(drawColor, Offset(w * 0.5f, h * 0.42f), Offset(w * 0.5f, h * 0.58f), strokeWidth, StrokeCap.Round)
                drawCircle(drawColor, radius = strokeWidth * 0.6f, center = Offset(w * 0.5f, h * 0.68f))
            }
            AstraGlyph.SHIELD -> {
                val p = Path().apply {
                    moveTo(w * 0.50f, h * 0.14f)
                    lineTo(w * 0.80f, h * 0.28f)
                    lineTo(w * 0.74f, h * 0.64f)
                    lineTo(w * 0.50f, h * 0.86f)
                    lineTo(w * 0.26f, h * 0.64f)
                    lineTo(w * 0.20f, h * 0.28f)
                    close()
                }
                drawPath(p, color = drawColor, style = stroke)
            }
            AstraGlyph.SPARK -> {
                drawLine(drawColor, Offset(w * 0.5f, h * 0.16f), Offset(w * 0.5f, h * 0.84f), strokeWidth, StrokeCap.Round)
                drawLine(drawColor, Offset(w * 0.16f, h * 0.5f), Offset(w * 0.84f, h * 0.5f), strokeWidth, StrokeCap.Round)
                drawCircle(accent, radius = strokeWidth * 1.1f, center = Offset(w * 0.5f, h * 0.5f))
            }
            AstraGlyph.EDIT -> {
                drawLine(drawColor, Offset(w * 0.24f, h * 0.76f), Offset(w * 0.76f, h * 0.24f), strokeWidth * 1.2f, StrokeCap.Round)
            }
            AstraGlyph.FOLDER -> {
                drawRoundRect(drawColor, Offset(w * 0.16f, h * 0.28f), Size(w * 0.68f, h * 0.50f), CornerRadius(10f, 10f), stroke)
                drawLine(drawColor, Offset(w * 0.22f, h * 0.28f), Offset(w * 0.46f, h * 0.28f), strokeWidth * 1.5f, StrokeCap.Round)
            }
            AstraGlyph.CLOSE -> {
                drawLine(drawColor, Offset(w * 0.26f, h * 0.26f), Offset(w * 0.74f, h * 0.74f), strokeWidth, StrokeCap.Round)
                drawLine(drawColor, Offset(w * 0.74f, h * 0.26f), Offset(w * 0.26f, h * 0.74f), strokeWidth, StrokeCap.Round)
            }
            AstraGlyph.CHEVRON_RIGHT -> {
                val p = Path().apply {
                    moveTo(w * 0.38f, h * 0.24f)
                    lineTo(w * 0.64f, h * 0.50f)
                    lineTo(w * 0.38f, h * 0.76f)
                }
                drawPath(p, color = drawColor, style = stroke)
            }
        }
    }
}
