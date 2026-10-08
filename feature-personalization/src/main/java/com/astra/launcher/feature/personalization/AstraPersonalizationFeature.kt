package com.astra.launcher.feature.personalization

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.astra.launcher.core.design.AstraCategoryChip
import com.astra.launcher.core.design.AstraClock
import com.astra.launcher.core.design.AstraPalette
import com.astra.launcher.core.design.AstraShapes
import com.astra.launcher.core.design.AstraSurfaceCard
import com.astra.launcher.core.design.AstraTypography
import com.astra.launcher.core.design.toDrawableResId
import com.astra.launcher.core.storage.AstraClockStyle
import com.astra.launcher.core.storage.AstraIconStyle
import com.astra.launcher.core.storage.AstraThemePreset
import com.astra.launcher.core.storage.AstraWallpaperId
import com.astra.launcher.core.storage.HomeLayout
import com.astra.launcher.core.storage.ThemeSettings
import com.astra.launcher.core.storage.WallpaperSource

/**
 * Personalization Studio Overlay (Sections 16, 26, 42, 43).
 * Configures bundled Astra wallpapers or Android's live/static System Wallpaper,
 * theme presets, clock style, icon treatment, grid dimensions, and dock appearance.
 */
@Composable
fun AstraPersonalizationOverlay(
    themeSettings: ThemeSettings,
    homeLayout: HomeLayout,
    palette: AstraPalette,
    onApplyPreset: (AstraThemePreset) -> Unit,
    onSelectWallpaperSource: (WallpaperSource) -> Unit,
    onSelectWallpaper: (AstraWallpaperId) -> Unit,
    onApplyWallpaperToAndroidSystem: (AstraWallpaperId) -> Unit,
    onOpenSystemWallpaperPicker: () -> Unit,
    onSelectClockStyle: (AstraClockStyle) -> Unit,
    onSelectIconStyle: (AstraIconStyle) -> Unit,
    onUpdateIconScale: (Float) -> Unit,
    onToggleIconLabels: (Boolean) -> Unit,
    onToggleDockGlass: (Boolean) -> Unit,
    onUpdateGridSize: (cols: Int, rows: Int) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(palette.scrimOverlay)
            .statusBarsPadding()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Personalization Studio",
                            style = AstraTypography.TitleL,
                            color = palette.primaryText
                        )
                        Text(
                            text = "Real-time wallpaper, icon treatment, clock & grid studio",
                            style = AstraTypography.Caption,
                            color = palette.secondaryText
                        )
                    }
                    Surface(
                        modifier = Modifier.clickable(onClick = onClose),
                        shape = AstraShapes.ChipPill,
                        color = palette.primaryAccent
                    ) {
                        Text(
                            text = "Done",
                            style = AstraTypography.Caption.copy(fontWeight = FontWeight.SemiBold),
                            color = palette.obsidian0,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                        )
                    }
                }
            }

            // Live Wallpaper + Clock Preview Card
            item {
                AstraSurfaceCard(
                    palette = palette,
                    useGlass = true,
                    shape = AstraShapes.CardLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(175.dp)
                ) {
                    Image(
                        painter = painterResource(id = themeSettings.wallpaperId.toDrawableResId()),
                        contentDescription = themeSettings.wallpaperId.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(palette.obsidian0.copy(alpha = 0.36f))
                            .padding(16.dp)
                    ) {
                        AstraClock(
                            style = themeSettings.clockStyle,
                            palette = palette,
                            modifier = Modifier.align(Alignment.TopStart)
                        )
                        Text(
                            text = "Wallpaper Preview · ${themeSettings.wallpaperSource.label} (${themeSettings.wallpaperId.title})",
                            style = AstraTypography.Caption,
                            color = palette.primaryAccent,
                            modifier = Modifier.align(Alignment.BottomStart)
                        )
                    }
                }
            }

            // Wallpaper Source (Bundled Astra vs Android System Wallpaper)
            item {
                AstraSurfaceCard(
                    palette = palette,
                    useGlass = false,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "WALLPAPER SOURCE",
                            style = AstraTypography.Caption.copy(fontWeight = FontWeight.SemiBold),
                            color = palette.secondaryText
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            WallpaperSource.entries.forEach { src ->
                                AstraCategoryChip(
                                    label = src.label,
                                    selected = themeSettings.wallpaperSource == src,
                                    palette = palette,
                                    onClick = { onSelectWallpaperSource(src) }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                modifier = Modifier.clickable {
                                    onApplyWallpaperToAndroidSystem(themeSettings.wallpaperId)
                                },
                                shape = AstraShapes.ChipPill,
                                color = palette.primaryAccent.copy(alpha = 0.18f),
                                border = BorderStroke(1.dp, palette.primaryAccent)
                            ) {
                                Text(
                                    text = "Set as Android System Wallpaper",
                                    style = AstraTypography.Caption.copy(fontWeight = FontWeight.SemiBold),
                                    color = palette.primaryAccent,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                            Surface(
                                modifier = Modifier.clickable(onClick = onOpenSystemWallpaperPicker),
                                shape = AstraShapes.ChipPill,
                                color = palette.elevatedSurface,
                                border = BorderStroke(1.dp, palette.hairlineBorder)
                            ) {
                                Text(
                                    text = "System Picker ↗",
                                    style = AstraTypography.Caption,
                                    color = palette.primaryText,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Curated Astra Wallpaper Collection (Orbit, Nocturne Flow, Horizon)
            item {
                Column {
                    Text(
                        text = "ASTRA WALLPAPER FAMILIES (ORBIT · NOCTURNE FLOW · HORIZON)",
                        style = AstraTypography.Caption.copy(fontWeight = FontWeight.SemiBold),
                        color = palette.secondaryText
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(AstraWallpaperId.entries, key = { it.id }) { wp ->
                            val selected = themeSettings.wallpaperId == wp
                            Box(
                                modifier = Modifier
                                    .width(126.dp)
                                    .height(175.dp)
                                    .clip(AstraShapes.CardMedium)
                                    .border(
                                        width = if (selected) 2.dp else 1.dp,
                                        color = if (selected) palette.primaryAccent else palette.hairlineBorder,
                                        shape = AstraShapes.CardMedium
                                    )
                                    .clickable {
                                        onSelectWallpaperSource(WallpaperSource.BUNDLED_ASTRA)
                                        onSelectWallpaper(wp)
                                    }
                            ) {
                                Image(
                                    painter = painterResource(id = wp.toDrawableResId()),
                                    contentDescription = wp.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Column(
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .fillMaxWidth()
                                        .background(palette.obsidian0.copy(alpha = 0.78f))
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        text = wp.familyName,
                                        style = AstraTypography.Caption,
                                        color = palette.primaryAccent
                                    )
                                    Text(
                                        text = wp.title,
                                        style = AstraTypography.BodyM.copy(fontWeight = FontWeight.SemiBold),
                                        color = palette.primaryText
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Theme Presets
            item {
                Column {
                    Text(
                        text = "THEME PRESETS",
                        style = AstraTypography.Caption.copy(fontWeight = FontWeight.SemiBold),
                        color = palette.secondaryText
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(AstraThemePreset.entries, key = { it.id }) { preset ->
                            AstraCategoryChip(
                                label = preset.displayName,
                                selected = themeSettings.themePreset == preset,
                                palette = palette,
                                onClick = { onApplyPreset(preset) }
                            )
                        }
                    }
                }
            }

            // Icon Treatment & Scale (Section 6)
            item {
                AstraSurfaceCard(
                    palette = palette,
                    useGlass = false,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "REAL APP ICON TREATMENT",
                            style = AstraTypography.Caption.copy(fontWeight = FontWeight.SemiBold),
                            color = palette.secondaryText
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(AstraIconStyle.entries, key = { it.id }) { style ->
                                AstraCategoryChip(
                                    label = style.label,
                                    selected = themeSettings.iconStyle == style,
                                    palette = palette,
                                    onClick = { onSelectIconStyle(style) }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Icon Scale: ${(themeSettings.iconScale * 100).toInt()}%",
                            style = AstraTypography.Caption,
                            color = palette.primaryText
                        )
                        Slider(
                            value = themeSettings.iconScale,
                            onValueChange = onUpdateIconScale,
                            valueRange = 0.85f..1.20f,
                            colors = SliderDefaults.colors(
                                thumbColor = palette.primaryAccent,
                                activeTrackColor = palette.primaryAccent
                            )
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Show App Icon Labels", style = AstraTypography.BodyM, color = palette.primaryText)
                            Switch(checked = themeSettings.showIconLabels, onCheckedChange = onToggleIconLabels)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Smoked Glass Dock Surface", style = AstraTypography.BodyM, color = palette.primaryText)
                            Switch(checked = themeSettings.dockStyleGlass, onCheckedChange = onToggleDockGlass)
                        }
                    }
                }
            }

            // Clock Style & Workspace Grid Dimensions
            item {
                AstraSurfaceCard(
                    palette = palette,
                    useGlass = false,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "CLOCK & 2D WORKSPACE GRID",
                            style = AstraTypography.Caption.copy(fontWeight = FontWeight.SemiBold),
                            color = palette.secondaryText
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(AstraClockStyle.entries, key = { it.id }) { cs ->
                                AstraCategoryChip(
                                    label = cs.label,
                                    selected = themeSettings.clockStyle == cs,
                                    palette = palette,
                                    onClick = { onSelectClockStyle(cs) }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(4 to 5, 4 to 6, 5 to 5, 5 to 6).forEach { (c, r) ->
                                AstraCategoryChip(
                                    label = "${c}×${r} Grid",
                                    selected = homeLayout.gridColumns == c && homeLayout.gridRows == r,
                                    palette = palette,
                                    onClick = { onUpdateGridSize(c, r) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
