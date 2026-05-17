# Overview

This is the Meshmore SNS UX

## Design Intent

We want to build a Meshcore client that has a chat interface, and has a dashboard view as the default home screen to summarize the network status, active nodes, etc., for hyperlocal discovery.  Design theme should be inspired by Wipeout 2097 and Evangolion.

## References

- Wipeout_2097 — https://en.wikipedia.org/wiki/Wipeout_2097
- Neon Genesis Evangelion — https://en.wikipedia.org/wiki/Neon_Genesis_Evangelion
- TDR · Wipeout — https://www.thedesignersrepublic.com/project/wipeout
- "How a PlayStation game inspired a generation of graphic designers"
  — https://www.creativebloq.com/news/wipeout-design-inspiration
- WipEout logo history —
  https://imjustcreative.com/wipeout-logo-history/2023/01/18
- Evangelion typography (Fonts In Use) —
  https://fontsinuse.com/uses/28760/neon-genesis-evangelion
- NERV-UI / MAGI convention — https://github.com/lotap/magi-theme
- Bitmap/terminal fonts — https://korigamik.dev/blog/bitmap_fonts/

The sourced design-research synthesis (TDR, Evangelion, cyberpunk
terminal lineage, the Eurostile shared-DNA insight) and the
OSS/Flutter typography plan live in `meshmore-sns-UX-brief.md`.

## Proposals

### Information architecture

Five primary views, navigated via horizontal swipe (R11). Each is a top-level
page in a `PageView`; secondary screens (pair device, edit channel, etc.) are
pushed via `go_router`.

| # | View | Purpose | Maps to spec |
|---|------|---------|---|
| 1 | **Dashboard** (default home) | Network status, RSSI/SNR gauges, peer count, link state, recent events, active-channel summary. Also hosts the small collapsible chat overlay. | R6, R8 |
| 2 | **Chat** | Full chat for the active channel, with channel-selector chip strip on top. Per-channel TTS toggle in the header. | R6, R5 |
| 3 | **Nodes** | Discovered peers with signal strength, distance estimate, last-heard, and a hook for the future live map (F1). | R6 |
| 4 | **Settings** (hub) | Entry point to the three sub-screens below. | R3,R4,R5,R7,R12,R13,R14 |
| 5 | **About / Terms** | App info + legal. | R9, R10 |

The **Settings** view is a hub that pushes three concept-agnostic
sub-screens via `go_router` (they re-skin to the active theme; not
swipe pages):

| Sub-screen | Purpose | Maps to spec |
|---|---|---|
| **Device configuration** | Meshcore radio (freq/BW/SF/CR/TX power), identity/advert (name, lat/lon, self-advert), channels + PSK / QR import, other params + tuning, device info/battery/time. Drives the M4 protocol surface. | R7 |
| **App settings** | BLE auto-reconnect (M7 backoff), language EN/JA, TTS global (off by default), notification/critical parity, diagnostics export (M6), About/Terms links. | R3,R4,R5 |
| **Profile & personalization** | Theme preset (the six concepts incl. D high-contrast & F night) + custom palette, font family & **size scale**, density, **audio alerts master + per-event**, haptics + pattern preview, **accessibility switches incl. one-tap "visual + haptic only"**, identity/advert name. Persisted via `shared_preferences`. | R12,R13,R14 |

### Navigation gestures (R11)

- Horizontal swipe between the five primary views.
- **Long-press on the leading app-bar icon** opens a bottom-sheet quick-nav
  with the five destinations. (We are deliberately *not* using a hamburger
  drawer — the long-press interaction matches the spec better and frees the
  top-left area for status indicators.)
- App-bar trailing area shows live status pills: BLE link state, channel,
  TTS on/off.

### Text-to-speech UX (R5)

- TTS is **off by default** (per spec).
- Two scopes:
  - **Global toggle** in Settings (parent switch).
  - **Per-channel toggle** in the Chat header (only enabled when global is on).
- Voice picker (lists native TTS voices via `flutter_tts`); language follows
  the locale unless overridden.
- Critical system messages (e.g. lost BLE link, low battery) get an audible
  alert prefix when TTS is on.

### Audible theme pack (R12)

- Each design concept ships a **matching audible theme pack** — a
  curated set of short UI/event sounds (message in/out, node
  discovered, link up/down, send, critical/alert) plus a TTS voice
  character that matches the visual identity. Six candidate packs
  ("Mission Control", "Velocity", "Sonar", "Tribunal", "Pure Phase",
  "Codec" — one per concept A–F) are previewable by live synthesis in
  `meshmore-sns-UX-brief.html`; the chosen one is specified here once
  the concept is picked.
- The pack is a first-class deliverable, not an afterthought, and is
  **mutable independently of TTS**. A **"visual + haptic only"**
  mode is a shipped setting (default audio = on at low volume;
  honours OS silent / reduce-sound).
- Hard rule: **no sound carries information alone** — every cue has
  the visual + (optional) haptic parity defined below. Implemented as
  a single cue service: `event → {sound, visual, haptic}` so parity
  cannot drift.

### Localization (R4)

- ARB-based localization (`flutter_localizations` + `intl`), one source of
  truth for strings.
- Locales: `en` (default), `ja`.
- Settings exposes an explicit language override; otherwise follow the
  device locale.

### Personalization & theme system (R14)

The chosen design concept is the **default theme**, not a one-time
lock-in. The Profile & personalization sub-screen lets the user
customise appearance and behaviour; settings persist via
`shared_preferences`.

- **Theme presets:** the six concepts (A NERV, B AG-HUD, C
  Hyperlocal, D SEELE High-Contrast, E DR Pop, F Recon Night) ship
  as selectable `ThemeData`/token sets. **Custom** preset = a palette
  editor over the token names (`mm.base/surface/accent/alert/…`).
- **Typography:** font family from the OSS set; **font-size scale
  (S–XL)** layered on top of OS `textScaleFactor`; comfortable /
  compact density.
- **Audio (R12):** master toggle + per-event toggles
  (message/node/link/critical) + volume + pack selector.
- **Accessibility (R13):** high-contrast, reduce-motion,
  one-tap "visual + haptic only", captions, large targets, follow-OS
  bold-text — all live here (single source of truth).
- Implementation: a `ThemeController` (Provider) exposing the active
  token set + text-scale + flags; the `CueService` reads the audio/
  haptic prefs so parity can't drift. D and F are literally the
  high-contrast and low-power/night presets, so accessibility and
  field modes are *themes*, not separate code paths.

### Accessibility & sensory parity (hearing-impaired-first) — design guideline

This is a binding design guideline for **every** screen and the audio
pack (R13). The audible theme pack (R12) must never imply
sound-dependence.

- **Triple-channel parity.** Every notable event is expressed on up
  to three channels — **visual** (always), **audible** (R12,
  mutable), **haptic** (configurable, independent). Visual is the
  baseline; audio and haptic are augmentations. A deaf user must lose
  *zero* information with sound fully off.
- **No sole carriers.** Information is never conveyed by sound alone
  *or* colour alone. Critical/alert states use **icon + text +
  motion** together; status uses shape/label, not just hue.
- **Visible alert region.** A persistent status/alert area surfaces
  link-lost / low-battery / new-message visually (badge + text +
  brief motion), mirroring whatever the audio pack would play.
- **TTS** (R5) is off by default and is an augmentation only; any
  spoken content has its on-screen text/transcript.
- **Respect the platform.** Honour `MediaQuery` `boldText`,
  `textScaleFactor` (to large), high-contrast, and
  **`disableAnimations`/reduce-motion** (scanline, gauge sweeps,
  swipe-sweep all degrade gracefully); honour OS silent / reduce
  sound for the audio pack.
- **Contrast & targets.** AA+ (target ≥7:1 for primary text on the
  dark grounds); interactive targets ≥ 48 dp; one-handed reach;
  usable with gloves.
- **Semantics.** All actionable widgets carry `Semantics` labels;
  swipe navigation (R11) has an accessible equivalent (the long-press
  quick-nav) so it is not gesture-only.
- **Settings.** Independent toggles for audio pack, haptics, TTS,
  reduce-motion override, and high-contrast; a one-switch
  "visual + haptic only" mode.

## Design

### Chosen direction: **D "SEELE Monolith" — default; all six as presets**

**Decision (locked).** Concept **D "SEELE Monolith"** is the
**default theme**: Evangelion *"SOUND ONLY"* brutalism — cream on
true black, one dominant numeral per screen, ruthless hierarchy,
near-zero chrome, status as a full-width slab that colour-inverts to
red on alert. D is *also* the high-contrast / sunlight accessibility
benchmark, so the default is the most legible option.

All **six concepts ship as selectable theme presets** via the R14
theme system (see *Personalization & theme system*): a single
`ThemeController` swaps a token set + type + motion flags at runtime;
D is just the default token set. The other presets — A NERV, B
AG-HUD, C Hyperlocal, E DR Pop, F Recon Night — are defined token
sets, not separate code paths. (Per-preset palettes are catalogued in
`meshmore-sns-UX-brief.md`; screens are built against the *token
names* below, never hard-coded colours, so every preset works.)

#### Palette — token contract (preset-independent)

Screens reference only these semantic tokens; each preset supplies
values. **Default = D "SEELE Monolith":**

| Token | D default | Use |
|---|---|---|
| `mm.base` | `#000000` | App background (true black) |
| `mm.surface` | `#0E0E0C` | Cards, panels |
| `mm.surfaceAlt` | `#15140F` | Elevated panels, inputs |
| `mm.line` | `#2A2A26` | Hairlines, dividers |
| `mm.fg` | `#EDE6D6` | Primary text (cream) |
| `mm.fgMuted` | `#9A958A` | Secondary text (bone) |
| `mm.accent` | `#EDE6D6` | Primary accent (D = cream; e.g. C → `#35E0F0`) |
| `mm.alert` | `#C8102E` | Critical slab / warnings (SEELE red) |
| `mm.ok` | `#9A958A` | Healthy/neutral (D keeps it austere) |

Every preset defines the same nine tokens (A–F values in the brief).
A light theme is out of scope; presets are dark (D/F darkest).

#### Typography (OSS-only; reference faces are inspiration, not bundled)

The project is MIT-class licensed → **Eurostile/Microgramma,
Matisse EB, Helvetica, Futura are inspiration only, never bundled.**
Substitutes are OFL/MIT and Japanese-capable (R4). Full rationale,
licensing table, per-concept assignment and the Flutter bitmap
constraint are in `meshmore-sns-UX-brief.md`.

- **Display / titles:** **Saira / Saira Condensed** (OFL) — the
  free Eurostile-class squared face; condensed weights deliver the
  Evangelion "mechanical compression". (Alt: Chakra Petch / Michroma
  / Orbitron for the Wipeout team-logo geometry.)
- **Telemetry / console:** **Departure Mono** (OFL 1.1) — modern
  pixel/bitmap mono for the NERV/cyberpunk console layer; lock sizes
  to its 11px grid for crispness. (Alt: Terminus TTF / Cozette.)
- **Japanese + mono:** **M PLUS 1 Code** (OFL) — the linchpin:
  monospace techno *with full Japanese*, covering the console layer,
  R4 localization, and the Wipeout "kanji-as-graphic" motif in one
  license-clean family.
- **Body / fallback:** IBM Plex Sans/Mono (OFL) or system.
- Numerals on telemetry use an outlined **slashed zero** (the
  Evangelion "SOUND ONLY" monolith motif).
- Vendor TTFs under `assets/fonts/` (deterministic/offline) or via
  `google_fonts`.

#### Components

- **Chamfered containers** — panels use a 12 px corner-cut clip-path instead
  of rounded corners (Wipeout panels).
- **Warning chevron headers** — section headers on critical panels use a
  diagonal stripe band (NERV-style "DANGER" header).
- **HUD gauges** — radial gauges for RSSI/SNR, painted in `mm.hud`.
- **Scanline overlay** — dashboard has a very subtle animated scanline
  (1 frame per ~80 ms; opacity ~0.04) for the CRT vibe.
- **Status pills** — `[ BLE • LINK ]`, `[ CH 0 • PUBLIC ]`, `[ TTS • OFF ]`
  in the app-bar trailing area.

#### Motion

- Page transitions: 250 ms ease-out, with a faint colored sweep matching
  the destination view's accent.
- "Acquired/lost" events animate the relevant gauge with a flash.

### UI implementation plan (U-series, app-side)

Mirrors the protocol M-plan discipline (small, analyzed, tested,
committed to the `meshmore-sns` submodule branch).

| ID | Deliverable | Status |
|----|-------------|--------|
| U0 | Theme foundation: `MmTokens` + all 6 presets (D default), `ThemeController` (Provider, persisted via `shared_preferences`), wired into `MaterialApp`; a Personalization screen with the live preset/font/accessibility picker | ✅ done (submodule `cbb80b9`; 24 app tests) |
| U1 | Nav shell: 5-view `PageView` swipe + long-press quick-nav (R11); Settings hub → 3 sub-screen routes (go_router) | ✅ done (submodule `1ef39df`; 27 app tests) |
| UD | **Diagnostics & connect** screen (parallel, unblocks hardware): real Connect/disconnect, live state, raw frame log, test sends, in-app M6 channel-tail oracle + fixture export | ✅ done (submodule `pending`; 30 app tests) |
| U2 | Dashboard (D "monolith" layout) wired to `MeshcoreController` (link/peers/channel/events) | ⏭ next |
| U3 | Chat (collapsible, channel switch, R6) + TTS toggle wiring (R5) | ☐ |
| U4 | Nodes screen; Device configuration screen → M4/R7 commands | ☐ |
| U5 | App settings + About/Terms (R9/R10); l10n EN/JA (R4) | ☐ |
| U6 | `CueService` (event→sound+visual+haptic parity, R12/R13); audio packs | ☐ |
| U7 | Polish: motion + reduce-motion, conformance/widget tests, a11y audit | ☐ |

### Open questions for design review

1. Do we vendor Rajdhani/Eurostile locally or fall back to a permissive
   open-source equivalent (e.g. **Saira Condensed**)?
2. Should the Chat overlay on the Dashboard be a collapsing bottom sheet
   (Material) or a fixed-height bottom panel that the user resizes?
3. Live map (F1) — out of scope for the first build, but reserve a tab slot
   in the Nodes view header?
