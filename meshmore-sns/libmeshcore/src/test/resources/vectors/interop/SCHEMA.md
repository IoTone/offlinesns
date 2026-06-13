# Interop fixture schema (M6)

Drop **real captured** device data here as `*.json` files. The
`test/interop_replay_test.dart` harness picks them up automatically:
absent → the test is skipped (CI stays green); present → it asserts
our codec/crypto against the real device and resolves the
channel-secret tail.

**Never hand-write or synthesise these — they must come from a real
T1000-E capture** (see `meshmore-sns/M6-interop-runbook.md`).

## `grp_txt_capture` — closes the channel-secret-tail open item

```json
{
  "kind": "grp_txt_capture",
  "description": "Public-channel msg from node B, captured on node A via 0x88",
  "firmware": "companion-v1.15.0 / dee3e26ac0 (Seeed T1000-E)",
  "channel_name": "Public",
  "psk_hex": "8b3387e9c5cdea6ac9e5edbaa115cd72",
  "known_plaintext_utf8": "M6 interop test 1",
  "rf_log_frame_hex": "88<snr><rssi><raw OTA packet…>"
}
```

`rf_log_frame_hex` is the **entire** `PUSH_CODE_LOG_RX_DATA` (0x88)
BLE notification, hex, exactly as received (including the leading
`88`). One file per captured message; ≥2 distinct messages are
recommended so the 2-byte MAC match is unambiguous.

The harness asserts `resolveChannelTail(...)` returns `resolved:true`
and reports which `ChannelTailHypothesis` the real device uses — that
is the answer to open item #1.
