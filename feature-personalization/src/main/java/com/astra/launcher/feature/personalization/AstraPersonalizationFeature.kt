package com.astra.launcher.feature.personalization

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.astra.launcher.core.design.AstraCard
import com.astra.launcher.core.design.AstraClock
import com.astra.launcher.core.design.AstraComponentState
import com.astra.launcher.core.design.AstraGlyph
import com.astra.launcher.core.design.AstraIconButton
import com.astra.launcher.core.design.AstraSegmentedControl
import com.astra.launcher.core.design.AstraSlider
import com.astra.launcher.core.design.AstraSurfaceFamily
import com.astra.launcher.core.design.AstraTheme
import com.astra.launcher.core.design.AstraToggle
import com.astra.launcher.core.design.AstraWallpaperSurface
import com.astra.launcher.core.storage.AstraAccentSource
import com.astra.launcher.core.storage.AstraClockStyle
import com.astra.launcher.core.storage.AstraIconStyle
import com.astra.launcher.core.storage.AstraThemeMode
import com.astra.launcher.core.storage.AstraThemePreset
import com.astra.launcher.core.storage.AstraWallpaperId
import com.astra.launcher.core.storage.ThemeSettings

enum class StudioSubTab(val label: String) {
    PRESETS("Atmosphere"),
    WALLPAPERS("Wallpapers"),
    CLOCKS("Clocks"),
    ICONS("Icons & Dock")
}

/**
 * Personalization Studio (Section 23; Frames 20, 21, 22, 24).
 * Includes live environment preview so the user sees the full Astra atmosphere change in real time.
 */
@Composable
fun AstraPersonalizationStudioScreen(
    themeSettings: ThemeSettings,
    initialTab: StudioSubTab = StudioSubTab.PRESETS,
    onApplyPreset: (AstraThemePreset) -> Unit,
    onUpdateTheme: ((ThemeSettings) -> ThemeSettings) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AstraTheme.colors
    var activeTab by remember(initialTab) { mutableStateOf(initialTab) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "PERSONALIZATION STUDIO",
                    style = AstraTheme.typography.labelS,
                    color = colors.accentPrimary
                )
                Text(
                    text = "Atmosphere & Material Tokens",
                    style = AstraTheme.typography.headlineM,
                    color = colors.textPrimary
                )
            }
            AstraIconButton(
                glyph = AstraGlyph.CLOSE,
                contentDescriptionLabel = "Close Studio",
                onClick = onClose
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Live Interactive Phone Environment Preview (Section 23)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
                .clip(AstraTheme.shapes.heroPanel)
                .border(1.5.dp, colors.accentPrimary.copy(alpha = 0.55f), AstraTheme.shapes.heroPanel)
        ) {
            AstraWallpaperSurface(
                wallpaperId = themeSettings.wallpaperId,
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "LIVE ENVIRONMENT PREVIEW · ${themeSettings.themePreset.displayName.uppercase()}",
                            style = AstraTheme.typography.labelS,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "${themeSettings.wallpaperId.surfaceTemperatureKelvin}K",
                            style = AstraTheme.typography.labelS,
                            color = colors.accentPrimary
                        )
                    }

                    AstraClock(
                        style = themeSettings.clockStyle,
                        upperLuminance = themeSettings.wallpaperId.upperRegionLuminance,
                        isLockScreen = false
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(CircleShape)
                            .background(colors.surfaceGlassStrong)
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Wallpaper: ${themeSettings.wallpaperId.title}",
                            style = AstraTheme.typography.labelS,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Icon Style: ${themeSettings.iconStyle.label}",
                            style = AstraTheme.typography.labelS,
                            color = colors.accentPrimary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        AstraSegmentedControl(
            items = StudioSubTab.entries,
            selectedItem = activeTab,
            labelProvider = { it.label },
            onSelect = { activeTab = it }
        )

        Spacer(modifier = Modifier.height(14.dp))

        when (activeTab) {
            StudioSubTab.PRESETS -> {
                // Frame 20: Theme Presets & Mode
                Text(
                    text = "CURATED ASTRA PRESETS (5 SIGNATURES)",
                    style = AstraTheme.typography.labelS,
                    color = colors.textTertiary
                )
                Spacer(modifier = Modifier.height(8.dp))

                AstraThemePreset.entries.forEach { preset ->
                    val isSelected = themeSettings.themePreset == preset
                    AstraCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        family = AstraSurfaceFamily.SOFT_TRANSLUCENT,
                        state = if (isSelected) AstraComponentState.SELECTED else AstraComponentState.DEFAULT,
                        onClick = { onApplyPreset(preset) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(Color(preset.primaryAccentHex))
                                        .border(2.dp, Color(preset.secondaryAccentHex), CircleShape)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = preset.displayName,
                                        style = AstraTheme.typography.bodyM.copy(fontWeight = FontWeight.SemiBold),
                                        color = colors.textPrimary
                                    )
                                    Text(
                                        text = preset.subtitle,
                                        style = AstraTheme.typography.bodyS,
                                        color = colors.textSecondary
                                    )
                                }
                            }
                            if (isSelected) {
                                Text(
                                    text = "Active",
                                    style = AstraTheme.typography.labelS,
                                    color = colors.accentPrimary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "SURFACE LUMINANCE MODE",
                    style = AstraTheme.typography.labelS,
                    color = colors.textTertiary
                )
                Spacer(modifier = Modifier.height(8.dp))
                AstraSegmentedControl(
                    items = AstraThemeMode.entries,
                    selectedItem = themeSettings.themeMode,
                    labelProvider = { it.label },
                    onSelect = { mode -> onUpdateTheme { it.copy(themeMode = mode) } }
                )

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "ACCENT DERIVATION SOURCE",
                    style = AstraTheme.typography.labelS,
                    color = colors.textTertiary
                )
                Spacer(modifier = Modifier.height(8.dp))
                AstraSegmentedControl(
                    items = AstraAccentSource.entries,
                    selectedItem = themeSettings.accentSource,
                    labelProvider = { it.label },
                    onSelect = { src -> onUpdateTheme { it.copy(accentSource = src) } }
                )
            }

            StudioSubTab.WALLPAPERS -> {
                // Frame 21: Wallpaper Picker
                Text(
                    text = "CURATED ASTRA WALLPAPER COLLECTION",
                    style = AstraTheme.typography.labelS,
                    color = colors.textTertiary
                )
                Spacer(modifier = Modifier.height(8.dp))

                AstraWallpaperId.entries.forEach { wp ->
                    val selected = themeSettings.wallpaperId == wp
                    AstraCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp),
                        family = AstraSurfaceFamily.SOFT_TRANSLUCENT,
                        state = if (selected) AstraComponentState.SELECTED else AstraComponentState.DEFAULT,
                        onClick = { onUpdateTheme { it.copy(wallpaperId = wp) } }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(56.dp)
                                    .height(92.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(1.dp, colors.borderSubtle, RoundedCornerShape(12.dp))
                            ) {
                                AstraWallpaperSurface(
                                    wallpaperId = wp,
                                    enableAtmosphericOverlay = false
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = wp.title,
                                        style = AstraTheme.typography.bodyL.copy(fontWeight = FontWeight.SemiBold),
                                        color = colors.textPrimary
                                    )
                                    Text(
                                        text = "Upper Lum ${(wp.upperRegionLuminance * 100).toInt()}%",
                                        style = AstraTheme.typography.labelS,
                                        color = colors.accentPrimary
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = wp.familyDescription,
                                    style = AstraTheme.typography.bodyS,
                                    color = colors.textSecondary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Safe Zones Verified · Lock Clock & Home Grid AAA",
                                    style = AstraTheme.typography.labelS,
                                    color = colors.success
                                )
                            }
                        }
                    }
                }
            }

            StudioSubTab.CLOCKS -> {
                // Frame 22: Clock Picker
                Text(
                    text = "ADAPTIVE CLOCK TYPOGRAPHY (3 FAMILIES)",
                    style = AstraTheme.typography.labelS,
                    color = colors.textTertiary
                )
                Spacer(modifier = Modifier.height(8.dp))

                AstraClockStyle.entries.forEach { clock ->
                    val selected = themeSettings.clockStyle == clock
                    AstraCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp),
                        family = AstraSurfaceFamily.SOFT_TRANSLUCENT,
                        state = if (selected) AstraComponentState.SELECTED else AstraComponentState.DEFAULT,
                        onClick = { onUpdateTheme { it.copy(clockStyle = clock) } }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = clock.label,
                                    style = AstraTheme.typography.bodyL.copy(fontWeight = FontWeight.SemiBold),
                                    color = colors.textPrimary
                                )
                                if (selected) {
                                    Text("Selected", style = AstraTheme.typography.labelS, color = colors.accentPrimary)
                                }
                            }
                            Text(
                                text = clock.description,
                                style = AstraTheme.typography.bodyS,
                                color = colors.textSecondary
                            )
                        }
                    }
                }
            }

            StudioSubTab.ICONS -> {
                // Frame 24: Icon Style Picker & Dock Controls
                Text(
                    text = "ICONOGRAPHY HARMONIZATION",
                    style = AstraTheme.typography.labelS,
                    color = colors.textTertiary
                )
                Spacer(modifier = Modifier.height(8.dp))

                AstraIconStyle.entries.forEach { iconStyle ->
                    val selected = themeSettings.iconStyle == iconStyle
                    AstraCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        family = AstraSurfaceFamily.SOFT_TRANSLUCENT,
                        state = if (selected) AstraComponentState.SELECTED else AstraComponentState.DEFAULT,
                        onClick = { onUpdateTheme { it.copy(iconStyle = iconStyle) } }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = iconStyle.label,
                                    style = AstraTheme.typography.bodyM.copy(fontWeight = FontWeight.SemiBold),
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = iconStyle.description,
                                    style = AstraTheme.typography.bodyS,
                                    color = colors.textSecondary
                                )
                            }
                            if (selected) {
                                Text("Active", style = AstraTheme.typography.labelS, color = colors.accentPrimary)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                AstraSlider(
                    label = "Optical Icon Scale",
                    glyph = AstraGlyph.APPS,
                    value = ((themeSettings.iconScale - 0.85f) / 0.35f).coerceIn(0f, 1f),
                    valueText = "${(themeSettings.iconScale * 100).toInt()}%",
                    onValueChange = { frac ->
                        val scale = 0.85f + frac * 0.35f
                        onUpdateTheme { it.copy(iconScale = scale) }
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                AstraCard(
                    modifier = Modifier.fillMaxWidth(),
                    family = AstraSurfaceFamily.SOFT_TRANSLUCENT
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Show Application Labels", style = AstraTheme.typography.bodyM, color = colors.textPrimary)
                            AstraToggle(
                                checked = themeSettings.showIconLabels,
                                onCheckedChange = { v -> onUpdateTheme { it.copy(showIconLabels = v) } }
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Translucent Spatial Dock", style = AstraTheme.typography.bodyM, color = colors.textPrimary)
                            AstraToggle(
                                checked = themeSettings.dockStyleGlass,
                                onCheckedChange = { v -> onUpdateTheme { it.copy(dockStyleGlass = v) } }
                            )
                        }
                    }
                }
            }
        }
    }
}
