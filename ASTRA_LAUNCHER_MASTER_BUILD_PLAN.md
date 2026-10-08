# ASTRA LAUNCHER — MASTER BUILD PLAN (FULL SCOPE)

**Version:** 1.2 — **Date:** 2026-10-08
**Status:** Phase 4 (Google Doc) ⏸ PAUSED by reviewer redirect; Phase 7 (CI/CD) executed — **APK build GREEN**.

---

## 0. What this document is

- The **single master plan** for building **Astra Launcher**: an Android launcher that becomes the phone's **default UI**.
- **Update rule:** every artifact carries a version + date. After every phase, this plan and `CHANGELOG.md` are updated. Nothing goes stale.
- Legacy repo content was removed in Phase 1 (recoverable via git history).

## 1. Vision / North Star

> **Astra is a cinematic, intelligent phone environment that replaces the stock home experience as the default UI.**

- One coherent visual world from **lock screen → home → search → apps → recents → controls → widgets → settings**.
- **Design fusion (principles, never assets):** Samsung One UI ergonomics + Apple HIG polish + Google Material structure. Original, not a clone.
- **Tone:** calm, premium, cinematic. Dark-first, starlight/aurora atmosphere + full light variant.
- **Hardware ambition:** elegant on mid-range and low-memory devices, not only flagships.

## 2. "Take over the system" — capability map

- **Track A — App-level default home (building now):** default launcher (HOME intent), home screen, drawer, search, widgets, wallpaper, recents/gestures via AccessibilityService (opt-in + disclosed), QS tiles via TileService.
- **Track B — System-level UI (architected for, built later):** AOSP priv-app, platform-signed; replaces status bar, nav bar, keyguard, recents.

## 3. The design system ("the dress")

Full spec: `docs/01_design_system.md` — semantic color tokens (dark/light), Space Grotesk + Inter type scale, icon language, 4/8dp spacing, component library, motion & haptics, generative wallpapers, accessibility (AA).

## 4. Screen scope (12 screens)

Specs + mockups: `docs/02_screen_specs.md` — lock, home, drawer, search, recents, quick settings, notifications, widgets, media, charging, settings, onboarding.

## 5. Asset generation status

20 planned assets; **10 generated** (8 brand + 2 home mockups) in `assets/`; **10 pending** (image-gen limit 10/turn — queued). Inventory: `docs/05_asset_inventory.md`.

## 6. Technical build

Spec: `docs/03_technical_build_spec.md` — Kotlin + Compose + Material 3 (Astra tokens), MVVM/Clean, Launcher3 fork base, Hilt, Room + DataStore, WorkManager. Min SDK 26, target 35, 60Hz baseline / 120Hz-ready. Permissions matrix: `docs/04_permissions_integration.md`.

## 7. Phases & gates — status

| Phase | Work | Status |
|---|---|---|
| 0 | Master plan for review | ✅ approved ("go ahead with all plans") |
| 1 | Clean repo, scaffold structure, CHANGELOG | ✅ done |
| 2 | Design system doc + 8 brand assets | ✅ done |
| 3 | 12 screen specs + mockups (10 images pending batch 2) | ✅ specs done / ⏳ 10 images queued |
| 4 | Full-scope Google Doc with embedded assets | ⏸ **PAUSED** — reviewer redirect 2026-10-08 ("forget about Google docs"); partial doc (sections 1–2.5, 8 images) remains in Google Docs |
| 5 | Technical build spec + permissions matrix + asset inventory | ✅ done |
| 6 | Android app scaffold (Compose + Astra tokens + default-home manifest) | ✅ done |
| **7** | **CI/CD — GitHub Actions debug-APK build on push to `arena/67cc6168-launcher`** | ✅ done (v1.1) |

## 8. Phase 7 — CI/CD detail

- Workflow: `.github/workflows/build-apk.yml`
- Trigger: push to `arena/67cc6168-launcher` + manual `workflow_dispatch`
- Steps: checkout → JDK 17 (temurin) → Gradle 8.10.2 (`gradle/actions/setup-gradle`) → `gradle :app:assembleDebug` → upload `app-debug.apk` as artifact `astra-launcher-debug-apk` (14-day retention)
- Gradle wrapper committed (`gradlew`, `gradle-wrapper.jar`, properties → 8.10.2) for local builds
- `app/src/main/res/values/themes.xml` added (`Theme.Astra`, referenced by the manifest)
- `.gitignore` keeps build outputs and APKs out of git
- **Next step (not yet done):** release signing (keystore + `signingConfig`) for `assembleRelease`

## 9. Decisions (accepted with "go ahead with all plans")

D1 Track A first, architected for B · D2 cinematic deep-space dark-first · D3 Launcher3 fork base · D4 mid-range/low-memory priority · D5 Google Doc + repo assets (Phase 4 now paused by reviewer).

## 10. Changelog

- **v1.1 — 2026-10-08** — Phase 4 (Google Doc) paused by reviewer redirect; Phase 7 (CI/CD) added: GitHub Actions APK build, Gradle wrapper, Theme.Astra resource, .gitignore.
- **v1.0 — 2026-10-08** — All phases 0–6 executed: clean slate, design system, screen specs, 10 assets, tech spec, permissions, asset inventory, app scaffold.
