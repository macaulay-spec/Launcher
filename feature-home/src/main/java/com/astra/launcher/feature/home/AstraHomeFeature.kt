package com.astra.launcher.feature.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.astra.launcher.core.design.AstraAppIcon
import com.astra.launcher.core.design.AstraCard
import com.astra.launcher.core.design.AstraClock
import com.astra.launcher.core.design.AstraComponentState
import com.astra.launcher.core.design.AstraDock
import com.astra.launcher.core.design.AstraGlyph
import com.astra.launcher.core.design.AstraIconButton
import com.astra.launcher.core.design.AstraSegmentedControl
import com.astra.launcher.core.design.AstraSurfaceFamily
import com.astra.launcher.core.design.AstraTheme
import com.astra.launcher.core.design.AstraToggle
import com.astra.launcher.core.design.AstraVectorIcon
import com.astra.launcher.core.storage.AstraAppEntry
import com.astra.launcher.core.storage.AstraClockStyle
import com.astra.launcher.core.storage.AstraDeviceStatus
import com.astra.launcher.core.storage.AstraFolderItem
import com.astra.launcher.core.storage.AstraMediaState
import com.astra.launcher.core.storage.AstraThemePreset
import com.astra.launcher.core.storage.HomeLayout
import com.astra.launcher.core.storage.ThemeSettings
import com.astra.launcher.feature.widgets.AstraWidgetHostCard

/**
 * Non-Happy System Diagnostic State Mode (Section 28; Frames 30, 31, 32, 33, 34, 35).
 */
enum class SystemNonHappyState {
    NONE,
    LOADING_SKELETON,
    EMPTY_HOME,
    ERROR_STATE,
    RECOVERY_SAFE_MODE
}

/**
 * Astra Home Screen (Section 13, 40, 41, 42; Frames 04, 05, 06, 07).
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AstraHomeScreen(
    apps: List<AstraAppEntry>,
    themeSettings: ThemeSettings,
    homeLayout: HomeLayout,
    deviceStatus: AstraDeviceStatus,
    mediaState: AstraMediaState,
    isDefaultHome: Boolean,
    initialEditMode: Boolean = false,
    nonHappyState: SystemNonHappyState = SystemNonHappyState.NONE,
    onClearNonHappyState: () -> Unit = {},
    onLaunchApp: (AstraAppEntry) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenAppDrawer: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenControlCenter: () -> Unit,
    onOpenRecents: () -> Unit,
    onOpenPersonalization: () -> Unit,
    onOpenWidgetPicker: () -> Unit,
    onOpenLockScreen: () -> Unit,
    onOpenSettings: () -> Unit,
    onRequestDefaultHomeRole: () -> Unit,
    onApplyPreset: (AstraThemePreset) -> Unit,
    onUpdateTheme: ((ThemeSettings) -> ThemeSettings) -> Unit,
    onUpdateHome: ((HomeLayout) -> HomeLayout) -> Unit,
    onOpenAppInfo: (String) -> Unit,
    onUninstallApp: (String) -> Unit,
    onResetSafeMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AstraTheme.colors
    var currentPage by remember { mutableIntStateOf(0) }
    var isEditMode by remember(initialEditMode) { mutableStateOf(initialEditMode) }
    var activeFolder by remember { mutableStateOf<AstraFolderItem?>(null) }
    var selectedShortcutApp by remember { mutableStateOf<AstraAppEntry?>(null) }

    val pagePackages = if (currentPage == 0) homeLayout.pinnedAppsPage0 else homeLayout.pinnedAppsPage1
    val priorityApps = remember(apps, pagePackages, homeLayout.hiddenPackages) {
        pagePackages.mapNotNull { pkg ->
            apps.firstOrNull { it.packageName == pkg && it.packageName !in homeLayout.hiddenPackages }
        }.ifEmpty {
            apps.filterNot { it.packageName in homeLayout.hiddenPackages }.take(6)
        }
    }

    val dockApps = remember(apps, homeLayout.dockPackages) {
        homeLayout.dockPackages.mapNotNull { pkg ->
            apps.firstOrNull { it.packageName == pkg }
        }.ifEmpty {
            apps.take(5)
        }
    }

    val pageWidgets = remember(homeLayout.widgets, currentPage) {
        homeLayout.widgets.filter { it.pageIndex == currentPage }
    }

    val pageFolders = remember(homeLayout.folders, currentPage) {
        homeLayout.folders.filter { it.pageIndex == currentPage }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                var accumulatedDy = 0f
                detectVerticalDragGestures(
                    onDragStart = { accumulatedDy = 0f },
                    onVerticalDrag = { _, dragAmount -> accumulatedDy += dragAmount },
                    onDragEnd = {
                        if (accumulatedDy < -120f) {
                            onOpenAppDrawer()
                        } else if (accumulatedDy > 120f) {
                            onOpenNotifications()
                        }
                    }
                )
            }
            .combinedClickable(
                onClick = {},
                onLongClick = { isEditMode = !isEditMode }
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Upper Zone: System Banners + Adaptive Clock + Folders/Priority Apps + Widgets
            Column {
                // Default Home Role Prompt Banner (if not currently default Home)
                if (!isDefaultHome) {
                    AstraCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        family = AstraSurfaceFamily.SOFT_TRANSLUCENT,
                        onClick = onRequestDefaultHomeRole
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AstraVectorIcon(glyph = AstraGlyph.HOME, size = 16.dp, tint = colors.accentPrimary, active = true)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Set Astra as Default Android Home",
                                    style = AstraTheme.typography.bodyS.copy(fontWeight = FontWeight.Medium),
                                    color = colors.textPrimary
                                )
                            }
                            Text(
                                text = "ROLE_HOME →",
                                style = AstraTheme.typography.labelS,
                                color = colors.accentPrimary
                            )
                        }
                    }
                }

                // Offline State Banner (Section 28 & Frame 31)
                if (deviceStatus.isOffline) {
                    AstraCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        family = AstraSurfaceFamily.SOFT_TRANSLUCENT,
                        state = AstraComponentState.ACTIVE
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Astra is offline.",
                                style = AstraTheme.typography.bodyM.copy(fontWeight = FontWeight.SemiBold),
                                color = colors.warning
                            )
                            Text(
                                text = "App launching, local command search, widgets, notes, and personalization continue to work 100% locally.",
                                style = AstraTheme.typography.bodyS,
                                color = colors.textSecondary
                            )
                        }
                    }
                }

                // Low Battery Banner (Section 28 & Frame 30)
                if (deviceStatus.isLowBattery) {
                    AstraCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        family = AstraSurfaceFamily.SOFT_TRANSLUCENT
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
                                    text = "Low Battery (${deviceStatus.batteryPercent}%) · Calm Power Mode",
                                    style = AstraTheme.typography.bodyS.copy(fontWeight = FontWeight.SemiBold),
                                    color = colors.warning
                                )
                                Text(
                                    text = "Decorative animations and live wallpaper shaders reduced to preserve task completion.",
                                    style = AstraTheme.typography.labelS,
                                    color = colors.textSecondary
                                )
                            }
                            AstraVectorIcon(glyph = AstraGlyph.BATTERY, size = 18.dp, tint = colors.warning)
                        }
                    }
                }

                // Non-Happy Diagnostic States (Frames 32, 33, 34, 35)
                if (nonHappyState != SystemNonHappyState.NONE) {
                    AstraNonHappyStateCard(
                        state = nonHappyState,
                        onRecover = {
                            if (nonHappyState == SystemNonHappyState.RECOVERY_SAFE_MODE) {
                                onResetSafeMode()
                            }
                            onClearNonHappyState()
                        }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Upper-middle: Large Adaptive Clock/Weather/Date Composition (Section 13)
                AstraClock(
                    style = themeSettings.clockStyle,
                    upperLuminance = themeSettings.wallpaperId.upperRegionLuminance,
                    isLockScreen = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenLockScreen() }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Page Switcher & Edit Mode Trigger
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (p in 0 until homeLayout.pageCount) {
                            val active = p == currentPage
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(if (active) colors.accentPrimary.copy(alpha = 0.24f) else colors.surfaceGlass)
                                    .border(
                                        1.dp,
                                        if (active) colors.accentPrimary else colors.borderSubtle,
                                        CircleShape
                                    )
                                    .clickable { currentPage = p }
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (p == 0) "Space 01 · Primary" else "Space 02 · Focus",
                                    style = AstraTheme.typography.labelS,
                                    color = if (active) colors.accentPrimary else colors.textSecondary
                                )
                            }
                        }
                    }

                    Text(
                        text = if (isEditMode) "Done Editing" else "Edit Home",
                        style = AstraTheme.typography.labelS,
                        color = colors.accentPrimary,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(colors.surfaceGlass)
                            .clickable { isEditMode = !isEditMode }
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Frame 07: Home Edit Mode Live Control Panel (Section 40)
                if (isEditMode) {
                    AstraHomeEditorPanel(
                        themeSettings = themeSettings,
                        homeLayout = homeLayout,
                        onApplyPreset = onApplyPreset,
                        onUpdateTheme = onUpdateTheme,
                        onUpdateHome = onUpdateHome,
                        onOpenWidgetPicker = onOpenWidgetPicker,
                        onOpenPersonalization = onOpenPersonalization,
                        onDone = { isEditMode = false }
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Middle: 4–6 High-Priority App Shortcuts (Section 13)
                val cols = homeLayout.gridColumns.coerceIn(4, 5)
                priorityApps.chunked(cols).forEach { rowApps ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        for (i in 0 until cols) {
                            val app = rowApps.getOrNull(i)
                            if (app != null) {
                                AstraAppIcon(
                                    app = app,
                                    iconStyle = themeSettings.iconStyle,
                                    showLabel = themeSettings.showIconLabels,
                                    iconScale = themeSettings.iconScale,
                                    showNotificationDot = app.packageName.contains("messaging"),
                                    onClick = { onLaunchApp(app) },
                                    onLongClick = { selectedShortcutApp = app },
                                    modifier = Modifier.weight(1f)
                                )
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                // Folders Row (Section 41)
                if (pageFolders.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        pageFolders.forEach { folder ->
                            AstraCard(
                                modifier = Modifier.weight(1f),
                                family = AstraSurfaceFamily.SOFT_TRANSLUCENT,
                                onClick = { activeFolder = folder }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(colors.accentPrimary.copy(alpha = 0.18f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        AstraVectorIcon(
                                            glyph = AstraGlyph.FOLDER,
                                            size = 18.dp,
                                            tint = colors.accentPrimary,
                                            active = true
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = folder.title,
                                            style = AstraTheme.typography.bodyM.copy(fontWeight = FontWeight.SemiBold),
                                            color = colors.textPrimary,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = "${folder.packageNames.size} apps",
                                            style = AstraTheme.typography.labelS,
                                            color = colors.textTertiary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Lower-Middle: Intelligent Widget Stack (Section 13 & 20)
                if (pageWidgets.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        pageWidgets.take(2).forEach { placement ->
                            AstraWidgetHostCard(
                                placement = placement,
                                deviceStatus = deviceStatus,
                                mediaState = mediaState,
                                quickNoteText = homeLayout.quickNoteText,
                                isEditMode = isEditMode,
                                onQuickAction = { actionKey ->
                                    when (actionKey) {
                                        "search" -> onOpenSearch()
                                        "controls" -> onOpenControlCenter()
                                        "calendar", "clock", "weather", "battery", "focus", "note" -> onOpenWidgetPicker()
                                        else -> {}
                                    }
                                },
                                onRemoveWidget = {
                                    onUpdateHome { cur ->
                                        cur.copy(widgets = cur.widgets.filterNot { it.instanceId == placement.instanceId })
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bottom Zone: Global Search Trigger Pill + Quick Surface Bar + Adaptive 5-App Dock
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Global Search / Command Palette Trigger Pill
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(CircleShape)
                        .background(colors.surfaceGlassStrong)
                        .border(1.dp, colors.borderSubtle, CircleShape)
                        .clickable { onOpenSearch() }
                        .padding(horizontal = 16.dp, vertical = 11.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AstraVectorIcon(glyph = AstraGlyph.SEARCH, size = 18.dp, tint = colors.accentPrimary, active = true)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Search apps or run command ('open camera')...",
                            style = AstraTheme.typography.bodyS,
                            color = colors.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    AstraVectorIcon(glyph = AstraGlyph.APPS, size = 18.dp, tint = colors.iconSecondary)
                }

                // Continuum Surface Navigation Strip (Connects Lock, Apps, Notifications, Controls, Recents, Studio, Settings)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val quickPills = listOf(
                        Triple("Apps", AstraGlyph.APPS, onOpenAppDrawer),
                        Triple("Shade", AstraGlyph.NOTIFICATIONS, onOpenNotifications),
                        Triple("Controls", AstraGlyph.CONTROLS, onOpenControlCenter),
                        Triple("Recents", AstraGlyph.WIDGETS, onOpenRecents),
                        Triple("Studio", AstraGlyph.WALLPAPER, onOpenPersonalization),
                        Triple("Settings", AstraGlyph.SETTINGS, onOpenSettings)
                    )
                    quickPills.forEach { (label, glyph, action) ->
                        Column(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { action() }
                                .padding(horizontal = 6.dp, vertical = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            AstraVectorIcon(glyph = glyph, size = 16.dp, tint = colors.iconSecondary)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(label, style = AstraTheme.typography.labelS, color = colors.textTertiary)
                        }
                    }
                }

                // Adaptive Bottom Dock (Section 13)
                AstraDock(
                    apps = dockApps,
                    iconStyle = themeSettings.iconStyle,
                    glassEnabled = themeSettings.dockStyleGlass,
                    onAppClick = onLaunchApp,
                    onAppLongClick = { selectedShortcutApp = it }
                )
            }
        }

        // Folder Expansion Modal (Section 41)
        activeFolder?.let { folder ->
            AstraFolderModal(
                folder = folder,
                apps = apps,
                themeSettings = themeSettings,
                onLaunchApp = {
                    activeFolder = null
                    onLaunchApp(it)
                },
                onRenameFolder = { newTitle ->
                    val updated = folder.copy(title = newTitle)
                    activeFolder = updated
                    onUpdateHome { h ->
                        h.copy(folders = h.folders.map { if (it.folderId == folder.folderId) updated else it })
                    }
                },
                onRemoveFolder = {
                    activeFolder = null
                    onUpdateHome { h ->
                        h.copy(folders = h.folders.filterNot { it.folderId == folder.folderId })
                    }
                },
                onDismiss = { activeFolder = null }
            )
        }

        // Long-Press App Shortcut & Platform Action Popup (Section 42)
        selectedShortcutApp?.let { app ->
            AstraAppShortcutDialog(
                app = app,
                isHidden = app.packageName in homeLayout.hiddenPackages,
                onLaunchShortcut = {
                    selectedShortcutApp = null
                    onLaunchApp(app)
                },
                onToggleHidden = {
                    onUpdateHome { h ->
                        val nextHidden = if (app.packageName in h.hiddenPackages) {
                            h.hiddenPackages - app.packageName
                        } else {
                            h.hiddenPackages + app.packageName
                        }
                        h.copy(hiddenPackages = nextHidden)
                    }
                    selectedShortcutApp = null
                },
                onOpenAppInfo = {
                    selectedShortcutApp = null
                    onOpenAppInfo(app.packageName)
                },
                onUninstall = {
                    selectedShortcutApp = null
                    onUninstallApp(app.packageName)
                },
                onDismiss = { selectedShortcutApp = null }
            )
        }
    }
}

/**
 * Section 40 & Frame 07: Home Editor Panel.
 */
@Composable
fun AstraHomeEditorPanel(
    themeSettings: ThemeSettings,
    homeLayout: HomeLayout,
    onApplyPreset: (AstraThemePreset) -> Unit,
    onUpdateTheme: ((ThemeSettings) -> ThemeSettings) -> Unit,
    onUpdateHome: ((HomeLayout) -> HomeLayout) -> Unit,
    onOpenWidgetPicker: () -> Unit,
    onOpenPersonalization: () -> Unit,
    onDone: () -> Unit
) {
    val colors = AstraTheme.colors
    AstraCard(
        modifier = Modifier.fillMaxWidth(),
        family = AstraSurfaceFamily.SOFT_TRANSLUCENT,
        shape = AstraTheme.shapes.elevatedSheet,
        state = AstraComponentState.SELECTED
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("HOME EDITOR · SNAP GRID ACTIVE", style = AstraTheme.typography.labelS, color = colors.accentPrimary)
                    Text("Customize Layout & Atmosphere", style = AstraTheme.typography.titleM, color = colors.textPrimary)
                }
                AstraIconButton(
                    glyph = AstraGlyph.CHECK,
                    contentDescriptionLabel = "Finish Editing",
                    onClick = onDone,
                    state = AstraComponentState.ACTIVE,
                    size = 36.dp
                )
            }

            Text("ATMOSPHERE PRESET", style = AstraTheme.typography.labelS, color = colors.textTertiary)
            AstraSegmentedControl(
                items = AstraThemePreset.entries,
                selectedItem = themeSettings.themePreset,
                labelProvider = { it.displayName },
                onSelect = onApplyPreset
            )

            Text("CLOCK TYPOGRAPHY", style = AstraTheme.typography.labelS, color = colors.textTertiary)
            AstraSegmentedControl(
                items = AstraClockStyle.entries,
                selectedItem = themeSettings.clockStyle,
                labelProvider = { it.label },
                onSelect = { c -> onUpdateTheme { it.copy(clockStyle = c) } }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Grid Density (${homeLayout.gridColumns}×${homeLayout.gridRows})", style = AstraTheme.typography.bodyS, color = colors.textPrimary)
                AstraSegmentedControl(
                    items = listOf(4, 5),
                    selectedItem = homeLayout.gridColumns,
                    labelProvider = { "$it Col" },
                    onSelect = { cols -> onUpdateHome { it.copy(gridColumns = cols) } },
                    modifier = Modifier.width(150.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Show Icon Labels", style = AstraTheme.typography.bodyS, color = colors.textPrimary)
                AstraToggle(
                    checked = themeSettings.showIconLabels,
                    onCheckedChange = { v -> onUpdateTheme { it.copy(showIconLabels = v) } }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(CircleShape)
                        .background(colors.surfaceFloating)
                        .clickable { onOpenWidgetPicker() }
                        .padding(vertical = 9.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("+ Widgets", style = AstraTheme.typography.labelS, color = colors.accentPrimary)
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(CircleShape)
                        .background(colors.accentPrimary)
                        .clickable { onOpenPersonalization() }
                        .padding(vertical = 9.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Full Studio →", style = AstraTheme.typography.labelS, color = colors.surfaceBase)
                }
            }
        }
    }
}

/**
 * Section 41: Elevated Folder Surface.
 */
@Composable
fun AstraFolderModal(
    folder: AstraFolderItem,
    apps: List<AstraAppEntry>,
    themeSettings: ThemeSettings,
    onLaunchApp: (AstraAppEntry) -> Unit,
    onRenameFolder: (String) -> Unit,
    onRemoveFolder: () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = AstraTheme.colors
    var editableTitle by remember(folder.title) { mutableStateOf(folder.title) }
    val folderApps = remember(folder, apps) {
        folder.packageNames.mapNotNull { pkg -> apps.firstOrNull { it.packageName == pkg } }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.65f))
            .clickable { onDismiss() }
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        AstraCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = false) {},
            family = AstraSurfaceFamily.SOFT_TRANSLUCENT,
            shape = AstraTheme.shapes.heroPanel
        ) {
            Column(modifier = Modifier.padding(22.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("ASTRA FOLDER · TAP TITLE TO RENAME", style = AstraTheme.typography.labelS, color = colors.accentPrimary)
                        BasicTextField(
                            value = editableTitle,
                            onValueChange = {
                                editableTitle = it
                                onRenameFolder(it)
                            },
                            textStyle = AstraTheme.typography.headlineM.copy(color = colors.textPrimary),
                            cursorBrush = SolidColor(colors.accentPrimary)
                        )
                    }
                    AstraIconButton(
                        glyph = AstraGlyph.CLOSE,
                        contentDescriptionLabel = "Close Folder",
                        onClick = onDismiss
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                folderApps.chunked(4).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        for (i in 0 until 4) {
                            val item = row.getOrNull(i)
                            if (item != null) {
                                AstraAppIcon(
                                    app = item,
                                    iconStyle = themeSettings.iconStyle,
                                    onClick = { onLaunchApp(item) },
                                    modifier = Modifier.weight(1f)
                                )
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${folderApps.size} grouped applications",
                        style = AstraTheme.typography.labelS,
                        color = colors.textTertiary
                    )
                    Text(
                        text = "Dissolve Folder",
                        style = AstraTheme.typography.labelS,
                        color = colors.error,
                        modifier = Modifier.clickable { onRemoveFolder() }
                    )
                }
            }
        }
    }
}

/**
 * Section 42: App Shortcuts & Platform Actions Popup.
 */
@Composable
fun AstraAppShortcutDialog(
    app: AstraAppEntry,
    isHidden: Boolean,
    onLaunchShortcut: () -> Unit,
    onToggleHidden: () -> Unit,
    onOpenAppInfo: () -> Unit,
    onUninstall: () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = AstraTheme.colors

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.6f))
            .clickable { onDismiss() }
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        AstraCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = false) {},
            family = AstraSurfaceFamily.SOFT_TRANSLUCENT,
            shape = AstraTheme.shapes.elevatedSheet
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(app.category.label.uppercase(), style = AstraTheme.typography.labelS, color = colors.accentPrimary)
                        Text(app.label, style = AstraTheme.typography.titleL, color = colors.textPrimary)
                        Text(app.packageName, style = AstraTheme.typography.labelS, color = colors.textTertiary)
                    }
                    AstraIconButton(
                        glyph = AstraGlyph.CLOSE,
                        contentDescriptionLabel = "Close Menu",
                        onClick = onDismiss
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text("ANDROID APP SHORTCUTS", style = AstraTheme.typography.labelS, color = colors.textTertiary)
                Spacer(modifier = Modifier.height(6.dp))

                app.shortcuts.forEach { sc ->
                    AstraCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        family = AstraSurfaceFamily.CLEAR_ATMOSPHERIC,
                        onClick = onLaunchShortcut
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(sc.shortLabel, style = AstraTheme.typography.bodyM, color = colors.textPrimary)
                                Text(sc.longLabel, style = AstraTheme.typography.labelS, color = colors.textSecondary)
                            }
                            AstraVectorIcon(glyph = AstraGlyph.CHEVRON_RIGHT, size = 14.dp, tint = colors.accentPrimary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text("PLATFORM ACTIONS", style = AstraTheme.typography.labelS, color = colors.textTertiary)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(CircleShape)
                            .background(colors.surfaceFloating)
                            .clickable { onToggleHidden() }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isHidden) "Unhide" else "Hide App",
                            style = AstraTheme.typography.labelS,
                            color = colors.textPrimary
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(CircleShape)
                            .background(colors.surfaceFloating)
                            .clickable { onOpenAppInfo() }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("App Info", style = AstraTheme.typography.labelS, color = colors.accentPrimary)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(CircleShape)
                            .background(colors.error.copy(alpha = 0.2f))
                            .clickable { onUninstall() }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Uninstall", style = AstraTheme.typography.labelS, color = colors.error)
                    }
                }
            }
        }
    }
}

/**
 * Section 28 & Frames 32, 33, 34, 35: Non-Happy System Diagnostic Cards.
 */
@Composable
fun AstraNonHappyStateCard(
    state: SystemNonHappyState,
    onRecover: () -> Unit
) {
    val colors = AstraTheme.colors
    val (badge, title, body, actionText) = when (state) {
        SystemNonHappyState.LOADING_SKELETON -> listOf(
            "FRAME 32 · LOADING STATE",
            "Indexing Installed Packages & Shortcuts",
            "Local package scanner warming LRU icon cache without blocking Home interactions.",
            "Dismiss Shimmer"
        )
        SystemNonHappyState.EMPTY_HOME -> listOf(
            "FRAME 33 · EMPTY STATE",
            "Minimalist Blank Space",
            "This workspace page has no pinned widgets yet. Astra looks intentional even when empty.",
            "Restore Default Widgets"
        )
        SystemNonHappyState.ERROR_STATE -> listOf(
            "FRAME 34 · ERROR STATE",
            "Widget Provider Handshake Timeout",
            "A third-party AppWidget failed to respond within 400ms. Astra isolated the fault so Home remains responsive.",
            "Retry Widget Host"
        )
        SystemNonHappyState.RECOVERY_SAFE_MODE -> listOf(
            "FRAME 35 · CRASH / RECOVERY SAFE MODE",
            "Astra Shell Safe Mode Active",
            "Previous layout anomaly detected and isolated. You can restart the shell or reset layout to factory safe defaults without losing personal data.",
            "Reset Layout & Resume"
        )
        SystemNonHappyState.NONE -> return
    }

    AstraCard(
        modifier = Modifier.fillMaxWidth(),
        family = AstraSurfaceFamily.SOFT_TRANSLUCENT,
        state = if (state == SystemNonHappyState.ERROR_STATE || state == SystemNonHappyState.RECOVERY_SAFE_MODE) {
            AstraComponentState.ERROR
        } else {
            AstraComponentState.ACTIVE
        }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(badge, style = AstraTheme.typography.labelS, color = colors.accentPrimary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(title, style = AstraTheme.typography.titleM, color = colors.textPrimary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(body, style = AstraTheme.typography.bodyS, color = colors.textSecondary)
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(colors.accentPrimary)
                    .clickable { onRecover() }
                    .padding(horizontal = 14.dp, vertical = 7.dp)
            ) {
                Text(actionText, style = AstraTheme.typography.labelS, color = colors.surfaceBase)
            }
        }
    }
}
