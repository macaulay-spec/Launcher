# ASTRA LAUNCHER — MASTER BUILD PLAN (FULL SCOPE)

**Version:** 0.1 (DRAFT FOR REVIEW)
**Date:** 2026-10-08
**Status:** ⏳ AWAITING REVIEW — nothing is executed until the reviewer says **"go ahead"**
**Reviewer gate:** This document is the contract. Each phase below ends in a review gate.

---

## 0. What this document is

- The **single master plan** for building **Astra Launcher**: an Android launcher that becomes the phone's **default UI** — the complete home environment of the device, not an app that lives inside the OS.
- **Process rule:** plan first → reviewer reviews → reviewer says "go ahead" → execution, phase by phase.
- **Update rule:** every artifact carries a version + date. After every phase, this plan and the `CHANGELOG.md` are updated. Nothing is ever left stale.
- The old repo content (`ASTRA_LAUNCHER_MASTER_DESIGN_BUILD_SPEC.md`, `Rebuild`, `Innitiate comit`) is **ignored** per the reviewer's instruction and will be **removed in Phase 1** (recoverable via git history — nothing is truly lost).

---

## 1. Vision / North Star

> **Astra is a cinematic, intelligent phone environment that replaces the stock home experience as the default UI.**

- One coherent visual world from **lock screen → home → search → apps → recents → controls → widgets → settings**. Every screen speaks the same visual language: wallpaper, surfaces, typography, icons, motion, light, depth, sound/haptics.
- **Design fusion (borrow principles, never assets):**
  - **Samsung One UI** — one-handed reachability, clear viewing vs. interaction zones, practical information density, personalization, responsive layouts.
  - **Apple HIG** — relentless spacing discipline, high-quality typography, purposeful materials, controlled translucency, motion that communicates continuity.
  - **Google Material** — semantic design tokens, dynamic color, adaptive layout, accessibility as a system property.
- **Original, not a clone.** Astra must not read as "Samsung skin" or "iOS port". It is its own design language.
- **Tone:** calm, premium, dark-first, cinematic (starlight/aurora atmosphere — see Decision D2).
- **Hardware ambition:** elegant and smooth on mid-range and low-memory devices, not only flagships.

---

## 2. "Take over the system" — honest capability map

A normal app **cannot** replace the status bar, navigation bar, or lock screen. Full takeover has two tracks:

### Track A — App-level default home (shippable as a normal app)
- Registered as **default launcher** (`HOME` intent, `CATEGORY_HOME`, Android Role Manager).
- **Home screen:** pages, app icons, folders, dock, widgets (`AppWidgetHost`), live wallpaper (`WallpaperService`), search, contextual suggestions.
- **App drawer**, app management (uninstall/hide via intents), shortcuts, deep links.
- **Recents/overview + gesture navigation:** via `AccessibilityService` (requires explicit user consent + in-app disclosure, per Google Play policy).
- **Quick settings tiles:** `TileService` (media, flashlight, rotation, etc.).
- **Notifications:** `NotificationListenerService` (listen/shade content) — visual restyle of the shade itself is limited at app level.
- Icon packs, theming, Material You dynamic color, per-app icon shapes.

### Track B — System-level UI (true OS takeover)
- Built into an **AOSP / custom ROM** as a `priv-app` signed with the **platform key** (or `android:sharedUserId="android.uid.system"`).
- Replaces: **status bar, navigation bar, lock screen/keyguard, recents/overview, system dialogs**.
- Requires: AOSP source tree, platform signing, system-level permissions (`WRITE_SECURE_SETTINGS`, `STATUS_BAR_SERVICE`, etc.).
- Optional **Device Owner / kiosk mode** for managed, locked-down deployments.

**Recommendation:** Build **Track A fully**, architect the codebase so **Track B is achievable later** without a rewrite.

---

## 3. The "new dress" — design direction (summary)

- **Dark-first cinematic** visual identity with aurora/starlight accents, plus a full **light variant**.
- Semantic **color token system**, disciplined **typography scale**, custom **icon language**, strict **spacing grid**, purposeful **motion + haptics**, generative **wallpapers** with dynamic color extraction.
- Full detail lands in `docs/01_design_system.md` (Phase 2). Direction is Decision **D2** below.

---

## 4. Design system plan (tokens → components)

| Layer | Deliverable |
|---|---|
| Color | Semantic tokens (primary/secondary/tertiary, surface hierarchy, state colors), hex values, dark + light palettes, contrast-checked pairs |
| Typography | Display + text families, full type scale, weights, line heights, fallback stack |
| Iconography | Grid, stroke weight, corner radii, adaptive-icon mask, app-icon style rules |
| Layout & spacing | 4/8dp grid, safe zones, one-handed reach map (Samsung principle) |
| Components | App icon tiles, folders, dock, search pill, cards, bottom sheets, dialogs, sliders, toggles, widget frames |
| Motion & haptics | Duration/easing tokens, continuity transitions, haptic event mapping, reduced-motion support |
| Wallpaper | Generative "Astra" art direction + dynamic color extraction from wallpaper |

---

## 5. Screen-by-screen scope (12 screens, each = spec + mockup(s))

1. **Lock screen** — clock, date, notifications, quick actions, unlock motion
2. **Home** — pages, icon grid, folders, dock, widget stacks, page indicator
3. **App drawer** — search, alphabetical + suggested apps, categories
4. **Search overlay** — app search, contacts, settings, web, on-device results
5. **Recents / overview** — card carousel, split screen, app actions
6. **Quick settings** — tiles, brightness/volume sliders, media controls
7. **Notifications** — shade grouping, heads-up behavior, actions
8. **Widgets library** — clock, weather, calendar, music, battery, custom Astra widgets
9. **Media** — persistent player, lock-screen media, cast
10. **Charging** — ambient charging screen
11. **Settings** — Astra settings hub, theming, gestures, backup
12. **Onboarding** — setup, default-launcher consent, accessibility disclosure, permissions

---

## 6. Asset generation plan (AI-generated images)

Style-consistency rules for every asset: same palette, same device frame (1080×2400, 20:9, punch-hole), same lighting, dark + light variants where relevant.

| # | Asset | Purpose |
|---|---|---|
| 1 | Brand board / moodboard | Direction lock for the whole identity |
| 2–4 | Logo — primary + 2 alternates | App icon, splash, watermark |
| 5 | Color palette sheet (dark + light) | Token reference |
| 6 | Typography specimen | Type scale in practice |
| 7 | Icon style grid | App-icon language sample (8–12 icons) |
| 8 | Adaptive icon + mask demo | Launcher icon across OEM shapes |
| 9–10 | Home screen mockup (dark + light) | Hero screen |
| 11 | Lock screen mockup | First impression |
| 12 | App drawer mockup | Discovery |
| 13 | Search overlay mockup | Search-first interaction |
| 14 | Recents mockup | Overview/multitasking |
| 15 | Quick settings + notifications mockup | Control center |
| 16 | Widgets mockup | Clock/weather/calendar/music widgets |
| 17–19 | Wallpaper concepts (3) | Generative Astra art direction |
| 20 | Motion & depth diagram | Layer hierarchy + transition rules |

---

## 7. Mockup plan

- Consistent **device frame**, resolution, status-bar style, and corner radius across all mockups.
- **Annotated** versions (callouts for spacing, zones, behavior) alongside clean versions.
- **Light + dark** variants for key screens (home, lock, drawer, recents).
- Delivered embedded in the **Google Doc** and saved to `assets/` in the repo.

---

## 8. Technical build plan

- **Language/stack:** Kotlin, Jetpack Compose for UI, MVVM + Clean Architecture.
- **Base decision (D3):** fork **Launcher3** (proven launcher plumbing: app loading, widgets, drag & drop) and rebuild the experience on top — recommended over from-scratch.
- **Modules:** `ui-home`, `ui-drawer`, `ui-search`, `ui-recents`, `ui-quicksettings`, `ui-widgets`, `ui-settings`, `ui-onboarding`, `core-design` (tokens/components), `data-apps`, `data-widgets`, `data-settings`, `wallpaper`, `service-accessibility`, `service-tiles`.
- **DI:** Hilt. **Persistence:** Room (favorites/usage), DataStore (settings). **Suggestions:** WorkManager + on-device ranking.
- **Performance budgets:** fast cold start, 60fps baseline / 120fps-ready, low-memory footprint on mid-range devices, no jank on scroll/fling.
- **Accessibility:** TalkBack labels, contrast ≥ AA, dynamic type, reduced-motion mode, switch access.

---

## 9. Permissions & integration checklist

- Manifest: `BIND_APPWIDGET`, `QUERY_ALL_PACKAGES` (scoped), `REQUEST_DELETE_PACKAGES`/`REQUEST_UNINSTALL_PACKAGES` where needed, `SET_WALLPAPER`, `EXPAND_STATUS_BAR`.
- AccessibilityService: opt-in flow, prominent disclosure, Play-policy-compliant usage (recents/gestures only).
- `TileService` for QS tiles; `WallpaperService` for live wallpaper; `NotificationListenerService` for shade content.
- Track B only: platform signature, `priv-app`, system permissions list (documented in `docs/04_permissions_integration.md`).

---

## 10. Repo & documentation structure (after Phase 1 cleanup)

```
ASTRA_LAUNCHER_MASTER_BUILD_PLAN.md   ← this file, always current
CHANGELOG.md                          ← updated after every phase
README.md                             ← project overview (regenerated)
docs/
  01_design_system.md                 ← colors, type, icons, spacing, components, motion
docs/
  02_screen_specs.md                  ← 12 screens, spec + mockup references
docs/
  03_technical_build_spec.md          ← architecture, modules, Track A/B build
docs/
  04_permissions_integration.md       ← manifest, services, system integration
docs/
  05_asset_inventory.md               ← every asset: name, path, purpose, export spec
assets/                               ← all generated images, organized by category
```

---

## 11. Google Doc deliverable

One **full-scope Google Doc** containing: brand story, the complete design system, all 12 screen specs, **every mockup and asset image embedded**, the technical summary, the permissions matrix, and the asset inventory. Created in **Phase 4**.

---

## 12. Phases & gates

| Phase | Work | Gate |
|---|---|---|
| **0** | **This plan** — written to file for review | ⏳ **You review → say "go ahead" or request edits** |
| 1 | Clean repo (remove old files), scaffold structure above, init `CHANGELOG.md` | You confirm structure |
| 2 | Design system doc + brand assets (assets #1–8 generated) | You approve direction & assets |
| 3 | Screen specs + mockups (assets #9–20 generated, annotated) | You approve screens |
| 4 | Full-scope **Google Doc** with all images embedded | You approve doc |
| 5 | Technical build spec (architecture, permissions, Track A/B) | You approve spec |
| 6 | Code kickoff (app scaffold + first screens) | Only after your explicit go |

**Update rule:** after every phase — this plan + `CHANGELOG.md` updated, every file versioned and dated.

---

## 13. Decisions needed from you (with my recommendations)

| # | Decision | My recommendation |
|---|---|---|
| D1 | Track A only, or A + B (system ROM track)? | **A first**, architected so B is possible later |
| D2 | Visual direction | **Cinematic deep-space, dark-first** (aurora/starlight accents) + light variant |
| D3 | Launcher base | **Launcher3 fork**, experience rebuilt on top |
| D4 | Hardware priority | **Mid-range + low-memory first**, 60Hz baseline, 120Hz-ready |
| D5 | Deliverables | **Google Doc (full scope, images embedded) + repo assets + markdown sources** |

👉 Reply **"go ahead"** to accept all recommendations, or tell me what to change.

---

## 14. Changelog

- **v0.1 — 2026-10-08** — Initial full-scope master build plan drafted and submitted for review. No execution yet.
