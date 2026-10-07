package com.astra.launcher.core.storage

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Local-first persistence engine for Astra Launcher (Section 36).
 * Persists ThemeSettings, HomeLayout, SearchPreferences, NotificationPreferences,
 * GesturePreferences, PerformancePreferences, and Onboarding completion state.
 * Also supports full deterministic backup/restore and safe-mode layout reset.
 */
class AstraStorageRepository(context: Context? = null) {

    private val prefs: SharedPreferences? = context?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val inMemoryStore = mutableMapOf<String, String>()

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

    private val _onboardingCompleted = MutableStateFlow(getString(KEY_ONBOARDING_DONE, "false") == "true")
    val onboardingCompleted: StateFlow<Boolean> = _onboardingCompleted.asStateFlow()

    private fun getString(key: String, default: String): String {
        return prefs?.getString(key, default) ?: inMemoryStore[key] ?: default
    }

    private fun putString(key: String, value: String) {
        inMemoryStore[key] = value
        prefs?.edit()?.putString(key, value)?.apply()
    }

    fun setOnboardingCompleted(completed: Boolean) {
        putString(KEY_ONBOARDING_DONE, completed.toString())
        _onboardingCompleted.value = completed
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
    }

    fun exportBackupSnapshot(): String {
        val t = _themeSettings.value
        val h = _homeLayout.value
        val p = _performancePreferences.value
        return buildString {
            appendLine("ASTRA_BACKUP_V1")
            appendLine("themePreset=${t.themePreset.id}")
            appendLine("wallpaperId=${t.wallpaperId.id}")
            appendLine("themeMode=${t.themeMode.id}")
            appendLine("clockStyle=${t.clockStyle.id}")
            appendLine("iconStyle=${t.iconStyle.id}")
            appendLine("accentSource=${t.accentSource.id}")
            appendLine("textScale=${t.textScaleMultiplier}")
            appendLine("highContrast=${t.highContrastMode}")
            appendLine("gridColumns=${h.gridColumns}")
            appendLine("gridRows=${h.gridRows}")
            appendLine("dock=${h.dockPackages.joinToString(",")}")
            appendLine("favorites=${h.favoritePackages.joinToString(",")}")
            appendLine("hidden=${h.hiddenPackages.joinToString(",")}")
            appendLine("reducedMotion=${p.reducedMotion}")
            appendLine("blurEnabled=${p.blurEnabled}")
        }
    }

    fun importBackupSnapshot(payload: String): Boolean {
        if (!payload.startsWith("ASTRA_BACKUP_V1")) return false
        val map = payload.lines()
            .drop(1)
            .filter { it.contains("=") }
            .associate { line ->
                val idx = line.indexOf('=')
                line.substring(0, idx).trim() to line.substring(idx + 1).trim()
            }

        updateThemeSettings { current ->
            current.copy(
                themePreset = map["themePreset"]?.let(AstraThemePreset::fromId) ?: current.themePreset,
                wallpaperId = map["wallpaperId"]?.let(AstraWallpaperId::fromId) ?: current.wallpaperId,
                themeMode = map["themeMode"]?.let(AstraThemeMode::fromId) ?: current.themeMode,
                clockStyle = map["clockStyle"]?.let(AstraClockStyle::fromId) ?: current.clockStyle,
                iconStyle = map["iconStyle"]?.let(AstraIconStyle::fromId) ?: current.iconStyle,
                accentSource = map["accentSource"]?.let(AstraAccentSource::fromId) ?: current.accentSource,
                textScaleMultiplier = map["textScale"]?.toFloatOrNull() ?: current.textScaleMultiplier,
                highContrastMode = map["highContrast"]?.toBooleanStrictOrNull() ?: current.highContrastMode
            )
        }
        updateHomeLayout { current ->
            current.copy(
                gridColumns = map["gridColumns"]?.toIntOrNull() ?: current.gridColumns,
                gridRows = map["gridRows"]?.toIntOrNull() ?: current.gridRows,
                dockPackages = map["dock"]?.split(",")?.filter { it.isNotBlank() } ?: current.dockPackages,
                favoritePackages = map["favorites"]?.split(",")?.filter { it.isNotBlank() }?.toSet() ?: current.favoritePackages,
                hiddenPackages = map["hidden"]?.split(",")?.filter { it.isNotBlank() }?.toSet() ?: current.hiddenPackages
            )
        }
        updatePerformancePreferences { current ->
            current.copy(
                reducedMotion = map["reducedMotion"]?.toBooleanStrictOrNull() ?: current.reducedMotion,
                blurEnabled = map["blurEnabled"]?.toBooleanStrictOrNull() ?: current.blurEnabled
            )
        }
        return true
    }

    private fun saveThemeSettings(s: ThemeSettings) {
        putString(KEY_THEME_PRESET, s.themePreset.id)
        putString(KEY_WALLPAPER_ID, s.wallpaperId.id)
        putString(KEY_ACCENT_SOURCE, s.accentSource.id)
        putString(KEY_THEME_MODE, s.themeMode.id)
        putString(KEY_ICON_STYLE, s.iconStyle.id)
        putString(KEY_CLOCK_STYLE, s.clockStyle.id)
        putString(KEY_CUSTOM_ACCENT, s.customAccentHex.toString())
        putString(KEY_ICON_SCALE, s.iconScale.toString())
        putString(KEY_SHOW_LABELS, s.showIconLabels.toString())
        putString(KEY_DOCK_GLASS, s.dockStyleGlass.toString())
        putString(KEY_CC_COMPACT, s.controlCenterCompact.toString())
        putString(KEY_TEXT_SCALE, s.textScaleMultiplier.toString())
        putString(KEY_HIGH_CONTRAST, s.highContrastMode.toString())
    }

    private fun loadThemeSettings(): ThemeSettings {
        val default = ThemeSettings()
        return ThemeSettings(
            themePreset = AstraThemePreset.fromId(getString(KEY_THEME_PRESET, default.themePreset.id)),
            wallpaperId = AstraWallpaperId.fromId(getString(KEY_WALLPAPER_ID, default.wallpaperId.id)),
            accentSource = AstraAccentSource.fromId(getString(KEY_ACCENT_SOURCE, default.accentSource.id)),
            themeMode = AstraThemeMode.fromId(getString(KEY_THEME_MODE, default.themeMode.id)),
            iconStyle = AstraIconStyle.fromId(getString(KEY_ICON_STYLE, default.iconStyle.id)),
            clockStyle = AstraClockStyle.fromId(getString(KEY_CLOCK_STYLE, default.clockStyle.id)),
            customAccentHex = getString(KEY_CUSTOM_ACCENT, default.customAccentHex.toString()).toLongOrNull() ?: default.customAccentHex,
            iconScale = getString(KEY_ICON_SCALE, default.iconScale.toString()).toFloatOrNull() ?: default.iconScale,
            showIconLabels = getString(KEY_SHOW_LABELS, default.showIconLabels.toString()) == "true",
            dockStyleGlass = getString(KEY_DOCK_GLASS, default.dockStyleGlass.toString()) == "true",
            controlCenterCompact = getString(KEY_CC_COMPACT, default.controlCenterCompact.toString()) == "true",
            textScaleMultiplier = getString(KEY_TEXT_SCALE, default.textScaleMultiplier.toString()).toFloatOrNull() ?: default.textScaleMultiplier,
            highContrastMode = getString(KEY_HIGH_CONTRAST, default.highContrastMode.toString()) == "true"
        )
    }

    private fun saveHomeLayout(h: HomeLayout) {
        putString(KEY_GRID_COLS, h.gridColumns.toString())
        putString(KEY_GRID_ROWS, h.gridRows.toString())
        putString(KEY_PAGE_COUNT, h.pageCount.toString())
        putString(KEY_PINNED_P0, h.pinnedAppsPage0.joinToString(","))
        putString(KEY_PINNED_P1, h.pinnedAppsPage1.joinToString(","))
        putString(KEY_DOCK_PKGS, h.dockPackages.joinToString(","))
        putString(KEY_HIDDEN_PKGS, h.hiddenPackages.joinToString(","))
        putString(KEY_FAV_PKGS, h.favoritePackages.joinToString(","))
        putString(KEY_QUICK_NOTE, h.quickNoteText)
        val widgetsEncoded = h.widgets.joinToString(";") { w ->
            "${w.instanceId}|${w.widgetType.id}|${w.appWidgetId}|${w.providerPackage}|${w.providerLabel}|${w.pageIndex}"
        }
        putString(KEY_WIDGETS, widgetsEncoded)
        val foldersEncoded = h.folders.joinToString(";") { f ->
            "${f.folderId}|${f.title}|${f.packageNames.joinToString(",")}|${f.pageIndex}"
        }
        putString(KEY_FOLDERS, foldersEncoded)
    }

    private fun loadHomeLayout(): HomeLayout {
        val d = HomeLayout()
        val widgetsRaw = getString(KEY_WIDGETS, "")
        val parsedWidgets = if (widgetsRaw.isBlank()) {
            d.widgets
        } else {
            widgetsRaw.split(";").mapNotNull { token ->
                val parts = token.split("|")
                if (parts.size >= 6) {
                    AstraWidgetPlacement(
                        instanceId = parts[0].toIntOrNull() ?: 1000,
                        widgetType = AstraWidgetType.fromId(parts[1]),
                        appWidgetId = parts[2].toIntOrNull() ?: -1,
                        providerPackage = parts[3],
                        providerLabel = parts[4],
                        pageIndex = parts[5].toIntOrNull() ?: 0
                    )
                } else null
            }
        }
        val foldersRaw = getString(KEY_FOLDERS, "")
        val parsedFolders = if (foldersRaw.isBlank()) {
            d.folders
        } else {
            foldersRaw.split(";").mapNotNull { token ->
                val parts = token.split("|")
                if (parts.size >= 4) {
                    AstraFolderItem(
                        folderId = parts[0],
                        title = parts[1],
                        packageNames = parts[2].split(",").filter { it.isNotBlank() },
                        pageIndex = parts[3].toIntOrNull() ?: 0
                    )
                } else null
            }
        }
        return HomeLayout(
            pageCount = getString(KEY_PAGE_COUNT, d.pageCount.toString()).toIntOrNull() ?: d.pageCount,
            gridColumns = getString(KEY_GRID_COLS, d.gridColumns.toString()).toIntOrNull() ?: d.gridColumns,
            gridRows = getString(KEY_GRID_ROWS, d.gridRows.toString()).toIntOrNull() ?: d.gridRows,
            pinnedAppsPage0 = getString(KEY_PINNED_P0, d.pinnedAppsPage0.joinToString(",")).split(",").filter { it.isNotBlank() },
            pinnedAppsPage1 = getString(KEY_PINNED_P1, d.pinnedAppsPage1.joinToString(",")).split(",").filter { it.isNotBlank() },
            dockPackages = getString(KEY_DOCK_PKGS, d.dockPackages.joinToString(",")).split(",").filter { it.isNotBlank() },
            hiddenPackages = getString(KEY_HIDDEN_PKGS, "").split(",").filter { it.isNotBlank() }.toSet(),
            favoritePackages = getString(KEY_FAV_PKGS, d.favoritePackages.joinToString(",")).split(",").filter { it.isNotBlank() }.toSet(),
            widgets = parsedWidgets,
            folders = parsedFolders,
            quickNoteText = getString(KEY_QUICK_NOTE, d.quickNoteText)
        )
    }

    private fun saveSearchPreferences(s: SearchPreferences) {
        putString(KEY_SEARCH_APPS, s.searchApps.toString())
        putString(KEY_SEARCH_SETTINGS, s.searchSettings.toString())
        putString(KEY_SEARCH_SHORTCUTS, s.searchShortcuts.toString())
        putString(KEY_SEARCH_CONTACTS, s.searchContacts.toString())
        putString(KEY_SEARCH_COMMANDS, s.searchCommands.toString())
        putString(KEY_SEARCH_RECENT, s.recentQueries.joinToString("|"))
    }

    private fun loadSearchPreferences(): SearchPreferences {
        val d = SearchPreferences()
        return SearchPreferences(
            searchApps = getString(KEY_SEARCH_APPS, d.searchApps.toString()) == "true",
            searchSettings = getString(KEY_SEARCH_SETTINGS, d.searchSettings.toString()) == "true",
            searchShortcuts = getString(KEY_SEARCH_SHORTCUTS, d.searchShortcuts.toString()) == "true",
            searchContacts = getString(KEY_SEARCH_CONTACTS, d.searchContacts.toString()) == "true",
            searchCommands = getString(KEY_SEARCH_COMMANDS, d.searchCommands.toString()) == "true",
            recentQueries = getString(KEY_SEARCH_RECENT, d.recentQueries.joinToString("|")).split("|").filter { it.isNotBlank() }
        )
    }

    private fun saveNotificationPreferences(n: NotificationPreferences) {
        putString(KEY_NOTIF_PRIORITY, n.groupPriorityFirst.toString())
        putString(KEY_NOTIF_SILENT, n.collapseSilentSection.toString())
        putString(KEY_NOTIF_PRIVACY, n.privacyMode.id)
        putString(KEY_NOTIF_BADGES, n.showBadgeDots.toString())
    }

    private fun loadNotificationPreferences(): NotificationPreferences {
        val d = NotificationPreferences()
        return NotificationPreferences(
            groupPriorityFirst = getString(KEY_NOTIF_PRIORITY, d.groupPriorityFirst.toString()) == "true",
            collapseSilentSection = getString(KEY_NOTIF_SILENT, d.collapseSilentSection.toString()) == "true",
            privacyMode = NotificationPrivacyMode.fromId(getString(KEY_NOTIF_PRIVACY, d.privacyMode.id)),
            showBadgeDots = getString(KEY_NOTIF_BADGES, d.showBadgeDots.toString()) == "true"
        )
    }

    private fun saveGesturePreferences(g: GesturePreferences) {
        putString(KEY_GESTURE_DOWN, g.swipeDownAction.id)
        putString(KEY_HAPTICS, g.hapticsEnabled.toString())
        putString(KEY_SOUNDS, g.soundEffectsEnabled.toString())
    }

    private fun loadGesturePreferences(): GesturePreferences {
        val d = GesturePreferences()
        return GesturePreferences(
            swipeDownAction = SwipeDownAction.fromId(getString(KEY_GESTURE_DOWN, d.swipeDownAction.id)),
            hapticsEnabled = getString(KEY_HAPTICS, d.hapticsEnabled.toString()) == "true",
            soundEffectsEnabled = getString(KEY_SOUNDS, d.soundEffectsEnabled.toString()) == "true"
        )
    }

    private fun savePerformancePreferences(p: PerformancePreferences) {
        putString(KEY_PERF_ANIM, p.animationsEnabled.toString())
        putString(KEY_PERF_REDUCED_MOTION, p.reducedMotion.toString())
        putString(KEY_PERF_BLUR, p.blurEnabled.toString())
        putString(KEY_PERF_LIVE_WP, p.liveWallpaperEffects.toString())
        putString(KEY_PERF_LOW_END, p.lowEndDeviceModeOverride.toString())
    }

    private fun loadPerformancePreferences(): PerformancePreferences {
        val d = PerformancePreferences()
        return PerformancePreferences(
            animationsEnabled = getString(KEY_PERF_ANIM, d.animationsEnabled.toString()) == "true",
            reducedMotion = getString(KEY_PERF_REDUCED_MOTION, d.reducedMotion.toString()) == "true",
            blurEnabled = getString(KEY_PERF_BLUR, d.blurEnabled.toString()) == "true",
            liveWallpaperEffects = getString(KEY_PERF_LIVE_WP, d.liveWallpaperEffects.toString()) == "true",
            lowEndDeviceModeOverride = getString(KEY_PERF_LOW_END, d.lowEndDeviceModeOverride.toString()) == "true"
        )
    }

    companion object {
        private const val PREFS_NAME = "astra_launcher_state_v1"
        private const val KEY_ONBOARDING_DONE = "onboarding_done"
        private const val KEY_THEME_PRESET = "theme_preset"
        private const val KEY_WALLPAPER_ID = "wallpaper_id"
        private const val KEY_ACCENT_SOURCE = "accent_source"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_ICON_STYLE = "icon_style"
        private const val KEY_CLOCK_STYLE = "clock_style"
        private const val KEY_CUSTOM_ACCENT = "custom_accent"
        private const val KEY_ICON_SCALE = "icon_scale"
        private const val KEY_SHOW_LABELS = "show_labels"
        private const val KEY_DOCK_GLASS = "dock_glass"
        private const val KEY_CC_COMPACT = "cc_compact"
        private const val KEY_TEXT_SCALE = "text_scale"
        private const val KEY_HIGH_CONTRAST = "high_contrast"

        private const val KEY_GRID_COLS = "grid_cols"
        private const val KEY_GRID_ROWS = "grid_rows"
        private const val KEY_PAGE_COUNT = "page_count"
        private const val KEY_PINNED_P0 = "pinned_p0"
        private const val KEY_PINNED_P1 = "pinned_p1"
        private const val KEY_DOCK_PKGS = "dock_pkgs"
        private const val KEY_HIDDEN_PKGS = "hidden_pkgs"
        private const val KEY_FAV_PKGS = "fav_pkgs"
        private const val KEY_WIDGETS = "widgets_v1"
        private const val KEY_FOLDERS = "folders_v1"
        private const val KEY_QUICK_NOTE = "quick_note"

        private const val KEY_SEARCH_APPS = "search_apps"
        private const val KEY_SEARCH_SETTINGS = "search_settings"
        private const val KEY_SEARCH_SHORTCUTS = "search_shortcuts"
        private const val KEY_SEARCH_CONTACTS = "search_contacts"
        private const val KEY_SEARCH_COMMANDS = "search_commands"
        private const val KEY_SEARCH_RECENT = "search_recent"

        private const val KEY_NOTIF_PRIORITY = "notif_priority"
        private const val KEY_NOTIF_SILENT = "notif_silent"
        private const val KEY_NOTIF_PRIVACY = "notif_privacy"
        private const val KEY_NOTIF_BADGES = "notif_badges"

        private const val KEY_GESTURE_DOWN = "gesture_down"
        private const val KEY_HAPTICS = "haptics"
        private const val KEY_SOUNDS = "sounds"

        private const val KEY_PERF_ANIM = "perf_anim"
        private const val KEY_PERF_REDUCED_MOTION = "perf_reduced_motion"
        private const val KEY_PERF_BLUR = "perf_blur"
        private const val KEY_PERF_LIVE_WP = "perf_live_wp"
        private const val KEY_PERF_LOW_END = "perf_low_end"
    }
}
