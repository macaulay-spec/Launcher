package com.astra.launcher.feature.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.astra.launcher.core.design.AstraCategoryChip
import com.astra.launcher.core.design.AstraPalette
import com.astra.launcher.core.design.AstraShapes
import com.astra.launcher.core.design.AstraSurfaceCard
import com.astra.launcher.core.design.AstraTypography
import com.astra.launcher.core.storage.AstraAppEntry
import com.astra.launcher.core.storage.AstraCapabilityReport
import com.astra.launcher.core.storage.AstraClockStyle
import com.astra.launcher.core.storage.AstraIconStyle
import com.astra.launcher.core.storage.AstraThemeMode
import com.astra.launcher.core.storage.AstraThemePreset
import com.astra.launcher.core.storage.AstraWallpaperId
import com.astra.launcher.core.storage.GesturePreferences
import com.astra.launcher.core.storage.HomeLayout
import com.astra.launcher.core.storage.NotificationPreferences
import com.astra.launcher.core.storage.PerformancePreferences
import com.astra.launcher.core.storage.SearchPreferences
import com.astra.launcher.core.storage.SwipeDownAction
import com.astra.launcher.core.storage.ThemeSettings
import com.astra.launcher.core.storage.WallpaperSource

/**
 * All 14 Official Astra Launcher Settings Sections (Section 25).
 * Every control modifies persisted launcher state immediately.
 */
enum class AstraSettingsSection(val id: String, val title: String) {
    APPEARANCE("appearance", "1. Appearance"),
    HOME_SCREEN("home_screen", "2. Home Screen"),
    APP_DRAWER("app_drawer", "3. App Drawer"),
    ICONS("icons", "4. Icons"),
    WIDGETS("widgets", "5. Widgets"),
    DOCK("dock", "6. Dock"),
    SEARCH("search", "7. Search"),
    GESTURES("gestures", "8. Gestures"),
    WALLPAPER("wallpaper", "9. Wallpaper"),
    NOTIFICATIONS("notifications", "10. Notifications"),
    PRIVACY("privacy", "11. Privacy"),
    PERFORMANCE("performance", "12. Performance"),
    ACCESSIBILITY("accessibility", "13. Accessibility"),
    ABOUT_ASTRA("about", "14. About Astra")
}

@Composable
fun AstraSettingsOverlay(
    themeSettings: ThemeSettings,
    homeLayout: HomeLayout,
    searchPreferences: SearchPreferences,
    notificationPreferences: NotificationPreferences,
    gesturePreferences: GesturePreferences,
    performancePreferences: PerformancePreferences,
    capabilities: AstraCapabilityReport,
    installedApps: List<AstraAppEntry>,
    palette: AstraPalette,
    onUpdateTheme: ((ThemeSettings) -> ThemeSettings) -> Unit,
    onUpdateHomeLayout: ((HomeLayout) -> HomeLayout) -> Unit,
    onUpdateSearch: ((SearchPreferences) -> SearchPreferences) -> Unit,
    onUpdateNotifications: ((NotificationPreferences) -> NotificationPreferences) -> Unit,
    onUpdateGestures: ((GesturePreferences) -> GesturePreferences) -> Unit,
    onUpdatePerformance: ((PerformancePreferences) -> PerformancePreferences) -> Unit,
    onRequestDefaultHomeRole: () -> Unit,
    onOpenNotificationAccessSettings: () -> Unit,
    onOpenWidgetPicker: () -> Unit,
    onOpenPersonalizationStudio: () -> Unit,
    onRescanPackages: () -> Unit,
    onResetDefaults: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSection by remember { mutableStateOf(AstraSettingsSection.APPEARANCE) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(palette.scrimOverlay)
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Astra Launcher Settings",
                        style = AstraTypography.TitleL,
                        color = palette.primaryText
                    )
                    Text(
                        text = "14 persistent launcher configuration categories",
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
                        text = "Home",
                        style = AstraTypography.Caption.copy(fontWeight = FontWeight.SemiBold),
                        color = palette.obsidian0,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 14 Section Selector Rail
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 2.dp)
            ) {
                items(AstraSettingsSection.entries, key = { it.id }) { section ->
                    AstraCategoryChip(
                        label = section.title,
                        selected = selectedSection == section,
                        palette = palette,
                        onClick = { selectedSection = section }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 28.dp)
            ) {
                // Default Home Role Banner
                item(key = "role_home_status") {
                    AstraSurfaceCard(
                        palette = palette,
                        useGlass = true,
                        modifier = Modifier.fillMaxWidth()
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
                                    text = if (capabilities.isCurrentlyDefaultHome)
                                        "Default Home App: Active (ROLE_HOME)"
                                    else
                                        "Astra is not yet set as Default Home App",
                                    style = AstraTypography.SectionHeader,
                                    color = if (capabilities.isCurrentlyDefaultHome) palette.successTone else palette.warningTone
                                )
                                Text(
                                    text = "Pressing the Android Home button returns directly to Astra when set as Default Home.",
                                    style = AstraTypography.Caption,
                                    color = palette.secondaryText
                                )
                            }
                            Surface(
                                modifier = Modifier.clickable(onClick = onRequestDefaultHomeRole),
                                shape = AstraShapes.ChipPill,
                                color = palette.primaryAccent
                            ) {
                                Text(
                                    text = if (capabilities.isCurrentlyDefaultHome) "Change" else "Set Default",
                                    style = AstraTypography.Caption.copy(fontWeight = FontWeight.SemiBold),
                                    color = palette.obsidian0,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                when (selectedSection) {
                    AstraSettingsSection.APPEARANCE -> {
                        item {
                            SettingsCard(title = "1. Appearance & Atmosphere", palette = palette) {
                                Text("Theme Preset", style = AstraTypography.Caption, color = palette.secondaryText)
                                Spacer(modifier = Modifier.height(6.dp))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(AstraThemePreset.entries, key = { it.id }) { preset ->
                                        AstraCategoryChip(
                                            label = preset.displayName,
                                            selected = themeSettings.themePreset == preset,
                                            palette = palette,
                                            onClick = {
                                                onUpdateTheme {
                                                    it.copy(
                                                        themePreset = preset,
                                                        wallpaperId = preset.defaultWallpaper,
                                                        customAccentHex = preset.primaryAccentHex
                                                    )
                                                }
                                            }
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text("Theme Mode", style = AstraTypography.Caption, color = palette.secondaryText)
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    AstraThemeMode.entries.forEach { mode ->
                                        AstraCategoryChip(
                                            label = mode.label,
                                            selected = themeSettings.themeMode == mode,
                                            palette = palette,
                                            onClick = { onUpdateTheme { it.copy(themeMode = mode) } }
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                ActionChipButton("Open Personalization Studio", palette, onOpenPersonalizationStudio)
                            }
                        }
                    }

                    AstraSettingsSection.HOME_SCREEN -> {
                        item {
                            SettingsCard(title = "2. Home Screen Workspace", palette = palette) {
                                ToggleRow(
                                    label = "Show Live Clock & Date on Page 1",
                                    checked = homeLayout.showClockOnWorkspace,
                                    palette = palette,
                                    onCheckedChange = { v -> onUpdateHomeLayout { it.copy(showClockOnWorkspace = v) } }
                                )
                                ToggleRow(
                                    label = "Lock Workspace Layout (Prevent accidental moves)",
                                    checked = homeLayout.lockWorkspaceLayout,
                                    palette = palette,
                                    onCheckedChange = { v -> onUpdateHomeLayout { it.copy(lockWorkspaceLayout = v) } }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Grid Dimensions", style = AstraTypography.Caption, color = palette.secondaryText)
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf(4 to 5, 4 to 6, 5 to 5, 5 to 6).forEach { (c, r) ->
                                        AstraCategoryChip(
                                            label = "${c}×${r}",
                                            selected = homeLayout.gridColumns == c && homeLayout.gridRows == r,
                                            palette = palette,
                                            onClick = { onUpdateHomeLayout { it.copy(gridColumns = c, gridRows = r) } }
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Clock Typography Style", style = AstraTypography.Caption, color = palette.secondaryText)
                                Spacer(modifier = Modifier.height(6.dp))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(AstraClockStyle.entries, key = { it.id }) { cs ->
                                        AstraCategoryChip(
                                            label = cs.label,
                                            selected = themeSettings.clockStyle == cs,
                                            palette = palette,
                                            onClick = { onUpdateTheme { it.copy(clockStyle = cs) } }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    AstraSettingsSection.APP_DRAWER -> {
                        item {
                            SettingsCard(title = "3. App Drawer / App Library", palette = palette) {
                                ToggleRow(
                                    label = "Show Recent & Frequent Apps Row",
                                    checked = performancePreferences.showDrawerRecentRow,
                                    palette = palette,
                                    onCheckedChange = { v -> onUpdatePerformance { it.copy(showDrawerRecentRow = v) } }
                                )
                                ToggleRow(
                                    label = "Show Category Filter Tabs",
                                    checked = performancePreferences.showDrawerCategories,
                                    palette = palette,
                                    onCheckedChange = { v -> onUpdatePerformance { it.copy(showDrawerCategories = v) } }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Discovered Launchable Apps: ${installedApps.size}",
                                    style = AstraTypography.Caption,
                                    color = palette.primaryAccent
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                ActionChipButton("Rescan Installed Apps Now", palette, onRescanPackages)
                            }
                        }
                    }

                    AstraSettingsSection.ICONS -> {
                        item {
                            SettingsCard(title = "4. Real App Icons & Treatment", palette = palette) {
                                Text(
                                    text = "Astra loads every app's real LauncherActivityInfo / AdaptiveIconDrawable icon and caches it in memory.",
                                    style = AstraTypography.Caption,
                                    color = palette.secondaryText
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(AstraIconStyle.entries, key = { it.id }) { style ->
                                        AstraCategoryChip(
                                            label = style.label,
                                            selected = themeSettings.iconStyle == style,
                                            palette = palette,
                                            onClick = { onUpdateTheme { it.copy(iconStyle = style) } }
                                        )
                                    }
                                }
                                ToggleRow(
                                    label = "Show Icon Labels on Workspace & Drawer",
                                    checked = themeSettings.showIconLabels,
                                    palette = palette,
                                    onCheckedChange = { v -> onUpdateTheme { it.copy(showIconLabels = v) } }
                                )
                            }
                        }
                    }

                    AstraSettingsSection.WIDGETS -> {
                        item {
                            SettingsCard(title = "5. Android AppWidgetHost Integration", palette = palette) {
                                Text(
                                    text = "Astra hosts real Android widgets via AppWidgetHost (ID 2026) and AppWidgetManager.",
                                    style = AstraTypography.BodyM,
                                    color = palette.secondaryText
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                ActionChipButton("Open Android Widget Picker", palette, onOpenWidgetPicker)
                            }
                        }
                    }

                    AstraSettingsSection.DOCK -> {
                        item {
                            SettingsCard(title = "6. Persistent Home Dock", palette = palette) {
                                Text("Dock Slot Capacity", style = AstraTypography.Caption, color = palette.secondaryText)
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf(4, 5, 6).forEach { count ->
                                        AstraCategoryChip(
                                            label = "$count Slots",
                                            selected = homeLayout.dockSlotCount == count,
                                            palette = palette,
                                            onClick = { onUpdateHomeLayout { it.copy(dockSlotCount = count) } }
                                        )
                                    }
                                }
                                ToggleRow(
                                    label = "Smoked Glass Dock Surface",
                                    checked = themeSettings.dockStyleGlass,
                                    palette = palette,
                                    onCheckedChange = { v -> onUpdateTheme { it.copy(dockStyleGlass = v) } }
                                )
                                ToggleRow(
                                    label = "Show Labels in Dock",
                                    checked = themeSettings.showDockLabels,
                                    palette = palette,
                                    onCheckedChange = { v -> onUpdateTheme { it.copy(showDockLabels = v) } }
                                )
                            }
                        }
                    }

                    AstraSettingsSection.SEARCH -> {
                        item {
                            SettingsCard(title = "7. Universal Keyboard-First Search", palette = palette) {
                                ToggleRow(
                                    label = "Auto-Focus Keyboard on Search Open",
                                    checked = searchPreferences.autoFocusKeyboard,
                                    palette = palette,
                                    onCheckedChange = { v -> onUpdateSearch { it.copy(autoFocusKeyboard = v) } }
                                )
                                ToggleRow(
                                    label = "Search Installed Applications",
                                    checked = searchPreferences.searchApps,
                                    palette = palette,
                                    onCheckedChange = { v -> onUpdateSearch { it.copy(searchApps = v) } }
                                )
                                ToggleRow(
                                    label = "Search App Shortcuts (LauncherApps)",
                                    checked = searchPreferences.searchShortcuts,
                                    palette = palette,
                                    onCheckedChange = { v -> onUpdateSearch { it.copy(searchShortcuts = v) } }
                                )
                                ToggleRow(
                                    label = "Search Android System Settings Deep-Links",
                                    checked = searchPreferences.searchSettings,
                                    palette = palette,
                                    onCheckedChange = { v -> onUpdateSearch { it.copy(searchSettings = v) } }
                                )
                            }
                        }
                    }

                    AstraSettingsSection.GESTURES -> {
                        item {
                            SettingsCard(title = "8. Workspace Gestures & Haptics", palette = palette) {
                                Text("Swipe Down on Workspace Action", style = AstraTypography.Caption, color = palette.secondaryText)
                                Spacer(modifier = Modifier.height(6.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    SwipeDownAction.entries.forEach { action ->
                                        AstraCategoryChip(
                                            label = action.label,
                                            selected = gesturePreferences.swipeDownAction == action,
                                            palette = palette,
                                            onClick = { onUpdateGestures { it.copy(swipeDownAction = action) } }
                                        )
                                    }
                                }
                                ToggleRow(
                                    label = "Tactile Haptic Feedback",
                                    checked = gesturePreferences.hapticsEnabled,
                                    palette = palette,
                                    onCheckedChange = { v -> onUpdateGestures { it.copy(hapticsEnabled = v) } }
                                )
                            }
                        }
                    }

                    AstraSettingsSection.WALLPAPER -> {
                        item {
                            SettingsCard(title = "9. Wallpaper System", palette = palette) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    WallpaperSource.entries.forEach { src ->
                                        AstraCategoryChip(
                                            label = src.label,
                                            selected = themeSettings.wallpaperSource == src,
                                            palette = palette,
                                            onClick = { onUpdateTheme { it.copy(wallpaperSource = src) } }
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(AstraWallpaperId.entries, key = { it.id }) { wp ->
                                        AstraCategoryChip(
                                            label = wp.title,
                                            selected = themeSettings.wallpaperId == wp,
                                            palette = palette,
                                            onClick = {
                                                onUpdateTheme {
                                                    it.copy(
                                                        wallpaperSource = WallpaperSource.BUNDLED_ASTRA,
                                                        wallpaperId = wp
                                                    )
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    AstraSettingsSection.NOTIFICATIONS -> {
                        item {
                            SettingsCard(title = "10. Notification Badges & Listener", palette = palette) {
                                Text(
                                    text = if (capabilities.hasNotificationAccess)
                                        "NotificationListenerService: Granted"
                                    else
                                        "NotificationListenerService: Not granted (badges disabled until granted)",
                                    style = AstraTypography.BodyM,
                                    color = if (capabilities.hasNotificationAccess) palette.successTone else palette.warningTone
                                )
                                ToggleRow(
                                    label = "Show Notification Count Badges on App Icons",
                                    checked = notificationPreferences.showAppBadgeDots,
                                    palette = palette,
                                    onCheckedChange = { v -> onUpdateNotifications { it.copy(showAppBadgeDots = v) } }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                ActionChipButton("Configure Android Notification Access ↗", palette, onOpenNotificationAccessSettings)
                            }
                        }
                    }

                    AstraSettingsSection.PRIVACY -> {
                        item {
                            SettingsCard(title = "11. Privacy & Hidden Applications", palette = palette) {
                                Text(
                                    text = "Hidden Apps (${homeLayout.hiddenComponents.size}) are excluded from the main App Drawer and Search results.",
                                    style = AstraTypography.BodyM,
                                    color = palette.secondaryText
                                )
                                if (homeLayout.hiddenComponents.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    ActionChipButton("Unhide All Apps", palette) {
                                        onUpdateHomeLayout { it.copy(hiddenComponents = emptySet()) }
                                    }
                                }
                            }
                        }
                    }

                    AstraSettingsSection.PERFORMANCE -> {
                        item {
                            SettingsCard(title = "12. Performance & Low-Memory Budget", palette = palette) {
                                ToggleRow(
                                    label = "Enable Spring Motion & Transitions",
                                    checked = performancePreferences.animationsEnabled,
                                    palette = palette,
                                    onCheckedChange = { v -> onUpdatePerformance { it.copy(animationsEnabled = v) } }
                                )
                                ToggleRow(
                                    label = "Enable Translucent Glass Surfaces",
                                    checked = performancePreferences.blurEnabled,
                                    palette = palette,
                                    onCheckedChange = { v -> onUpdatePerformance { it.copy(blurEnabled = v) } }
                                )
                                ToggleRow(
                                    label = "Force Low-RAM / Solid Surface Mode",
                                    checked = performancePreferences.lowEndDeviceModeOverride,
                                    palette = palette,
                                    onCheckedChange = { v -> onUpdatePerformance { it.copy(lowEndDeviceModeOverride = v) } }
                                )
                            }
                        }
                    }

                    AstraSettingsSection.ACCESSIBILITY -> {
                        item {
                            SettingsCard(title = "13. Accessibility & Legibility", palette = palette) {
                                ToggleRow(
                                    label = "High-Contrast Text & Borders (WCAG AAA)",
                                    checked = themeSettings.highContrastMode,
                                    palette = palette,
                                    onCheckedChange = { v -> onUpdateTheme { it.copy(highContrastMode = v) } }
                                )
                                ToggleRow(
                                    label = "Reduced Motion Mode",
                                    checked = performancePreferences.reducedMotion,
                                    palette = palette,
                                    onCheckedChange = { v -> onUpdatePerformance { it.copy(reducedMotion = v) } }
                                )
                            }
                        }
                    }

                    AstraSettingsSection.ABOUT_ASTRA -> {
                        item {
                            SettingsCard(title = "14. About Astra Launcher", palette = palette) {
                                Text(
                                    text = "Astra Launcher v2.0.0 (com.astra.launcher)",
                                    style = AstraTypography.SectionHeader,
                                    color = palette.primaryText
                                )
                                Text(
                                    text = "Real Android Home Launcher · Persistent 2D Workspace · LauncherApps & AppWidgetHost Engine · Zero Telemetry",
                                    style = AstraTypography.BodyM,
                                    color = palette.secondaryText
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                ActionChipButton("Reset Workspace & Settings to Defaults", palette, onResetDefaults)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsCard(
    title: String,
    palette: AstraPalette,
    content: @Composable () -> Unit
) {
    AstraSurfaceCard(
        palette = palette,
        useGlass = false,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = AstraTypography.SectionHeader,
                color = palette.primaryText
            )
            Spacer(modifier = Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
private fun ToggleRow(
    label: String,
    checked: Boolean,
    palette: AstraPalette,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = AstraTypography.BodyM,
            color = palette.primaryText,
            modifier = Modifier.weight(1f)
        )
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun ActionChipButton(
    label: String,
    palette: AstraPalette,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = AstraShapes.ChipPill,
        color = palette.primaryAccent.copy(alpha = 0.18f),
        border = BorderStroke(1.dp, palette.primaryAccent)
    ) {
        Text(
            text = label,
            style = AstraTypography.Caption.copy(fontWeight = FontWeight.SemiBold),
            color = palette.primaryAccent,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        )
    }
}
