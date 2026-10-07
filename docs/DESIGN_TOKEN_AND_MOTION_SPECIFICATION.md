# Astra Launcher — Design Token, Material, Motion & Component Specification
### Companion to `ASTRA_LAUNCHER_MASTER_DESIGN_BUILD_SPEC.md` (Sections 4–9, 30–32, 46, 53)

---

## 1. Brand System (`AstraBrandMark`)
- **Geometry:** Elliptical orbital arc (`195°` start, `235°` sweep), structural abstract `A` apex (`(0.28, 0.78) → (0.50, 0.18) → (0.72, 0.78)`), and a precision star point at the orbital intersection (`(0.74, 0.30)`).
- **Scale envelope:** Optically balanced from `16dp` status indicator to `128dp` onboarding/splash hero mark.
- **Luminous behavior:** Radial atmospheric glow reserved strictly for splash/onboarding and charging state transitions.

---

## 2. Color System & Semantic Tokens (`AstraPalette` & `AstraSemanticColors`)

### 2.1 Base Palette
| Token | Hex | Role |
|---|---|---|
| `Astra Black` | `#0A0B0D` | Primary dark canvas & OLED AOD base |
| `Graphite 1` | `#111318` | Raised solid surface (Settings cards, sheets) |
| `Graphite 2` | `#171A20` | Floating surface & translucent glass tint base |
| `Graphite 3` | `#20242C` | Elevated interactive track / subtle divider |
| `Cloud` | `#F5F6F8` | Primary text on dark / Base surface on light |
| `Mist` | `#D8DBE1` | Secondary text & inactive iconography |
| `Ink` | `#171A20` | Primary text & iconography on light surfaces |

### 2.2 Semantic Color Roles
| Semantic Token | Dark Theme Value | Light Theme Value |
|---|---|---|
| `text.primary` | `#F5F6F8` (`Cloud`) | `#171A20` (`Ink`) |
| `text.secondary` | `#D8DBE1` (`Mist`) | `#3E4656` |
| `text.tertiary` | `#949BA8` | `#646E82` |
| `text.disabled` | `#565D6B` | `#9AA3B5` |
| `icon.primary` | `#F5F6F8` | `#171A20` |
| `icon.secondary` | `#D8DBE1` | `#475063` |
| `surface.base` | `#0A0B0D` | `#F5F6F8` |
| `surface.raised` | `#111318` | `#FFFFFF` |
| `surface.floating` | `#171A20` | `#FDFEFF` |
| `surface.glass` | `rgba(23, 26, 32, 0.72)` | `rgba(245, 246, 248, 0.85)` |
| `surface.glassStrong` | `rgba(17, 19, 24, 0.88)` | `rgba(245, 246, 248, 0.95)` |
| `border.subtle` | `rgba(245, 246, 248, 0.15)` | `rgba(23, 26, 32, 0.13)` |
| `focus.ring` | Derived `accent.primary` | Derived `accent.primary` |
| `accent.primary` | `#7DD3FC` (Orbit Dawn default) | `#0284C7` (Morning default) |
| `accent.secondary` | `#A78BFA` | `#4F46E5` |
| `success` | `#34D399` | `#059669` |
| `warning` | `#FBBF24` | `#D97706` |
| `error` | `#F87171` | `#DC2626` |
| `info` | `#60A5FA` | `#0284C7` |

### 2.3 Dynamic Wallpaper Atmosphere Envelope
- `AstraColorEngine.ensureAccessibleAccent()` verifies WCAG relative luminance contrast before applying any wallpaper-derived or custom accent color.
- Minimum contrast ratio enforced: `>= 3.5:1` for UI controls/accents and `>= 10.0:1` for primary body/headline text.

---

## 3. Material & Surface Families (`AstraSurfaceFamily`)
1. **Family A — Solid (`AstraSurfaceFamily.SOLID`):** Used for Settings root & detail pages, third-party widget hosts, and dense configuration lists.
2. **Family B — Soft Translucent (`AstraSurfaceFamily.SOFT_TRANSLUCENT`):** Used for Notification Shade cards, Control Center tiles, Global Search field, and Folder modals.
3. **Family C — Clear Atmospheric (`AstraSurfaceFamily.CLEAR_ATMOSPHERIC`):** Used sparingly for Lock Screen weather chips, App Library category containers, and Recents secondary cards.

### Corner Radius Scale (`AstraShapes`)
- `smallControl`: `12dp`
- `compactCard`: `16dp`
- `standardCard`: `20dp`
- `elevatedSheet`: `26dp`
- `heroPanel`: `32dp`
- `pill`: `CircleShape` (`999dp`)

---

## 4. Typography Hierarchy (`AstraTypography`)
| Role | Size | Line Height | Weight | Tracking |
|---|---|---|---|---|
| `Display XL` | `72sp` (`64–80px`) | `78sp` | Light (`300`) | `-1.5sp` |
| `Display L` | `54sp` (`48–60px`) | `60sp` | Light (`300`) | `-1.0sp` |
| `Headline XL` | `34sp` (`32–40px`) | `40sp` | SemiBold (`600`) | `-0.5sp` |
| `Headline L` | `28sp` (`28–32px`) | `34sp` | SemiBold (`600`) | `-0.3sp` |
| `Headline M` | `24sp` (`24–28px`) | `30sp` | Medium (`500`) | `0sp` |
| `Title L` | `20sp` (`20–22px`) | `26sp` | SemiBold (`600`) | `0sp` |
| `Title M` | `17sp` (`17–19px`) | `23sp` | Medium (`500`) | `0sp` |
| `Body L` | `16sp` (`16–18px`) | `24sp` | Regular (`400`) | `0sp` |
| `Body M` | `14sp` (`14–16px`) | `20sp` | Regular (`400`) | `0sp` |
| `Body S` | `12sp` (`12–14px`) | `17sp` | Regular (`400`) | `0sp` |
| `Label L` | `13sp` (`13–14px`) | `18sp` | Medium (`500`) | `+0.2sp` |
| `Label S` | `11sp` (`11–12px`) | `15sp` | Medium (`500`) | `+0.4sp` |

---

## 5. Motion, Haptic & Sound Specification (Sections 30–32)

### 5.1 Motion Tokens (`AstraMotion`)
| Token | Duration | Easing Curve | Primary Usage |
|---|---|---|---|
| `motion.instant` | `100 ms` | `CubicBezier(0.22, 1.0, 0.36, 1.0)` | Toggle thumb, focus ring, key selection |
| `motion.quick` | `160 ms` | `CubicBezier(0.22, 1.0, 0.36, 1.0)` | Surface exit fade, chip filter |
| `motion.standard` | `220 ms` | `CubicBezier(0.22, 1.0, 0.36, 1.0)` | Home ↔ Search, Home ↔ Shade transitions |
| `motion.emphasized` | `320 ms` | `CubicBezier(0.16, 1.0, 0.30, 1.0)` | Folder expansion, Home Editor snap |
| `motion.spatial` | `420 ms` | `CubicBezier(0.16, 1.0, 0.30, 1.0)` | Lock → Home unlock continuum |

### 5.2 Haptic Vocabulary (`AstraHaptics`)
- `haptic.light` (`CLOCK_TICK`): Selection, quick tile tap, Back navigation.
- `haptic.medium` (`CONTEXT_CLICK`): App launch confirmation, command execution, unlock.
- `haptic.heavy` (`LONG_PRESS`): Home Editor activation, app shortcut menu.
- `haptic.warning` (`REJECT`): Restricted toggle notice or unavailable capability.

---

## 6. Complete 18-Component Inventory (`core-design`)
All 18 components in `com.astra.launcher.core.design` implement `AstraComponentState` (`DEFAULT`, `PRESSED`, `FOCUSED`, `DISABLED`, `SELECTED`, `ACTIVE`, `LOADING`, `ERROR`):
1. `AstraIconButton`
2. `AstraToggle`
3. `AstraQuickTile`
4. `AstraSearchField`
5. `AstraAppIcon`
6. `AstraDock`
7. `AstraWidgetHostCard` (`AstraWidget`)
8. `AstraCard`
9. `AstraSheet` (`AstraWidgetPickerSheet` / `AstraHomeEditorPanel`)
10. `AstraDialog` (`AstraAppShortcutDialog` / `AstraFolderModal`)
11. `AstraToast`
12. `AstraNotificationCard` (`AstraNotification`)
13. `AstraMediaCard`
14. `AstraClock` (`Minimal Numeral`, `Editorial Stacked`, `Orbital Compact`)
15. `AstraSegmentedControl`
16. `AstraSlider`
17. `AstraAppPreview` (Live environment preview in `AstraPersonalizationStudioScreen`)
18. `AstraWallpaperSurface`
