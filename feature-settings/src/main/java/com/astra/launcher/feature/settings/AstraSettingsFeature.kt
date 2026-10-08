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
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import com.astra.launcher.core.storage.AppSortOrder
import com.astra.launcher.core.storage.AstraAppEntry
import com.astra.launcher.core.storage.AstraCapabilityReport
import com.astra.launcher.core.storage.AstraClockStyle
import com.astra.launcher.core.storage.AstraIconStyle
import com.astra.launcher.core.storage.AstraThemeMode
import com.astra.launcher.core.storage.AstraThemePreset
import com.astra.launcher.core.storage.AstraWallpaperId
import com.astra.launcher.core.storage.DoubleTapAction
import com.astra.launcher.core.storage.GesturePreferences
import com.astra.launcher.core.storage.HomeDensityMode
import com.astra.launcher.core.storage.HomeLayout
import com.astra.launcher.core.storage.NotificationPreferences
import com.astra.launcher.core.storage.PerformancePreferences
import com.astra.launcher.core.storage.SearchPreferences
import com.astra.launcher.core.storage.SwipeDownAction
import com.astra.launcher.core.storage.ThemeSettings
import com.astra.launcher.core.storage.WallpaperSource

/**
 * Dedicated Astra Settings Surface (Rebuild Sections 7, 13, 14, 44, 46, 47).
 * Organized into the exact categories defined in Rebuild Section 13:
 * Home, Appearance, Search, Gestures, Apps, Notifications, Widgets, Performance, About Astra.
 * Zero advertising settings, zero telemetry.
 */
enum class AstraSettingsSection(val id: String, val title: String) {
    HOME("home", "Home"),
    APPEARANCE("appearance", "Appearance"),
    SEARCH("search", "Search"),
    GESTURES("gestures", "Gestures"),
    APPS("apps", "Apps"),
    NOTIFICATIONS("notifications", "Notifications"),
    WIDGETS("widgets", "Widgets"),
    PERFORMANCE("performance", "Performance"),
    ABOUT_ASTRA("about", "About Astra")
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
    onReopenFirstRunSetup: () -> Unit,
    onRescanPackages: () -> Unit,
    onResetDefaults: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSection by remember { mutableStateOf(AstraSettingsSection.HOME) }

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Astra Settings",
                        style = AstraTypography.TitleL,
                        color = palette.primaryText
                    )
                    Text(
                        text = "Personalize your Astra Home environment",
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

            Spacer(modifier = Modifier.height(10.dp))

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
                // Default Home Role & HiOS Status Card
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
                                        "Default Home: Astra Launcher ✓"
                                    else
                                        "Astra is not currently Default Home",
                                    style = AstraTypography.SectionHeader,
                                    color = if (capabilities.isCurrentlyDefaultHome) palette.successTone else palette.warningTone
                                )
                                Text(
                                    text = if (capabilities.currentDefaultHomePackage.isNotBlank())
                                        "Active Home role holder: ${capabilities.currentDefaultHomePackage}"
                                    else
                                        "Pressing the Android Home button returns directly to Astra when set as Default Home.",
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
                                    text = if (capabilities.isCurrentlyDefaultHome) "Verify" else "Set Default",
                                    style = AstraTypography.Caption.copy(fontWeight = FontWeight.SemiBold),
                                    color = palette.obsidian0,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                when (selectedSection) {
                    AstraSettingsSection.HOME -> {
                        item {
                            SettingsCard(title = "Home Layout, Grid, Dock & Pages", palette = palette) {
                                Text("Home Density Preset", style = AstraTypography.Caption, color = palette.secondaryText)
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    HomeDensityMode.entries.forEach { mode ->
                                        AstraCategoryChip(
                                            label = mode.title,
                                            selected = homeLayout.densityMode == mode,
                                            palette = palette,
                                            onClick = {
                                                onUpdateHomeLayout {
                                                    it.copy(
                                                        densityMode = mode,
                                                        gridColumns = mode.defaultColumns,
                                                        gridRows = mode.defaultRows
                                                    )
                                                }
                                            }
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(10.dp))
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
                                Spacer(modifier = Modifier.height(10.dp))
                                Text("Dock Capacity", style = AstraTypography.Caption, color = palette.secondaryText)
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf(4, 5, 6).forEach { count ->
                                        AstraCategoryChip(
                                            label = "$count Apps",
                                            selected = homeLayout.dockSlotCount == count,
                                            palette = palette,
                                            onClick = { onUpdateHomeLayout { it.copy(dockSlotCount = count) } }
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                ToggleRow(
                                    label = "Show Persistent Home Dock",
                                    checked = homeLayout.dockEnabled,
                                    palette = palette,
                                    onCheckedChange = { v -> onUpdateHomeLayout { it.copy(dockEnabled = v) } }
                                )
                                ToggleRow(
                                    label = "Show Dock App Labels",
                                    checked = themeSettings.showDockLabels,
                                    palette = palette,
                                    onCheckedChange = { v -> onUpdateTheme { it.copy(showDockLabels = v) } }
                                )
                                ToggleRow(
                                    label = "Show Page Indicator Dots",
                                    checked = homeLayout.showPageIndicator,
                                    palette = palette,
                                    onCheckedChange = { v -> onUpdateHomeLayout { it.copy(showPageIndicator = v) } }
                                )
                                ToggleRow(
                                    label = "Show Adaptive Clock & Contextual Status on Page 1",
                                    checked = homeLayout.showClockOnWorkspace,
                                    palette = palette,
                                    onCheckedChange = { v -> onUpdateHomeLayout { it.copy(showClockOnWorkspace = v) } }
                                )
                                ToggleRow(
                                    label = "Show App Icon Labels",
                                    checked = themeSettings.showIconLabels,
                                    palette = palette,
                                    onCheckedChange = { v -> onUpdateTheme { it.copy(showIconLabels = v) } }
                                )
                                ToggleRow(
                                    label = "Lock Home Layout (Prevent accidental moves)",
                                    checked = homeLayout.lockWorkspaceLayout,
                                    palette = palette,
                                    onCheckedChange = { v -> onUpdateHomeLayout { it.copy(lockWorkspaceLayout = v) } }
                                )
                            }
                        }
                    }

                    AstraSettingsSection.APPEARANCE -> {
                        item {
                            SettingsCard(title = "Wallpaper, Theme, Icons & Typography", palette = palette) {
                                Text("Astra Atmosphere Preset", style = AstraTypography.Caption, color = palette.secondaryText)
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
                                Text("Wallpaper Family (Orbit · Nocturne Flow · Horizon)", style = AstraTypography.Caption, color = palette.secondaryText)
                                Spacer(modifier = Modifier.height(6.dp))
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
                                Spacer(modifier = Modifier.height(10.dp))
                                Text("Icon Treatment (Real Application Icons)", style = AstraTypography.Caption, color = palette.secondaryText)
                                Spacer(modifier = Modifier.height(6.dp))
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
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Icon Size: ${(themeSettings.iconScale * 100).toInt()}%",
                                    style = AstraTypography.Caption,
                                    color = palette.primaryText
                                )
                                Slider(
                                    value = themeSettings.iconScale,
                                    onValueChange = { s -> onUpdateTheme { it.copy(iconScale = s) } },
                                    valueRange = 0.85f..1.20f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = palette.primaryAccent,
                                        activeTrackColor = palette.primaryAccent
                                    )
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Clock Style", style = AstraTypography.Caption, color = palette.secondaryText)
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
                                Spacer(modifier = Modifier.height(8.dp))
                                ToggleRow(
                                    label = "High-Contrast Accessibility Mode",
                                    checked = themeSettings.highContrastMode,
                                    palette = palette,
                                    onCheckedChange = { v -> onUpdateTheme { it.copy(highContrastMode = v) } }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                ActionChipButton("Open Live Wallpaper & Style Studio", palette, onOpenPersonalizationStudio)
                            }
                        }
                    }

                    AstraSettingsSection.SEARCH -> {
                        item {
                            SettingsCard(title = "Universal System Search", palette = palette) {
                                ToggleRow(
                                    label = "Open Keyboard Immediately on Search",
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
                                    label = "Search Application Shortcuts",
                                    checked = searchPreferences.searchShortcuts,
                                    palette = palette,
                                    onCheckedChange = { v -> onUpdateSearch { it.copy(searchShortcuts = v) } }
                                )
                                ToggleRow(
                                    label = "Search System Settings & Astra Customization",
                                    checked = searchPreferences.searchSettings,
                                    palette = palette,
                                    onCheckedChange = { v -> onUpdateSearch { it.copy(searchSettings = v) } }
                                )
                            }
                        }
                    }

                    AstraSettingsSection.GESTURES -> {
                        item {
                            SettingsCard(title = "Home Gestures & Navigation", palette = palette) {
                                Text("Swipe Down on Home Screen", style = AstraTypography.Caption, color = palette.secondaryText)
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
                                Spacer(modifier = Modifier.height(10.dp))
                                Text("Double Tap Empty Home Space", style = AstraTypography.Caption, color = palette.secondaryText)
                                Spacer(modifier = Modifier.height(6.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    DoubleTapAction.entries.forEach { action ->
                                        AstraCategoryChip(
                                            label = action.label,
                                            selected = gesturePreferences.doubleTapAction == action,
                                            palette = palette,
                                            onClick = { onUpdateGestures { it.copy(doubleTapAction = action) } }
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                ToggleRow(
                                    label = "Tactile Haptic Feedback",
                                    checked = gesturePreferences.hapticsEnabled,
                                    palette = palette,
                                    onCheckedChange = { v -> onUpdateGestures { it.copy(hapticsEnabled = v) } }
                                )
                            }
                        }
                    }

                    AstraSettingsSection.APPS -> {
                        item {
                            SettingsCard(title = "App Discovery, Categories & Hidden Apps", palette = palette) {
                                ToggleRow(
                                    label = "Default to Modern Discovery Surface (Categories, Favorites, Recent)",
                                    checked = performancePreferences.showDrawerCategories,
                                    palette = palette,
                                    onCheckedChange = { v -> onUpdatePerformance { it.copy(showDrawerCategories = v) } }
                                )
                                ToggleRow(
                                    label = "Show Recently Used Strip",
                                    checked = performancePreferences.showDrawerRecentRow,
                                    palette = palette,
                                    onCheckedChange = { v -> onUpdatePerformance { it.copy(showDrawerRecentRow = v) } }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Default All-Apps Sort Order", style = AstraTypography.Caption, color = palette.secondaryText)
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    AppSortOrder.entries.forEach { order ->
                                        AstraCategoryChip(
                                            label = order.label,
                                            selected = performancePreferences.appSortOrder == order,
                                            palette = palette,
                                            onClick = { onUpdatePerformance { it.copy(appSortOrder = order) } }
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Discovered Installed Apps: ${installedApps.size} · Hidden Apps: ${homeLayout.hiddenComponents.size}",
                                    style = AstraTypography.Caption,
                                    color = palette.primaryAccent
                                )
                                if (homeLayout.hiddenComponents.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    ActionChipButton("Unhide All (${homeLayout.hiddenComponents.size}) Apps", palette) {
                                        onUpdateHomeLayout { it.copy(hiddenComponents = emptySet()) }
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                ActionChipButton("Rescan Installed Applications", palette, onRescanPackages)
                            }
                        }
                    }

                    AstraSettingsSection.NOTIFICATIONS -> {
                        item {
                            SettingsCard(title = "Notification Integration & Badges", palette = palette) {
                                Text(
                                    text = if (capabilities.hasNotificationAccess)
                                        "Notification Access: Active ✓"
                                    else
                                        "Notification Access: Not enabled (tap below to grant in Android Settings)",
                                    style = AstraTypography.BodyM,
                                    color = if (capabilities.hasNotificationAccess) palette.successTone else palette.warningTone
                                )
                                ToggleRow(
                                    label = "Show Notification Badges on App Icons",
                                    checked = notificationPreferences.showAppBadgeDots,
                                    palette = palette,
                                    onCheckedChange = { v -> onUpdateNotifications { it.copy(showAppBadgeDots = v) } }
                                )
                                ToggleRow(
                                    label = "Show Active Notification Pill on Home Header",
                                    checked = notificationPreferences.showHomeNotificationPill,
                                    palette = palette,
                                    onCheckedChange = { v -> onUpdateNotifications { it.copy(showHomeNotificationPill = v) } }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                ActionChipButton("Configure Android Notification Access ↗", palette, onOpenNotificationAccessSettings)
                            }
                        }
                    }

                    AstraSettingsSection.WIDGETS -> {
                        item {
                            val widgetCount = homeLayout.items.count { it.appWidgetId >= 0 }
                            SettingsCard(title = "Android Widget Management", palette = palette) {
                                Text(
                                    text = "Active Bound Widgets on Workspace: $widgetCount",
                                    style = AstraTypography.BodyM,
                                    color = palette.primaryText
                                )
                                Text(
                                    text = "Long-press Home and tap Widgets to place, move, or resize real Android AppWidgetHost widgets across your pages.",
                                    style = AstraTypography.Caption,
                                    color = palette.secondaryText
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                ActionChipButton("Open Android Widget Picker", palette, onOpenWidgetPicker)
                            }
                        }
                    }

                    AstraSettingsSection.PERFORMANCE -> {
                        item {
                            SettingsCard(title = "Performance & Low-End Hardware Budget", palette = palette) {
                                ToggleRow(
                                    label = "Smooth Spatial Animations",
                                    checked = performancePreferences.animationsEnabled,
                                    palette = palette,
                                    onCheckedChange = { v -> onUpdatePerformance { it.copy(animationsEnabled = v) } }
                                )
                                ToggleRow(
                                    label = "Translucent Smoked Glass Surfaces",
                                    checked = performancePreferences.blurEnabled,
                                    palette = palette,
                                    onCheckedChange = { v -> onUpdatePerformance { it.copy(blurEnabled = v) } }
                                )
                                ToggleRow(
                                    label = "Battery-Conscious / Low-RAM Mode (Solid Graphite Surfaces)",
                                    checked = performancePreferences.lowEndDeviceModeOverride,
                                    palette = palette,
                                    onCheckedChange = { v -> onUpdatePerformance { it.copy(lowEndDeviceModeOverride = v) } }
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
                            SettingsCard(title = "About Astra, Privacy & Diagnostics", palette = palette) {
                                Text(
                                    text = "Astra Launcher v2.1.0 (com.astra.launcher)",
                                    style = AstraTypography.SectionHeader,
                                    color = palette.primaryText
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "• Zero Advertising: Astra contains zero ad SDKs, zero sponsored results, and zero promotional cards.\n" +
                                        "• Local-First Privacy: All app indexing, usage ranking, favorites, and workspace state stay 100% on your device.\n" +
                                        "• Default Home Role: ${if (capabilities.isCurrentlyDefaultHome) "Active" else "Inactive (${capabilities.currentDefaultHomePackage.ifBlank { "System Default" }})"}\n" +
                                        "• TECNO / HiOS Environment Detected: ${if (capabilities.isHiOsDetectedOnDevice) "Yes (Ensure Astra is selected in Settings -> Apps -> Default Apps -> Home App)" else "Standard Android"}",
                                    style = AstraTypography.Caption,
                                    color = palette.secondaryText
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    ActionChipButton("Re-run First-Run Setup", palette) {
                                        onClose()
                                        onReopenFirstRunSetup()
                                    }
                                    ActionChipButton("Reset Workspace", palette, onResetDefaults)
                                }
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
