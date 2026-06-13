# libmeshcore-android

Android/AndroidXR BLE transport and live integration test harness for
[libmeshcore](../libmeshcore) — exercises the full Java protocol stack
against a real MeshCore companion radio over BLE.

## What this module provides

| Class | Purpose |
|---|---|
| `AndroidBleTransport` | Implements `MeshcoreTransport` over Android BLE, backed by the Nordic Android BLE Library. Handles MTU negotiation, notification subscription, and the connection state machine. |
| `MeshcoreSession` | Drives the protocol lifecycle over any `MeshcoreTransport`: handshake, message-drain loop, frame dispatch to `SessionListener`. |
| `SessionListener` | Typed callbacks (channel messages, adverts, contacts, decode failures), dispatched on the Android main thread. |
| `SessionState` | `DISCONNECTED → CONNECTING → HANDSHAKING → READY`. |

`MeshcoreSession` is transport-agnostic — it works with any implementation of `MeshcoreTransport`, including a mock for unit testing.

## Running the live integration test

```sh
./gradlew :libmeshcore-android:connectedAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.deviceName="T1000-E"
```

**Prerequisites:**
1. One T1000-E flashed with MeshCore companion firmware `companion-v1.15.0`  
2. A connected Android or AndroidXR device with BLE enabled  
3. The `deviceName` argument must be a prefix of the radio's BLE advertisement name

**`deviceName` not provided → all live tests are skipped** (CI stays green without a radio).

### What the tests verify

| Test | Requires | Validates |
|---|---|---|
| `testA_Handshake` | Node A only | `CMD_APP_START` → `RESP_CODE_SELF_INFO` decoded correctly by libmeshcore |
| `testB_SendChannelMessage` | Node A only | Send path accepted without BLE error |
| `testC_ChannelMessageRoundTrip` | Node A + B | Inbound `ChannelMessageFrame` decoded correctly (skipped if B absent) |
| `testD_ExportInteropFixture` | Node A + B + RF logging on A | Writes `meshcore_interop_fixture.json` to device Downloads |

### Exporting an interop fixture (M6 runbook)

After `testD_ExportInteropFixture` runs successfully:

```sh
adb pull /sdcard/Download/meshcore_interop_fixture.json \
  ../libmeshcore/src/test/resources/vectors/interop/public_msg_1.json

# Then validate both libraries from the same fixture:
cd ../libmeshcore && ./gradlew test
```

This closes the channel-secret-tail open crypto item by running the
`InteropReplayTest` harness in both `libmeshcore` (JVM) and the Dart
package simultaneously.

## Building

```sh
./gradlew build      # compile + lint + package AAR
./gradlew assembleDebug   # compile only (no device needed)
```

**Toolchain:** JDK 17+, Android SDK API 35. The `gradlew` wrapper is
committed — no local Gradle installation needed.

**Key dependencies:**
- `io.iotone.meshcore:libmeshcore` — resolved via Gradle composite build  
  (no separate publish step; changes to libmeshcore are picked up immediately)
- `no.nordicsemi.android:ble:2.10.1` — Nordic Android BLE Library
- `androidx.test:runner`, `androidx.test.ext:junit` — Instrumented test runner

## Architecture

```
App / AndroidXR
       │
       ▼
  MeshcoreSession          ← protocol session (transport-agnostic)
       │ MeshcoreTransport SPI
       ▼
  AndroidBleTransport      ← BLE layer (Android/AndroidXR only)
       │ composes
       ▼
  BleManager (Nordic)      ← handles GATT, MTU, state machine
       │
       ▼
  BluetoothGatt (Android)  ← platform BLE stack
       │
       ▼
  T1000-E / MeshCore radio
```

`AndroidBleTransport` uses **composition** over inheritance for the
Nordic `BleManager` — this avoids method-name conflicts between
`BleManager`'s `final` methods (`isConnected()`, `close()`) and the
identically-named `MeshcoreTransport` interface methods.

## AndroidXR note

AndroidXR is based on Android 15 (API 35). The BLE transport code is
identical to standard Android — no XR-specific changes are needed in
the library layer. XR-specific rendering and spatial UI live in the
consuming app, not here.

`minSdk = 26` keeps the library compatible with Android 8.0+ while
covering AndroidXR (which is API 35+).

## License

MIT © 2026 IoTone, Inc.
