# Overview

This is the Meshmore SNS UX

## Design Intent

We want to build a Meshcore client that has a chat interface, and has a dashboard view as the default home screen to summarize the network status, active nodes, etc., for hyperlocal discovery.  Design theme should be inspired by Wipeout 2097 and Evangolion.

## References

- https://en.wikipedia.org/wiki/Wipeout_2097
- https://en.wikipedia.org/wiki/Neon_Genesis_Evangelion

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
| 4 | **Settings** | BLE pairing, channel and radio configuration, TTS global toggle + voice picker, language toggle (EN/JA), theme variant. | R3, R4, R5, R7 |
| 5 | **About / Terms** | App info + legal. | R9, R10 |

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

### Localization (R4)

- ARB-based localization (`flutter_localizations` + `intl`), one source of
  truth for strings.
- Locales: `en` (default), `ja`.
- Settings exposes an explicit language override; otherwise follow the
  device locale.

### Accessibility

- Theme honors `MediaQuery.boldText`, text scale factor, and high-contrast.
- All actionable widgets have `Semantics` labels.
- TTS feature doubles as an accessibility affordance.

## Design

### Visual language: Wipeout 2097 × NERV terminal

The mood is *retro-future racing HUD* (Wipeout 2097 — angular shapes,
team-livery accents, dense telemetry) layered with *NERV operations console*
(Evangelion — flat warning chevrons, monospaced annotations, alert-orange
slabs over near-black panels).

#### Palette

| Token | Hex | Use |
|---|---|---|
| `mm.base` | `#0A0E1A` | App background |
| `mm.surface` | `#121826` | Cards, panels |
| `mm.surfaceAlt` | `#1B2436` | Elevated panels, app-bar |
| `mm.alert` | `#E6005C` | Critical / warnings, alert chevrons |
| `mm.eva` | `#FF7A00` | Primary accent ("Eva orange") |
| `mm.hud` | `#00E5FF` | Live data, gauges, links |
| `mm.term` | `#9CFF00` | Terminal text, healthy status |
| `mm.fg` | `#E6ECF5` | Primary text |
| `mm.fgMuted` | `#7C8AA3` | Secondary text |

A light theme will be derived later; dark is the default.

#### Typography

- **Display / titles:** an angular condensed face such as **Rajdhani** or
  **Eurostile** (we'll vendor as a local font asset).
- **Body:** the same display face at regular weight, or system sans as a
  fallback.
- **Telemetry / numerics:** **JetBrains Mono** for any monospaced readouts
  (`RSSI: −87 dBm  SNR: 7.2`).

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

### Open questions for design review

1. Do we vendor Rajdhani/Eurostile locally or fall back to a permissive
   open-source equivalent (e.g. **Saira Condensed**)?
2. Should the Chat overlay on the Dashboard be a collapsing bottom sheet
   (Material) or a fixed-height bottom panel that the user resizes?
3. Live map (F1) — out of scope for the first build, but reserve a tab slot
   in the Nodes view header?
