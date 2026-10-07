# ASTRA LAUNCHER
## Master Design + Product + Interaction + Build Specification
### Single Source of Truth for the Complete Phone-Wide Launcher Experience

**Document purpose:** This is the single source-of-truth file an AI design/build agent should read before touching the project. It defines what Astra is, what it should look like, how it behaves, what can be implemented as a normal Android launcher, what requires special permissions or privileged/system integration, how wallpapers are created, and how the finished product is accepted.

**Working product name:** Astra Launcher

**Target platform:** Android phones first.

**Primary design target:** Modern Android phones with small to large displays, with special attention to mid-range and low-memory devices. The experience must remain elegant and usable without requiring flagship hardware.

**Implementation preference:** Native Android architecture is the reference implementation for deep launcher/system integration. Kotlin + Jetpack Compose is preferred for UI layers where appropriate, with Android launcher/system APIs used directly where Compose alone is insufficient.

**Core objective:** Build a credible, original, premium phone interface that feels like a complete visual environment rather than a decorative launcher. Astra should combine the best ideas from Samsung One UI, Google/Pixel Material, and Apple iOS/iPadOS while remaining visually and structurally original.

---

# 1. NORTH STAR

Astra is a **cinematic intelligent launcher and phone control environment**.

It is not:
- a Samsung clone
- an iOS clone
- a generic Android launcher with a fancy wallpaper
- a glassmorphism demo
- a dashboard packed with widgets
- a Dribbble concept that collapses when someone actually taps a button

It is:

> **One coherent visual world that connects lock screen, wallpaper, home, search, apps, notifications, controls, widgets, recents, settings, media, charging, and system feedback.**

The fundamental design idea is **continuity**.

The user should feel that every screen belongs to the same operating environment. The wallpaper, surfaces, typography, controls, iconography, motion, light, depth, and sound/haptics should all speak the same visual language.

---

# 2. DESIGN DNA

## 2.1 Samsung principles to borrow

Borrow principles, not assets or copied layouts.

- One-handed reachability.
- A clear distinction between viewing space and interaction space.
- Comfortable hierarchy on large screens.
- Practical information density.
- Personalization.
- Responsive layouts.
- Fast paths for frequent tasks.
- Readable text and accessible controls.

Samsung's current One UI guidance explicitly emphasizes focusing on the task, natural interaction, visual comfort, and responsive layouts, including a viewing area and interaction area that makes controls easier to reach. Use that philosophy as ergonomics inspiration, not as a visual template. [Research reference: Samsung One UI Developer Overview]

## 2.2 Apple principles to borrow

- Relentless spacing discipline.
- High-quality typography.
- Spatial coherence.
- Purposeful materials.
- Controlled translucency.
- Clear layering.
- Motion that communicates state and continuity.
- Strong visual hierarchy without visual noise.
- Respect for context and content.

Apple's current Human Interface Guidelines describe materials as tools for depth and hierarchy, and specifically warn against putting highly translucent material everywhere. They also emphasize purposeful motion and accessibility-aware animation. Use those principles to make Astra calm and dimensional rather than glossy for its own sake. [Research references: Apple HIG Materials, Motion, Layout]

## 2.3 Google / Material principles to borrow

- Semantic design tokens.
- Dynamic color concepts.
- Adaptive layout.
- Consistent component states.
- Structured typography.
- Responsive surfaces.
- Accessibility as a system property rather than an afterthought.

Android's current guidance supports wallpaper-derived dynamic color and semantic color roles, with custom fallback schemes when dynamic color is unavailable. Astra should use the concept but control it more tightly so the entire OS retains a recognizable Astra identity. [Research references: Android Developers Material 3 color and dynamic color]

## 2.4 Nothing principles to borrow

- Strong identity.
- Cohesion across system surfaces.
- Distinctive visual grammar.
- Intentional micro-details.
- Hardware/software relationship.
- A recognizable personality without clutter.

Do not copy Glyph graphics, dot matrices, exact typefaces, or Nothing OS layouts. Nothing is useful as evidence that a phone interface can become a recognizable design language rather than a pile of conventional Android screens. [Research reference: Nothing OS / Glyph materials]

---

# 3. THE ASTRA VISUAL IDEA

## 3.1 The visual sentence

**Astra = cinematic calm + intelligent utility + spatial depth + restrained personalization.**

The visual system should feel:
- premium
- quiet
- precise
- atmospheric
- modern
- tactile
- intelligent
- customizable
- slightly mysterious
- emotionally controlled

It should not feel:
- cartoonish
- toy-like
- over-glossy
- aggressively futuristic
- monochrome for the sake of being monochrome
- neon cyberpunk
- overloaded with blur
- permanently red/purple/blue

## 3.2 The central visual metaphor

Think of the phone as a **window into a living visual atmosphere**.

The wallpaper is not a background asset. It is one of the visual inputs that influence the system.

The system derives:
- accent atmosphere
- surface temperature
- highlight strength
- contrast strategy
- lock-screen treatment
- widget emphasis
- media surface tint
- selected-state glow

from the wallpaper/theme, while preserving accessibility and a stable Astra visual identity.

The wallpaper can change the mood, but should not make the UI lose its identity.

---

# 4. BRAND SYSTEM

## 4.1 Working brand mark

Use a simple Astra symbol built around the idea of:
- orbital geometry
- a small star point
- a controlled arc
- an abstract A/orbit hybrid

Avoid literal rockets, planets, galaxies, stars scattered everywhere, or NASA-like branding.

The logo should remain clean at 16 px and elegant at 128 px.

## 4.2 Logo behavior

Primary:
- solid mark
- light and dark variants
- subtle luminous version for splash/charging

Do not use a glowing logo everywhere. Reserve glow for a state transition or brand moment.

---

# 5. COLOR SYSTEM

Astra should NOT rely on one fixed accent color.

### Base palette

`Astra Black`      #0A0B0D
`Graphite 1`       #111318
`Graphite 2`       #171A20
`Graphite 3`       #20242C
`Cloud`            #F5F6F8
`Mist`             #D8DBE1
`Ink`              #171A20

### Semantic roles

`text.primary`
`text.secondary`
`text.tertiary`
`text.disabled`
`icon.primary`
`icon.secondary`
`surface.base`
`surface.raised`
`surface.floating`
`surface.glass`
`surface.glassStrong`
`border.subtle`
`focus.ring`
`accent.primary`
`accent.secondary`
`success`
`warning`
`error`
`info`

### Accent strategy

Astra's default accent is a restrained **astral cyan-violet spectrum**, but the default UI must not become a purple AI dashboard.

Recommended default accent family:
- cool blue-violet
- icy cyan
- very subtle violet edge

Use accent sparsely.

Accent belongs primarily to:
- active state
- focus
- selected control
- progress
- confirmation
- small visual highlights

Never flood the whole screen with accent color.

### Dynamic palette

When the wallpaper contains a strong color, Astra may derive a secondary atmosphere from it.

Rules:
1. The source color must pass contrast checks before being used for text/control combinations.
2. The derived palette must remain within an Astra-approved tonal envelope.
3. Dynamic wallpaper color must never turn the interface into a completely different brand.
4. Critical status colors remain semantically stable.
5. Error is always recognizable as error; success as success.

Android's current Material guidance uses a wallpaper-derived color system and semantic roles. Astra should use the same underlying idea while applying stronger brand constraints. [Research reference: Android Developers dynamic color guidance]

---

# 6. MATERIAL / SURFACE LANGUAGE

## 6.1 Core rule

**Do not make everything glass.**

Use three primary surface families:

### A. Solid surfaces
Used for:
- settings pages
- dense lists
- high-legibility areas
- critical dialogs
- content that must remain stable

### B. Soft translucent surfaces
Used for:
- notification shade
- Control Center
- floating search
- media controls
- quick actions
- temporary overlays

### C. Clear atmospheric surfaces
Used sparingly for:
- immersive media
- lock-screen overlays
- hero controls
- wallpaper-aware floating elements

Apple's current materials guidance similarly treats translucent material as a functional layer and warns against indiscriminate use in content layers. Astra should follow that restraint. [Research reference: Apple HIG Materials]

## 6.2 Blur

Blur should create separation, not decoration.

Use:
- low blur for floating pills
- medium blur for navigation/system overlays
- stronger blur when underlying wallpaper would reduce readability

Never blur the whole screen merely because the technology exists.

## 6.3 Elevation

Depth comes from a combination of:
- tonal difference
- shadow
- blur
- scale
- translucency
- motion

Avoid thick borders around every card.

## 6.4 Corner language

Primary radii:
- 12 px small control
- 16 px compact card
- 20 px standard card
- 26 px elevated card/sheet
- 32 px hero panel
- pill for compact controls

Radii should scale with component size rather than becoming arbitrary.

---

# 7. TYPOGRAPHY

Primary typography should feel like a premium system UI rather than a marketing website.

Use a highly readable modern variable sans family available under a practical license for Android. The final implementation should use a real production font, not a placeholder.

### Hierarchy

`Display XL` 64–80 px
`Display L` 48–60 px
`Headline XL` 32–40 px
`Headline L` 28–32 px
`Headline M` 24–28 px
`Title L` 20–22 px
`Title M` 17–19 px
`Body L` 16–18 px
`Body M` 14–16 px
`Body S` 12–14 px
`Label L` 13–14 px
`Label S` 11–12 px

Do not use giant typography everywhere. The lock screen may use enormous numerals because the context justifies it. Settings should not look like a fashion magazine.

Use generous line height.

Numerals for clocks and statistics should use optical alignment.

---

# 8. SPACING + GRID

Base spacing unit: 4 px.

Preferred rhythm:
4 / 8 / 12 / 16 / 20 / 24 / 28 / 32 / 40 / 48 / 64

Primary horizontal safe margin:
- compact phone: 16–20 px
- standard phone: 20–24 px
- large phone: 24–32 px

Use responsive content width rather than hardcoding large-screen values.

Respect display cutouts, gesture insets, status bar and navigation insets.

---

# 9. ICONOGRAPHY

Create or adopt a coherent Astra icon family with:
- consistent optical stroke
- rounded geometric structure
- consistent corner behavior
- active/inactive variants
- filled/outline variants where needed

Never mix five incompatible icon families.

Critical system controls should use familiar metaphors.

Originality belongs in the visual grammar, not in making the airplane-mode icon incomprehensible.

---

# 10. WALLPAPER SYSTEM

## 10.1 Wallpaper philosophy

Wallpaper is a first-class design component.

Astra should launch with a small curated wallpaper collection. The first set should establish the identity.

There should be at least three hero wallpapers:

1. **Orbit Dawn**
2. **Nocturne Flow**
3. **Glass Horizon**

Each should work behind:
- lock screen
- home screen
- widgets
- app drawer
- search
- notification shade
- Control Center

The wallpaper should have safe zones so important content is not destroyed by clock, widgets, icons or system controls.

## 10.2 Wallpaper composition rules

- 9:19.5 or 9:20 portrait base composition.
- 4K source where feasible.
- Main visual subject should not sit directly under the primary lock-screen clock.
- Avoid excessive high-frequency texture.
- Avoid text baked into the wallpaper.
- Keep central region visually calm enough for foreground content.
- Corners can carry more detail because controls tend to avoid them.
- Top and bottom should support subtle luminance gradients for readability.
- Create light and dark tonal versions without simply inverting the artwork.

## 10.3 WALLPAPER GENERATION PROMPT 1 — ORBIT DAWN

Create a premium abstract smartphone wallpaper for an original operating system called Astra. Portrait 9:20 composition. The scene is an atmospheric field of soft mineral-blue, muted indigo, pale silver and extremely subtle warm light, with one elegant orbital arc sweeping through the composition. Add very restrained depth, glass-like atmospheric layers and soft volumetric light. The central upper area must remain calm and readable for a large lock-screen clock. The lower third may contain slightly richer depth but must remain suitable for widgets and app icons. No stars scattered everywhere, no planets, no text, no logos, no neon cyberpunk, no sci-fi spaceship imagery, no obvious Apple/Samsung/Google resemblance. Premium industrial design aesthetic, editorial photography-level restraint, soft cinematic lighting, physically plausible gradients, subtle texture, extremely clean negative space, sophisticated and timeless.

## 10.4 WALLPAPER GENERATION PROMPT 2 — NOCTURNE FLOW

Create a premium dark atmospheric smartphone wallpaper for the Astra operating system. Portrait 9:20. Deep graphite, smoked black, desaturated blue and a restrained thread of violet light moving diagonally through layered abstract fluid forms. The composition should resemble illuminated architectural glass and liquid shadow rather than a galaxy. Create depth through soft reflections, translucent layers and controlled gradients. Keep the upper center dark and quiet enough for bright white clock typography. Allow a slightly brighter visual anchor in one lower corner for depth, but preserve generous negative space. No text, no people, no objects, no planets, no stars, no cyberpunk, no excessive glow, no chrome effects. Sophisticated premium phone OS wallpaper, understated, cinematic, calm, modern, high-end industrial design.

## 10.5 WALLPAPER GENERATION PROMPT 3 — GLASS HORIZON

Create an original premium abstract smartphone wallpaper for Astra. Portrait 9:20. A broad luminous horizon made from translucent architectural planes, soft frosted glass, pale cyan, graphite, subtle champagne highlights and very small amounts of cool lavender. The scene should feel like a futuristic physical material photographed in soft studio light. Make the central top region sparse for lock-screen information and the mid/lower regions slightly more dimensional for home-screen layering. Use restrained reflections and depth. No text, no logo, no stars, no planets, no humans, no buildings, no sci-fi spacecraft, no direct imitation of iOS, One UI or Pixel wallpapers. The result should feel like a new hardware brand's signature wallpaper: elegant, minimal, spatial, tactile, sophisticated.

## 10.6 Wallpaper validation

Every wallpaper must be previewed behind:
- large lock-screen time
- short notification stack
- 4 app icons
- one medium widget
- search field
- Control Center

Reject wallpaper if:
- text becomes difficult to read
- controls vanish into the artwork
- visual detail competes with the primary content
- the wallpaper reads as generic stock art
- the wallpaper looks obviously copied from another operating system

---

# 11. LOCK SCREEN

## Goal

The lock screen should be one of Astra's signature moments.

### Composition

Top:
- status indicators kept extremely quiet

Upper center:
- optional weather/condition chip

Center:
- large adaptive clock
- date beneath

Lower center:
- notifications, grouped by priority

Bottom:
- left quick access
- biometric/unlock affordance
- right quick access

The clock should adapt to wallpaper luminance.

### Clock styles

At least three:
1. Minimal Numeral
2. Editorial Stacked
3. Orbital Compact

Clock styling should be a theme variant, not hardcoded screen art.

### Interactions

- tap notifications to expand
- swipe to dismiss a notification group
- swipe toward Control Center where supported by the implementation model
- tap-and-hold to enter lock-screen customization
- biometric status subtly changes the lower affordance

### Safety

Sensitive notification content must obey device privacy settings.

Do not expose full private message content on a locked device unless Android's security/privacy state permits it.

---

# 12. ALWAYS-ON DISPLAY

AOD should be extremely restrained.

Show:
- time
- date
- battery
- selected essential notifications

Use lower brightness, reduced animation and a minimal visual footprint.

Do not animate constantly.

Do not burn-in static UI positions. Where the platform allows, use safe movement strategies.

If the normal launcher cannot replace the device's true AOD surface, implement an Astra AOD preview/companion experience rather than falsely claiming system-level replacement.

---

# 13. HOME SCREEN

## Core composition

Astra Home should prioritize:
1. identity
2. time/context
3. fastest tasks
4. apps
5. personalization

### Default home

Top:
- compact status area
- optional contextual micro-information

Upper-middle:
- large clock/weather/date composition

Middle:
- 4–6 high-priority app shortcuts or dynamic favorites

Lower-middle:
- optional intelligent widget stack

Bottom:
- adaptive dock with 4–5 apps
- subtle gesture affordance

### Home behavior

- swipe up -> app library / all apps
- swipe down -> notifications or a configurable universal shade action
- swipe horizontally -> secondary spaces/pages
- long press -> Home editor
- pinch -> overview/customization mode

### No clutter rule

Do not ship with ten widgets just to make the mockup look rich.

Default home should look good when almost empty.

---

# 14. APP DRAWER / APP LIBRARY

The app library should be fast and visually calm.

### Default mode

Search at top.

Below it:
- recent apps
- suggested apps
- grouped app grid

### Organization

Support:
- all apps
- alphabetical
- intelligent categories
- favorites
- recently used
- hidden apps
- private apps where platform/security model permits

### Visual style

Use an almost invisible atmospheric surface over the wallpaper.

Icons should remain primary.

Avoid giant cards around every icon.

### Gestures

- swipe up from Home
- fast alphabet scroll
- keyboard search
- long press app
- drag to Home if supported

---

# 15. GLOBAL SEARCH / COMMAND PALETTE

This is a major Astra differentiator.

## Trigger

Multiple configurable triggers:
- swipe gesture
- keyboard shortcut when physical keyboard exists
- home double tap if user chooses
- dedicated search affordance

## Search screen

The screen should transform rather than simply overlay a box.

Wallpaper dims slightly.

The search field becomes the focal element.

Below it, adaptive suggestions appear.

### Search domains

Search should eventually support:
- apps
- contacts
- settings
- shortcuts
- files
- calendar
- notifications where permission allows
- music/media
- recent actions

### Command examples

`open YouTube`
`turn on Wi-Fi`
`find Settings`
`call Mum`
`show battery settings`
`open camera`
`start timer`

Natural-language commands are optional and should degrade gracefully to text search.

## Search ranking

Prioritize:
1. exact match
2. recently used
3. frequently used
4. contextual relevance
5. fuzzy match

Do not require an online AI service for basic app search.

---

# 16. KEYBOARD-FIRST INTERACTION

Astra should feel excellent for fast typing.

### Search input

- immediate focus
- no unnecessary transition delay
- full keyboard-safe layout
- clear cancel/back behavior
- result sections collapse when typing becomes specific

### Keyboard navigation

When a hardware keyboard exists:
- Arrow Up/Down moves results
- Enter launches
- Esc dismisses
- Tab cycles sections

For Android software keyboard:
- use IME action appropriately
- never let the keyboard cover the selected result
- keep search context visible

---

# 17. NOTIFICATION SHADE

The notification system should feel like Astra without pretending it controls notification internals it cannot access.

### Visual model

Top:
- time/context/status

First section:
- urgent/priority notifications

Second section:
- regular notifications

Third:
- silent/low-priority notifications

Use grouped, expandable cards.

Avoid endlessly tall identical rectangles.

### Interactions

- tap opens source app
- swipe removes if allowed
- long press shows notification controls where Android permits
- clear-all is visually quiet and easy to reach

If a NotificationListenerService is implemented, it requires user-granted notification access. Android documents this service as the mechanism for receiving posted/removed notifications and their ranking. [Research reference: Android NotificationListenerService]

---

# 18. CONTROL CENTER / QUICK SETTINGS

Do not directly clone iOS Control Center or Samsung Quick Panel.

Astra's version is a **spatial control plane**.

### Layout

Top:
- time
- connectivity summary
- device state

Primary zone:
- Wi-Fi
- Bluetooth
- Mobile Data
- Airplane mode
- hotspot

Secondary zone:
- brightness
- volume
- flashlight
- rotation
- screenshot
- battery saver

Media:
- compact persistent media card when active

### Interaction

Tiles should have at least:
- inactive
- hover/focus
- active
- unavailable
- processing

Where Android APIs do not allow an ordinary application to directly toggle a setting, the UI must explain or deep-link to the official system setting rather than pretending the toggle worked.

---

# 19. STATUS BAR + NAVIGATION LANGUAGE

Status bar should be:
- minimal
- adaptive
- readable
- visually integrated with wallpaper and surface context

Do not permanently hide essential system information.

Navigation behavior must respect Android's gesture/navigation model.

Astra should feel visually cohesive with the device's system navigation even when Astra cannot replace that system component.

---

# 20. WIDGET SYSTEM

Widgets should feel like citizens of the Astra visual environment.

Support:
- first-party Astra widgets
- standard Android app widgets where host support permits

First-party widgets:
- clock
- weather
- calendar
- battery
- music
- quick actions
- screen time/focus
- notes

Widget surfaces should use Astra tokens.

Where a third-party widget has its own styling, allow the content to coexist without breaking its host contract.

Android's AppWidget APIs explicitly support widgets for home screen contexts and provide launcher-host integration. Use the platform contracts rather than trying to redraw third-party widgets manually. [Research reference: Android AppWidgetManager]

---

# 21. RECENTS / MULTITASKING

Astra should not fake app thumbnails.

Use platform-provided task information where available.

### Visual style

Large, slightly elevated app cards with:
- app icon
- app name
- content preview
- optional quick action

The current app should have the clearest visual focus.

### Gestures

- swipe vertically through recent tasks
- tap to restore
- dismiss gesture
- optional split-screen shortcut where supported

### Motion

Cards should follow the user's gesture naturally.

Apple's motion guidance stresses purposeful, physically consistent motion. Apply that principle without copying Apple's exact animations. [Research reference: Apple HIG Motion]

---

# 22. SETTINGS

Settings should be the most practical Astra surface.

### Main sections

1. Personalization
2. Home Screen
3. Lock Screen
4. Search
5. Notifications
6. Gestures
7. Widgets
8. Performance
9. Privacy & Security
10. Accessibility
11. Apps
12. Backup & Data
13. About Astra

### Visual structure

Use:
- large page title
- short explanatory subtitle
- grouped sections
- quiet separators
- simple controls

Do not turn Settings into a dense spreadsheet.

---

# 23. PERSONALIZATION STUDIO

This is the place where users shape their Astra environment.

### Customization categories

- wallpapers
- clock style
- icon style
- accent palette
- home layout
- widget style
- dock style
- notification style
- Control Center density
- animation intensity
- haptics
- fonts where practical/legal

### Preview

Customization should show a live phone preview.

The user should see the full environment change, not isolated controls.

### Theme presets

Ship at least:
- Astral
- Graphite
- Morning
- Nocturne
- Glass Horizon

---

# 24. PERMISSIONS + SECURITY UI

Permission dialogs should be visually Astra-branded only where legally/platform-appropriate, without disguising Android's actual consent mechanism.

Before asking for access:
- explain why it is needed
- explain what changes if permission is denied
- offer a safe alternative when possible

After denial:
- never loop infinitely
- provide a route to Settings where appropriate
- make the disabled state explicit

Sensitive actions:
- changing default launcher
- notification access
- overlay-like capabilities if applicable
- accessibility service if genuinely required
- device admin features if ever used

must have clear explanations.

Never use dark patterns to obtain privileged access.

---

# 25. ONBOARDING

Maximum 5–6 meaningful steps.

### Step 1: Welcome

Show the full visual identity immediately.

### Step 2: Make Astra Home

Request the Android Home role/default launcher state.

Android exposes the `ROLE_HOME` role starting at API 29. The implementation must use the platform role flow rather than pretending the app becomes the launcher without user action. [Research reference: Android RoleManager]

### Step 3: Choose your atmosphere

Wallpaper/theme selection.

### Step 4: Arrange your essentials

Choose primary apps and widget placement.

### Step 5: Search + controls

Teach the global search gesture and control surface.

### Step 6: Finish

Transition into Home.

The onboarding should feel like a cinematic setup sequence, not a permission interrogation.

---

# 26. MEDIA EXPERIENCE

Media controls should adapt to album artwork when available.

### Media card

- artwork
- title
- artist
- play/pause
- next/previous
- progress
- output route where available

### Dynamic media atmosphere

Optionally derive a muted secondary tint from artwork, but preserve text contrast and Astra's identity.

Android already supports color systems derived from content in related Material contexts; use the concept conservatively. [Research reference: Android dynamic color guidance]

---

# 27. CHARGING EXPERIENCE

Charging should be a small premium moment, not an advertisement for the launcher.

### Plug-in event

Astra may show:
- subtle center pulse
- battery percentage
- estimated charging state if available
- restrained orbital animation

Animation ends quickly.

No permanent giant branding.

If the app lacks authority to replace the device's native charging UI, provide an optional charging visualization only where Android permits, and never claim it owns the system charging surface.

---

# 28. ERROR / EMPTY / LOADING / OFFLINE / RECOVERY

Every system surface needs intentional non-happy states.

## Loading

Use subtle skeletons or shimmer only when useful.

## Empty

Use helpful explanation + one action.

## Offline

State:
`Astra is offline.`
Explain what still works locally.

## Permission denied

State what cannot work and why.

## Crash/recovery

Provide:
- restart shell
- reset layout option
- safe mode if feasible
- diagnostics without dumping frightening developer logs at normal users

## Low battery

Reduce animation and decorative effects.

Do not merely dim the UI; preserve task completion.

## Performance degradation

Gracefully reduce:
- blur
- live wallpaper effects
- complex transitions
- nonessential animation

The UI should become simpler, not broken.

---

# 29. ACCESSIBILITY

Accessibility is part of the visual system.

Support:
- scalable text
- sufficient contrast
- touch target minimums appropriate to Android guidance
- screen reader labels
- reduced motion
- high contrast mode where possible
- color-independent semantic states
- haptic alternatives to motion-only feedback
- large text layouts

Do not communicate status using color alone.

Apple's current motion guidance explicitly notes that motion should not be the sole method for communicating important information and should respond to accessibility settings. Astra should adopt that principle. [Research reference: Apple HIG Motion]

---

# 30. MOTION SYSTEM

Motion is the connective tissue of the OS.

### Motion principles

1. Fast for frequent actions.
2. Soft for context changes.
3. Responsive to direct touch.
4. Short enough to avoid feeling slow.
5. Consistent across surfaces.
6. Never decorative for its own sake.

### Suggested motion tokens

`motion.instant` 100 ms
`motion.quick` 160 ms
`motion.standard` 220 ms
`motion.emphasized` 320 ms
`motion.spatial` 420 ms

Use easing that produces smooth acceleration and natural settling.

Avoid spring overshoot on every control.

### Major transitions

Home -> Search:
- home content gently recedes
- wallpaper darkens slightly
- search field expands into focus

Home -> App:
- selected icon visually anchors transition
- app surface expands from context where feasible

Shade -> Home:
- panel retracts toward its origin

Recents -> App:
- task card expands naturally

Lock -> Home:
- clock/lock content recedes
- home emerges without a harsh cut

---

# 31. HAPTIC LANGUAGE

Define a small vocabulary.

`haptic.light`
- selection
- small toggle

`haptic.medium`
- launch success
- confirmation

`haptic.heavy`
- destructive action confirmation if appropriate

`haptic.warning`
- unavailable/safety state

Never vibrate on every tap.

---

# 32. SOUND LANGUAGE

Optional and conservative.

Astra should have:
- subtle unlock cue
- restrained navigation cue
- charging confirmation cue
- error cue

Sounds should be short, soft, and recognizable.

Respect system sound settings.

---

# 33. APP LAUNCHER ARCHITECTURE

## Primary technology direction

Native Android Kotlin implementation.

Use Jetpack Compose for Astra-owned UI where appropriate.

Use Android framework services and APIs for:
- Launcher/Home role
- package discovery
- app launching
- widgets
- task/recents integration where permitted
- notifications where permitted
- device settings deep links
- shortcuts
- accessibility support

Do not use React Native as the core implementation for system-surface features where native Android access is required.

## Core modules

`app`
- launcher shell

`core-design`
- tokens
- typography
- shapes
- icon system
- motion

`feature-home`

`feature-search`

`feature-apps`

`feature-lock-preview`

`feature-notifications`

`feature-controls`

`feature-widgets`

`feature-recents`

`feature-settings`

`feature-personalization`

`feature-onboarding`

`core-platform`
- role management
- package manager
- widget host
- system intents
- notification access state
- device configuration

`core-storage`
- user preferences
- layout state
- theme state

`core-performance`
- caches
- lazy loading
- startup timing
- bitmap/image loading

---

# 34. PLATFORM REALITY RULES

This section exists to prevent the AI builder from creating fictional capabilities.

## 34.1 Default launcher

Astra must implement the Android Home role properly.

Do not simply show an in-app “launcher screen” and call it a system launcher.

## 34.2 Lock screen

A normal third-party launcher does not automatically gain ownership of every device lock-screen surface. Build an Astra lock-screen experience only to the extent allowed by the device/platform. Where true lock-screen replacement is unavailable, create a faithful lock-screen design/preview and integrate permitted entry points without pretending to replace the secure lock screen.

## 34.3 Notification shade

A third-party launcher cannot freely replace the entire system notification shade on all Android devices. If NotificationListenerService is used, it is permission-gated. The app must show a custom notification dashboard/preview or use permitted overlays only when platform rules allow it.

## 34.4 Control Center / Quick Settings

A normal app cannot arbitrarily control every system setting. For unsupported toggles, provide deep links or appropriate permission flows.

## 34.5 AOD / charging

True AOD and charging UI ownership may require OEM/system integration. Treat these as progressive enhancement surfaces.

## 34.6 Goal

Build the maximum credible experience using public Android APIs, and isolate privileged-only features behind explicit capability detection.

The product must never crash or lie when a capability is unavailable.

---

# 35. CAPABILITY MATRIX

Create a runtime capability layer.

Example capability flags:

`canBeDefaultHome`
`hasNotificationAccess`
`canHostWidgets`
`canReadPackages`
`supportsShortcuts`
`supportsExactSystemToggle`
`supportsLockSurface`
`supportsAodSurface`
`supportsChargingSurface`
`supportsAdvancedRecents`

Every feature checks capability before rendering unsupported controls.

---

# 36. DATA MODEL

Persist at minimum:

`ThemeSettings`
- theme id
- wallpaper id
- accent source
- light/dark mode
- icon style
- clock style

`HomeLayout`
- pages
- icon positions
- dock
- widgets
- hidden apps
- folders

`SearchPreferences`
- enabled providers
- ranking preferences
- search history settings

`NotificationPreferences`
- category handling
- privacy mode

`GesturePreferences`
- home gesture
- search gesture
- shade gesture

`PerformancePreferences`
- animations
- blur
- wallpaper effects

Keep storage local-first for baseline functionality.

---

# 37. SEARCH INDEX

Index installed apps locally.

Fields:
- package name
- app name
- aliases
- category
- usage score
- last used timestamp
- shortcut actions

Search must work offline.

Do not make the launcher dependent on cloud AI for opening an app.

Cloud AI, if later added, should be a separate enhancement layer.

---

# 38. PERFORMANCE TARGETS

The launcher is system-adjacent. It must feel faster than the average social app.

Targets:
- cold-start as fast as realistically achievable on target hardware
- no blocking network calls before Home is visible
- app list cached locally
- wallpaper loaded efficiently
- lazy-load heavy widgets
- avoid unnecessary recomposition
- avoid constant background polling
- minimize memory leaks

Performance mode should automatically reduce expensive effects on constrained devices.

---

# 39. LOW-END DEVICE MODE

Astra must still look deliberate on lower-end phones.

When performance pressure is detected:
- reduce blur radius
- disable expensive live effects
- simplify shadows
- reduce transition complexity
- reduce background processing
- keep static wallpaper

The interface should not become ugly. It should become calmer.

---

# 40. HOME EDITOR

Long press Home -> Edit mode.

Provide:
- wallpaper
- widgets
- grid
- dock
- icon size
- labels
- page management
- search position
- clock style
- theme

Show a live preview.

Use drag-and-drop with snap guides.

Do not hide every useful setting behind obscure nested menus.

---

# 41. FOLDERS

Folder opening should feel like an expansion of the Home environment.

Use an elevated surface with the same material rules as the rest of Astra.

No miniature desktop inside a folder.

Folder title is editable.

Support:
- create
- rename
- reorder
- remove

---

# 42. APP SHORTCUTS

Respect Android app shortcuts where available.

Long-press an app to show:
- shortcut actions
- app info
- remove from Home
- uninstall if permitted

Do not erase platform affordances that users expect.

---

# 43. PRIVACY

Privacy should be visible but not paranoid.

Show clear indicators for:
- microphone use
- camera use
- location-related state where platform exposes it
- notification access
- sensitive search/index permissions

The launcher should never secretly collect private user content for convenience.

Search data should have a clear local/cloud distinction.

---

# 44. AI LAYER, OPTIONAL FUTURE CAPABILITY

AI should be treated as a layer above deterministic launcher functions.

Baseline:
- local app search
- deterministic commands
- fast launches

Optional future AI:
- natural language commands
- contextual suggestions
- smart organization
- semantic file search
- proactive routines

Never make opening WhatsApp require an LLM round trip.

---

# 45. VISUAL SCREEN INVENTORY

The design/build agent must create concrete screen definitions for at least these frames:

01. Splash / brand moment
02. First-run onboarding 01
03. First-run onboarding 02
04. Home default dark
05. Home default light
06. Home personalized
07. Home edit mode
08. App drawer
09. Search idle
10. Search typing
11. Search result selected
12. Search command mode
13. Notification shade collapsed
14. Notification shade expanded
15. Control Center partial
16. Control Center full
17. Recents overview
18. Settings root
19. Settings detail
20. Personalization studio
21. Wallpaper picker
22. Clock picker
23. Widget picker
24. Icon style picker
25. Permission pre-explanation
26. System permission handoff
27. Permission denied state
28. Media controls
29. Charging
30. Low battery
31. Offline
32. Loading
33. Empty state
34. Error state
35. Recovery state
36. Lock-screen concept
37. AOD concept
38. Privacy indicator state
39. Third-party widget coexistence
40. Accessibility large-text variant

Each frame should have a phone-frame presentation plus component annotations for design review.

---

# 46. FIGMA-READY STRUCTURE

Create a Figma-friendly component taxonomy.

## Foundations
- Color
- Typography
- Spacing
- Radius
- Shadow
- Blur
- Motion
- Iconography

## Components
- AstraIconButton
- AstraToggle
- AstraQuickTile
- AstraSearchField
- AstraAppIcon
- AstraDock
- AstraWidget
- AstraCard
- AstraSheet
- AstraDialog
- AstraToast
- AstraNotification
- AstraMediaCard
- AstraClock
- AstraSegmentedControl
- AstraSlider
- AstraAppPreview
- AstraWallpaperSurface

## Variants
Every reusable component should define relevant states:
- default
- pressed
- focused
- disabled
- selected
- active
- loading
- error

## Pages
Suggested Figma pages:
1. Cover
2. Brand / Visual Direction
3. Wallpapers
4. Color
5. Typography
6. Icons
7. Components
8. Home
9. Search
10. Notifications
11. Control Center
12. Recents
13. Settings
14. Lock Screen
15. AOD
16. Motion
17. Accessibility
18. Prototype Flows
19. Implementation Handoff

---

# 47. VISUAL REVIEW BOARD

The first design review should show, side by side:

- Astra Lock Screen
- Astra Home
- Astra Search
- Astra Control Center
- Astra Notifications
- Astra Recents
- Astra Settings
- Astra Personalization

All should visibly share:
- same typography
- same surfaces
- same corner logic
- same accent logic
- same spatial rules
- same icon language
- same motion philosophy

If one screen looks like Apple, another looks like Samsung, and another looks like a random Android template, reject the design.

---

# 48. PRODUCT BEHAVIOR PRINCIPLES

## Fast path principle

Frequent tasks should require as little friction as possible.

## Context principle

The UI changes emphasis based on context, not randomly.

## Calm principle

The interface should never feel busy simply because the device can display more information.

## Continuity principle

Transitions should preserve spatial relationships.

## Truth principle

Never fake a successful system action when Android denied it.

## Graceful degradation principle

Unsupported platform features must become clear, functional fallbacks.

## Personal identity principle

Customization should change Astra's atmosphere while preserving Astra's core language.

---

# 49. ANTI-GOALS

Never do these:

- clone Apple Control Center
- clone Samsung Quick Panel
- clone Pixel launcher literally
- use a generic bottom navigation bar everywhere
- use glass on every component
- use neon gradients everywhere
- make every card enormous
- fill Home with widgets
- use unreadable tiny text
- create fake system toggles that do nothing
- request dangerous permissions without necessity
- use remote AI for deterministic launcher actions
- depend on network connectivity for Home
- build only static screenshots
- build a non-functional prototype and call it finished
- use fake app icons
- use placeholder wallpaper in final presentation
- let wallpaper destroy text readability
- create 20 animation styles
- make every interaction bounce

---

# 50. BUILD ORDER

The implementation agent should work in this order.

## Phase 1: Foundations

- Android project setup
- theme system
- design tokens
- icon system
- navigation shell
- local persistence

## Phase 2: Functional launcher core

- Home role
- package scanning
- app launch
- app drawer
- Home layout
- dock
- folders
- shortcuts

## Phase 3: Visual system

- wallpaper engine
- adaptive colors
- surfaces
- blur policy
- typography
- motion
- haptics

## Phase 4: Search

- local index
- ranking
- keyboard interaction
- command actions

## Phase 5: Widgets + personalization

- WidgetHost integration
- widget picker
- theme editor
- live preview
- wallpaper picker

## Phase 6: System-adjacent surfaces

- notification access where available
- notification dashboard
- Control Center-like panel for supported actions
- media controls
- charging/AOD preview capabilities

## Phase 7: Hardening

- accessibility
- low-RAM optimization
- crash recovery
- state restoration
- startup reliability
- device compatibility

## Phase 8: Polish

- transition tuning
- shadow/blur tuning
- wallpaper integration
- icon optical corrections
- empty/loading/error states
- onboarding refinement

---

# 51. ACCEPTANCE TESTS

The build is not complete until all of these pass.

## Launcher

- Astra can be selected as the default Home app using Android's supported role flow.
- Pressing Home returns to Astra.
- Apps launch reliably.
- App drawer reflects installed apps.
- App search works offline.
- Home state persists after restart.

## Visual system

- All major screens use the same token system.
- Dark theme is coherent.
- Light theme is coherent.
- Wallpaper influences atmosphere without destroying readability.
- No screen looks copied from another OEM.

## Interaction

- Gestures feel predictable.
- Search opens quickly.
- Search results are keyboard navigable.
- Home edit mode works.
- Widgets can be added when platform support exists.
- App shortcuts work where apps provide them.

## Resilience

- Network loss does not break Home.
- Permission denial does not crash the launcher.
- Missing notification access does not break normal launcher use.
- Unsupported system controls show appropriate fallback behavior.
- Restarting the phone does not corrupt layout state.

## Accessibility

- Text scaling works.
- Important controls remain reachable.
- Focus states are visible.
- Screen-reader labels exist for interactive elements.
- Reduced-motion behavior exists.

## Performance

- Home is usable on low-memory target devices.
- Expensive visual effects reduce gracefully.
- No runaway memory growth from wallpaper/widgets.
- No unnecessary long-running background processing.

---

# 52. DESIGN ACCEPTANCE GATE

Before calling the design finished, compare the full system as a sequence:

**Lock -> Unlock -> Home -> Search -> Launch -> Back -> Home -> Notifications -> Control Center -> Recents -> Settings -> Personalize -> Home**

The sequence should feel like one continuous environment.

Then check:

### Does it feel premium?
Yes means:
- spacing is deliberate
- typography is controlled
- motion is restrained
- materials are believable

### Does it feel original?
Yes means:
- no copied panel composition
- no copied icon family
- no copied wallpaper style
- no obvious OEM clone cues

### Does it feel useful?
Yes means:
- common actions are fast
- search is excellent
- Home remains calm
- information density can scale with user needs

### Does it feel like a phone system?
Yes means:
- system states exist
- edge cases exist
- permissions are real
- platform limitations are respected
- third-party apps coexist naturally

---

# 53. REQUIRED VISUAL DELIVERABLES

The build/design agent must produce:

1. A complete visual direction board.
2. At least three finished Astra wallpapers using the concepts in this file.
3. Lock-screen designs for each wallpaper family.
4. Home-screen designs for dark and light themes.
5. Search/command palette screens.
6. Notification shade.
7. Control Center.
8. Recents.
9. Settings.
10. Personalization studio.
11. Error/empty/loading/offline states.
12. Accessibility variants.
13. Motion specification.
14. Design token specification.
15. Component inventory.
16. Functional Android implementation.

Do not substitute only static screenshots for the functional implementation.

---

# 54. FINAL BUILD DIRECTIVE

Treat this document as a contract.

Do not invent a different visual direction halfway through.

Do not simplify away the system surfaces because they are inconvenient.

Do not implement fake controls.

Do not ship placeholders as final design.

Do not treat the wallpaper as an afterthought.

Do not build each screen independently.

Instead:

**Build Astra as one visual environment.**

The design language must exist first in the foundations, then flow through every component, every screen, every transition and every state.

The Home screen should look like Astra.
The search screen should look like Astra.
The notification experience should look like Astra.
The controls should look like Astra.
Settings should look like Astra.
The lock screen concept should look like Astra.
The wallpapers should look like Astra.
The motion should look like Astra.

But Astra must still feel like a real Android device, not a fantasy operating system that cannot survive contact with the platform.

---

# 55. IMPLEMENTATION SELF-CHECK BEFORE HANDOFF

The AI build agent must perform a final internal checklist before reporting completion:

- Did I build the actual launcher instead of a mock screen?
- Did I implement the Android Home role correctly?
- Does app discovery work from the installed package set?
- Does app launch work?
- Does search work offline?
- Are Home settings persistent?
- Are wallpaper/theme changes persistent?
- Do widgets use the correct Android host contract?
- Are system-dependent features capability-gated?
- Did I avoid fake toggles?
- Did I avoid hardcoded data where a platform API exists?
- Did I avoid shipping copied Apple/Samsung/Google visuals?
- Are the three wallpaper directions implemented or at minimum represented by production-ready assets/specifications?
- Does dark mode work?
- Does light mode work?
- Does large text work?
- Does reduced motion work?
- Does the launcher remain usable offline?
- Does the launcher degrade gracefully on lower-memory devices?
- Did I test startup after reboot?
- Did I test Home after force-stop/restart?
- Did I test permission denial?
- Did I test missing notification access?
- Did I test widget add/remove?
- Did I test search while the keyboard is open?
- Did I test empty state, error state and recovery?

Only after those checks should the build be considered ready for review.

---

# 56. CURRENT RESEARCH BASIS

This specification is grounded in current official design guidance checked during preparation:

- Samsung One UI Developer Overview: emphasizes task focus, natural interaction, comfortable viewing/interactions and responsive adaptation.
- Apple Human Interface Guidelines: current guidance on materials, layout, motion, accessibility and platform coherence.
- Android Developers / Material 3: current semantic color, dynamic color and adaptive UI guidance.
- Android RoleManager: official Home role capability and role-request flow.
- Android NotificationListenerService: official notification-listening mechanism and user-granted access model.
- Android AppWidgetManager: official widget hosting support.
- Nothing OS materials: evidence of strong system-wide visual identity, wallpaper-aware colors, typography consistency and distinctive interaction design.

The research is used to extract principles. The final Astra visual identity must remain original.

---

# 57. END STATE

The finished Astra experience should make the first five seconds on the phone feel intentional:

**Wake -> see atmosphere -> understand time -> reach what matters -> search anything -> launch instantly -> move through the phone without visual friction.**

That is the product.

Not “Samsung but darker.”
Not “iOS on Android.”
Not “AI launcher with glass cards.”

Astra should feel like a new phone interface that could plausibly ship on hardware.

**END OF MASTER BUILD SPECIFICATION**
