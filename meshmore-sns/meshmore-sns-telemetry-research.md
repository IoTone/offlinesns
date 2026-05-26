# Telemetry research — using device altitude instead of phone GPS

**Status:** research only, no code shipped yet. Reviewed against
`ripplebiz/MeshCore` `main` (companion_radio firmware).
**Date:** 2026-05-26

## Goal

R45 elevation-profile view currently plots ME from phone-GPS altitude.
Better: use the LoRa device's own GPS altitude (T1000-E has GPS; many
nodes do). MeshCore already broadcasts altitude as part of its
telemetry data path. We just haven't wired that path into the app.

## What we have today

Only the *telemetry mode byte* — `CMD_SET_OTHER_PARAMS` (0x26) packs a
byte that tells the device whether/what to broadcast. We never read
the actual telemetry data the device emits or that peers emit.

## What MeshCore actually provides

### Opcodes (from `examples/companion_radio/MyMesh.cpp`)

| Direction | Name | Hex | Notes |
|---|---|---|---|
| App → device | `CMD_SEND_TELEMETRY_REQ` | **0x27** (39) | Bytes 1–3 reserved; `len==4` → self telemetry; `len==36` (4 + 32-byte pubkey) → query a peer over the air |
| Device → app | `PUSH_CODE_TELEMETRY_RESPONSE` | **0x8B** | Frame: `[0x8B][reserved 1B][6B pubkey-prefix][CayenneLPP payload]` |
| App → device | `CMD_SEND_BINARY_REQ` | **0x32** (50) | Newer; firmware comment says telemetry req "can deprecate, in favour of" this. Pair: `PUSH_CODE_BINARY_RESPONSE` (0x8C). Out of scope for v1. |

For *self* telemetry the device returns immediately (no air round-trip,
no queueing). For *peer* telemetry the device sends a unicast over the
air; the response arrives later as a separate push.

### Payload encoding — CayenneLPP

Firmware (`SensorManager::querySensors`) builds a `CayenneLPP&` buffer.
For position data the call is:

```cpp
telemetry.addGPS(TELEM_CHANNEL_SELF, node_lat, node_lon, node_altitude);
```

with `TELEM_CHANNEL_SELF = 1`. `addGPS` emits the **standard Cayenne
LPP GPS entry** — 11 bytes:

```
[channel:1][type=0x88:1][lat:3 s24 BE ÷10000][lon:3 s24 BE ÷10000][alt:3 s24 BE ÷100]
```

Altitude divisor is **100** → unit is metres × 100, signed 24-bit, big
endian. Range: ±83 886.07 m (way more than we need).

The full telemetry payload is a concatenation of channel/type/value
triplets — one CayenneLPP "entry" per sensor. The GPS entry is the
one we care about; other entries (battery via `addAnalogInput`,
temperature, humidity, pressure, etc.) are skippable in v1.

### Permissions

`querySensors(uint8_t requester_permissions, ...)` filters by:

```
TELEM_PERM_BASE        = 0x01   // battery + uptime
TELEM_PERM_LOCATION    = 0x02   // GPS entry
TELEM_PERM_ENVIRONMENT = 0x04   // BME / DHT / etc.
```

For *self* telemetry the device-side code passes the full mask, so we
get everything the chip has. For *peer* telemetry the responder
applies its own privacy policy (out of our control).

## Proposed data model (Dart side)

### New types

- `lib/meshcore/cayenne_lpp.dart`
  - `class LppEntry { int channel; int type; List<num> values; }`
  - `Iterable<LppEntry> decodeLpp(Uint8List bytes)` — parser; emits
    one entry per type byte. Supports at minimum type 0x88 (GPS).
    Unknown types skipped by length-table lookup; if a type isn't in
    the table we stop parsing (Cayenne LPP has no length prefix per
    entry — must know the type to advance the cursor).
- `lib/meshcore/telemetry.dart`
  - `class NodeTelemetry { double? lat, lon, altMeters; int? batteryMv; DateTime receivedAt; }`
  - Convenience builder from a list of `LppEntry`.

### Codec additions (`packages/meshcore/lib/src/codec/frame_codec.dart`)

- Encoder: `Uint8List sendTelemetryReq({Uint8List? pubKey})` →
  `[0x27][0x00][0x00][0x00]` for self, plus 32 bytes when querying a
  peer.
- Decoder: new push case `0x8B` →
  `TelemetryResponseFrame(pubKeyPrefix6, lpp: Uint8List)`.

### Controller additions (`lib/meshcore/meshcore_controller.dart`)

- Self-telemetry poll: piggyback on the existing ready-state probe
  cascade (`_requestCustomVars`, `_startBatteryPolling`, etc.) and
  add a single `sendTelemetryReq()` on ready, then re-poll on the
  same cadence as battery (every N minutes).
- Cache: `Map<String /*pubKey6*/, NodeTelemetry> _telemetry` plus a
  `selfTelemetry` slot (pubkey-prefix matches own pubkey6).
- `ownLocation` getter — change priority order:
  1. Device-reported lat/lon from SelfInfo, altitude from
     `selfTelemetry?.altMeters` if present.
  2. Self-telemetry alone (if it has GPS but SelfInfo lat/lon was 0,0
     — unlikely but possible).
  3. Phone fix fallback (unchanged behaviour, but only when neither
     of the above produced an altitude).
- Peer altitude: `nodeTelemetry(pubKeyHex)` accessor. Elevation view
  uses it to draw real altitudes for peers instead of the hash-band
  "?" zone.

### What stays on phone GPS

- Cases where the device has no GPS hardware (a static repeater
  without a chip) or hasn't acquired a fix yet — phone fix is still
  the only altitude source.
- Auto-publish (R36) still uses phone fix as the source of lat/lon
  *we push to the device*; that's separate from reading telemetry
  the device emits. The device, given lat/lon by us, will report
  back via telemetry with whatever altitude its own GPS thinks (or
  no altitude if it has no GPS chip — in that case the GPS LPP
  entry's altitude bytes will be 0, which we should treat as "no
  data" not "sea level"). **Edge case to handle:** distinguish
  "altitude = 0" from "no altitude" — likely by also checking lat/lon
  in the same triplet; if all three are 0 the entry is unset.

## Open questions

1. **Telemetry permission bits on the requester side.** The 3 reserved
   bytes in the `CMD_SEND_TELEMETRY_REQ` frame may carry a permission
   mask in newer firmware. Today the parser ignores them, so sending
   zeros is safe; but if firmware evolves to use them we should be
   ready to set the LOCATION bit (0x02). **Action:** send zeros for
   now, revisit if peers start refusing requests.

2. **Self-telemetry latency.** Firmware says self path is immediate.
   Need to confirm in-app that calling `sendTelemetryReq()` right
   after ready actually returns within the same BLE notify cycle and
   not 5–10 s later (which would matter for the elevation view's
   "altitude available on mount" UX).

3. **Cadence.** Polling self-telemetry every battery cycle (~minutes)
   is probably overkill — altitude doesn't change fast. A single
   poll on ready, plus a re-poll when the elevation view mounts, may
   be enough. To decide after wiring.

4. **Peer telemetry consent / cost.** Querying a peer's telemetry
   costs an OTA round-trip and consumes their permission policy. The
   elevation view should *not* automatically query every visible
   peer's telemetry — that would flood the channel. Per-peer
   on-demand only (e.g. tap a peer in the elevation view → "show
   altitude"), with a cache TTL to avoid re-querying within minutes.

5. **Other LPP types worth decoding.** Battery, temperature, pressure
   would unlock real telemetry chips in the UI. Out of scope for the
   altitude task but the decoder should be extensible (length table,
   not switch).

## Risks

- **Speculative opcodes:** all opcodes here are confirmed against
  upstream `main` (commit-tracked by `gh api`). Low risk.
- **CayenneLPP altitude divisor:** confirmed against the standard
  (Adafruit/MyDevices spec, ÷100 for altitude). The firmware uses the
  stock library so this matches.
- **Endianness:** Cayenne LPP is big-endian, signed magnitude is
  two's-complement 24-bit. Standard parsing.
- **Test surface:** can unit-test the LPP decoder with a captured
  byte string (mock a `PUSH_CODE_TELEMETRY_RESPONSE` from a known
  lat/lon/alt). No device-in-the-loop needed.

## Recommended ship order (if approved)

1. CayenneLPP decoder (pure function + tests).
2. `CMD_SEND_TELEMETRY_REQ` encoder + `PUSH_CODE_TELEMETRY_RESPONSE`
   decoder in `frame_codec.dart` (+ tests).
3. `NodeTelemetry` cache on controller; self-telemetry poll on ready.
4. `ownLocation` priority change (altitude source = self-telemetry →
   phone fix). Existing test "phone fix is used as fallback when
   device unset" gets a new sibling test for "device telemetry
   altitude wins over phone fix altitude".
5. Elevation view: drop the `requestPhoneLocationFix` shortcut, read
   from `mc.selfTelemetry?.altMeters` first.
6. (Optional, separate) per-peer telemetry on tap, batched, with TTL.

Each step ships independently and bumps patch by one.
