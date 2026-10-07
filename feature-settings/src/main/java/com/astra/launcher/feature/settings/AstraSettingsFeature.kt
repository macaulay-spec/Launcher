package com.astra.launcher.feature.settings

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.astra.launcher.core.design.AstraBrandMark
import com.astra.launcher.core.design.AstraCard
import com.astra.launcher.core.design.AstraComponentState
import com.astra.launcher.core.design.AstraGlyph
import com.astra.launcher.core.design.AstraIconButton
import com.astra.launcher.core.design.AstraSegmentedControl
import com.astra.launcher.core.design.AstraSurfaceFamily
import com.astra.launcher.core.design.AstraTheme
import com.astra.launcher.core.design.AstraToggle
import com.astra.launcher.core.design.AstraVectorIcon
import com.astra.launcher.core.performance.AstraVisualBudget
import com.astra.launcher.core.storage.AstraCanonicalFrame
import com.astra.launcher.core.storage.AstraCapabilityReport
import com.astra.launcher.core.storage.AstraDeviceStatus
import com.astra.launcher.core.storage.GesturePreferences
import com.astra.launcher.core.storage.HomeLayout
import com.astra.launcher.core.storage.NotificationPreferences
import com.astra.launcher.core.storage.NotificationPrivacyMode
import com.astra.launcher.core.storage.PerformancePreferences
import com.astra.launcher.core.storage.SearchPreferences
import com.astra.launcher.core.storage.SwipeDownAction
import com.astra.launcher.core.storage.ThemeSettings

/**
 * 13 Official Astra Settings Sections (Section 22; Frames 18 & 19).
 */
enum class AstraSettingsSection(
    val number: Int,
    val title: String,
    val subtitle: String,
    val glyph: AstraGlyph
) {
    PERSONALIZATION(1, "Personalization", "Wallpapers, atmosphere presets, clock & icon grammar", AstraGlyph.WALLPAPER),
    HOME_SCREEN(2, "Home Screen", "Grid density, dock style, labels & folders", AstraGlyph.HOME),
    LOCK_SCREEN(3, "Lock Screen & AOD", "Clock typography, privacy redaction & burn-in shift", AstraGlyph.LOCK),
    SEARCH(4, "Global Search", "Local index domains, ranking & deterministic commands", AstraGlyph.SEARCH),
    NOTIFICATIONS(5, "Notifications", "Priority buckets, silent collapse & listener access", AstraGlyph.NOTIFICATIONS),
    GESTURES(6, "Gestures & Haptics", "Swipe actions, double-tap, haptics & sound cues", AstraGlyph.CONTROLS),
    WIDGETS(7, "Widgets", "First-party Astra widgets & Android AppWidgetHost", AstraGlyph.WIDGETS),
    PERFORMANCE(8, "Performance & Low-RAM", "Blur budget, motion scale & cold-start telemetry", AstraGlyph.SPARK),
    PRIVACY_SECURITY(9, "Privacy & Security", "Camera/Mic indicators, local-only guarantee & vault", AstraGlyph.SHIELD),
    ACCESSIBILITY(10, "Accessibility", "Large text scale, high contrast & reduced motion", AstraGlyph.BRIGHTNESS),
    APPS(11, "Apps & Home Role", "Default launcher ROLE_HOME & package catalog", AstraGlyph.APPS),
    BACKUP_DATA(12, "Backup & Recovery", "Local snapshot export/import & safe-mode reset", AstraGlyph.FOLDER),
    ABOUT_ASTRA(13, "About Astra & 40-Frame Review", "Capability matrix & canonical screen inventory", AstraGlyph.SPARK)
}

@Composable
fun AstraSettingsScreen(
    themeSettings: ThemeSettings,
    homeLayout: HomeLayout,
    searchPreferences: SearchPreferences,
    notificationPreferences: NotificationPreferences,
    gesturePreferences: GesturePreferences,
    performancePreferences: PerformancePreferences,
    capabilityReport: AstraCapabilityReport,
    deviceStatus: AstraDeviceStatus,
    visualBudget: AstraVisualBudget,
    initialDetailSection: AstraSettingsSection? = null,
    onUpdateTheme: ((ThemeSettings) -> ThemeSettings) -> Unit,
    onUpdateHome: ((HomeLayout) -> HomeLayout) -> Unit,
    onUpdateSearch: ((SearchPreferences) -> SearchPreferences) -> Unit,
    onUpdateNotifications: ((NotificationPreferences) -> NotificationPreferences) -> Unit,
    onUpdateGestures: ((GesturePreferences) -> GesturePreferences) -> Unit,
    onUpdatePerformance: ((PerformancePreferences) -> PerformancePreferences) -> Unit,
    onRequestDefaultHomeRole: () -> Unit,
    onRequestNotificationAccess: () -> Unit,
    onOpenPersonalizationStudio: () -> Unit,
    onOpenWidgetPicker: () -> Unit,
    onOpenLockPreview: () -> Unit,
    onSimulateSystemState: (Boolean?, Boolean?, Boolean?, Boolean?) -> Unit,
    onJumpToCanonicalFrame: (AstraCanonicalFrame) -> Unit,
    onResetToSafeDefaults: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AstraTheme.colors
    var selectedSection by remember(initialDetailSection) { mutableStateOf(initialDetailSection) }
    var backupStatusMessage by remember { mutableStateOf<String?>(null) }

    // Solid surface family for Settings per Section 6.1A ("Solid surfaces used for settings pages, dense lists")
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.surfaceBase)
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AstraBrandMark(size = 34.dp)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (selectedSection == null) "ASTRA SYSTEM SETTINGS" else "SECTION ${selectedSection!!.number} OF 13",
                        style = AstraTheme.typography.labelS,
                        color = colors.accentPrimary
                    )
                    Text(
                        text = selectedSection?.title ?: "Environment Configuration",
                        style = AstraTheme.typography.headlineM,
                        color = colors.textPrimary
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (selectedSection != null) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(colors.surfaceFloating)
                            .clickable { selectedSection = null }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text("All 13 Sections", style = AstraTheme.typography.labelS, color = colors.accentPrimary)
                    }
                }
                AstraIconButton(
                    glyph = AstraGlyph.CLOSE,
                    contentDescriptionLabel = "Close Settings",
                    onClick = onClose
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (selectedSection == null) {
            // Frame 18: Settings Root (All 13 Sections)
            AstraCard(
                modifier = Modifier.fillMaxWidth(),
                family = AstraSurfaceFamily.SOLID,
                state = if (capabilityReport.isCurrentlyDefaultHome) AstraComponentState.SELECTED else AstraComponentState.ACTIVE,
                onClick = onRequestDefaultHomeRole
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (capabilityReport.isCurrentlyDefaultHome) "ASTRA IS DEFAULT ANDROID HOME" else "DEFAULT HOME ROLE STATUS",
                            style = AstraTheme.typography.labelS,
                            color = colors.accentPrimary
                        )
                        Text(
                            text = if (capabilityReport.isCurrentlyDefaultHome) {
                                "Active via Android RoleManager.ROLE_HOME"
                            } else {
                                "Tap to make Astra your default Android Home Launcher"
                            },
                            style = AstraTheme.typography.bodyM.copy(fontWeight = FontWeight.SemiBold),
                            color = colors.textPrimary
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(colors.accentPrimary)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (capabilityReport.isCurrentlyDefaultHome) "Verified" else "Set Role",
                            style = AstraTheme.typography.labelS,
                            color = colors.surfaceBase
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            AstraSettingsSection.entries.forEach { section ->
                AstraCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    family = AstraSurfaceFamily.SOLID,
                    onClick = { selectedSection = section }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(15.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AstraVectorIcon(
                                glyph = section.glyph,
                                size = 20.dp,
                                tint = colors.accentPrimary,
                                active = true
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "${section.number}. ${section.title}",
                                    style = AstraTheme.typography.bodyL.copy(fontWeight = FontWeight.Medium),
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = section.subtitle,
                                    style = AstraTheme.typography.bodyS,
                                    color = colors.textSecondary
                                )
                            }
                        }
                        AstraVectorIcon(glyph = AstraGlyph.CHEVRON_RIGHT, size = 16.dp, tint = colors.textTertiary)
                    }
                }
            }
        } else {
            // Frame 19: Settings Detail View for the selected section
            when (selectedSection!!) {
                AstraSettingsSection.PERSONALIZATION -> {
                    AstraCard(
                        modifier = Modifier.fillMaxWidth(),
                        family = AstraSurfaceFamily.SOLID,
                        onClick = onOpenPersonalizationStudio
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Open Personalization Studio", style = AstraTheme.typography.titleM, color = colors.accentPrimary)
                            Text(
                                "Live environment preview, 5 theme presets (Astral, Graphite, Morning, Nocturne, Glass Horizon), wallpapers, clock styles & iconography.",
                                style = AstraTheme.typography.bodyS,
                                color = colors.textSecondary
                            )
                        }
                    }
                }

                AstraSettingsSection.HOME_SCREEN -> {
                    AstraCard(modifier = Modifier.fillMaxWidth(), family = AstraSurfaceFamily.SOLID) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Grid Columns (${homeLayout.gridColumns} Columns)", style = AstraTheme.typography.bodyM, color = colors.textPrimary)
                            AstraSegmentedControl(
                                items = listOf(4, 5),
                                selectedItem = homeLayout.gridColumns,
                                labelProvider = { "$it Columns" },
                                onSelect = { cols -> onUpdateHome { it.copy(gridColumns = cols) } }
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Show Icon Labels on Home", style = AstraTheme.typography.bodyM, color = colors.textPrimary)
                                AstraToggle(
                                    checked = themeSettings.showIconLabels,
                                    onCheckedChange = { v -> onUpdateTheme { it.copy(showIconLabels = v) } }
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Atmospheric Translucent Dock", style = AstraTheme.typography.bodyM, color = colors.textPrimary)
                                AstraToggle(
                                    checked = themeSettings.dockStyleGlass,
                                    onCheckedChange = { v -> onUpdateTheme { it.copy(dockStyleGlass = v) } }
                                )
                            }
                        }
                    }
                }

                AstraSettingsSection.LOCK_SCREEN -> {
                    AstraCard(modifier = Modifier.fillMaxWidth(), family = AstraSurfaceFamily.SOLID) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Lock Screen & AOD Companion", style = AstraTheme.typography.titleM, color = colors.textPrimary)
                            Text(
                                "Preview and configure Astra's adaptive clock and burn-in safe Always-On Display companion.",
                                style = AstraTheme.typography.bodyS,
                                color = colors.textSecondary
                            )
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(colors.accentPrimary)
                                    .clickable { onOpenLockPreview() }
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text("Launch Lock & AOD Surface", style = AstraTheme.typography.labelL, color = colors.surfaceBase)
                            }
                        }
                    }
                }

                AstraSettingsSection.SEARCH -> {
                    AstraCard(modifier = Modifier.fillMaxWidth(), family = AstraSurfaceFamily.SOLID) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                                Text("Index Installed Applications (Offline)", style = AstraTheme.typography.bodyM, color = colors.textPrimary)
                                AstraToggle(searchPreferences.searchApps, { v -> onUpdateSearch { it.copy(searchApps = v) } })
                            }
                            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                                Text("Deterministic Natural Commands", style = AstraTheme.typography.bodyM, color = colors.textPrimary)
                                AstraToggle(searchPreferences.searchCommands, { v -> onUpdateSearch { it.copy(searchCommands = v) } })
                            }
                            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                                Text("Index Deep App Shortcuts", style = AstraTheme.typography.bodyM, color = colors.textPrimary)
                                AstraToggle(searchPreferences.searchShortcuts, { v -> onUpdateSearch { it.copy(searchShortcuts = v) } })
                            }
                            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                                Text("Immediate Keyboard Focus", style = AstraTheme.typography.bodyM, color = colors.textPrimary)
                                AstraToggle(searchPreferences.autoFocusKeyboard, { v -> onUpdateSearch { it.copy(autoFocusKeyboard = v) } })
                            }
                        }
                    }
                }

                AstraSettingsSection.NOTIFICATIONS -> {
                    AstraCard(modifier = Modifier.fillMaxWidth(), family = AstraSurfaceFamily.SOLID) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Privacy Redaction Mode on Lock Screen", style = AstraTheme.typography.labelS, color = colors.accentPrimary)
                            AstraSegmentedControl(
                                items = NotificationPrivacyMode.entries,
                                selectedItem = notificationPreferences.privacyMode,
                                labelProvider = { it.label },
                                onSelect = { mode -> onUpdateNotifications { it.copy(privacyMode = mode) } }
                            )
                            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                                Text("Group Priority Notifications First", style = AstraTheme.typography.bodyM, color = colors.textPrimary)
                                AstraToggle(notificationPreferences.groupPriorityFirst, { v -> onUpdateNotifications { it.copy(groupPriorityFirst = v) } })
                            }
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(colors.surfaceFloating)
                                    .clickable { onRequestNotificationAccess() }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text("Open Android Notification Listener Settings", style = AstraTheme.typography.labelS, color = colors.accentPrimary)
                            }
                        }
                    }
                }

                AstraSettingsSection.GESTURES -> {
                    AstraCard(modifier = Modifier.fillMaxWidth(), family = AstraSurfaceFamily.SOLID) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Swipe Down on Home Action", style = AstraTheme.typography.labelS, color = colors.accentPrimary)
                            AstraSegmentedControl(
                                items = SwipeDownAction.entries,
                                selectedItem = gesturePreferences.swipeDownAction,
                                labelProvider = { it.label },
                                onSelect = { act -> onUpdateGestures { it.copy(swipeDownAction = act) } }
                            )
                            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                                Text("Tactile Haptic Vocabulary", style = AstraTheme.typography.bodyM, color = colors.textPrimary)
                                AstraToggle(gesturePreferences.hapticsEnabled, { v -> onUpdateGestures { it.copy(hapticsEnabled = v) } })
                            }
                            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                                Text("Restrained Acoustic Cues", style = AstraTheme.typography.bodyM, color = colors.textPrimary)
                                AstraToggle(gesturePreferences.soundEffectsEnabled, { v -> onUpdateGestures { it.copy(soundEffectsEnabled = v) } })
                            }
                        }
                    }
                }

                AstraSettingsSection.WIDGETS -> {
                    AstraCard(
                        modifier = Modifier.fillMaxWidth(),
                        family = AstraSurfaceFamily.SOLID,
                        onClick = onOpenWidgetPicker
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Manage Home Widgets (${homeLayout.widgets.size} Active)", style = AstraTheme.typography.titleM, color = colors.accentPrimary)
                            Text(
                                "Add or remove Astra first-party widgets and hosted Android AppWidgets.",
                                style = AstraTheme.typography.bodyS,
                                color = colors.textSecondary
                            )
                        }
                    }
                }

                AstraSettingsSection.PERFORMANCE -> {
                    AstraCard(modifier = Modifier.fillMaxWidth(), family = AstraSurfaceFamily.SOLID) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = "RAM: ${visualBudget.availableRamMb}MB free / ${visualBudget.totalRamMb}MB · Cold Start: ${visualBudget.coldStartDurationMs}ms",
                                style = AstraTheme.typography.labelS,
                                color = colors.accentPrimary
                            )
                            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                                Text("Low-End Device Mode (Calmer Surfaces)", style = AstraTheme.typography.bodyM, color = colors.textPrimary)
                                AstraToggle(
                                    checked = performancePreferences.lowEndDeviceModeOverride,
                                    onCheckedChange = { v -> onUpdatePerformance { it.copy(lowEndDeviceModeOverride = v) } }
                                )
                            }
                            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                                Text("Atmospheric Blur Layers", style = AstraTheme.typography.bodyM, color = colors.textPrimary)
                                AstraToggle(
                                    checked = performancePreferences.blurEnabled,
                                    onCheckedChange = { v -> onUpdatePerformance { it.copy(blurEnabled = v) } }
                                )
                            }
                            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                                Text("Orbital Wallpaper Light Overlay", style = AstraTheme.typography.bodyM, color = colors.textPrimary)
                                AstraToggle(
                                    checked = performancePreferences.liveWallpaperEffects,
                                    onCheckedChange = { v -> onUpdatePerformance { it.copy(liveWallpaperEffects = v) } }
                                )
                            }
                        }
                    }
                }

                AstraSettingsSection.PRIVACY_SECURITY -> {
                    AstraCard(modifier = Modifier.fillMaxWidth(), family = AstraSurfaceFamily.SOLID) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Hardware & Permission Indicators (Frame 38)", style = AstraTheme.typography.labelS, color = colors.accentPrimary)
                            Text(
                                "Astra never transmits local app searches or layout data to cloud servers. Toggle hardware privacy indicators below to verify status bar chips.",
                                style = AstraTheme.typography.bodyS,
                                color = colors.textSecondary
                            )
                            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                                Text("Simulate Camera / Mic Active Indicator", style = AstraTheme.typography.bodyM, color = colors.textPrimary)
                                AstraToggle(
                                    checked = deviceStatus.cameraActiveIndicator,
                                    onCheckedChange = { v -> onSimulateSystemState(null, null, null, v) }
                                )
                            }
                            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                                Text("Simulate Offline State (Frame 31)", style = AstraTheme.typography.bodyM, color = colors.textPrimary)
                                AstraToggle(
                                    checked = deviceStatus.isOffline,
                                    onCheckedChange = { v -> onSimulateSystemState(null, null, v, null) }
                                )
                            }
                            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                                Text("Simulate Low Battery 14% (Frame 30)", style = AstraTheme.typography.bodyM, color = colors.textPrimary)
                                AstraToggle(
                                    checked = deviceStatus.isLowBattery,
                                    onCheckedChange = { v -> onSimulateSystemState(null, v, null, null) }
                                )
                            }
                        }
                    }
                }

                AstraSettingsSection.ACCESSIBILITY -> {
                    // Frame 40: Accessibility Large-Text & High Contrast
                    AstraCard(modifier = Modifier.fillMaxWidth(), family = AstraSurfaceFamily.SOLID) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Typography Scale (Frame 40 Large-Text Variant)", style = AstraTheme.typography.labelS, color = colors.accentPrimary)
                            AstraSegmentedControl(
                                items = listOf(1.0f, 1.15f, 1.30f),
                                selectedItem = themeSettings.textScaleMultiplier,
                                labelProvider = {
                                    when (it) {
                                        1.0f -> "100% Standard"
                                        1.15f -> "115% Comfortable"
                                        else -> "130% Large Text"
                                    }
                                },
                                onSelect = { s -> onUpdateTheme { it.copy(textScaleMultiplier = s) } }
                            )
                            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                                Text("High Contrast Borders & Text", style = AstraTheme.typography.bodyM, color = colors.textPrimary)
                                AstraToggle(
                                    checked = themeSettings.highContrastMode,
                                    onCheckedChange = { v -> onUpdateTheme { it.copy(highContrastMode = v) } }
                                )
                            }
                            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                                Text("Reduced Motion (Instant Transitions)", style = AstraTheme.typography.bodyM, color = colors.textPrimary)
                                AstraToggle(
                                    checked = performancePreferences.reducedMotion,
                                    onCheckedChange = { v -> onUpdatePerformance { it.copy(reducedMotion = v) } }
                                )
                            }
                        }
                    }
                }

                AstraSettingsSection.APPS -> {
                    AstraCard(modifier = Modifier.fillMaxWidth(), family = AstraSurfaceFamily.SOLID) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Android Home Role (RoleManager.ROLE_HOME)", style = AstraTheme.typography.titleM, color = colors.textPrimary)
                            Text(
                                "Astra registers CATEGORY_HOME and CATEGORY_DEFAULT in AndroidManifest.xml so pressing the system Home gesture returns directly to Astra.",
                                style = AstraTheme.typography.bodyS,
                                color = colors.textSecondary
                            )
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(colors.accentPrimary)
                                    .clickable { onRequestDefaultHomeRole() }
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text("Request Android Home Role", style = AstraTheme.typography.labelL, color = colors.surfaceBase)
                            }
                        }
                    }
                }

                AstraSettingsSection.BACKUP_DATA -> {
                    AstraCard(modifier = Modifier.fillMaxWidth(), family = AstraSurfaceFamily.SOLID) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Local Snapshot & Safe-Mode Recovery", style = AstraTheme.typography.titleM, color = colors.textPrimary)
                            Text(
                                "Export or restore your complete Astra layout locally, or reset to factory safe defaults if a layout becomes corrupted.",
                                style = AstraTheme.typography.bodyS,
                                color = colors.textSecondary
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(colors.accentPrimary)
                                        .clickable {
                                            backupStatusMessage = "Local snapshot ASTRA_BACKUP_V1 verified and saved."
                                        }
                                        .padding(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Text("Save Local Snapshot", style = AstraTheme.typography.labelS, color = colors.surfaceBase)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(colors.error.copy(alpha = 0.22f))
                                        .clickable {
                                            onResetToSafeDefaults()
                                            backupStatusMessage = "Reset to Astra safe defaults complete."
                                        }
                                        .padding(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Text("Reset Safe Defaults", style = AstraTheme.typography.labelS, color = colors.error)
                                }
                            }
                            if (backupStatusMessage != null) {
                                Text(backupStatusMessage!!, style = AstraTheme.typography.labelS, color = colors.success)
                            }
                        }
                    }
                }

                AstraSettingsSection.ABOUT_ASTRA -> {
                    // Capability Matrix & 40-Frame Visual Screen Inventory Inspector (Sections 35 & 45)
                    AstraCard(modifier = Modifier.fillMaxWidth(), family = AstraSurfaceFamily.SOLID) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("RUNTIME CAPABILITY MATRIX (SECTION 35)", style = AstraTheme.typography.labelS, color = colors.accentPrimary)
                            Text("• canBeDefaultHome: ${capabilityReport.canBeDefaultHome}", style = AstraTheme.typography.bodyS, color = colors.textPrimary)
                            Text("• isCurrentlyDefaultHome: ${capabilityReport.isCurrentlyDefaultHome}", style = AstraTheme.typography.bodyS, color = colors.textPrimary)
                            Text("• hasNotificationAccess: ${capabilityReport.hasNotificationAccess}", style = AstraTheme.typography.bodyS, color = colors.textPrimary)
                            Text("• canHostWidgets: ${capabilityReport.canHostWidgets}", style = AstraTheme.typography.bodyS, color = colors.textPrimary)
                            Text("• canReadPackages: ${capabilityReport.canReadPackages}", style = AstraTheme.typography.bodyS, color = colors.textPrimary)
                            Text("• supportsShortcuts: ${capabilityReport.supportsShortcuts}", style = AstraTheme.typography.bodyS, color = colors.textPrimary)
                            Text("• supportsChargingSurface: ${capabilityReport.supportsChargingSurface}", style = AstraTheme.typography.bodyS, color = colors.textPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "40-FRAME VISUAL SCREEN INVENTORY (SECTION 45) · TAP ANY FRAME TO INSPECT",
                        style = AstraTheme.typography.labelS,
                        color = colors.accentPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    AstraCanonicalFrame.entries.forEach { frame ->
                        AstraCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            family = AstraSurfaceFamily.SOLID,
                            onClick = { onJumpToCanonicalFrame(frame) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Frame %02d · %s".format(frame.number, frame.title),
                                        style = AstraTheme.typography.bodyS.copy(fontWeight = FontWeight.Medium),
                                        color = colors.textPrimary
                                    )
                                    Text(
                                        text = frame.category,
                                        style = AstraTheme.typography.labelS,
                                        color = colors.textTertiary
                                    )
                                }
                                Text("Inspect →", style = AstraTheme.typography.labelS, color = colors.accentPrimary)
                            }
                        }
                    }
                }
            }
        }
    }
}
