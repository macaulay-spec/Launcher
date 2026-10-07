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
 * Astra Brand Mark (Section 18).
 * Orbital geometry with precision star point and abstract 'A' structure.
 */
@Composable
fun AstraBrandMark(
    accentColor: Color = Color(0xFF7DD3FC),
    secondaryColor: Color = Color(0xFFA78BFA),
    size: Dp = 44.dp,
    modifier: Modifier = Modifier,
    luminousMoment: Boolean = false
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val stroke = (w * 0.072f).coerceAtLeast(1.8f)

        if (luminousMoment) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(accentColor.copy(alpha = 0.34f), Color.Transparent),
                    center = Offset(w * 0.5f, h * 0.48f),
                    radius = w * 0.55f
                ),
                radius = w * 0.55f
            )
        }

        drawArc(
            color = accentColor,
            startAngle = 195f,
            sweepAngle = 235f,
            useCenter = false,
            topLeft = Offset(w * 0.10f, h * 0.24f),
            size = Size(w * 0.80f, h * 0.54f),
            style = Stroke(width = stroke * 0.9f, cap = StrokeCap.Round)
        )

        val apexPath = Path().apply {
            moveTo(w * 0.28f, h * 0.78f)
            lineTo(w * 0.50f, h * 0.18f)
            lineTo(w * 0.72f, h * 0.78f)
        }
        drawPath(
            path = apexPath,
            color = Color(0xFFF5F6F8),
            style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        drawCircle(
            color = secondaryColor,
            radius = stroke * 0.85f,
            center = Offset(w * 0.74f, h * 0.30f)
        )
    }
}

/**
 * Astra UI Chrome Vector Glyphs.
 * Used strictly for launcher chrome (Search, Settings, App Drawer button, Close, Edit) —
 * NEVER used as a replacement for real third-party application icons (Section 6).
 */
enum class AstraGlyph {
    SEARCH,
    HOME,
    APPS,
    APPS_GRID,
    SETTINGS,
    WIFI,
    BLUETOOTH,
    WALLPAPER,
    WIDGETS,
    CLOCK,
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
    tint: Color = Color(0xFFF5F6F8),
    size: Dp = 22.dp,
    modifier: Modifier = Modifier,
    active: Boolean = false
) {
    val drawColor = if (active) Color(0xFF7DD3FC) else tint

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
            AstraGlyph.APPS, AstraGlyph.APPS_GRID -> {
                val r = w * 0.12f
                val coords = listOf(0.28f to 0.28f, 0.72f to 0.28f, 0.28f to 0.72f, 0.72f to 0.72f)
                coords.forEach { (cx, cy) ->
                    drawCircle(
                        color = drawColor,
                        radius = r,
                        center = Offset(w * cx, h * cy),
                        style = stroke
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
                    radius = w * 0.36f,
                    center = Offset(w * 0.5f, h * 0.5f),
                    style = Stroke(width = strokeWidth * 0.75f, cap = StrokeCap.Round)
                )
            }
            AstraGlyph.CLOSE -> {
                drawLine(drawColor, Offset(w * 0.26f, h * 0.26f), Offset(w * 0.74f, h * 0.74f), strokeWidth, StrokeCap.Round)
                drawLine(drawColor, Offset(w * 0.74f, h * 0.26f), Offset(w * 0.26f, h * 0.74f), strokeWidth, StrokeCap.Round)
            }
            AstraGlyph.FOLDER -> {
                drawRoundRect(drawColor, Offset(w * 0.16f, h * 0.28f), Size(w * 0.68f, h * 0.50f), CornerRadius(10f, 10f), stroke)
                drawLine(drawColor, Offset(w * 0.22f, h * 0.28f), Offset(w * 0.46f, h * 0.28f), strokeWidth * 1.5f, StrokeCap.Round)
            }
            else -> {
                drawCircle(
                    color = drawColor,
                    radius = w * 0.28f,
                    center = Offset(w * 0.5f, h * 0.5f),
                    style = stroke
                )
            }
        }
    }
}
