package com.astra.launcher.core.storage

/**
 * Astra Launcher Complete Product, UX & System Experience Domain Models (Rebuild Specification Sections 1–50).
 * Zero fake data, zero ads, zero telemetry.
 */

enum class AstraThemePreset(
    val id: String,
    val displayName: String,
    val subtitle: String,
    val defaultWallpaper: AstraWallpaperId,
    val isLightDefault: Boolean,
    val primaryAccentHex: Long,
    val secondaryAccentHex: Long
) {
    ASTRAL(
        id = "astral",
        displayName = "Orbit",
        subtitle = "Deep spatial obsidian with restrained mineral-cyan light",
        defaultWallpaper = AstraWallpaperId.ORBIT_DAWN,
        isLightDefault = false,
        primaryAccentHex = 0xFF7DD3FC,
        secondaryAccentHex = 0xFF38BDF8
    ),
    NOCTURNE(
        id = "nocturne",
        displayName = "Nocturne",
        subtitle = "Architectural matte charcoal and smoked glass",
        defaultWallpaper = AstraWallpaperId.NOCTURNE_FLOW,
        isLightDefault = false,
        primaryAccentHex = 0xFF93C5FD,
        secondaryAccentHex = 0xFF60A5FA
    ),
    GLASS_HORIZON(
        id = "glass_horizon",
        displayName = "Horizon",
        subtitle = "Warm horizon light-field with calm dark lower workspace",
        defaultWallpaper = AstraWallpaperId.GLASS_HORIZON,
        isLightDefault = false,
        primaryAccentHex = 0xFF67E8F9,
        secondaryAccentHex = 0xFF93C5FD
    ),
    GRAPHITE(
        id = "graphite",
        displayName = "Graphite",
        subtitle = "Ultra-clean solid graphite optimized for low-memory hardware",
        defaultWallpaper = AstraWallpaperId.GRAPHITE_MONOLITH,
        isLightDefault = false,
        primaryAccentHex = 0xFF94A3B8,
        secondaryAccentHex = 0xFF38BDF8
    ),
    MORNING(
        id = "morning",
        displayName = "Daylight",
        subtitle = "High-contrast daylight environment for outdoor readability",
        defaultWallpaper = AstraWallpaperId.MORNING_MIST,
        isLightDefault = true,
        primaryAccentHex = 0xFF0284C7,
        secondaryAccentHex = 0xFF2563EB
    );

    companion object {
        fun fromId(id: String): AstraThemePreset =
            entries.firstOrNull { it.id == id } ?: ASTRAL
    }
}

/**
 * First-Run & Home Layout Density Modes (Rebuild Section 2 & 3).
 */
enum class HomeDensityMode(
    val id: String,
    val title: String,
    val description: String,
    val defaultColumns: Int,
    val defaultRows: Int,
    val initialWorkspaceCount: Int
) {
    MINIMAL(
        id = "minimal",
        title = "Minimal",
        description = "Generous negative space, large adaptive clock, and 4 essential apps",
        defaultColumns = 4,
        defaultRows = 5,
        initialWorkspaceCount = 4
    ),
    BALANCED(
        id = "balanced",
        title = "Balanced",
        description = "Harmonious spatial clock, contextual suggestions, and 8 curated apps",
        defaultColumns = 4,
        defaultRows = 5,
        initialWorkspaceCount = 8
    ),
    DENSE(
        id = "dense",
        title = "Dense",
        description = "Compact 5×6 workspace grid for power users with more apps per page",
        defaultColumns = 5,
        defaultRows = 6,
        initialWorkspaceCount = 10
    );

    companion object {
        fun fromId(id: String): HomeDensityMode =
            entries.firstOrNull { it.id == id } ?: BALANCED
    }
}

enum class WallpaperSource(val id: String, val label: String) {
    BUNDLED_ASTRA("bundled_astra", "Astra Wallpaper System"),
    SYSTEM_WALLPAPER("system_wallpaper", "Android System Wallpaper");

    companion object {
        fun fromId(id: String): WallpaperSource =
            entries.firstOrNull { it.id == id } ?: BUNDLED_ASTRA
    }
}

enum class AstraWallpaperId(
    val id: String,
    val title: String,
    val familyName: String,
    val familyDescription: String,
    val upperRegionLuminance: Float,
    val recommendedAccentHex: Long,
    val secondaryAtmosphereHex: Long
) {
    ORBIT_DAWN(
        id = "orbit_dawn",
        title = "Orbit",
        familyName = "ORBIT",
        familyDescription = "Atmospheric orbital geometry, dark spatial environment, generous negative space",
        upperRegionLuminance = 0.22f,
        recommendedAccentHex = 0xFF7DD3FC,
        secondaryAtmosphereHex = 0xFFFDE68A
    ),
    NOCTURNE_FLOW(
        id = "nocturne_flow",
        title = "Nocturne Flow",
        familyName = "NOCTURNE FLOW",
        familyDescription = "Dark architectural fluid forms on a deep charcoal and black foundation",
        upperRegionLuminance = 0.08f,
        recommendedAccentHex = 0xFF93C5FD,
        secondaryAtmosphereHex = 0xFF60A5FA
    ),
    GLASS_HORIZON(
        id = "glass_horizon",
        title = "Horizon",
        familyName = "HORIZON",
        familyDescription = "Abstract horizon light-field environment with dark lower workspace region",
        upperRegionLuminance = 0.78f,
        recommendedAccentHex = 0xFF38BDF8,
        secondaryAtmosphereHex = 0xFF818CF8
    ),
    MORNING_MIST(
        id = "morning_mist",
        title = "Orbit Daylight",
        familyName = "ORBIT",
        familyDescription = "Light-aware orbital composition with high-contrast upper safe zone",
        upperRegionLuminance = 0.90f,
        recommendedAccentHex = 0xFF0284C7,
        secondaryAtmosphereHex = 0xFF2563EB
    ),
    GRAPHITE_MONOLITH(
        id = "graphite_monolith",
        title = "Graphite Monolith",
        familyName = "NOCTURNE FLOW",
        familyDescription = "Ultra-calm low-memory architectural graphite with subtle mineral edge",
        upperRegionLuminance = 0.06f,
        recommendedAccentHex = 0xFF38BDF8,
        secondaryAccentHex = 0xFF94A3B8
    );

    companion object {
        fun fromId(id: String): AstraWallpaperId =
            entries.firstOrNull { it.id == id } ?: ORBIT_DAWN
    }
}

enum class AstraClockStyle(val id: String, val label: String, val description: String) {
    MINIMAL_NUMERAL("minimal_numeral", "Spatial Numeral", "Balanced optical numerals with live date, context, and alarm status"),
    EDITORIAL_STACKED("editorial_stacked", "Editorial Stacked", "Two-line vertical time with calm negative space"),
    ORBITAL_COMPACT("orbital_compact", "Compact Header", "Clean single-line time and date giving maximum workspace area");

    companion object {
        fun fromId(id: String): AstraClockStyle =
            entries.firstOrNull { it.id == id } ?: MINIMAL_NUMERAL
    }
}

/**
 * Icon Treatment (Rebuild Section 2 & 9):
 * Preserves real application icons while offering Original, Astra Adaptive, or Monochrome treatments.
 */
enum class AstraIconStyle(val id: String, val label: String, val description: String) {
    ASTRA_SQUIRCLE("astra_squircle", "Astra Adaptive", "Real app icon harmonized inside a disciplined squircle container"),
    ORIGINAL("original", "Original", "Unmodified real application launcher icon with optical scale normalization"),
    MONOCHROME_TINT("monochrome_tint", "Monochrome", "Uses Android 13+ monochrome layer or tonal luminance-mapped real icon"),
    CIRCLE("circle", "Adaptive Circle", "Normalized circular container around the real application icon");

    companion object {
        fun fromId(id: String): AstraIconStyle =
            entries.firstOrNull { it.id == id } ?: ASTRA_SQUIRCLE
    }
}

enum class AstraThemeMode(val id: String, val label: String) {
    DARK("dark", "Dark"),
    LIGHT("light", "Light"),
    AUTO_WALLPAPER("auto", "Adaptive to Wallpaper");

    companion object {
        fun fromId(id: String): AstraThemeMode =
            entries.firstOrNull { it.id == id } ?: DARK
    }
}

enum class AstraAccentSource(val id: String, val label: String) {
    WALLPAPER("wallpaper", "Wallpaper Contrast Envelope"),
    THEME_PRESET("preset", "Astra Preset"),
    CUSTOM("custom", "Custom Accent");

    companion object {
        fun fromId(id: String): AstraAccentSource =
            entries.firstOrNull { it.id == id } ?: WALLPAPER
    }
}

data class ThemeSettings(
    val themePreset: AstraThemePreset = AstraThemePreset.ASTRAL,
    val wallpaperSource: WallpaperSource = WallpaperSource.BUNDLED_ASTRA,
    val wallpaperId: AstraWallpaperId = AstraWallpaperId.ORBIT_DAWN,
    val accentSource: AstraAccentSource = AstraAccentSource.WALLPAPER,
    val themeMode: AstraThemeMode = AstraThemeMode.DARK,
    val iconStyle: AstraIconStyle = AstraIconStyle.ASTRA_SQUIRCLE,
    val clockStyle: AstraClockStyle = AstraClockStyle.MINIMAL_NUMERAL,
    val customAccentHex: Long = 0xFF7DD3FC,
    val iconScale: Float = 1.0f,
    val showIconLabels: Boolean = true,
    val showDockLabels: Boolean = false,
    val dockStyleGlass: Boolean = true,
    val materialIntensity: Float = 0.78f,
    val textScaleMultiplier: Float = 1.0f,
    val highContrastMode: Boolean = false
)

enum class WorkspaceItemType(val id: String) {
    APP("app"),
    SHORTCUT("shortcut"),
    FOLDER("folder"),
    WIDGET("widget");

    companion object {
        fun fromId(id: String): WorkspaceItemType =
            entries.firstOrNull { it.id == id } ?: APP
    }
}

data class FolderMemberApp(
    val packageName: String,
    val componentName: String,
    val userSerial: Long = 0L,
    val label: String
)

data class WorkspaceCellItem(
    val id: String,
    val page: Int,
    val cellX: Int,
    val cellY: Int,
    val spanX: Int = 1,
    val spanY: Int = 1,
    val itemType: WorkspaceItemType = WorkspaceItemType.APP,
    val packageName: String = "",
    val componentName: String = "",
    val userSerial: Long = 0L,
    val label: String = "",
    val shortcutId: String = "",
    val appWidgetId: Int = -1,
    val widgetProvider: String = "",
    val folderId: String = "",
    val folderItems: List<FolderMemberApp> = emptyList()
)

data class DockSlotItem(
    val slotIndex: Int,
    val packageName: String,
    val componentName: String,
    val userSerial: Long = 0L,
    val label: String
)

data class HomeLayout(
    val isInitialized: Boolean = false,
    val hasCompletedFirstRunSetup: Boolean = false,
    val densityMode: HomeDensityMode = HomeDensityMode.BALANCED,
    val pageCount: Int = 2,
    val gridColumns: Int = 4,
    val gridRows: Int = 5,
    val dockEnabled: Boolean = true,
    val dockSlotCount: Int = 5,
    val showDockSearchButton: Boolean = false,
    val showPageIndicator: Boolean = true,
    val showClockOnWorkspace: Boolean = true,
    val showContextualSuggestionsOnHome: Boolean = true,
    val lockWorkspaceLayout: Boolean = false,
    val items: List<WorkspaceCellItem> = emptyList(),
    val dockItems: List<DockSlotItem> = emptyList(),
    val hiddenComponents: Set<String> = emptySet(),
    val favoriteComponents: Set<String> = emptySet()
)

data class SearchPreferences(
    val searchApps: Boolean = true,
    val searchSettings: Boolean = true,
    val searchShortcuts: Boolean = true,
    val searchCommands: Boolean = true,
    val saveSearchHistory: Boolean = true,
    val recentQueries: List<String> = emptyList(),
    val autoFocusKeyboard: Boolean = true
)

data class NotificationPreferences(
    val showAppBadgeDots: Boolean = true,
    val showNotificationCountOnMenu: Boolean = true,
    val showHomeNotificationPill: Boolean = true
)

enum class SwipeDownAction(val id: String, val label: String) {
    SEARCH("search", "Open Astra Universal Search"),
    ASTRA_NOTIFICATIONS("astra_notifications", "Open Astra Notification & Control Surface"),
    ANDROID_NOTIFICATION_SHADE("android_shade", "Expand Android System Notification Shade");

    companion object {
        fun fromId(id: String): SwipeDownAction =
            entries.firstOrNull { it.id == id } ?: SEARCH
    }
}

enum class DoubleTapAction(val id: String, val label: String) {
    SEARCH("search", "Open Astra Search"),
    CONTROL_SURFACE("control_surface", "Open Astra Control & Status Surface"),
    APP_DISCOVERY("app_discovery", "Open App Discovery"),
    NONE("none", "Disabled");

    companion object {
        fun fromId(id: String): DoubleTapAction =
            entries.firstOrNull { it.id == id } ?: CONTROL_SURFACE
    }
}

data class GesturePreferences(
    val swipeUpAction: String = "app_drawer",
    val swipeDownAction: SwipeDownAction = SwipeDownAction.SEARCH,
    val doubleTapAction: DoubleTapAction = DoubleTapAction.CONTROL_SURFACE,
    val longPressAction: String = "home_editor",
    val pinchAction: String = "home_editor",
    val hapticsEnabled: Boolean = true
)

enum class AppSortOrder(val id: String, val label: String) {
    MOST_USED("most_used", "Most Used"),
    ALPHABETICAL("alphabetical", "A–Z");

    companion object {
        fun fromId(id: String): AppSortOrder =
            entries.firstOrNull { it.id == id } ?: MOST_USED
    }
}

data class PerformancePreferences(
    val animationsEnabled: Boolean = true,
    val reducedMotion: Boolean = false,
    val blurEnabled: Boolean = true,
    val lowEndDeviceModeOverride: Boolean = false,
    val showDrawerCategories: Boolean = true,
    val showDrawerRecentRow: Boolean = true,
    val appSortOrder: AppSortOrder = AppSortOrder.MOST_USED
)

enum class AppCategory(val id: String, val label: String) {
    COMMUNICATION("communication", "Communication"),
    MEDIA("media", "Entertainment & Media"),
    PRODUCTIVITY("productivity", "Productivity"),
    UTILITIES("utilities", "Utilities & System"),
    INTERNET("internet", "Browsing & Social"),
    GAMES_OTHER("other", "Games & Other");

    companion object {
        fun fromId(id: String): AppCategory =
            entries.firstOrNull { it.id == id } ?: UTILITIES
    }
}

data class AstraShortcutItem(
    val id: String,
    val shortLabel: String,
    val longLabel: String,
    val packageName: String,
    val componentName: String = "",
    val userSerial: Long = 0L,
    val isPinned: Boolean = false
)

data class AstraAppEntry(
    val packageName: String,
    val componentName: String,
    val activityClassName: String,
    val label: String,
    val userSerial: Long = 0L,
    val isWorkProfile: Boolean = false,
    val versionCode: Long = 1L,
    val lastUpdateTime: Long = 0L,
    val isSystemApp: Boolean = false,
    val category: AppCategory = AppCategory.UTILITIES,
    val usageScore: Int = 0,
    val lastUsedTimestamp: Long = 0L,
    val shortcuts: List<AstraShortcutItem> = emptyList(),
    val activeNotificationCount: Int = 0
)

data class AstraNotificationEntry(
    val id: String,
    val packageName: String,
    val appName: String,
    val title: String,
    val content: String,
    val postTimeMillis: Long,
    val isClearable: Boolean = true,
    val isHighPriority: Boolean = false
)

data class AstraCapabilityReport(
    val canBeDefaultHome: Boolean = true,
    val isCurrentlyDefaultHome: Boolean = false,
    val currentDefaultHomePackage: String = "",
    val isHiOsDetectedOnDevice: Boolean = false,
    val hasNotificationAccess: Boolean = false,
    val canHostWidgets: Boolean = true,
    val canReadPackages: Boolean = true,
    val supportsShortcuts: Boolean = false,
    val canExpandSystemShade: Boolean = true
)

data class AstraDeviceStatus(
    val batteryPercent: Int = -1,
    val isCharging: Boolean = false,
    val isLowBattery: Boolean = false,
    val isWifiConnected: Boolean = false,
    val isOffline: Boolean = false,
    val lowMemoryPressure: Boolean = false,
    val nextAlarmLabel: String? = null,
    val manufacturer: String = "",
    val model: String = ""
)
