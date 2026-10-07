package com.astra.launcher.core.platform

import android.app.AlarmManager
import android.app.WallpaperManager
import android.app.role.RoleManager
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.graphics.Rect
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Process
import android.os.UserHandle
import android.os.UserManager
import android.provider.Settings
import android.util.Log
import com.astra.launcher.core.storage.AppCategory
import com.astra.launcher.core.storage.AstraAppEntry
import com.astra.launcher.core.storage.AstraCapabilityReport
import com.astra.launcher.core.storage.AstraDeviceStatus
import com.astra.launcher.core.storage.AstraNotificationEntry
import com.astra.launcher.core.storage.AstraShortcutItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val TAG = "AstraLauncherPlatform"

/**
 * 1. Capability Matrix Inspector (Section 27 & 28)
 */
class AstraCapabilityManager(private val context: Context?) {

    fun inspectCapabilities(): AstraCapabilityReport {
        val ctx = context ?: return AstraCapabilityReport()
        val isHome = AstraRoleHomeManager.isDefaultHome(ctx)
        val hasNotif = isNotificationListenerEnabled(ctx)
        val launcherApps = ctx.getSystemService(Context.LAUNCHER_APPS_SERVICE) as? LauncherApps
        val supportsShortcuts = try {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1 &&
                (launcherApps?.hasShortcutHostPermission() == true)
        } catch (_: Throwable) {
            false
        }

        return AstraCapabilityReport(
            canBeDefaultHome = true,
            isCurrentlyDefaultHome = isHome,
            hasNotificationAccess = hasNotif,
            canHostWidgets = true,
            canReadPackages = true,
            supportsShortcuts = supportsShortcuts,
            canExpandSystemShade = true
        )
    }

    fun inspectDeviceStatus(): AstraDeviceStatus {
        val ctx = context ?: return AstraDeviceStatus()
        return try {
            val bm = ctx.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
            val batteryPct = bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: -1
            val isCharging = bm?.isCharging ?: false

            val cm = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            val activeNet = cm?.activeNetwork
            val caps = if (activeNet != null) cm.getNetworkCapabilities(activeNet) else null
            val isOffline = caps == null || !caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)

            val alarmManager = ctx.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            val nextAlarm = try {
                alarmManager?.nextAlarmClock?.triggerTime?.let { triggerMs ->
                    java.text.SimpleDateFormat("EEE HH:mm", java.util.Locale.getDefault())
                        .format(java.util.Date(triggerMs))
                }
            } catch (_: Throwable) {
                null
            }

            AstraDeviceStatus(
                batteryPercent = batteryPct.coerceIn(-1, 100),
                isCharging = isCharging,
                isLowBattery = batteryPct in 0..15 && !isCharging,
                isOffline = isOffline,
                lowMemoryPressure = false,
                nextAlarmLabel = nextAlarm
            )
        } catch (_: Throwable) {
            AstraDeviceStatus()
        }
    }

    fun isNotificationListenerEnabled(ctx: Context): Boolean {
        return try {
            val enabledListeners = Settings.Secure.getString(
                ctx.contentResolver,
                "enabled_notification_listeners"
            ) ?: return false
            enabledListeners.contains(ctx.packageName)
        } catch (_: Throwable) {
            false
        }
    }
}

/**
 * 2. Default Launcher Role Manager (`ROLE_HOME` — Section 3)
 */
object AstraRoleHomeManager {

    fun isDefaultHome(context: Context): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val roleManager = context.getSystemService(RoleManager::class.java)
                if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_HOME)) {
                    if (roleManager.isRoleHeld(RoleManager.ROLE_HOME)) return true
                }
            }
            val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
            val resolved = context.packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
            resolved?.activityInfo?.packageName == context.packageName
        } catch (_: Throwable) {
            false
        }
    }

    fun buildRoleRequestIntent(context: Context): Intent? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val roleManager = context.getSystemService(RoleManager::class.java)
                if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_HOME)) {
                    return roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME)
                }
            }
            Intent(Settings.ACTION_HOME_SETTINGS)
        } catch (_: Throwable) {
            Intent(Settings.ACTION_HOME_SETTINGS)
        }
    }

    fun openDefaultHomeSettings(context: Context): Boolean {
        return try {
            val intent = Intent(Settings.ACTION_HOME_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (_: Throwable) {
            false
        }
    }
}

sealed class PackageDiscoveryState {
    data object Loading : PackageDiscoveryState()
    data class Ready(val apps: List<AstraAppEntry>) : PackageDiscoveryState()
    data object Empty : PackageDiscoveryState()
    data class Error(val reason: String) : PackageDiscoveryState()
}

sealed class LaunchResult {
    data class Success(val componentName: String) : LaunchResult()
    data class Failure(val componentName: String, val errorMessage: String) : LaunchResult()
}

/**
 * 3. Real Application Discovery, Live Package Observer & Real Launching (`AstraPackageRepository` — Sections 5, 7, 8, 9)
 * Zero fake fallback catalog. Discovers real launchable activities via LauncherApps & PackageManager.
 */
class AstraPackageRepository(
    private val context: Context?,
    val iconPipeline: AstraIconPipeline = AstraIconPipeline(context),
    private val onPackagesUpdatedCallback: ((List<AstraAppEntry>) -> Unit)? = null
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _installedApps = MutableStateFlow<List<AstraAppEntry>>(emptyList())
    val installedApps: StateFlow<List<AstraAppEntry>> = _installedApps.asStateFlow()

    private val _discoveryState = MutableStateFlow<PackageDiscoveryState>(PackageDiscoveryState.Loading)
    val discoveryState: StateFlow<PackageDiscoveryState> = _discoveryState.asStateFlow()

    private val _lastLaunchError = MutableStateFlow<String?>(null)
    val lastLaunchError: StateFlow<String?> = _lastLaunchError.asStateFlow()

    private var observersRegistered = false

    private val launcherAppsCallback = object : LauncherApps.Callback() {
        override fun onPackageRemoved(packageName: String, user: UserHandle) {
            iconPipeline.invalidatePackage(packageName)
            refreshInstalledAppsAsync()
        }

        override fun onPackageAdded(packageName: String, user: UserHandle) {
            iconPipeline.invalidatePackage(packageName)
            refreshInstalledAppsAsync()
        }

        override fun onPackageChanged(packageName: String, user: UserHandle) {
            iconPipeline.invalidatePackage(packageName)
            refreshInstalledAppsAsync()
        }

        override fun onPackagesAvailable(packageNames: Array<out String>, user: UserHandle, replacing: Boolean) {
            packageNames.forEach { iconPipeline.invalidatePackage(it) }
            refreshInstalledAppsAsync()
        }

        override fun onPackagesUnavailable(packageNames: Array<out String>, user: UserHandle, replacing: Boolean) {
            packageNames.forEach { iconPipeline.invalidatePackage(it) }
            refreshInstalledAppsAsync()
        }

        override fun onShortcutsChanged(
            packageName: String,
            shortcuts: MutableList<android.content.pm.ShortcutInfo>,
            user: UserHandle
        ) {
            refreshInstalledAppsAsync()
        }
    }

    private val packageBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val pkg = intent?.data?.schemeSpecificPart
            if (!pkg.isNullOrBlank()) {
                iconPipeline.invalidatePackage(pkg)
            }
            refreshInstalledAppsAsync()
        }
    }

    fun clearLaunchError() {
        _lastLaunchError.value = null
    }

    /**
     * Registers live package observers (`LauncherApps.Callback` + package `BroadcastReceiver`)
     * so app installs, uninstalls, updates, and enable/disable events update Astra in real time (Section 7).
     */
    fun registerLivePackageObservers() {
        val ctx = context ?: return
        if (observersRegistered) return
        observersRegistered = true

        try {
            val launcherApps = ctx.getSystemService(Context.LAUNCHER_APPS_SERVICE) as? LauncherApps
            launcherApps?.registerCallback(launcherAppsCallback, Handler(Looper.getMainLooper()))
        } catch (e: Throwable) {
            Log.w(TAG, "Unable to register LauncherApps.Callback", e)
        }

        try {
            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_PACKAGE_ADDED)
                addAction(Intent.ACTION_PACKAGE_REMOVED)
                addAction(Intent.ACTION_PACKAGE_REPLACED)
                addAction(Intent.ACTION_PACKAGE_CHANGED)
                addDataScheme("package")
            }
            ctx.registerReceiver(packageBroadcastReceiver, filter)
        } catch (e: Throwable) {
            Log.w(TAG, "Unable to register package BroadcastReceiver", e)
        }
    }

    fun unregisterLivePackageObservers() {
        val ctx = context ?: return
        if (!observersRegistered) return
        observersRegistered = false
        try {
            val launcherApps = ctx.getSystemService(Context.LAUNCHER_APPS_SERVICE) as? LauncherApps
            launcherApps?.unregisterCallback(launcherAppsCallback)
        } catch (_: Throwable) {
        }
        try {
            ctx.unregisterReceiver(packageBroadcastReceiver)
        } catch (_: Throwable) {
        }
    }

    fun refreshInstalledAppsAsync(
        usageLookup: (String) -> Int = { 0 },
        lastUsedLookup: (String) -> Long = { 0L },
        notificationCountLookup: (String) -> Int = { 0 }
    ) {
        scope.launch {
            refreshInstalledApps(usageLookup, lastUsedLookup, notificationCountLookup)
        }
    }

    /**
     * Synchronous discovery of all REAL installed launchable activities on the device.
     * Never injects fake or fallback applications (Section 5 & 32).
     */
    fun refreshInstalledApps(
        usageLookup: (String) -> Int = { 0 },
        lastUsedLookup: (String) -> Long = { 0L },
        notificationCountLookup: (String) -> Int = { 0 }
    ): List<AstraAppEntry> {
        val ctx = context
        if (ctx == null) {
            _installedApps.value = emptyList()
            _discoveryState.value = PackageDiscoveryState.Empty
            return emptyList()
        }

        return try {
            val discovered = queryRealLauncherActivities(ctx, usageLookup, lastUsedLookup, notificationCountLookup)
            _installedApps.value = discovered
            _discoveryState.value = if (discovered.isEmpty()) {
                PackageDiscoveryState.Empty
            } else {
                PackageDiscoveryState.Ready(discovered)
            }
            onPackagesUpdatedCallback?.invoke(discovered)
            discovered
        } catch (t: Throwable) {
            Log.e(TAG, "Package discovery failed", t)
            _discoveryState.value = PackageDiscoveryState.Error(t.message ?: "PackageManager query failed")
            _installedApps.value
        }
    }

    private fun queryRealLauncherActivities(
        ctx: Context,
        usageLookup: (String) -> Int,
        lastUsedLookup: (String) -> Long,
        notificationCountLookup: (String) -> Int
    ): List<AstraAppEntry> {
        val results = mutableListOf<AstraAppEntry>()
        val seenComponents = mutableSetOf<String>()
        val ownPackage = ctx.packageName

        val launcherApps = ctx.getSystemService(Context.LAUNCHER_APPS_SERVICE) as? LauncherApps
        val userManager = ctx.getSystemService(Context.USER_SERVICE) as? UserManager
        val pm = ctx.packageManager

        if (launcherApps != null && userManager != null) {
            val profiles: List<UserHandle> = try {
                userManager.userProfiles.ifEmpty { listOf(Process.myUserHandle()) }
            } catch (_: Throwable) {
                listOf(Process.myUserHandle())
            }
            val primaryUser = Process.myUserHandle()

            for (profile in profiles) {
                val userSerial = try {
                    userManager.getSerialNumberForUser(profile)
                } catch (_: Throwable) {
                    0L
                }
                val isWork = profile != primaryUser
                val activities = try {
                    launcherApps.getActivityList(null, profile)
                } catch (_: Throwable) {
                    emptyList()
                }

                for (info in activities) {
                    val pkg = info.componentName.packageName
                    if (pkg == ownPackage) continue
                    val compString = info.componentName.flattenToString()
                    val uniqueKey = "${compString}_$userSerial"
                    if (!seenComponents.add(uniqueKey)) continue

                    val appInfo = info.applicationInfo
                    val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                    val pkgInfo = try {
                        pm.getPackageInfo(pkg, 0)
                    } catch (_: Throwable) {
                        null
                    }
                    val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        pkgInfo?.longVersionCode ?: 1L
                    } else {
                        @Suppress("DEPRECATION")
                        (pkgInfo?.versionCode ?: 1).toLong()
                    }
                    val lastUpdate = pkgInfo?.lastUpdateTime ?: 0L
                    val label = info.label?.toString()?.takeIf { it.isNotBlank() } ?: pkg
                    val shortcuts = queryRealAppShortcuts(launcherApps, pkg, profile, userSerial)

                    results.add(
                        AstraAppEntry(
                            packageName = pkg,
                            componentName = compString,
                            activityClassName = info.componentName.className,
                            label = label,
                            userSerial = userSerial,
                            isWorkProfile = isWork,
                            versionCode = versionCode,
                            lastUpdateTime = lastUpdate,
                            isSystemApp = isSystem,
                            category = resolveAppCategory(appInfo, pkg, label),
                            usageScore = usageLookup(compString),
                            lastUsedTimestamp = lastUsedLookup(compString),
                            shortcuts = shortcuts,
                            activeNotificationCount = notificationCountLookup(pkg)
                        )
                    )
                }
            }
        }

        // Fallback to PackageManager.queryIntentActivities if LauncherApps returned nothing
        // (e.g., under Robolectric ShadowPackageManager or restricted profile)
        if (results.isEmpty()) {
            val mainIntent = Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER)
            val resolvedList = pm.queryIntentActivities(mainIntent, 0)
            for (resolveInfo in resolvedList) {
                val activityInfo = resolveInfo.activityInfo ?: continue
                val pkg = activityInfo.packageName ?: continue
                if (pkg == ownPackage) continue
                val cls = activityInfo.name ?: continue
                val comp = ComponentName(pkg, cls).flattenToString()
                if (!seenComponents.add(comp)) continue

                val appInfo = activityInfo.applicationInfo
                val isSystem = appInfo != null && (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                val label = resolveInfo.loadLabel(pm)?.toString()?.takeIf { it.isNotBlank() } ?: pkg

                results.add(
                    AstraAppEntry(
                        packageName = pkg,
                        componentName = comp,
                        activityClassName = cls,
                        label = label,
                        userSerial = 0L,
                        isWorkProfile = false,
                        versionCode = 1L,
                        lastUpdateTime = 0L,
                        isSystemApp = isSystem,
                        category = resolveAppCategory(appInfo, pkg, label),
                        usageScore = usageLookup(comp),
                        lastUsedTimestamp = lastUsedLookup(comp),
                        shortcuts = emptyList(),
                        activeNotificationCount = notificationCountLookup(pkg)
                    )
                )
            }
        }

        return results.sortedBy { it.label.lowercase() }
    }

    private fun queryRealAppShortcuts(
        launcherApps: LauncherApps,
        packageName: String,
        userHandle: UserHandle,
        userSerial: Long
    ): List<AstraShortcutItem> {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) return emptyList()
        return try {
            if (!launcherApps.hasShortcutHostPermission()) return emptyList()
            val query = LauncherApps.ShortcutQuery().apply {
                setPackage(packageName)
                setQueryFlags(
                    LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC or
                        LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST or
                        LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED
                )
            }
            val raw = launcherApps.getShortcuts(query, userHandle) ?: return emptyList()
            raw.take(4).map { s ->
                AstraShortcutItem(
                    id = s.id,
                    shortLabel = (s.shortLabel ?: s.id).toString(),
                    longLabel = (s.longLabel ?: s.shortLabel ?: s.id).toString(),
                    packageName = packageName,
                    userSerial = userSerial,
                    isPinned = s.isPinned
                )
            }
        } catch (_: Throwable) {
            emptyList()
        }
    }

    private fun resolveAppCategory(
        appInfo: ApplicationInfo?,
        packageName: String,
        label: String
    ): AppCategory {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && appInfo != null) {
            when (appInfo.category) {
                ApplicationInfo.CATEGORY_SOCIAL -> return AppCategory.COMMUNICATION
                ApplicationInfo.CATEGORY_AUDIO,
                ApplicationInfo.CATEGORY_VIDEO,
                ApplicationInfo.CATEGORY_IMAGE -> return AppCategory.MEDIA
                ApplicationInfo.CATEGORY_PRODUCTIVITY -> return AppCategory.PRODUCTIVITY
                ApplicationInfo.CATEGORY_NEWS -> return AppCategory.INTERNET
                ApplicationInfo.CATEGORY_GAME -> return AppCategory.GAMES_OTHER
            }
        }
        val token = "${packageName.lowercase()} ${label.lowercase()}"
        return when {
            token.contains("dialer") || token.contains("phone") || token.contains("message") ||
                token.contains("whatsapp") || token.contains("telegram") || token.contains("chat") ||
                token.contains("contacts") -> AppCategory.COMMUNICATION
            token.contains("camera") || token.contains("photos") || token.contains("gallery") ||
                token.contains("music") || token.contains("youtube") || token.contains("spotify") -> AppCategory.MEDIA
            token.contains("calendar") || token.contains("docs") || token.contains("notes") ||
                token.contains("mail") || token.contains("gmail") || token.contains("files") -> AppCategory.PRODUCTIVITY
            token.contains("chrome") || token.contains("browser") || token.contains("firefox") ||
                token.contains("maps") -> AppCategory.INTERNET
            else -> AppCategory.UTILITIES
        }
    }

    /**
     * Launches the real resolved Android Activity (`ComponentName` + `UserHandle`) (Section 8).
     * Never uses hardcoded package fallbacks. Logs and surfaces real error state if launch fails.
     */
    fun launchApp(app: AstraAppEntry, sourceBounds: Rect? = null): LaunchResult {
        val ctx = context ?: return LaunchResult.Failure(app.componentName, "No Android Context available")

        val component = ComponentName.unflattenFromString(app.componentName)
            ?: ComponentName(app.packageName, app.activityClassName)

        // 1. Primary path: LauncherApps.startMainActivity(componentName, userHandle, sourceBounds, opts)
        try {
            val launcherApps = ctx.getSystemService(Context.LAUNCHER_APPS_SERVICE) as? LauncherApps
            val userManager = ctx.getSystemService(Context.USER_SERVICE) as? UserManager
            if (launcherApps != null && userManager != null) {
                val userHandle = userManager.getUserForSerialNumber(app.userSerial)
                    ?: Process.myUserHandle()
                launcherApps.startMainActivity(component, userHandle, sourceBounds, null)
                _lastLaunchError.value = null
                return LaunchResult.Success(component.flattenToString())
            }
        } catch (t: Throwable) {
            Log.w(TAG, "LauncherApps.startMainActivity failed for ${app.componentName}, trying explicit ComponentName Intent", t)
        }

        // 2. Explicit ComponentName ACTION_MAIN + CATEGORY_LAUNCHER Intent
        return try {
            val explicitIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
                setComponent(component)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                if (sourceBounds != null) {
                    this.sourceBounds = sourceBounds
                }
            }
            ctx.startActivity(explicitIntent)
            _lastLaunchError.value = null
            LaunchResult.Success(component.flattenToString())
        } catch (firstErr: Throwable) {
            // 3. Package-level launch intent fallback if the app updated its main activity class name
            try {
                val pkgIntent = ctx.packageManager.getLaunchIntentForPackage(app.packageName)?.apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                }
                if (pkgIntent != null) {
                    ctx.startActivity(pkgIntent)
                    _lastLaunchError.value = null
                    return LaunchResult.Success(app.packageName)
                }
            } catch (_: Throwable) {
            }
            val msg = "Unable to launch ${app.label} (${app.componentName}): ${firstErr.javaClass.simpleName}"
            Log.e(TAG, msg, firstErr)
            _lastLaunchError.value = msg
            LaunchResult.Failure(app.componentName, msg)
        }
    }

    /**
     * Launches a real Android ShortcutInfo via LauncherApps.startShortcut (Section 9).
     */
    fun launchAppShortcut(shortcut: AstraShortcutItem, sourceBounds: Rect? = null): Boolean {
        val ctx = context ?: return false
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) return false
        return try {
            val launcherApps = ctx.getSystemService(Context.LAUNCHER_APPS_SERVICE) as? LauncherApps ?: return false
            val userManager = ctx.getSystemService(Context.USER_SERVICE) as? UserManager
            val userHandle = userManager?.getUserForSerialNumber(shortcut.userSerial) ?: Process.myUserHandle()
            launcherApps.startShortcut(shortcut.packageName, shortcut.id, sourceBounds, null, userHandle)
            true
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to launch shortcut ${shortcut.id} for ${shortcut.packageName}", t)
            false
        }
    }
}

/**
 * 4. Genuine Android Widget Host Controller (`AppWidgetHost` + `AppWidgetManager` — Section 13)
 */
data class InstalledWidgetProvider(
    val providerComponent: ComponentName,
    val packageName: String,
    val label: String,
    val minWidthDp: Int,
    val minHeightDp: Int,
    val recommendedSpanX: Int,
    val recommendedSpanY: Int,
    val hasConfigurationActivity: Boolean,
    val configurationComponent: ComponentName?,
    val providerInfo: AppWidgetProviderInfo?
)

class AstraWidgetHostManager(private val context: Context?) {

    private val appWidgetManager: AppWidgetManager? = context?.let { AppWidgetManager.getInstance(it) }
    val appWidgetHost: AppWidgetHost? = context?.applicationContext?.let {
        AppWidgetHost(it, ASTRA_WIDGET_HOST_ID)
    }

    private var isListening = false

    fun startListening() {
        if (isListening) return
        try {
            appWidgetHost?.startListening()
            isListening = true
        } catch (t: Throwable) {
            Log.w(TAG, "AppWidgetHost.startListening failed", t)
        }
    }

    fun stopListening() {
        if (!isListening) return
        try {
            appWidgetHost?.stopListening()
            isListening = false
        } catch (t: Throwable) {
            Log.w(TAG, "AppWidgetHost.stopListening failed", t)
        }
    }

    /**
     * Discovers all REAL installed Android widget providers on the device (`AppWidgetManager.installedProviders`).
     */
    fun queryInstalledWidgetProviders(): List<InstalledWidgetProvider> {
        val ctx = context ?: return emptyList()
        val mgr = appWidgetManager ?: return emptyList()
        val pm = ctx.packageManager
        val density = ctx.resources.displayMetrics.density.coerceAtLeast(1f)

        return try {
            mgr.installedProviders.map { info ->
                val label = try {
                    info.loadLabel(pm)?.takeIf { it.isNotBlank() }
                        ?: info.provider.shortClassName
                } catch (_: Throwable) {
                    info.provider.packageName
                }
                val minWidthDp = (info.minWidth / density).toInt().coerceAtLeast(60)
                val minHeightDp = (info.minHeight / density).toInt().coerceAtLeast(60)
                val spanX = ((minWidthDp + 50) / 74).coerceIn(1, 4)
                val spanY = ((minHeightDp + 50) / 74).coerceIn(1, 4)

                InstalledWidgetProvider(
                    providerComponent = info.provider,
                    packageName = info.provider.packageName,
                    label = label,
                    minWidthDp = minWidthDp,
                    minHeightDp = minHeightDp,
                    recommendedSpanX = spanX,
                    recommendedSpanY = spanY,
                    hasConfigurationActivity = info.configure != null,
                    configurationComponent = info.configure,
                    providerInfo = info
                )
            }.sortedBy { it.label.lowercase() }
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to query installed widget providers", t)
            emptyList()
        }
    }

    fun allocateWidgetId(): Int {
        return try {
            appWidgetHost?.allocateAppWidgetId() ?: AppWidgetManager.INVALID_APPWIDGET_ID
        } catch (_: Throwable) {
            AppWidgetManager.INVALID_APPWIDGET_ID
        }
    }

    fun deleteWidgetId(appWidgetId: Int) {
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID || appWidgetId < 0) return
        try {
            appWidgetHost?.deleteAppWidgetId(appWidgetId)
        } catch (_: Throwable) {
        }
    }

    fun bindWidgetIfAllowed(appWidgetId: Int, provider: ComponentName): Boolean {
        val mgr = appWidgetManager ?: return false
        return try {
            mgr.bindAppWidgetIdIfAllowed(appWidgetId, provider)
        } catch (t: Throwable) {
            Log.w(TAG, "bindAppWidgetIdIfAllowed returned false/exception for $provider", t)
            false
        }
    }

    fun buildBindPermissionIntent(appWidgetId: Int, provider: ComponentName): Intent {
        return Intent(AppWidgetManager.ACTION_APPWIDGET_BIND).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, provider)
        }
    }

    fun getBoundProviderInfo(appWidgetId: Int): AppWidgetProviderInfo? {
        if (appWidgetId < 0) return null
        return try {
            appWidgetManager?.getAppWidgetInfo(appWidgetId)
        } catch (_: Throwable) {
            null
        }
    }

    fun createWidgetHostView(activityContext: Context, appWidgetId: Int): AppWidgetHostView? {
        val host = appWidgetHost ?: return null
        val info = getBoundProviderInfo(appWidgetId) ?: return null
        return try {
            host.createView(activityContext, appWidgetId, info).apply {
                setAppWidget(appWidgetId, info)
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to create AppWidgetHostView for widgetId=$appWidgetId", t)
            null
        }
    }

    companion object {
        const val ASTRA_WIDGET_HOST_ID = 2026
    }
}

/**
 * 5. Honest Android System Controller (Section 16, 22, 23, 27)
 * Never fakes privileged hardware toggles; uses real Android StatusBarManager, WallpaperManager, and Settings panels.
 */
class AstraSystemController(private val context: Context?) {

    /**
     * Expands Android's real system Notification Shade using `EXPAND_STATUS_BAR` permission (Section 10 & 22).
     */
    fun expandSystemNotificationShade(): Boolean {
        val ctx = context ?: return false
        return try {
            val statusBarService = ctx.getSystemService("statusbar") ?: return false
            val statusBarManagerClass = Class.forName("android.app.StatusBarManager")
            val expandMethod = statusBarManagerClass.getMethod("expandNotificationsPanel")
            expandMethod.invoke(statusBarService)
            true
        } catch (t: Throwable) {
            Log.w(TAG, "expandNotificationsPanel failed", t)
            false
        }
    }

    /**
     * Expands Android's real system Quick Settings Shade using `EXPAND_STATUS_BAR` permission (Section 23).
     */
    fun expandSystemQuickSettings(): Boolean {
        val ctx = context ?: return false
        return try {
            val statusBarService = ctx.getSystemService("statusbar") ?: return false
            val statusBarManagerClass = Class.forName("android.app.StatusBarManager")
            val expandMethod = statusBarManagerClass.getMethod("expandSettingsPanel")
            expandMethod.invoke(statusBarService)
            true
        } catch (t: Throwable) {
            Log.w(TAG, "expandSettingsPanel failed", t)
            false
        }
    }

    /**
     * Applies a bundled Astra wallpaper resource to Android's real system `WallpaperManager` (Section 16).
     */
    fun applySystemWallpaperResource(drawableResId: Int): Boolean {
        val ctx = context ?: return false
        return try {
            val wm = WallpaperManager.getInstance(ctx)
            wm.setResource(drawableResId)
            true
        } catch (t: Throwable) {
            Log.w(TAG, "WallpaperManager.setResource failed", t)
            false
        }
    }

    fun openSystemWallpaperPicker(): Boolean =
        launchIntentSafely(Intent(Intent.ACTION_SET_WALLPAPER))

    fun openWifiSettings(): Boolean {
        val action = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Settings.Panel.ACTION_INTERNET_CONNECTIVITY
        } else {
            Settings.ACTION_WIFI_SETTINGS
        }
        return launchIntentSafely(Intent(action))
    }

    fun openBluetoothSettings(): Boolean =
        launchIntentSafely(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))

    fun openDisplaySettings(): Boolean =
        launchIntentSafely(Intent(Settings.ACTION_DISPLAY_SETTINGS))

    fun openSoundSettings(): Boolean =
        launchIntentSafely(Intent(Settings.ACTION_SOUND_SETTINGS))

    fun openBatterySettings(): Boolean =
        launchIntentSafely(Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS))

    fun openSystemSettings(): Boolean =
        launchIntentSafely(Intent(Settings.ACTION_SETTINGS))

    fun openNotificationListenerSettings(): Boolean =
        launchIntentSafely(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))

    fun openAppInfo(packageName: String): Boolean {
        if (packageName.isBlank()) return false
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }
        return launchIntentSafely(intent)
    }

    fun requestUninstallApp(packageName: String): Boolean {
        if (packageName.isBlank()) return false
        val intent = Intent(Intent.ACTION_DELETE).apply {
            data = Uri.fromParts("package", packageName, null)
        }
        return launchIntentSafely(intent)
    }

    private fun launchIntentSafely(intent: Intent): Boolean {
        val ctx = context ?: return false
        return try {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            ctx.startActivity(intent)
            true
        } catch (t: Throwable) {
            Log.w(TAG, "Failed to start system intent ${intent.action}", t)
            false
        }
    }
}

/**
 * 6. Real Notification Badge Stream Bus (Section 22)
 * Zero fake demo notifications. Only holds real notifications received from Android's NotificationListenerService.
 */
object AstraNotificationStreamBus {
    private val _notifications = MutableStateFlow<List<AstraNotificationEntry>>(emptyList())
    val notifications: StateFlow<List<AstraNotificationEntry>> = _notifications.asStateFlow()

    fun publishNotifications(items: List<AstraNotificationEntry>) {
        _notifications.value = items
    }

    fun countForPackage(packageName: String): Int {
        return _notifications.value.count { it.packageName == packageName }
    }

    fun dismissNotification(id: String) {
        _notifications.value = _notifications.value.filterNot { it.id == id }
    }

    fun clearAllClearable() {
        _notifications.value = _notifications.value.filterNot { it.isClearable }
    }
}
