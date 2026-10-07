package com.astra.launcher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import com.astra.launcher.core.design.AstraHapticCue
import com.astra.launcher.core.design.AstraHaptics
import com.astra.launcher.core.design.AstraMotion
import com.astra.launcher.core.design.AstraSoundCue
import com.astra.launcher.core.design.AstraSoundEngine
import com.astra.launcher.core.design.AstraStatusBar
import com.astra.launcher.core.design.AstraThemeProvider
import com.astra.launcher.core.design.AstraWallpaperSurface
import com.astra.launcher.core.performance.AstraPerformanceManager
import com.astra.launcher.core.platform.AstraCapabilityMatrix
import com.astra.launcher.core.platform.AstraNotificationBridge
import com.astra.launcher.core.platform.AstraPackageRepository
import com.astra.launcher.core.platform.AstraRoleManager
import com.astra.launcher.core.platform.AstraSystemController
import com.astra.launcher.core.platform.AstraWidgetHostManager
import com.astra.launcher.core.storage.AstraCanonicalFrame
import com.astra.launcher.core.storage.AstraClockStyle
import com.astra.launcher.core.storage.AstraThemeMode
import com.astra.launcher.core.storage.AstraThemePreset
import com.astra.launcher.core.storage.AstraWallpaperId
import com.astra.launcher.core.storage.AstraWidgetPlacement
import com.astra.launcher.core.storage.AstraWidgetType
import com.astra.launcher.core.storage.NotificationPrivacyMode
import com.astra.launcher.core.storage.AstraStorageRepository
import com.astra.launcher.feature.apps.AstraAppDrawerScreen
import com.astra.launcher.feature.controls.AstraControlCenterScreen
import com.astra.launcher.feature.home.AstraHomeScreen
import com.astra.launcher.feature.home.SystemNonHappyState
import com.astra.launcher.feature.lockpreview.AstraAodScreen
import com.astra.launcher.feature.lockpreview.AstraChargingOverlay
import com.astra.launcher.feature.lockpreview.AstraLockScreen
import com.astra.launcher.feature.notifications.AstraNotificationShadeScreen
import com.astra.launcher.feature.onboarding.AstraOnboardingScreen
import com.astra.launcher.feature.personalization.AstraPersonalizationStudioScreen
import com.astra.launcher.feature.personalization.StudioSubTab
import com.astra.launcher.feature.recents.AstraRecentsScreen
import com.astra.launcher.feature.search.AstraSearchScreen
import com.astra.launcher.feature.search.SearchCommandActionType
import com.astra.launcher.feature.settings.AstraSettingsScreen
import com.astra.launcher.feature.settings.AstraSettingsSection
import com.astra.launcher.feature.widgets.AstraWidgetPickerSheet

/**
 * Spatial Surface Continuum States (Section 1 & Section 52).
 * Lock -> Unlock -> Home -> Search -> Launch -> Back -> Home -> Notifications ->
 * Control Center -> Recents -> Settings -> Personalize -> Home
 */
enum class AstraActiveSurface {
    ONBOARDING,
    LOCK_SCREEN,
    AOD,
    HOME,
    APP_DRAWER,
    SEARCH,
    NOTIFICATIONS,
    CONTROL_CENTER,
    RECENTS,
    PERSONALIZATION_STUDIO,
    WIDGET_PICKER,
    SETTINGS
}

class AstraLauncherActivity : ComponentActivity() {

    private lateinit var storageRepository: AstraStorageRepository
    private lateinit var packageRepository: AstraPackageRepository
    private lateinit var systemController: AstraSystemController
    private lateinit var performanceManager: AstraPerformanceManager
    private lateinit var widgetHostManager: AstraWidgetHostManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        storageRepository = AstraStorageRepository(applicationContext)
        packageRepository = AstraPackageRepository(applicationContext)
        systemController = AstraSystemController(applicationContext)
        performanceManager = AstraPerformanceManager(applicationContext)
        widgetHostManager = AstraWidgetHostManager(applicationContext)

        packageRepository.refreshInstalledApps()
        widgetHostManager.startListening()
        performanceManager.markHomeFirstFrameRendered()

        setContent {
            AstraLauncherRootShell(
                activity = this,
                storageRepository = storageRepository,
                packageRepository = packageRepository,
                systemController = systemController,
                performanceManager = performanceManager,
                widgetHostManager = widgetHostManager
            )
        }
    }

    override fun onResume() {
        super.onResume()
        if (::packageRepository.isInitialized) {
            packageRepository.refreshInstalledApps()
        }
        if (::systemController.isInitialized) {
            systemController.refreshDeviceTelemetry()
        }
    }

    override fun onDestroy() {
        if (::widgetHostManager.isInitialized) {
            widgetHostManager.stopListening()
        }
        super.onDestroy()
    }
}

@Composable
fun AstraLauncherRootShell(
    activity: ComponentActivity,
    storageRepository: AstraStorageRepository,
    packageRepository: AstraPackageRepository,
    systemController: AstraSystemController,
    performanceManager: AstraPerformanceManager,
    widgetHostManager: AstraWidgetHostManager
) {
    val view = LocalView.current
    val themeSettings by storageRepository.themeSettings.collectAsState()
    val homeLayout by storageRepository.homeLayout.collectAsState()
    val searchPrefs by storageRepository.searchPreferences.collectAsState()
    val notifPrefs by storageRepository.notificationPreferences.collectAsState()
    val gesturePrefs by storageRepository.gesturePreferences.collectAsState()
    val perfPrefs by storageRepository.performancePreferences.collectAsState()
    val onboardingDone by storageRepository.onboardingCompleted.collectAsState()

    val installedApps by packageRepository.installedApps.collectAsState()
    val deviceStatus by systemController.deviceStatus.collectAsState()
    val mediaState by systemController.mediaState.collectAsState()
    val lastControlFeedback by systemController.lastFeedback.collectAsState()
    val notifications by AstraNotificationBridge.notifications.collectAsState()

    var activeSurface by remember {
        mutableStateOf(
            if (onboardingDone) AstraActiveSurface.HOME else AstraActiveSurface.ONBOARDING
        )
    }

    var searchQuery by remember { mutableStateOf("") }
    var homeEditModeInitial by remember { mutableStateOf(false) }
    var notifCollapsedInitial by remember { mutableStateOf(false) }
    var controlsPartialInitial by remember { mutableStateOf(false) }
    var studioInitialTab by remember { mutableStateOf(StudioSubTab.PRESETS) }
    var settingsInitialDetail by remember { mutableStateOf<AstraSettingsSection?>(null) }
    var onboardingInitialStep by remember { mutableIntStateOf(1) }
    var nonHappyState by remember { mutableStateOf(SystemNonHappyState.NONE) }
    var showChargingMoment by remember { mutableStateOf(ChargingEventBus.isPlugInMomentActive) }

    val capabilityReport = remember(activeSurface) {
        AstraCapabilityMatrix.inspect(activity)
    }

    val visualBudget = remember(perfPrefs, deviceStatus.isLowBattery) {
        performanceManager.evaluateBudget(perfPrefs, deviceStatus.isLowBattery)
    }

    // Pressing Back on any overlay surface smoothly returns to Home (Section 51 & 52)
    BackHandler(enabled = activeSurface != AstraActiveSurface.HOME) {
        AstraHaptics.perform(view, AstraHapticCue.LIGHT, gesturePrefs.hapticsEnabled)
        activeSurface = AstraActiveSurface.HOME
    }

    val dimAmount = when (activeSurface) {
        AstraActiveSurface.SEARCH -> 0.42f
        AstraActiveSurface.APP_DRAWER -> 0.34f
        AstraActiveSurface.NOTIFICATIONS -> 0.48f
        AstraActiveSurface.CONTROL_CENTER -> 0.48f
        AstraActiveSurface.RECENTS -> 0.52f
        AstraActiveSurface.PERSONALIZATION_STUDIO -> 0.56f
        AstraActiveSurface.WIDGET_PICKER -> 0.56f
        else -> 0f
    }

    AstraThemeProvider(
        themeSettings = themeSettings,
        visualBudget = visualBudget
    ) {
        if (activeSurface == AstraActiveSurface.AOD) {
            AstraAodScreen(
                clockStyle = themeSettings.clockStyle,
                batteryPercent = deviceStatus.batteryPercent,
                urgentNotificationCount = notifications.size,
                onWakeToLock = { activeSurface = AstraActiveSurface.LOCK_SCREEN },
                onWakeToHome = {
                    AstraSoundEngine.playCue(AstraSoundCue.UNLOCK, gesturePrefs.soundEffectsEnabled)
                    activeSurface = AstraActiveSurface.HOME
                }
            )
        } else {
            AstraWallpaperSurface(
                wallpaperId = themeSettings.wallpaperId,
                dimAmount = dimAmount,
                enableAtmosphericOverlay = visualBudget.enableAtmosphericShaderOverlay
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Persistent Adaptive Status Bar (Section 19)
                    AstraStatusBar(
                        batteryPercent = deviceStatus.batteryPercent,
                        isCharging = deviceStatus.isCharging,
                        isOffline = deviceStatus.isOffline,
                        micActive = deviceStatus.micActiveIndicator,
                        cameraActive = deviceStatus.cameraActiveIndicator,
                        locationActive = deviceStatus.locationActiveIndicator,
                        hasNotificationAccess = capabilityReport.hasNotificationAccess,
                        onStatusBarTap = {
                            AstraHaptics.perform(view, AstraHapticCue.LIGHT, gesturePrefs.hapticsEnabled)
                            activeSurface = if (activeSurface == AstraActiveSurface.NOTIFICATIONS) {
                                AstraActiveSurface.CONTROL_CENTER
                            } else {
                                AstraActiveSurface.NOTIFICATIONS
                            }
                        }
                    )

                    // Optional Charging Moment Banner (Section 27 & Frame 29)
                    if (showChargingMoment || deviceStatus.isCharging) {
                        Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
                            AstraChargingOverlay(
                                batteryPercent = deviceStatus.batteryPercent,
                                onDismiss = {
                                    showChargingMoment = false
                                    ChargingEventBus.isPlugInMomentActive = false
                                    systemController.setSimulatedSystemState(isCharging = false)
                                }
                            )
                        }
                    }

                    // Spatial Continuum Animated Surface Host (Section 30)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        AnimatedContent(
                            targetState = activeSurface,
                            transitionSpec = {
                                fadeIn(AstraMotion.spec(AstraMotion.STANDARD_MS, visualBudget.motionDurationScale)) togetherWith
                                    fadeOut(AstraMotion.spec(AstraMotion.QUICK_MS, visualBudget.motionDurationScale))
                            },
                            label = "AstraSurfaceTransition"
                        ) { surface ->
                            when (surface) {
                                AstraActiveSurface.ONBOARDING -> {
                                    AstraOnboardingScreen(
                                        themeSettings = themeSettings,
                                        isDefaultHome = capabilityReport.isCurrentlyDefaultHome,
                                        initialStep = onboardingInitialStep,
                                        onApplyPreset = { storageRepository.applyThemePreset(it) },
                                        onSelectClock = { c ->
                                            storageRepository.updateThemeSettings { it.copy(clockStyle = c) }
                                        },
                                        onRequestDefaultHomeRole = {
                                            AstraRoleManager.openDefaultHomeSelector(activity)
                                        },
                                        onRequestNotificationAccess = {
                                            systemController.openNotificationListenerSettings()
                                        },
                                        onCompleteOnboarding = {
                                            storageRepository.setOnboardingCompleted(true)
                                            activeSurface = AstraActiveSurface.HOME
                                        }
                                    )
                                }

                                AstraActiveSurface.LOCK_SCREEN -> {
                                    AstraLockScreen(
                                        wallpaperId = themeSettings.wallpaperId,
                                        clockStyle = themeSettings.clockStyle,
                                        notifications = notifications,
                                        deviceStatus = deviceStatus,
                                        hideSensitiveOnLock = notifPrefs.privacyMode == NotificationPrivacyMode.HIDE_SENSITIVE_ON_LOCK,
                                        onSelectClockStyle = { c ->
                                            storageRepository.updateThemeSettings { it.copy(clockStyle = c) }
                                        },
                                        onToggleTorch = { systemController.toggleFlashlight() },
                                        onOpenCamera = { packageRepository.launchApp("com.android.camera2") },
                                        onSwitchToAod = { activeSurface = AstraActiveSurface.AOD },
                                        onUnlockToHome = {
                                            AstraHaptics.perform(view, AstraHapticCue.MEDIUM, gesturePrefs.hapticsEnabled)
                                            AstraSoundEngine.playCue(AstraSoundCue.UNLOCK, gesturePrefs.soundEffectsEnabled)
                                            activeSurface = AstraActiveSurface.HOME
                                        }
                                    )
                                }

                                AstraActiveSurface.HOME -> {
                                    AstraHomeScreen(
                                        apps = installedApps,
                                        themeSettings = themeSettings,
                                        homeLayout = homeLayout,
                                        deviceStatus = deviceStatus,
                                        mediaState = mediaState,
                                        isDefaultHome = capabilityReport.isCurrentlyDefaultHome,
                                        initialEditMode = homeEditModeInitial,
                                        nonHappyState = nonHappyState,
                                        onClearNonHappyState = { nonHappyState = SystemNonHappyState.NONE },
                                        onLaunchApp = { app ->
                                            AstraHaptics.perform(view, AstraHapticCue.MEDIUM, gesturePrefs.hapticsEnabled)
                                            packageRepository.launchApp(app.packageName)
                                        },
                                        onOpenSearch = {
                                            searchQuery = ""
                                            activeSurface = AstraActiveSurface.SEARCH
                                        },
                                        onOpenAppDrawer = { activeSurface = AstraActiveSurface.APP_DRAWER },
                                        onOpenNotifications = {
                                            notifCollapsedInitial = false
                                            activeSurface = AstraActiveSurface.NOTIFICATIONS
                                        },
                                        onOpenControlCenter = {
                                            controlsPartialInitial = false
                                            activeSurface = AstraActiveSurface.CONTROL_CENTER
                                        },
                                        onOpenRecents = { activeSurface = AstraActiveSurface.RECENTS },
                                        onOpenPersonalization = {
                                            studioInitialTab = StudioSubTab.PRESETS
                                            activeSurface = AstraActiveSurface.PERSONALIZATION_STUDIO
                                        },
                                        onOpenWidgetPicker = { activeSurface = AstraActiveSurface.WIDGET_PICKER },
                                        onOpenLockScreen = { activeSurface = AstraActiveSurface.LOCK_SCREEN },
                                        onOpenSettings = {
                                            settingsInitialDetail = null
                                            activeSurface = AstraActiveSurface.SETTINGS
                                        },
                                        onRequestDefaultHomeRole = {
                                            AstraRoleManager.openDefaultHomeSelector(activity)
                                        },
                                        onApplyPreset = { storageRepository.applyThemePreset(it) },
                                        onUpdateTheme = { storageRepository.updateThemeSettings(it) },
                                        onUpdateHome = { storageRepository.updateHomeLayout(it) },
                                        onOpenAppInfo = { packageRepository.openAppInfo(it) },
                                        onUninstallApp = { packageRepository.requestUninstall(it) },
                                        onResetSafeMode = { storageRepository.resetToSafeDefaults() }
                                    )
                                }

                                AstraActiveSurface.APP_DRAWER -> {
                                    AstraAppDrawerScreen(
                                        apps = installedApps,
                                        favoritePackages = homeLayout.favoritePackages,
                                        hiddenPackages = homeLayout.hiddenPackages,
                                        privateUnlocked = homeLayout.privateSpaceUnlocked,
                                        iconStyle = themeSettings.iconStyle,
                                        iconScale = themeSettings.iconScale,
                                        showLabels = themeSettings.showIconLabels,
                                        onLaunchApp = { app ->
                                            AstraHaptics.perform(view, AstraHapticCue.MEDIUM, gesturePrefs.hapticsEnabled)
                                            packageRepository.launchApp(app.packageName)
                                            activeSurface = AstraActiveSurface.HOME
                                        },
                                        onLongPressApp = { app ->
                                            packageRepository.openAppInfo(app.packageName)
                                        },
                                        onTogglePrivateLock = {
                                            storageRepository.updateHomeLayout {
                                                it.copy(privateSpaceUnlocked = !it.privateSpaceUnlocked)
                                            }
                                        },
                                        onClose = { activeSurface = AstraActiveSurface.HOME }
                                    )
                                }

                                AstraActiveSurface.SEARCH -> {
                                    AstraSearchScreen(
                                        query = searchQuery,
                                        onQueryChange = { searchQuery = it },
                                        apps = installedApps,
                                        hiddenPackages = homeLayout.hiddenPackages,
                                        recentQueries = searchPrefs.recentQueries,
                                        iconStyle = themeSettings.iconStyle,
                                        onLaunchApp = { app ->
                                            AstraHaptics.perform(view, AstraHapticCue.MEDIUM, gesturePrefs.hapticsEnabled)
                                            packageRepository.launchApp(app.packageName)
                                            activeSurface = AstraActiveSurface.HOME
                                        },
                                        onExecuteCommand = { cmd ->
                                            AstraHaptics.perform(view, AstraHapticCue.MEDIUM, gesturePrefs.hapticsEnabled)
                                            when (cmd.actionType) {
                                                SearchCommandActionType.OPEN_APP,
                                                SearchCommandActionType.DIAL_CONTACT,
                                                SearchCommandActionType.START_TIMER -> {
                                                    packageRepository.launchApp(cmd.targetPayload)
                                                    activeSurface = AstraActiveSurface.HOME
                                                }
                                                SearchCommandActionType.SYSTEM_TILE -> {
                                                    if (cmd.targetPayload == "torch") {
                                                        systemController.toggleFlashlight()
                                                    } else {
                                                        systemController.handleSystemTileTap(cmd.targetPayload)
                                                    }
                                                    activeSurface = AstraActiveSurface.CONTROL_CENTER
                                                }
                                                SearchCommandActionType.OPEN_SETTINGS,
                                                SearchCommandActionType.CALCULATOR -> {
                                                    activeSurface = AstraActiveSurface.SETTINGS
                                                }
                                            }
                                        },
                                        onSelectSetting = { key ->
                                            when (key) {
                                                "personalization" -> activeSurface = AstraActiveSurface.PERSONALIZATION_STUDIO
                                                "notifications" -> activeSurface = AstraActiveSurface.NOTIFICATIONS
                                                "home_role" -> AstraRoleManager.openDefaultHomeSelector(activity)
                                                else -> activeSurface = AstraActiveSurface.CONTROL_CENTER
                                            }
                                        },
                                        onDismiss = { activeSurface = AstraActiveSurface.HOME }
                                    )
                                }

                                AstraActiveSurface.NOTIFICATIONS -> {
                                    AstraNotificationShadeScreen(
                                        notifications = notifications,
                                        hasNotificationListenerPermission = capabilityReport.hasNotificationAccess,
                                        hideSensitiveContent = false,
                                        initiallyCollapsed = notifCollapsedInitial,
                                        onOpenNotificationApp = { pkg ->
                                            packageRepository.launchApp(pkg)
                                            activeSurface = AstraActiveSurface.HOME
                                        },
                                        onDismissNotification = { id ->
                                            AstraNotificationBridge.dismissNotification(id)
                                        },
                                        onClearAll = {
                                            AstraNotificationBridge.clearAllClearable()
                                        },
                                        onRestoreSampleNotifications = {
                                            AstraNotificationBridge.restoreSampleNotifications()
                                        },
                                        onRequestNotificationAccess = {
                                            systemController.openNotificationListenerSettings()
                                        },
                                        onOpenControlCenter = {
                                            activeSurface = AstraActiveSurface.CONTROL_CENTER
                                        },
                                        onClose = { activeSurface = AstraActiveSurface.HOME }
                                    )
                                }

                                AstraActiveSurface.CONTROL_CENTER -> {
                                    AstraControlCenterScreen(
                                        deviceStatus = deviceStatus,
                                        mediaState = mediaState,
                                        compactDensity = themeSettings.controlCenterCompact,
                                        initiallyPartial = controlsPartialInitial,
                                        lastFeedback = lastControlFeedback,
                                        onToggleTorch = {
                                            AstraHaptics.perform(view, AstraHapticCue.LIGHT, gesturePrefs.hapticsEnabled)
                                            systemController.toggleFlashlight()
                                        },
                                        onVolumeChange = { systemController.setVolumeFraction(it) },
                                        onBrightnessChange = { systemController.setBrightnessFraction(it) },
                                        onSystemTileTap = { tileId ->
                                            AstraHaptics.perform(view, AstraHapticCue.LIGHT, gesturePrefs.hapticsEnabled)
                                            systemController.handleSystemTileTap(tileId)
                                        },
                                        onMediaPlayPause = { systemController.toggleMediaPlayPause() },
                                        onMediaNext = { systemController.skipMediaNext() },
                                        onOpenNotifications = { activeSurface = AstraActiveSurface.NOTIFICATIONS },
                                        onOpenSettings = { activeSurface = AstraActiveSurface.SETTINGS },
                                        onClose = { activeSurface = AstraActiveSurface.HOME }
                                    )
                                }

                                AstraActiveSurface.RECENTS -> {
                                    AstraRecentsScreen(
                                        onRestoreTask = { pkg ->
                                            packageRepository.launchApp(pkg)
                                            activeSurface = AstraActiveSurface.HOME
                                        },
                                        onOpenAppInfo = { pkg -> packageRepository.openAppInfo(pkg) },
                                        onClose = { activeSurface = AstraActiveSurface.HOME }
                                    )
                                }

                                AstraActiveSurface.PERSONALIZATION_STUDIO -> {
                                    AstraPersonalizationStudioScreen(
                                        themeSettings = themeSettings,
                                        initialTab = studioInitialTab,
                                        onApplyPreset = { storageRepository.applyThemePreset(it) },
                                        onUpdateTheme = { storageRepository.updateThemeSettings(it) },
                                        onClose = { activeSurface = AstraActiveSurface.HOME }
                                    )
                                }

                                AstraActiveSurface.WIDGET_PICKER -> {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(18.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        AstraWidgetPickerSheet(
                                            activeWidgets = homeLayout.widgets,
                                            installedThirdPartyCount = widgetHostManager.getInstalledSystemWidgetProviders().size,
                                            onAddWidget = { type ->
                                                val newId = (homeLayout.widgets.maxOfOrNull { it.instanceId } ?: 1000) + 1
                                                val allocatedHostId = if (type == AstraWidgetType.ANDROID_HOSTED) {
                                                    widgetHostManager.allocateWidgetId()
                                                } else -1
                                                storageRepository.updateHomeLayout { cur ->
                                                    cur.copy(
                                                        widgets = cur.widgets + AstraWidgetPlacement(
                                                            instanceId = newId,
                                                            widgetType = type,
                                                            appWidgetId = allocatedHostId,
                                                            pageIndex = 0
                                                        )
                                                    )
                                                }
                                                activeSurface = AstraActiveSurface.HOME
                                            },
                                            onRemoveWidget = { instanceId ->
                                                storageRepository.updateHomeLayout { cur ->
                                                    cur.copy(widgets = cur.widgets.filterNot { it.instanceId == instanceId })
                                                }
                                            },
                                            onClose = { activeSurface = AstraActiveSurface.HOME }
                                        )
                                    }
                                }

                                AstraActiveSurface.SETTINGS -> {
                                    AstraSettingsScreen(
                                        themeSettings = themeSettings,
                                        homeLayout = homeLayout,
                                        searchPreferences = searchPrefs,
                                        notificationPreferences = notifPrefs,
                                        gesturePreferences = gesturePrefs,
                                        performancePreferences = perfPrefs,
                                        capabilityReport = capabilityReport,
                                        deviceStatus = deviceStatus,
                                        visualBudget = visualBudget,
                                        initialDetailSection = settingsInitialDetail,
                                        onUpdateTheme = { storageRepository.updateThemeSettings(it) },
                                        onUpdateHome = { storageRepository.updateHomeLayout(it) },
                                        onUpdateSearch = { storageRepository.updateSearchPreferences(it) },
                                        onUpdateNotifications = { storageRepository.updateNotificationPreferences(it) },
                                        onUpdateGestures = { storageRepository.updateGesturePreferences(it) },
                                        onUpdatePerformance = { storageRepository.updatePerformancePreferences(it) },
                                        onRequestDefaultHomeRole = {
                                            AstraRoleManager.openDefaultHomeSelector(activity)
                                        },
                                        onRequestNotificationAccess = {
                                            systemController.openNotificationListenerSettings()
                                        },
                                        onOpenPersonalizationStudio = {
                                            studioInitialTab = StudioSubTab.PRESETS
                                            activeSurface = AstraActiveSurface.PERSONALIZATION_STUDIO
                                        },
                                        onOpenWidgetPicker = { activeSurface = AstraActiveSurface.WIDGET_PICKER },
                                        onOpenLockPreview = { activeSurface = AstraActiveSurface.LOCK_SCREEN },
                                        onSimulateSystemState = { charging, lowBatt, offline, camMic ->
                                            systemController.setSimulatedSystemState(
                                                isCharging = charging,
                                                isLowBattery = lowBatt,
                                                isOffline = offline,
                                                cameraActive = camMic,
                                                micActive = camMic
                                            )
                                        },
                                        onJumpToCanonicalFrame = { frame ->
                                            // Jump directly to any of the 40 canonical visual frames (Section 45)
                                            when (frame) {
                                                AstraCanonicalFrame.F01_SPLASH,
                                                AstraCanonicalFrame.F02_ONBOARDING_01 -> {
                                                    onboardingInitialStep = 1
                                                    activeSurface = AstraActiveSurface.ONBOARDING
                                                }
                                                AstraCanonicalFrame.F03_ONBOARDING_02 -> {
                                                    onboardingInitialStep = 3
                                                    activeSurface = AstraActiveSurface.ONBOARDING
                                                }
                                                AstraCanonicalFrame.F04_HOME_DARK -> {
                                                    storageRepository.applyThemePreset(AstraThemePreset.ASTRAL)
                                                    homeEditModeInitial = false
                                                    nonHappyState = SystemNonHappyState.NONE
                                                    activeSurface = AstraActiveSurface.HOME
                                                }
                                                AstraCanonicalFrame.F05_HOME_LIGHT -> {
                                                    storageRepository.applyThemePreset(AstraThemePreset.MORNING)
                                                    homeEditModeInitial = false
                                                    activeSurface = AstraActiveSurface.HOME
                                                }
                                                AstraCanonicalFrame.F06_HOME_PERSONALIZED -> {
                                                    storageRepository.applyThemePreset(AstraThemePreset.NOCTURNE)
                                                    storageRepository.updateThemeSettings {
                                                        it.copy(clockStyle = AstraClockStyle.ORBITAL_COMPACT)
                                                    }
                                                    homeEditModeInitial = false
                                                    activeSurface = AstraActiveSurface.HOME
                                                }
                                                AstraCanonicalFrame.F07_HOME_EDIT_MODE -> {
                                                    homeEditModeInitial = true
                                                    activeSurface = AstraActiveSurface.HOME
                                                }
                                                AstraCanonicalFrame.F08_APP_DRAWER -> {
                                                    activeSurface = AstraActiveSurface.APP_DRAWER
                                                }
                                                AstraCanonicalFrame.F09_SEARCH_IDLE -> {
                                                    searchQuery = ""
                                                    activeSurface = AstraActiveSurface.SEARCH
                                                }
                                                AstraCanonicalFrame.F10_SEARCH_TYPING,
                                                AstraCanonicalFrame.F11_SEARCH_SELECTED -> {
                                                    searchQuery = "Cam"
                                                    activeSurface = AstraActiveSurface.SEARCH
                                                }
                                                AstraCanonicalFrame.F12_SEARCH_COMMAND -> {
                                                    searchQuery = "open YouTube"
                                                    activeSurface = AstraActiveSurface.SEARCH
                                                }
                                                AstraCanonicalFrame.F13_NOTIFICATIONS_COLLAPSED -> {
                                                    notifCollapsedInitial = true
                                                    activeSurface = AstraActiveSurface.NOTIFICATIONS
                                                }
                                                AstraCanonicalFrame.F14_NOTIFICATIONS_EXPANDED,
                                                AstraCanonicalFrame.F25_PERMISSION_PRE_EXPLAIN,
                                                AstraCanonicalFrame.F26_PERMISSION_HANDOFF,
                                                AstraCanonicalFrame.F27_PERMISSION_DENIED -> {
                                                    notifCollapsedInitial = false
                                                    activeSurface = AstraActiveSurface.NOTIFICATIONS
                                                }
                                                AstraCanonicalFrame.F15_CONTROL_CENTER_PARTIAL -> {
                                                    controlsPartialInitial = true
                                                    activeSurface = AstraActiveSurface.CONTROL_CENTER
                                                }
                                                AstraCanonicalFrame.F16_CONTROL_CENTER_FULL,
                                                AstraCanonicalFrame.F28_MEDIA_CONTROLS -> {
                                                    controlsPartialInitial = false
                                                    activeSurface = AstraActiveSurface.CONTROL_CENTER
                                                }
                                                AstraCanonicalFrame.F17_RECENTS_OVERVIEW -> {
                                                    activeSurface = AstraActiveSurface.RECENTS
                                                }
                                                AstraCanonicalFrame.F18_SETTINGS_ROOT -> {
                                                    settingsInitialDetail = null
                                                    activeSurface = AstraActiveSurface.SETTINGS
                                                }
                                                AstraCanonicalFrame.F19_SETTINGS_DETAIL -> {
                                                    settingsInitialDetail = AstraSettingsSection.PERFORMANCE
                                                    activeSurface = AstraActiveSurface.SETTINGS
                                                }
                                                AstraCanonicalFrame.F20_PERSONALIZATION_STUDIO -> {
                                                    studioInitialTab = StudioSubTab.PRESETS
                                                    activeSurface = AstraActiveSurface.PERSONALIZATION_STUDIO
                                                }
                                                AstraCanonicalFrame.F21_WALLPAPER_PICKER -> {
                                                    studioInitialTab = StudioSubTab.WALLPAPERS
                                                    activeSurface = AstraActiveSurface.PERSONALIZATION_STUDIO
                                                }
                                                AstraCanonicalFrame.F22_CLOCK_PICKER -> {
                                                    studioInitialTab = StudioSubTab.CLOCKS
                                                    activeSurface = AstraActiveSurface.PERSONALIZATION_STUDIO
                                                }
                                                AstraCanonicalFrame.F23_WIDGET_PICKER -> {
                                                    activeSurface = AstraActiveSurface.WIDGET_PICKER
                                                }
                                                AstraCanonicalFrame.F24_ICON_STYLE_PICKER -> {
                                                    studioInitialTab = StudioSubTab.ICONS
                                                    activeSurface = AstraActiveSurface.PERSONALIZATION_STUDIO
                                                }
                                                AstraCanonicalFrame.F29_CHARGING -> {
                                                    showChargingMoment = true
                                                    systemController.setSimulatedSystemState(isCharging = true)
                                                    activeSurface = AstraActiveSurface.HOME
                                                }
                                                AstraCanonicalFrame.F30_LOW_BATTERY -> {
                                                    systemController.setSimulatedSystemState(isLowBattery = true)
                                                    activeSurface = AstraActiveSurface.HOME
                                                }
                                                AstraCanonicalFrame.F31_OFFLINE -> {
                                                    systemController.setSimulatedSystemState(isOffline = true)
                                                    activeSurface = AstraActiveSurface.HOME
                                                }
                                                AstraCanonicalFrame.F32_LOADING -> {
                                                    nonHappyState = SystemNonHappyState.LOADING_SKELETON
                                                    activeSurface = AstraActiveSurface.HOME
                                                }
                                                AstraCanonicalFrame.F33_EMPTY_STATE -> {
                                                    nonHappyState = SystemNonHappyState.EMPTY_HOME
                                                    activeSurface = AstraActiveSurface.HOME
                                                }
                                                AstraCanonicalFrame.F34_ERROR_STATE -> {
                                                    nonHappyState = SystemNonHappyState.ERROR_STATE
                                                    activeSurface = AstraActiveSurface.HOME
                                                }
                                                AstraCanonicalFrame.F35_RECOVERY_STATE -> {
                                                    nonHappyState = SystemNonHappyState.RECOVERY_SAFE_MODE
                                                    activeSurface = AstraActiveSurface.HOME
                                                }
                                                AstraCanonicalFrame.F36_LOCK_SCREEN -> {
                                                    activeSurface = AstraActiveSurface.LOCK_SCREEN
                                                }
                                                AstraCanonicalFrame.F37_AOD_CONCEPT -> {
                                                    activeSurface = AstraActiveSurface.AOD
                                                }
                                                AstraCanonicalFrame.F38_PRIVACY_INDICATORS -> {
                                                    systemController.setSimulatedSystemState(cameraActive = true, micActive = true)
                                                    activeSurface = AstraActiveSurface.HOME
                                                }
                                                AstraCanonicalFrame.F39_THIRD_PARTY_WIDGET -> {
                                                    val hasHosted = homeLayout.widgets.any { it.widgetType == AstraWidgetType.ANDROID_HOSTED }
                                                    if (!hasHosted) {
                                                        storageRepository.updateHomeLayout { cur ->
                                                            cur.copy(
                                                                widgets = listOf(
                                                                    AstraWidgetPlacement(
                                                                        instanceId = 1999,
                                                                        widgetType = AstraWidgetType.ANDROID_HOSTED,
                                                                        appWidgetId = 2026,
                                                                        providerPackage = "com.spotify.music",
                                                                        providerLabel = "Spotify Now Playing",
                                                                        pageIndex = 0
                                                                    )
                                                                ) + cur.widgets
                                                            )
                                                        }
                                                    }
                                                    activeSurface = AstraActiveSurface.HOME
                                                }
                                                AstraCanonicalFrame.F40_ACCESSIBILITY_LARGE_TEXT -> {
                                                    storageRepository.updateThemeSettings {
                                                        it.copy(textScaleMultiplier = 1.30f, highContrastMode = true)
                                                    }
                                                    activeSurface = AstraActiveSurface.HOME
                                                }
                                            }
                                        },
                                        onResetToSafeDefaults = {
                                            storageRepository.resetToSafeDefaults()
                                        },
                                        onClose = { activeSurface = AstraActiveSurface.HOME }
                                    )
                                }

                                AstraActiveSurface.AOD -> {
                                    // Handled at root
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
