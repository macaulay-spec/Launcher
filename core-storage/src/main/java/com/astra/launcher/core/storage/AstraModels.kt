package com.astra.launcher.core.storage

/**
 * Canonical Astra Data Models (Section 35, 36, 37, 40, 41, 45).
 * Single source of truth for persisted state and cross-module domain contracts.
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
        displayName = "Astral",
        subtitle = "Cinematic mineral-indigo atmosphere with icy cyan-violet highlights",
        defaultWallpaper = AstraWallpaperId.ORBIT_DAWN,
        isLightDefault = false,
        primaryAccentHex = 0xFF7DD3FC,
        secondaryAccentHex = 0xFFA78BFA
    ),
    GRAPHITE(
        id = "graphite",
        displayName = "Graphite",
        subtitle = "Precision anodized obsidian with subtle titanium contrast",
        defaultWallpaper = AstraWallpaperId.GRAPHITE_MONOLITH,
        isLightDefault = false,
        primaryAccentHex = 0xFF94A3B8,
        secondaryAccentHex = 0xFF38BDF8
    ),
    MORNING(
        id = "morning",
        displayName = "Morning",
        subtitle = "Luminous cloud-white and warm alabaster daylight clarity",
        defaultWallpaper = AstraWallpaperId.MORNING_MIST,
        isLightDefault = true,
        primaryAccentHex = 0xFF0284C7,
        secondaryAccentHex = 0xFF6366F1
    ),
    NOCTURNE(
        id = "nocturne",
        displayName = "Nocturne",
        subtitle = "Deep liquid shadow with a restrained thread of violet light",
        defaultWallpaper = AstraWallpaperId.NOCTURNE_FLOW,
        isLightDefault = false,
        primaryAccentHex = 0xFFA78BFA,
        secondaryAccentHex = 0xFF60A5FA
    ),
    GLASS_HORIZON(
        id = "glass_horizon",
        displayName = "Glass Horizon",
        subtitle = "Translucent architectural planes with pale cyan and champagne edge",
        defaultWallpaper = AstraWallpaperId.GLASS_HORIZON,
        isLightDefault = false,
        primaryAccentHex = 0xFF67E8F9,
        secondaryAccentHex = 0xFFC4B5FD
    );

    companion object {
        fun fromId(id: String): AstraThemePreset =
            entries.firstOrNull { it.id == id } ?: ASTRAL
    }
}

enum class AstraWallpaperId(
    val id: String,
    val title: String,
    val familyDescription: String,
    val upperRegionLuminance: Float, // 0.0 (dark) -> 1.0 (light) for clock adaptation
    val recommendedAccentHex: Long,
    val secondaryAtmosphereHex: Long,
    val surfaceTemperatureKelvin: Int
) {
    ORBIT_DAWN(
        id = "orbit_dawn",
        title = "Orbit Dawn",
        familyDescription = "Soft mineral-blue, muted indigo, pale silver and warm orbital arc",
        upperRegionLuminance = 0.22f,
        recommendedAccentHex = 0xFF7DD3FC,
        secondaryAtmosphereHex = 0xFFFDE68A,
        surfaceTemperatureKelvin = 5800
    ),
    NOCTURNE_FLOW(
        id = "nocturne_flow",
        title = "Nocturne Flow",
        familyDescription = "Deep graphite, smoked black and diagonal violet architectural glass",
        upperRegionLuminance = 0.08f,
        recommendedAccentHex = 0xFFA78BFA,
        secondaryAtmosphereHex = 0xFF60A5FA,
        surfaceTemperatureKelvin = 6800
    ),
    GLASS_HORIZON(
        id = "glass_horizon",
        title = "Glass Horizon",
        familyDescription = "Translucent architectural planes, frosted cyan, graphite and champagne",
        upperRegionLuminance = 0.84f,
        recommendedAccentHex = 0xFF0EA5E9,
        secondaryAtmosphereHex = 0xFF818CF8,
        surfaceTemperatureKelvin = 6200
    ),
    MORNING_MIST(
        id = "morning_mist",
        title = "Orbit Dawn · Light",
        familyDescription = "Cloud-white, mist-silver and delicate mineral-cyan daylight arc",
        upperRegionLuminance = 0.91f,
        recommendedAccentHex = 0xFF0284C7,
        secondaryAtmosphereHex = 0xFF4F46E5,
        surfaceTemperatureKelvin = 5400
    ),
    GRAPHITE_MONOLITH(
        id = "graphite_monolith",
        title = "Graphite Monolith",
        familyDescription = "Matte obsidian planes with a razor-thin icy cyan rim-light",
        upperRegionLuminance = 0.06f,
        recommendedAccentHex = 0xFF38BDF8,
        secondaryAtmosphereHex = 0xFF94A3B8,
        surfaceTemperatureKelvin = 6500
    );

    companion object {
        fun fromId(id: String): AstraWallpaperId =
            entries.firstOrNull { it.id == id } ?: ORBIT_DAWN
    }
}

enum class AstraClockStyle(val id: String, val label: String, val description: String) {
    MINIMAL_NUMERAL("minimal_numeral", "Minimal Numeral", "Precision optical numerals with balanced weight"),
    EDITORIAL_STACKED("editorial_stacked", "Editorial Stacked", "Vertical two-line display time with architectural calm"),
    ORBITAL_COMPACT("orbital_compact", "Orbital Compact", "Compact time integrated with subtle orbital arc and weather");

    companion object {
        fun fromId(id: String): AstraClockStyle =
            entries.firstOrNull { it.id == id } ?: MINIMAL_NUMERAL
    }
}

enum class AstraIconStyle(val id: String, val label: String, val description: String) {
    ASTRA_ADAPTIVE("astra_adaptive", "Astra Adaptive", "Harmonized squircle container with subtle tonal rim"),
    MONOCHROME_GLASS("monochrome_glass", "Atmospheric Tonal", "Quiet monochrome glyphs tinted by wallpaper atmosphere"),
    GRAPHITE_SOLID("graphite_solid", "Graphite Solid", "High-contrast dark graphite tiles with crisp geometry"),
    ORIGINAL("original", "System Original", "Unmodified application icons with optical scale correction");

    companion object {
        fun fromId(id: String): AstraIconStyle =
            entries.firstOrNull { it.id == id } ?: ASTRA_ADAPTIVE
    }
}

enum class AstraThemeMode(val id: String, val label: String) {
    DARK("dark", "Dark Atmosphere"),
    LIGHT("light", "Light Atmosphere"),
    AUTO_WALLPAPER("auto", "Adaptive to Wallpaper");

    companion object {
        fun fromId(id: String): AstraThemeMode =
            entries.firstOrNull { it.id == id } ?: DARK
    }
}

enum class AstraAccentSource(val id: String, val label: String) {
    WALLPAPER("wallpaper", "Wallpaper Atmosphere"),
    THEME_PRESET("preset", "Astra Preset Spectrum"),
    CUSTOM("custom", "Controlled Custom Token");

    companion object {
        fun fromId(id: String): AstraAccentSource =
            entries.firstOrNull { it.id == id } ?: WALLPAPER
    }
}

data class ThemeSettings(
    val themePreset: AstraThemePreset = AstraThemePreset.ASTRAL,
    val wallpaperId: AstraWallpaperId = AstraWallpaperId.ORBIT_DAWN,
    val accentSource: AstraAccentSource = AstraAccentSource.WALLPAPER,
    val themeMode: AstraThemeMode = AstraThemeMode.DARK,
    val iconStyle: AstraIconStyle = AstraIconStyle.ASTRA_ADAPTIVE,
    val clockStyle: AstraClockStyle = AstraClockStyle.MINIMAL_NUMERAL,
    val customAccentHex: Long = 0xFF7DD3FC,
    val iconScale: Float = 1.0f,
    val showIconLabels: Boolean = true,
    val dockStyleGlass: Boolean = true,
    val controlCenterCompact: Boolean = false,
    val textScaleMultiplier: Float = 1.0f,
    val highContrastMode: Boolean = false
)

enum class AstraWidgetType(
    val id: String,
    val title: String,
    val subtitle: String,
    val spanColumns: Int,
    val spanRows: Int
) {
    CLOCK_ATMOS("clock_atmos", "Astra Orbital Clock", "Time, timezone & solar arc", 2, 1),
    WEATHER_CALM("weather_calm", "Atmospheric Weather", "Current conditions, UV & 6-hour trend", 2, 1),
    CALENDAR_AGENDA("calendar_agenda", "Next Up Agenda", "Upcoming schedule & focus blocks", 2, 1),
    BATTERY_TELEMETRY("battery_telemetry", "Power & Thermal", "Device battery, charging & efficiency", 2, 1),
    MEDIA_COMPACT("media_compact", "Now Playing", "Adaptive album atmosphere & transport", 4, 1),
    QUICK_ACTIONS("quick_actions", "Fast Path Matrix", "One-tap search, torch, scanner & timer", 4, 1),
    FOCUS_SCREEN_TIME("focus_screen_time", "Calm Focus", "Screen time balance & deep work timer", 2, 1),
    QUICK_NOTES("quick_notes", "Astra Scratchpad", "Local encrypted quick capture note", 2, 1),
    ANDROID_HOSTED("android_hosted", "System App Widget", "Third-party Android AppWidgetHost view", 4, 2);

    companion object {
        fun fromId(id: String): AstraWidgetType =
            entries.firstOrNull { it.id == id } ?: WEATHER_CALM
    }
}

data class AstraWidgetPlacement(
    val instanceId: Int,
    val widgetType: AstraWidgetType,
    val appWidgetId: Int = -1,
    val providerPackage: String = "com.astra.launcher",
    val providerLabel: String = widgetType.title,
    val pageIndex: Int = 0
)

data class AstraFolderItem(
    val folderId: String,
    val title: String,
    val packageNames: List<String>,
    val pageIndex: Int = 0
)

data class HomeLayout(
    val pageCount: Int = 2,
    val gridColumns: Int = 4,
    val gridRows: Int = 5,
    val pinnedAppsPage0: List<String> = listOf(
        "com.android.Dialer",
        "com.google.android.apps.messaging",
        "com.android.camera2",
        "com.android.settings",
        "com.android.calendar",
        "com.android.calculator2"
    ),
    val pinnedAppsPage1: List<String> = listOf(
        "com.google.android.youtube",
        "com.android.vending",
        "com.google.android.apps.maps",
        "com.android.chrome"
    ),
    val dockPackages: List<String> = listOf(
        "com.android.Dialer",
        "com.google.android.apps.messaging",
        "com.android.chrome",
        "com.android.camera2",
        "com.android.settings"
    ),
    val widgets: List<AstraWidgetPlacement> = listOf(
        AstraWidgetPlacement(
            instanceId = 1001,
            widgetType = AstraWidgetType.WEATHER_CALM,
            pageIndex = 0
        ),
        AstraWidgetPlacement(
            instanceId = 1002,
            widgetType = AstraWidgetType.CALENDAR_AGENDA,
            pageIndex = 0
        ),
        AstraWidgetPlacement(
            instanceId = 1003,
            widgetType = AstraWidgetType.BATTERY_TELEMETRY,
            pageIndex = 1
        ),
        AstraWidgetPlacement(
            instanceId = 1004,
            widgetType = AstraWidgetType.QUICK_NOTES,
            pageIndex = 1
        )
    ),
    val folders: List<AstraFolderItem> = listOf(
        AstraFolderItem(
            folderId = "folder_essentials",
            title = "Essentials",
            packageNames = listOf(
                "com.android.settings",
                "com.android.calculator2",
                "com.android.calendar",
                "com.android.deskclock"
            ),
            pageIndex = 0
        ),
        AstraFolderItem(
            folderId = "folder_creative",
            title = "Studio",
            packageNames = listOf(
                "com.android.camera2",
                "com.google.android.apps.photos",
                "com.google.android.youtube"
            ),
            pageIndex = 0
        )
    ),
    val hiddenPackages: Set<String> = emptySet(),
    val favoritePackages: Set<String> = setOf(
        "com.android.Dialer",
        "com.android.camera2",
        "com.android.settings"
    ),
    val privateSpaceUnlocked: Boolean = false,
    val quickNoteText: String = "Review orbital optics & finalize launch checklist at 16:00."
)

data class SearchPreferences(
    val searchApps: Boolean = true,
    val searchSettings: Boolean = true,
    val searchShortcuts: Boolean = true,
    val searchContacts: Boolean = true,
    val searchCalendar: Boolean = true,
    val searchCommands: Boolean = true,
    val saveSearchHistory: Boolean = true,
    val recentQueries: List<String> = listOf("open camera", "battery settings", "Wi-Fi"),
    val autoFocusKeyboard: Boolean = true
)

enum class NotificationPrivacyMode(val id: String, val label: String) {
    HIDE_SENSITIVE_ON_LOCK("hide_sensitive", "Hide Private Content When Locked"),
    SHOW_ALL("show_all", "Show Full Notification Previews"),
    SUMMARY_ONLY("summary_only", "Count & Source Icon Only");

    companion object {
        fun fromId(id: String): NotificationPrivacyMode =
            entries.firstOrNull { it.id == id } ?: HIDE_SENSITIVE_ON_LOCK
    }
}

data class NotificationPreferences(
    val groupPriorityFirst: Boolean = true,
    val collapseSilentSection: Boolean = true,
    val privacyMode: NotificationPrivacyMode = NotificationPrivacyMode.HIDE_SENSITIVE_ON_LOCK,
    val showBadgeDots: Boolean = true
)

enum class SwipeDownAction(val id: String, val label: String) {
    NOTIFICATION_SHADE("notifications", "Astra Notification Shade"),
    GLOBAL_SEARCH("search", "Global Search & Command Palette"),
    CONTROL_CENTER("controls", "Spatial Control Center");

    companion object {
        fun fromId(id: String): SwipeDownAction =
            entries.firstOrNull { it.id == id } ?: NOTIFICATION_SHADE
    }
}

data class GesturePreferences(
    val swipeUpAction: String = "app_drawer",
    val swipeDownAction: SwipeDownAction = SwipeDownAction.NOTIFICATION_SHADE,
    val doubleTapAction: String = "search",
    val longPressAction: String = "home_editor",
    val pinchAction: String = "personalization_studio",
    val hapticsEnabled: Boolean = true,
    val soundEffectsEnabled: Boolean = true
)

data class PerformancePreferences(
    val animationsEnabled: Boolean = true,
    val reducedMotion: Boolean = false,
    val blurEnabled: Boolean = true,
    val liveWallpaperEffects: Boolean = true,
    val lowEndDeviceModeOverride: Boolean = false
)

enum class AppCategory(val id: String, val label: String) {
    COMMUNICATION("communication", "Communication"),
    MEDIA("media", "Media & Camera"),
    PRODUCTIVITY("productivity", "Productivity"),
    UTILITIES("utilities", "System & Tools"),
    INTERNET("internet", "Browsing & Cloud"),
    FINANCE_LIFESTYLE("lifestyle", "Lifestyle");

    companion object {
        fun fromId(id: String): AppCategory =
            entries.firstOrNull { it.id == id } ?: UTILITIES
    }
}

data class AstraShortcutItem(
    val id: String,
    val shortLabel: String,
    val longLabel: String,
    val packageName: String
)

data class AstraAppEntry(
    val packageName: String,
    val label: String,
    val activityName: String = "",
    val category: AppCategory = AppCategory.UTILITIES,
    val aliases: List<String> = emptyList(),
    val usageScore: Int = 10,
    val lastUsedTimestamp: Long = 0L,
    val isSystemApp: Boolean = false,
    val accentTintHex: Long = 0xFF7DD3FC,
    val shortcuts: List<AstraShortcutItem> = emptyList()
)

enum class NotificationPriorityBucket {
    URGENT,
    REGULAR,
    SILENT
}

data class AstraNotificationEntry(
    val id: String,
    val packageName: String,
    val appName: String,
    val title: String,
    val content: String,
    val timestampLabel: String,
    val bucket: NotificationPriorityBucket,
    val isSensitive: Boolean = false,
    val isClearable: Boolean = true,
    val groupCount: Int = 1
)

data class AstraTaskEntry(
    val taskId: Int,
    val packageName: String,
    val appName: String,
    val lastActiveLabel: String,
    val contextSummary: String,
    val accentHex: Long,
    val supportsSplitScreen: Boolean = true
)

data class AstraMediaState(
    val isPlaying: Boolean = true,
    val trackTitle: String = "Orbital Resonance (Nocturne Mix)",
    val artist: String = "Solaris Ensemble",
    val album: String = "Architecture of Light",
    val progressFraction: Float = 0.42f,
    val elapsedLabel: String = "01:48",
    val durationLabel: String = "04:16",
    val outputDeviceName: String = "Astra Spatial Buds",
    val artworkDominantHex: Long = 0xFF38BDF8,
    val artworkSecondaryHex: Long = 0xFF6366F1
)

/**
 * Capability Matrix (Section 35).
 */
data class AstraCapabilityReport(
    val canBeDefaultHome: Boolean = true,
    val isCurrentlyDefaultHome: Boolean = false,
    val hasNotificationAccess: Boolean = false,
    val canHostWidgets: Boolean = true,
    val canReadPackages: Boolean = true,
    val supportsShortcuts: Boolean = true,
    val supportsExactSystemToggle: Boolean = false, // Normal apps deep-link for restricted settings
    val supportsLockSurface: Boolean = false,       // Preview/companion unless OEM/privileged
    val supportsAodSurface: Boolean = false,        // Preview/companion unless OEM/privileged
    val supportsChargingSurface: Boolean = true,    // Permitted battery receiver visualization
    val supportsAdvancedRecents: Boolean = false    // Requires QuickStep/SystemUI signature for live task bitmaps
)

data class AstraDeviceStatus(
    val batteryPercent: Int = 84,
    val isCharging: Boolean = false,
    val isLowBattery: Boolean = false,
    val isOffline: Boolean = false,
    val wifiEnabled: Boolean = true,
    val wifiSsid: String = "Astra-5G-Studio",
    val bluetoothEnabled: Boolean = true,
    val mobileDataEnabled: Boolean = true,
    val airplaneModeEnabled: Boolean = false,
    val hotspotEnabled: Boolean = false,
    val flashlightEnabled: Boolean = false,
    val autoRotateEnabled: Boolean = true,
    val batterySaverEnabled: Boolean = false,
    val brightnessFraction: Float = 0.72f,
    val volumeFraction: Float = 0.58f,
    val micActiveIndicator: Boolean = false,
    val cameraActiveIndicator: Boolean = false,
    val locationActiveIndicator: Boolean = false,
    val lowMemoryPressure: Boolean = false
)

/**
 * Complete 40-frame visual inventory identifiers (Section 45).
 */
enum class AstraCanonicalFrame(val number: Int, val title: String, val category: String) {
    F01_SPLASH(1, "Splash / Brand Moment", "Foundations"),
    F02_ONBOARDING_01(2, "First-Run Onboarding 01 · Welcome & Home Role", "Onboarding"),
    F03_ONBOARDING_02(3, "First-Run Onboarding 02 · Atmosphere & Essentials", "Onboarding"),
    F04_HOME_DARK(4, "Home Default Dark (Orbit Dawn)", "Home"),
    F05_HOME_LIGHT(5, "Home Default Light (Morning Mist)", "Home"),
    F06_HOME_PERSONALIZED(6, "Home Personalized (Nocturne Flow)", "Home"),
    F07_HOME_EDIT_MODE(7, "Home Edit Mode", "Home"),
    F08_APP_DRAWER(8, "App Drawer / App Library", "Apps"),
    F09_SEARCH_IDLE(9, "Search Idle", "Search"),
    F10_SEARCH_TYPING(10, "Search Typing", "Search"),
    F11_SEARCH_SELECTED(11, "Search Result Selected", "Search"),
    F12_SEARCH_COMMAND(12, "Search Command Mode", "Search"),
    F13_NOTIFICATIONS_COLLAPSED(13, "Notification Shade Collapsed", "Notifications"),
    F14_NOTIFICATIONS_EXPANDED(14, "Notification Shade Expanded", "Notifications"),
    F15_CONTROL_CENTER_PARTIAL(15, "Control Center Partial", "Controls"),
    F16_CONTROL_CENTER_FULL(16, "Control Center Full", "Controls"),
    F17_RECENTS_OVERVIEW(17, "Recents Overview", "Recents"),
    F18_SETTINGS_ROOT(18, "Settings Root (13 Sections)", "Settings"),
    F19_SETTINGS_DETAIL(19, "Settings Detail", "Settings"),
    F20_PERSONALIZATION_STUDIO(20, "Personalization Studio", "Personalization"),
    F21_WALLPAPER_PICKER(21, "Wallpaper Picker", "Personalization"),
    F22_CLOCK_PICKER(22, "Clock Picker", "Personalization"),
    F23_WIDGET_PICKER(23, "Widget Picker", "Widgets"),
    F24_ICON_STYLE_PICKER(24, "Icon Style Picker", "Personalization"),
    F25_PERMISSION_PRE_EXPLAIN(25, "Permission Pre-Explanation", "Permissions"),
    F26_PERMISSION_HANDOFF(26, "System Permission Handoff", "Permissions"),
    F27_PERMISSION_DENIED(27, "Permission Denied State", "Permissions"),
    F28_MEDIA_CONTROLS(28, "Media Controls (Adaptive Artwork)", "Controls"),
    F29_CHARGING(29, "Charging Experience", "System States"),
    F30_LOW_BATTERY(30, "Low Battery State", "System States"),
    F31_OFFLINE(31, "Offline State", "System States"),
    F32_LOADING(32, "Loading Skeleton State", "System States"),
    F33_EMPTY_STATE(33, "Empty State", "System States"),
    F34_ERROR_STATE(34, "Error State", "System States"),
    F35_RECOVERY_STATE(35, "Crash / Recovery Safe Mode", "System States"),
    F36_LOCK_SCREEN(36, "Lock-Screen Concept", "Lock & AOD"),
    F37_AOD_CONCEPT(37, "Always-On Display (AOD) Concept", "Lock & AOD"),
    F38_PRIVACY_INDICATORS(38, "Privacy Indicator State", "Privacy"),
    F39_THIRD_PARTY_WIDGET(39, "Third-Party Widget Coexistence", "Widgets"),
    F40_ACCESSIBILITY_LARGE_TEXT(40, "Accessibility Large-Text Variant", "Accessibility")
}
