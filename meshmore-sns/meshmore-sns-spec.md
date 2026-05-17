# Overview

Meshmore SNS is a mobile client built in Flutter, and used to communicate on the Meshcore network.

## References

- Meshcore : https://meshcore.co.uk/
- LoRa : https://resources.lora-alliance.org/technical-specifications 
- Flutter : https://flutter.dev/
- Meshcore Github: https://github.com/meshcore-dev/MeshCore
- Meshcore Flasher : https://meshcore.co.uk/flasher.html
- Meshcore Wiki (Repeater setup, relevant to initial test config) : https://meshcore.co.uk/wiki.html#124-repeater

## Problem

While Meshcore protocol is open source, the client is not open source.  We want ta general purpose Meshcore compatible client available under a friendly academic open source license.
 

## Requirements

- R1: app is written in flutter
- R2: app runs on Android and iOS
- R3: app uses bluetooth to communicate with a Meshcore client device. 
- R4: app is localized to English and Japanese
- R5: app can perform TTS using the GPU on the phone and should have a TTS mode that can be turned on, off by default
- R6: app should support a small scrollable, collapsable chat interface that is using the active channel, and has an easy way to switch channels 
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
  `dee3e26ac081a5c668c69b66c16a6544a44ddc5b`.
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
  (`https://meshcore.co.uk/flasher.html`).
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

### Open crypto items (to confirm via M6 on-device interop fixture)

1. **Channel secret tail** — `GroupChannel.secret[16..32]`. Strong
   evidence (qr_codes.md: ecosystem only ever shares 16 bytes;
   struct zero-init) ⇒ zeros; `channelSecretFromPsk` zero-fills.
   Confirm with the channel-hash / MAC oracle (`Public` PSK is known).
2. **`ed25519_key_exchange` exact bytes** — *largely closed in M3b*:
   anchored to RFC 8032/7748 **and** offline libsodium KATs (the
   conversion, full key-exchange, and DH symmetry all match libsodium
   byte-for-byte; output is raw/unhashed, matching orlp). Residual:
   confirm against a *real MeshCore device* DM exchange in M6 (low
   risk — libsodium is the reference implementation for this map).

Reverse-engineering path for both: source archaeology → reference-
client (meshcore.py / meshcore.js) differential → channel-hash air
oracle → MAC oracle on a T1000-E (M6). The known `Public` channel
PSK makes the channel-hash oracle a one-packet test.

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

- F1: Live map
 
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
- TBD
