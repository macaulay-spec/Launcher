# Astra Launcher — Permissions & System Integration

**Version:** 1.0 — **Date:** 2026-10-08 — **Status:** APPROVED

---

## 1. Manifest declarations (Track A)
| Permission | Why | User-facing? |
|---|---|---|
| `QUERY_ALL_PACKAGES` (scoped via `<queries>`) | List launchable apps | No |
| `BIND_APPWIDGET` / `APPWIDGET_BIND` (system-granted) | Host home widgets | Implicit at widget add |
| `SET_WALLPAPER` | Set static wallpaper | No |
| `EXPAND_STATUS_BAR` | Open/close shade for tile demos | No |
| `REQUEST_DELETE_PACKAGES` | Uninstall apps from home long-press | Per-action confirm |
| `FOREGROUND_SERVICE` (+ type) | Wallpaper engine, tile service | No |
| `RECEIVE_BOOT_COMPLETED` | Restore wallpaper/service after reboot | No |
| `QUERY_ALL_PACKAGES` alternatives | Use `<queries><intent><action MAIN/><category LAUNCHER/></intent></queries>` | No |

## 2. Services (all opt-in, all disclosed)
| Service | Type | Purpose | Consent flow |
|---|---|---|---|
| Recents/gestures | `AccessibilityService` | Overview cards, gesture navigation | Onboarding screen with prominent disclosure + system settings link; Play-policy-compliant usage (no data collection) |
| Quick settings | `TileService` | Astra tiles in shade | Tiles appear after install; tap to add |
| Wallpaper | `WallpaperService` | Live Astra wallpaper + dynamic color | Set in onboarding |
| Notifications | `NotificationListenerService` | Shade content actions | Optional, onboarding |

## 3. Default-home registration
- `AndroidManifest.xml`: `MAIN` + `HOME` + `DEFAULT` intent categories on `MainActivity`.
- Runtime: `RoleManager` check (`ROLE_HOME`) → if not default, show system chooser (`ACTION_HOME` settings intent).
- `stateNotNeeded="true"`, `launchMode="singleTask"`, `excludeFromRecents` handled by system.

## 4. Track B system integration (future)
- `priv-app` + platform signature; `sharedUserId="android.uid.system"` only if truly required.
- System permissions: `WRITE_SECURE_SETTINGS`, `STATUS_BAR_SERVICE`, `MANAGE_ACTIVITY_TASKS`, `INTERNAL_SYSTEM_WINDOW`, `DEVICE_POWER` (ambient).
- Keyguard/status bar/nav replacement points documented in AOSP `SystemUI` fork plan.
- Device Owner (`DevicePolicyManager`) path for kiosk/managed mode.

## 5. Google Play policy compliance
- AccessibilityService: prominent disclosure, no sensitive-data collection, usage limited to recents/gestures.
- `QUERY_ALL_PACKAGES`: only with core launcher functionality justification.
- No ad IDs, no analytics of app usage off-device; suggestions computed on-device.
