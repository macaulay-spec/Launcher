package com.astra.launcher.core.storage

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

/**
 * Local-First 2D Launcher Workspace, First-Run Setup & Personalization Repository.
 * Zero cloud requirement, zero advertising, 100% local persistence across reboot and process death.
 */
class AstraStorageRepository(context: Context? = null) {

    private val prefs: SharedPreferences? = context?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val inMemoryStore = mutableMapOf<String, String>()

    private val _corruptedWorkspaceRecovered = MutableStateFlow(false)
    val corruptedWorkspaceRecovered: StateFlow<Boolean> = _corruptedWorkspaceRecovered.asStateFlow()

    private val _themeSettings = MutableStateFlow(loadThemeSettings())
    val themeSettings: StateFlow<ThemeSettings> = _themeSettings.asStateFlow()

    private val _homeLayout = MutableStateFlow(loadHomeLayout())
    val homeLayout: StateFlow<HomeLayout> = _homeLayout.asStateFlow()

    private val _searchPreferences = MutableStateFlow(loadSearchPreferences())
    val searchPreferences: StateFlow<SearchPreferences> = _searchPreferences.asStateFlow()

    private val _notificationPreferences = MutableStateFlow(loadNotificationPreferences())
    val notificationPreferences: StateFlow<NotificationPreferences> = _notificationPreferences.asStateFlow()

    private val _gesturePreferences = MutableStateFlow(loadGesturePreferences())
    val gesturePreferences: StateFlow<GesturePreferences> = _gesturePreferences.asStateFlow()

    private val _performancePreferences = MutableStateFlow(loadPerformancePreferences())
    val performancePreferences: StateFlow<PerformancePreferences> = _performancePreferences.asStateFlow()

    private val usageCounts = mutableMapOf<String, Int>()
    private val lastUsedMap = mutableMapOf<String, Long>()

    init {
        loadUsageStats()
    }

    private fun getString(key: String, default: String): String {
        return prefs?.getString(key, default) ?: inMemoryStore[key] ?: default
    }

    private fun putString(key: String, value: String) {
        inMemoryStore[key] = value
        prefs?.edit()?.putString(key, value)?.apply()
    }

    fun getUsageScore(componentName: String): Int = usageCounts[componentName] ?: 0
    fun getLastUsedTimestamp(componentName: String): Long = lastUsedMap[componentName] ?: 0L

    fun recordAppLaunch(componentName: String, timestampMillis: Long = System.currentTimeMillis()) {
        usageCounts[componentName] = (usageCounts[componentName] ?: 0) + 1
        lastUsedMap[componentName] = timestampMillis
        saveUsageStats()
    }

    fun acknowledgeCorruptionRecovery() {
        _corruptedWorkspaceRecovered.value = false
    }

    /**
     * Completes the First-Run Setup flow (Rebuild Section 2 & 32) and populates a polished Home
     * matching the user's selected `HomeDensityMode` (`MINIMAL`, `BALANCED`, or `DENSE`).
     */
    fun completeFirstRunSetup(
        preset: AstraThemePreset,
        densityMode: HomeDensityMode,
        iconStyle: AstraIconStyle,
        swipeDownAction: SwipeDownAction,
        installedApps: List<AstraAppEntry>
    ) {
        updateThemeSettings { cur ->
            cur.copy(
                themePreset = preset,
                wallpaperId = preset.defaultWallpaper,
                themeMode = if (preset.isLightDefault) AstraThemeMode.LIGHT else AstraThemeMode.DARK,
                customAccentHex = preset.primaryAccentHex,
                iconStyle = iconStyle
            )
        }
        updateGesturePreferences { cur ->
            cur.copy(swipeDownAction = swipeDownAction)
        }
        updateHomeLayout { cur ->
            cur.copy(
                hasCompletedFirstRunSetup = true,
                isInitialized = false,
                densityMode = densityMode,
                gridColumns = densityMode.defaultColumns,
                gridRows = densityMode.defaultRows,
                items = emptyList(),
                dockItems = emptyList()
            )
        }
        populateInitialWorkspaceFromInstalledApps(installedApps)
    }

    fun reopenFirstRunSetup() {
        updateHomeLayout { it.copy(hasCompletedFirstRunSetup = false) }
    }

    /**
     * Populates the initial 2D workspace, favorites, and dock strictly from REAL installed applications
     * discovered on the user's device, tailored to the chosen `HomeDensityMode` (Minimal / Balanced / Dense).
     * Never injects fake applications.
     */
    fun populateInitialWorkspaceFromInstalledApps(installedApps: List<AstraAppEntry>) {
        if (installedApps.isEmpty()) return
        val current = _homeLayout.value
        if (current.isInitialized && (current.items.isNotEmpty() || current.dockItems.isNotEmpty())) {
            return
        }

        val dockCandidates = selectPreferredRealAppsForDock(installedApps, current.dockSlotCount)
        val dockItems = dockCandidates.mapIndexed { idx, app ->
            DockSlotItem(
                slotIndex = idx,
                packageName = app.packageName,
                componentName = app.componentName,
                userSerial = app.userSerial,
                label = app.label
            )
        }

        val dockComponents = dockCandidates.map { it.componentName }.toSet()
        val maxInitialApps = current.densityMode.initialWorkspaceCount
        val workspaceCandidates = installedApps
            .filterNot { it.componentName in dockComponents }
            .take(maxInitialApps)

        val cols = current.gridColumns.coerceAtLeast(3)
        // Place curated apps in the lower ergonomic zone of Page 0 (rows 2..4)
        // so rows 0..1 remain open for the spatial clock, contextual summary, and wallpaper breathing room!
        val startRow = if (current.densityMode == HomeDensityMode.MINIMAL) 3 else 2
        val workspaceItems = workspaceCandidates.mapIndexed { index, app ->
            val cellX = index % cols
            val cellY = (startRow + (index / cols)).coerceAtMost(current.gridRows - 1)
            WorkspaceCellItem(
                id = "item_${UUID.randomUUID()}",
                page = 0,
                cellX = cellX,
                cellY = cellY,
                spanX = 1,
                spanY = 1,
                itemType = WorkspaceItemType.APP,
                packageName = app.packageName,
                componentName = app.componentName,
                userSerial = app.userSerial,
                label = app.label
            )
        }

        val initialFavorites = (dockCandidates.take(4) + workspaceCandidates.take(4))
            .map { it.componentName }
            .toSet()

        updateHomeLayout { layout ->
            layout.copy(
                isInitialized = true,
                items = workspaceItems,
                dockItems = dockItems,
                favoriteComponents = layout.favoriteComponents.ifEmpty { initialFavorites }
            )
        }
    }

    private fun selectPreferredRealAppsForDock(
        installed: List<AstraAppEntry>,
        maxSlots: Int
    ): List<AstraAppEntry> {
        val chosen = mutableListOf<AstraAppEntry>()
        val roleMatchers: List<(AstraAppEntry) -> Boolean> = listOf(
            { it.packageName.contains("dialer", true) || it.packageName.contains("telecom", true) || it.label.equals("Phone", true) },
            { it.packageName.contains("messaging", true) || it.packageName.contains("mms", true) || it.packageName.contains("whatsapp", true) || it.label.contains("Message", true) },
            { it.packageName.contains("chrome", true) || it.packageName.contains("browser", true) || it.packageName.contains("firefox", true) },
            { it.packageName.contains("camera", true) || it.label.contains("Camera", true) },
            { it.packageName.contains("settings", true) || it.label.equals("Settings", true) }
        )

        for (matcher in roleMatchers) {
            if (chosen.size >= maxSlots) break
            val match = installed.firstOrNull { app -> app !in chosen && matcher(app) }
            if (match != null) chosen.add(match)
        }

        for (app in installed) {
            if (chosen.size >= maxSlots) break
            if (app !in chosen) chosen.add(app)
        }
        return chosen
    }

    fun toggleFavoriteApp(componentName: String) {
        updateHomeLayout { cur ->
            val next = if (componentName in cur.favoriteComponents) {
                cur.favoriteComponents - componentName
            } else {
                cur.favoriteComponents + componentName
            }
            cur.copy(favoriteComponents = next)
        }
    }

    /**
     * Reconciles workspace items, folders, and dock when a package is uninstalled or disabled.
     */
    fun reconcileWithInstalledPackages(installedApps: List<AstraAppEntry>) {
        if (installedApps.isEmpty()) return
        val validPackages = installedApps.map { it.packageName }.toSet()
        val labelByComponent = installedApps.associate { it.componentName to it.label }

        updateHomeLayout { current ->
            val updatedItems = current.items.mapNotNull { item ->
                when (item.itemType) {
                    WorkspaceItemType.APP, WorkspaceItemType.SHORTCUT -> {
                        if (item.packageName in validPackages) {
                            val updatedLabel = labelByComponent[item.componentName] ?: item.label
                            item.copy(label = updatedLabel)
                        } else null
                    }
                    WorkspaceItemType.FOLDER -> {
                        val remaining = item.folderItems.filter { it.packageName in validPackages }
                        if (remaining.isEmpty()) {
                            null
                        } else {
                            item.copy(folderItems = remaining)
                        }
                    }
                    WorkspaceItemType.WIDGET -> {
                        val pkg = item.widgetProvider.substringBefore('/')
                        if (pkg.isBlank() || pkg in validPackages) item else null
                    }
                }
            }

            val updatedDock = current.dockItems.filter { it.packageName in validPackages }
            current.copy(
                items = updatedItems,
                dockItems = updatedDock
            )
        }
    }

    fun isGridRegionFree(
        page: Int,
        cellX: Int,
        cellY: Int,
        spanX: Int = 1,
        spanY: Int = 1,
        ignoreItemId: String? = null
    ): Boolean {
        val layout = _homeLayout.value
        if (page < 0 || page >= layout.pageCount) return false
        if (cellX < 0 || cellY < 0) return false
        if (cellX + spanX > layout.gridColumns || cellY + spanY > layout.gridRows) return false

        val pageItems = layout.items.filter { it.page == page && it.id != ignoreItemId }
        for (existing in pageItems) {
            val overlapX = cellX < existing.cellX + existing.spanX && cellX + spanX > existing.cellX
            val overlapY = cellY < existing.cellY + existing.spanY && cellY + spanY > existing.cellY
            if (overlapX && overlapY) return false
        }
        return true
    }

    fun findFirstFreeCell(page: Int, spanX: Int = 1, spanY: Int = 1): Pair<Int, Int>? {
        val layout = _homeLayout.value
        val startRow = if (page == 0 && layout.showClockOnWorkspace) 2 else 0
        for (y in startRow..(layout.gridRows - spanY)) {
            for (x in 0..(layout.gridColumns - spanX)) {
                if (isGridRegionFree(page, x, y, spanX, spanY)) {
                    return x to y
                }
            }
        }
        for (y in 0 until startRow) {
            for (x in 0..(layout.gridColumns - spanX)) {
                if (isGridRegionFree(page, x, y, spanX, spanY)) {
                    return x to y
                }
            }
        }
        return null
    }

    fun pinAppToWorkspace(app: AstraAppEntry, preferredPage: Int = 0): WorkspaceCellItem? {
        val layout = _homeLayout.value
        if (layout.lockWorkspaceLayout) return null

        for (p in preferredPage until layout.pageCount) {
            val cell = findFirstFreeCell(p, 1, 1)
            if (cell != null) {
                val newItem = WorkspaceCellItem(
                    id = "item_${UUID.randomUUID()}",
                    page = p,
                    cellX = cell.first,
                    cellY = cell.second,
                    spanX = 1,
                    spanY = 1,
                    itemType = WorkspaceItemType.APP,
                    packageName = app.packageName,
                    componentName = app.componentName,
                    userSerial = app.userSerial,
                    label = app.label
                )
                updateHomeLayout { it.copy(isInitialized = true, items = it.items + newItem) }
                return newItem
            }
        }
        return null
    }

    /**
     * Suggests an automatic smart folder name based on the member apps' package/labels (Rebuild Section 11).
     */
    fun suggestSmartFolderName(members: List<FolderMemberApp>): String {
        val combined = members.joinToString(" ") { "${it.packageName} ${it.label}".lowercase() }
        return when {
            combined.contains("whatsapp") || combined.contains("telegram") || combined.contains("message") || combined.contains("dialer") -> "Communication"
            combined.contains("youtube") || combined.contains("spotify") || combined.contains("music") || combined.contains("camera") || combined.contains("photo") -> "Media"
            combined.contains("chrome") || combined.contains("firefox") || combined.contains("browser") -> "Browsers"
            combined.contains("docs") || combined.contains("calendar") || combined.contains("mail") || combined.contains("gmail") -> "Productivity"
            else -> "Folder"
        }
    }

    fun moveOrMergeWorkspaceItem(
        itemId: String,
        targetPage: Int,
        targetCellX: Int,
        targetCellY: Int
    ): Boolean {
        val layout = _homeLayout.value
        if (layout.lockWorkspaceLayout) return false
        val moving = layout.items.firstOrNull { it.id == itemId } ?: return false

        val occupant = layout.items.firstOrNull { item ->
            item.id != itemId &&
                item.page == targetPage &&
                targetCellX in item.cellX until (item.cellX + item.spanX) &&
                targetCellY in item.cellY until (item.cellY + item.spanY)
        }

        if (occupant == null && isGridRegionFree(targetPage, targetCellX, targetCellY, moving.spanX, moving.spanY, itemId)) {
            updateHomeLayout { cur ->
                cur.copy(
                    items = cur.items.map {
                        if (it.id == itemId) {
                            it.copy(page = targetPage, cellX = targetCellX, cellY = targetCellY)
                        } else it
                    }
                )
            }
            return true
        }

        if (moving.itemType == WorkspaceItemType.APP && occupant?.itemType == WorkspaceItemType.APP) {
            val folderId = "folder_${UUID.randomUUID()}"
            val members = listOf(
                FolderMemberApp(occupant.packageName, occupant.componentName, occupant.userSerial, occupant.label),
                FolderMemberApp(moving.packageName, moving.componentName, moving.userSerial, moving.label)
            )
            val autoName = suggestSmartFolderName(members)
            val folderCell = WorkspaceCellItem(
                id = folderId,
                page = targetPage,
                cellX = occupant.cellX,
                cellY = occupant.cellY,
                spanX = 1,
                spanY = 1,
                itemType = WorkspaceItemType.FOLDER,
                label = autoName,
                folderId = folderId,
                folderItems = members
            )
            updateHomeLayout { cur ->
                cur.copy(
                    items = cur.items.filterNot { it.id == moving.id || it.id == occupant.id } + folderCell
                )
            }
            return true
        }

        if (moving.itemType == WorkspaceItemType.APP && occupant?.itemType == WorkspaceItemType.FOLDER) {
            val newMember = FolderMemberApp(moving.packageName, moving.componentName, moving.userSerial, moving.label)
            val alreadyInFolder = occupant.folderItems.any { it.componentName == newMember.componentName }
            val updatedFolder = if (alreadyInFolder) {
                occupant
            } else {
                occupant.copy(folderItems = occupant.folderItems + newMember)
            }
            updateHomeLayout { cur ->
                cur.copy(
                    items = cur.items
                        .filterNot { it.id == moving.id }
                        .map { if (it.id == occupant.id) updatedFolder else it }
                )
            }
            return true
        }

        if (occupant != null && moving.spanX == 1 && moving.spanY == 1 && occupant.spanX == 1 && occupant.spanY == 1) {
            updateHomeLayout { cur ->
                cur.copy(
                    items = cur.items.map {
                        when (it.id) {
                            moving.id -> it.copy(page = occupant.page, cellX = occupant.cellX, cellY = occupant.cellY)
                            occupant.id -> it.copy(page = moving.page, cellX = moving.cellX, cellY = moving.cellY)
                            else -> it
                        }
                    }
                )
            }
            return true
        }

        return false
    }

    fun createFolderOnWorkspace(
        page: Int,
        title: String,
        apps: List<FolderMemberApp>
    ): WorkspaceCellItem? {
        if (apps.isEmpty()) return null
        val cell = findFirstFreeCell(page, 1, 1) ?: return null
        val folderId = "folder_${UUID.randomUUID()}"
        val folderItem = WorkspaceCellItem(
            id = folderId,
            page = page,
            cellX = cell.first,
            cellY = cell.second,
            spanX = 1,
            spanY = 1,
            itemType = WorkspaceItemType.FOLDER,
            label = title.ifBlank { suggestSmartFolderName(apps) },
            folderId = folderId,
            folderItems = apps
        )
        updateHomeLayout { cur -> cur.copy(isInitialized = true, items = cur.items + folderItem) }
        return folderItem
    }

    fun renameFolder(folderId: String, newTitle: String) {
        updateHomeLayout { cur ->
            cur.copy(
                items = cur.items.map { item ->
                    if (item.id == folderId || item.folderId == folderId) {
                        item.copy(label = newTitle.ifBlank { "Folder" })
                    } else item
                }
            )
        }
    }

    fun addAppToFolder(folderId: String, app: AstraAppEntry) {
        updateHomeLayout { cur ->
            cur.copy(
                items = cur.items.map { item ->
                    if (item.id == folderId || item.folderId == folderId) {
                        val exists = item.folderItems.any { it.componentName == app.componentName }
                        if (exists) item else item.copy(
                            folderItems = item.folderItems + FolderMemberApp(
                                packageName = app.packageName,
                                componentName = app.componentName,
                                userSerial = app.userSerial,
                                label = app.label
                            )
                        )
                    } else item
                }
            )
        }
    }

    fun removeAppFromFolder(folderId: String, componentName: String) {
        updateHomeLayout { cur ->
            val updated = cur.items.mapNotNull { item ->
                if (item.id == folderId || item.folderId == folderId) {
                    val remaining = item.folderItems.filterNot { it.componentName == componentName }
                    if (remaining.isEmpty()) null else item.copy(folderItems = remaining)
                } else item
            }
            cur.copy(items = updated)
        }
    }

    fun reorderFolderItems(folderId: String, fromIndex: Int, toIndex: Int) {
        updateHomeLayout { cur ->
            cur.copy(
                items = cur.items.map { item ->
                    if (item.id == folderId || item.folderId == folderId) {
                        val list = item.folderItems.toMutableList()
                        if (fromIndex in list.indices && toIndex in list.indices) {
                            val moved = list.removeAt(fromIndex)
                            list.add(toIndex, moved)
                            item.copy(folderItems = list)
                        } else item
                    } else item
                }
            )
        }
    }

    fun addWidgetToWorkspace(
        appWidgetId: Int,
        providerComponent: String,
        label: String,
        preferredPage: Int = 0,
        spanX: Int = 4,
        spanY: Int = 2
    ): WorkspaceCellItem? {
        val layout = _homeLayout.value
        val clampedSpanX = spanX.coerceIn(1, layout.gridColumns)
        val clampedSpanY = spanY.coerceIn(1, layout.gridRows)

        for (p in preferredPage until layout.pageCount) {
            val cell = findFirstFreeCell(p, clampedSpanX, clampedSpanY)
            if (cell != null) {
                val widgetItem = WorkspaceCellItem(
                    id = "widget_$appWidgetId",
                    page = p,
                    cellX = cell.first,
                    cellY = cell.second,
                    spanX = clampedSpanX,
                    spanY = clampedSpanY,
                    itemType = WorkspaceItemType.WIDGET,
                    packageName = providerComponent.substringBefore('/'),
                    label = label,
                    appWidgetId = appWidgetId,
                    widgetProvider = providerComponent
                )
                updateHomeLayout { cur -> cur.copy(isInitialized = true, items = cur.items + widgetItem) }
                return widgetItem
            }
        }
        val newPageIndex = layout.pageCount
        val widgetItem = WorkspaceCellItem(
            id = "widget_$appWidgetId",
            page = newPageIndex,
            cellX = 0,
            cellY = 0,
            spanX = clampedSpanX,
            spanY = clampedSpanY,
            itemType = WorkspaceItemType.WIDGET,
            packageName = providerComponent.substringBefore('/'),
            label = label,
            appWidgetId = appWidgetId,
            widgetProvider = providerComponent
        )
        updateHomeLayout { cur ->
            cur.copy(
                isInitialized = true,
                pageCount = newPageIndex + 1,
                items = cur.items + widgetItem
            )
        }
        return widgetItem
    }

    fun resizeWidgetOnWorkspace(itemId: String, newSpanX: Int, newSpanY: Int): Boolean {
        val layout = _homeLayout.value
        val target = layout.items.firstOrNull { it.id == itemId && it.itemType == WorkspaceItemType.WIDGET }
            ?: return false
        val sx = newSpanX.coerceIn(1, layout.gridColumns - target.cellX)
        val sy = newSpanY.coerceIn(1, layout.gridRows - target.cellY)
        if (!isGridRegionFree(target.page, target.cellX, target.cellY, sx, sy, itemId)) {
            return false
        }
        updateHomeLayout { cur ->
            cur.copy(
                items = cur.items.map {
                    if (it.id == itemId) it.copy(spanX = sx, spanY = sy) else it
                }
            )
        }
        return true
    }

    fun removeWorkspaceItem(itemId: String) {
        updateHomeLayout { cur ->
            cur.copy(items = cur.items.filterNot { it.id == itemId })
        }
    }

    fun setDockSlot(slotIndex: Int, app: AstraAppEntry) {
        updateHomeLayout { cur ->
            val maxSlots = cur.dockSlotCount.coerceIn(3, 6)
            val clampedSlot = slotIndex.coerceIn(0, maxSlots - 1)
            val filtered = cur.dockItems.filterNot {
                it.slotIndex == clampedSlot || it.componentName == app.componentName
            }
            val newDockItem = DockSlotItem(
                slotIndex = clampedSlot,
                packageName = app.packageName,
                componentName = app.componentName,
                userSerial = app.userSerial,
                label = app.label
            )
            cur.copy(dockItems = (filtered + newDockItem).sortedBy { it.slotIndex })
        }
    }

    fun removeDockSlot(slotIndex: Int) {
        updateHomeLayout { cur ->
            cur.copy(dockItems = cur.dockItems.filterNot { it.slotIndex == slotIndex })
        }
    }

    fun addWorkspacePage() {
        updateHomeLayout { cur ->
            cur.copy(pageCount = (cur.pageCount + 1).coerceAtMost(8))
        }
    }

    fun removeLastWorkspacePage(): Boolean {
        val cur = _homeLayout.value
        if (cur.pageCount <= 1) return false
        val lastPage = cur.pageCount - 1
        val hasItemsOnLastPage = cur.items.any { it.page == lastPage }
        if (hasItemsOnLastPage) return false
        updateHomeLayout { it.copy(pageCount = lastPage) }
        return true
    }

    fun updateThemeSettings(transform: (ThemeSettings) -> ThemeSettings) {
        val updated = transform(_themeSettings.value)
        saveThemeSettings(updated)
        _themeSettings.value = updated
    }

    fun updateHomeLayout(transform: (HomeLayout) -> HomeLayout) {
        val updated = transform(_homeLayout.value)
        saveHomeLayout(updated)
        _homeLayout.value = updated
    }

    fun updateSearchPreferences(transform: (SearchPreferences) -> SearchPreferences) {
        val updated = transform(_searchPreferences.value)
        saveSearchPreferences(updated)
        _searchPreferences.value = updated
    }

    fun updateNotificationPreferences(transform: (NotificationPreferences) -> NotificationPreferences) {
        val updated = transform(_notificationPreferences.value)
        saveNotificationPreferences(updated)
        _notificationPreferences.value = updated
    }

    fun updateGesturePreferences(transform: (GesturePreferences) -> GesturePreferences) {
        val updated = transform(_gesturePreferences.value)
        saveGesturePreferences(updated)
        _gesturePreferences.value = updated
    }

    fun updatePerformancePreferences(transform: (PerformancePreferences) -> PerformancePreferences) {
        val updated = transform(_performancePreferences.value)
        savePerformancePreferences(updated)
        _performancePreferences.value = updated
    }

    fun applyThemePreset(preset: AstraThemePreset) {
        updateThemeSettings { current ->
            current.copy(
                themePreset = preset,
                wallpaperId = preset.defaultWallpaper,
                themeMode = if (preset.isLightDefault) AstraThemeMode.LIGHT else AstraThemeMode.DARK,
                customAccentHex = preset.primaryAccentHex
            )
        }
    }

    fun resetToSafeDefaults() {
        val defaultTheme = ThemeSettings()
        val defaultLayout = HomeLayout()
        val defaultSearch = SearchPreferences()
        val defaultNotif = NotificationPreferences()
        val defaultGesture = GesturePreferences()
        val defaultPerf = PerformancePreferences()

        saveThemeSettings(defaultTheme)
        saveHomeLayout(defaultLayout)
        saveSearchPreferences(defaultSearch)
        saveNotificationPreferences(defaultNotif)
        saveGesturePreferences(defaultGesture)
        savePerformancePreferences(defaultPerf)

        _themeSettings.value = defaultTheme
        _homeLayout.value = defaultLayout
        _searchPreferences.value = defaultSearch
        _notificationPreferences.value = defaultNotif
        _gesturePreferences.value = defaultGesture
        _performancePreferences.value = defaultPerf
        _corruptedWorkspaceRecovered.value = false
    }

    private fun saveUsageStats() {
        val encoded = usageCounts.entries.joinToString(";") { (comp, count) ->
            val last = lastUsedMap[comp] ?: 0L
            "${escapeField(comp)}|$count|$last"
        }
        putString(KEY_USAGE_STATS, encoded)
    }

    private fun loadUsageStats() {
        val raw = getString(KEY_USAGE_STATS, "")
        if (raw.isBlank()) return
        raw.split(";").forEach { token ->
            val parts = token.split("|")
            if (parts.size >= 3) {
                val comp = unescapeField(parts[0])
                val count = parts[1].toIntOrNull() ?: 0
                val last = parts[2].toLongOrNull() ?: 0L
                usageCounts[comp] = count
                lastUsedMap[comp] = last
            }
        }
    }

    private fun saveThemeSettings(s: ThemeSettings) {
        putString(KEY_THEME_PRESET, s.themePreset.id)
        putString(KEY_WALLPAPER_SOURCE, s.wallpaperSource.id)
        putString(KEY_WALLPAPER_ID, s.wallpaperId.id)
        putString(KEY_ACCENT_SOURCE, s.accentSource.id)
        putString(KEY_THEME_MODE, s.themeMode.id)
        putString(KEY_ICON_STYLE, s.iconStyle.id)
        putString(KEY_CLOCK_STYLE, s.clockStyle.id)
        putString(KEY_CUSTOM_ACCENT, s.customAccentHex.toString())
        putString(KEY_ICON_SCALE, s.iconScale.toString())
        putString(KEY_SHOW_LABELS, s.showIconLabels.toString())
        putString(KEY_SHOW_DOCK_LABELS, s.showDockLabels.toString())
        putString(KEY_DOCK_GLASS, s.dockStyleGlass.toString())
        putString(KEY_MATERIAL_INTENSITY, s.materialIntensity.toString())
        putString(KEY_TEXT_SCALE, s.textScaleMultiplier.toString())
        putString(KEY_HIGH_CONTRAST, s.highContrastMode.toString())
    }

    private fun loadThemeSettings(): ThemeSettings {
        val d = ThemeSettings()
        return ThemeSettings(
            themePreset = AstraThemePreset.fromId(getString(KEY_THEME_PRESET, d.themePreset.id)),
            wallpaperSource = WallpaperSource.fromId(getString(KEY_WALLPAPER_SOURCE, d.wallpaperSource.id)),
            wallpaperId = AstraWallpaperId.fromId(getString(KEY_WALLPAPER_ID, d.wallpaperId.id)),
            accentSource = AstraAccentSource.fromId(getString(KEY_ACCENT_SOURCE, d.accentSource.id)),
            themeMode = AstraThemeMode.fromId(getString(KEY_THEME_MODE, d.themeMode.id)),
            iconStyle = AstraIconStyle.fromId(getString(KEY_ICON_STYLE, d.iconStyle.id)),
            clockStyle = AstraClockStyle.fromId(getString(KEY_CLOCK_STYLE, d.clockStyle.id)),
            customAccentHex = getString(KEY_CUSTOM_ACCENT, d.customAccentHex.toString()).toLongOrNull() ?: d.customAccentHex,
            iconScale = getString(KEY_ICON_SCALE, d.iconScale.toString()).toFloatOrNull() ?: d.iconScale,
            showIconLabels = getString(KEY_SHOW_LABELS, d.showIconLabels.toString()) == "true",
            showDockLabels = getString(KEY_SHOW_DOCK_LABELS, d.showDockLabels.toString()) == "true",
            dockStyleGlass = getString(KEY_DOCK_GLASS, d.dockStyleGlass.toString()) == "true",
            materialIntensity = getString(KEY_MATERIAL_INTENSITY, d.materialIntensity.toString()).toFloatOrNull() ?: d.materialIntensity,
            textScaleMultiplier = getString(KEY_TEXT_SCALE, d.textScaleMultiplier.toString()).toFloatOrNull() ?: d.textScaleMultiplier,
            highContrastMode = getString(KEY_HIGH_CONTRAST, d.highContrastMode.toString()) == "true"
        )
    }

    private fun saveHomeLayout(h: HomeLayout) {
        putString(KEY_WS_INITIALIZED, h.isInitialized.toString())
        putString(KEY_FIRST_RUN_DONE, h.hasCompletedFirstRunSetup.toString())
        putString(KEY_DENSITY_MODE, h.densityMode.id)
        putString(KEY_PAGE_COUNT, h.pageCount.toString())
        putString(KEY_GRID_COLS, h.gridColumns.toString())
        putString(KEY_GRID_ROWS, h.gridRows.toString())
        putString(KEY_DOCK_ENABLED, h.dockEnabled.toString())
        putString(KEY_DOCK_SLOTS, h.dockSlotCount.toString())
        putString(KEY_DOCK_SEARCH, h.showDockSearchButton.toString())
        putString(KEY_SHOW_PAGE_IND, h.showPageIndicator.toString())
        putString(KEY_SHOW_CLOCK_WS, h.showClockOnWorkspace.toString())
        putString(KEY_SHOW_CONTEXT_SUGGEST, h.showContextualSuggestionsOnHome.toString())
        putString(KEY_LOCK_WS, h.lockWorkspaceLayout.toString())
        putString(KEY_HIDDEN_PKGS, h.hiddenComponents.joinToString(",") { escapeField(it) })
        putString(KEY_FAV_PKGS, h.favoriteComponents.joinToString(",") { escapeField(it) })

        val encodedItems = h.items.joinToString("\n") { item ->
            val folderMembersEncoded = item.folderItems.joinToString("~") { m ->
                "${escapeField(m.packageName)}^${escapeField(m.componentName)}^${m.userSerial}^${escapeField(m.label)}"
            }
            listOf(
                escapeField(item.id),
                item.page.toString(),
                item.cellX.toString(),
                item.cellY.toString(),
                item.spanX.toString(),
                item.spanY.toString(),
                item.itemType.id,
                escapeField(item.packageName),
                escapeField(item.componentName),
                item.userSerial.toString(),
                escapeField(item.label),
                escapeField(item.shortcutId),
                item.appWidgetId.toString(),
                escapeField(item.widgetProvider),
                escapeField(item.folderId),
                folderMembersEncoded
            ).joinToString("|")
        }
        putString(KEY_WS_ITEMS_V2, encodedItems)

        val encodedDock = h.dockItems.joinToString("\n") { slot ->
            listOf(
                slot.slotIndex.toString(),
                escapeField(slot.packageName),
                escapeField(slot.componentName),
                slot.userSerial.toString(),
                escapeField(slot.label)
            ).joinToString("|")
        }
        putString(KEY_DOCK_ITEMS_V2, encodedDock)
    }

    private fun loadHomeLayout(): HomeLayout {
        val d = HomeLayout()
        return try {
            val initialized = getString(KEY_WS_INITIALIZED, "false") == "true"
            val firstRunDone = getString(KEY_FIRST_RUN_DONE, "false") == "true"
            val densityMode = HomeDensityMode.fromId(getString(KEY_DENSITY_MODE, d.densityMode.id))
            val pageCount = (getString(KEY_PAGE_COUNT, d.pageCount.toString()).toIntOrNull() ?: d.pageCount).coerceIn(1, 8)
            val cols = (getString(KEY_GRID_COLS, d.gridColumns.toString()).toIntOrNull() ?: d.gridColumns).coerceIn(3, 6)
            val rows = (getString(KEY_GRID_ROWS, d.gridRows.toString()).toIntOrNull() ?: d.gridRows).coerceIn(4, 7)
            val dockSlots = (getString(KEY_DOCK_SLOTS, d.dockSlotCount.toString()).toIntOrNull() ?: d.dockSlotCount).coerceIn(3, 6)

            val rawItems = getString(KEY_WS_ITEMS_V2, "")
            val parsedItems = if (rawItems.isBlank()) {
                emptyList()
            } else {
                rawItems.lines().filter { it.isNotBlank() }.map { line ->
                    val p = line.split("|")
                    if (p.size < 16) throw IllegalArgumentException("Corrupted workspace item record")
                    val folderRaw = p[15]
                    val folderMembers = if (folderRaw.isBlank()) {
                        emptyList()
                    } else {
                        folderRaw.split("~").mapNotNull { mToken ->
                            val mp = mToken.split("^")
                            if (mp.size >= 4) {
                                FolderMemberApp(
                                    packageName = unescapeField(mp[0]),
                                    componentName = unescapeField(mp[1]),
                                    userSerial = mp[2].toLongOrNull() ?: 0L,
                                    label = unescapeField(mp[3])
                                )
                            } else null
                        }
                    }
                    WorkspaceCellItem(
                        id = unescapeField(p[0]),
                        page = p[1].toInt().coerceIn(0, pageCount - 1),
                        cellX = p[2].toInt().coerceIn(0, cols - 1),
                        cellY = p[3].toInt().coerceIn(0, rows - 1),
                        spanX = p[4].toInt().coerceIn(1, cols),
                        spanY = p[5].toInt().coerceIn(1, rows),
                        itemType = WorkspaceItemType.fromId(p[6]),
                        packageName = unescapeField(p[7]),
                        componentName = unescapeField(p[8]),
                        userSerial = p[9].toLongOrNull() ?: 0L,
                        label = unescapeField(p[10]),
                        shortcutId = unescapeField(p[11]),
                        appWidgetId = p[12].toIntOrNull() ?: -1,
                        widgetProvider = unescapeField(p[13]),
                        folderId = unescapeField(p[14]),
                        folderItems = folderMembers
                    )
                }
            }

            val rawDock = getString(KEY_DOCK_ITEMS_V2, "")
            val parsedDock = if (rawDock.isBlank()) {
                emptyList()
            } else {
                rawDock.lines().filter { it.isNotBlank() }.map { line ->
                    val p = line.split("|")
                    if (p.size < 5) throw IllegalArgumentException("Corrupted dock record")
                    DockSlotItem(
                        slotIndex = p[0].toInt().coerceIn(0, dockSlots - 1),
                        packageName = unescapeField(p[1]),
                        componentName = unescapeField(p[2]),
                        userSerial = p[3].toLongOrNull() ?: 0L,
                        label = unescapeField(p[4])
                    )
                }
            }

            HomeLayout(
                isInitialized = initialized,
                hasCompletedFirstRunSetup = firstRunDone,
                densityMode = densityMode,
                pageCount = pageCount,
                gridColumns = cols,
                gridRows = rows,
                dockEnabled = getString(KEY_DOCK_ENABLED, "true") == "true",
                dockSlotCount = dockSlots,
                showDockSearchButton = getString(KEY_DOCK_SEARCH, "false") == "true",
                showPageIndicator = getString(KEY_SHOW_PAGE_IND, "true") == "true",
                showClockOnWorkspace = getString(KEY_SHOW_CLOCK_WS, "true") == "true",
                showContextualSuggestionsOnHome = getString(KEY_SHOW_CONTEXT_SUGGEST, "true") == "true",
                lockWorkspaceLayout = getString(KEY_LOCK_WS, "false") == "true",
                items = parsedItems,
                dockItems = parsedDock,
                hiddenComponents = getString(KEY_HIDDEN_PKGS, "")
                    .split(",")
                    .filter { it.isNotBlank() }
                    .map { unescapeField(it) }
                    .toSet(),
                favoriteComponents = getString(KEY_FAV_PKGS, "")
                    .split(",")
                    .filter { it.isNotBlank() }
                    .map { unescapeField(it) }
                    .toSet()
            )
        } catch (_: Throwable) {
            _corruptedWorkspaceRecovered.value = true
            HomeLayout()
        }
    }

    fun injectRawWorkspacePayloadForRecoveryTest(rawPayload: String) {
        putString(KEY_WS_ITEMS_V2, rawPayload)
        _homeLayout.value = loadHomeLayout()
    }

    private fun saveSearchPreferences(s: SearchPreferences) {
        putString(KEY_SEARCH_APPS, s.searchApps.toString())
        putString(KEY_SEARCH_SETTINGS, s.searchSettings.toString())
        putString(KEY_SEARCH_SHORTCUTS, s.searchShortcuts.toString())
        putString(KEY_SEARCH_COMMANDS, s.searchCommands.toString())
        putString(KEY_SEARCH_RECENT, s.recentQueries.joinToString("|") { escapeField(it) })
        putString(KEY_SEARCH_AUTOFOCUS, s.autoFocusKeyboard.toString())
    }

    private fun loadSearchPreferences(): SearchPreferences {
        val d = SearchPreferences()
        return SearchPreferences(
            searchApps = getString(KEY_SEARCH_APPS, d.searchApps.toString()) == "true",
            searchSettings = getString(KEY_SEARCH_SETTINGS, d.searchSettings.toString()) == "true",
            searchShortcuts = getString(KEY_SEARCH_SHORTCUTS, d.searchShortcuts.toString()) == "true",
            searchCommands = getString(KEY_SEARCH_COMMANDS, d.searchCommands.toString()) == "true",
            recentQueries = getString(KEY_SEARCH_RECENT, "")
                .split("|")
                .filter { it.isNotBlank() }
                .map { unescapeField(it) },
            autoFocusKeyboard = getString(KEY_SEARCH_AUTOFOCUS, "true") == "true"
        )
    }

    private fun saveNotificationPreferences(n: NotificationPreferences) {
        putString(KEY_NOTIF_BADGES, n.showAppBadgeDots.toString())
        putString(KEY_NOTIF_COUNTS, n.showNotificationCountOnMenu.toString())
        putString(KEY_NOTIF_PILL, n.showHomeNotificationPill.toString())
    }

    private fun loadNotificationPreferences(): NotificationPreferences {
        val d = NotificationPreferences()
        return NotificationPreferences(
            showAppBadgeDots = getString(KEY_NOTIF_BADGES, d.showAppBadgeDots.toString()) == "true",
            showNotificationCountOnMenu = getString(KEY_NOTIF_COUNTS, d.showNotificationCountOnMenu.toString()) == "true",
            showHomeNotificationPill = getString(KEY_NOTIF_PILL, d.showHomeNotificationPill.toString()) == "true"
        )
    }

    private fun saveGesturePreferences(g: GesturePreferences) {
        putString(KEY_GESTURE_DOWN, g.swipeDownAction.id)
        putString(KEY_GESTURE_DOUBLE_TAP, g.doubleTapAction.id)
        putString(KEY_HAPTICS, g.hapticsEnabled.toString())
    }

    private fun loadGesturePreferences(): GesturePreferences {
        val d = GesturePreferences()
        return GesturePreferences(
            swipeDownAction = SwipeDownAction.fromId(getString(KEY_GESTURE_DOWN, d.swipeDownAction.id)),
            doubleTapAction = DoubleTapAction.fromId(getString(KEY_GESTURE_DOUBLE_TAP, d.doubleTapAction.id)),
            hapticsEnabled = getString(KEY_HAPTICS, d.hapticsEnabled.toString()) == "true"
        )
    }

    private fun savePerformancePreferences(p: PerformancePreferences) {
        putString(KEY_PERF_ANIM, p.animationsEnabled.toString())
        putString(KEY_PERF_REDUCED_MOTION, p.reducedMotion.toString())
        putString(KEY_PERF_BLUR, p.blurEnabled.toString())
        putString(KEY_PERF_LOW_END, p.lowEndDeviceModeOverride.toString())
        putString(KEY_DRAWER_CATS, p.showDrawerCategories.toString())
        putString(KEY_DRAWER_RECENT, p.showDrawerRecentRow.toString())
        putString(KEY_APP_SORT, p.appSortOrder.id)
    }

    private fun loadPerformancePreferences(): PerformancePreferences {
        val d = PerformancePreferences()
        return PerformancePreferences(
            animationsEnabled = getString(KEY_PERF_ANIM, d.animationsEnabled.toString()) == "true",
            reducedMotion = getString(KEY_PERF_REDUCED_MOTION, d.reducedMotion.toString()) == "true",
            blurEnabled = getString(KEY_PERF_BLUR, d.blurEnabled.toString()) == "true",
            lowEndDeviceModeOverride = getString(KEY_PERF_LOW_END, d.lowEndDeviceModeOverride.toString()) == "true",
            showDrawerCategories = getString(KEY_DRAWER_CATS, d.showDrawerCategories.toString()) == "true",
            showDrawerRecentRow = getString(KEY_DRAWER_RECENT, d.showDrawerRecentRow.toString()) == "true",
            appSortOrder = AppSortOrder.fromId(getString(KEY_APP_SORT, d.appSortOrder.id))
        )
    }

    private fun escapeField(raw: String): String =
        raw.replace("%", "%25")
            .replace("|", "%7C")
            .replace("\n", "%0A")
            .replace("~", "%7E")
            .replace("^", "%5E")
            .replace(",", "%2C")

    private fun unescapeField(encoded: String): String =
        encoded.replace("%2C", ",")
            .replace("%5E", "^")
            .replace("%7E", "~")
            .replace("%0A", "\n")
            .replace("%7C", "|")
            .replace("%25", "%")

    companion object {
        private const val PREFS_NAME = "astra_real_launcher_v2"
        private const val KEY_THEME_PRESET = "theme_preset"
        private const val KEY_WALLPAPER_SOURCE = "wallpaper_source"
        private const val KEY_WALLPAPER_ID = "wallpaper_id"
        private const val KEY_ACCENT_SOURCE = "accent_source"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_ICON_STYLE = "icon_style"
        private const val KEY_CLOCK_STYLE = "clock_style"
        private const val KEY_CUSTOM_ACCENT = "custom_accent"
        private const val KEY_ICON_SCALE = "icon_scale"
        private const val KEY_SHOW_LABELS = "show_labels"
        private const val KEY_SHOW_DOCK_LABELS = "show_dock_labels"
        private const val KEY_DOCK_GLASS = "dock_glass"
        private const val KEY_MATERIAL_INTENSITY = "material_intensity"
        private const val KEY_TEXT_SCALE = "text_scale"
        private const val KEY_HIGH_CONTRAST = "high_contrast"

        private const val KEY_WS_INITIALIZED = "ws_initialized"
        private const val KEY_FIRST_RUN_DONE = "first_run_done"
        private const val KEY_DENSITY_MODE = "density_mode"
        private const val KEY_PAGE_COUNT = "page_count"
        private const val KEY_GRID_COLS = "grid_cols"
        private const val KEY_GRID_ROWS = "grid_rows"
        private const val KEY_DOCK_ENABLED = "dock_enabled"
        private const val KEY_DOCK_SLOTS = "dock_slots"
        private const val KEY_DOCK_SEARCH = "dock_search"
        private const val KEY_SHOW_PAGE_IND = "show_page_ind"
        private const val KEY_SHOW_CLOCK_WS = "show_clock_ws"
        private const val KEY_SHOW_CONTEXT_SUGGEST = "show_context_suggest"
        private const val KEY_LOCK_WS = "lock_ws"
        private const val KEY_HIDDEN_PKGS = "hidden_pkgs"
        private const val KEY_FAV_PKGS = "fav_pkgs"
        private const val KEY_WS_ITEMS_V2 = "ws_items_v2"
        private const val KEY_DOCK_ITEMS_V2 = "dock_items_v2"
        private const val KEY_USAGE_STATS = "usage_stats_v2"

        private const val KEY_SEARCH_APPS = "search_apps"
        private const val KEY_SEARCH_SETTINGS = "search_settings"
        private const val KEY_SEARCH_SHORTCUTS = "search_shortcuts"
        private const val KEY_SEARCH_COMMANDS = "search_commands"
        private const val KEY_SEARCH_RECENT = "search_recent"
        private const val KEY_SEARCH_AUTOFOCUS = "search_autofocus"

        private const val KEY_NOTIF_BADGES = "notif_badges"
        private const val KEY_NOTIF_COUNTS = "notif_counts"
        private const val KEY_NOTIF_PILL = "notif_pill"

        private const val KEY_GESTURE_DOWN = "gesture_down"
        private const val KEY_GESTURE_DOUBLE_TAP = "gesture_double_tap"
        private const val KEY_HAPTICS = "haptics"

        private const val KEY_PERF_ANIM = "perf_anim"
        private const val KEY_PERF_REDUCED_MOTION = "perf_reduced_motion"
        private const val KEY_PERF_BLUR = "perf_blur"
        private const val KEY_PERF_LOW_END = "perf_low_end"
        private const val KEY_DRAWER_CATS = "drawer_cats"
        private const val KEY_DRAWER_RECENT = "drawer_recent"
        private const val KEY_APP_SORT = "app_sort"
    }
}
