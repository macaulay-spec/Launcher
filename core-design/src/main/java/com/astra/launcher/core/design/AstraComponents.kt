package com.astra.launcher.core.design

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.text.format.DateFormat
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.astra.launcher.core.platform.AstraIconPipeline
import com.astra.launcher.core.storage.AstraAppEntry
import com.astra.launcher.core.storage.AstraClockStyle
import com.astra.launcher.core.storage.AstraIconStyle
import com.astra.launcher.core.storage.AstraWallpaperId
import com.astra.launcher.core.storage.WallpaperSource
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

/**
 * Resolves the drawable resource ID for a bundled Astra wallpaper (Section 16 & 42).
 */
fun AstraWallpaperId.toDrawableResId(): Int = when (this) {
    AstraWallpaperId.ORBIT_DAWN -> R.drawable.wallpaper_orbit_dawn
    AstraWallpaperId.NOCTURNE_FLOW -> R.drawable.wallpaper_nocturne_flow
    AstraWallpaperId.GLASS_HORIZON -> R.drawable.wallpaper_glass_horizon
    AstraWallpaperId.MORNING_MIST -> R.drawable.wallpaper_morning_mist
    AstraWallpaperId.GRAPHITE_MONOLITH -> R.drawable.wallpaper_graphite_monolith
}

/**
 * 1. `AstraWallpaperSurface` (Section 16, 42, 43)
 * Supports BOTH:
 * - `WallpaperSource.BUNDLED_ASTRA`: renders the curated high-res Astra wallpaper with safe-zone contrast scrims.
 * - `WallpaperSource.SYSTEM_WALLPAPER`: leaves the canvas transparent so Android's live/static system wallpaper
 *   (`android:windowShowWallpaper="true"`) shows through, applying only restrained top/bottom legibility protection.
 */
@Composable
fun AstraWallpaperSurface(
    wallpaperId: AstraWallpaperId,
    palette: AstraPalette,
    modifier: Modifier = Modifier,
    wallpaperSource: WallpaperSource = WallpaperSource.BUNDLED_ASTRA,
    dimAmount: Float = 0f,
    content: @Composable BoxScope.() -> Unit
) {
    val drawableRes = remember(wallpaperId) { wallpaperId.toDrawableResId() }

    Box(modifier = modifier.fillMaxSize()) {
        if (wallpaperSource == WallpaperSource.BUNDLED_ASTRA) {
            Image(
                painter = painterResource(id = drawableRes),
                contentDescription = wallpaperId.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Restrained safe-zone legibility gradient & overlay dim
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0x5506080E),
                        Color.Transparent,
                        Color(0x8806080E)
                    ),
                    startY = 0f,
                    endY = h
                )
            )

            if (dimAmount > 0.01f) {
                drawRect(color = Color.Black.copy(alpha = dimAmount.coerceIn(0f, 0.88f)))
            }
        }
        content()
    }
}

/**
 * 2. `AstraSurfaceCard` (Elevated / Glass / Solid Material Card)
 */
@Composable
fun AstraSurfaceCard(
    palette: AstraPalette,
    modifier: Modifier = Modifier,
    useGlass: Boolean = true,
    shape: RoundedCornerShape = AstraShapes.CardMedium,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val bg = if (useGlass) palette.glassSurface else palette.elevatedSurface
    val border = if (useGlass) palette.glassStroke else palette.hairlineBorder
    val baseModifier = modifier
        .clip(shape)
        .background(bg)
        .border(BorderStroke(1.dp, border), shape)
        .let { mod ->
            if (onClick != null) {
                mod.clickable(role = Role.Button, onClick = onClick)
            } else mod
        }

    Box(modifier = baseModifier, content = content)
}

/**
 * 3. Live System Clock State (Section 19 & 32 — NO hardcoded "09:41" or "WEDNESDAY, OCT 7")
 */
data class LiveSystemClockSnapshot(
    val formattedTime: String,
    val hourPart: String,
    val minutePart: String,
    val formattedDate: String,
    val nextAlarmText: String?
)

@Composable
fun rememberLiveSystemClockSnapshot(): State<LiveSystemClockSnapshot> {
    val context = LocalContext.current
    val state = remember { mutableStateOf(computeCurrentClockSnapshot(context)) }

    // Listen to Android system time, timezone, and alarm broadcasts
    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                state.value = computeCurrentClockSnapshot(context)
            }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_TIME_TICK)
            addAction(Intent.ACTION_TIME_CHANGED)
            addAction(Intent.ACTION_TIMEZONE_CHANGED)
            addAction(AlarmManager.ACTION_NEXT_ALARM_CLOCK_CHANGED)
        }
        try {
            context.registerReceiver(receiver, filter)
        } catch (_: Throwable) {
        }
        onDispose {
            try {
                context.unregisterReceiver(receiver)
            } catch (_: Throwable) {
            }
        }
    }

    // Periodic refresh every 15 seconds to guarantee live accuracy
    LaunchedEffect(context) {
        while (true) {
            state.value = computeCurrentClockSnapshot(context)
            delay(15_000L)
        }
    }

    return state
}

private fun computeCurrentClockSnapshot(context: Context): LiveSystemClockSnapshot {
    val now = LocalDateTime.now()
    val is24Hour = try {
        DateFormat.is24HourFormat(context)
    } catch (_: Throwable) {
        true
    }
    val timePattern = if (is24Hour) "HH:mm" else "h:mm"
    val hourPattern = if (is24Hour) "HH" else "h"
    val formattedTime = now.format(DateTimeFormatter.ofPattern(timePattern, Locale.getDefault()))
    val hourPart = now.format(DateTimeFormatter.ofPattern(hourPattern, Locale.getDefault()))
    val minutePart = now.format(DateTimeFormatter.ofPattern("mm", Locale.getDefault()))
    val formattedDate = now.format(DateTimeFormatter.ofPattern("EEEE, MMM d", Locale.getDefault()))
        .uppercase(Locale.getDefault())

    val nextAlarm = try {
        val am = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        am?.nextAlarmClock?.triggerTime?.let { triggerMs ->
            val fmt = if (is24Hour) "EEE HH:mm" else "EEE h:mm a"
            java.text.SimpleDateFormat(fmt, Locale.getDefault()).format(Date(triggerMs))
        }
    } catch (_: Throwable) {
        null
    }

    return LiveSystemClockSnapshot(
        formattedTime = formattedTime,
        hourPart = hourPart,
        minutePart = minutePart,
        formattedDate = formattedDate,
        nextAlarmText = nextAlarm
    )
}

/**
 * 4. `AstraClock` (Section 19 — Real Live System Clock & Date Header)
 * Renders actual system time, date, and optional next scheduled system alarm.
 * Never displays hardcoded time, hardcoded date, or fake weather.
 */
@Composable
fun AstraClock(
    style: AstraClockStyle,
    palette: AstraPalette,
    modifier: Modifier = Modifier,
    onClockClick: (() -> Unit)? = null
) {
    val snapshot by rememberLiveSystemClockSnapshot()

    Column(
        modifier = modifier
            .let { if (onClockClick != null) it.clickable { onClockClick() } else it },
        horizontalAlignment = Alignment.Start
    ) {
        when (style) {
            AstraClockStyle.MINIMAL_NUMERAL -> {
                Text(
                    text = snapshot.formattedTime,
                    style = AstraTypography.ClockHero.copy(fontSize = 54.sp, lineHeight = 58.sp),
                    color = palette.primaryText
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = snapshot.formattedDate,
                        style = AstraTypography.Caption.copy(letterSpacing = 1.1.sp),
                        color = palette.secondaryText
                    )
                    if (!snapshot.nextAlarmText.isNullOrBlank()) {
                        Text(
                            text = "· Alarm ${snapshot.nextAlarmText}",
                            style = AstraTypography.Caption,
                            color = palette.primaryAccent
                        )
                    }
                }
            }

            AstraClockStyle.EDITORIAL_STACKED -> {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = snapshot.hourPart,
                        style = AstraTypography.ClockHero.copy(fontSize = 58.sp, lineHeight = 60.sp),
                        color = palette.primaryText
                    )
                    Text(
                        text = snapshot.minutePart,
                        style = AstraTypography.ClockHero.copy(fontSize = 58.sp, lineHeight = 60.sp),
                        color = palette.primaryAccent
                    )
                }
                Text(
                    text = snapshot.formattedDate,
                    style = AstraTypography.Caption.copy(letterSpacing = 1.2.sp),
                    color = palette.secondaryText
                )
            }

            AstraClockStyle.ORBITAL_COMPACT -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = snapshot.formattedTime,
                        style = AstraTypography.DisplayM.copy(fontWeight = FontWeight.Light),
                        color = palette.primaryText
                    )
                    Text(
                        text = snapshot.formattedDate,
                        style = AstraTypography.BodyM,
                        color = palette.secondaryText
                    )
                }
            }
        }
    }
}

/**
 * 5. `AstraAppIcon` (Section 6 — REAL APPLICATION ICONS MANDATORY)
 * Loads and renders the REAL application icon Drawable/Bitmap from Android's `LauncherApps` / `PackageManager`
 * via `AstraIconPipeline`, cached in memory and invalidated on package install/update/remove.
 * Never replaces real app icons with generic vector glyphs.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AstraAppIcon(
    app: AstraAppEntry,
    iconStyle: AstraIconStyle,
    palette: AstraPalette,
    iconPipeline: AstraIconPipeline? = null,
    showLabel: Boolean = true,
    iconScale: Float = 1.0f,
    showNotificationBadge: Boolean = true,
    hapticsEnabled: Boolean = true,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val view = LocalView.current
    val pipeline = remember(context, iconPipeline) {
        iconPipeline ?: AstraIconPipeline(context)
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1.0f,
        animationSpec = AstraMotion.TapSpring,
        label = "app_icon_press_scale"
    )

    val accentArgb = palette.primaryAccent.toArgb()
    var realIconBitmap by remember(app.componentName, app.versionCode, iconStyle) {
        mutableStateOf<Bitmap?>(pipeline.getCachedIcon(app, iconStyle))
    }

    LaunchedEffect(app.componentName, app.versionCode, iconStyle, accentArgb) {
        if (realIconBitmap == null) {
            realIconBitmap = pipeline.loadAppIconBitmap(app, iconStyle, accentArgb)
        }
    }

    val containerShape = when (iconStyle) {
        AstraIconStyle.CIRCLE -> CircleShape
        AstraIconStyle.ASTRA_SQUIRCLE, AstraIconStyle.MONOCHROME_TINT -> AstraShapes.IconSquircle
        AstraIconStyle.ORIGINAL -> RoundedCornerShape(12.dp)
    }

    val boxSize: Dp = (54.dp * iconScale.coerceIn(0.85f, 1.20f))

    Column(
        modifier = modifier
            .scale(scale)
            .semantics { contentDescription = "Launch ${app.label}" }
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    AstraHaptics.perform(view, AstraHapticToken.LIGHT_TICK, hapticsEnabled)
                    onClick()
                },
                onLongClick = {
                    if (onLongClick != null) {
                        AstraHaptics.perform(view, AstraHapticToken.LONG_PRESS_ANCHOR, hapticsEnabled)
                        onLongClick()
                    }
                }
            )
            .padding(vertical = 4.dp, horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            Box(
                modifier = Modifier
                    .size(boxSize)
                    .clip(containerShape)
                    .background(
                        if (iconStyle == AstraIconStyle.ORIGINAL) Color.Transparent
                        else palette.glassSurface.copy(alpha = 0.72f)
                    )
                    .let { mod ->
                        if (iconStyle != AstraIconStyle.ORIGINAL) {
                            mod.border(1.dp, palette.glassStroke, containerShape)
                        } else mod
                    },
                contentAlignment = Alignment.Center
            ) {
                val bmp = realIconBitmap
                if (bmp != null) {
                    Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = app.label,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize(if (iconStyle == AstraIconStyle.ORIGINAL) 1.0f else 0.88f)
                            .clip(containerShape)
                    )
                } else {
                    // Only while async bitmap is loading on IO thread or in headless unit test without PackageManager icon:
                    // render the app's clean monogram initial inside its container.
                    Text(
                        text = app.label.take(1).uppercase(Locale.getDefault()),
                        style = AstraTypography.SectionHeader,
                        color = palette.primaryAccent
                    )
                }
            }

            // Real notification count badge from NotificationListenerService (Section 22)
            if (showNotificationBadge && app.activeNotificationCount > 0) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(palette.primaryAccent)
                        .border(1.5.dp, palette.obsidian0, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = app.activeNotificationCount.coerceAtMost(9).toString(),
                        style = AstraTypography.Caption.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                        color = palette.obsidian0
                    )
                }
            }
        }

        if (showLabel) {
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = app.label,
                style = AstraTypography.IconLabel,
                color = palette.primaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * 6. `AstraDock` (Section 15 — Persistent Configurable Home Dock)
 * Displays the user's real configured Dock apps + optional App Library button.
 */
@Composable
fun AstraDock(
    dockApps: List<AstraAppEntry>,
    iconStyle: AstraIconStyle,
    palette: AstraPalette,
    iconPipeline: AstraIconPipeline? = null,
    useGlass: Boolean = true,
    showLabels: Boolean = false,
    hapticsEnabled: Boolean = true,
    onAppClick: (AstraAppEntry) -> Unit,
    onAppLongClick: (AstraAppEntry) -> Unit,
    onOpenDrawerClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = AstraShapes.DockPill,
        color = if (useGlass) palette.glassSurface else palette.elevatedSurface,
        border = BorderStroke(1.dp, if (useGlass) palette.glassStroke else palette.hairlineBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            dockApps.forEach { app ->
                AstraAppIcon(
                    app = app,
                    iconStyle = iconStyle,
                    palette = palette,
                    iconPipeline = iconPipeline,
                    showLabel = showLabels,
                    iconScale = 0.96f,
                    hapticsEnabled = hapticsEnabled,
                    onClick = { onAppClick(app) },
                    onLongClick = { onAppLongClick(app) }
                )
            }

            // App Drawer trigger button
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(AstraShapes.IconSquircle)
                    .background(palette.primaryAccent.copy(alpha = 0.14f))
                    .border(1.dp, palette.primaryAccent.copy(alpha = 0.35f), AstraShapes.IconSquircle)
                    .clickable(role = Role.Button, onClick = onOpenDrawerClick)
                    .semantics { contentDescription = "Open App Drawer" },
                contentAlignment = Alignment.Center
            ) {
                AstraVectorIcon(
                    glyph = AstraGlyph.APPS_GRID,
                    tint = palette.primaryAccent,
                    size = 22.dp
                )
            }
        }
    }
}

/**
 * 7. `AstraSearchBar` (Home Search Pill)
 */
@Composable
fun AstraSearchBar(
    palette: AstraPalette,
    placeholder: String = "Search apps, shortcuts, settings…",
    onClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = "Open Universal Search" },
        shape = AstraShapes.SearchPill,
        color = palette.glassSurface,
        border = BorderStroke(1.dp, palette.glassStroke)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AstraVectorIcon(
                glyph = AstraGlyph.SEARCH,
                tint = palette.primaryAccent,
                size = 20.dp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = placeholder,
                style = AstraTypography.BodyM,
                color = palette.secondaryText,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .clickable(role = Role.Button, onClick = onSettingsClick)
                    .semantics { contentDescription = "Open Launcher Settings" },
                contentAlignment = Alignment.Center
            ) {
                AstraVectorIcon(
                    glyph = AstraGlyph.SETTINGS,
                    tint = palette.secondaryText,
                    size = 18.dp
                )
            }
        }
    }
}

/**
 * 8. `AstraCategoryChip`
 */
@Composable
fun AstraCategoryChip(
    label: String,
    selected: Boolean,
    palette: AstraPalette,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg by animateColorAsState(
        targetValue = if (selected) palette.primaryAccent.copy(alpha = 0.20f) else palette.elevatedSurface,
        label = "chip_bg"
    )
    val border by animateColorAsState(
        targetValue = if (selected) palette.primaryAccent else palette.hairlineBorder,
        label = "chip_border"
    )
    val textColor by animateColorAsState(
        targetValue = if (selected) palette.primaryAccent else palette.secondaryText,
        label = "chip_text"
    )

    Surface(
        modifier = modifier
            .height(36.dp)
            .clickable(role = Role.Tab, onClick = onClick),
        shape = AstraShapes.ChipPill,
        color = bg,
        border = BorderStroke(1.dp, border)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = AstraTypography.Caption.copy(fontWeight = FontWeight.SemiBold),
                color = textColor
            )
        }
    }
}

/**
 * 9. `AstraPermissionPromptCard` (Honest Platform Consent & Role Card — Section 3 & 27)
 */
@Composable
fun AstraPermissionPromptCard(
    title: String,
    whyNeeded: String,
    whatHappensIfDenied: String,
    primaryButtonLabel: String,
    palette: AstraPalette,
    onGrantClick: () -> Unit,
    onContinueWithoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AstraSurfaceCard(
        palette = palette,
        useGlass = true,
        shape = AstraShapes.CardLarge,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AstraBrandMark(
                    accentColor = palette.primaryAccent,
                    secondaryColor = palette.secondaryAccent,
                    size = 24.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    style = AstraTypography.SectionHeader,
                    color = palette.primaryText
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = whyNeeded,
                style = AstraTypography.BodyM,
                color = palette.secondaryText
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = whatHappensIfDenied,
                style = AstraTypography.Caption,
                color = palette.mutedText
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clickable(onClick = onContinueWithoutClick),
                    shape = AstraShapes.ChipPill,
                    color = palette.elevatedSurface,
                    border = BorderStroke(1.dp, palette.hairlineBorder)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("Later", style = AstraTypography.BodyM, color = palette.secondaryText)
                    }
                }
                Surface(
                    modifier = Modifier
                        .weight(1.3f)
                        .height(40.dp)
                        .clickable(onClick = onGrantClick),
                    shape = AstraShapes.ChipPill,
                    color = palette.primaryAccent
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = primaryButtonLabel,
                            style = AstraTypography.BodyM.copy(fontWeight = FontWeight.SemiBold),
                            color = palette.obsidian0
                        )
                    }
                }
            }
        }
    }
}
