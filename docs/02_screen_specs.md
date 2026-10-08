# Astra Launcher — Screen Specifications (12 Screens)

**Version:** 1.0 — **Date:** 2026-10-08 — **Status:** APPROVED
Companion to `docs/01_design_system.md`. Every screen references its mockup in `assets/mockups/` or `assets/wallpapers/`. Device frame for all mockups: 1080x2400, 20:9, punch-hole camera, dark gradient backdrop.

---

## 1. Lock screen — `assets/mockups/11_lock_screen.png`
- **Purpose:** first impression; glanceable time, date, notifications.
- **Layout:** clock (Space Grotesk Light 64sp) top-center below punch-hole; date + weather caption under clock; up to 3 notification cards (surface.highest at 80% + blur); two quick-action circles bottom corners (flashlight, camera); swipe-up hint pill bottom-center.
- **Motion:** clock digits cross-fade on change; screen lifts/fades to home on unlock (300ms emphasized).
- **Track A note:** app-level lock screen = wallpaper + widget-style clock via `AppWidgetHost` on home + system keyguard underneath; full keyguard replacement is Track B.

## 2. Home — `assets/mockups/09_home_dark.png`, `assets/mockups/10_home_light.png`
- **Purpose:** the core environment.
- **Layout:** status bar (system); 4-column icon grid, max 5 rows; widget stacks between rows (2x2, 4x2, 4x4); dock pinned bottom (translucent blur bar, 4–5 icons); page indicator dots above dock; search pill pinned above dock (opens Search overlay).
- **Gestures:** swipe up from dock area → app drawer; swipe up from anywhere → search; long-press icon → app info / remove / uninstall; long-press empty space → wallpaper & style sheet.
- **Motion:** icon press scale 0.92 + light haptic; page swipe with parallax wallpaper; folder opens with scale+fade.

## 3. App drawer — `assets/mockups/12_app_drawer.png`
- **Purpose:** discovery of all apps.
- **Layout:** search pill pinned top; suggested apps row (on-device ranking, 4 apps); alphabetical grid below (4 columns); vertical alphabet index on right edge; "All apps" vs categorized tabs (opt-in).
- **Motion:** drawer rises from bottom (300ms); search filters with stagger fade; scroll = 60fps baseline.

## 4. Search overlay — `assets/mockups/13_search_overlay.png`
- **Purpose:** search-first interaction (apps, contacts, settings, web).
- **Layout:** centered search field with mic glyph; below: recent searches chips; live results list (app icons + names, settings deep links, contact cards); keyboard occupies lower half.
- **Behavior:** results update per keystroke; Enter opens top result; mic → voice search intent.

## 5. Recents / overview — `assets/mockups/14_recents.png`
- **Purpose:** multitasking.
- **Layout:** horizontal card carousel (surface.raised cards, app snapshot + label); top action bar (Split screen, Pin, Close); wallpaper blurred + scrimmed behind; clear-all at end of list.
- **Access:** via gesture (AccessibilityService-driven overview, opt-in with disclosure) or 3-button nav recents key.
- **Motion:** cards enter with scale 0.9 + stagger; swipe card up/away to dismiss with haptic.

## 6. Quick settings — `assets/mockups/15_quick_settings_notifications.png`
- **Purpose:** control center.
- **Layout (Track A via TileService):** QS tile grid (Wi-Fi, Bluetooth, flashlight, rotation, battery saver, hotspot — Astra-styled tiles); brightness slider; volume slider; media notification card with album art + transport controls.
- **App-level limit:** the shade container itself is system-owned; Astra styles its tiles, media card, and (via NotificationListenerService) notification content actions.
- **Track B:** Astra owns the whole shade + status bar.

## 7. Notifications
- **Purpose:** glanceable incoming info.
- **Layout:** grouped cards (surface.highest at 85% + blur, radius 20); heads-up banners with app icon + accent; inline actions (Reply, Mark done); per-app settings deep link.
- **Behavior:** group by app after 3+; swipe to dismiss; hold to snooze (1h options).

## 8. Widgets — `assets/mockups/16_widgets.png`
- **Purpose:** glanceable info at a glance on home.
- **Library:** clock (2x2, glass, Space Grotesk), weather (4x2, aurora gradient), calendar (4x2, agenda), music (4x2, album art + controls), battery (2x2, ring), Astra custom: next-event, screen-time, device care.
- **Spec:** all widgets follow widget frame spec (radius 24, surface fill, 1dp outline, 16dp padding).

## 9. Media
- **Purpose:** persistent playback control.
- **Layout:** media card in QS + lock screen + home music widget; album art, track/artist, progress bar, transport (prev/play/next), cast icon.
- **Behavior:** binds to `MediaSession`; controls any playing app.

## 10. Charging
- **Purpose:** ambient screen while charging.
- **Layout:** black background, large clock, battery ring (teal progress), "Charging · 67% · 32 min to full" caption; gentle aurora glow animation (subtle, slow).
- **Track A note:** ambient charging visuals shown via full-screen activity when charger connects (opt-in) — true always-on ambient is Track B.

## 11. Settings — Astra hub
- **Purpose:** theming, gestures, behavior, backup.
- **Layout:** grouped list (surface cards, radius 20): Appearance (theme, dynamic color, icon pack, icon shape), Gestures, Home screen (grid, dock, folders), Privacy (hide apps, lock apps), Backup & restore, About.
- **Principle:** every setting has an immediate preview where possible.

## 12. Onboarding
- **Purpose:** first-run setup, consent, permissions.
- **Flow:** Welcome (brand hero, Display type) → Set as default home (system dialog trigger) → Accessibility consent (recents/gestures, prominent disclosure) → Wallpaper pick (Astra set) → Done (lands on home).
- **Rule:** every permission has a plain-language reason and a "Not now" path; nothing is forced.

---

## Wallpaper set — `assets/wallpapers/`
- `17_wallpaper_aurora.png` — aurora ribbons over starfield (default dark)
- `18_wallpaper_nebula.png` — deep violet/teal nebula
- `19_wallpaper_constellation.png` — minimal constellation geometry (abstract orbit-A)

## Motion & depth reference — `assets/mockups/20_motion_depth_diagram.png`
Layer hierarchy: wallpaper → surface → card → overlay → dialog; transition directions and timing per `docs/01_design_system.md` §8.
