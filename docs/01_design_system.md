# Astra Launcher — Design System ("The Dress")

**Version:** 1.0 — **Date:** 2026-10-08 — **Status:** APPROVED (Master Plan v1.0, "go ahead with all plans")

---

## 1. Brand story

**Astra** — Latin for "stars"; "ad astra", to the stars. The launcher is the moment before launch: calm, dark, full of possibility. One coherent visual world from lock screen to settings — the wallpaper, surfaces, typography, icons, motion, light, depth, and haptics all speak the same language.

**North star: continuity.** Astra is not a Samsung clone, not an iOS clone, not a glassmorphism demo. It is its own design language.

**Tone:** calm, premium, cinematic. Dark-first with starlight/aurora atmosphere, plus a full light variant.

## 2. Design principles (fusion — borrow principles, never assets)

| Source | What we borrow |
|---|---|
| Samsung One UI | One-handed reachability; clear viewing vs. interaction zones; practical information density; personalization; responsive layouts |
| Apple HIG | Relentless spacing discipline; high-quality typography; purposeful materials; controlled translucency; motion that communicates state and continuity |
| Google Material | Semantic design tokens; dynamic color; adaptive layout; accessibility as a system property |

## 3. Color system (semantic tokens)

Dark-first. All text/background pairs meet WCAG AA (>= 4.5:1). Dynamic color: a Material You seed is extracted from the wallpaper and mapped into the primary/secondary tokens.

### Dark theme (default)
| Token | Hex | Role |
|---|---|---|
| astra.background | #0B1020 | App background (deep ink navy) |
| astra.surface | #131A2E | Cards, sheets, widget frames |
| astra.surface.raised | #1B2440 | Elevated surfaces |
| astra.surface.highest | #26304F | Dialogs, top-most layers |
| astra.primary | #2DD4BF | Aurora teal — primary actions, focus, active states |
| astra.primary.dim | #1FA898 | Pressed/hover teal |
| astra.secondary | #8B5CF6 | Violet — secondary accents, gradients |
| astra.accent | #F5B04C | Amber — used sparingly (highlights, warnings) |
| astra.onBackground | #E8ECF4 | Starlight — primary text |
| astra.onSurface.muted | #9AA5C0 | Secondary text, icons |
| astra.success | #34D399 | Success states |
| astra.error | #F87171 | Error states |
| astra.outline | rgba(232,236,244,0.10) | Hairline borders |
| astra.scrim | rgba(11,16,32,0.60) | Scrims over wallpaper |

### Light theme
| Token | Hex | Role |
|---|---|---|
| astra.background | #F6F4EF | Warm paper background |
| astra.surface | #FFFFFF | Cards, sheets |
| astra.surface.raised | #EFEDE6 | Elevated surfaces |
| astra.primary | #0E9488 | Deep teal (contrast-safe on light) |
| astra.secondary | #7C3AED | Deep violet |
| astra.onBackground | #141A2E | Ink — primary text |
| astra.onSurface.muted | #5A6480 | Secondary text |
| astra.outline | rgba(20,26,46,0.12) | Hairline borders |

Gradients: aurora = linear teal (#2DD4BF) → violet (#8B5CF6), 135deg, used on logo, primary buttons, and widget glyph accents only.

## 4. Typography

- **Display:** Space Grotesk (headings, clock, wordmark) — geometric, technical, warm.
- **Text:** Inter (body, labels, buttons).

| Style | Family / weight | Size (sp) | Line height | Use |
|---|---|---|---|---|
| Display | Space Grotesk Light | 57 | 1.1 | Onboarding hero |
| H1 | Space Grotesk Medium | 32 | 1.2 | Screen titles |
| H2 | Space Grotesk Medium | 24 | 1.25 | Section headers |
| H3 | Space Grotesk Medium | 20 | 1.3 | Card titles |
| Title | Inter SemiBold | 16 | 1.4 | List titles, app names |
| Body | Inter Regular | 15 | 1.5 | Paragraphs |
| Body small | Inter Regular | 14 | 1.5 | Secondary info |
| Caption | Inter Regular | 12 | 1.4 | Timestamps, meta |
| Overline | Inter Medium, uppercase, +8% tracking | 11 | 1.3 | Section eyebrows |
| Clock (lock) | Space Grotesk Light | 64 | 1.0 | Lock screen clock |

## 5. Iconography

- **Grid:** 24dp; Material adaptive-icon keylines for app icons.
- **Stroke:** 1.75dp, round caps and joins, single weight.
- **App icons:** rounded-square tiles (squircle, ~22% corner radius), dark tile `#131A2E` in dark theme, white tile in light theme; glyph = line icon with aurora gradient (teal→violet); subtle top-light. No gloss, no drop shadows on glyphs.
- **System glyphs:** line icons tinted with `onSurface.muted` / `primary` per state.
- **Folders:** translucent surface (`surface.raised` at 85% + blur) showing 3 stacked app previews.

## 6. Layout & spacing

- **Grid:** 4dp base; 8dp rhythm for all spacing.
- **Safe zones:** 16dp side padding; top inset = status bar + 8dp; bottom inset = nav bar + 8dp.
- **One-handed reach map (Samsung principle):** primary actions live in the lower 60% of the screen; dock pinned to the bottom; destructive/secondary actions at top.
- **Home icon grid:** 4 columns (phones), 5–6 (tablets/foldables); icon 56dp; label 12sp, 4dp below icon.
- **Density:** comfortable — never more than 5 rows of icons + dock on standard phones.

## 7. Components

| Component | Spec |
|---|---|
| App icon tile | 56dp, squircle 22%, label below |
| Folder | Translucent raised surface, 3 stacked previews, opens with scale+fade |
| Dock | Translucent bar (surface at 70% + backdrop blur), radius 28, 4–5 icons, height 64dp |
| Search pill | surface.raised, 48dp height, radius 24, search glyph + mic glyph |
| Widget frame | radius 24, surface fill, 1dp outline, 16dp inner padding |
| Card | surface.raised, radius 20, 16dp padding |
| Bottom sheet | surface.highest, top radius 28, drag handle |
| Dialog | surface.highest, radius 24, title + actions |
| Slider | 4dp track, teal active fill, 20dp thumb |
| Toggle | teal when on, muted track when off |
| Chip | radius 16, outline hairline, 32dp height |
| Snackbar | surface.highest, radius 12, bottom above dock |
| Page indicator | 6dp dots, active = teal, elongated to 16dp pill |

## 8. Motion & haptics

- **Durations:** micro 100ms, short 200ms, medium 300ms, long 450ms.
- **Easing:** standard `cubic-bezier(0.2, 0, 0, 1)`; emphasized entrance `cubic-bezier(0.05, 0.7, 0.1, 1)`.
- **Continuity:** icon↔app shared-element transitions; home page transitions slide+fade; recents cards scale from 0.9 with stagger; sheets rise from bottom.
- **Haptics:** light tick on icon press, medium on folder open, subtle success pattern on completion. Never haptic spam.
- **Reduced motion:** cross-fades only, durations halved.

## 9. Wallpaper art direction

Generative "Astra" set (see `assets/wallpapers/`): aurora ribbons over starfield, deep nebula, minimal constellation geometry. Dark-first; a light variant is generated from the same seed. Dynamic color tokens are extracted from the active wallpaper.

## 10. Accessibility

- Contrast AA minimum on every text pair.
- TalkBack content descriptions on all interactive elements.
- 48dp minimum touch targets; icon labels always visible on home.
- Dynamic type support; layouts reflow, never clip.
- Reduced-motion mode; color is never the sole carrier of meaning.

## 11. Assets

Brand assets: `assets/brand/` (board, logos, palette, type specimen, icon grid, adaptive icon). Full inventory: `docs/05_asset_inventory.md`.
