# Astra Launcher (`com.astra.launcher`) — Real Android Home Launcher

Astra Launcher is a real, persistent Android Home Launcher (`CATEGORY_HOME` + `CATEGORY_DEFAULT`, `RoleManager.ROLE_HOME`) engineered with a 2D coordinate grid workspace, real `LauncherApps` & `PackageManager` discovery, real application icon pipeline with memory caching and live package invalidation, genuine Android `AppWidgetHost` / `AppWidgetManager` widget hosting, and a keyboard-first universal launcher search overlay.

---

## Core Launcher Architecture (`AstraLauncherRoot`)

Unlike fake-OS screen-switcher apps, Astra Launcher is architected around a **persistent Home Workspace** (`AstraLauncherRoot`) where pressing the Android Home button (`Intent.ACTION_MAIN` + `Intent.CATEGORY_HOME` delivered via `onNewIntent`) always returns to the resting workspace:

```text
AstraLauncherActivity (singleTask, CATEGORY_HOME, CATEGORY_DEFAULT, stateNotNeeded="true")
└── AstraLauncherRoot
    ├── AstraWallpaperSurface (Bundled Astra Wallpapers OR Android System Wallpaper via windowShowWallpaper)
    ├── AstraWorkspaceLayer (Persistent Multi-Page 2D Coordinate Grid + Live System Clock + PageIndicator + Dock)
    ├── AstraAppDrawerOverlay (Real installed apps via LauncherApps, Work Profile tab, A–Z fast scroller, filter bar)
    ├── AstraSearchOverlay (Keyboard-first search: Enter-to-launch #1, Escape-to-close, arrow navigation, IME-safe)
    ├── AstraFolderOverlay (Create by dropping app onto app, rename, reorder, add/remove apps, auto-delete when empty)
    ├── AstraBoundWidgetHostCell & AstraWidgetPickerSheet (Real AppWidgetHost ID 2026 & AppWidgetManager binding)
    ├── AstraAppContextMenuSheet (Real LauncherApps ShortcutQuery shortcuts, Move, Pin, Hide, App Info, Uninstall)
    ├── AstraPersonalizationOverlay (Wallpaper source, Orbit/Nocturne/Horizon families, Icon treatments, Grid size)
    └── AstraSettingsOverlay (All 14 persistent launcher settings categories)
```

---

## Module Structure (11 Modules)

| Module | Responsibility |
|---|---|
| `:app` | `AstraLauncherActivity` (`CATEGORY_HOME`, `onNewIntent`, `AppWidgetHost` & `ROLE_HOME` lifecycle), `AstraNotificationListenerService`, `AstraOrbitalClockWidgetProvider`, Robolectric acceptance tests |
| `:core-storage` | 2D coordinate workspace model (`WorkspaceCellItem`, `DockSlotItem`, `FolderMemberApp`), folder merge/auto-delete engine, corruption recovery, 14 settings persistence |
| `:core-platform` | `AstraRoleHomeManager`, `AstraPackageRepository` (`LauncherApps` + live `LauncherApps.Callback` & package `BroadcastReceiver`), `AstraIconPipeline` (`LauncherActivityInfo` -> normalized `Bitmap` -> `LruCache`), `AstraWidgetHostManager` (`AppWidgetHost` + `AppWidgetManager`), `AstraSystemController` (`StatusBarManager` shade expansion, `WallpaperManager`) |
| `:core-design` | WCAG contrast-enforced `AstraColorEngine`, live system `AstraClock`, real-icon `AstraAppIcon`, `AstraWallpaperSurface`, `AstraDock` |
| `:core-performance` | Low-RAM visual budget engine (`ActivityManager.isLowRamDevice`, adaptive cache sizing) |
| `:feature-home` | Multi-page 2D coordinate grid workspace, drag-and-drop / cell move & folder merge, `AstraFolderOverlay`, `AstraAppContextMenuSheet`, `AstraEditModeBar` |
| `:feature-apps` | Real App Drawer / App Library with Work Profile support, A–Z fast-scroll rail, and live filtering |
| `:feature-search` | Keyboard-first search index & overlay with real apps, shortcuts, settings handoffs, and deterministic math/unit conversion |
| `:feature-widgets` | Real `AppWidgetHostView` cell container (`AndroidView`) + `AppWidgetManager.installedProviders` picker |
| `:feature-personalization` | Personalization Studio for system vs bundled wallpapers, icon treatments, clock styles, and grid density |
| `:feature-settings` | All 14 persistent launcher configuration categories |
