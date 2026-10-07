package com.astra.launcher.feature.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.astra.launcher.core.design.AstraBrandMark
import com.astra.launcher.core.design.AstraCard
import com.astra.launcher.core.design.AstraComponentState
import com.astra.launcher.core.design.AstraGlyph
import com.astra.launcher.core.design.AstraIconButton
import com.astra.launcher.core.design.AstraMediaCard
import com.astra.launcher.core.design.AstraSurfaceFamily
import com.astra.launcher.core.design.AstraTheme
import com.astra.launcher.core.design.AstraVectorIcon
import com.astra.launcher.core.storage.AstraDeviceStatus
import com.astra.launcher.core.storage.AstraMediaState
import com.astra.launcher.core.storage.AstraWidgetPlacement
import com.astra.launcher.core.storage.AstraWidgetType

/**
 * First-Party Astra Widgets & Android AppWidgetHost Coexistence (Section 20, Frame 23 & 39).
 */
@Composable
fun AstraWidgetHostCard(
    placement: AstraWidgetPlacement,
    deviceStatus: AstraDeviceStatus,
    mediaState: AstraMediaState,
    quickNoteText: String,
    isEditMode: Boolean = false,
    onQuickAction: (String) -> Unit = {},
    onRemoveWidget: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val colors = AstraTheme.colors

    Box(modifier = modifier) {
        when (placement.widgetType) {
            AstraWidgetType.WEATHER_CALM -> {
                AstraCard(
                    modifier = Modifier.fillMaxWidth(),
                    family = AstraSurfaceFamily.SOFT_TRANSLUCENT,
                    onClick = { onQuickAction("weather") }
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("ATMOSPHERE", style = AstraTheme.typography.labelS, color = colors.accentPrimary)
                            AstraVectorIcon(glyph = AstraGlyph.BRIGHTNESS, size = 16.dp, tint = colors.accentPrimary, active = true)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("18°C", style = AstraTheme.typography.headlineL, color = colors.textPrimary)
                        Text(
                            "Clear Sky · UV 2 Low",
                            style = AstraTheme.typography.bodyS,
                            color = colors.textSecondary,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "H: 21°  L: 12° · Sunset 18:44",
                            style = AstraTheme.typography.labelS,
                            color = colors.textTertiary
                        )
                    }
                }
            }

            AstraWidgetType.CALENDAR_AGENDA -> {
                AstraCard(
                    modifier = Modifier.fillMaxWidth(),
                    family = AstraSurfaceFamily.SOFT_TRANSLUCENT,
                    onClick = { onQuickAction("calendar") }
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("NEXT UP", style = AstraTheme.typography.labelS, color = colors.accentPrimary)
                            AstraVectorIcon(glyph = AstraGlyph.CALENDAR, size = 16.dp, tint = colors.accentPrimary, active = true)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Design Gate Review",
                            style = AstraTheme.typography.bodyM.copy(fontWeight = FontWeight.SemiBold),
                            color = colors.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            "10:00 – 10:45 · Studio 4B",
                            style = AstraTheme.typography.bodyS,
                            color = colors.textSecondary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "+2 afternoon blocks free",
                            style = AstraTheme.typography.labelS,
                            color = colors.textTertiary
                        )
                    }
                }
            }

            AstraWidgetType.BATTERY_TELEMETRY -> {
                AstraCard(
                    modifier = Modifier.fillMaxWidth(),
                    family = AstraSurfaceFamily.SOFT_TRANSLUCENT,
                    onClick = { onQuickAction("battery") }
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("POWER CELL", style = AstraTheme.typography.labelS, color = colors.accentPrimary)
                            AstraVectorIcon(glyph = AstraGlyph.BATTERY, size = 16.dp, tint = colors.accentPrimary, active = true)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "${deviceStatus.batteryPercent}%",
                            style = AstraTheme.typography.headlineL,
                            color = if (deviceStatus.isLowBattery) colors.warning else colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { (deviceStatus.batteryPercent / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(CircleShape),
                            color = if (deviceStatus.isLowBattery) colors.warning else colors.accentPrimary,
                            trackColor = colors.surfaceFloating
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            if (deviceStatus.isCharging) "Fast Orbital Charge · 24m left"
                            else "Est. 19h 40m remaining · 31°C",
                            style = AstraTheme.typography.labelS,
                            color = colors.textTertiary
                        )
                    }
                }
            }

            AstraWidgetType.CLOCK_ATMOS -> {
                AstraCard(
                    modifier = Modifier.fillMaxWidth(),
                    family = AstraSurfaceFamily.SOFT_TRANSLUCENT,
                    onClick = { onQuickAction("clock") }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AstraBrandMark(size = 36.dp, luminousMoment = true)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("UTC 09:41 · SFO", style = AstraTheme.typography.labelS, color = colors.accentPrimary)
                            Text("TYO 18:41 · LDN 10:41", style = AstraTheme.typography.bodyM, color = colors.textPrimary)
                            Text("Solar zenith in 2h 18m", style = AstraTheme.typography.labelS, color = colors.textTertiary)
                        }
                    }
                }
            }

            AstraWidgetType.MEDIA_COMPACT -> {
                AstraMediaCard(
                    media = mediaState,
                    onPlayPause = { onQuickAction("media_play_pause") },
                    onSkipNext = { onQuickAction("media_next") }
                )
            }

            AstraWidgetType.QUICK_ACTIONS -> {
                AstraCard(
                    modifier = Modifier.fillMaxWidth(),
                    family = AstraSurfaceFamily.SOFT_TRANSLUCENT
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AstraIconButton(
                            glyph = AstraGlyph.SEARCH,
                            contentDescriptionLabel = "Global Search",
                            onClick = { onQuickAction("search") }
                        )
                        AstraIconButton(
                            glyph = AstraGlyph.FLASHLIGHT,
                            contentDescriptionLabel = "Toggle Torch",
                            onClick = { onQuickAction("torch") },
                            state = if (deviceStatus.flashlightEnabled) AstraComponentState.ACTIVE else AstraComponentState.DEFAULT
                        )
                        AstraIconButton(
                            glyph = AstraGlyph.CAMERA,
                            contentDescriptionLabel = "Open Camera",
                            onClick = { onQuickAction("camera") }
                        )
                        AstraIconButton(
                            glyph = AstraGlyph.CONTROLS,
                            contentDescriptionLabel = "Control Center",
                            onClick = { onQuickAction("controls") }
                        )
                    }
                }
            }

            AstraWidgetType.FOCUS_SCREEN_TIME -> {
                AstraCard(
                    modifier = Modifier.fillMaxWidth(),
                    family = AstraSurfaceFamily.SOFT_TRANSLUCENT,
                    onClick = { onQuickAction("focus") }
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("CALM FOCUS", style = AstraTheme.typography.labelS, color = colors.accentPrimary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("2h 14m", style = AstraTheme.typography.headlineM, color = colors.textPrimary)
                        Text("18% below daily average", style = AstraTheme.typography.labelS, color = colors.success)
                    }
                }
            }

            AstraWidgetType.QUICK_NOTES -> {
                AstraCard(
                    modifier = Modifier.fillMaxWidth(),
                    family = AstraSurfaceFamily.SOFT_TRANSLUCENT,
                    onClick = { onQuickAction("note") }
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("SCRATCHPAD", style = AstraTheme.typography.labelS, color = colors.accentPrimary)
                            AstraVectorIcon(glyph = AstraGlyph.EDIT, size = 14.dp, tint = colors.textTertiary)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = quickNoteText,
                            style = AstraTheme.typography.bodyS,
                            color = colors.textPrimary,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            AstraWidgetType.ANDROID_HOSTED -> {
                // Frame 39: Third-party widget coexistence
                AstraCard(
                    modifier = Modifier.fillMaxWidth(),
                    family = AstraSurfaceFamily.SOLID
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ANDROID APPWIDGETHOST · ${placement.providerLabel.uppercase()}",
                                style = AstraTheme.typography.labelS,
                                color = colors.accentSecondary
                            )
                            Text(
                                text = "Host ID #2026",
                                style = AstraTheme.typography.labelS,
                                color = colors.textTertiary
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Third-Party Widget Container (${placement.providerPackage})",
                            style = AstraTheme.typography.bodyM.copy(fontWeight = FontWeight.Medium),
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Hosted natively via AppWidgetHostView without overriding third-party widget contract.",
                            style = AstraTheme.typography.bodyS,
                            color = colors.textSecondary
                        )
                    }
                }
            }
        }

        if (isEditMode && onRemoveWidget != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(colors.error)
                    .clickable { onRemoveWidget() },
                contentAlignment = Alignment.Center
            ) {
                AstraVectorIcon(glyph = AstraGlyph.CLOSE, size = 12.dp, tint = colors.textPrimary)
            }
        }
    }
}

/**
 * Frame 23: Widget Picker Sheet (Section 20 & 45).
 */
@Composable
fun AstraWidgetPickerSheet(
    activeWidgets: List<AstraWidgetPlacement>,
    installedThirdPartyCount: Int,
    onAddWidget: (AstraWidgetType) -> Unit,
    onRemoveWidget: (Int) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AstraTheme.colors

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(AstraTheme.shapes.heroPanel)
            .background(colors.surfaceGlassStrong)
            .border(1.dp, colors.borderSubtle, AstraTheme.shapes.heroPanel)
            .padding(22.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("ASTRA WIDGET SYSTEM", style = AstraTheme.typography.labelS, color = colors.accentPrimary)
                Text("First-Party & Hosted Widgets", style = AstraTheme.typography.headlineM, color = colors.textPrimary)
            }
            AstraIconButton(
                glyph = AstraGlyph.CLOSE,
                contentDescriptionLabel = "Close Widget Picker",
                onClick = onClose
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Widgets share Astra design tokens while respecting standard Android AppWidgetManager contracts ($installedThirdPartyCount system providers detected).",
            style = AstraTheme.typography.bodyS,
            color = colors.textSecondary
        )

        Spacer(modifier = Modifier.height(16.dp))

        AstraWidgetType.entries.forEach { widgetType ->
            val alreadyAdded = activeWidgets.any { it.widgetType == widgetType }
            AstraCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp),
                family = AstraSurfaceFamily.SOFT_TRANSLUCENT,
                state = if (alreadyAdded) AstraComponentState.SELECTED else AstraComponentState.DEFAULT,
                onClick = { onAddWidget(widgetType) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = widgetType.title,
                                style = AstraTheme.typography.bodyM.copy(fontWeight = FontWeight.SemiBold),
                                color = colors.textPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${widgetType.spanColumns}×${widgetType.spanRows}",
                                style = AstraTheme.typography.labelS,
                                color = colors.accentPrimary
                            )
                        }
                        Text(
                            text = widgetType.subtitle,
                            style = AstraTheme.typography.bodyS,
                            color = colors.textSecondary
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(
                                if (alreadyAdded) colors.accentPrimary.copy(alpha = 0.22f)
                                else colors.surfaceFloating
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (alreadyAdded) "Added · +1" else "Add to Home",
                            style = AstraTheme.typography.labelS,
                            color = if (alreadyAdded) colors.accentPrimary else colors.textPrimary
                        )
                    }
                }
            }
        }
    }
}
