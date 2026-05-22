# Overview

Meshmore SNS is a mobile client built in Flutter, and used to communicate on the Meshcore network.

## References

- Meshcore : https://meshcore.co.uk/
- LoRa : https://resources.lora-alliance.org/technical-specifications 
- Flutter : https://flutter.dev/
- Meshcore Github: https://github.com/meshcore-dev/MeshCore
- Meshcore Flasher : https://meshcore.co.uk/flasher.html
- Meshcore Wiki (Repeater setup, relevant to initial test config) : https://meshcore.co.uk/wiki.html#124-repeater
- Seeed SenseCAP T1000-E + MeshCore setup (flash, "Companion Bluetooth" variant, BLE pair PIN 123456, LoRa region) : https://wiki.seeedstudio.com/sensecap_t1000_e_meshcore/
- MeshCore v1.15.0 release notes (the pinned firmware; flash via flasher.meshcore.io) : https://blog.meshcore.io/2026/04/19/release-1-15-0

## Problem

While Meshcore protocol is open source, the client is not open source.  We want ta general purpose Meshcore compatible client available under a friendly academic open source license.
 

## Requirements

- R1: app is written in flutter
- R2: app runs on Android and iOS
- R3: app uses bluetooth to communicate with a Meshcore client device. 
- R4: app is localized to English and Japanese
- R5: app can perform TTS using the GPU on the phone and should have a TTS mode that can be turned on, off by default
- R6: app should support a small scrollable, collapsable chat
  interface that is using the active channel, and has an easy way
  to switch channels. **Channel management** (the underlying
  surface for R6) covers the full lifecycle of a MeshCore channel
  slot — slot listing with active marker, set-active by tap,
  create/overwrite via an Edit dialog with three PSK sources
  (Public well-known key, `#hashtag`-derived via SHA-256, raw 32-
  hex), an **encryption explainer banner** (Public = world-
  readable; private slots = AES-128 with the user-set PSK), a
  **Random PSK** generator (Hex mode, `Random.secure` 16 bytes)
  plus a **Copy** button when a full key is in the field, a
  **#tag strength hint** that warns on tags shorter than 8 chars
  or matching a common-word denylist (EN + JA), a **slot-0
  overwrite warning** dialog when about to write a non-Public PSK
  into slot 0, a **Show current key** reveal (per-dialog latched,
  `SelectableText` for copy-out), a **Clear slot** affordance
  (pragmatic implementation: overwrite back to the well-known
  Public defaults, since the protocol has no `CLEAR_CHANNEL`
  opcode), and **edit-dialog key-source detection** (segmented
  button defaults to Hex with the field pre-filled when the slot
  has a non-Public PSK). Tag-derived slots read back as Hex on
  re-open because the bytes aren't reversible to a hashtag; the
  user can switch modes to re-derive from a tag if they prefer.
  See R33 for the default-channel preference.
- R7: app should be able to configure all Meshcore settings, and input radio settings if defaults are not available
- R8: app has a dashboard as the default home screen
- R9: app has an about screen with the app information
- R10: app has a Terms and Condition
- R11: app has a swipe gesture to left or right to navigate to diferent views, and a long press on the icon to get a navigation menu. 
- R12: app ships an **audible theme pack** matching the selected
  design concept (UI/event sounds, alert tones, TTS voice character).
  Every audible cue MUST have a synchronised non-audible equivalent
  (visual + optional haptic); the sound pack is mutable independently
  of TTS and a "visual + haptic only" mode is provided.
- R13: app is **accessible and hearing-impaired-friendly**: no
  information conveyed by sound (or colour) alone; WCAG-aligned
  (AA+ contrast, ≥48dp targets); honours OS bold-text, text-scale,
  high-contrast, reduce-motion and reduce-sound/silent settings;
  all actionable widgets carry semantic labels.
- R14: app has a **user-customizable appearance & profile**: theme
  presets (incl. the design-concept themes + a high-contrast and a
  night/low-power preset) and a custom palette, font family & size
  scale, audio-alert and accessibility preferences; settings persist
  per user/profile. Reached from Settings (Device configuration,
  App settings, Profile & personalization sub-screens).
- R15: app lets the user **set the LoRa region** for their country
  from a list of community/regulatory presets (each carrying
  frequency, bandwidth, spreading factor, coding rate and a TX-power
  hint), with a **Custom** option for full manual entry (subsumes
  the radio side of R7). Presets only ship values that can be cited;
  the UI states that the user is responsible for legality in their
  jurisdiction and that **every node on a mesh must use identical
  radio params**. Regions supported at minimum:
  - **US** 902–928 MHz (915.0 MHz; ≤30 dBm, 100% duty)
  - **EU 868** 869.4–869.65 MHz (869.525 MHz; ≤27 dBm, 10% duty)
  - **Japan / ARIB STD-T108** — 920.8 MHz · BW 125 kHz · SF 12 ·
    CR 4/8 · TX ≤ 13 dBm. Band 920.5–923.5 MHz; 920.8 MHz sits in
    the 920.6–922.2 MHz zone (carrier-sense/LBT ≥ 5 ms, 4 s max
    single TX, no hourly duty cap, 50 ms post-TX pause). SF12/BW125
    is the field-validated setting for urban JP RF noise (SF7/SF9
    failed in Akihabara testing). Source: MeshCore issue
    [#460](https://github.com/meshcore-dev/MeshCore/issues/460#issuecomment-4080531481)
    (@jirogit, 2026-03-18). **Note:** ARIB mandates LBT; MeshCore's
    `int.thresh` defaults to 0 (disabled) — full JP compliance also
    needs a non-zero carrier-sense threshold in firmware
    (upstream-tracked, JP_STRICT mode #2079); the app preset alone
    is not sufficient for regulatory compliance and the UI must say
    so.
- R16: app **displays the connected device's battery level** (from
  `CMD_GET_BATTERY` 0x14 → `RESP_CODE_BATT_AND_STORAGE` 0x0C,
  `batt_mv`), shown on the Dashboard and the radio-link/Diagnostics
  area, polled periodically while connected and refreshed on
  reconnect. Show a **charging** indicator when charging.
  *Protocol caveat:* the pinned companion firmware
  (`companion-v1.15.0`) `BATT_AND_STORAGE` frame carries only
  battery millivolts (no explicit charge-state bit). So: always
  show level (mV → approx %/voltage); show "charging" only when it
  can be determined (a future firmware charge flag, or a
  conservative rising-voltage heuristic) — never display a false
  charging state. Absent/again unknown ⇒ show level only.
- R17: app **does not lose messages while backgrounded or
  screen-locked** (the normal state for these apps). Baseline =
  **eventual-delivery guarantee**, not live background reception:
  the MeshCore radio buffers inbound messages while the companion
  app is suspended/disconnected; on next foreground/startup the app
  **auto-reconnects, drains the device queue (`SYNC_NEXT_MESSAGE`
  until `NO_MORE`), and the chat is locally persisted** — so
  messages received while backgrounded appear on reconnect and
  survive restarts. Already substantially satisfied (auto-reconnect,
  SYNC drain on ready, `ChatStore`) + an **app-lifecycle observer**
  that ensures-connected + drains on resume/foreground.
  **Android needs a special solution** (decided): under Doze /
  background limits the link is killed and the device buffer can
  overflow over a long lock, so Android requires a **foreground
  service** (or equivalent periodic background work) that keeps /
  periodically re-establishes the link and drains the queue so the
  eventual-delivery guarantee actually holds. This **requires a
  persistent notification** (accepted as the cost). iOS: relies on
  reconnect-on-foreground (OS-driven); `bluetooth-central` is a
  later optional add. **Still a non-goal:** real-time per-message
  push/UI while locked — the Android service does *periodic
  background drain*, not live delivery. Net contract remains
  *eventual delivery*, just made reliable on Android.
- R18: app provides a **hyperlocal grid visual-notification
  system** — a grid representing the area within MeshCore's
  theoretical practical range, our node as the reference. Nodes /
  message events register at a position **relative to us** using
  **hybrid positioning**: advert GPS lat/lon → true bearing +
  distance when available; else signal strength (RSSI/SNR) →
  distance ring with a stable arbitrary bearing; else an abstract
  slot. **Recency → brightness:** 100% = just registered, decaying
  to 0% at >24 h, after which it is removed (no longer visible).
  **Colour is not semantic** — taken from the active theme palette
  (R14); per R13 all information is carried by brightness /
  animation / position, never colour alone. **Audible** cue is the
  theme's pack (R12) with visual+haptic parity (R13), honouring
  reduce-motion (a non-animated fallback). **Animation semantics:**
  a *known* node → **pulses**; a node favourited as a contact →
  **blinks rapidly**. **"Known" = a node that has communicated
  directly/attributably to us** (e.g. a received DM, or one we've
  directly exchanged with) — strictly node/comms-level, **not**
  knowledge of the human(s), and **not** conferred by anonymous
  channel/Public traffic (clarified by user 2026-05-19). *Resolved
  sub-decisions:* geometry = **radial range-rings** (rings =
  distance, angle = bearing or stable per-pubkey hash); outer-range
  scale **derived from live radio params (SF/BW/freq), nominal
  fallback**; **anonymous channel/Public messages → a generic
  non-attributed ripple** (centre/ring), never mark a node known or
  place it. *Dependency:* a persisted **favourite-contact** flag
  (new, small). This is the concrete realisation of F2 (reframes
  F1). Depends on the R12 CueService (U6).
- R19: app uses a **standard back-button pattern**: Back always
  navigates **up the navigation stack to where it came from**, never
  pops the root unexpectedly nor destroys context the user expects
  to return to. Specifically: a pushed sub-screen (Settings sub-
  pages, dialogs, sheets) returns to its parent; from a non-
  Dashboard tab in the HomeShell Back returns to the Dashboard
  first; from the Dashboard Back lets the OS background the app
  gracefully (no in-app root pop). Dialogs and bottom sheets
  dismiss on back / tap-outside. Already substantially in place
  (PopScope on HomeShell `e83f0ba`; go_router push for sub-routes;
  Material AppBar back buttons auto-supplied) — R19 codifies and
  *guards* the pattern.
- R20: chat messages (channel and DM) support per-message **Reply**,
  **Copy** and **Delete** via long-press (and accessible
  equivalent — e.g. context menu key / a trailing overflow). Reply
  inserts a quoted prefix in the composer that references the
  source line (MeshCore has no thread semantics — quote is the
  carrier, optionally prefixed by the sender's `shortId`). Copy
  puts the message text on the clipboard. Delete removes the
  message **from the local history only** — the MeshCore companion
  protocol has no recall/unsend (over-the-air messages cannot be
  retracted); the UI must say so on the destructive action.
  Applies in Chat (channel) and DM screens; ChatStore prunes the
  deleted line and re-persists. Reduce-motion / a11y: the long-
  press menu is also reachable via a per-row trailing affordance
  for users who can't long-press reliably.
- R21: the app **handles permissions appropriately at install /
  first use**, never blocks **offline** use (viewing history,
  settings, the grid replaying local fabric, etc.), and degrades
  gracefully when a permission is denied. Specifically:
  - **Declared in platform manifests** at install (Android
    `BLUETOOTH_SCAN` / `BLUETOOTH_CONNECT` / `POST_NOTIFICATIONS`
    + `FOREGROUND_SERVICE`(`_CONNECTED_DEVICE`) / pre-12
    `BLUETOOTH`/`ACCESS_FINE_LOCATION`; iOS
    `NSBluetoothAlwaysUsageDescription`).
  - **First-run intro** explains the small set required (BLE +
    notifications) and **proactively requests** them via
    `permission_handler` — the user may **decline / skip** and
    still use the app for offline browsing.
  - **Per-action just-in-time prompts** re-request a denied
    permission only when the action that needs it is invoked
    (Connect, Scan, Advertise, Stay-connected toggle); a denial
    surfaces a clear "open settings to enable" path.
  - **Offline mode is first-class**: connection-state =
    disconnected is a normal state, every screen renders, history
    is from `ChatStore` + `KnownStore` + `FavoriteStore`. Refuses
    only the *connection-requiring* commands until granted.
  - Notification permission (Android 13+) is requested **only when
    the user toggles "Stay connected in background" on** (or the
    app first elects to show a system notification), not at
    install — keeps the first-run experience clean for users who
    decline background mode.
- R22: app can **set the connected device's advertised location**
  from either of two explicit sources, and never silently:
  - (a) the **phone's current GPS**, as a **one-shot fix** — no
    continuous tracking, no background usage; the fix is captured
    only when the user invokes it from Device config →
    Identity/Advert → **"Use phone location"** and is then offered
    for review before transmission.
  - (b) the **connected MeshCore device's own reported location**
    (`SelfInfo.latitude` / `longitude`), via an explicit **"Read
    device location"** affordance (and the existing pre-fill on
    Identity/Advert load).
  Setting the lat/lon field does **NOT** auto-broadcast; the user
  still has to tap **"Set advert location"** (`SET_ADVERT_LATLON`)
  to push it on the next advert. Adds platform permissions
  (Android `ACCESS_FINE_LOCATION`, iOS
  `NSLocationWhenInUseUsageDescription`); requested **just-in-time**
  on first "Use phone location" tap, never at install (per R21).
  Improves R18's hybrid positioning — once our own lat/lon is
  known, the grid switches from RSSI-rings to GPS bearing+distance
  for peers that also advertise a location.
- R23: the **RAW FRAME LOG** on the Diagnostics screen lives in
  its **own bounded scroll view** (not the page's main scroll),
  so the user can scroll the frame log without losing the State /
  Sends / Channel-tail oracle / capture-export context above it.
  Currently the whole Diagnostics screen is one `ListView`; the
  frame log section should become a fixed-height inner pane with
  its own scrollbar. Keeps the **Copy log** action's value too —
  long captures stay browsable in place.
- R24: **per-channel notification settings** — per-device global
  toggles are not granular enough. Each channel slot gets its own
  set of notification overrides for the cues that apply to its
  traffic, layered on top of the global R12/R13 settings:
  - audio cue on/off (overrides `audioMaster` for this channel only)
  - haptic cue on/off
  - TTS speak inbound on/off (this exists for channels already via
    `TtsController.toggleChannelMute` — fold it into the per-channel
    bundle)
  - OS system notification on/off (future, when per-channel notifs
    are wired)
  Defaults inherit from the global setting; an explicit channel
  override is persisted. Reached from **Channel management → per-
  slot Notifications** (and a quick toggle in the Chat header). DMs
  may later get the same treatment per-peer; out of R24 scope.
- R25 (future): **reverse-geocoded equal-grid map** — a second map
  option in the **Hyperlocal grid** picker (the existing radial
  range-ring view becomes "Hyperlocal grid · radial" / default).
  This second view is a **true equal-sized-box grid** (think
  electoral district map) covering the area around our own
  location. Behaviour:
  - **Grid cells** are equal-sized rectangles in screen space; the
    map projection is stretched to fit a rectangular canvas (not a
    spherical/Mercator render — the goal is comparability between
    cells, not cartographic accuracy).
  - **Labelling**: each cell shows the town name when one falls
    within it; major **POI** are labelled inside cells when they
    exist. At minimum we mark the town(s) covering the user.
  - **Node markers** inside each cell roughly preserve the node's
    geographic position relative to our own device — i.e. cells
    are aggregator buckets, not bins that randomise position
    within them.
  - **Type glyph**: companion-app nodes render as a **dot**;
    repeater nodes render as a **triangle**.
  - **Density collapse**: when a cell contains more than ~8 nodes,
    collapse to a single badge **"• × N"** (or "▲ × N" if all
    repeaters; mixed cells fall back to "• × N") so the view
    doesn't choke. Tapping the badge expands an inline list.
  - **Selectable nodes** (same model as the radial grid, R18) —
    tap to open the NodeDetailSheet with the per-node actions.
  - **Offline-first**: map tiles load **opportunistically in the
    background and are cached on disk**. Two candidate sources —
    (a) OpenStreetMap raster tiles (well-known licence; needs
    attribution), or (b) a custom minimal tile DB built from
    public data sources (Natural Earth, GeoNames) for towns/POI
    + coastline/admin boundaries. (a) is cheapest to ship; (b)
    is the most ideologically aligned and avoids the live OSM
    dependency. Decision deferred.
  - **Online detection** drives prefetch — when connected to
    Wi-Fi/cellular we pre-pull tiles for the area surrounding
    our last-known own location and a ring of N km out. Offline
    use degrades gracefully: cells without tiles show plain
    background + labels; node markers + selection still work.
  Listed here as a **forward-looking requirement**; not yet
  built. The current /grid stays as our default until this lands.

- R26 (future): **XR data streaming via private temp URL**. The
  app generates a one-off, **not-publicly-listed** share URL via a
  short-lived reverse-proxy in the cloud. An XR headset opens that
  URL and renders MeshCore **symbology** overlaid in world space,
  using the connected device's GPS (or the phone's GPS) for our
  own pose and the live fabric data for surrounding nodes. URL is
  per-session, capability-token gated, expires fast (minutes), and
  carries only what's needed for the render (positions, types,
  status, callsigns), never PII or unrelated app state. The XR
  surface is the natural place to *use* topological info
  (latitude / longitude / **altitude** if the protocol publishes
  it later) — adverts already carry lat/lon; altitude is a
  forward-looking extension.
- R27 (future): **Macro-globe view via Globe.GL**. A third map
  view (after the radial /grid and R25 equal-grid map) that
  pins our position and the fabric on a **3-D globe**, using
  [Globe.GL](https://globe.gl/) (Three.js wrapper) inside a
  **WebView** component. The earth tile-set is pre-cached at a
  **very macro scale** so the globe spins offline with a useful
  basemap. Triangulation is **purely client-side math** — we know
  our lat/lon and the node's lat/lon, no server needed; the globe
  is just the projection surface. Pin styling matches our R18
  symbology (companion = dot, repeater = triangle, contact/known
  badges). Use case: confirm a node's geographic position at a
  glance without needing the OSM tile pipeline that R25 demands.

- R28 (future): **user-defined node tagging / custom groups**. Today
  we have three orthogonal flags (fabric / known / contact), all
  derived from observed traffic. R28 adds **user-authored tags** —
  arbitrary string labels the user can apply to one or more nodes
  ("trusted", "family", "field-team-A", "burning-man-2026", etc.).
  Tags compose into **filters** on the Nodes list and the
  hyperlocal grid ("show only family + field-team-A"), and can
  drive **per-group notification settings** (extension of R24) so
  e.g. "family" can be loud while "trusted" stays silent. Tag set
  is **local-only** by default (lives in shared_preferences keyed
  by pubkey hex); a future protocol extension could let users
  share tag-sets between devices but is out of scope here. UI
  surfaces: a Tags row in NodeDetailSheet (chip input + existing
  tag chips), and a tag chip strip above the Nodes list that
  composes with the R20-pattern filter bar.

- R29: **jump-to-newest control in Chat and DM**. When the user
  scrolls up to read older history, the auto-scroll-on-new-message
  behaviour pauses (so the screen doesn't yank away under them).
  Surface a **floating pill** anchored bottom-right above the
  composer that fades in only when the scroll position is **not at
  the latest message**, labelled "Jump to newest" (`↓ N new`-style
  badge if new messages arrived while scrolled up). Tap → smooth-
  scroll to the bottom and re-arm auto-scroll. Applies to both
  ChatScreen (channel feed) and DmScreen (peer feed). Pill uses
  the active theme's accent + the existing cue palette; respects
  `reduceMotion` (instant jump, no animated scroll).

- R30: **brightness cycle toggle on the Dashboard**. A single
  affordance on the Dashboard cycles the app's display brightness
  through a fixed ladder — `dim → med → bright → max → dim …` —
  with a visual readout of the current step. Implements via a
  controller that sets a global `brightnessTier` (persisted in
  `shared_preferences`); ThemeController consumes the tier and
  scales the active palette's surface luminance accordingly (NOT
  the system brightness — keeping the OS brightness untouched
  avoids surprising the user). Goal is field-readability in bright
  sun without diving into Settings → Personalization. Possible
  step labels: DIM / MED / BRIGHT / MAX. Control lives in the
  Dashboard status slab area (small icon + tier label), tappable
  to advance one step. Long-press resets to MED. Accessibility:
  honours `reduceMotion` (no fade between tiers).

- R31 (future): **device CLI / power-user shell**. The MeshCore
  companion protocol doesn't have typed opcodes for many useful
  firmware-side knobs (on-device buzzer mute, LED mode, deep-
  sleep schedule, OTA carrier-sense thresholds, etc.) — most
  firmware variants accept ad-hoc text commands DM'd to the
  device's own pubkey (e.g. `"buzzer off"`, `"led 1"`,
  `"sleep 30"`). R31 adds a power-user **Device CLI** section
  in Device config: a text input that sends commands to
  `selfPubkey`, an output log that shows the echo, and a small
  starter palette (per-firmware-fork) of known-good commands.
  Behaviour is **firmware-dependent** — the UI is generic, the
  command set is fork-specific. Off by default; gated behind
  an "advanced" toggle so casual users don't fire random
  strings at their radio.

- R32 (future): **dice-roll PSK derivation via IMU**. A new
  channel-key source alongside Public / #tag / Hex: **Shake**.
  The user holds the phone and shakes it; we sample the
  accelerometer via `sensors_plus`, mix each delta-magnitude
  sample into a running SHA-256, and once enough entropy is
  collected (target: 256 bits of accelerometer variance
  surface, conservative budget: ~3–5 s of moderate motion)
  derive a 16-byte AES-128 key from the final digest. UI: a
  big "shake to roll" prompt, a progress ring showing entropy
  accumulated, a hex preview that fills byte-by-byte as the
  digest stabilises. The deliberate framing is a **16-sided
  die** ("hexadecimal die") rolled per nibble for the visual,
  even though the underlying derivation is whole-bytes from
  the digest. "Reroll" restarts. Honours `reduceMotion` by
  showing a fallback "tap to roll dice" path that uses
  `Random.secure` instead of motion — same result, no
  accelerometer dependency. The motion path needs the
  motion sensor permission on iOS (Info.plist) and is opt-in.

- R33: **default channel preference**. The app should remember
  the user's preferred channel slot to land on at startup,
  independent of whatever the device's internal active-slot
  state happens to be (which a multi-device user may want to
  vary). UI surface: a "Set as default" affordance per slot in
  the Channels mgmt screen + Edit dialog. Persisted in
  shared_preferences. On controller init, if a default is set,
  call `setActiveChannel(default)` once the device reaches
  ready. Falls back to slot 0 (Public) if unset.

### Terminology — Fabric vs Contact

- **Fabric** = the set of nodes we have *seen* on the mesh (any
  advert, RF-log or device-side contact entry). General discovery
  surface; what the **Nodes** view shows.
- **Contact (UX sense)** = a fabric node the **user has explicitly
  favourited** — a relationship the user keeps. (The protocol-level
  `Contact` decode is unchanged; this is purely a UX concept layered
  on top.) The favourite flag drives R18's *rapid-blink* semantics.
- "Known" (R18) = a node we have had **direct/attributable
  communication with** (DM/direct exchange) — orthogonal to fabric
  vs contact, drives R18's *steady pulse*.

### Design rationale — our reasoning, not derivation

The Fabric / Contact / Known split, the bounded-active-set
discipline, and the "**projection, not bigger caps**" stance for
dense areas are **our reasoning from first principles**. They are
not chosen because MeshCore or Meshtastic chose similar numbers —
we converge on similar magnitudes for similar physics (human
attention, flood resilience, sync sanity, notification hygiene),
but the *model* is ours.

Anchor (field observation, user, 2026-05-19): one day of normal use
of the original MeshCore iOS / Android app **filled its ~350
"contacts" cap from passive advert reception**, with the app
**over-notifying about the cap being full**. That makes "contact"
in that app effectively *"anything we've heard"* — a misnomer
masquerading as a relationship.

Our response is **not** "match the cap" or "raise the cap" — it's
**rename what the bucket means**:

- **Fabric** = anything seen — bounded loosely, cheap, **no
  notification cost just for existing**.
- **Contact** = something the user *explicitly intended* (a
  favourite — an act of attention) — protected from eviction,
  surfaced prominently, drives R18's rapid-blink.
- **Known** = something with direct/attributable comms (a DM) — a
  separate, evidence-based axis, drives R18's pulse.

The numeric ceilings the build lands at (active ≈ device's
`maxContacts`; history ≈ 2 000 LRU, see task #36) are pragmatic
projections derived **for our scenarios** (single-radio, mostly
moderate density, occasional dense event), independently of what
other apps do — they may have chosen similar bounds, but our
*reasons* are the failure modes listed under task #36
(notification spam, sync semantics, flood-displacement of
favourites, write churn, UI saturation), not "compatibility."

The interesting *difference* from the official apps is the split
itself: they conflate "anything heard" with "contact"; we don't.
This means the user's first time-cost — paying attention to who
they actually care about — never gets washed out by passive
advert traffic.

## Meshcore Protocol Implementation Plan

This section is the agreed plan for implementing the Meshcore companion
protocol in Dart and proving conformance (satisfies R3 and the TC1–TC4
test cases).

### Scope and source of truth

- The client talks to a Meshcore radio using the **companion-radio
  protocol** (the frame protocol a phone/app uses to drive a Meshcore
  device). This is distinct from the over-the-air LoRa mesh packet format;
  we implement the companion side only.
- **Authoritative source:** the MeshCore firmware repository
  (`https://github.com/meshcore-dev/MeshCore`). We **pin a specific
  firmware commit** and transcribe all opcodes, frame layouts, struct
  field orders, and crypto parameters from that commit. The pinned commit
  is recorded in the protocol package (`lib/src/codec/constants.dart`
  header + README) and **asserted by a test**; it is bumped deliberately,
  never silently.
- **Current pin:** tag `companion-v1.15.0`, commit
  `dee3e26ac081a5c668c69b66c16a6544a44ddc5b` — the official
  **MeshCore v1.15.0** release (2026-04-19,
  <https://blog.meshcore.io/2026/04/19/release-1-15-0>; flash the
  "Companion Bluetooth" build via <https://flasher.meshcore.io>).
  The 1.15.0 notes document **no breaking** companion-protocol /
  opcode / channel / crypto / packet-format changes, so the
  transcription is valid for this release. Additive items:
  *GROUP_DATA binary packets* (`PAYLOAD_TYPE_GRP_DATA` 0x06 /
  `SEND_CHANNEL_DATAGRAM` 0x3E — opcodes known, payload body not yet
  decoded → `UnsupportedFrame` by design; tracked future item) and
  *Default Scope support*.
- **Source precedence (learned in M1):** the markdown spec
  `docs/companion_protocol.md` is **incomplete** — it documents framing,
  UUIDs, APP_START, SELF_INFO, and OK/ERROR, but **omits field layouts
  for contacts/time and several commands entirely** (`GET_CONTACTS`,
  `GET_DEVICE_TIME`, `SET_DEVICE_TIME` appear only in source). The
  firmware **source is authoritative** where the two differ or the doc is
  silent. The companion frame (de)serialization lives in
  `examples/companion_radio/MyMesh.cpp` (command dispatch + `out_frame`
  writes) — transcribe byte layouts from there, at the pinned commit.
- `tools/liboffsns/` is a separate **Meshtastic** PoC and is **not** reused
  here — Meshcore is a different protocol.

### Packaging and architecture

The protocol is a **standalone, pure-Dart package** with zero Flutter
dependencies, so conformance tests run under `dart test` in CI without a
device or emulator. It lives at the **submodule root**, a sibling of the
Flutter app, so the app's path dependency (`meshcore: { path:
../packages/meshcore }`) stays self-contained on the `meshmore-sns`
submodule branch.

```
packages/meshcore/                 # pure-Dart, no Flutter imports
  lib/
    meshcore.dart                  # public API barrel
    src/
      codec/
        constants.dart             # [M0] pinned opcodes / UUIDs / framing / sizes
        byte_cursor.dart           # [M1] bounds-checked LE read (ByteCursor) + FrameBuilder
        frame_codec.dart           # [M1] command encoders + total decode()
        inbound.dart               # [M1] sealed MeshcoreInbound frame hierarchy
        decode_error.dart          # [M1] typed decode-failure model
      model/
        self_info.dart             # [M1] SELF_INFO (0x05)
        contact.dart               # [M1] CONTACT (0x03)
        channel_message.dart       # [M2] CHANNEL_MSG_RECV (0x08 / 0x11 V3)
        channel_info.dart          # [M2] CHANNEL_INFO (0x12) + MSG_SENT (0x06)
        # later: advert, radio_params, …
      crypto/
        channel_crypto.dart        # [M2] AES-128-ECB + HMAC-SHA256 MAC + channel hash
        # [M3] Ed25519, X25519 ECDH (pointycastle)
      session/                     # [later] req/resp correlation, contact-sync, msg queue, time sync
      transport/
        transport.dart             # [M0] abstract MeshcoreTransport — NO BLE here
  test/
    constants_test.dart            # [M0] pin + opcode + framing assertions
    frame_codec_test.dart          # [M1] vector goldens + programmatic + totality
    vectors/m1_frames.json         # [M1] conformance vectors (hex ⇄ decoded)
    # later: crypto/ KATs, more vectors/*.json
meshmore_sns_app/ (Flutter, ./responsive_starter_app)
  lib/transport/ble_meshcore_transport.dart   # [M5] MeshcoreTransport via flutter_blue_plus + the BLE service
  lib/services/…                              # [M5+] wires transport → codec/session → app state (Provider)
```

A core discipline established in M1: **`decode()` is total** — malformed
or truncated input yields a `DecodeFailure` value and unknown opcodes an
`UnsupportedFrame` (raw bytes preserved); the decoder never throws to
callers. This is what the negative/fuzz conformance tests enforce.

Layering (each layer testable in isolation):

1. **Transport** — abstract `MeshcoreTransport`: opens a bidirectional byte
   stream. BLE implementation lives in the app (R3); the package stays
   pure-Dart and is fed bytes by tests.
2. **Framing** — split the raw byte stream into protocol frames; handle
   BLE-MTU fragmentation/reassembly.
3. **Codec** — encode app→device command frames; decode device→app
   response and async push frames into typed events; typed decode errors
   (never throws on malformed input).
4. **Session** — request/response correlation, the contact-sync state
   machine, outbound message queue/ack tracking, device time sync.
5. **Crypto** — split by capability across two pure-Dart libraries
   (neither covers both needs; finalized in M2/M3b):
   - **`package:pointycastle`** — *symmetric*: raw **AES-128-ECB**
     (MeshCore's channel/DM cipher; `package:cryptography` omits ECB),
     HMAC-SHA256, SHA-256/512.
   - **`package:cryptography`** — *asymmetric*: Ed25519 verify and
     X25519 ECDH (pure-Dart `DartEd25519`/`DartX25519`; pointycastle
     3.9.x ships neither). This re-vindicates the original
     `package:cryptography` selection for the asymmetric layer.
   The Ed25519→Montgomery-u birational map (the one piece neither
   library exposes) is a small in-package BigInt routine. All anchored
   by RFC 8032/7748 + offline **libsodium**-generated KATs.
6. **Domain/state** — app-facing repository of contacts, channels,
   messages, and node/link status.

### Initial real-hardware test configuration

On-device milestones (M5/M6) and TC1/TC3/TC4 are validated against this
reference setup:

- **App host:** a Flutter **Android** device (Android is the primary
  on-device test target; iOS validated subsequently).
- **Radio hardware:** **Seeed Studio T1000-E** tracker devices flashed
  with MeshCore firmware via the official flasher
  (`https://meshcore.co.uk/flasher.html`). Device-specific setup
  (flashing, **"Companion Bluetooth"** variant, BLE pairing PIN
  `123456`, LoRa region):
  `https://wiki.seeedstudio.com/sensecap_t1000_e_meshcore/`.
- **Topology:** at least two T1000-E nodes so messages traverse the mesh
  rather than a single device loopback. Where a relay is needed to
  exercise multi-hop, one node is configured per the **Repeater** guidance
  in the MeshCore wiki
  (`https://meshcore.co.uk/wiki.html#124-repeater`); this also documents
  the role/region/radio settings used to seed the app's defaults (R7) and
  the conformance interop fixtures.
- **Link under test:** the Android app connects to one T1000-E over the
  BLE companion transport (R3); the second node (and any repeater) closes
  the loop for send/receive and encrypted/unencrypted checks.

The firmware version flashed onto the T1000-E units is recorded alongside
the pinned firmware commit (see *Scope and source of truth*) so the
on-device behaviour and the offline conformance vectors stay aligned.

### Conformance test strategy (TC2)

"Passes all conformance tests" is defined as the union of:

- **Vector goldens** — `test/vectors/*.json`. Two directions:
  *encode* cases assert the encoder produces a **byte-exact** frame
  (anchored where possible to verbatim examples in the pinned doc, e.g.
  the APP_START `mccli` example); *decode* cases assert a hex frame
  decodes to the expected typed model. Large/variable frames
  (SELF_INFO, CONTACT) are additionally exercised by programmatic
  goldens that build the bytes from the documented layout and assert
  every field plus the fixed frame length, so layout drift is caught.
- **Crypto KATs** — fixed known-answer tests for Ed25519 sign/verify,
  X25519 shared-secret derivation, and AES, with vectors taken from the
  firmware and/or the relevant RFCs, so our crypto matches the device
  byte-for-byte.
- **Negative/fuzz tests** — truncated, over-length, and garbage frames
  must yield typed errors and never crash or hang the decoder.
- **Interop fixtures** — optional captured real-device frames (BLE sniff
  or wired serial dump) checked in under `test/vectors/` as regression
  anchors against the pinned firmware.
- **CI gate** — `dart test` for `packages/meshcore` runs on every change;
  the conformance suite is the merge gate. On-device integration tests are
  separate and run behind a flag.

### Milestones

| ID | Milestone | Delivers | Test cases | Status |
|----|-----------|----------|-----------|--------|
| M0 | Package scaffold + firmware pin + constants transcription + CI | `packages/meshcore` builds, `dart test` green, pinned commit documented | — | ✅ done |
| M1 | Framing + core codec (app-start, self-info, get-contacts, device-time), unencrypted | Connect handshake decodes; vectors for core frames | TC2 (partial) | ✅ done |
| M2 | Channel messaging + public-channel AES | Send/receive channel text; channel crypto KATs | TC2, TC3, TC4 | ✅ done |
| M3 | Contacts + DM (X25519 ECDH + AES) + advert parse/Ed25519 verify | Direct messages; advert verification | TC2, TC3, TC4 | ✅ done (M3a codec + M3b crypto) |
| M4 | Radio/device configuration | Read/write radio params and settings (supports R7) | TC2, TC4 | ✅ done |
| M5 | BLE transport in app + connection state machine | App connects to a real device over BLE | TC1 | ✅ code done (on-device TC1 in M6) |
| M6 | End-to-end integration | Send/receive encrypted and unencrypted over a real device | TC1, TC3, TC4 | 🔶 prep done (turnkey); on-device run pending hardware |
| M7 | Hardening | Fuzz/error taxonomy, reconnection/backoff, full conformance gate enforced | TC2 (final) | ✅ done |

### Progress log

- **M0** (submodule commit `488b776`): pure-Dart package scaffolded at
  `packages/meshcore`, sibling to the app; firmware pinned
  (`companion-v1.15.0` / `dee3e26`) and asserted by test; `constants.dart`
  seeded with BLE UUIDs, framing rules, core opcodes, SNR conversion;
  abstract `MeshcoreTransport`; CI (`.github/workflows/ci.yml`) running
  the conformance gate + app analyze/test.
- **M1** (submodule commit `69acc47`): byte layouts transcribed from
  `MyMesh.cpp` at the pin (doc was partial — see *Source precedence*);
  `ByteCursor`/`FrameBuilder`; encoders (`appStart`, `getContacts`,
  `getDeviceTime`, `setDeviceTime`, `syncNextMessage`); **total**
  `decode()` → sealed `MeshcoreInbound` (OK, ERROR, CONTACTS_START,
  CONTACT 148B, END_OF_CONTACTS, SELF_INFO, CURR_TIME, NO_MORE_MESSAGES,
  UnsupportedFrame, DecodeFailure); `SelfInfo`/`Contact` models;
  conformance suite **28 tests green** (`vectors/m1_frames.json` +
  programmatic SELF_INFO/CONTACT goldens + totality tests).

- **M2** (submodule commit _pending_): channel frame layouts from
  `MyMesh.cpp`; encoders `sendChannelTextMessage` / `getChannel` /
  `setChannel`; decoders MSG_SENT (0x06), CHANNEL_MSG_RECV (0x08) +
  V3 (0x11), CHANNEL_INFO (0x12); models `ChannelMessage` /
  `ChannelInfo` / `MsgSent`. `MeshcoreChannelCrypto` ports
  `Utils::encrypt/decrypt/encryptThenMAC/MACThenDecrypt` + channel
  hash (AES-128-ECB zero-pad; HMAC-SHA256→2B over the 32-byte secret;
  `SHA256(secret)[0]`). Crypto-lib decision changed to **pointycastle**
  (see Crypto layer note + Risks). Conformance **57 tests green**
  (M2 vectors + programmatic + KATs anchored to NIST/RFC 4231).
  *Open item:* the public channel's name and the upper 16 bytes of a
  channel secret are not on the companion link — to be pinned by an
  M6 on-device interop fixture; `channelSecretFromPsk` zero-fills
  provisionally and no public-channel constants are fabricated.

- **M3a** (submodule commit _pending_): codec half of M3. Commands
  `sendTextMessage` (0x02), `sendSelfAdvert` (0x07), `setAdvertName`
  (0x08), `addUpdateContact` (0x09); decoders `CONTACT_MSG_RECV`
  (0x07) + V3 (0x10, signed `txt_type==2`), `ADVERTISEMENT` (0x80)
  with exact Ed25519 `signedMessage = pub_key ‖ ts ‖ app_data`;
  models `ContactMessage`/`Advert`; `Contact` stores raw lat/lon
  micros for byte-exact re-encode. **Public-channel constants now
  sourced** from `docs/qr_codes.md` (`Public` /
  `8b3387e9c5cdea6ac9e5edbaa115cd72`) — closes the M2 gap, no
  fabrication. Conformance **70 tests green**.
- **M3b** (submodule commit _pending_): crypto half — completes M3.
  `MeshcoreIdentityCrypto` (Ed25519 verify/verifyAdvert; pure-Dart
  `edPublicKeyToMontgomeryU`; `ed25519KeyExchange` =
  clamp(prv64[0:32]) · Montgomery-u via X25519, raw output;
  orlp-style `expandedPrivateKeyFromSeed`) and `MeshcoreDmCrypto`
  (ECDH secret + DM payload crypto delegating to the M2 routine — DM
  uses a full 32-byte secret, so **no tail ambiguity**). `cryptography`
  added for the asymmetric primitives. Anchored by RFC 8032 §7.1, RFC
  7748 §5.2, and **offline libsodium-generated** KATs
  (`vectors/m3b_x25519_kat.json`, via pynacl) covering the conversion,
  the full key exchange, and DH symmetry — our composition matches
  libsodium byte-for-byte. Conformance **81 tests green**.

- **M4** (submodule commit _pending_): device/radio configuration
  (R7). Encoders `setRadioParams` (0x0B), `setRadioTxPower` (0x0C),
  `setAdvertLatLon` (0x0E), `setOtherParams` (0x26),
  `setTuningParams` (0x15), `deviceQuery` (0x16), `getBatteryStorage`
  (0x14); decoders `DEVICE_INFO` (0x0D, 82B) / `BATT_AND_STORAGE`
  (0x0C); models `RadioParams`/`DeviceInfo`/`BatteryStorage`.
  Conformance **97 tests green** (incl. negative int8 TX power and
  ×1000/×1e6 scaling round-trips). The full R7 protocol surface (read
  via SELF_INFO/DEVICE_INFO, write via the SET_* commands) is now
  available to the app.

- **M5** (submodule commit _pending_): BLE transport + connection
  state machine, **in the Flutter app** (first milestone outside the
  pure-Dart package). `MeshcoreConnection` — hardware-free state
  machine (disconnected/handshaking/ready/reconnecting/failed) that
  drives the APP_START→SELF_INFO handshake and decodes inbound frames;
  `BleMeshcoreTransport` (flutter_blue_plus, the companion
  NUS-style service, 1 notification == 1 frame); `BleConnector`
  (scan/permission/connect/discover); `MeshcoreController`
  (ChangeNotifier provider, injectable transport factory) wired into
  `MultiProvider`. The state machine + controller are
  **`flutter test`-covered with a fake transport (no hardware)** — 10
  app tests green, `flutter analyze` clean. The flutter_blue_plus
  paths are analyze-clean and validated on real hardware in M6 (TC1).

- **M6 prep** (submodule commit _pending_; runbook in parent):
  hardware-free half of M6, making the on-device step turnkey.
  Decisive finding — the firmware `0x88` push carries the **full raw
  OTA packet**, so both open items are resolvable **BLE-only, no
  SDR**. Built: `RfLogFrame`/`OtaPacket`/`GrpTxtPayload` codec
  (`docs/packet_format.md` at the pin), the `resolveChannelTail`
  oracle, a skip-when-absent interop replay harness + schema, and an
  app-side raw-frame capture + `exportGrpTxtFixture`. Operator
  procedure: `meshmore-sns/M6-interop-runbook.md`. Conformance 103
  green + 1 skipped (interop, until real captures land). The on-device
  TC1/TC3/TC4 run + committing real fixtures is the remaining
  hardware step.

- **M7** (submodule commit _pending_): hardening; **TC2 final**.
  Property-fuzz (seeded, 4000+ cases) proves `decode` /
  `OtaPacket.parse` / `macThenDecrypt` / `resolveChannelTail` /
  `edPublicKeyToMontgomeryU` are total. Found+fixed two real
  totality gaps (`macThenDecrypt` non-aligned ciphertext;
  degenerate Ed25519 point) and a real **controller stale-state
  reconnect bug**. Error taxonomy tightened (dead kind removed,
  invariant documented + asserted). App: `ReconnectPolicy`
  (exp-backoff + full jitter) + `MeshcoreController` auto-reconnect.
  Conformance: meshcore 111 + 1 skipped; app 17. The
  `dart analyze --fatal-infos` + `dart test` (+ `flutter analyze/test`)
  CI workflow is the **enforced merge gate**.
- **M8** (submodule commit _pending_): external-reference review
  (docs.meshcore.io + wirehack7 gist). **Closed open item #1**
  (channel secret = `psk ‖ 0·16`). Fixed the channel-hash source
  (`SHA256(psk16)[0]`, was hashing the 32-byte secret); added
  `channelHashFromPsk` / `channelPskFromHashtag` (`#test` KAT) and
  a public-PSK cross-source KAT; reworked `resolveChannelTail`
  (`channelHashOk` tail-independent, MAC disambiguates). meshcore
  113 + 1 skipped; app 30. analyze clean.
- **Discovery fix** (submodule commit `006fb9f`): on-hardware report
  "can't discover the other node" — frames arrive but Nodes stays
  empty, radios on identical params. Root cause: the companion
  firmware queues heard contacts/adverts **and received messages**
  and signals via `PUSH_CODE_MSGS_WAITING` (0x83); the app must
  drain with `CMD_SYNC_NEXT_MESSAGE` (0x0A) until
  `NO_MORE_MESSAGES`. The app did neither (0x83 → UnsupportedFrame,
  no SYNC). Added `MessagesWaitingFrame` decode + a controller SYNC
  drain loop (on 0x83, on ready, after `scan()`; 512-step guard).
  This also unblocks U3 inbound chat on real hardware. meshcore 118
  + 1 skipped; app 56. analyze clean. **TC1/TC3/TC4 on-device
  retest recommended now that the queue is drained.**
- **Discovery is advert-driven** (submodule commit `bb2d3cf`):
  follow-up — on hardware, Public chat worked (send+receive) but
  Nodes stayed empty. Not a bug: MeshCore only creates a
  node/contact from a heard **advert**; channel/Public traffic
  never does, and the other node had never advertised. Discovery is
  bilateral. Nodes screen reworded (advert-driven empty state) and a
  distinct **Advertise** button (`SEND_SELF_ADVERT`) added next to
  Scan. 57 app tests; analyze clean. No protocol change.
- **"In range" clock fixes** (submodule commits `58f505c`,
  `37be563`): adjacent devices showed "1 known, 0 in range".
  Two causes, both clock-skew: (a) live adverts (0x80/0x88) used
  the sender's embedded timestamp — fixed to local receive time;
  (b) the dominant `GET_CONTACTS` path uses the device-clock
  `lastAdvertTimestamp` and the app never set the device clock
  (no RTC). Fix: `SET_DEVICE_TIME` = phone now on ready (also
  corrects message ordering). Caveat: pre-existing on-device
  contacts stay stale until the neighbour is re-heard post-sync.
  Also added **R16** (display connected-device battery + charging,
  with the mV-only protocol caveat). 59 app tests; analyze clean.
- **Clock/ERR hardening** (`a599d0c`): firmware rejects
  `SET_DEVICE_TIME` (`ErrorFrame 0106`); now also `GET_DEVICE_TIME`
  → device-clock offset translates contact `lastAdvertTimestamp`
  (in-range correct even when SET is refused; race-safe re-derive);
  ERR frames surface in recent activity. On-device frame log
  confirmed the peer (`C8DDE2F9AB60`) decodes & ingests. Added a
  Diagnostics **Copy log** button (`098d017`). 64 tests.
- **Feature batch** (`3a53e01` advert routing, `c8fcce9` R16
  battery, `54f5253` channels): Advertise flood/zero-hop chooser;
  R16 battery wired (GET_BATTERY poll + Dashboard/Diagnostics +
  conservative charging heuristic); **Channel management**
  (/settings/channels: slots, set-active, name+PSK via
  Public/#hashtag/hex → SET_CHANNEL/GET_CHANNEL). Logo set to
  concept E · DR Pop (`465de74`); back-button crash fixed
  (`e83f0ba`). 71 app tests; analyze clean. Future **F2** (map
  alternatives) / **F3** (AI integrations) flagged to revisit.
- **Device-config build-out** (submodule commit `1a92509`):
  IDENTITY/ADVERT (SET_ADVERT_NAME 0x08, SET_ADVERT_LATLON 0x0E
  prefilled from SelfInfo), DEVICE info (DEVICE_QUERY 0x16 →
  DeviceInfo read-only) + read-only OTHER PARAMS view, Channels row
  links to /settings/channels. Identity/advert helpers on
  controller. 74 app tests.
- **U8 pt1 — lifecycle resume (R17)** (submodule `8f32eb0`):
  `MeshcoreController.onAppResumed()` (ready→drain; paired+dropped
  →reconnect; respects manual/connecting) + HomeShell
  `WidgetsBindingObserver`. Cross-platform half of R17's
  eventual-delivery guarantee. 84 app tests.
- **U8 pt2 — Android foreground service (R17)** (submodule
  `a7b552f`): `BackgroundKeepalive` abstraction (Noop default +
  Android `ForegroundServiceKeepalive` via `flutter_foreground_task`
  9.2.2, FGS type `connectedDevice` — avoids the Android-15
  dataSync timeout) so the link survives Doze; opt-in default-on
  via `BackgroundKeepalivePrefs` + App-settings switch; manifest
  permissions + service decl; isolated so a native misconfig can't
  regress other platforms. 87 app tests. **Native UNVERIFIED in
  sandbox** (Android can't build here) — needs clean rebuild +
  on-device screen-locked test.
- **Favourite-contact flag + terminology + R19 + F8** (`8bf10cc`):
  `FavoriteStore` (shared_preferences pubkey-hex set), controller
  toggle/load/persist, Nodes-screen star (outlined→filled),
  favourites sort to top, status copy "X in fabric · Z contact(s)".
  Unblocks R18 rapid-blink. **Added R19** (back-button pattern —
  existing PopScope + go_router push already conform; codified to
  guard the pattern). **Terminology** (Fabric = seen; Contact UX =
  favourited; Known R18 = direct/attributable comms). **F8**
  (fabric survey grid for planning) flagged. 92 app tests.
- **U9 — hyperlocal grid (R18), visual first cut** (`1070de5`):
  radial range-rings + hybrid GPS→RSSI→stable-hash positioning,
  24h recency brightness, pulse=known, rapid-blink=favourite,
  reduce-motion fallback, theme-driven colour, `/grid` route +
  Nodes-screen entry. New `KnownStore` (persisted) + DM-prefix
  marks known. 98 app tests. Deferred: live-radio-params outer
  scale; channel ripple (added next via U6).
- **U6 — CueService (R12/R13) + grid channel ripple**
  (`cc8e851`): `CueService` + pluggable `AudioPack` /
  `HapticBackend` (defaults `SystemSound` + `HapticFeedback`,
  asset-free); `CueBridge` wires `MeshcoreController` → cues
  (state transitions + incomingChannelMessages); grid renders R18's
  **anonymous-channel ripple** (transient centre-out wave). Gates:
  audio = `audioMaster && !visualHapticOnly`; haptic always (OS
  honours silent). 103 app tests. Per-theme audio assets
  (Mission Control / Velocity / Sonar / Tribunal / Pure Phase /
  Codec) deferred — abstraction is plug-in-ready.
- **P2P direct messages + chat→channels shortcut** (`34fc0c4`):
  `ChatMessage.peerPubKeyHex`; controller `sendDirectText`
  (CMD_SEND_TXT_MSG 0x02, 6-byte prefix), `_ingestDm` (prefix→
  pubkey resolution, broadcast stream), `dmHistoryFor`; new
  `DmScreen` at `/dm/:pubkey` reached by tapping a node row;
  `CueBridge` fires `dmIn`. Chat header gains a "Manage channels"
  icon for discoverability (channel mgmt was wired but invisible
  from chat context). 107 app tests; analyze clean.
- **Brief refresh** (parent docs, separate from submodule): the
  UX brief now embeds a **per-theme rendering plan** (R18 grid +
  U6 cues + R13 haptic parity) for all 6 concepts, generated
  idempotently from `brand/_render_plan.py`.
- **Test gotcha discovered & recorded**: `Future.delayed` inside
  `testWidgets` hangs (binding's fake clock advances only via
  `pump()`); pattern = pumpWidget → connect/emit → `pump()`.
  Memorialised in `flutter-build-test-gotchas`.
- **Design rationale captured — Fabric / Contact / Known split**:
  spec gains a *Design rationale* sub-block under Terminology
  asserting the split is **our reasoning from first principles**
  (notification spam, sync semantics, flood-displacement of
  favourites, write churn, UI saturation), **not derived** from
  the official MeshCore / Meshtastic apps. Anchor: one day of
  ambient use of the official MeshCore app filled its ~350-contact
  cap from passive adverts and over-notified about being full —
  the "contact" bucket there means *"anything heard"*, not
  *"a relationship"*. Our response is to **rename the bucket**
  (Fabric for seen, Contact only for favourited, Known for direct
  comms) rather than match or raise their cap. Detailed dense-area
  polish design captured in task #36 (R18 density filters,
  clustering, ripple cap, FabricStore LRU → F8 fabric survey).

### Open crypto items

1. **Channel secret tail** — `GroupChannel.secret[16..32]`.
   **✅ CLOSED (M8).** Authoritative: `docs.meshcore.io/companion_
   protocol` ("32-byte variant unsupported") + the wirehack7
   packet-builder gist ("PSK + zero pad to 32 bytes"); verified
   locally. The 32-byte secret is **`psk ‖ 0x00·16`** — exactly
   `channelSecretFromPsk`. Also corrected (M8): the on-air channel
   hash is `SHA256(psk16)[0]` (`PATH_HASH_SIZE`=1), keyed on the
   16-byte PSK, not the 32-byte secret. The M6 on-device oracle
   remains as a regression anchor, no longer the sole proof.
2. **`ed25519_key_exchange` exact bytes** — *largely closed in M3b*:
   anchored to RFC 8032/7748 **and** offline libsodium KATs (the
   conversion, full key-exchange, and DH symmetry all match libsodium
   byte-for-byte; output is raw/unhashed, matching orlp). Residual:
   confirm against a *real MeshCore device* DM exchange in M6 (low
   risk — libsodium is the reference implementation for this map).

Item #2 reverse-engineering path: source archaeology → reference-
client differential → MAC oracle on a T1000-E (M6). (Item #1 needed
none of this in the end — the external docs/gist settled it.)

External references used to close #1 (M8):
- Official companion protocol — https://docs.meshcore.io/companion_protocol/
- wirehack7 GRP_TXT packet-builder gist —
  https://gist.github.com/wirehack7/1c2b3fa04886705aee0b6e3d42570e6f

Commit hashes refer to the `meshmore-sns` branch of the
`flutter-responsive-mobile-app-starter-iotj` submodule.

### Risks and mitigations

- **Protocol drift** — opcodes/structs change with firmware. *Mitigation:*
  pin a commit, transcribe constants in one place, bump deliberately,
  keep interop fixtures as regression anchors.
- **BLE MTU / fragmentation** — companion frames can exceed the BLE MTU.
  *Mitigation:* reassembly handled in the framing layer with explicit
  tests for split frames.
- **Crypto exactness** — KDF, nonce/IV construction, and AES mode must
  match the firmware precisely. *Mitigation:* KATs derived from firmware,
  not assumptions.
- **Device/version capability gating** — some features depend on device
  role or firmware version. *Mitigation:* capability/version negotiation
  from the self-info/version response before exercising optional commands.

## Future

- F1: Live map — likely reframed/superseded by R18's hyperlocal grid.
- F2: **Alternatives to maps for hyperlocal discovery** — now
  **concretely specified as R18** (hyperlocal grid, hybrid
  positioning). F2 is satisfied by R18; this entry stays as the
  umbrella for any *further* non-map discovery ideas.
- F3: **AI features for integrations** — the user has ideas for AI
  integrations (revisit; details TBD from user). Scope, on/offline
  boundary, and privacy posture to be defined before any design.
- F4: **Battery analysis graph** — a one-line current-usage trace
  plus an estimated remaining-life projection from the observed
  usage pattern. Backed by an internal **known-LoRa-device
  database** encoded in IoTone *Universal Device Metadata*
  (https://github.com/IoTone/IoToneSpec_UniversalDeviceMetadata/blob/master/IOTONE_SPEC_1.md);
  hosted in-app for now, semicomplete specs generated from
  datasheets, external generation tooling formalised later by the
  user. **Reference device available: Seeed SenseCAP T1000-E.**
  (Builds on R16 battery polling.)
- F5: **"Partyline"** — the app exposes a BLE characteristic that
  external devices can read/write to queue send/receive on the
  currently selected channel, bridging through the phone's
  connected MeshCore radio. Explore later.
- F6: **TCP/WiFi companion transport** — connect to a WiFi-capable
  MeshCore node over the binary companion protocol on TCP
  (default port 4403), as an alternative to BLE. The companion
  protocol is transport-agnostic and `MeshcoreTransport` already
  isolates the carrier, so this is a `TcpMeshcoreTransport`
  alongside `BleMeshcoreTransport` — no codec/controller changes.
  **TCP is not required by the protocol; BLE-only is fully
  conformant.** Official-app parity for WiFi nodes.
- F7: **MQTT bridge interop** — optional **online** capability:
  consume/publish mesh traffic via an MQTT broker (the ecosystem's
  live-map / Home-Assistant / dashboard pattern; done by
  gateways/bridges, not the companion app itself). Explicitly at
  odds with the offline-first core — keep clearly separated and
  opt-in. Explore later.
- F8: **Fabric survey grid** — a planning view that renders the
  mesh **fabric** (nodes seen + observed connectivity) as either a
  *solid grid* (good coverage) or *patchwork with holes* (gaps);
  surfaces true network viability/connectivity, which the official
  app's map view does not. Distinct from R18's notification grid
  (event-driven, recency-decayed): F8 is *coverage-survey* —
  aggregates observed adverts/SNR/RSSI/hops over time. Builds on
  R18's positioning primitives + the F4 device DB. Explore later.

> F2–F8 are **flagged to revisit**, not specified yet — capture
> intent now, design later once the user details them.
 
## Test

Real-hardware test cases run against the reference setup in
*Meshcore Protocol Implementation Plan → Initial real-hardware test
configuration* (Flutter Android + Seeed T1000-E nodes on MeshCore).

- TC1: Connect to network — app establishes a BLE companion link to a
  Meshcore device and completes the app-start/self-info handshake. (M5/M6)
- TC2: Meshcore protocol parser passes all conformance tests — the full
  conformance suite (vector goldens + round-trip + crypto KATs +
  negative/fuzz + interop fixtures) is green and enforced as the merge
  gate. (M1–M4, finalized M7)
- TC3: Can send messages — channel and direct messages are sent and
  acknowledged against a real device. (M2/M3/M6)
- TC4: Can handle encrypted and unencrypted data — public/unencrypted and
  channel/DM-encrypted payloads round-trip correctly. (M2/M3/M4/M6)
- TC5: (Manual test) Verify app detects disconnect when device is offline
  — ✅ **PASSED on hardware 2026-05-17** (user-verified; reconnect
  latency very low, worked well)
- TC6: (Manual test) Verify app detects reconnect when device is back online
  — ✅ **PASSED on hardware 2026-05-17** (user-verified; very low
  latency)
