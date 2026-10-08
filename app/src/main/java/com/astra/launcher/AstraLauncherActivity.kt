package com.astra.launcher

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.astra.launcher.core.design.AstraColorEngine
import com.astra.launcher.core.design.AstraWallpaperSurface
import com.astra.launcher.core.design.toDrawableResId
import com.astra.launcher.core.performance.AstraPerformanceManager
import com.astra.launcher.core.platform.AstraCapabilityManager
import com.astra.launcher.core.platform.AstraIconPipeline
import com.astra.launcher.core.platform.AstraNotificationStreamBus
import com.astra.launcher.core.platform.AstraPackageRepository
import com.astra.launcher.core.platform.AstraRoleHomeManager
import com.astra.launcher.core.platform.AstraSystemController
import com.astra.launcher.core.platform.AstraWidgetHostManager
import com.astra.launcher.core.platform.InstalledWidgetProvider
import com.astra.launcher.core.platform.LaunchResult
import com.astra.launcher.core.storage.AstraAppEntry
import com.astra.launcher.core.storage.AstraStorageRepository
import com.astra.launcher.core.storage.DoubleTapAction
import com.astra.launcher.core.storage.SwipeDownAction
import com.astra.launcher.core.storage.WorkspaceCellItem
import com.astra.launcher.feature.apps.AstraAppDrawerOverlay
import com.astra.launcher.feature.home.AstraAppContextMenuSheet
import com.astra.launcher.feature.home.AstraFirstRunSetupOverlay
import com.astra.launcher.feature.home.AstraFolderOverlay
import com.astra.launcher.feature.home.AstraNotificationAndControlOverlay
import com.astra.launcher.feature.home.AstraWorkspaceLayer
import com.astra.launcher.feature.personalization.AstraPersonalizationOverlay
import com.astra.launcher.feature.search.AstraSearchOverlay
import com.astra.launcher.feature.search.SearchCommandAction
import com.astra.launcher.feature.search.SearchSettingAction
import com.astra.launcher.feature.settings.AstraSettingsOverlay
import com.astra.launcher.feature.widgets.AstraWidgetPickerSheet
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Primary Real Android Home Launcher Activity (`com.astra.launcher.AstraLauncherActivity`).
 *
 * Implements the complete Astra Product, UX, and System Experience (Rebuild Sections 1–50):
 * - Real Default Home Role (`CATEGORY_HOME` + `CATEGORY_DEFAULT` + `RoleManager.ROLE_HOME` + `singleTask`)
 * - 6-Step Visual First-Run Setup ("Your phone, redesigned.")
 * - Persistent 2D Coordinate Home Workspace with Adaptive Clock, Contextual Header & Dock
 * - Modern Application Discovery (Recently Used, Favorites, Contextual Suggestions, Smart Categories, All Apps)
 * - System-Level Universal Search with Natural-Language Intent Handoff
 * - Real Android `AppWidgetHost` Widget Hosting & `LauncherApps` Real Icon Pipeline
 * - Truthful Control & Notification Surface
 */
class AstraLauncherActivity : ComponentActivity() {

    lateinit var storageRepository: AstraStorageRepository
        private set
    lateinit var iconPipeline: AstraIconPipeline
        private set
    lateinit var packageRepository: AstraPackageRepository
        private set
    lateinit var capabilityManager: AstraCapabilityManager
        private set
    lateinit var systemController: AstraSystemController
        private set
    lateinit var widgetHostManager: AstraWidgetHostManager
        private set
    lateinit var performanceManager: AstraPerformanceManager
        private set

    private val _homeIntentSignal = MutableStateFlow(0L)
    val homeIntentSignal: StateFlow<Long> = _homeIntentSignal.asStateFlow()

    private var pendingWidgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID
    private var pendingWidgetProvider: InstalledWidgetProvider? = null

    private val requestHomeRoleLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        // Re-inspected on resume
    }

    private val bindWidgetPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val widgetId = pendingWidgetId
        val provider = pendingWidgetProvider
        if (result.resultCode == Activity.RESULT_OK && widgetId != AppWidgetManager.INVALID_APPWIDGET_ID && provider != null) {
            completeWidgetBindingOrLaunchConfigure(widgetId, provider)
        } else {
            if (widgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                widgetHostManager.deleteWidgetId(widgetId)
            }
            pendingWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
            pendingWidgetProvider = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        storageRepository = AstraStorageRepository(applicationContext)
        iconPipeline = AstraIconPipeline(applicationContext)
        capabilityManager = AstraCapabilityManager(applicationContext)
        systemController = AstraSystemController(applicationContext)
        widgetHostManager = AstraWidgetHostManager(applicationContext)
        performanceManager = AstraPerformanceManager(applicationContext)

        packageRepository = AstraPackageRepository(
            context = applicationContext,
            iconPipeline = iconPipeline,
            onPackagesUpdatedCallback = { discoveredApps ->
                storageRepository.populateInitialWorkspaceFromInstalledApps(discoveredApps)
                storageRepository.reconcileWithInstalledPackages(discoveredApps)
            }
        )

        packageRepository.registerLivePackageObservers()
        refreshLauncherPackages()

        setContent {
            val homeResetTick by homeIntentSignal.collectAsState()
            AstraLauncherRoot(
                storageRepository = storageRepository,
                packageRepository = packageRepository,
                iconPipeline = iconPipeline,
                capabilityManager = capabilityManager,
                systemController = systemController,
                widgetHostManager = widgetHostManager,
                homeResetTick = homeResetTick,
                onRequestDefaultHomeRole = { requestDefaultHomeRole() },
                onInitiateWidgetAdd = { provider -> initiateWidgetAdd(provider) },
                onRebindExistingWidget = { cellItem -> rebindExistingWidget(cellItem) }
            )
        }
    }

    override fun onStart() {
        super.onStart()
        widgetHostManager.startListening()
    }

    override fun onResume() {
        super.onResume()
        refreshLauncherPackages()
    }

    override fun onStop() {
        super.onStop()
        widgetHostManager.stopListening()
    }

    override fun onDestroy() {
        packageRepository.unregisterLivePackageObservers()
        super.onDestroy()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.action == Intent.ACTION_MAIN || intent.hasCategory(Intent.CATEGORY_HOME)) {
            _homeIntentSignal.value = System.currentTimeMillis()
        }
    }

    private fun refreshLauncherPackages() {
        packageRepository.refreshInstalledAppsAsync(
            usageLookup = { comp -> storageRepository.getUsageScore(comp) },
            lastUsedLookup = { comp -> storageRepository.getLastUsedTimestamp(comp) },
            notificationCountLookup = { pkg -> AstraNotificationStreamBus.countForPackage(pkg) }
        )
    }

    private fun requestDefaultHomeRole() {
        val roleIntent = AstraRoleHomeManager.buildRoleRequestIntent(this)
        if (roleIntent != null) {
            try {
                requestHomeRoleLauncher.launch(roleIntent)
                return
            } catch (_: Throwable) {
            }
        }
        AstraRoleHomeManager.openDefaultHomeSettings(this)
    }

    private fun initiateWidgetAdd(provider: InstalledWidgetProvider) {
        val widgetId = widgetHostManager.allocateWidgetId()
        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) return

        val boundImmediately = widgetHostManager.bindWidgetIfAllowed(widgetId, provider.providerComponent)
        if (boundImmediately) {
            completeWidgetBindingOrLaunchConfigure(widgetId, provider)
        } else {
            pendingWidgetId = widgetId
            pendingWidgetProvider = provider
            try {
                val bindIntent = widgetHostManager.buildBindPermissionIntent(widgetId, provider.providerComponent)
                bindWidgetPermissionLauncher.launch(bindIntent)
            } catch (_: Throwable) {
                storageRepository.addWidgetToWorkspace(
                    appWidgetId = widgetId,
                    providerComponent = provider.providerComponent.flattenToString(),
                    label = provider.label,
                    spanX = provider.recommendedSpanX,
                    spanY = provider.recommendedSpanY
                )
            }
        }
    }

    private fun completeWidgetBindingOrLaunchConfigure(widgetId: Int, provider: InstalledWidgetProvider) {
        if (provider.hasConfigurationActivity && widgetHostManager.appWidgetHost != null) {
            try {
                widgetHostManager.appWidgetHost?.startAppWidgetConfigureActivityForResult(
                    this,
                    widgetId,
                    0,
                    REQUEST_CONFIGURE_APPWIDGET,
                    null
                )
            } catch (_: Throwable) {
            }
        }
        storageRepository.addWidgetToWorkspace(
            appWidgetId = widgetId,
            providerComponent = provider.providerComponent.flattenToString(),
            label = provider.label,
            spanX = provider.recommendedSpanX,
            spanY = provider.recommendedSpanY
        )
        pendingWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
        pendingWidgetProvider = null
    }

    private fun rebindExistingWidget(cellItem: WorkspaceCellItem) {
        val comp = ComponentName.unflattenFromString(cellItem.widgetProvider) ?: return
        val newWidgetId = if (cellItem.appWidgetId >= 0) cellItem.appWidgetId else widgetHostManager.allocateWidgetId()
        if (widgetHostManager.bindWidgetIfAllowed(newWidgetId, comp)) {
            return
        }
        try {
            val bindIntent = widgetHostManager.buildBindPermissionIntent(newWidgetId, comp)
            bindWidgetPermissionLauncher.launch(bindIntent)
        } catch (_: Throwable) {
        }
    }

    companion object {
        private const val REQUEST_CONFIGURE_APPWIDGET = 2027
    }
}

@Composable
fun AstraLauncherRoot(
    storageRepository: AstraStorageRepository,
    packageRepository: AstraPackageRepository,
    iconPipeline: AstraIconPipeline,
    capabilityManager: AstraCapabilityManager,
    systemController: AstraSystemController,
    widgetHostManager: AstraWidgetHostManager,
    homeResetTick: Long,
    onRequestDefaultHomeRole: () -> Unit,
    onInitiateWidgetAdd: (InstalledWidgetProvider) -> Unit,
    onRebindExistingWidget: (WorkspaceCellItem) -> Unit
) {
    val themeSettings by storageRepository.themeSettings.collectAsState()
    val homeLayout by storageRepository.homeLayout.collectAsState()
    val searchPrefs by storageRepository.searchPreferences.collectAsState()
    val notifPrefs by storageRepository.notificationPreferences.collectAsState()
    val gesturePrefs by storageRepository.gesturePreferences.collectAsState()
    val perfPrefs by storageRepository.performancePreferences.collectAsState()
    val corruptedRecovered by storageRepository.corruptedWorkspaceRecovered.collectAsState()

    val installedApps by packageRepository.installedApps.collectAsState()
    val lastLaunchError by packageRepository.lastLaunchError.collectAsState()
    val activeNotifications by AstraNotificationStreamBus.notifications.collectAsState()

    var isAppDrawerOpen by remember { mutableStateOf(false) }
    var isSearchOpen by remember { mutableStateOf(false) }
    var isWidgetPickerOpen by remember { mutableStateOf(false) }
    var isPersonalizationOpen by remember { mutableStateOf(false) }
    var isSettingsOpen by remember { mutableStateOf(false) }
    var isNotificationAndControlOpen by remember { mutableStateOf(false) }
    var isEditMode by remember { mutableStateOf(false) }
    var openFolderId by remember { mutableStateOf<String?>(null) }
    var contextMenuTarget by remember { mutableStateOf<Pair<AstraAppEntry, WorkspaceCellItem?>?>(null) }
    var movingWorkspaceItem by remember { mutableStateOf<WorkspaceCellItem?>(null) }
    var defaultHomeBannerDismissed by remember { mutableStateOf(false) }

    LaunchedEffect(homeResetTick) {
        if (homeResetTick > 0L) {
            isAppDrawerOpen = false
            isSearchOpen = false
            isWidgetPickerOpen = false
            isPersonalizationOpen = false
            isSettingsOpen = false
            isNotificationAndControlOpen = false
            isEditMode = false
            openFolderId = null
            contextMenuTarget = null
            movingWorkspaceItem = null
        }
    }

    val capabilities = remember(
        installedApps.size,
        isSettingsOpen,
        isNotificationAndControlOpen,
        homeResetTick
    ) {
        capabilityManager.inspectCapabilities()
    }

    val deviceStatus = remember(
        installedApps.size,
        isNotificationAndControlOpen,
        homeResetTick
    ) {
        capabilityManager.inspectDeviceStatus()
    }

    val palette = remember(themeSettings) {
        AstraColorEngine.resolvePalette(
            wallpaper = themeSettings.wallpaperId,
            themeMode = themeSettings.themeMode,
            accentSource = themeSettings.accentSource,
            preset = themeSettings.themePreset,
            customAccentHex = themeSettings.customAccentHex,
            highContrast = themeSettings.highContrastMode
        )
    }

    val anyOverlayOpen = !homeLayout.hasCompletedFirstRunSetup ||
        isAppDrawerOpen ||
        isSearchOpen ||
        isWidgetPickerOpen ||
        isPersonalizationOpen ||
        isSettingsOpen ||
        isNotificationAndControlOpen ||
        openFolderId != null ||
        contextMenuTarget != null ||
        isEditMode ||
        movingWorkspaceItem != null

    BackHandler(enabled = true) {
        when {
            contextMenuTarget != null -> contextMenuTarget = null
            openFolderId != null -> openFolderId = null
            isWidgetPickerOpen -> isWidgetPickerOpen = false
            isPersonalizationOpen -> isPersonalizationOpen = false
            isSettingsOpen -> isSettingsOpen = false
            isNotificationAndControlOpen -> isNotificationAndControlOpen = false
            isSearchOpen -> isSearchOpen = false
            isAppDrawerOpen -> isAppDrawerOpen = false
            movingWorkspaceItem != null -> movingWorkspaceItem = null
            isEditMode -> isEditMode = false
        }
    }

    fun launchRealApplication(app: AstraAppEntry) {
        val result = packageRepository.launchApp(app)
        if (result is LaunchResult.Success) {
            storageRepository.recordAppLaunch(app.componentName)
            isAppDrawerOpen = false
            isSearchOpen = false
            isNotificationAndControlOpen = false
            openFolderId = null
            contextMenuTarget = null
        }
    }

    AstraWallpaperSurface(
        wallpaperId = themeSettings.wallpaperId,
        palette = palette,
        wallpaperSource = themeSettings.wallpaperSource,
        dimAmount = if (anyOverlayOpen) 0.35f else 0f,
        modifier = Modifier.fillMaxSize()
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // 1. PERSISTENT ROOT: 2D Coordinate Grid Workspace + Contextual Header + Dock
            AstraWorkspaceLayer(
                installedApps = installedApps,
                homeLayout = homeLayout,
                themeSettings = themeSettings,
                capabilities = capabilities,
                deviceStatus = deviceStatus,
                activeNotifications = if (notifPrefs.showHomeNotificationPill) activeNotifications else emptyList(),
                showDefaultHomeBanner = !defaultHomeBannerDismissed && homeLayout.hasCompletedFirstRunSetup,
                corruptedWorkspaceRecovered = corruptedRecovered,
                lastLaunchError = lastLaunchError,
                isEditMode = isEditMode,
                movingWorkspaceItem = movingWorkspaceItem,
                palette = palette,
                iconPipeline = iconPipeline,
                widgetHostManager = widgetHostManager,
                onLaunchApp = { app -> launchRealApplication(app) },
                onAppLongPress = { app, wsItem -> contextMenuTarget = app to wsItem },
                onOpenFolder = { folderItem -> openFolderId = folderItem.id },
                onOpenAppDrawer = { isAppDrawerOpen = true },
                onSwipeDownTrigger = {
                    when (gesturePrefs.swipeDownAction) {
                        SwipeDownAction.ANDROID_NOTIFICATION_SHADE -> {
                            val expanded = systemController.expandSystemNotificationShade()
                            if (!expanded) isNotificationAndControlOpen = true
                        }
                        SwipeDownAction.ASTRA_NOTIFICATIONS -> {
                            isNotificationAndControlOpen = true
                        }
                        SwipeDownAction.SEARCH -> {
                            isSearchOpen = true
                        }
                    }
                },
                onDoubleTapTrigger = {
                    when (gesturePrefs.doubleTapAction) {
                        DoubleTapAction.SEARCH -> isSearchOpen = true
                        DoubleTapAction.CONTROL_SURFACE -> isNotificationAndControlOpen = true
                        DoubleTapAction.APP_DISCOVERY -> isAppDrawerOpen = true
                        DoubleTapAction.NONE -> {}
                    }
                },
                onOpenSearch = { isSearchOpen = true },
                onOpenSettings = { isSettingsOpen = true },
                onOpenPersonalization = { isPersonalizationOpen = true },
                onOpenWidgetPicker = { isWidgetPickerOpen = true },
                onOpenNotificationAndControlSurface = { isNotificationAndControlOpen = true },
                onToggleEditMode = { enabled ->
                    isEditMode = enabled
                    if (!enabled) movingWorkspaceItem = null
                },
                onSelectItemForMove = { item -> movingWorkspaceItem = item },
                onMoveOrMergeItem = { itemId, targetPage, targetX, targetY ->
                    storageRepository.moveOrMergeWorkspaceItem(itemId, targetPage, targetX, targetY)
                    movingWorkspaceItem = null
                },
                onResizeWidget = { itemId, sx, sy ->
                    storageRepository.resizeWidgetOnWorkspace(itemId, sx, sy)
                },
                onRebindWidget = { item -> onRebindExistingWidget(item) },
                onRemoveWorkspaceItem = { itemId ->
                    val existing = homeLayout.items.firstOrNull { it.id == itemId }
                    if (existing?.appWidgetId != null && existing.appWidgetId >= 0) {
                        widgetHostManager.deleteWidgetId(existing.appWidgetId)
                    }
                    storageRepository.removeWorkspaceItem(itemId)
                },
                onAddPage = { storageRepository.addWorkspacePage() },
                onRemoveLastPage = { storageRepository.removeLastWorkspacePage() },
                onRequestDefaultHomeRole = onRequestDefaultHomeRole,
                onDismissDefaultHomeBanner = { defaultHomeBannerDismissed = true },
                onAcknowledgeCorruptionRecovery = { storageRepository.acknowledgeCorruptionRecovery() },
                onClearLaunchError = { packageRepository.clearLaunchError() },
                onRescanPackages = {
                    packageRepository.refreshInstalledAppsAsync(
                        usageLookup = { storageRepository.getUsageScore(it) },
                        lastUsedLookup = { storageRepository.getLastUsedTimestamp(it) },
                        notificationCountLookup = { AstraNotificationStreamBus.countForPackage(it) }
                    )
                }
            )

            // 2. OVERLAY: Modern Application Discovery Drawer
            AnimatedVisibility(
                visible = isAppDrawerOpen,
                enter = slideInVertically(initialOffsetY = { it / 3 }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it / 3 }) + fadeOut()
            ) {
                AstraAppDrawerOverlay(
                    installedApps = installedApps,
                    hiddenComponents = homeLayout.hiddenComponents,
                    favoriteComponents = homeLayout.favoriteComponents,
                    showCategories = perfPrefs.showDrawerCategories,
                    showRecentRow = perfPrefs.showDrawerRecentRow,
                    iconStyle = themeSettings.iconStyle,
                    iconScale = themeSettings.iconScale,
                    showLabels = themeSettings.showIconLabels,
                    palette = palette,
                    iconPipeline = iconPipeline,
                    onAppClick = { app -> launchRealApplication(app) },
                    onAppLongClick = { app -> contextMenuTarget = app to null },
                    onRescanPackages = {
                        packageRepository.refreshInstalledAppsAsync(
                            usageLookup = { storageRepository.getUsageScore(it) },
                            lastUsedLookup = { storageRepository.getLastUsedTimestamp(it) },
                            notificationCountLookup = { AstraNotificationStreamBus.countForPackage(it) }
                        )
                    },
                    onCloseDrawer = { isAppDrawerOpen = false }
                )
            }

            // 3. OVERLAY: System-Level Universal Search
            AnimatedVisibility(
                visible = isSearchOpen,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                AstraSearchOverlay(
                    installedApps = installedApps,
                    hiddenComponents = homeLayout.hiddenComponents,
                    searchPreferences = searchPrefs,
                    iconStyle = themeSettings.iconStyle,
                    palette = palette,
                    iconPipeline = iconPipeline,
                    onLaunchApp = { app -> launchRealApplication(app) },
                    onAppLongPress = { app -> contextMenuTarget = app to null },
                    onLaunchShortcut = { shortcut ->
                        packageRepository.launchAppShortcut(shortcut)
                        isSearchOpen = false
                    },
                    onExecuteSettingAction = { action ->
                        isSearchOpen = false
                        when (action) {
                            SearchSettingAction.WIFI -> systemController.openWifiSettings()
                            SearchSettingAction.BLUETOOTH -> systemController.openBluetoothSettings()
                            SearchSettingAction.DISPLAY -> systemController.openDisplaySettings()
                            SearchSettingAction.SOUND -> systemController.openSoundSettings()
                            SearchSettingAction.WALLPAPER -> { isPersonalizationOpen = true }
                            SearchSettingAction.DEFAULT_HOME -> onRequestDefaultHomeRole()
                            SearchSettingAction.NOTIFICATION_SHADE -> systemController.expandSystemNotificationShade()
                            SearchSettingAction.SYSTEM_SETTINGS -> systemController.openSystemSettings()
                        }
                    },
                    onExecuteCommandAction = { cmd ->
                        isSearchOpen = false
                        when (cmd) {
                            SearchCommandAction.OPEN_APP_DRAWER -> isAppDrawerOpen = true
                            SearchCommandAction.OPEN_WIDGET_PICKER -> isWidgetPickerOpen = true
                            SearchCommandAction.EDIT_HOME_WORKSPACE -> isEditMode = true
                            SearchCommandAction.OPEN_ASTRA_SETTINGS -> isSettingsOpen = true
                            SearchCommandAction.REFRESH_PACKAGES -> {
                                packageRepository.refreshInstalledAppsAsync(
                                    usageLookup = { storageRepository.getUsageScore(it) },
                                    lastUsedLookup = { storageRepository.getLastUsedTimestamp(it) },
                                    notificationCountLookup = { AstraNotificationStreamBus.countForPackage(it) }
                                )
                            }
                        }
                    },
                    onRecordQuery = { q ->
                        storageRepository.updateSearchPreferences { cur ->
                            cur.copy(recentQueries = (listOf(q) + cur.recentQueries.filterNot { it == q }).take(8))
                        }
                    },
                    onClose = { isSearchOpen = false }
                )
            }

            // 4. OVERLAY: Truthful Notification & Control Surface
            AnimatedVisibility(
                visible = isNotificationAndControlOpen,
                enter = slideInVertically(initialOffsetY = { -it / 4 }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it / 4 }) + fadeOut()
            ) {
                AstraNotificationAndControlOverlay(
                    deviceStatus = deviceStatus,
                    capabilities = capabilities,
                    notifications = activeNotifications,
                    palette = palette,
                    onOpenWifiPanel = { systemController.openWifiSettings() },
                    onOpenBluetoothSettings = { systemController.openBluetoothSettings() },
                    onOpenDisplaySettings = { systemController.openDisplaySettings() },
                    onOpenSoundSettings = { systemController.openSoundSettings() },
                    onExpandAndroidNotificationShade = {
                        isNotificationAndControlOpen = false
                        systemController.expandSystemNotificationShade()
                    },
                    onExpandAndroidQuickSettings = {
                        isNotificationAndControlOpen = false
                        systemController.expandSystemQuickSettings()
                    },
                    onOpenNotificationAccessSettings = { systemController.openNotificationListenerSettings() },
                    onClearNotifications = { AstraNotificationStreamBus.clearAllClearable() },
                    onClose = { isNotificationAndControlOpen = false }
                )
            }

            // 5. OVERLAY: Spatial Folder Expansion
            val activeFolder = remember(homeLayout.items, openFolderId) {
                openFolderId?.let { id -> homeLayout.items.firstOrNull { it.id == id } }
            }
            if (activeFolder != null) {
                AstraFolderOverlay(
                    folderItem = activeFolder,
                    installedApps = installedApps,
                    iconStyle = themeSettings.iconStyle,
                    palette = palette,
                    iconPipeline = iconPipeline,
                    onLaunchApp = { app -> launchRealApplication(app) },
                    onRenameFolder = { title -> storageRepository.renameFolder(activeFolder.id, title) },
                    onRemoveAppFromFolder = { comp ->
                        storageRepository.removeAppFromFolder(activeFolder.id, comp)
                    },
                    onReorderFolderItem = { from, to ->
                        storageRepository.reorderFolderItems(activeFolder.id, from, to)
                    },
                    onAddAppToFolder = { app ->
                        storageRepository.addAppToFolder(activeFolder.id, app)
                    },
                    onClose = { openFolderId = null }
                )
            }

            // 6. OVERLAY: Long-Press Application Context Menu
            contextMenuTarget?.let { (targetApp, wsItem) ->
                val isHidden = targetApp.componentName in homeLayout.hiddenComponents
                val isFavorite = targetApp.componentName in homeLayout.favoriteComponents
                AstraAppContextMenuSheet(
                    app = targetApp,
                    workspaceItem = wsItem,
                    isHidden = isHidden,
                    isFavorite = isFavorite,
                    palette = palette,
                    onOpenApp = { launchRealApplication(targetApp) },
                    onToggleFavorite = { storageRepository.toggleFavoriteApp(targetApp.componentName) },
                    onLaunchShortcut = { shortcut -> packageRepository.launchAppShortcut(shortcut) },
                    onPinToWorkspace = {
                        storageRepository.pinAppToWorkspace(targetApp)
                        isAppDrawerOpen = false
                    },
                    onStartMoveOnWorkspace = { item ->
                        movingWorkspaceItem = item
                        isEditMode = true
                    },
                    onPinToDockSlot0 = {
                        val nextSlot = homeLayout.dockItems.size.coerceAtMost(homeLayout.dockSlotCount - 1)
                        storageRepository.setDockSlot(nextSlot, targetApp)
                    },
                    onRemoveFromWorkspace = { id -> storageRepository.removeWorkspaceItem(id) },
                    onToggleHideApp = {
                        storageRepository.updateHomeLayout { cur ->
                            val next = if (isHidden) {
                                cur.hiddenComponents - targetApp.componentName
                            } else {
                                cur.hiddenComponents + targetApp.componentName
                            }
                            cur.copy(hiddenComponents = next)
                        }
                    },
                    onOpenAppInfo = { systemController.openAppInfo(targetApp.packageName) },
                    onUninstallApp = { systemController.requestUninstallApp(targetApp.packageName) },
                    onDismiss = { contextMenuTarget = null }
                )
            }

            // 7. OVERLAY: Android AppWidgetManager Widget Picker
            if (isWidgetPickerOpen) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = androidx.compose.ui.Alignment.BottomCenter
                ) {
                    AstraWidgetPickerSheet(
                        palette = palette,
                        widgetHostManager = widgetHostManager,
                        onSelectProvider = { provider ->
                            isWidgetPickerOpen = false
                            onInitiateWidgetAdd(provider)
                        },
                        onDismiss = { isWidgetPickerOpen = false }
                    )
                }
            }

            // 8. OVERLAY: Wallpaper & Personalization Studio
            AnimatedVisibility(
                visible = isPersonalizationOpen,
                enter = slideInVertically(initialOffsetY = { it / 4 }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it / 4 }) + fadeOut()
            ) {
                AstraPersonalizationOverlay(
                    themeSettings = themeSettings,
                    homeLayout = homeLayout,
                    palette = palette,
                    onApplyPreset = { preset -> storageRepository.applyThemePreset(preset) },
                    onSelectWallpaperSource = { src ->
                        storageRepository.updateThemeSettings { it.copy(wallpaperSource = src) }
                    },
                    onSelectWallpaper = { wp ->
                        storageRepository.updateThemeSettings { it.copy(wallpaperId = wp) }
                    },
                    onApplyWallpaperToAndroidSystem = { wp ->
                        systemController.applySystemWallpaperResource(wp.toDrawableResId())
                    },
                    onOpenSystemWallpaperPicker = {
                        systemController.openSystemWallpaperPicker()
                    },
                    onSelectClockStyle = { cs ->
                        storageRepository.updateThemeSettings { it.copy(clockStyle = cs) }
                    },
                    onSelectIconStyle = { style ->
                        storageRepository.updateThemeSettings { it.copy(iconStyle = style) }
                    },
                    onUpdateIconScale = { scale ->
                        storageRepository.updateThemeSettings { it.copy(iconScale = scale) }
                    },
                    onToggleIconLabels = { show ->
                        storageRepository.updateThemeSettings { it.copy(showIconLabels = show) }
                    },
                    onToggleDockGlass = { glass ->
                        storageRepository.updateThemeSettings { it.copy(dockStyleGlass = glass) }
                    },
                    onUpdateGridSize = { cols, rows ->
                        storageRepository.updateHomeLayout { it.copy(gridColumns = cols, gridRows = rows) }
                    },
                    onClose = { isPersonalizationOpen = false }
                )
            }

            // 9. OVERLAY: Dedicated Astra Settings Surface
            AnimatedVisibility(
                visible = isSettingsOpen,
                enter = slideInVertically(initialOffsetY = { it / 4 }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it / 4 }) + fadeOut()
            ) {
                AstraSettingsOverlay(
                    themeSettings = themeSettings,
                    homeLayout = homeLayout,
                    searchPreferences = searchPrefs,
                    notificationPreferences = notifPrefs,
                    gesturePreferences = gesturePrefs,
                    performancePreferences = perfPrefs,
                    capabilities = capabilities,
                    installedApps = installedApps,
                    palette = palette,
                    onUpdateTheme = { storageRepository.updateThemeSettings(it) },
                    onUpdateHomeLayout = { storageRepository.updateHomeLayout(it) },
                    onUpdateSearch = { storageRepository.updateSearchPreferences(it) },
                    onUpdateNotifications = { storageRepository.updateNotificationPreferences(it) },
                    onUpdateGestures = { storageRepository.updateGesturePreferences(it) },
                    onUpdatePerformance = { storageRepository.updatePerformancePreferences(it) },
                    onRequestDefaultHomeRole = onRequestDefaultHomeRole,
                    onOpenNotificationAccessSettings = { systemController.openNotificationListenerSettings() },
                    onOpenWidgetPicker = {
                        isSettingsOpen = false
                        isWidgetPickerOpen = true
                    },
                    onOpenPersonalizationStudio = {
                        isSettingsOpen = false
                        isPersonalizationOpen = true
                    },
                    onReopenFirstRunSetup = {
                        storageRepository.reopenFirstRunSetup()
                    },
                    onRescanPackages = {
                        packageRepository.refreshInstalledAppsAsync(
                            usageLookup = { storageRepository.getUsageScore(it) },
                            lastUsedLookup = { storageRepository.getLastUsedTimestamp(it) },
                            notificationCountLookup = { AstraNotificationStreamBus.countForPackage(it) }
                        )
                    },
                    onResetDefaults = { storageRepository.resetToSafeDefaults() },
                    onClose = { isSettingsOpen = false }
                )
            }

            // 10. FIRST-RUN SETUP EXPERIENCE ("Your phone, redesigned.")
            AnimatedVisibility(
                visible = !homeLayout.hasCompletedFirstRunSetup,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                AstraFirstRunSetupOverlay(
                    initialPreset = themeSettings.themePreset,
                    initialDensity = homeLayout.densityMode,
                    initialIconStyle = themeSettings.iconStyle,
                    initialSwipeDown = gesturePrefs.swipeDownAction,
                    capabilities = capabilities,
                    palette = palette,
                    onRequestDefaultHomeRole = onRequestDefaultHomeRole,
                    onOpenNotificationAccessSettings = { systemController.openNotificationListenerSettings() },
                    onCompleteSetup = { preset, density, iconStyle, swipeDown ->
                        storageRepository.completeFirstRunSetup(
                            preset = preset,
                            densityMode = density,
                            iconStyle = iconStyle,
                            swipeDownAction = swipeDown,
                            installedApps = installedApps
                        )
                    }
                )
            }
        }
    }
}
