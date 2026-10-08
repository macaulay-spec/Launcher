# Astra Launcher — Changelog

All notable changes to this project are documented here. Every file carries a version + date. Nothing goes stale.

## [1.1.0] — 2026-10-08

### Phase 7 — CI/CD (reviewer redirect: "start your project, push, build APK via GitHub Actions")
- Added `.github/workflows/build-apk.yml` — builds the debug APK on push to `arena/67cc6168-launcher` (+ manual dispatch); uploads `app-debug.apk` as artifact `astra-launcher-debug-apk`
- Added Gradle wrapper (`gradlew`, `gradlew.bat`, `gradle/wrapper/gradle-wrapper.jar`, properties → Gradle 8.10.2)
- Added `app/src/main/res/values/themes.xml` (`Theme.Astra` — referenced by the manifest; without it the build would fail)
- Added explicit `androidx.compose.foundation` dependency in `app/build.gradle.kts`
- Added `.gitignore` (build outputs, APKs, local.properties stay out of git)
- Updated `ASTRA_LAUNCHER_MASTER_BUILD_PLAN.md` → v1.1 and `README.md` → v1.1

### Phase 4 — PAUSED
- Google Doc paused per reviewer ("forget about Google docs"). Partial doc (sections 1–2.5, 8 embedded images) remains in Google Docs; no further work on it.

## [1.0.0] — 2026-10-08

### Phase 1 — Clean slate
- Removed legacy files: `ASTRA_LAUNCHER_MASTER_DESIGN_BUILD_SPEC.md`, `Rebuild`, `Innitiate comit` (recoverable via git history)
- Scaffolded `docs/` and `assets/` structure; initialized `CHANGELOG.md`; regenerated `README.md`

### Phase 2 — Design system + brand assets
- Added `docs/01_design_system.md` (v1.0)
- Generated 8 brand assets in `assets/brand/`

### Phase 3 — Screen specs + mockups
- Added `docs/02_screen_specs.md` (v1.0)
- Generated 2 mockups (`assets/mockups/09_home_dark.png`, `10_home_light.png`); 10 images queued (image-gen limit 10 per turn)

### Phase 4 — Google Doc (later paused, see v1.1)
- Created full-scope Google Doc; embedded sections 1–2.5 with 8 images

### Phase 5 — Technical build spec
- Added `docs/03_technical_build_spec.md`, `docs/04_permissions_integration.md`, `docs/05_asset_inventory.md`

### Phase 6 — Code kickoff
- Added Android app scaffold: `settings.gradle.kts`, root `build.gradle.kts`, `gradle.properties`, `app/build.gradle.kts`, `AndroidManifest.xml` (default-home intent), `MainActivity.kt`, `AstraColors.kt`, `AstraTheme.kt`, `HomeScreen.kt`
