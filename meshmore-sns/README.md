# Meshmore SNS

An **open-source Flutter client for the [MeshCore](https://github.com/ripplebiz/MeshCore) LoRa
mesh network** — a companion app for tracker/radio devices like the Seeed
**T1000-E** that pair over BLE and exchange messages over the local mesh.
Offline-first, theme-driven, with a hyperlocal radar grid, per-channel chat,
1:1 DMs, and a device-first → phone-fallback location model.

> **Status — in development.** Source is here for review and contribution;
> there's no public release channel yet. 125 widget/unit tests passing,
> `flutter analyze` clean on Android + iOS Dart paths. Android FGS path
> for R17 is implemented but **unverified on-device** in our sandbox —
> see `/Users/dkords/.claude/...` memory or open issues.

---

## Screens at this stage

Honest UI mockups in the **SEELE Monolith** default theme. Real builds
will look the same modulo platform fonts.

| Dashboard | Hyperlocal grid | Chat |
|:--:|:--:|:--:|
| ![Dashboard](screens/dashboard.svg) | ![Hyperlocal grid](screens/grid.svg) | ![Chat](screens/chat.svg) |
| PEERS IN RANGE numeral · status slab · LOCATION tile with `source · device` once GPS acquired · recent-events feed | Radial range-rings, GPS-or-RSSI positioning, pulse/blink for known/contact, tap-to-detail, Pause/Play interval, Range slider | Channel chip strip, per-channel TTS toggle, long-press → Reply / Copy / Delete locally |

| Nodes | Device config (Identity / Advert) | First-run permissions |
|:--:|:--:|:--:|
| ![Nodes](screens/nodes.svg) | ![Device config](screens/device-config.svg) | ![First-run intro](screens/first-run.svg) |
| Fabric list with per-node distance estimate, IN RANGE chip, favourite star, tap to DM | **Advert location source** (None / Pinned / Device GPS) + Use phone location / Read device location buttons | Proactive Bluetooth ask + first-class Offline path |

The UX design brief covers the six concept themes (NERV, AG-HUD, Hyperlocal,
SEELE, DR Pop, Recon) with palette anchors and per-theme rendering plans.

- **Design brief (Markdown):** [`meshmore-sns-UX-brief.md`](./meshmore-sns-UX-brief.md)
- **Design brief (HTML — has the per-theme SVG grid mockups):** [`meshmore-sns-UX-brief.html`](./meshmore-sns-UX-brief.html)

---

## What's built

The full requirements list (R1–R27) and UX plan (U0–U15) live in:

- **Requirements spec:** [`meshmore-sns-spec.md`](./meshmore-sns-spec.md)
- **UX implementation spec:** [`meshmore-sns-UX-spec.md`](./meshmore-sns-UX-spec.md)

Highlights of what's already shipping:

- **Companion protocol over BLE** end-to-end: APP_START handshake, contact/advert
  ingestion, channel & DM send/receive, SET_ADVERT_NAME / SET_ADVERT_LATLON /
  SET_OTHER_PARAMS, auto-reconnect with backoff (R17), Android foreground service
  for background keep-alive.
- **Per-channel chat (R6)** + **1:1 DMs (R6 extended)** with persisted history,
  per-message Reply/Copy/Delete (R20), per-channel TTS toggle (R5).
- **Hyperlocal grid (R18 / U9)** — radial range-rings with hybrid GPS → RSSI →
  hash-bearing positioning, recency-fade brightness, pulse for *known*, rapid
  blink for *favourite*, anonymous-channel ripple, reduce-motion fallback,
  tap-to-select detail sheet, Pause/Play with selectable interval, six-stop
  Range slider (Room → Wide).
- **Device-first / phone-fallback location (R22)** — `SelfInfo.latitude/longitude`
  populate `mc.ownLocation` directly; if the device reports `(0, 0)` we fall back
  to a one-shot `geolocator` fix. Dashboard surfaces source + coordinates;
  Nodes rows show inline distance estimates.
- **First-run permissions UX (R21)** with proactive Bluetooth ask, just-in-time
  Notifications (only when "Stay connected in background" is enabled), and a
  fully-supported **offline mode**.
- **Per-theme presets** wired through tokens (`lib/theme/mm_tokens.dart`); the
  brief covers the visual identity per theme.
- **Diagnostics** screen with a bounded RAW FRAME LOG scroll (R23) for live
  protocol inspection.

Forward-looking but already speccd (not built):

- **R24** per-channel notification settings.
- **R25** reverse-geocoded equal-grid map view.
- **R26** XR data streaming via private temp-URL reverse proxy.
- **R27** Globe.GL macro view inside a WebView with a pre-cached macro basemap.

---

## Building & running

```bash
cd meshmore_sns_app/responsive_starter_app
flutter pub get
flutter run            # debug on a connected device
flutter analyze        # static analysis
flutter test           # 125 tests at time of writing
```

**Submodule note** — the `meshcore` codec lives at
`meshmore_sns_app/packages/meshcore` and is consumed via a `path:`
dependency in `pubspec.yaml`. Tests in the responsive app reach the
codec directly.

---

## Repository layout

```
meshmore-sns/
├── README.md                       ← this file
├── meshmore-sns-spec.md            ← R1–R27 requirements
├── meshmore-sns-UX-spec.md         ← U0–U15 implementation plan
├── meshmore-sns-UX-brief.md/.html  ← visual design brief
├── brand/                          ← logo work + mockup generators
│   ├── _app_screens.py             ← regenerates screens/*.svg
│   ├── _grid_mockup.py             ← per-theme grid mockups in the brief
│   ├── _render_plan.py             ← per-theme rendering plan in the brief
│   └── _apply_headers.py           ← MIT/SPDX header sweep
├── screens/                        ← README screen mockups
└── meshmore_sns_app/               ← the Flutter project (git submodule)
    ├── packages/meshcore/          ← pure-Dart protocol codec
    └── responsive_starter_app/     ← Flutter app
        ├── lib/                    ← all controllers, screens, theme, perms…
        ├── test/                   ← 125 widget/unit tests
        ├── android/                ← FGS manifest, BLE perms
        └── ios/                    ← Info.plist usage strings
```

---

## Attribution

### MeshCore (the protocol + the firmware this app talks to)

- **MeshCore project**: <https://github.com/ripplebiz/MeshCore> — the LoRa
  mesh-network protocol + firmware that runs on the radio devices this app
  pairs with (T1000-E etc.). Our [`packages/meshcore/`](./meshmore_sns_app/packages/meshcore/)
  is a Dart re-implementation of the companion-protocol codec, written against
  the public source.
- Hardware reference: **Seeed Studio T1000-E** tracker device used as the
  primary on-device test target.

### Flutter dependencies (all MIT / BSD / Apache-2.0)

| Package | Use |
|---|---|
| [`flutter`](https://flutter.dev) | Framework + Material widgets |
| [`provider`](https://pub.dev/packages/provider) | State + DI |
| [`go_router`](https://pub.dev/packages/go_router) | Routing |
| [`flutter_blue_plus`](https://pub.dev/packages/flutter_blue_plus) | BLE link to the MeshCore device |
| [`permission_handler`](https://pub.dev/packages/permission_handler) | OS permissions (Bluetooth, notifications, location) |
| [`geolocator`](https://pub.dev/packages/geolocator) | Phone-GPS one-shot fallback (R22) |
| [`flutter_foreground_task`](https://pub.dev/packages/flutter_foreground_task) | Android `connectedDevice` FGS for background keep-alive (R17 / U8) |
| [`flutter_tts`](https://pub.dev/packages/flutter_tts) | Text-to-speech for inbound channel messages (R5) |
| [`shared_preferences`](https://pub.dev/packages/shared_preferences) | Local prefs (theme, favourites, known set, first-run, chat history) |
| [`flutter_native_splash`](https://pub.dev/packages/flutter_native_splash) | Launch splash |
| [`cupertino_icons`](https://pub.dev/packages/cupertino_icons) | iOS-style glyphs |
| [`responsive_toolkit`](https://pub.dev/packages/responsive_toolkit) | Breakpoint helpers |
| [`package_info_plus`](https://pub.dev/packages/package_info_plus), [`device_info_plus`](https://pub.dev/packages/device_info_plus) | App/device introspection (About screen, Diagnostics) |
| [`intl`](https://pub.dev/packages/intl) | Locale + formatting |
| [`flutter_localizations`](https://api.flutter.dev/flutter/flutter_localizations/flutter_localizations-library.html) | L10n scaffolding (R4) |
| [`flutter_lints`](https://pub.dev/packages/flutter_lints) | Lint config (dev) |
| [`flutter_launcher_icons`](https://pub.dev/packages/flutter_launcher_icons) | Build-time icon generation (dev) |

### External tools / libraries referenced but not yet integrated

- [**Globe.GL**](https://globe.gl/) — referenced by **R27** (future Globe macro
  view inside a WebView).
- [**OpenStreetMap**](https://www.openstreetmap.org/) raster tiles — one
  candidate basemap source for **R25** (future reverse-geocoded equal-grid map).
- [**Natural Earth**](https://www.naturalearthdata.com/), [**GeoNames**](https://www.geonames.org/) — public
  data sources we'd consider for a custom minimal tile DB (R25 alternative path).

### Design inspirations

The brief's six concept themes draw aesthetic inspiration from a range of
cultural / industrial sources. **No copyrighted assets are used** — palette
anchors, glyph language, and motifs are our own implementation in Flutter
tokens (`lib/theme/mm_tokens.dart`); the names below are credits for the
visual lineage:

- **NERV Terminal / SEELE Monolith** — *Neon Genesis Evangelion* (Khara).
- **AG-HUD Velocity** — *Aegis-class* combat-system HUDs (generic naval / pilot
  HUD idiom).
- **Hyperlocal Field** — sonar / hydrology display conventions.
- **DR Pop / Pure Phase** — halftone-pop poster idiom; the *shipped logo*
  builds from this concept.
- **Tactical Recon / Codec** — *Metal Gear* codec-screen idiom (Konami).

Fonts default to the platform's system family (no bundled fonts).

### Test fixtures + interop replay

Some test vectors under `packages/meshcore/test/vectors/` are captured from
public MeshCore traffic between reference firmware builds, used as
known-good interop fixtures. The captures are byte-for-byte
deterministic and contain no PII.

---

## License

[MIT](./meshmore_sns_app/LICENSE) — Copyright (c) 2026 IoTone, Inc.

The protocol codec at `meshmore_sns_app/packages/meshcore/` is published under the
same MIT license; see its [`LICENSE`](./meshmore_sns_app/packages/meshcore/LICENSE).
