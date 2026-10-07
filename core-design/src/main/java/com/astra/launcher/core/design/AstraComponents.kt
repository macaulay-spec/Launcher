package com.astra.launcher.core.design

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.astra.launcher.core.storage.AstraAppEntry
import com.astra.launcher.core.storage.AstraClockStyle
import com.astra.launcher.core.storage.AstraIconStyle
import com.astra.launcher.core.storage.AstraMediaState
import com.astra.launcher.core.storage.AstraNotificationEntry
import com.astra.launcher.core.storage.AstraWallpaperId

/**
 * Component Variant State (Section 46).
 */
enum class AstraComponentState {
    DEFAULT,
    PRESSED,
    FOCUSED,
    DISABLED,
    SELECTED,
    ACTIVE,
    LOADING,
    ERROR
}

/**
 * 18. AstraWallpaperSurface (Section 10 & 46).
 * Renders the curated high-res Astra wallpapers with safe-zone luminance gradients
 * and optional atmospheric orbital light.
 */
@Composable
fun AstraWallpaperSurface(
    wallpaperId: AstraWallpaperId,
    modifier: Modifier = Modifier,
    dimAmount: Float = 0f,
    enableAtmosphericOverlay: Boolean = AstraTheme.visualBudget.enableAtmosphericShaderOverlay,
    content: @Composable BoxScope.() -> Unit = {}
) {
    val drawableRes = when (wallpaperId) {
        AstraWallpaperId.ORBIT_DAWN -> R.drawable.wallpaper_orbit_dawn
        AstraWallpaperId.NOCTURNE_FLOW -> R.drawable.wallpaper_nocturne_flow
        AstraWallpaperId.GLASS_HORIZON -> R.drawable.wallpaper_glass_horizon
        AstraWallpaperId.MORNING_MIST -> R.drawable.wallpaper_morning_mist
        AstraWallpaperId.GRAPHITE_MONOLITH -> R.drawable.wallpaper_graphite_monolith
    }

    val accent = Color(wallpaperId.recommendedAccentHex)
    val secondary = Color(wallpaperId.secondaryAtmosphereHex)
    val isLightWallpaper = wallpaperId.upperRegionLuminance > 0.65f

    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = drawableRes),
            contentDescription = "Astra Wallpaper ${wallpaperId.title}",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        if (enableAtmosphericOverlay) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(accent.copy(alpha = 0.12f), Color.Transparent),
                        center = Offset(w * 0.78f, h * 0.68f),
                        radius = w * 0.65f
                    ),
                    radius = w * 0.65f,
                    center = Offset(w * 0.78f, h * 0.68f)
                )
                drawArc(
                    color = secondary.copy(alpha = 0.14f),
                    startAngle = 195f,
                    sweepAngle = 155f,
                    useCenter = false,
                    topLeft = Offset(-w * 0.15f, h * 0.30f),
                    size = Size(w * 1.30f, h * 0.48f),
                    style = Stroke(width = 2.2f, cap = StrokeCap.Round)
                )
            }
        }

        // Top & bottom subtle readability safe-zone gradients (Section 10.2)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.0f to (if (isLightWallpaper) Color.White.copy(alpha = 0.16f) else Color.Black.copy(alpha = 0.28f)),
                        0.28f to Color.Transparent,
                        0.72f to Color.Transparent,
                        1.0f to (if (isLightWallpaper) Color.Black.copy(alpha = 0.14f) else Color.Black.copy(alpha = 0.45f))
                    )
                )
        )

        if (dimAmount > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = dimAmount.coerceIn(0f, 0.88f)))
            )
        }

        content()
    }
}

/**
 * 8. AstraCard — Supports the 3 primary surface families (Section 6.1):
 * A. SOLID, B. SOFT_TRANSLUCENT, C. CLEAR_ATMOSPHERIC.
 */
enum class AstraSurfaceFamily {
    SOLID,
    SOFT_TRANSLUCENT,
    CLEAR_ATMOSPHERIC
}

@Composable
fun AstraCard(
    modifier: Modifier = Modifier,
    family: AstraSurfaceFamily = AstraSurfaceFamily.SOFT_TRANSLUCENT,
    shape: Shape = AstraTheme.shapes.standardCard,
    state: AstraComponentState = AstraComponentState.DEFAULT,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val colors = AstraTheme.colors
    val bgColor = when (family) {
        AstraSurfaceFamily.SOLID -> colors.surfaceRaised
        AstraSurfaceFamily.SOFT_TRANSLUCENT -> colors.surfaceGlass
        AstraSurfaceFamily.CLEAR_ATMOSPHERIC -> colors.surfaceGlass.copy(alpha = if (colors.isLight) 0.58f else 0.44f)
    }
    val borderColor = when (state) {
        AstraComponentState.FOCUSED, AstraComponentState.SELECTED, AstraComponentState.ACTIVE -> colors.accentPrimary
        AstraComponentState.ERROR -> colors.error
        else -> colors.borderSubtle
    }

    Surface(
        modifier = modifier.then(
            if (onClick != null && state != AstraComponentState.DISABLED) {
                Modifier.clip(shape).clickable { onClick() }
            } else Modifier
        ),
        shape = shape,
        color = bgColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        content()
    }
}

/**
 * 1. AstraIconButton (Section 46).
 */
@Composable
fun AstraIconButton(
    glyph: AstraGlyph,
    contentDescriptionLabel: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    state: AstraComponentState = AstraComponentState.DEFAULT,
    size: Dp = 42.dp
) {
    val colors = AstraTheme.colors
    val isActive = state == AstraComponentState.ACTIVE || state == AstraComponentState.SELECTED
    val bg by animateColorAsState(
        targetValue = when {
            state == AstraComponentState.DISABLED -> colors.surfaceGlass.copy(alpha = 0.25f)
            isActive -> colors.accentPrimary.copy(alpha = 0.22f)
            else -> colors.surfaceGlass
        },
        label = "IconBtnBg"
    )
    val border = if (isActive || state == AstraComponentState.FOCUSED) {
        colors.accentPrimary.copy(alpha = 0.65f)
    } else {
        colors.borderSubtle
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(bg)
            .border(1.dp, border, CircleShape)
            .clickable(enabled = state != AstraComponentState.DISABLED) { onClick() }
            .semantics { contentDescription = contentDescriptionLabel },
        contentAlignment = Alignment.Center
    ) {
        AstraVectorIcon(
            glyph = glyph,
            size = size * 0.48f,
            tint = if (state == AstraComponentState.DISABLED) colors.textDisabled else colors.iconPrimary,
            active = isActive
        )
    }
}

/**
 * 2. AstraToggle (Section 46).
 */
@Composable
fun AstraToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    label: String = "Toggle"
) {
    val colors = AstraTheme.colors
    val trackColor by animateColorAsState(
        targetValue = when {
            !enabled -> colors.surfaceFloating.copy(alpha = 0.4f)
            checked -> colors.accentPrimary
            else -> colors.surfaceFloating
        },
        label = "ToggleTrack"
    )
    val thumbColor = if (checked) AstraPalette.AstraBlack else colors.textSecondary

    Box(
        modifier = modifier
            .width(48.dp)
            .height(26.dp)
            .clip(CircleShape)
            .background(trackColor)
            .border(1.dp, colors.borderSubtle, CircleShape)
            .clickable(enabled = enabled) { onCheckedChange(!checked) }
            .padding(horizontal = 4.dp)
            .semantics { contentDescription = "$label: ${if (checked) "On" else "Off"}" },
        contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(CircleShape)
                .background(thumbColor)
        )
    }
}

/**
 * 3. AstraQuickTile (Section 18 & 46).
 * Spatial Control Center tile supporting inactive, active, unavailable, processing, and handoff badges.
 */
@Composable
fun AstraQuickTile(
    title: String,
    subtitle: String,
    glyph: AstraGlyph,
    state: AstraComponentState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    isSystemHandoff: Boolean = false
) {
    val colors = AstraTheme.colors
    val isActive = state == AstraComponentState.ACTIVE || state == AstraComponentState.SELECTED
    val bg by animateColorAsState(
        targetValue = when (state) {
            AstraComponentState.ACTIVE, AstraComponentState.SELECTED -> colors.accentPrimary.copy(alpha = 0.20f)
            AstraComponentState.DISABLED -> colors.surfaceGlass.copy(alpha = 0.35f)
            AstraComponentState.ERROR -> colors.error.copy(alpha = 0.18f)
            else -> colors.surfaceGlass
        },
        label = "TileBg"
    )
    val border = when (state) {
        AstraComponentState.ACTIVE, AstraComponentState.SELECTED, AstraComponentState.FOCUSED ->
            colors.accentPrimary.copy(alpha = 0.65f)
        AstraComponentState.ERROR -> colors.error
        else -> colors.borderSubtle
    }

    Row(
        modifier = modifier
            .clip(AstraTheme.shapes.compactCard)
            .background(bg)
            .border(1.dp, border, AstraTheme.shapes.compactCard)
            .clickable(enabled = state != AstraComponentState.DISABLED) { onClick() }
            .padding(horizontal = 14.dp, vertical = if (compact) 10.dp else 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(
                    if (isActive) colors.accentPrimary.copy(alpha = 0.24f)
                    else colors.surfaceFloating.copy(alpha = 0.7f)
                ),
            contentAlignment = Alignment.Center
        ) {
            AstraVectorIcon(
                glyph = glyph,
                size = 18.dp,
                active = isActive
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = AstraTheme.typography.labelL,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = if (state == AstraComponentState.LOADING) "Syncing..." else subtitle,
                style = AstraTheme.typography.labelS,
                color = if (isActive) colors.accentPrimary else colors.textTertiary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (isSystemHandoff) {
            AstraVectorIcon(
                glyph = AstraGlyph.CHEVRON_RIGHT,
                size = 14.dp,
                tint = colors.textTertiary
            )
        }
    }
}

/**
 * 4. AstraSearchField (Section 15, 16, 46).
 */
@Composable
fun AstraSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String = "Search apps, commands, settings...",
    modifier: Modifier = Modifier,
    onClear: () -> Unit = {}
) {
    val colors = AstraTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(AstraTheme.shapes.pill)
            .background(colors.surfaceGlassStrong)
            .border(1.dp, colors.accentPrimary.copy(alpha = 0.42f), AstraTheme.shapes.pill)
            .padding(horizontal = 18.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AstraVectorIcon(
            glyph = AstraGlyph.SEARCH,
            size = 20.dp,
            tint = colors.accentPrimary,
            active = true
        )
        Spacer(modifier = Modifier.width(12.dp))
        Box(modifier = Modifier.weight(1f)) {
            if (query.isEmpty()) {
                Text(
                    text = placeholder,
                    style = AstraTheme.typography.bodyM,
                    color = colors.textTertiary
                )
            }
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = AstraTheme.typography.bodyM.copy(color = colors.textPrimary),
                cursorBrush = SolidColor(colors.accentPrimary),
                modifier = Modifier.fillMaxWidth()
            )
        }
        if (query.isNotEmpty()) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(colors.surfaceFloating)
                    .clickable { onClear() },
                contentAlignment = Alignment.Center
            ) {
                AstraVectorIcon(glyph = AstraGlyph.CLOSE, size = 12.dp, tint = colors.textSecondary)
            }
        }
    }
}

/**
 * 5. AstraAppIcon (Section 9, 13, 14, 42, 46).
 * Renders harmonized Astra icon container with optical stroke glyph, badge dot, and long-press support.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AstraAppIcon(
    app: AstraAppEntry,
    iconStyle: AstraIconStyle,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    showLabel: Boolean = true,
    iconScale: Float = 1.0f,
    showNotificationDot: Boolean = false
) {
    val colors = AstraTheme.colors
    val baseSize = (54f * iconScale.coerceIn(0.85f, 1.2f)).dp
    val appAccent = Color(app.accentTintHex)

    val containerBg = when (iconStyle) {
        AstraIconStyle.ASTRA_ADAPTIVE -> colors.surfaceGlassStrong
        AstraIconStyle.MONOCHROME_GLASS -> colors.surfaceGlass.copy(alpha = 0.55f)
        AstraIconStyle.GRAPHITE_SOLID -> AstraPalette.Graphite1
        AstraIconStyle.ORIGINAL -> appAccent.copy(alpha = 0.22f)
    }

    val glyph = resolveGlyphForPackage(app.packageName, app.label)

    Column(
        modifier = modifier
            .clip(AstraTheme.shapes.compactCard)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(vertical = 6.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            Box(
                modifier = Modifier
                    .size(baseSize)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                containerBg,
                                appAccent.copy(alpha = if (iconStyle == AstraIconStyle.MONOCHROME_GLASS) 0.10f else 0.18f)
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        color = if (iconStyle == AstraIconStyle.MONOCHROME_GLASS) colors.borderSubtle else appAccent.copy(alpha = 0.35f),
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                AstraVectorIcon(
                    glyph = glyph,
                    size = baseSize * 0.46f,
                    tint = if (iconStyle == AstraIconStyle.MONOCHROME_GLASS) colors.textPrimary else appAccent,
                    active = iconStyle != AstraIconStyle.MONOCHROME_GLASS
                )
            }
            if (showNotificationDot) {
                Box(
                    modifier = Modifier
                        .padding(3.dp)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(colors.accentPrimary)
                )
            }
        }
        if (showLabel) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = app.label,
                style = AstraTheme.typography.labelS,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

fun resolveGlyphForPackage(pkg: String, label: String): AstraGlyph {
    val hay = "$pkg $label".lowercase()
    return when {
        hay.contains("dial") || hay.contains("phone") || hay.contains("contact") -> AstraGlyph.PHONE
        hay.contains("messag") || hay.contains("sms") -> AstraGlyph.MESSAGES
        hay.contains("camera") || hay.contains("photo") || hay.contains("gallery") -> AstraGlyph.CAMERA
        hay.contains("chrome") || hay.contains("browser") || hay.contains("maps") -> AstraGlyph.BROWSER
        hay.contains("setting") -> AstraGlyph.SETTINGS
        hay.contains("calendar") -> AstraGlyph.CALENDAR
        hay.contains("clock") -> AstraGlyph.CLOCK
        hay.contains("youtube") || hay.contains("music") -> AstraGlyph.PLAY
        hay.contains("calculat") || hay.contains("vending") -> AstraGlyph.WIDGETS
        hay.contains("document") || hay.contains("file") -> AstraGlyph.FOLDER
        else -> AstraGlyph.SPARK
    }
}

/**
 * 6. AstraDock (Section 13 & 46).
 * Adaptive bottom dock with 4-5 primary apps and subtle spatial elevation.
 */
@Composable
fun AstraDock(
    apps: List<AstraAppEntry>,
    iconStyle: AstraIconStyle,
    glassEnabled: Boolean,
    onAppClick: (AstraAppEntry) -> Unit,
    onAppLongClick: (AstraAppEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AstraTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(AstraTheme.shapes.elevatedSheet)
            .background(if (glassEnabled) colors.surfaceGlassStrong else colors.surfaceRaised)
            .border(1.dp, colors.borderSubtle, AstraTheme.shapes.elevatedSheet)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        apps.take(5).forEach { app ->
            AstraAppIcon(
                app = app,
                iconStyle = iconStyle,
                showLabel = false,
                iconScale = 0.96f,
                onClick = { onAppClick(app) },
                onLongClick = { onAppLongClick(app) }
            )
        }
    }
}

/**
 * 14. AstraClock (Section 11, 13, 46).
 * Implements all 3 signature clock styles:
 * 1. Minimal Numeral, 2. Editorial Stacked, 3. Orbital Compact.
 * Adapts to wallpaper upper-region luminance so it never loses legibility.
 */
@Composable
fun AstraClock(
    style: AstraClockStyle,
    timeHours: String = "09",
    timeMinutes: String = "41",
    dateText: String = "WEDNESDAY, OCT 7",
    weatherSummary: String = "18°C · Clear Atmosphere",
    upperLuminance: Float = 0.22f,
    isLockScreen: Boolean = false,
    modifier: Modifier = Modifier
) {
    val colors = AstraTheme.colors
    val adaptiveClockColor = if (upperLuminance > 0.65f) AstraPalette.Ink else AstraPalette.Cloud
    val secondaryClockColor = adaptiveClockColor.copy(alpha = 0.76f)

    when (style) {
        AstraClockStyle.MINIMAL_NUMERAL -> {
            Column(
                modifier = modifier,
                horizontalAlignment = if (isLockScreen) Alignment.CenterHorizontally else Alignment.Start
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = dateText,
                        style = AstraTheme.typography.labelL,
                        color = colors.accentPrimary
                    )
                    Text(
                        text = "·",
                        style = AstraTheme.typography.labelL,
                        color = secondaryClockColor
                    )
                    Text(
                        text = weatherSummary,
                        style = AstraTheme.typography.labelL,
                        color = secondaryClockColor
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$timeHours:$timeMinutes",
                    style = if (isLockScreen) AstraTheme.typography.displayXl else AstraTheme.typography.displayL,
                    color = adaptiveClockColor
                )
            }
        }

        AstraClockStyle.EDITORIAL_STACKED -> {
            Column(
                modifier = modifier,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = dateText,
                    style = AstraTheme.typography.labelL,
                    color = secondaryClockColor
                )
                Text(
                    text = timeHours,
                    style = AstraTheme.typography.displayXl.copy(fontWeight = FontWeight.SemiBold),
                    color = adaptiveClockColor
                )
                Text(
                    text = timeMinutes,
                    style = AstraTheme.typography.displayXl.copy(fontWeight = FontWeight.Light),
                    color = colors.accentPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = weatherSummary,
                    style = AstraTheme.typography.bodyS,
                    color = secondaryClockColor
                )
            }
        }

        AstraClockStyle.ORBITAL_COMPACT -> {
            Row(
                modifier = modifier
                    .clip(AstraTheme.shapes.elevatedSheet)
                    .background(colors.surfaceGlass)
                    .border(1.dp, colors.borderSubtle, AstraTheme.shapes.elevatedSheet)
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AstraBrandMark(size = 34.dp, luminousMoment = true)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "$timeHours:$timeMinutes",
                            style = AstraTheme.typography.headlineXl,
                            color = colors.textPrimary
                        )
                        Text(
                            text = dateText,
                            style = AstraTheme.typography.labelS,
                            color = colors.textSecondary
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "18°C",
                        style = AstraTheme.typography.titleL,
                        color = colors.accentPrimary
                    )
                    Text(
                        text = "Calm · AQI 14",
                        style = AstraTheme.typography.labelS,
                        color = colors.textTertiary
                    )
                }
            }
        }
    }
}

/**
 * 12. AstraNotificationCard (Section 17 & 46).
 */
@Composable
fun AstraNotificationCard(
    item: AstraNotificationEntry,
    hideSensitiveContent: Boolean,
    onOpen: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AstraTheme.colors
    AstraCard(
        modifier = modifier.fillMaxWidth(),
        family = AstraSurfaceFamily.SOFT_TRANSLUCENT,
        shape = AstraTheme.shapes.standardCard,
        onClick = onOpen
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(colors.accentPrimary.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                AstraVectorIcon(
                    glyph = resolveGlyphForPackage(item.packageName, item.appName),
                    size = 18.dp,
                    tint = colors.accentPrimary,
                    active = true
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = item.appName,
                            style = AstraTheme.typography.labelS,
                            color = colors.accentPrimary
                        )
                        if (item.groupCount > 1) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "+${item.groupCount - 1}",
                                style = AstraTheme.typography.labelS,
                                color = colors.textTertiary
                            )
                        }
                    }
                    Text(
                        text = item.timestampLabel,
                        style = AstraTheme.typography.labelS,
                        color = colors.textTertiary
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = item.title,
                    style = AstraTheme.typography.bodyM.copy(fontWeight = FontWeight.Medium),
                    color = colors.textPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (hideSensitiveContent && item.isSensitive) {
                        "Sensitive notification content hidden while locked"
                    } else {
                        item.content
                    },
                    style = AstraTheme.typography.bodyS,
                    color = colors.textSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (item.isClearable) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    AstraVectorIcon(glyph = AstraGlyph.CLOSE, size = 12.dp, tint = colors.textTertiary)
                }
            }
        }
    }
}

/**
 * 13. AstraMediaCard (Section 26 & 46).
 * Adapts to album artwork tint while preserving contrast and Astra identity.
 */
@Composable
fun AstraMediaCard(
    media: AstraMediaState,
    onPlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AstraTheme.colors
    val artworkTint = Color(media.artworkDominantHex)
    val secondaryTint = Color(media.artworkSecondaryHex)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = AstraTheme.shapes.elevatedSheet,
        color = Color.Transparent,
        border = BorderStroke(1.dp, artworkTint.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            colors.surfaceGlassStrong,
                            artworkTint.copy(alpha = 0.22f),
                            secondaryTint.copy(alpha = 0.16f)
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Album artwork procedural tile
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(AstraTheme.shapes.compactCard)
                        .background(
                            Brush.linearGradient(
                                listOf(artworkTint.copy(alpha = 0.65f), secondaryTint.copy(alpha = 0.75f))
                            )
                        )
                        .border(1.dp, Color.White.copy(alpha = 0.25f), AstraTheme.shapes.compactCard),
                    contentAlignment = Alignment.Center
                ) {
                    AstraBrandMark(size = 28.dp, tint = Color.White, accent = Color.White)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = media.outputDeviceName.uppercase(),
                        style = AstraTheme.typography.labelS,
                        color = artworkTint
                    )
                    Text(
                        text = media.trackTitle,
                        style = AstraTheme.typography.bodyM.copy(fontWeight = FontWeight.SemiBold),
                        color = colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${media.artist} · ${media.album}",
                        style = AstraTheme.typography.bodyS,
                        color = colors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AstraIconButton(
                        glyph = if (media.isPlaying) AstraGlyph.PAUSE else AstraGlyph.PLAY,
                        contentDescriptionLabel = if (media.isPlaying) "Pause" else "Play",
                        onClick = onPlayPause,
                        state = if (media.isPlaying) AstraComponentState.ACTIVE else AstraComponentState.DEFAULT,
                        size = 40.dp
                    )
                    AstraIconButton(
                        glyph = AstraGlyph.SKIP_NEXT,
                        contentDescriptionLabel = "Next Track",
                        onClick = onSkipNext,
                        size = 40.dp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { media.progressFraction.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(CircleShape),
                color = artworkTint,
                trackColor = colors.surfaceFloating
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = media.elapsedLabel, style = AstraTheme.typography.labelS, color = colors.textTertiary)
                Text(text = media.durationLabel, style = AstraTheme.typography.labelS, color = colors.textTertiary)
            }
        }
    }
}

/**
 * 15. AstraSegmentedControl (Section 46).
 */
@Composable
fun <T> AstraSegmentedControl(
    items: List<T>,
    selectedItem: T,
    labelProvider: (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AstraTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(AstraTheme.shapes.pill)
            .background(colors.surfaceGlass)
            .border(1.dp, colors.borderSubtle, AstraTheme.shapes.pill)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items.forEach { item ->
            val selected = item == selectedItem
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(AstraTheme.shapes.pill)
                    .background(
                        if (selected) colors.accentPrimary.copy(alpha = 0.22f)
                        else Color.Transparent
                    )
                    .border(
                        width = if (selected) 1.dp else 0.dp,
                        color = if (selected) colors.accentPrimary.copy(alpha = 0.6f) else Color.Transparent,
                        shape = AstraTheme.shapes.pill
                    )
                    .clickable { onSelect(item) }
                    .padding(vertical = 8.dp, horizontal = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = labelProvider(item),
                    style = AstraTheme.typography.labelS,
                    color = if (selected) colors.accentPrimary else colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * 16. AstraSlider (Section 46).
 */
@Composable
fun AstraSlider(
    label: String,
    glyph: AstraGlyph,
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    valueText: String = "${(value * 100).toInt()}%"
) {
    val colors = AstraTheme.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(AstraTheme.shapes.compactCard)
            .background(colors.surfaceGlass)
            .border(1.dp, colors.borderSubtle, AstraTheme.shapes.compactCard)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AstraVectorIcon(glyph = glyph, size = 16.dp, tint = colors.accentPrimary, active = true)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = label, style = AstraTheme.typography.labelL, color = colors.textPrimary)
            }
            Text(text = valueText, style = AstraTheme.typography.labelS, color = colors.accentPrimary)
        }
        Slider(
            value = value.coerceIn(0f, 1f),
            onValueChange = onValueChange,
            colors = SliderDefaults.colors(
                thumbColor = colors.accentPrimary,
                activeTrackColor = colors.accentPrimary,
                inactiveTrackColor = colors.surfaceFloating
            )
        )
    }
}

/**
 * 9, 10, 11, 17. AstraSheet, AstraDialog, AstraToast, AstraAppPreview (Section 46).
 */
@Composable
fun AstraToast(
    message: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    onDismiss: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = AstraTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(AstraTheme.shapes.pill)
            .background(colors.surfaceGlassStrong)
            .border(1.dp, colors.accentPrimary.copy(alpha = 0.5f), AstraTheme.shapes.pill)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AstraVectorIcon(glyph = AstraGlyph.SPARK, size = 16.dp, tint = colors.accentPrimary, active = true)
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = message,
                style = AstraTheme.typography.bodyS,
                color = colors.textPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (actionLabel != null && onAction != null) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = actionLabel,
                style = AstraTheme.typography.labelL,
                color = colors.accentPrimary,
                modifier = Modifier.clickable { onAction() }
            )
        } else {
            Spacer(modifier = Modifier.width(8.dp))
            Box(modifier = Modifier.clickable { onDismiss() }) {
                AstraVectorIcon(glyph = AstraGlyph.CLOSE, size = 14.dp, tint = colors.textTertiary)
            }
        }
    }
}

@Composable
fun AstraStatusBar(
    timeText: String = "09:41",
    batteryPercent: Int = 84,
    isCharging: Boolean = false,
    isOffline: Boolean = false,
    micActive: Boolean = false,
    cameraActive: Boolean = false,
    locationActive: Boolean = false,
    hasNotificationAccess: Boolean = false,
    onStatusBarTap: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = AstraTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onStatusBarTap() }
            .padding(horizontal = 20.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = timeText,
                style = AstraTheme.typography.labelL,
                color = colors.textPrimary
            )
            if (isOffline) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(colors.warning.copy(alpha = 0.22f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("OFFLINE", style = AstraTheme.typography.labelS, color = colors.warning)
                }
            }
        }

        // Privacy indicators & system status (Section 19 & 43)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (micActive || cameraActive || locationActive) {
                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(colors.success.copy(alpha = 0.24f))
                        .border(1.dp, colors.success.copy(alpha = 0.7f), CircleShape)
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(colors.success)
                    )
                    Text(
                        text = buildList {
                            if (cameraActive) add("CAM")
                            if (micActive) add("MIC")
                            if (locationActive) add("LOC")
                        }.joinToString("·"),
                        style = AstraTheme.typography.labelS,
                        color = colors.success
                    )
                }
            }

            if (hasNotificationAccess) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(colors.accentPrimary)
                )
            }

            if (!isOffline) {
                AstraVectorIcon(glyph = AstraGlyph.WIFI, size = 14.dp, tint = colors.iconSecondary)
                AstraVectorIcon(glyph = AstraGlyph.CELLULAR, size = 14.dp, tint = colors.iconSecondary)
            }
            Text(
                text = "${if (isCharging) "⚡ " else ""}$batteryPercent%",
                style = AstraTheme.typography.labelS,
                color = if (batteryPercent <= 20) colors.warning else colors.textSecondary
            )
        }
    }
}
