# Astra Launcher

A **cinematic, intelligent Android launcher** that becomes the phone's **default UI** — the complete home environment, not an app inside the OS.

**Design:** Samsung One UI ergonomics + Apple HIG polish + Google Material structure, fused into one original visual world ("the dress").

**Version:** 1.2 — **Date:** 2026-10-08

## Build the APK

- **CI (automatic, GREEN):** every push to `arena/67cc6168-launcher` runs `.github/workflows/build-apk.yml` and uploads the `astra-launcher-debug-apk` artifact (Actions → latest run → Artifacts). Manual trigger: Actions → "Build Astra Launcher APK" → Run workflow.
- **Current build:** `app-debug.apk` — **7,873,117 bytes**, sha256 `f1fc435e14ecbc71730c9f5a72e6a10b0a209a3b259597610bbf61363a304ab6` (see `ci/last-build.txt`).
- **Local:** `./gradlew :app:assembleDebug` (JDK 17; Gradle 8.7 via wrapper). Output: `app/build/outputs/apk/debug/app-debug.apk`.
- **Toolchain (proven):** AGP 8.5.2 · Gradle 8.7 · compileSdk/targetSdk 34 · Kotlin 1.9.24 · Compose BOM 2024.06.00.

## Read in this order

1. `ASTRA_LAUNCHER_MASTER_BUILD_PLAN.md` — master plan (phases, gates, decisions, CI detail)
2. `docs/01_design_system.md` — the visual identity: color, type, icons, spacing, components, motion
3. `docs/02_screen_specs.md` — all 12 screens, spec + mockup references
4. `docs/03_technical_build_spec.md` — architecture, modules, Track A/B build, CI toolchain
5. `docs/04_permissions_integration.md` — manifest, services, system integration
6. `docs/05_asset_inventory.md` — every generated asset, path, purpose, export spec
7. `assets/` — generated images (brand / mockups / wallpapers)
8. `app/` — Android app (Compose + Astra design tokens + default-home manifest)
9. `ci/` — self-reported build results (`last-build.txt`, `last-build.log`)

## Status (v1.2, 2026-10-08)

- ✅ Phases 0–3, 5–7 done; **CI builds the APK automatically on every push — GREEN**
- ⏳ 10 of 20 planned asset images queued (image-gen limit 10 per turn)
- ⏸ Phase 4 (full-scope Google Doc) paused by reviewer; partial doc remains in Google Docs
- 🔜 Next: release signing config; Track B (system-level) build; remaining 10 assets
