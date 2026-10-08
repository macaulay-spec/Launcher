package com.astra.launcher.feature.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.astra.launcher.core.design.AstraAppIcon
import com.astra.launcher.core.design.AstraBrandMark
import com.astra.launcher.core.design.AstraCategoryChip
import com.astra.launcher.core.design.AstraClock
import com.astra.launcher.core.design.AstraDock
import com.astra.launcher.core.design.AstraPalette
import com.astra.launcher.core.design.AstraPermissionPromptCard
import com.astra.launcher.core.design.AstraSearchBar
import com.astra.launcher.core.design.AstraShapes
import com.astra.launcher.core.design.AstraSurfaceCard
import com.astra.launcher.core.design.AstraTypography
import com.astra.launcher.core.design.toDrawableResId
import com.astra.launcher.core.platform.AstraIconPipeline
import com.astra.launcher.core.platform.AstraWidgetHostManager
import com.astra.launcher.core.storage.AstraAppEntry
import com.astra.launcher.core.storage.AstraCapabilityReport
import com.astra.launcher.core.storage.AstraDeviceStatus
import com.astra.launcher.core.storage.AstraIconStyle
import com.astra.launcher.core.storage.AstraNotificationEntry
import com.astra.launcher.core.storage.AstraShortcutItem
import com.astra.launcher.core.storage.AstraThemePreset
import com.astra.launcher.core.storage.HomeDensityMode
import com.astra.launcher.core.storage.HomeLayout
import com.astra.launcher.core.storage.SwipeDownAction
import com.astra.launcher.core.storage.ThemeSettings
import com.astra.launcher.core.storage.WorkspaceCellItem
import com.astra.launcher.core.storage.WorkspaceItemType
import com.astra.launcher.feature.widgets.AstraBoundWidgetHostCell
import java.time.LocalTime
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Persistent 2D Launcher Workspace + Contextual Header + Dock (Rebuild Sections 3, 21, 28, 32).
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AstraWorkspaceLayer(
    installedApps: List<AstraAppEntry>,
    homeLayout: HomeLayout,
    themeSettings: ThemeSettings,
    capabilities: AstraCapabilityReport,
    deviceStatus: AstraDeviceStatus,
    activeNotifications: List<AstraNotificationEntry>,
    showDefaultHomeBanner: Boolean,
    corruptedWorkspaceRecovered: Boolean,
    lastLaunchError: String?,
    isEditMode: Boolean,
    movingWorkspaceItem: WorkspaceCellItem?,
    palette: AstraPalette,
    iconPipeline: AstraIconPipeline?,
    widgetHostManager: AstraWidgetHostManager,
    onLaunchApp: (AstraAppEntry) -> Unit,
    onAppLongPress: (AstraAppEntry, WorkspaceCellItem?) -> Unit,
    onOpenFolder: (WorkspaceCellItem) -> Unit,
    onOpenAppDrawer: () -> Unit,
    onSwipeDownTrigger: () -> Unit,
    onDoubleTapTrigger: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenPersonalization: () -> Unit,
    onOpenWidgetPicker: () -> Unit,
    onOpenNotificationAndControlSurface: () -> Unit,
    onToggleEditMode: (Boolean) -> Unit,
    onSelectItemForMove: (WorkspaceCellItem?) -> Unit,
    onMoveOrMergeItem: (itemId: String, targetPage: Int, targetCellX: Int, targetCellY: Int) -> Unit,
    onResizeWidget: (itemId: String, newSpanX: Int, newSpanY: Int) -> Unit,
    onRebindWidget: (WorkspaceCellItem) -> Unit,
    onRemoveWorkspaceItem: (String) -> Unit,
    onAddPage: () -> Unit,
    onRemoveLastPage: () -> Unit,
    onRequestDefaultHomeRole: () -> Unit,
    onDismissDefaultHomeBanner: () -> Unit,
    onAcknowledgeCorruptionRecovery: () -> Unit,
    onClearLaunchError: () -> Unit,
    onRescanPackages: () -> Unit,
    modifier: Modifier = Modifier
) {
    val appByComponent = remember(installedApps) {
        installedApps.associateBy { it.componentName }
    }
    val appByPackage = remember(installedApps) {
        installedApps.groupBy { it.packageName }.mapValues { it.value.first() }
    }

    val pageCount = homeLayout.pageCount.coerceIn(1, 8)
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { pageCount })

    var cumulativeVerticalDrag by remember { mutableFloatStateOf(0f) }

    val timeOfDayGreeting = remember {
        val hour = try {
            LocalTime.now().hour
        } catch (_: Throwable) {
            12
        }
        when (hour) {
            in 5..11 -> "Morning Focus"
            in 12..16 -> "Afternoon Workspace"
            in 17..21 -> "Evening Atmosphere"
            else -> "Night Mode"
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragStart = { cumulativeVerticalDrag = 0f },
                    onVerticalDrag = { _, dragAmount ->
                        cumulativeVerticalDrag += dragAmount
                    },
                    onDragEnd = {
                        if (cumulativeVerticalDrag < -68f) {
                            onOpenAppDrawer()
                        } else if (cumulativeVerticalDrag > 68f) {
                            onSwipeDownTrigger()
                        }
                        cumulativeVerticalDrag = 0f
                    }
                )
            }
    ) {
        if (showDefaultHomeBanner && !capabilities.isCurrentlyDefaultHome) {
            AstraPermissionPromptCard(
                title = "Make Astra Your Default Home",
                whyNeeded = "Set Astra as your Android Home application so pressing the Home button always returns to your Astra workspace.",
                whatHappensIfDenied = "You can set Astra as Default Home anytime in Astra Settings.",
                primaryButtonLabel = "Set Default Home",
                palette = palette,
                onGrantClick = onRequestDefaultHomeRole,
                onContinueWithoutClick = onDismissDefaultHomeBanner,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }

        if (!lastLaunchError.isNullOrBlank() || corruptedWorkspaceRecovered) {
            AstraSurfaceCard(
                palette = palette,
                useGlass = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = lastLaunchError
                            ?: "Workspace layout was safely restored to clean defaults.",
                        style = AstraTypography.Caption,
                        color = palette.warningTone,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "Dismiss",
                        style = AstraTypography.Caption.copy(fontWeight = FontWeight.Bold),
                        color = palette.primaryAccent,
                        modifier = Modifier
                            .padding(start = 10.dp)
                            .clickable {
                                onClearLaunchError()
                                onAcknowledgeCorruptionRecovery()
                            }
                    )
                }
            }
        }

        if (movingWorkspaceItem != null) {
            AstraSurfaceCard(
                palette = palette,
                useGlass = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Place \"${movingWorkspaceItem.label}\" · Tap any slot to move, or tap an app to group into a Folder",
                        style = AstraTypography.Caption,
                        color = palette.primaryAccent,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "Done",
                        style = AstraTypography.Caption.copy(fontWeight = FontWeight.Bold),
                        color = palette.primaryText,
                        modifier = Modifier
                            .padding(start = 10.dp)
                            .clickable { onSelectItemForMove(null) }
                    )
                }
            }
        }

        // Multi-Page 2D Coordinate Grid Workspace
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) { pageIndex ->
            val pageItems = remember(homeLayout.items, pageIndex) {
                homeLayout.items.filter { it.page == pageIndex }
            }

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .pointerInput(isEditMode, movingWorkspaceItem) {
                        detectTapGestures(
                            onDoubleTap = { onDoubleTapTrigger() },
                            onLongPress = { onToggleEditMode(!isEditMode) },
                            onTap = {
                                if (isEditMode && movingWorkspaceItem == null) {
                                    onToggleEditMode(false)
                                }
                            }
                        )
                    }
            ) {
                val cols = homeLayout.gridColumns.coerceIn(3, 6)
                val rows = homeLayout.gridRows.coerceIn(4, 7)
                val cellWidth = maxWidth / cols
                val cellHeight = maxHeight / rows
                val density = LocalDensity.current
                val cellWidthPx = with(density) { cellWidth.toPx() }
                val cellHeightPx = with(density) { cellHeight.toPx() }

                // Subtle spatial drop zones in Edit Mode (NO developer coordinate numbers!)
                if (isEditMode || movingWorkspaceItem != null) {
                    for (r in 0 until rows) {
                        for (c in 0 until cols) {
                            Box(
                                modifier = Modifier
                                    .offset(x = cellWidth * c, y = cellHeight * r)
                                    .size(width = cellWidth, height = cellHeight)
                                    .padding(4.dp)
                                    .clip(AstraShapes.CardMedium)
                                    .border(
                                        width = 1.dp,
                                        color = palette.primaryAccent.copy(alpha = 0.22f),
                                        shape = AstraShapes.CardMedium
                                    )
                                    .clickable {
                                        if (movingWorkspaceItem != null) {
                                            onMoveOrMergeItem(movingWorkspaceItem.id, pageIndex, c, r)
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(palette.primaryAccent.copy(alpha = 0.28f))
                                )
                            }
                        }
                    }
                }

                // Hero Adaptive Clock + Contextual Status Header on Page 0 (Rebuild Sections 3 & 21)
                val hasTopWidgetOnPage0 = pageIndex == 0 && pageItems.any { it.cellY == 0 }
                if (pageIndex == 0 && homeLayout.showClockOnWorkspace && !hasTopWidgetOnPage0) {
                    Column(
                        modifier = Modifier
                            .offset(x = 0.dp, y = 0.dp)
                            .size(width = maxWidth, height = cellHeight * 1.85f)
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.Top
                    ) {
                        AstraClock(
                            style = themeSettings.clockStyle,
                            palette = palette,
                            onClockClick = onOpenNotificationAndControlSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        // Contextual Status & Notification Pill
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                modifier = Modifier.clickable(onClick = onOpenNotificationAndControlSurface),
                                shape = AstraShapes.ChipPill,
                                color = palette.glassSurface,
                                border = BorderStroke(1.dp, palette.glassStroke)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(palette.primaryAccent)
                                    )
                                    val batteryText = if (deviceStatus.batteryPercent in 0..100) {
                                        " · ${deviceStatus.batteryPercent}%${if (deviceStatus.isCharging) " ⚡" else ""}"
                                    } else ""
                                    Text(
                                        text = "$timeOfDayGreeting$batteryText",
                                        style = AstraTypography.Caption,
                                        color = palette.primaryText
                                    )
                                }
                            }

                            if (activeNotifications.isNotEmpty()) {
                                Surface(
                                    modifier = Modifier.clickable(onClick = onOpenNotificationAndControlSurface),
                                    shape = AstraShapes.ChipPill,
                                    color = palette.primaryAccent.copy(alpha = 0.20f),
                                    border = BorderStroke(1.dp, palette.primaryAccent)
                                ) {
                                    Text(
                                        text = "${activeNotifications.size} active notification${if (activeNotifications.size == 1) "" else "s"}",
                                        style = AstraTypography.Caption.copy(fontWeight = FontWeight.SemiBold),
                                        color = palette.primaryAccent,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Render every persisted 2D WorkspaceCellItem at (cellX, cellY, spanX, spanY)
                pageItems.forEach { cellItem ->
                    var dragOffsetX by remember(cellItem.id) { mutableFloatStateOf(0f) }
                    var dragOffsetY by remember(cellItem.id) { mutableFloatStateOf(0f) }

                    Box(
                        modifier = Modifier
                            .offset(
                                x = cellWidth * cellItem.cellX,
                                y = cellHeight * cellItem.cellY
                            )
                            .offset {
                                IntOffset(dragOffsetX.roundToInt(), dragOffsetY.roundToInt())
                            }
                            .size(
                                width = cellWidth * cellItem.spanX,
                                height = cellHeight * cellItem.spanY
                            )
                            .pointerInput(cellItem.id, cellWidthPx, cellHeightPx) {
                                detectDragGesturesAfterLongPress(
                                    onDragStart = {
                                        dragOffsetX = 0f
                                        dragOffsetY = 0f
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        dragOffsetX += dragAmount.x
                                        dragOffsetY += dragAmount.y
                                    },
                                    onDragCancel = {
                                        dragOffsetX = 0f
                                        dragOffsetY = 0f
                                    },
                                    onDragEnd = {
                                        val deltaCellsX = (dragOffsetX / cellWidthPx.coerceAtLeast(1f)).roundToInt()
                                        val deltaCellsY = (dragOffsetY / cellHeightPx.coerceAtLeast(1f)).roundToInt()
                                        val targetX = (cellItem.cellX + deltaCellsX).coerceIn(0, cols - 1)
                                        val targetY = (cellItem.cellY + deltaCellsY).coerceIn(0, rows - 1)
                                        dragOffsetX = 0f
                                        dragOffsetY = 0f
                                        if (targetX != cellItem.cellX || targetY != cellItem.cellY) {
                                            onMoveOrMergeItem(cellItem.id, pageIndex, targetX, targetY)
                                        } else {
                                            val resolvedApp = appByComponent[cellItem.componentName]
                                                ?: appByPackage[cellItem.packageName]
                                            if (resolvedApp != null && cellItem.itemType == WorkspaceItemType.APP) {
                                                onAppLongPress(resolvedApp, cellItem)
                                            }
                                        }
                                    }
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        when (cellItem.itemType) {
                            WorkspaceItemType.APP, WorkspaceItemType.SHORTCUT -> {
                                val resolvedApp = appByComponent[cellItem.componentName]
                                    ?: appByPackage[cellItem.packageName]
                                    ?: AstraAppEntry(
                                        packageName = cellItem.packageName,
                                        componentName = cellItem.componentName,
                                        activityClassName = cellItem.componentName.substringAfter('/', ""),
                                        label = cellItem.label,
                                        userSerial = cellItem.userSerial
                                    )

                                AstraAppIcon(
                                    app = resolvedApp,
                                    iconStyle = themeSettings.iconStyle,
                                    palette = palette,
                                    iconPipeline = iconPipeline,
                                    showLabel = themeSettings.showIconLabels,
                                    iconScale = themeSettings.iconScale,
                                    onClick = {
                                        if (movingWorkspaceItem != null && movingWorkspaceItem.id != cellItem.id) {
                                            onMoveOrMergeItem(
                                                movingWorkspaceItem.id,
                                                pageIndex,
                                                cellItem.cellX,
                                                cellItem.cellY
                                            )
                                        } else {
                                            onLaunchApp(resolvedApp)
                                        }
                                    },
                                    onLongClick = {
                                        onAppLongPress(resolvedApp, cellItem)
                                    }
                                )
                            }

                            WorkspaceItemType.FOLDER -> {
                                AstraWorkspaceFolderCell(
                                    folderItem = cellItem,
                                    appByComponent = appByComponent,
                                    iconStyle = themeSettings.iconStyle,
                                    showLabel = themeSettings.showIconLabels,
                                    palette = palette,
                                    iconPipeline = iconPipeline,
                                    onClick = {
                                        if (movingWorkspaceItem != null && movingWorkspaceItem.id != cellItem.id) {
                                            onMoveOrMergeItem(
                                                movingWorkspaceItem.id,
                                                pageIndex,
                                                cellItem.cellX,
                                                cellItem.cellY
                                            )
                                        } else {
                                            onOpenFolder(cellItem)
                                        }
                                    }
                                )
                            }

                            WorkspaceItemType.WIDGET -> {
                                AstraBoundWidgetHostCell(
                                    item = cellItem,
                                    cellWidthDp = cellWidth.value.toInt(),
                                    cellHeightDp = cellHeight.value.toInt(),
                                    isEditMode = isEditMode,
                                    palette = palette,
                                    widgetHostManager = widgetHostManager,
                                    onResizeWidget = { sx, sy -> onResizeWidget(cellItem.id, sx, sy) },
                                    onRebindRequest = onRebindWidget,
                                    onRemoveWidget = { onRemoveWorkspaceItem(cellItem.id) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Home Edit Mode Control Bar (Rebuild Section 28)
        if (isEditMode) {
            AstraEditModeBar(
                pageCount = pageCount,
                palette = palette,
                onOpenWidgets = onOpenWidgetPicker,
                onOpenWallpapers = onOpenPersonalization,
                onOpenSettings = onOpenSettings,
                onAddPage = onAddPage,
                onRemovePage = onRemoveLastPage,
                onExitEditMode = { onToggleEditMode(false) }
            )
        }

        // Page Indicator Dots
        if (homeLayout.showPageIndicator && pageCount > 1) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(pageCount) { idx ->
                    val active = pagerState.currentPage == idx
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(width = if (active) 18.dp else 6.dp, height = 6.dp)
                            .clip(CircleShape)
                            .background(if (active) palette.primaryAccent else palette.mutedText.copy(alpha = 0.45f))
                    )
                }
            }
        }

        // Universal Search Pill
        AstraSearchBar(
            palette = palette,
            placeholder = "Search apps, shortcuts, settings…",
            onClick = onOpenSearch,
            onSettingsClick = onOpenSettings,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        // Persistent Configurable Home Dock (Rebuild Section 10)
        if (homeLayout.dockEnabled) {
            val dockApps = remember(homeLayout.dockItems, appByComponent, appByPackage) {
                homeLayout.dockItems.map { slot ->
                    appByComponent[slot.componentName]
                        ?: appByPackage[slot.packageName]
                        ?: AstraAppEntry(
                            packageName = slot.packageName,
                            componentName = slot.componentName,
                            activityClassName = slot.componentName.substringAfter('/', ""),
                            label = slot.label,
                            userSerial = slot.userSerial
                        )
                }
            }

            AstraDock(
                dockApps = dockApps,
                iconStyle = themeSettings.iconStyle,
                palette = palette,
                iconPipeline = iconPipeline,
                useGlass = themeSettings.dockStyleGlass,
                showLabels = themeSettings.showDockLabels,
                onAppClick = onLaunchApp,
                onAppLongClick = { app -> onAppLongPress(app, null) },
                onOpenDrawerClick = onOpenAppDrawer
            )
        }
    }
}

/**
 * First-Run Setup Experience (`AstraFirstRunSetupOverlay` — Rebuild Sections 2 & 32).
 *
 * "ASTRA — Your phone, redesigned."
 * 6-step visual phone setup flow:
 * Step 1: Choose your Astra atmosphere (ORBIT, NOCTURNE, HORIZON)
 * Step 2: Choose Home layout (Minimal, Balanced, Dense)
 * Step 3: Choose icon treatment (Astra Adaptive, Original, Monochrome)
 * Step 4: Choose navigation/gesture behavior
 * Step 5: Optional integrations (Notifications, Widgets, Shortcuts)
 * Step 6: Set Astra as default Home application -> "Welcome to Astra."
 */
@Composable
fun AstraFirstRunSetupOverlay(
    initialPreset: AstraThemePreset,
    initialDensity: HomeDensityMode,
    initialIconStyle: AstraIconStyle,
    initialSwipeDown: SwipeDownAction,
    capabilities: AstraCapabilityReport,
    palette: AstraPalette,
    onRequestDefaultHomeRole: () -> Unit,
    onOpenNotificationAccessSettings: () -> Unit,
    onCompleteSetup: (
        preset: AstraThemePreset,
        density: HomeDensityMode,
        iconStyle: AstraIconStyle,
        swipeDown: SwipeDownAction
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentStep by remember { mutableIntStateOf(1) }
    var selectedPreset by remember { mutableStateOf(initialPreset) }
    var selectedDensity by remember { mutableStateOf(initialDensity) }
    var selectedIconStyle by remember { mutableStateOf(initialIconStyle) }
    var selectedSwipeDown by remember { mutableStateOf(initialSwipeDown) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(palette.obsidian0.copy(alpha = 0.94f))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 22.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Brand Header
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AstraBrandMark(
                            accentColor = palette.primaryAccent,
                            secondaryColor = palette.secondaryAccent,
                            size = 34.dp,
                            luminousMoment = true
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "ASTRA",
                                style = AstraTypography.TitleL.copy(letterSpacing = 2.sp),
                                color = palette.primaryText
                            )
                            Text(
                                text = "Your phone, redesigned.",
                                style = AstraTypography.Caption,
                                color = palette.primaryAccent
                            )
                        }
                    }
                    Text(
                        text = "Step $currentStep of 6",
                        style = AstraTypography.Caption.copy(fontWeight = FontWeight.SemiBold),
                        color = palette.secondaryText
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Live Preview Card showing selected Atmosphere + Clock + Layout
                AstraSurfaceCard(
                    palette = palette,
                    useGlass = true,
                    shape = AstraShapes.CardLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(165.dp)
                ) {
                    Image(
                        painter = painterResource(id = selectedPreset.defaultWallpaper.toDrawableResId()),
                        contentDescription = selectedPreset.displayName,
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
                            style = themeSettingsPreviewClock(selectedDensity),
                            palette = palette,
                            modifier = Modifier.align(Alignment.TopStart)
                        )
                        Text(
                            text = "${selectedPreset.displayName} · ${selectedDensity.title} Layout · ${selectedIconStyle.label} Icons",
                            style = AstraTypography.Caption.copy(fontWeight = FontWeight.SemiBold),
                            color = palette.primaryAccent,
                            modifier = Modifier.align(Alignment.BottomStart)
                        )
                    }
                }
            }

            // Step Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                when (currentStep) {
                    1 -> {
                        Text(
                            text = "1. Choose your Astra atmosphere",
                            style = AstraTypography.DisplayM,
                            color = palette.primaryText
                        )
                        Text(
                            text = "Select the spatial wallpaper and tonal palette for your Home environment.",
                            style = AstraTypography.BodyM,
                            color = palette.secondaryText
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(vertical = 6.dp)
                        ) {
                            items(AstraThemePreset.entries, key = { it.id }) { preset ->
                                val isSelected = selectedPreset == preset
                                AstraSurfaceCard(
                                    palette = palette,
                                    useGlass = isSelected,
                                    onClick = { selectedPreset = preset },
                                    modifier = Modifier
                                        .width(155.dp)
                                        .let { mod ->
                                            if (isSelected) mod.border(2.dp, palette.primaryAccent, AstraShapes.CardMedium)
                                            else mod
                                        }
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(
                                            text = preset.displayName.uppercase(Locale.getDefault()),
                                            style = AstraTypography.SectionHeader,
                                            color = if (isSelected) palette.primaryAccent else palette.primaryText
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = preset.subtitle,
                                            style = AstraTypography.Caption,
                                            color = palette.secondaryText,
                                            maxLines = 3
                                        )
                                    }
                                }
                            }
                        }
                    }

                    2 -> {
                        Text(
                            text = "2. Choose Home layout",
                            style = AstraTypography.DisplayM,
                            color = palette.primaryText
                        )
                        Text(
                            text = "How much breathing room vs application density do you prefer on Home?",
                            style = AstraTypography.BodyM,
                            color = palette.secondaryText
                        )
                        HomeDensityMode.entries.forEach { mode ->
                            val isSelected = selectedDensity == mode
                            AstraSurfaceCard(
                                palette = palette,
                                useGlass = isSelected,
                                onClick = { selectedDensity = mode },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .let { mod ->
                                        if (isSelected) mod.border(1.5.dp, palette.primaryAccent, AstraShapes.CardMedium)
                                        else mod
                                    }
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "${mode.title} (${mode.defaultColumns}×${mode.defaultRows})",
                                        style = AstraTypography.SectionHeader,
                                        color = if (isSelected) palette.primaryAccent else palette.primaryText
                                    )
                                    Text(
                                        text = mode.description,
                                        style = AstraTypography.Caption,
                                        color = palette.secondaryText
                                    )
                                }
                            }
                        }
                    }

                    3 -> {
                        Text(
                            text = "3. Choose icon treatment",
                            style = AstraTypography.DisplayM,
                            color = palette.primaryText
                        )
                        Text(
                            text = "Astra always uses your real installed application icons while harmonizing their visual presentation.",
                            style = AstraTypography.BodyM,
                            color = palette.secondaryText
                        )
                        AstraIconStyle.entries.forEach { style ->
                            val isSelected = selectedIconStyle == style
                            AstraSurfaceCard(
                                palette = palette,
                                useGlass = isSelected,
                                onClick = { selectedIconStyle = style },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .let { mod ->
                                        if (isSelected) mod.border(1.5.dp, palette.primaryAccent, AstraShapes.CardMedium)
                                        else mod
                                    }
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = style.label,
                                        style = AstraTypography.SectionHeader,
                                        color = if (isSelected) palette.primaryAccent else palette.primaryText
                                    )
                                    Text(
                                        text = style.description,
                                        style = AstraTypography.Caption,
                                        color = palette.secondaryText
                                    )
                                }
                            }
                        }
                    }

                    4 -> {
                        Text(
                            text = "4. Navigation & gestures",
                            style = AstraTypography.DisplayM,
                            color = palette.primaryText
                        )
                        Text(
                            text = "Choose what happens when you swipe down anywhere on the Home workspace.",
                            style = AstraTypography.BodyM,
                            color = palette.secondaryText
                        )
                        SwipeDownAction.entries.forEach { action ->
                            val isSelected = selectedSwipeDown == action
                            AstraSurfaceCard(
                                palette = palette,
                                useGlass = isSelected,
                                onClick = { selectedSwipeDown = action },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .let { mod ->
                                        if (isSelected) mod.border(1.5.dp, palette.primaryAccent, AstraShapes.CardMedium)
                                        else mod
                                    }
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = action.label,
                                        style = AstraTypography.SectionHeader,
                                        color = if (isSelected) palette.primaryAccent else palette.primaryText
                                    )
                                }
                            }
                        }
                    }

                    5 -> {
                        Text(
                            text = "5. Optional integrations",
                            style = AstraTypography.DisplayM,
                            color = palette.primaryText
                        )
                        Text(
                            text = "Enable optional Android integrations now or skip and configure them later.",
                            style = AstraTypography.BodyM,
                            color = palette.secondaryText
                        )
                        AstraSurfaceCard(
                            palette = palette,
                            useGlass = false,
                            onClick = onOpenNotificationAccessSettings,
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
                                        text = "Notification Access (Optional)",
                                        style = AstraTypography.SectionHeader,
                                        color = palette.primaryText
                                    )
                                    Text(
                                        text = "Lets Astra display real notification badges and organize notifications on Home.",
                                        style = AstraTypography.Caption,
                                        color = palette.secondaryText
                                    )
                                }
                                Text(
                                    text = if (capabilities.hasNotificationAccess) "Enabled ✓" else "Configure ↗",
                                    style = AstraTypography.Caption.copy(fontWeight = FontWeight.Bold),
                                    color = palette.primaryAccent
                                )
                            }
                        }
                    }

                    else -> {
                        Text(
                            text = "6. Set Astra as Default Home",
                            style = AstraTypography.DisplayM,
                            color = palette.primaryText
                        )
                        Text(
                            text = "Select Astra as your default Android Home application so pressing Home always returns to your new Astra environment.",
                            style = AstraTypography.BodyM,
                            color = palette.secondaryText
                        )
                        AstraSurfaceCard(
                            palette = palette,
                            useGlass = true,
                            onClick = onRequestDefaultHomeRole,
                            modifier = Modifier.fillMaxWidth()
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
                                        text = if (capabilities.isCurrentlyDefaultHome)
                                            "Astra is your Default Home App ✓"
                                        else
                                            "Select Astra as Default Home",
                                        style = AstraTypography.SectionHeader,
                                        color = if (capabilities.isCurrentlyDefaultHome) palette.successTone else palette.primaryAccent
                                    )
                                    Text(
                                        text = "Uses Android RoleManager (ROLE_HOME) / Default Home Settings",
                                        style = AstraTypography.Caption,
                                        color = palette.secondaryText
                                    )
                                }
                                Text(
                                    text = "Choose ↗",
                                    style = AstraTypography.Caption.copy(fontWeight = FontWeight.Bold),
                                    color = palette.primaryAccent
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Navigation Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentStep > 1) {
                    Surface(
                        modifier = Modifier.clickable { currentStep -= 1 },
                        shape = AstraShapes.ChipPill,
                        color = palette.elevatedSurface,
                        border = BorderStroke(1.dp, palette.hairlineBorder)
                    ) {
                        Text(
                            text = "← Back",
                            style = AstraTypography.BodyM,
                            color = palette.primaryText,
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp)
                        )
                    }
                } else {
                    Surface(
                        modifier = Modifier.clickable {
                            onCompleteSetup(selectedPreset, selectedDensity, selectedIconStyle, selectedSwipeDown)
                        },
                        shape = AstraShapes.ChipPill,
                        color = palette.elevatedSurface
                    ) {
                        Text(
                            text = "Quick Start →",
                            style = AstraTypography.Caption,
                            color = palette.secondaryText,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                        )
                    }
                }

                Surface(
                    modifier = Modifier.clickable {
                        if (currentStep < 6) {
                            currentStep += 1
                        } else {
                            onCompleteSetup(selectedPreset, selectedDensity, selectedIconStyle, selectedSwipeDown)
                        }
                    },
                    shape = AstraShapes.ChipPill,
                    color = palette.primaryAccent
                ) {
                    Text(
                        text = if (currentStep < 6) "Continue →" else "Welcome to Astra ✓",
                        style = AstraTypography.BodyM.copy(fontWeight = FontWeight.SemiBold),
                        color = palette.obsidian0,
                        modifier = Modifier.padding(horizontal = 22.dp, vertical = 11.dp)
                    )
                }
            }
        }
    }
}

private fun themeSettingsPreviewClock(density: HomeDensityMode) = when (density) {
    HomeDensityMode.MINIMAL -> com.astra.launcher.core.storage.AstraClockStyle.EDITORIAL_STACKED
    HomeDensityMode.BALANCED -> com.astra.launcher.core.storage.AstraClockStyle.MINIMAL_NUMERAL
    HomeDensityMode.DENSE -> com.astra.launcher.core.storage.AstraClockStyle.ORBITAL_COMPACT
}

/**
 * Truthful Astra Notification & Control Surface (`AstraNotificationAndControlOverlay` — Rebuild Sections 15 & 16).
 *
 * - Never fakes Wi-Fi or Bluetooth toggle states; shows real device connectivity/battery status and
 *   opens the official Android system panel when tapped.
 * - Uses real notifications from `AstraNotificationListenerService` when enabled, or explains how to grant
 *   Notification Access when disabled.
 */
@Composable
fun AstraNotificationAndControlOverlay(
    deviceStatus: AstraDeviceStatus,
    capabilities: AstraCapabilityReport,
    notifications: List<AstraNotificationEntry>,
    palette: AstraPalette,
    onOpenWifiPanel: () -> Unit,
    onOpenBluetoothSettings: () -> Unit,
    onOpenDisplaySettings: () -> Unit,
    onOpenSoundSettings: () -> Unit,
    onExpandAndroidNotificationShade: () -> Unit,
    onExpandAndroidQuickSettings: () -> Unit,
    onOpenNotificationAccessSettings: () -> Unit,
    onClearNotifications: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(palette.scrimOverlay)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Control & Notifications",
                            style = AstraTypography.TitleL,
                            color = palette.primaryText
                        )
                        Text(
                            text = "Real device state · Honest Android system handoffs",
                            style = AstraTypography.Caption,
                            color = palette.secondaryText
                        )
                    }
                    Surface(
                        modifier = Modifier.clickable(onClick = onClose),
                        shape = AstraShapes.ChipPill,
                        color = palette.elevatedSurface,
                        border = BorderStroke(1.dp, palette.hairlineBorder)
                    ) {
                        Text(
                            text = "Close",
                            style = AstraTypography.Caption.copy(fontWeight = FontWeight.SemiBold),
                            color = palette.primaryAccent,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Real Device Status & System Control Cards (Rebuild Section 15)
            item {
                AstraSurfaceCard(
                    palette = palette,
                    useGlass = true,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "SYSTEM CONTROLS (OPENS ANDROID SYSTEM PANELS)",
                            style = AstraTypography.Caption.copy(fontWeight = FontWeight.SemiBold),
                            color = palette.secondaryText
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ControlTile(
                                title = "Internet & Wi-Fi",
                                status = when {
                                    deviceStatus.isOffline -> "Offline"
                                    deviceStatus.isWifiConnected -> "Wi-Fi Connected"
                                    else -> "Connected"
                                },
                                palette = palette,
                                onClick = onOpenWifiPanel,
                                modifier = Modifier.weight(1f)
                            )
                            ControlTile(
                                title = "Bluetooth",
                                status = "System Panel ↗",
                                palette = palette,
                                onClick = onOpenBluetoothSettings,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ControlTile(
                                title = "Display",
                                status = "Brightness & Dark Mode ↗",
                                palette = palette,
                                onClick = onOpenDisplaySettings,
                                modifier = Modifier.weight(1f)
                            )
                            ControlTile(
                                title = "Sound & Audio",
                                status = "Volume & DND ↗",
                                palette = palette,
                                onClick = onOpenSoundSettings,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable(onClick = onExpandAndroidNotificationShade),
                                shape = AstraShapes.ChipPill,
                                color = palette.elevatedSurface,
                                border = BorderStroke(1.dp, palette.hairlineBorder)
                            ) {
                                Text(
                                    text = "System Shade ↓",
                                    style = AstraTypography.Caption.copy(fontWeight = FontWeight.SemiBold),
                                    color = palette.primaryText,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable(onClick = onExpandAndroidQuickSettings),
                                shape = AstraShapes.ChipPill,
                                color = palette.elevatedSurface,
                                border = BorderStroke(1.dp, palette.hairlineBorder)
                            ) {
                                Text(
                                    text = "Quick Settings ↓",
                                    style = AstraTypography.Caption.copy(fontWeight = FontWeight.SemiBold),
                                    color = palette.primaryText,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Real Notification Stream (Rebuild Section 16)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "NOTIFICATIONS (${notifications.size})",
                        style = AstraTypography.Caption.copy(fontWeight = FontWeight.SemiBold),
                        color = palette.secondaryText
                    )
                    if (notifications.isNotEmpty()) {
                        Text(
                            text = "Clear All",
                            style = AstraTypography.Caption.copy(fontWeight = FontWeight.SemiBold),
                            color = palette.primaryAccent,
                            modifier = Modifier.clickable(onClick = onClearNotifications)
                        )
                    }
                }
            }

            if (!capabilities.hasNotificationAccess) {
                item {
                    AstraSurfaceCard(
                        palette = palette,
                        useGlass = false,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Notification Access Not Enabled",
                                style = AstraTypography.SectionHeader,
                                color = palette.primaryText
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "This lets Astra organize your real notifications and show notification badges on app icons. Astra never displays fake notifications.",
                                style = AstraTypography.BodyM,
                                color = palette.secondaryText
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                modifier = Modifier.clickable(onClick = onOpenNotificationAccessSettings),
                                shape = AstraShapes.ChipPill,
                                color = palette.primaryAccent
                            ) {
                                Text(
                                    text = "Grant Notification Access ↗",
                                    style = AstraTypography.Caption.copy(fontWeight = FontWeight.SemiBold),
                                    color = palette.obsidian0,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            } else if (notifications.isEmpty()) {
                item {
                    AstraSurfaceCard(
                        palette = palette,
                        useGlass = false,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "All Caught Up",
                                style = AstraTypography.SectionHeader,
                                color = palette.primaryText
                            )
                            Text(
                                text = "No active notifications reported by Android.",
                                style = AstraTypography.Caption,
                                color = palette.secondaryText
                            )
                        }
                    }
                }
            } else {
                items(notifications, key = { it.id }) { notif ->
                    AstraSurfaceCard(
                        palette = palette,
                        useGlass = false,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = notif.appName,
                                    style = AstraTypography.Caption.copy(fontWeight = FontWeight.SemiBold),
                                    color = palette.primaryAccent
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = notif.title,
                                style = AstraTypography.SectionHeader,
                                color = palette.primaryText
                            )
                            if (notif.content.isNotBlank()) {
                                Text(
                                    text = notif.content,
                                    style = AstraTypography.BodyM,
                                    color = palette.secondaryText
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ControlTile(
    title: String,
    status: String,
    palette: AstraPalette,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = AstraShapes.CardMedium,
        color = palette.elevatedSurface,
        border = BorderStroke(1.dp, palette.hairlineBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                style = AstraTypography.SectionHeader,
                color = palette.primaryText
            )
            Text(
                text = status,
                style = AstraTypography.Caption,
                color = palette.primaryAccent
            )
        }
    }
}

@Composable
fun AstraWorkspaceFolderCell(
    folderItem: WorkspaceCellItem,
    appByComponent: Map<String, AstraAppEntry>,
    iconStyle: AstraIconStyle,
    showLabel: Boolean,
    palette: AstraPalette,
    iconPipeline: AstraIconPipeline?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .semantics { contentDescription = "Open Folder ${folderItem.label}" }
            .padding(vertical = 4.dp, horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(AstraShapes.IconSquircle)
                .background(palette.glassSurface)
                .border(1.dp, palette.glassStroke, AstraShapes.IconSquircle)
                .padding(6.dp),
            contentAlignment = Alignment.Center
        ) {
            val previewMembers = folderItem.folderItems.take(4)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                for (row in 0..1) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        for (col in 0..1) {
                            val idx = row * 2 + col
                            val member = previewMembers.getOrNull(idx)
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(palette.elevatedSurface),
                                contentAlignment = Alignment.Center
                            ) {
                                if (member != null) {
                                    val resolved = appByComponent[member.componentName]
                                    val cached = if (resolved != null && iconPipeline != null) {
                                        iconPipeline.getCachedIcon(resolved, iconStyle)
                                    } else null
                                    if (cached != null) {
                                        Image(
                                            bitmap = cached.asImageBitmap(),
                                            contentDescription = member.label,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Text(
                                            text = member.label.take(1).uppercase(Locale.getDefault()),
                                            style = AstraTypography.Caption.copy(fontSize = 8.sp),
                                            color = palette.primaryAccent
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (showLabel) {
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = folderItem.label,
                style = AstraTypography.IconLabel,
                color = palette.primaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AstraFolderOverlay(
    folderItem: WorkspaceCellItem,
    installedApps: List<AstraAppEntry>,
    iconStyle: AstraIconStyle,
    palette: AstraPalette,
    iconPipeline: AstraIconPipeline?,
    onLaunchApp: (AstraAppEntry) -> Unit,
    onRenameFolder: (String) -> Unit,
    onRemoveAppFromFolder: (String) -> Unit,
    onReorderFolderItem: (fromIdx: Int, toIdx: Int) -> Unit,
    onAddAppToFolder: (AstraAppEntry) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var folderTitle by remember(folderItem.id, folderItem.label) { mutableStateOf(folderItem.label) }
    var showAddAppsPicker by remember { mutableStateOf(false) }
    val appByComponent = remember(installedApps) { installedApps.associateBy { it.componentName } }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(palette.scrimOverlay)
            .clickable(onClick = onClose),
        contentAlignment = Alignment.Center
    ) {
        AstraSurfaceCard(
            palette = palette,
            useGlass = true,
            shape = AstraShapes.CardLarge,
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .clickable(enabled = false) {}
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BasicTextField(
                        value = folderTitle,
                        onValueChange = {
                            folderTitle = it
                            onRenameFolder(it)
                        },
                        singleLine = true,
                        textStyle = AstraTypography.TitleL.copy(color = palette.primaryText),
                        cursorBrush = SolidColor(palette.primaryAccent),
                        modifier = Modifier.weight(1f)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(
                            modifier = Modifier.clickable { showAddAppsPicker = !showAddAppsPicker },
                            shape = AstraShapes.ChipPill,
                            color = palette.primaryAccent.copy(alpha = 0.20f),
                            border = BorderStroke(1.dp, palette.primaryAccent)
                        ) {
                            Text(
                                text = if (showAddAppsPicker) "Done" else "+ Add App",
                                style = AstraTypography.Caption.copy(fontWeight = FontWeight.SemiBold),
                                color = palette.primaryAccent,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                        Surface(
                            modifier = Modifier.clickable(onClick = onClose),
                            shape = AstraShapes.ChipPill,
                            color = palette.elevatedSurface
                        ) {
                            Text(
                                text = "Close",
                                style = AstraTypography.Caption,
                                color = palette.primaryText,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    maxItemsInEachRow = 4
                ) {
                    folderItem.folderItems.forEachIndexed { idx, member ->
                        val resolved = appByComponent[member.componentName] ?: AstraAppEntry(
                            packageName = member.packageName,
                            componentName = member.componentName,
                            activityClassName = member.componentName.substringAfter('/', ""),
                            label = member.label,
                            userSerial = member.userSerial
                        )
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.width(68.dp)
                        ) {
                            AstraAppIcon(
                                app = resolved,
                                iconStyle = iconStyle,
                                palette = palette,
                                iconPipeline = iconPipeline,
                                onClick = { onLaunchApp(resolved) }
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                if (idx > 0) {
                                    Text(
                                        text = "←",
                                        style = AstraTypography.Caption,
                                        color = palette.secondaryText,
                                        modifier = Modifier.clickable { onReorderFolderItem(idx, idx - 1) }
                                    )
                                }
                                Text(
                                    text = "Remove",
                                    style = AstraTypography.Caption.copy(fontSize = 10.sp),
                                    color = palette.warningTone,
                                    modifier = Modifier.clickable { onRemoveAppFromFolder(member.componentName) }
                                )
                            }
                        }
                    }
                }

                if (showAddAppsPicker) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "ADD APP TO FOLDER",
                        style = AstraTypography.Caption.copy(fontWeight = FontWeight.SemiBold),
                        color = palette.secondaryText
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val existingSet = folderItem.folderItems.map { it.componentName }.toSet()
                    val candidates = installedApps.filterNot { it.componentName in existingSet }.take(8)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        candidates.forEach { candidate ->
                            Surface(
                                modifier = Modifier.clickable { onAddAppToFolder(candidate) },
                                shape = AstraShapes.ChipPill,
                                color = palette.elevatedSurface,
                                border = BorderStroke(1.dp, palette.hairlineBorder)
                            ) {
                                Text(
                                    text = "+ ${candidate.label}",
                                    style = AstraTypography.Caption,
                                    color = palette.primaryText,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Long-Press Application Menu (`AstraAppContextMenuSheet` — Rebuild Section 29).
 * Actions: Open, Favorite/Unfavorite, Add/Remove from Home, Add to Dock, Move/Folder,
 * Real Shortcuts, Hide/Unhide, App Info, Uninstall.
 */
@Composable
fun AstraAppContextMenuSheet(
    app: AstraAppEntry,
    workspaceItem: WorkspaceCellItem?,
    isHidden: Boolean,
    isFavorite: Boolean,
    palette: AstraPalette,
    onOpenApp: () -> Unit,
    onToggleFavorite: () -> Unit,
    onLaunchShortcut: (AstraShortcutItem) -> Unit,
    onPinToWorkspace: () -> Unit,
    onStartMoveOnWorkspace: (WorkspaceCellItem) -> Unit,
    onPinToDockSlot0: () -> Unit,
    onRemoveFromWorkspace: (String) -> Unit,
    onToggleHideApp: () -> Unit,
    onOpenAppInfo: () -> Unit,
    onUninstallApp: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(palette.scrimOverlay)
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.BottomCenter
    ) {
        AstraSurfaceCard(
            palette = palette,
            useGlass = true,
            shape = AstraShapes.ModalSheet,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clickable(enabled = false) {}
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = app.label,
                            style = AstraTypography.TitleL,
                            color = palette.primaryText
                        )
                        Text(
                            text = app.category.label,
                            style = AstraTypography.Caption,
                            color = palette.secondaryText
                        )
                    }
                    Surface(
                        modifier = Modifier.clickable {
                            onOpenApp()
                            onDismiss()
                        },
                        shape = AstraShapes.ChipPill,
                        color = palette.primaryAccent
                    ) {
                        Text(
                            text = "Open App",
                            style = AstraTypography.Caption.copy(fontWeight = FontWeight.SemiBold),
                            color = palette.obsidian0,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (app.shortcuts.isNotEmpty()) {
                    Text(
                        text = "SHORTCUTS",
                        style = AstraTypography.Caption.copy(fontWeight = FontWeight.SemiBold),
                        color = palette.primaryAccent
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    app.shortcuts.forEach { shortcut ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .clickable {
                                    onLaunchShortcut(shortcut)
                                    onDismiss()
                                },
                            shape = AstraShapes.CardMedium,
                            color = palette.elevatedSurface,
                            border = BorderStroke(1.dp, palette.hairlineBorder)
                        ) {
                            Text(
                                text = shortcut.longLabel.ifBlank { shortcut.shortLabel },
                                style = AstraTypography.BodyM,
                                color = palette.primaryText,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ContextMenuButton(
                        label = if (isFavorite) "★ Remove from Favorites" else "☆ Add to Favorites",
                        palette = palette,
                        accent = !isFavorite,
                        onClick = {
                            onToggleFavorite()
                            onDismiss()
                        }
                    )

                    if (workspaceItem != null) {
                        ContextMenuButton(
                            label = "Move on Home / Group into Folder",
                            palette = palette,
                            accent = true,
                            onClick = {
                                onStartMoveOnWorkspace(workspaceItem)
                                onDismiss()
                            }
                        )
                        ContextMenuButton(
                            label = "Remove from Home",
                            palette = palette,
                            onClick = {
                                onRemoveFromWorkspace(workspaceItem.id)
                                onDismiss()
                            }
                        )
                    } else {
                        ContextMenuButton(
                            label = "Add to Home Screen",
                            palette = palette,
                            accent = true,
                            onClick = {
                                onPinToWorkspace()
                                onDismiss()
                            }
                        )
                    }

                    ContextMenuButton(
                        label = "Add to Dock",
                        palette = palette,
                        onClick = {
                            onPinToDockSlot0()
                            onDismiss()
                        }
                    )

                    ContextMenuButton(
                        label = if (isHidden) "Unhide Application" else "Hide Application",
                        palette = palette,
                        onClick = {
                            onToggleHideApp()
                            onDismiss()
                        }
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(modifier = Modifier.weight(1f)) {
                            ContextMenuButton(
                                label = "App Info ↗",
                                palette = palette,
                                onClick = {
                                    onOpenAppInfo()
                                    onDismiss()
                                }
                            )
                        }
                        if (!app.isSystemApp) {
                            Box(modifier = Modifier.weight(1f)) {
                                ContextMenuButton(
                                    label = "Uninstall ↗",
                                    palette = palette,
                                    onClick = {
                                        onUninstallApp()
                                        onDismiss()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ContextMenuButton(
    label: String,
    palette: AstraPalette,
    accent: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = AstraShapes.CardMedium,
        color = if (accent) palette.primaryAccent.copy(alpha = 0.20f) else palette.elevatedSurface,
        border = BorderStroke(1.dp, if (accent) palette.primaryAccent else palette.hairlineBorder)
    ) {
        Text(
            text = label,
            style = AstraTypography.BodyM.copy(fontWeight = if (accent) FontWeight.SemiBold else FontWeight.Normal),
            color = if (accent) palette.primaryAccent else palette.primaryText,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
        )
    }
}

/**
 * Long-Press Home Edit Mode Bar (Rebuild Section 28: Wallpapers, Widgets, Layout, Icons, Settings).
 */
@Composable
fun AstraEditModeBar(
    pageCount: Int,
    palette: AstraPalette,
    onOpenWidgets: () -> Unit,
    onOpenWallpapers: () -> Unit,
    onOpenSettings: () -> Unit,
    onAddPage: () -> Unit,
    onRemovePage: () -> Unit,
    onExitEditMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    AstraSurfaceCard(
        palette = palette,
        useGlass = true,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "HOME CUSTOMIZATION ($pageCount pages)",
                    style = AstraTypography.Caption.copy(fontWeight = FontWeight.Bold),
                    color = palette.primaryAccent
                )
                Text(
                    text = "Done ✓",
                    style = AstraTypography.Caption.copy(fontWeight = FontWeight.Bold),
                    color = palette.primaryText,
                    modifier = Modifier.clickable(onClick = onExitEditMode)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                EditActionPill("Wallpapers", palette, onOpenWallpapers, Modifier.weight(1f))
                EditActionPill("Widgets", palette, onOpenWidgets, Modifier.weight(1f))
                EditActionPill("Layout & Icons", palette, onOpenSettings, Modifier.weight(1.2f))
                EditActionPill("+ Page", palette, onAddPage)
                if (pageCount > 1) {
                    EditActionPill("- Page", palette, onRemovePage)
                }
            }
        }
    }
}

@Composable
private fun EditActionPill(
    label: String,
    palette: AstraPalette,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = AstraShapes.ChipPill,
        color = palette.elevatedSurface,
        border = BorderStroke(1.dp, palette.hairlineBorder)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = AstraTypography.Caption.copy(fontWeight = FontWeight.SemiBold),
                color = palette.primaryText,
                maxLines = 1
            )
        }
    }
}
