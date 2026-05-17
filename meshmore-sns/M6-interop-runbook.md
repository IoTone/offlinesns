# M6 — On-device interop runbook

End-to-end validation on real hardware (TC1/TC3/TC4) **and** closing
the two open crypto items via a BLE-only capture. Requires physical
hardware, so this is an operator procedure; everything else (codec,
oracle, replay harness) is already built and `dart test`-green.

## Hardware

- Flutter **Android** phone (primary on-device target).
- **≥2 Seeed Studio T1000-E** flashed with MeshCore **companion**
  firmware, tag `companion-v1.15.0` (commit `dee3e26ac0…`), via
  <https://meshcore.co.uk/flasher.html>. Record the exact flashed
  version — it must match the pin in `packages/meshcore`.
- Optionally a Repeater-configured node for multi-hop
  (<https://meshcore.co.uk/wiki.html#124-repeater>).

Call them **A** (paired to the phone over BLE) and **B** (sender).

## Part 1 — TC1: connect

1. `cd meshmore-sns/meshmore_sns_app/responsive_starter_app && flutter run`
   on the Android phone (release or debug).
2. Grant Bluetooth (and, pre-Android-12, Location) permissions.
3. `MeshcoreController.connect()` → expect state
   `handshaking → ready` and `selfInfo` populated. **TC1 pass.**

## Part 2 — TC3/TC4: send / receive, encrypted + unencrypted

1. On both A and B set the **Public** channel
   (`name=Public`, `secret=8b3387e9c5cdea6ac9e5edbaa115cd72`) — via
   the app's `SET_CHANNEL`, the config tool, or a QR
   `meshcore://channel/add?...`.
2. From the phone (node A) send a Public-channel text; confirm B
   receives it. Send from B; confirm A surfaces a
   `ChannelMessageFrame`. **TC3 pass** (channel = encrypted OTA;
   companion delivers plaintext = TC4 encrypted path). Repeat with a
   direct message between A and B for the DM path.

## Part 3 — Close open item #1 (channel-secret tail), BLE-only

The firmware push `0x88` (`PUSH_CODE_LOG_RX_DATA`) carries the **full
raw OTA packet**, so no SDR is needed.

1. Ensure node A emits `0x88` (RF logging enabled in its firmware
   build/config). Frames arrive on the companion link automatically.
2. From **B**, send a Public-channel message with a **known exact
   text**, e.g. `M6 interop test 1`.
3. In the app, after the message arrives, call
   `controller.exportGrpTxtFixture(pskHex:
   "8b3387e9c5cdea6ac9e5edbaa115cd72", knownPlaintextUtf8:
   "M6 interop test 1", firmware: "<flashed version>")`.
   (`lastRfLogHex` auto-selects the most recent `0x88`.)
4. Save the returned JSON as
   `packages/meshcore/test/vectors/interop/public_msg_1.json`.
   Repeat for ≥2 distinct messages (`public_msg_2.json`, …) so the
   2-byte MAC match is unambiguous.
5. Run `cd packages/meshcore && dart test test/interop_replay_test.dart`.
   It prints, per fixture, `channel-secret tail = <hypothesis>` and
   asserts it resolves. That hypothesis **is the answer** — almost
   certainly `zeros`.
6. Bake it in: replace `MeshcoreChannelCrypto.channelSecretFromPsk`'s
   provisional zero-fill with the confirmed construction (likely a
   no-op — it already zero-fills), commit the fixtures, and update
   `meshmore-sns-spec.md` → *Open crypto items* (#1 closed).

## Part 4 — Corroborate item #2 (ECDH/DM exactness)

The ed25519/X25519 composition is already libsodium-anchored
byte-for-byte (M3b). For a real-device cross-check: capture a `0x88`
for a **direct** message between A and B whose seeds you control,
decrypt with `MeshcoreDmCrypto.deriveSharedSecret` +
`macThenDecrypt`, and confirm the known plaintext is recovered. Add
as a `dm_capture` fixture (extend the schema/harness if pursuing this
beyond the M3b anchor — currently low-risk residual).

## Acceptance

- TC1/TC3/TC4 observed on hardware (note in the spec progress log).
- ≥2 `grp_txt_capture` fixtures committed; `interop_replay_test`
  green and resolving the same hypothesis for all.
- Spec open-item #1 marked closed with the confirmed hypothesis.
