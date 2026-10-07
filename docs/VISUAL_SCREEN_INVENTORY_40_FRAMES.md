# Astra Launcher — 40-Frame Visual Screen Inventory & Capability Matrix
### Companion to `ASTRA_LAUNCHER_MASTER_DESIGN_BUILD_SPEC.md` (Sections 34, 35, 45, 47, 51, 52, 55)

---

## 1. Complete 40-Frame Visual Screen Inventory (Section 45)
Every frame below is implemented both in the native Android Jetpack Compose application (`AstraCanonicalFrame` in `:core-storage` & `:app`) and in the Interactive Web Visual Review Board (`design-review-portal/`):

| Frame # | Canonical ID | Title | Android Composable / State |
|---|---|---|---|
| **01** | `F01_SPLASH` | Splash / Brand Moment | `AstraOnboardingScreen(step = 1)` with luminous `AstraBrandMark` |
| **02** | `F02_ONBOARDING_01` | First-Run Onboarding 01 · Welcome & Home Role | `AstraOnboardingScreen(step = 2)` (`RoleManager.ROLE_HOME`) |
| **03** | `F03_ONBOARDING_02` | First-Run Onboarding 02 · Atmosphere & Essentials | `AstraOnboardingScreen(step = 3)` (5 Theme Presets) |
| **04** | `F04_HOME_DARK` | Home Default Dark (`Orbit Dawn`) | `AstraHomeScreen` + `AstraThemePreset.ASTRAL` |
| **05** | `F05_HOME_LIGHT` | Home Default Light (`Morning Mist`) | `AstraHomeScreen` + `AstraThemePreset.MORNING` |
| **06** | `F06_HOME_PERSONALIZED` | Home Personalized (`Nocturne Flow` + Orbital Clock) | `AstraHomeScreen` + `AstraThemePreset.NOCTURNE` |
| **07** | `F07_HOME_EDIT_MODE` | Home Edit Mode | `AstraHomeScreen(initialEditMode = true)` + `AstraHomeEditorPanel` |
| **08** | `F08_APP_DRAWER` | App Drawer / App Library | `AstraAppDrawerScreen` (All, Categories, Favorites, Private) |
| **09** | `F09_SEARCH_IDLE` | Search Idle | `AstraSearchScreen(query = "")` |
| **10** | `F10_SEARCH_TYPING` | Search Typing | `AstraSearchScreen(query = "Cam")` |
| **11** | `F11_SEARCH_SELECTED` | Search Result Selected | `AstraSearchScreen` with keyboard index selection |
| **12** | `F12_SEARCH_COMMAND` | Search Command Mode | `AstraSearchScreen(query = "open YouTube")` |
| **13** | `F13_NOTIFICATIONS_COLLAPSED` | Notification Shade Collapsed | `AstraNotificationShadeScreen(initiallyCollapsed = true)` |
| **14** | `F14_NOTIFICATIONS_EXPANDED` | Notification Shade Expanded | `AstraNotificationShadeScreen(initiallyCollapsed = false)` |
| **15** | `F15_CONTROL_CENTER_PARTIAL` | Control Center Partial | `AstraControlCenterScreen(initiallyPartial = true)` |
| **16** | `F16_CONTROL_CENTER_FULL` | Control Center Full | `AstraControlCenterScreen(initiallyPartial = false)` |
| **17** | `F17_RECENTS_OVERVIEW` | Recents Overview | `AstraRecentsScreen` |
| **18** | `F18_SETTINGS_ROOT` | Settings Root (13 Sections) | `AstraSettingsScreen(initialDetailSection = null)` |
| **19** | `F19_SETTINGS_DETAIL` | Settings Detail | `AstraSettingsScreen(initialDetailSection = PERFORMANCE)` |
| **20** | `F20_PERSONALIZATION_STUDIO` | Personalization Studio | `AstraPersonalizationStudioScreen(PRESETS)` |
| **21** | `F21_WALLPAPER_PICKER` | Wallpaper Picker | `AstraPersonalizationStudioScreen(WALLPAPERS)` |
| **22** | `F22_CLOCK_PICKER` | Clock Picker | `AstraPersonalizationStudioScreen(CLOCKS)` |
| **23** | `F23_WIDGET_PICKER` | Widget Picker | `AstraWidgetPickerSheet` |
| **24** | `F24_ICON_STYLE_PICKER` | Icon Style Picker | `AstraPersonalizationStudioScreen(ICONS)` |
| **25** | `F25_PERMISSION_PRE_EXPLAIN` | Permission Pre-Explanation | `AstraNotificationShadeScreen` consent explanation card |
| **26** | `F26_PERMISSION_HANDOFF` | System Permission Handoff | `Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS` / `ROLE_HOME` |
| **27** | `F27_PERMISSION_DENIED` | Permission Denied State | `AstraNotificationShadeScreen` fallback state |
| **28** | `F28_MEDIA_CONTROLS` | Media Controls | `AstraMediaCard` with dynamic album-art tint |
| **29** | `F29_CHARGING` | Charging Experience | `AstraChargingOverlay` orbital pulse |
| **30** | `F30_LOW_BATTERY` | Low Battery State | `AstraHomeScreen` + `AstraVisualBudget` calm power mode |
| **31** | `F31_OFFLINE` | Offline State | `AstraHomeScreen` (`Astra is offline.` local guarantee banner) |
| **32** | `F32_LOADING` | Loading State | `AstraNonHappyStateCard(LOADING_SKELETON)` |
| **33** | `F33_EMPTY_STATE` | Empty State | `AstraNonHappyStateCard(EMPTY_HOME)` |
| **34** | `F34_ERROR_STATE` | Error State | `AstraNonHappyStateCard(ERROR_STATE)` |
| **35** | `F35_RECOVERY_STATE` | Crash / Recovery State | `AstraNonHappyStateCard(RECOVERY_SAFE_MODE)` |
| **36** | `F36_LOCK_SCREEN` | Lock-Screen Concept | `AstraLockScreen` |
| **37** | `F37_AOD_CONCEPT` | Always-On Display (AOD) Concept | `AstraAodScreen` with burn-in pixel shift |
| **38** | `F38_PRIVACY_INDICATORS` | Privacy Indicator State | `AstraStatusBar` (`CAM · MIC · LOC` pill) |
| **39** | `F39_THIRD_PARTY_WIDGET` | Third-Party Widget Coexistence | `AstraWidgetHostCard(ANDROID_HOSTED)` (`AppWidgetHost` #2026) |
| **40** | `F40_ACCESSIBILITY_LARGE_TEXT` | Accessibility Large-Text Variant | `AstraThemeProvider` with `1.30f` text scale & high contrast |

---

## 2. Platform Reality & Capability Matrix (Sections 34 & 35)
| Capability Flag | Public Android API Mechanism | Fallback / Truthful Behavior |
|---|---|---|
| `canBeDefaultHome` | `RoleManager.ROLE_HOME` (API 29+) + `CATEGORY_HOME` in manifest | Opens `RoleManager.createRequestRoleIntent` or `ACTION_HOME_SETTINGS` |
| `hasNotificationAccess` | `NotificationListenerService` + `enabled_notification_listeners` | Shows pre-explanation card & preview stream without crashing |
| `canHostWidgets` | `AppWidgetManager` + `AppWidgetHost(context, 2026)` | Coexists with 8 first-party Astra widgets |
| `canReadPackages` | `LauncherApps.getActivityList` + `QUERY_ALL_PACKAGES` | Local LRU cache + offline search index |
| `supportsShortcuts` | `LauncherApps.getShortcuts` when default launcher | Semantic deep shortcuts for common system apps |
| `supportsExactSystemToggle` | `CameraManager.setTorchMode` & `AudioManager` direct; `Settings.Panel` for Wi-Fi/Cellular | Never fakes restricted toggles; launches official Android Settings Panel |
| `supportsLockSurface` | Companion `AstraLockScreen` surface | Clearly labeled as Lock Companion without bypassing Android Keyguard |
| `supportsAodSurface` | Companion `AstraAodScreen` with burn-in pixel shift | Progressive enhancement companion |
| `supportsChargingSurface` | `ACTION_POWER_CONNECTED` broadcast receiver | Brief orbital charging pulse overlay |
| `supportsAdvancedRecents` | `AstraRecentsScreen` task cards | Honest platform notice regarding OEM QuickStep signature privilege |
