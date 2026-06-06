# Microclimate / "wx" — design (earmark)

**Status:** design only — earmarked, not built. Captured 2026-06-06.
**Owner concept:** the Hyperlocal Field grid's weather (**wx**) view.

## 1. Motivation

The mesh's own **environment telemetry is shelved** — the test-device
firmware emits no temperature/humidity/pressure (see the `weather_view.dart`
telemetry path; it's real but most field nodes report nothing). Yet people
*say* the weather constantly on channel ("raining hard in fukuoka", "28°
and clear here", "fog rolling in"). **Microclimate** mines that: it reads
chat on the selected channel, recognises weather language in the current
locale, and — when it can tell *where* the speaker is — pins a small
**microclimate bubble** on the hyperlocal grid.

It's the same bet the **place-inference engine** already makes (parse
meaning out of plain chat, offline, on-device) applied to weather. Build it
as a deliberate parallel to that subsystem.

Principles: **offline-first** (no network, no weather API), **passive**
(never sends anything), **language-aware** (EN + JA lock-step), **honest
about uncertainty** (confidence-gated; tap a bubble to see the source
messages).

## 2. What exists to build on (concrete)

- **Messages:** `MeshcoreController.messagesFor([idx])` (per channel, oldest
  first, cached) and the `incomingChannelMessages` stream. `ChatMessage`
  has `text`, `peerPubKeyHex`, `at`, `channelIdx`, `snrDb`.
- **Place inference:** `PlaceInferenceEngine.infer(message, originLat,
  originLon)` → `List<InferredPlace>` (≥0.80 confidence, region-scoped to
  ~300 km). `InferredPlace` carries `displayName/anchorName/lat/lon/
  confidence/cue/sourceSpan/bearingDegrees`. Controller exposes
  `inferredPlaces()` → `List<InferredMarker>` (reinforced by repeats, 1 h
  window). **Reuse this to locate the speaker.**
- **Sender → node:** `_resolveChannelSender(text)` parses a `"name: "`
  prefix to a `DiscoveredNode`; `DiscoveredNode.latitude/longitude/
  hasLocation` give a GPS anchor when present.
- **Real telemetry (optional cross-ref):** `telemetryFor(pubKeyHex)` →
  `NodeTelemetry.temperatureC/humidityPct/pressureHpa/hasEnvironment`.
- **Locale:** `LocaleController.locale` (+ `supported` = en, ja).
- **Surfaces:** `grid_screen.dart` has a `_GridViewMode` enum that already
  includes a `weather` mode; `weather_view.dart` renders telemetry-only
  weather today; `sns_cells_view.dart` already plots message traffic with
  toasts at sender lat/lon (a model for placing bubbles on the geo grid);
  the Hyperlocal dashboard radar is the brand home for a compact strip.

## 3. Data model

```
enum WxCondition { clear, clouds, fog, rain, snow, wind, storm, heat, cold }

class WxObservation {            // one weather mention, parsed from one msg
  final String sourceMsgId;
  final String? senderPubKeyHex;
  final Set<WxCondition> conditions;
  final double? temperatureC;    // normalised to °C (parse + convert)
  final String sourceSpan;       // matched text, e.g. "28° and clear"
  final DateTime at;
  final double confidence;       // lexicon-match strength 0..1
  // location, best-effort (see §5)
  final double? lat, lon;
  final String? placeName;       // InferredPlace.displayName or node name
  final WxLocationCue locationCue; // explicitPlace | senderGps | inferredSender | none
}

class Microclimate {             // aggregate shown as a bubble
  final String key;              // place/cell id
  final String label;            // "Fukuoka", "West of here", a node name
  final double? lat, lon;
  final WxCondition dominant;    // most-supported condition in window
  final double? tempC;           // latest / averaged
  final int mentions;            // reinforcement count (decays)
  final DateTime lastSeen;
  final double confidence;
  final bool measured;           // true if backed by real telemetry (§6)
}
```

Aggregation mirrors `InferredMarker`: group observations by place/cell,
**decay over a window** (default 2 h, configurable), **reinforce on
repeats**, pick the dominant condition + newest/averaged temp.

## 4. Lexicon (current-language keyword scan)

A per-language lexicon in a dedicated `lib/sns/weather_lexicon.dart`
(NOT ARB — these are matcher inputs, not UI strings), selected by
`LocaleController.locale`:

```
const Map<String, WxLexicon> kWxLexicons = { 'en': ..., 'ja': ... };
```

Categories (each maps surface terms → `WxCondition`), EN + JA:

| Condition | EN terms | JA terms |
|-----------|----------|----------|
| clear/sun | sun, sunny, clear, blue sky | 晴れ, 快晴, 日差し |
| clouds    | cloud(y), overcast, grey | 曇り, くもり, 雲 |
| fog       | fog(gy), mist(y), haze | 霧, もや, かすみ |
| rain      | rain(ing), drizzle, downpour, wet, shower | 雨, 小雨, 大雨, にわか雨 |
| snow      | snow(ing), sleet, hail, flurries | 雪, みぞれ, あられ |
| wind      | wind(y), gusty, breezy, gale | 風, 強風, 突風 |
| storm     | storm, thunder, lightning, typhoon | 嵐, 雷, 台風 |
| heat      | hot, scorching, sweltering, muggy, humid | 暑い, 蒸し暑い, 猛暑 |
| cold      | cold, freezing, chilly, frosty | 寒い, 冷える, 凍える |

Plus: **emoji** (☀️🌤☁️🌧❄️🌫⛈🌪) map directly; **negation** ("no rain",
"雨じゃない") suppresses; require word boundaries to avoid "sunday"/"hot
take" false hits (confidence penalty rather than hard block — show on tap).

**Temperature parse:** regex for number + unit — `°C` `C` `℃` `degrees`,
`°F` `F`, JA `度`/`℃`. Normalise to °C; keep the user's display unit by
locale (°F for en-US optional later). Capture ranges ("18–22°") → store
min/avg/max on the microclimate.

## 5. Locating the speaker (priority order)

1. **Explicit place in the same message** — run `PlaceInferenceEngine.infer`
   on the message; if a place ≥0.80 is found, use its lat/lon + displayName.
   ("raining in fukuoka" → Fukuoka.)
2. **Sender node GPS** — resolve sender via `_resolveChannelSender`; if the
   `DiscoveredNode.hasLocation`, anchor there. ("28° here" from a GPS node.)
3. **Inferred sender place** — the sender's strongest recent `InferredMarker`
   (where they've been talking *from*).
4. **None** — no bubble; the observation still feeds a channel-wide
   "ambient" summary chip (e.g. "mostly ☁️ · ~20°C") but isn't pinned.

Region-scoped already (place inference caps at ~300 km from origin), so a
mention of a far-away city won't drop a bubble on the local grid — it lands
in ambient or is dropped, matching the hyperlocal ethos.

## 6. Optional cross-reference with real telemetry

When a sender (or a node *at* the inferred place) has
`telemetryFor(...).hasEnvironment`, prefer the **measured** temperature and
flag the microclimate `measured = true` (render a small "·measured" tick).
This gracefully upgrades a text guess to a sensor reading wherever the
firmware *does* report — bridging to the shelved telemetry path without
depending on it.

## 7. UI

**Where:** the **wx** grid view (wire up the existing `_GridViewMode.weather`,
relabel "Microclimate"). Two layers:

- **Geo bubbles** — on the equal-grid / cells base (reuse `sns_cells_view`'s
  lat/lon→cell placement), pin a **microclimate bubble** per location:
  rounded chip with a condition glyph (☀/☁/🌧/🌫…), temp if known, a tiny
  freshness/▮▮▯ count, tap → the source messages. Skin-aware (MmTokens +
  VizPalette; bubble fill = condition-tinted but parity-safe with the glyph
  + label so colour is never the sole carrier).
- **Ambient strip** — a top/bottom band summarising un-placeable
  observations for the selected channel ("CH0 · mostly 🌧 · 18–22°C · 7
  mentions/2h").

**Hyperlocal dashboard tie-in (later):** a compact one-line microclimate
chip under the radar status rail showing the single strongest local
microclimate, tap → opens the wx view.

Empty state: "No weather chatter yet on this channel."

## 8. Settings / scope

- Per-channel enable, reusing the place-inference toggle pattern
  (`setPlaceInferenceEnabled` analogue) — but **default-on for the selected
  channel** per the request (not Public-only).
- Window length (default 2 h) and a confidence threshold in advanced.
- Units follow locale; manual °C/°F override later.
- **lobospeak/closed-network:** weather mining is benign, but keep it
  strictly **channel-scoped to the selected channel** and never cross
  channels; honour any closed-network scoping the way place inference does.

## 9. Suggested architecture (mirror place inference)

- `lib/sns/weather_lexicon.dart` — per-locale term tables + emoji + temp regex.
- `lib/sns/weather_inference.dart` — `WeatherInferenceEngine.scan(message,
  locale, {placeResolver})` → `List<WxObservation>` (pure, unit-testable).
- `lib/sns/microclimate_store.dart` — windowed aggregation +
  reinforcement/decay → `List<Microclimate>` (mirror `InferredPlaceStore`).
- `MeshcoreController`: `microclimates(int channelIdx)` getter + feed from
  `incomingChannelMessages` (and a one-time backfill over `messagesFor`).
- UI: `MicroclimateView` (the wx mode) + a `MicroclimateBubble` widget.

## 10. Phasing

- **P1** — lexicon (EN+JA) + `WeatherInferenceEngine.scan` + store +
  ambient strip on the wx view. No placement yet. (Pure-Dart + tests.)
- **P2** — speaker location (§5 priorities 1–3) + geo bubbles on the grid.
- **P3** — telemetry cross-reference + "measured" upgrade.
- **P4** — dashboard microclimate chip; units override; per-channel settings UI.

## 11. Risks / open questions

- **Keyword ambiguity / sarcasm** ("she's hot", "storming out of the room").
  Mitigate with word boundaries, condition co-occurrence, confidence gating,
  and always exposing source-on-tap. Never auto-act on a single low-confidence hit.
- **Wrong placement** worse than no placement → only pin a bubble when
  location confidence is high; otherwise ambient.
- **Mixed-language channels** — scan with the active locale lexicon first;
  consider scanning all supported lexicons (cheap) and tagging language.
- **Reconciling text vs telemetry** when they disagree — prefer measured,
  but show both on tap.
- **Naming:** the user calls the grid mode **"wx"**; surface label
  "Microclimate". Keep the existing telemetry `weather_view` or fold it in
  as the "measured" source.
