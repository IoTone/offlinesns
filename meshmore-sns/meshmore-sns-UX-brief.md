# Meshmore SNS — UX Design Brief (6 concepts for review)

Status: **for review**. Pick one (or a blend / specific elements); the
choice is then folded into `meshmore-sns-UX-spec.md` as the single
direction and built once in Flutter.

> **Open `meshmore-sns-UX-brief.html` in a browser** to *see* the phone
> mockups and *hear* each audible theme pack (synthesised live via Web
> Audio — no asset pipeline needed to evaluate the sonic identity).
> This Markdown is the canonical text-of-record; the HTML is the
> visual/audio companion. Each concept ships a **matching audible theme
> pack**, and every sound has a visual + haptic equivalent
> (hearing-impaired-first — see *Accessibility & sensory parity*).

## Shared constraints (all concepts honour these)

- Design Intent (from UX spec): **Wipeout 2097 × Neon Genesis
  Evangelion**, dark, hyperlocal-discovery focused.
- Requirements: R6 collapsible chat on active channel + channel
  switch; R8 dashboard is the home; R11 horizontal swipe between
  views + long-press the app-bar icon for quick-nav; R4 EN/JA; R5
  TTS toggle (off by default); R9/R10 About/Terms.
- 5 primary views (swipe-paged): **Dashboard · Chat · Nodes ·
  Settings · About/Terms**.
- Field reality: outdoor/sunlight, possibly gloves, one-handed,
  intermittent BLE link — legibility and glanceability matter.
- Each concept ships a **matching audible theme pack** (R12) —
  treated as first-class design effort, not an afterthought.

### Accessibility & sensory parity (hearing-impaired-first)

Non-negotiable for every concept (R13; do not let the audio packs
imply sound-dependence):

- **No information by sound alone.** Every audible cue has a
  synchronised, equivalent **visual** cue and an optional **haptic**
  pattern. The audio pack is an *augmentation channel*, never the
  only one. (WCAG-aligned; cf. 1.4.2 / 1.2.)
- TTS (R5) stays **off by default** and is likewise only an
  augmentation; spoken content always has its visible text/transcript.
- A persistent visual status/alert region; critical events are
  conveyed by **icon + text + motion**, never colour alone.
- **Haptics as an independent third channel** (configurable) for
  users who rely on vibration.
- Respect `MediaQuery`: `boldText`, large `textScaleFactor`,
  high-contrast, **reduce-motion** (scanline/sweeps must be
  disable-able) and the OS silent/"reduce sound" setting.
- The audio pack is fully mutable independently of TTS; a
  **"visual + haptic only" mode** is a shipped setting.
- Contrast target **AA+ (≥7:1 primary text)** on the dark grounds;
  touch targets ≥ 48 dp.

Concepts differ only on: visual language, information density,
dashboard metaphor, chat treatment, and *audio character* — never on
the parity guarantees above.

---

## Design research & references (sourced)

### Wipeout 2097 — The Designers Republic (TDR)

- **Eurostile / Microgramma** is the spine. Sci-fi-futurism canon
  since the late 60s; TDR built the Wipeout logo from Eurostile's
  *8* glyph. Geometric, squared, "plausible future" machine type.
- **Total fictional corporate identity**: every race team (FEISAR,
  AG-Systems, Auricom, Qirex, Pir-hana, Harimau…) has its own logo,
  livery and brand language extended to trackside ads/billboards —
  "coherent but distinctly different; an entirely plausible
  fictional future." Marks carry hidden concepts (Harimau = abstract
  tiger's nose).
- **Rave-era maximalism / anti-design**: dense, layered, hi-vis on
  neutral, angular (TDR's own "F500 Ang-ular"); **blurred kanji as a
  graphic speed signal** — an 80s Japan-techno callback.
- *Take for us:* a per-channel "livery" accent system; Eurostile-class
  squared display; Japanese type used graphically, not just for L10n.

### Neon Genesis Evangelion — NERV / MAGI

- **Matisse EB** (JP serif, extreme stroke contrast, mechanically
  compressed) = identity face on title cards & NERV HUD;
  **all-caps (compressed) Helvetica** on HUD panels & warning
  screens; **Eurostile (Extended)** on NERV control panels;
  **Futura** on Tokyo-3 / NERV signage; outlined **Chicago** numerals
  (slashed zero) on the "SOUND ONLY" monoliths.
- Motifs: mechanical compression = haste/despair; ALL-CAPS;
  monochrome title cards; interlocking compressed layouts; flash
  frames; **countdown/telemetry panels**; modern NERV-UI convention —
  emergency = all green/cyan → **red**, scanline speeds up, MAGI
  "conflict" state.
- *Take for us:* the **emergency colour-inversion + scanline** as the
  critical-alert visual (a perfect non-audible parity for R12/R13);
  outlined slashed-zero numerals for telemetry.

### Cyberpunk terminal lineage

- Blade Runner / Ghost in the Shell / TDR share a language: CRT
  scanlines & phosphor, **monospace/bitmap console type**, neon on
  near-black, Japanese signage, data density, glitch. Evangelion's
  NERV UI *is* this lineage — which is why **bitmap terminal fonts**
  carry the authentic console layer (next section).

> **Unifying insight:** Eurostile is the shared DNA of *both*
> references (Wipeout logo ⋂ NERV panels). So the type system is:
> a Eurostile-class **squared display** + a **bitmap/terminal mono**
> for telemetry + **Japanese-capable** type as a first-class graphic
> element. All three must be OSS/Flutter-bundleable (next section).

Sources: TDR Wipeout
<https://www.thedesignersrepublic.com/project/wipeout> ·
Creative Bloq <https://www.creativebloq.com/news/wipeout-design-inspiration> ·
WipEout logo history
<https://imjustcreative.com/wipeout-logo-history/2023/01/18> ·
Evangelion typography (Fonts In Use)
<https://fontsinuse.com/uses/28760/neon-genesis-evangelion> ·
NERV-UI convention <https://github.com/lotap/magi-theme> ·
Bitmap fonts <https://korigamik.dev/blog/bitmap_fonts/>

---

## Concept A — "NERV Terminal"

**One-liner:** an Evangelion operations console — information-maximal,
authoritative, "you are monitoring a mesh."

- **Mood / refs:** NERV command bridge; warning chevrons; monospaced
  readouts; CRT scanline.
- **Palette:** base `#0A0E1A`, surface `#121826`, alert `#E6005C`,
  Eva-orange `#FF7A00` (primary accent), HUD-cyan `#00E5FF` (live
  data), term-green `#9CFF00` (healthy). High contrast.
- **Type:** condensed display (Rajdhani/Eurostile) for headers;
  JetBrains Mono for all telemetry/numerics.
- **Components:** chamfered (12px corner-cut) panels; diagonal
  warning-stripe section headers; dense annotated readouts
  (`RSSI −87dBm  SNR 7.2  HOP 3`); subtle animated scanline; status
  pills in the app-bar.
- **Dashboard:** telemetry grid — everything visible at once.
- **Chat:** a terminal-style log; collapsible bottom band.
- **Audible theme pack — "Mission Control":** austere and synthetic.
  Square/pulse-wave blips; terse 2-tone message-in; Geiger-ish ticks
  for node discovery; a descending sawtooth klaxon for critical /
  link-lost. TTS voice: flat, clipped, "operator". *Parity:* each
  maps to a warning-stripe flash + a distinct haptic pattern.

```
┌─[ MESHMORE // NERV ]──────[BLE●][CH0][TTS○]┐
│▟▖ DASHBOARD                                │
│ ┌── LINK ───────────┐ ┌── RADIO ────────┐ │
│ │ BLE  ● LINKED      │ │ 915.0MHz SF7 CR5│ │
│ │ NODE T1000-E      │ │ TX 22dBm        │ │
│ └───────────────────┘ └─────────────────┘ │
│ ┌── MESH ◣◢◣◢◣◢◣◢◣◢◣◢◣◢◣◢◣◢◣◢◣◢◣◢◣◢◣◢ │
│ │ PEERS 7   CH0 PUBLIC   MSG/min 4       │ │
│ │ RSSI ▁▃▅▇▅▃  SNR ▂▄▆█▆▄  BATT 4.10V    │ │
│ └────────────────────────────────────────┘ │
│ ┌── EVENTS ─────────────────────────────┐  │
│ │ 12:04 advert  Kanako.1                  │ │
│ │ 12:03 ch0  "weather fukuoka 28°"        │ │
│ └────────────────────────────────────────┘ │
│ ▾ CHAT [CH0]  ───────────────────────────  │
│ Davi1> on my way                            │
│ [ type… ]                            [SEND] │
└──── ‹ swipe › NODES · SETTINGS · ABOUT ────┘
```

---

## Concept B — "AG Systems HUD"

**One-liner:** a Wipeout racing HUD — speed/energy aesthetic, bold,
glanceable, fewer things louder.

- **Mood / refs:** anti-grav cockpit HUD; team livery; momentum;
  big gauges; minimal chrome.
- **Palette:** base `#05060B`, surface `#10131F`, livery accents
  cyan `#22D3EE` + magenta `#FF2D78` (alternate per active channel
  like team colours), warning `#FFB020`. Lower text density, larger
  type.
- **Type:** big angular display face for headline metrics; sans for
  body; mono only for raw values.
- **Components:** large radial gauges with sweep animation; rounded
  chamfer; one hero metric per card; motion-forward transitions
  (colored sweep on swipe).
- **Dashboard:** 2–3 hero gauges (link quality, peers, channel
  activity); detail on tap.
- **Chat:** a sleek slide-up panel with a channel chip-strip.
- **Audible theme pack — "Velocity":** arcade / electronic. Bright
  triangle-wave arpeggios; rising whoosh on R11 navigation; a
  major-third confirm on send; energised alert sweep. TTS voice:
  upbeat, mid-tempo. *Parity:* the swipe-sweep is also the visual
  transition; alerts flash the warn-amber band + haptic burst.

```
┌ MESHMORE ───────────────────  ☰(hold)  ┐
│                                          │
│        ╭───────────╮     LINK            │
│        │   ▟▙ 87%  │     STRONG          │
│        │  ◜SIGNAL◝ │     915.0 MHz       │
│        ╰───────────╯                     │
│   ┌─────────┐   ┌─────────┐              │
│   │ PEERS   │   │ CH0     │              │
│   │   7     │   │ ▮▮▮▯▯ 4 │              │
│   └─────────┘   └─────────┘              │
│                                          │
│   ◀ ( • ) ▶   Dashboard                  │
│  ╭──────────────────────────────────╮    │
│  │ ⌃ CHAT  [CH0▾]  Public           │    │
│  │ Kanako.1  weather fukuoka 28°    │    │
│  │ [ message…                ]  ▶   │    │
│  ╰──────────────────────────────────╯    │
└──────────────────────────────────────────┘
```

---

## Concept C — "Hyperlocal Field" (recommended starting point)

**One-liner:** discovery-first for off-grid hyperlocal comms — a
proximity/radar dashboard, lower visual noise, chat as a first-class
sheet. Eva×Wipeout accents kept, dialed for field legibility &
accessibility.

- **Mood / refs:** tactical radar / constellation; the Eva-orange &
  HUD-cyan from the spec but on a calmer ground; honours
  `MediaQuery.boldText` / high-contrast / text scale.
- **Palette:** base `#0B0F17`, surface `#161B26`, accent Eva-orange
  `#FF7A00`, live cyan `#35E0F0`, ok-green `#7CFF6B`, alert
  `#FF3B6B`. Larger min tap targets; AA+ contrast.
- **Type:** Saira Condensed (open-source Eurostile-ish) headers;
  system sans body; mono for raw telemetry only.
- **Components:** central **node radar** (range rings, peers as
  blips by RSSI distance, tap a blip → node/DM); compact status
  rail; chat as a Material draggable sheet (peek → half → full,
  satisfies R6 collapsible).
- **Dashboard:** radar is the hero (maps to spec's hyperlocal
  discovery + the F1 live-map future); status rail underneath.
- **Audible theme pack — "Sonar":** calm / organic, field-tuned.
  A soft sonar ping for node discovery (sonically mirrors the radar);
  gentle two-note message; low calm tones for link state; sparse so
  it doesn't fatigue outdoors and stays distinct under ambient noise.
  TTS voice: calm, natural. *Parity:* the ping is also a radar-blip
  pulse; link tones map to the status-rail colour + a soft haptic.
- **Chat:** persistent draggable sheet; channel chips; TTS toggle in
  its header.

```
┌ MESHMORE  ◦Public      [BLE●] [TTS○] (hold≡)┐
│            · · NODE RADAR · ·                │
│            ╭───────────────╮                 │
│         ╭──┤   .  Kanako.1 ├──╮              │
│        │   │ .  ((•))  .    │   │  3km       │
│        │   │   Davi1  .  .  │   │  1km       │
│         ╰──┤      .        ├──╯              │
│            ╰───────────────╯                 │
│  PEERS 7  ·  CH0 ▮▮▮▯▯  ·  BATT 4.10V  ·  SNR│
│ ───────────────────────────────────────────  │
│ ⌃⌃ Chat · Public                      [CH▾]  │
│ Kanako.1  weather fukuoka 28°  12:03         │
│ Davi1     on my way            12:04         │
│ [ message…                          ] [▶]    │
└─ ‹swipe›  Dashboard ▸ Nodes ▸ Settings ▸… ──┘
```

---

## Concept D — "SEELE Monolith"

**One-liner:** the Evangelion *"SOUND ONLY"* tribunal — brutalist,
monumental, ruthless hierarchy. The accessibility/sunlight benchmark.

- **Mood / refs:** SEELE monoliths; cream-on-black; one huge numeral;
  near-zero chrome; severe.
- **Palette:** ink `#000000`, cream `#EDE6D6`, SEELE-red `#C8102E`
  (alert only), bone `#9A958A`, line `#2A2A26`.
- **Type:** giant Saira numerals; M PLUS 1 Code for the sparse text;
  slashed-zero. Type *is* the UI.
- **Dashboard:** a single dominant metric; status as a full-width
  slab that colour-inverts to red on alert.
- **Chat:** full-bleed monospace, almost no decoration.

```
┌ MESHMORE              SOUND ONLY ┐
│ PEERS                            │
│  07                              │
│                                  │
│ CH0 · PUBLIC · LINKED            │
│ 915.0 SF7                        │
│                                  │
│▌ NORMAL · NO ALERTS              │ ← slab (→red on alert)
│ davi1: on my way                 │
│ [ type…                        ] │
└──────────────────────────────────┘
```

- **Audible theme pack — "Tribunal":** severe and sparse — deep
  resonant sub-bass strikes, a single low choral swell for critical,
  near-silence otherwise. TTS: slow, grave, "committee". *Parity:*
  the status slab (→ red) is the visual cue + one firm haptic; the
  tone is never required.

---

## Concept E — "DR Pop"

**One-liner:** the Designers Republic *maximalist / Y2K-rave* half —
flat colour blocks, fake per-channel sub-brands, kanji as graphics.

- **Mood / refs:** TDR Wipeout liveries & fictional corporate brands;
  hi-sat colour blocking; coded labels (`PHASE_02`); katakana.
- **Palette:** ink `#101014`, magenta `#FF2E88`, acid `#D7FF00`,
  cyan `#00C2FF`, paper `#F2F0E6`.
- **Type:** Michroma/Orbitron display; M PLUS 1 Code for kanji/
  katakana run as graphic elements; bold numerals.
- **Dashboard:** colour-block tiles; each channel carries a generated
  "sub-brand" (the Wipeout team-livery system applied to channels).
- **Chat:** speech blocks in brand colours; katakana accents.

```
┌ MESHMORE™            PHASE_02 ┐  (magenta bar)
│██ PEERS 07 ██ │ ░░ CH0 ▮▮▮▯ ░░│  (acid / cyan blocks)
│ [AURICOM·NET]   通信 915.0     │
│ ▣ Kanako.1  weather 28°        │
│ ▣ advert · davi1               │
│ CH0 ▾ PUBLIC                   │
│ [ message…              ] [▶]  │
└────────────────────────────────┘
```

- **Audible theme pack — "Pure Phase":** bright syncopated arcade
  blips, funky confirms, Wipeout-soundtrack energy. TTS: punchy,
  stylised. *Parity:* each event flips its sub-brand block + a
  rhythmic haptic; loudness never required. (Contrast risk is real —
  the parity rules keep colour from being a sole carrier.)

---

## Concept F — "Tactical Recon"

**One-liner:** night-ops / codec field mode — OLED-black, amber
phosphor, map-first, power-frugal, dark-adapted.

- **Mood / refs:** recon codec / NV optics; single-hue phosphor;
  glare- and battery-aware; covert.
- **Palette:** oled `#000000`, amber `#FFB000`, dim `#6E4E00`,
  alert `#B3231F`, panel `#141414`. (Alt phosphor-green variant.)
- **Type:** M PLUS 1 Code / Departure Mono throughout; minimal
  display type. Single hue, high glare resistance.
- **Dashboard:** map/bearing-first — waypoints with range + heading,
  sweep line; tiny status line.
- **Chat:** codec/recon log, terse amber mono.

```
┌ MESHMORE · RECON      BLE TTS○ ┐
│  ┌───────── map ──────────┐    │
│  │   ▲Kanako.1 2.1km 041°  │   │
│  │        ◬   ╲            │   │
│  │     ▲Davi1 0.8km 190°   │   │
│  └────────────────────────┘    │
│ PEERS 7 · CH0 · BATT 4.10V     │
│ [KANAKO.1] weather 28°         │
│ [ msg…                       ] │
└────────────────────────────────┘
```

- **Audible theme pack — "Codec":** comms-radio — terse UHF beeps,
  a brief squelch on link change, the codec-call ring for critical;
  low volume, cuts through ambient noise. TTS: clipped radio
  operator. *Parity:* amber blink on the map/log + terse haptic;
  designed to run **fully silent** (field stealth) without losing
  information.

---

## Configuration & profile screens (shared across all concepts)

These three are **concept-agnostic utility screens** — form/list
driven, so they re-skin to whatever concept theme is active rather
than being redesigned per concept (one set, not 18). They sit under
the **Settings** primary view (pushed via `go_router`), *not* as new
swipe pages. The HTML shows them in the C "Hyperlocal" theme for
reference.

### 1. Device configuration (Meshcore radio/device)

Drives the real M4 / R7 protocol surface already implemented in
`packages/meshcore`:

- **Radio:** frequency (MHz), bandwidth (kHz), spreading factor
  (5–12), coding rate (5–8), TX power (dBm) — `setRadioParams` /
  `setRadioTxPower`.
- **Identity / advert:** node name (`setAdvertName`), advert lat/lon
  (`setAdvertLatLon`), self-advert flood/zero-hop (`sendSelfAdvert`).
- **Channels:** list / add / edit (name + 16-byte PSK), QR import
  (`meshcore://channel/add`), the known **Public** preset.
- **Other params / tuning:** manual-add-contacts, telemetry mode,
  location policy, multi-acks (`setOtherParams`), rx-delay/airtime
  (`setTuningParams`).
- **Device:** firmware/build/model, battery & storage, device-time
  sync (DEVICE_INFO / BATTERY / get·setDeviceTime).

### 2. App settings (general)

- **Connection:** auto-reconnect on/off (the M7 backoff policy),
  forget device.
- **Language (R4):** System / English / 日本語.
- **Speech (R5):** TTS off by default; voice + rate; augmentation
  only.
- **Notifications:** critical alert → system notification + vibrate
  (the lock-screen parity from the haptics discussion).
- **Data / about:** export diagnostics fixture (the M6
  `exportGrpTxtFixture`), logs, About (R9), Terms (R10).

### 3. Profile & personalization (R14 — the customization hub)

This is the control centre that makes all the concept work
user-selectable:

- **Theme preset:** the six concepts as selectable themes — NERV /
  AG-HUD / Hyperlocal / **SEELE High-Contrast** / DR Pop / **Recon
  Night** — plus **Custom…** (a palette editor: base / surface /
  accent / alert swatches).
- **Type & density:** font family (the OSS set), **font-size scale
  S–XL** (honours OS `textScaleFactor`), comfortable/compact.
- **Audio alerts (R12):** master on/off, **per-event toggles**
  (message / node / link / critical), volume, pack selector.
- **Haptics:** on/off, intensity, “learn the patterns” preview.
- **Accessibility (R13):** high-contrast, reduce-motion, **one-tap
  “visual + haptic only” mode**, captions/transcripts, large touch
  targets, follow-OS bold-text.
- **Identity:** display/advert name, short Ed25519 public-key,
  accent colour. Persisted via `shared_preferences`.

> Design consequence: concepts A–F stop being a one-time pick — the
> chosen one is the **default theme**, the others (notably D as
> high-contrast and F as night/low-power) ship as **selectable
> theme presets** in this screen. That is the unifying answer to
> "make the theme customizable".

---

## Typography — OSS & Flutter-ready (license-clean)

The project ships under a friendly academic (MIT-class) licence, so
the *reference* faces can inspire but **cannot be bundled**:
Eurostile/Microgramma (Linotype), Matisse EB (Fontworks), Helvetica,
Futura — **inspiration only**. We substitute with OFL/MIT families
that hold the same character and, critically, cover **Japanese (R4)**.

| Role | Bundled face | Licence | Why |
|---|---|---|---|
| Squared display (Eurostile-class) | **Saira** / **Saira Condensed** | OFL | Closest free Eurostile-ish; condensed = the "mechanical compression" Eva motif |
| Alt techno display | **Chakra Petch** / **Michroma** / **Orbitron** | OFL | Wipeout team-logo geometry; Chakra Petch also has a technical/JP-adjacent feel |
| Telemetry / console (bitmap) | **Departure Mono** | OFL 1.1 | Modern pixel-mono; pixel-perfect at 11px steps; the NERV/cyberpunk console layer |
| Alt console mono | **Terminus TTF** / **Cozette** | OFL 1.1 / MIT | Classic bitmap workhorses (from the bitmap-fonts research) |
| Japanese + mono (R4) | **M PLUS 1 Code** | OFL | Linchpin: monospace techno **with full Japanese** — covers NERV console *and* JP L10n *and* the Wipeout "kanji-as-graphic" motif in one license-clean family |
| Body / fallback | **IBM Plex Sans/Mono** or system | OFL | Neutral, JP-capable, ships everywhere |

**Bitmap-in-Flutter reality:** Flutter rasterises TTF/OTF (no native
.bdf/.pcf). Use pixel-*outline* TTFs (Departure Mono, Terminus TTF,
Cozette) and **lock font sizes to the font's native pixel grid**
(Departure Mono → multiples of 11) so glyphs stay crisp; antialiasing
isn't disengageable per-glyph in Flutter, so pick faces that read
well AA'd at integer sizes (these do). Pull via `google_fonts` or
vendor the TTFs in `assets/fonts/` (deterministic, offline — preferred
for an academic OSS app).

**Per-concept assignment:**

- **A · NERV Terminal:** Saira (ALL-CAPS, compressed headers) +
  **Departure Mono** for all telemetry; **M PLUS 1 Code** for JP &
  kanji callouts; outlined **slashed-zero** numerals (the "SOUND
  ONLY" motif).
- **B · AG HUD:** Michroma/Orbitron (race-team geometry) display +
  Saira body + **JetBrains Mono** values; minimal bitmap — polish
  over console.
- **C · Hyperlocal Field:** Saira Condensed (calm Eurostile
  substitute) + **M PLUS 1 Code** for mono *and* Japanese (one
  family → lowest effort, JP built-in); **Departure Mono** only for
  radar range labels.
- **D · SEELE Monolith:** oversized **Saira** numerals + **M PLUS 1
  Code** sparse text; slashed-zero. Type is the entire UI.
- **E · DR Pop:** **Michroma/Orbitron** display + **M PLUS 1 Code**
  for kanji/katakana run as graphics; bold numerals.
- **F · Tactical Recon:** **M PLUS 1 Code / Departure Mono**
  throughout, single hue; minimal display type.

## Comparison

One row per concept (scales cleanly to six):

| Concept | Hero metaphor | Density | Audio pack | Field legibility | Accessibility | Build effort |
|---|---|---|---|---|---|---|
| **A** NERV Terminal | Telemetry grid | Very high | Mission Control | Medium | Lower | Med-High |
| **B** AG HUD | Big gauges | Low | Velocity | High | Medium | Medium |
| **C** Hyperlocal Field | Node radar | Medium | Sonar | High | **Best** | **Lowest** |
| **D** SEELE Monolith | Giant numeral | Minimal | Tribunal | **Highest** | **Highest** | Low |
| **E** DR Pop | Colour-block sub-brands | High | Pure Phase | Medium | Needs care | Medium |
| **F** Tactical Recon | Map / bearings | Low | Codec | **Highest (sun & dark)** | High | Med (map paint) |

**Recommendation:** still **C** as the build base (best off-grid
hyperlocal fit, lowest effort, strong accessibility). Of the new
three: **D** is the accessibility/sunlight benchmark — ship it as the
**high-contrast alternate theme**; **F** is the true field/low-power
mode — ship as a **"Recon" theme variant**; **E** is the
brand-forward show-piece. Visual, audio, and theme variants are all
separable, e.g. *"C base + D high-contrast theme + F night mode + A's
telemetry on Nodes"*. Yours to call.

## How to proceed (after your pick)

1. You: open the HTML, choose A / B / C / D / E / F / a blend
   (visual, audio, *and* theme-variant are independently mixable —
   e.g. "C visuals + A's alert klaxon + D as high-contrast theme"),
   with notes.
2. Me: rewrite `meshmore-sns-UX-spec.md` *Design* section to the one
   chosen direction — palette tokens, type, component specs, the 5
   screens, R11 nav, **the audio-pack spec (event→sound→visual→haptic
   parity table)**, plus a component checklist.
3. Me: build it once in Flutter (theme + 5 view scaffolds + real
   Dashboard/Chat wired to `MeshcoreController`; audio via a small
   `flutter_soloud`/`audioplayers`-backed cue service with the
   visual+haptic parity baked in).

No Flutter UI or audio asset is built until you pick — keeps this
review cheap. Audio packs are previewed by synthesis in the HTML so
the sonic identity is decidable now.
