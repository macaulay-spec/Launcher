# Astra Launcher (`com.astra.launcher`) — v2.1.0 Product, UX & System Experience

**Astra Launcher** is a native Kotlin & Jetpack Compose Android Home Launcher (`CATEGORY_HOME` + `CATEGORY_DEFAULT` + `RoleManager.ROLE_HOME` + `singleTask` + `resumeWhilePausing="true"`) engineered for **Android 10+ (API 29–35)** and **TECNO / HiOS** devices.

Every screen in Astra is designed to feel calm, atmospheric, tactile, and intelligent—from the **6-Step First-Run Setup ("Your phone, redesigned.")** to the **Modern Application Discovery Surface**, **System-Level Natural-Language Search**, **Composite Live Wallpaper Studio**, and **Persistent 2D Spatial Home Workspace**.

---

## 1. Product & Architectural Highlights (`Rebuild` Sections 1–50)

| Pillar | Implementation |
|---|---|
| **Real Default Home Ownership & TECNO / HiOS Compatibility** | `AstraLauncherActivity` declares `android.intent.action.MAIN` + `android.intent.category.HOME` + `android.intent.category.DEFAULT` (`priority="1000"`), `launchMode="singleTask"`, `clearTaskOnLaunch="true"`, `stateNotNeeded="true"`, and `resumeWhilePausing="true"`. `AstraRoleHomeManager` detects the active default home holder (`resolveActivity(CATEGORY_HOME)`) and guides TECNO/HiOS users through `RoleManager.ROLE_HOME` or `Settings.ACTION_HOME_SETTINGS`. |
| **6-Step First-Run Experience ("Your phone, redesigned.")** | `AstraFirstRunSetupOverlay` welcomes the user on first launch: **(1)** Choose Astra Atmosphere (`ORBIT`, `NOCTURNE`, `HORIZON`) with live preview -> **(2)** Choose Home Density (`Minimal`, `Balanced`, `Dense`) -> **(3)** Choose Icon Treatment (`Original`, `Astra Adaptive`, `Monochrome`) -> **(4)** Choose Home Gestures -> **(5)** Optional Integrations -> **(6)** Set Default Home & *"Welcome to Astra."* |
| **Hero Home Screen** | Large adaptive clock, live date, contextual time-of-day header (*Morning Focus / Daytime Flow / Evening Calm / Night Quiet*) with real battery & notification pill, generous negative space, spatial 2D grid (`4×5` / `4×6` / `5×5`), smart folders with 2×2 real icon previews, bound Android widgets, and an ergonomic glass/matte dock. |
| **Modern Application Discovery Surface** | Replaces primitive A–Z grids with a multi-layered discovery experience: **Top Search Bar** -> **Recently Used** strip -> **Favorites & Frequent** grid -> **Contextual Time-of-Day Suggestions** -> **Smart Categories Bento Grid** (*Communication*, *Entertainment & Media*, *Productivity*, *Utilities & System*, *Browsing & Social*, *Games & Other*) -> **All Apps** (with *Most Used* / *A–Z* toggle and alphabetical rail). |
| **System-Level Search & Natural-Language Actions** | Supports queries like `"open YouTube"`, `"launch Spotify"`, `"wallpaper"`, `"edit home"`, `"turn on Wi-Fi"`, `"Bluetooth"`, `"245 * 18"`, and `"100 km to mi"`. System settings queries hand off truthfully to Android system panels without faking privileged state toggles. |
| **Live Composite Wallpaper & Style Studio** | Interactive side-by-side preview cards for **ORBIT**, **NOCTURNE FLOW**, and **HORIZON** rendering the real Home clock, icons, widgets, and dock before applying to Astra or the Android system `WallpaperManager`. |
| **Truthful Control & Notification Surface** | Displays real device battery, ringer mode, Wi-Fi state, next alarm, and real active `NotificationListenerService` notifications (or an honest permission card + one-tap handoff to Android's native Notification Shade / Quick Settings). |
| **Zero Ads & 100% Local Privacy** | Zero ad SDKs, zero sponsored search results, zero promotional cards, and zero cloud telemetry. |

---

## 2. 11-Module Gradle Architecture

1. **`:app`** — `AstraLauncherActivity`, `AstraNotificationListenerService`, `AstraOrbitalClockWidgetProvider`, `AndroidManifest.xml`, and Robolectric acceptance tests.
2. **`:core-storage`** — Versioned JSON persistence (`SCHEMA_VERSION = 3`) for 2D workspace items, `HomeDensityMode`, `hasCompletedFirstRunSetup`, `favoriteComponents`, smart folder naming (`suggestSmartFolderName`), and corruption recovery.
3. **`:core-platform`** — `AstraPackageRepository` (`LauncherApps` + `PackageManager`), `AstraIconPipeline` (real `AdaptiveIconDrawable` + `LruCache`), `AstraWidgetHostManager` (`AppWidgetHost`), `AstraRoleHomeManager` (with TECNO/HiOS detection), and `AstraSystemController`.
4. **`:core-design`** — `AstraColorEngine` (WCAG 4.5:1 contrast enforcement), `AstraWallpaperSurface` (`ORBIT`, `NOCTURNE FLOW`, `HORIZON`), `AstraRealAppIconTile`, and `AstraHomeDock`.
5. **`:core-performance`** — `AstraPerformanceManager` (`ActivityManager.isLowRamDevice` and battery-saver budgets).
6. **`:feature-home`** — `AstraWorkspaceLayer`, `AstraFirstRunSetupOverlay`, `AstraNotificationAndControlOverlay`, `AstraFolderOverlay`, `AstraAppContextMenuSheet`, and `AstraEditModeBar`.
7. **`:feature-apps`** — `AstraAppDrawerOverlay` (Modern Application Discovery + Smart Categories + All Apps + Hidden Apps).
8. **`:feature-search`** — `AstraSearchOverlay` and `AstraSearchIndex` (natural-language query parser, apps, shortcuts, settings, Astra surfaces, and calculator/converter).
9. **`:feature-widgets`** — `AstraWidgetPickerSheet` and `AstraWorkspaceWidgetContainer`.
10. **`:feature-personalization`** — `AstraPersonalizationOverlay` with live composite Home previews for `ORBIT`, `NOCTURNE FLOW`, and `HORIZON`.
11. **`:feature-settings`** — `AstraSettingsOverlay` (`Home`, `Appearance`, `Search`, `Gestures`, `Apps`, `Notifications`, `Widgets`, `Performance`, `About Astra`).
