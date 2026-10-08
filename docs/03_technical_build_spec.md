# Astra Launcher — Technical Build Specification

**Version:** 1.0 — **Date:** 2026-10-08 — **Status:** APPROVED

---

## 1. Stack
- **Language:** Kotlin (100%)
- **UI:** Jetpack Compose + Material 3 (Astra tokens on top)
- **Architecture:** MVVM + Clean Architecture (ui / domain / data modules)
- **Base:** Launcher3 fork (proven launcher plumbing: app loading, widgets, drag & drop, workspace) with the experience rebuilt on top (Decision D3)
- **DI:** Hilt · **Persistence:** Room (favorites/usage) + DataStore (settings) · **Work:** WorkManager (suggestions refresh)
- **Min SDK 26 (Android 8.0)** · Target 35 · 60Hz baseline, 120Hz-ready

## 2. Module map
| Module | Responsibility |
|---|---|
| `app` | Application entry, manifest, DI entry point |
| `core-design` | Astra tokens (color/type/spacing/motion) + component library |
| `ui-home` | Home screen: workspace, icons, folders, dock, widgets, gestures |
| `ui-drawer` | App drawer + alphabetical/categorized browsing |
| `ui-search` | Search overlay (apps, contacts, settings, web) |
| `ui-recents` | Overview UI (cards, split screen, actions) |
| `ui-quicksettings` | Astra QS tiles + media card styling hooks |
| `ui-widgets` | Astra widget library (clock, weather, calendar, music, battery) |
| `ui-settings` | Astra settings hub + onboarding |
| `data-apps` | App repository (PackageManager, usage stats, suggestions ranking) |
| `data-widgets` | AppWidgetHost binding + widget config |
| `data-settings` | DataStore-backed settings + theme state |
| `wallpaper` | WallpaperService + dynamic color extraction |
| `service-accessibility` | Recents/gesture AccessibilityService (opt-in) |
| `service-tiles` | QuickSettingsService tiles |

## 3. Data layer
- **Room:** favorites, hidden apps, usage counts (for suggestions).
- **DataStore:** theme, icon pack, grid size, gesture prefs, wallpaper seed.
- **Suggestions:** on-device ranking (time-of-day + usage frequency), refreshed by WorkManager; no network calls for ranking.

## 4. Performance budgets
- Cold start to first frame: < 500ms on mid-range (SD 6-series class).
- Scroll/fling: no dropped frames at 60fps; 120fps where supported.
- Memory: < 150MB RSS on low-memory devices; images downsampled.
- Battery: no wakelocks; WorkManager constraints respected.

## 5. Track A build (app-level default home)
1. Fork Launcher3 → rebrand to Astra, strip to workspace + app loading core.
2. Rebuild UI layer in Compose per `docs/01_design_system.md` + `docs/02_screen_specs.md`.
3. Wire widgets (AppWidgetHost), wallpaper service, QS tiles, accessibility service.
4. Onboarding flow: set-as-default → accessibility consent → wallpaper → done.
5. Play Store compliance pass (accessibility disclosure, privacy policy).

## 6. Track B build (system-level takeover)
- AOSP tree; Astra as `priv-app` in `/system/priv-app`, signed with platform key.
- Replaces: status bar, navigation bar, keyguard/lock screen, recents (SystemUI hooks).
- System permissions: `WRITE_SECURE_SETTINGS`, `STATUS_BAR_SERVICE`, `MANAGE_ACTIVITY_TASKS`, etc.
- Optional Device Owner / kiosk mode for managed deployments.
- **Architectural rule:** all Track A modules stay platform-agnostic so Track B reuses them.

## 7. Verification
- Design review against mockups (`assets/mockups/`).
- Performance profiling on low-memory device (budgets in §4).
- Accessibility audit (TalkBack, contrast, touch targets).
- Play policy pre-review (accessibility service usage).

*Note: the sandbox has no Android SDK, so `app/` is a scaffold — compile on a machine with Android Studio / command-line SDK.*
