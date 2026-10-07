package com.astra.launcher.feature.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.astra.launcher.core.design.AstraAppIcon
import com.astra.launcher.core.design.AstraClock
import com.astra.launcher.core.design.AstraDock
import com.astra.launcher.core.design.AstraPalette
import com.astra.launcher.core.design.AstraPermissionPromptCard
import com.astra.launcher.core.design.AstraSearchBar
import com.astra.launcher.core.design.AstraShapes
import com.astra.launcher.core.design.AstraSurfaceCard
import com.astra.launcher.core.design.AstraTypography
import com.astra.launcher.core.platform.AstraIconPipeline
import com.astra.launcher.core.platform.AstraWidgetHostManager
import com.astra.launcher.core.storage.AstraAppEntry
import com.astra.launcher.core.storage.AstraCapabilityReport
import com.astra.launcher.core.storage.AstraIconStyle
import com.astra.launcher.core.storage.AstraShortcutItem
import com.astra.launcher.core.storage.HomeLayout
import com.astra.launcher.core.storage.ThemeSettings
import com.astra.launcher.core.storage.WorkspaceCellItem
import com.astra.launcher.core.storage.WorkspaceItemType
import com.astra.launcher.feature.widgets.AstraBoundWidgetHostCell
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Persistent 2D Launcher Workspace + Dock + PageIndicator (Sections 4, 13, 14, 15, 19, 29, 34).
 * This is the true root workspace of Astra Launcher — never replaced by a fake-OS screen switch.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AstraWorkspaceLayer(
    installedApps: List<AstraAppEntry>,
    homeLayout: HomeLayout,
    themeSettings: ThemeSettings,
    capabilities: AstraCapabilityReport,
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
    onOpenSearch: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenPersonalization: () -> Unit,
    onOpenWidgetPicker: () -> Unit,
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
                        if (cumulativeVerticalDrag < -72f) {
                            onOpenAppDrawer()
                        } else if (cumulativeVerticalDrag > 72f) {
                            onSwipeDownTrigger()
                        }
                        cumulativeVerticalDrag = 0f
                    }
                )
            }
    ) {
        // Optional non-blocking banner if Astra is not yet Default Home (Section 3)
        if (showDefaultHomeBanner && !capabilities.isCurrentlyDefaultHome) {
            AstraPermissionPromptCard(
                title = "Set Astra as Default Home App",
                whyNeeded = "Setting Astra as your Android Home role (ROLE_HOME) ensures pressing the Home button always returns to your persistent Astra workspace.",
                whatHappensIfDenied = "You can continue using Astra and set it as Default Home later in Settings.",
                primaryButtonLabel = "Set Default Home",
                palette = palette,
                onGrantClick = onRequestDefaultHomeRole,
                onContinueWithoutClick = onDismissDefaultHomeBanner,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }

        // Honest Launch Failure / Corruption Recovery Banner (Section 8 & 31)
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
                            ?: "Corrupted workspace state was safely recovered to clean defaults.",
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

        // Move / Merge Guidance Banner when moving an item across 2D cells
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
                        text = "Moving \"${movingWorkspaceItem.label}\" · Tap any empty cell to move, or tap another app to create/join a Folder",
                        style = AstraTypography.Caption,
                        color = palette.primaryAccent,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "Cancel",
                        style = AstraTypography.Caption.copy(fontWeight = FontWeight.Bold),
                        color = palette.primaryText,
                        modifier = Modifier
                            .padding(start = 10.dp)
                            .clickable { onSelectItemForMove(null) }
                    )
                }
            }
        }

        // Multi-Page 2D Coordinate Grid Workspace (Section 4)
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
                    .padding(horizontal = 14.dp, vertical = 6.dp)
                    .combinedClickable(
                        onClick = {
                            if (isEditMode && movingWorkspaceItem == null) {
                                onToggleEditMode(false)
                            }
                        },
                        onLongClick = {
                            onToggleEditMode(!isEditMode)
                        }
                    )
            ) {
                val cols = homeLayout.gridColumns.coerceIn(3, 6)
                val rows = homeLayout.gridRows.coerceIn(4, 7)
                val cellWidth = maxWidth / cols
                val cellHeight = maxHeight / rows
                val density = LocalDensity.current
                val cellWidthPx = with(density) { cellWidth.toPx() }
                val cellHeightPx = with(density) { cellHeight.toPx() }

                // 1. Render subtle 2D grid target cells when in Edit Mode or moving an item
                if (isEditMode || movingWorkspaceItem != null) {
                    for (r in 0 until rows) {
                        for (c in 0 until cols) {
                            Box(
                                modifier = Modifier
                                    .offset(x = cellWidth * c, y = cellHeight * r)
                                    .size(width = cellWidth, height = cellHeight)
                                    .padding(3.dp)
                                    .clip(AstraShapes.CardMedium)
                                    .border(
                                        width = 1.dp,
                                        color = palette.primaryAccent.copy(alpha = 0.25f),
                                        shape = AstraShapes.CardMedium
                                    )
                                    .clickable {
                                        if (movingWorkspaceItem != null) {
                                            onMoveOrMergeItem(movingWorkspaceItem.id, pageIndex, c, r)
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$c,$r",
                                    style = AstraTypography.Caption.copy(fontSize = 9.sp),
                                    color = palette.mutedText.copy(alpha = 0.45f)
                                )
                            }
                        }
                    }
                }

                // 2. Live System Clock & Date Header in Top Safe Zone of Page 0 (Section 19)
                val hasTopWidgetOnPage0 = pageIndex == 0 && pageItems.any { it.cellY == 0 }
                if (pageIndex == 0 && homeLayout.showClockOnWorkspace && !hasTopWidgetOnPage0) {
                    Box(
                        modifier = Modifier
                            .offset(x = 0.dp, y = 0.dp)
                            .size(width = maxWidth, height = cellHeight * 1.45f)
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        AstraClock(
                            style = themeSettings.clockStyle,
                            palette = palette
                        )
                    }
                }

                // 3. Empty Workspace Recovery Card if 0 apps are installed on device
                if (installedApps.isEmpty() && pageItems.isEmpty() && pageIndex == 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        AstraSurfaceCard(
                            palette = palette,
                            useGlass = true,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Workspace Ready · Scanning Installed Apps",
                                    style = AstraTypography.SectionHeader,
                                    color = palette.primaryText
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Tap Rescan Packages or open the App Library to pin real installed apps to your 2D workspace.",
                                    style = AstraTypography.Caption,
                                    color = palette.secondaryText
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Surface(
                                    modifier = Modifier.clickable(onClick = onRescanPackages),
                                    shape = AstraShapes.ChipPill,
                                    color = palette.primaryAccent
                                ) {
                                    Text(
                                        text = "Rescan Installed Packages",
                                        style = AstraTypography.Caption.copy(fontWeight = FontWeight.SemiBold),
                                        color = palette.obsidian0,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // 4. Render every persisted 2D WorkspaceCellItem at (cellX, cellY, spanX, spanY)
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
                                            // Long press without drag opens context menu or edit mode
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

        // Edit Mode Control Strip (Section 29)
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
        if (pageCount > 1) {
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

        // Home Search Pill
        AstraSearchBar(
            palette = palette,
            onClick = onOpenSearch,
            onSettingsClick = onOpenSettings,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        // Persistent Configurable Home Dock (Section 15)
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

/**
 * Folder Cell on the 2D Workspace (Section 14).
 * Shows a 2×2 preview of the real application icons inside the folder.
 */
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

/**
 * Folder Overlay Sheet (Section 14).
 * Supports launching apps, renaming the folder, reordering items, adding apps, and removing apps
 * (automatically deleting the folder when empty).
 */
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
                                text = if (showAddAppsPicker) "Done Adding" else "+ Add App",
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
                        text = "TAP AN INSTALLED APP TO ADD TO FOLDER",
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
 * Long-Press App Context Menu Sheet (Section 9).
 * Displays REAL Android `ShortcutInfo` shortcuts from `LauncherApps`, plus Pin to Home,
 * Move/Folder merge, Pin to Dock, Hide/Unhide, App Info, and Uninstall.
 */
@Composable
fun AstraAppContextMenuSheet(
    app: AstraAppEntry,
    workspaceItem: WorkspaceCellItem?,
    isHidden: Boolean,
    palette: AstraPalette,
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
                Text(
                    text = app.label,
                    style = AstraTypography.TitleL,
                    color = palette.primaryText
                )
                Text(
                    text = app.componentName,
                    style = AstraTypography.Caption,
                    color = palette.secondaryText
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Real Android App Shortcuts (LauncherApps.ShortcutQuery)
                if (app.shortcuts.isNotEmpty()) {
                    Text(
                        text = "ANDROID APP SHORTCUTS",
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

                // Workspace & Platform Actions
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (workspaceItem != null) {
                        ContextMenuButton(
                            label = "Move on 2D Grid / Drop onto App to Create Folder",
                            palette = palette,
                            accent = true,
                            onClick = {
                                onStartMoveOnWorkspace(workspaceItem)
                                onDismiss()
                            }
                        )
                        ContextMenuButton(
                            label = "Remove from Home Workspace",
                            palette = palette,
                            onClick = {
                                onRemoveFromWorkspace(workspaceItem.id)
                                onDismiss()
                            }
                        )
                    } else {
                        ContextMenuButton(
                            label = "Add to Home Workspace",
                            palette = palette,
                            accent = true,
                            onClick = {
                                onPinToWorkspace()
                                onDismiss()
                            }
                        )
                    }

                    ContextMenuButton(
                        label = "Pin to Home Dock",
                        palette = palette,
                        onClick = {
                            onPinToDockSlot0()
                            onDismiss()
                        }
                    )

                    ContextMenuButton(
                        label = if (isHidden) "Unhide Application" else "Hide from App Drawer & Search",
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
 * Long-Press Workspace Edit Mode Control Bar (Section 29).
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
                    text = "WORKSPACE EDIT MODE ($pageCount pages)",
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
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                EditActionPill("Widgets", palette, onOpenWidgets, Modifier.weight(1f))
                EditActionPill("Wallpaper", palette, onOpenWallpapers, Modifier.weight(1f))
                EditActionPill("Settings", palette, onOpenSettings, Modifier.weight(1f))
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
