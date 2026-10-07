# Astra Launcher (`com.astra.launcher`)
### Cinematic Intelligent Phone Launcher & Spatial Control Environment for Android

Astra Launcher is a native Android launcher and phone-wide visual environment implemented in **Kotlin + Jetpack Compose** across **16 modular Gradle subprojects**, built strictly to the contract defined in [`ASTRA_LAUNCHER_MASTER_DESIGN_BUILD_SPEC.md`](./ASTRA_LAUNCHER_MASTER_DESIGN_BUILD_SPEC.md).

---

## 1. Core Design & Product DNA
> **Astra = cinematic calm + intelligent utility + spatial depth + restrained personalization.**

- **Continuous Spatial Environment:** Connects lock screen, Always-On Display (AOD), curated wallpapers, Home screen, offline command search, App Library, Notification Shade, Spatial Control Center, first-party & hosted widgets, Recents continuum, Personalization Studio, and 13-section Settings into one visual world.
- **Three Material Surface Families:**
  1. **Solid (`AstraSurfaceFamily.SOLID`):** Used for Settings root/detail pages, third-party widget containers, and high-legibility lists.
  2. **Soft Translucent (`AstraSurfaceFamily.SOFT_TRANSLUCENT`):** Used for Notification Shade cards, Spatial Control Center tiles, Global Search field, and Folder sheets.
  3. **Clear Atmospheric (`AstraSurfaceFamily.CLEAR_ATMOSPHERIC`):** Used sparingly for Lock Screen chips, App Library category containers, and secondary Recents cards.
- **Platform Truth Principle:** Never fakes restricted Android system toggles. Uses `RoleManager.ROLE_HOME` (API 29+) for default launcher consent, `CameraManager.setTorchMode` for hardware flashlight, `AudioManager` for volume, `NotificationListenerService` for notification stream access, `AppWidgetHost` (#2026) for widgets, and official `Settings.Panel` deep-link handoffs for restricted connectivity toggles.

---

## 2. Multi-Module Android Architecture (16 Gradle Modules)

| Module | Responsibility |
|---|---|
| **`:app`** | Main launcher shell (`AstraLauncherActivity` with `CATEGORY_HOME` + `CATEGORY_DEFAULT`), `AstraNotificationListenerService`, `AstraSystemEventReceiver` (`BOOT_COMPLETED`, `ACTION_POWER_CONNECTED`), and `AstraOrbitalClockWidgetProvider`. |
| **`:core-design`** | Semantic color engine (`AstraPalette`, `AstraColorEngine` with WCAG contrast validation), typography (`AstraTypography`), spacing/radius (`AstraSpacing`, `AstraShapes`), motion (`AstraMotion`), haptics (`AstraHaptics`), sound (`AstraSoundEngine`), orbital brand mark (`AstraBrandMark`), vector icon system (`AstraVectorIcon`), wallpaper surface (`AstraWallpaperSurface`), and all 18 reusable `Astra*` components. |
| **`:core-platform`** | Runtime capability detector (`AstraCapabilityMatrix`), Home role flow (`AstraRoleManager`), package & shortcut scanner (`AstraPackageRepository`), `AppWidgetHost` wrapper (`AstraWidgetHostManager`), system controller (`AstraSystemController`), and live notification bus (`AstraNotificationBridge`). |
| **`:core-storage`** | Local-first persistence (`AstraStorageRepository`), domain models (`ThemeSettings`, `HomeLayout`, `SearchPreferences`, `NotificationPreferences`, `GesturePreferences`, `PerformancePreferences`), `ASTRA_BACKUP_V1` snapshot export/import, and safe-mode recovery reset. |
| **`:core-performance`** | RAM/memory pressure inspection (`AstraPerformanceManager`), automatic Low-End Device Mode visual budget degradation, LRU icon bitmap cache, and startup timing telemetry. |
| **`:feature-home`** | `AstraHomeScreen`, live snap-grid `AstraHomeEditorPanel`, elevated `AstraFolderModal`, `AstraAppShortcutDialog`, and non-happy system state cards (`Offline`, `Low Battery`, `Loading`, `Empty`, `Error`, `Recovery Safe Mode`). |
| **`:feature-apps`** | `AstraAppDrawerScreen` with instant filter, Suggested/Recent row, fast Alphabet index rail, Intelligent Categories, Favorites, and biometric/PIN-gated Private Vault. |
| **`:feature-search`** | `AstraSearchIndex` (5-tier offline ranking: Exact, Recent, Frequent, Contextual Alias, Fuzzy Subsequence) + Deterministic Command Engine (`open YouTube`, `turn on Wi-Fi`, `find Settings`, `call Mum`, `show battery settings`, `open camera`, `start timer`) + hardware keyboard navigation (`↑`/`↓`/`Enter`/`Esc`). |
| **`:feature-lock-preview`** | `AstraLockScreen` (3 adaptive clock styles: `Minimal Numeral`, `Editorial Stacked`, `Orbital Compact`, privacy-redacted notifications, torch/camera quick actions), `AstraAodScreen` (burn-in pixel shift), and `AstraChargingOverlay`. |
| **`:feature-notifications`** | `AstraNotificationShadeScreen` with Urgent, Regular, and Silent priority buckets, collapsed/expanded modes, and permission pre-explanation / handoff / denied fallback states. |
| **`:feature-controls`** | `AstraControlCenterScreen` (Spatial Control Plane: primary connectivity matrix, luminance & volume sliders, direct hardware torch, and `AstraMediaCard` with adaptive album-art tint). |
| **`:feature-widgets`** | `AstraWidgetHostCard` (8 first-party Astra widgets: Orbital Clock, Weather, Calendar Agenda, Battery Telemetry, Media Compact, Quick Actions, Calm Focus, Scratchpad Notes + third-party Android `AppWidgetHost` coexistence) and `AstraWidgetPickerSheet`. |
| **`:feature-recents`** | `AstraRecentsScreen` vertical task continuum with restore, dismiss, split-screen handoff, and truthful platform capability notice. |
| **`:feature-personalization`** | `AstraPersonalizationStudioScreen` with live interactive phone environment preview, 5 theme presets (`Astral`, `Graphite`, `Morning`, `Nocturne`, `Glass Horizon`), 5 wallpapers, 3 clock styles, and 4 icon styles. |
| **`:feature-settings`** | `AstraSettingsScreen` covering all 13 main sections + interactive **40-Frame Visual Screen Inventory Inspector**. |
| **`:feature-onboarding`** | `AstraOnboardingScreen` 6-step cinematic setup flow (`Welcome` → `ROLE_HOME` → `Atmosphere` → `Essentials` → `Search & Controls` → `Finish`). |

---

## 3. Curated Wallpaper Collection
Bundled in `core-design/src/main/res/drawable-nodpi/` and `design/wallpapers/`:
1. **Orbit Dawn (`wallpaper_orbit_dawn.jpg`)** — Soft mineral-blue, muted indigo, pale silver, and warm orbital arc (`22%` upper luminance, `5800K`).
2. **Nocturne Flow (`wallpaper_nocturne_flow.jpg`)** — Deep graphite, smoked black, and diagonal violet architectural glass (`8%` upper luminance, `6800K`).
3. **Glass Horizon (`wallpaper_glass_horizon.jpg`)** — Translucent architectural planes, frosted cyan, graphite, and champagne highlights (`84%` upper luminance, `6200K`).
4. **Orbit Dawn · Light (`wallpaper_morning_mist.jpg`)** — Cloud-white, mist-silver, and delicate mineral-cyan daylight arc (`91%` upper luminance, `5400K`).
5. **Graphite Monolith (`wallpaper_graphite_monolith.jpg`)** — Matte anodized obsidian planes with razor-thin icy cyan rim-light (`6%` upper luminance, `6500K`).

---

## 4. Building the APK (GitHub Actions & Local Gradle)

### GitHub Actions Automated APK Pipeline
Every push and pull request runs [`.github/workflows/build-apk.yml`](./.github/workflows/build-apk.yml), which:
1. Runs unit tests across all 16 modules (`./gradlew testDebugUnitTest`)
2. Generates a release signing keystore and builds both Debug and Signed Release APKs (`./gradlew assembleDebug assembleRelease`)
3. Uploads **`AstraLauncher-Debug-APK`** (`AstraLauncher-v1.0.0-debug.apk`) and **`AstraLauncher-Release-Signed-APK`** (`AstraLauncher-v1.0.0-release-signed.apk` + `SHA256SUMS.txt`) as downloadable workflow artifacts.

### Local Build Commands
```bash
# Run unit tests across all 16 modules
./gradlew testDebugUnitTest

# Build Debug and Release APKs
./gradlew assembleDebug assembleRelease
```

---

## 5. Companion Design Specifications & Interactive Review Portal
- [`ASTRA_LAUNCHER_MASTER_DESIGN_BUILD_SPEC.md`](./ASTRA_LAUNCHER_MASTER_DESIGN_BUILD_SPEC.md) — Master Product & Build Contract
- [`docs/DESIGN_TOKEN_AND_MOTION_SPECIFICATION.md`](./docs/DESIGN_TOKEN_AND_MOTION_SPECIFICATION.md) — Color, Typography, Surface, Motion & 18-Component Specification
- [`docs/VISUAL_SCREEN_INVENTORY_40_FRAMES.md`](./docs/VISUAL_SCREEN_INVENTORY_40_FRAMES.md) — Complete 40-Frame Inventory & Capability Matrix
- [`design-review-portal/`](./design-review-portal/) — Interactive Web Visual Review Board, 8-Screen Side-by-Side Comparison, & 40-Frame Simulator (`python3 design-review-portal/server.py`)
