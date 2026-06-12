# libmeshcore

Pure-Java client library for the **MeshCore companion-radio protocol**
— frame codec, CayenneLPP telemetry decoding, and the channel/DM/identity
cryptography. Android- and server-ready.

A faithful port of the hardware-verified Dart reference implementation
(`meshmore_sns_app/packages/meshcore`), pinned to MeshCore firmware
**`companion-v1.15.0`** (`dee3e26ac081…`). Both libraries are validated
against the **same JSON golden vectors** (`src/test/resources/vectors/`),
so they are provably byte-identical on every covered frame, cipher
composition, and key-exchange step.

> **Why this exists:** as of June 2026 there was no Java jar library for
> the MeshCore companion protocol — the closest JVM option is the Kotlin
> Multiplatform `Wavesonics/MeshCoreKmp`. This library provides a plain
> `java-library` jar with full javadocs, consumable from Java, Kotlin,
> or any JVM language, on Android (AGP 8+) or a server (JDK 17+).

## What's inside

| Package | Contents |
|---|---|
| `io.iotone.meshcore` | Protocol constants, command/response opcode tables, BLE UUIDs (`MeshcoreConstants`, `MeshcoreCommand`, `MeshcoreResponse`, `MeshcoreBle`) |
| `…meshcore.codec` | `MeshcoreFrameCodec` (encode commands / decode frames — **total**, never throws), `CayenneLpp` (full ElectronicCats 1.6.1 type table), `ByteCursor`/`FrameBuilder` |
| `…meshcore.frames` | The sealed `MeshcoreInbound` hierarchy — exhaustively `switch`-able decode results |
| `…meshcore.model` | `Contact`, `Advert`, `SelfInfo`, `DeviceInfo`, `ChannelMessage`, `ContactMessage`, `OtaPacket`, … (immutable records) |
| `…meshcore.crypto` | `ChannelCrypto` (AES-128-ECB + truncated HMAC — the firmware's wire scheme), `DmCrypto`, `IdentityCrypto` (Ed25519 verify/keygen, orlp-style `ed25519_key_exchange` ECDH) |
| `…meshcore.diagnostics` | `ChannelTailOracle` — corroborates the channel-secret construction against captured OTA packets |
| `…meshcore.transport` | `MeshcoreTransport` — the listener-based transport SPI (bring your own BLE/serial/TCP) |

## Quick start

```java
import io.iotone.meshcore.codec.MeshcoreFrameCodec;
import io.iotone.meshcore.frames.*;

// Encode: first frame after connecting.
byte[] hello = MeshcoreFrameCodec.appStart("MyApp");
transport.send(hello);

// Decode: one BLE notification = one frame. Decoding is total.
MeshcoreInbound in = MeshcoreFrameCodec.decode(frameBytes);
switch (in) {
    case SelfInfoFrame f -> System.out.println(f.selfInfo().name());
    case ChannelMessageFrame f -> System.out.println(f.message().text());
    case TelemetryResponseFrame f ->
        io.iotone.meshcore.codec.CayenneLpp.decode(f.lppPayload())
            .forEach(System.out::println);
    case DecodeFailure f -> System.err.println(f.error());
    default -> { /* other frames */ }
}
```

Channel crypto (e.g. decrypting a captured GRP_TXT packet):

```java
byte[] secret = ChannelCrypto.channelSecretFromPsk(
        MeshcoreConstants.publicChannelPsk());   // psk ‖ 0x00·16
byte[] plain = ChannelCrypto.macThenDecrypt(secret, macAndCiphertext);
// null = MAC failure; else block-padded plaintext.
```

## Building

```sh
./gradlew build        # compiles, runs the 98-test suite, packages
./gradlew javadoc      # full javadocs (doclint-clean, -Xwerror)
./gradlew publishToMavenLocal
```

The Gradle wrapper is committed — no local Gradle install needed. Run
with any JDK 17+ (`JAVA_HOME`); the build toolchain (Java 17) is
auto-provisioned via the foojay resolver if not installed. One
dependency:
`org.bouncycastle:bcprov-jdk18on` — used via the **lightweight math
API only** (`org.bouncycastle.math.ec.rfc8032/rfc7748`), with no JCE
provider registration, so it coexists with Android's bundled provider.
Symmetric crypto (AES-ECB, HMAC-SHA256, SHA-2) uses `javax.crypto`,
present on every Android API level.

### Android notes

- Java 17 language features (records, sealed types) are desugared by
  AGP 8+ / D8 — no minSdk constraint from this library.
- `MeshcoreBle` publishes the Nordic-UART-style service/characteristic
  UUIDs; implement `MeshcoreTransport` over your BLE stack
  (one notification = one frame, one write = one frame).

## Conformance & provenance

- **Pinned firmware:** every layout/constant is transcribed from
  `companion-v1.15.0` (commit `dee3e26ac…`) — do not edit a numeric
  value without re-reading the pinned sources.
- **Golden vectors:** `m1`–`m4` frame vectors + `m3b` libsodium
  conversion/ECDH KATs are shared verbatim with the Dart package.
- **Standards anchors:** NIST AES/SHA KATs, RFC 4231 HMAC, RFC 8032
  Ed25519, RFC 7748 X25519.
- **Field-verified quirks baked in:** the CayenneLPP decoder carries the
  full ElectronicCats 1.6.1 type table because the firmware *leads*
  every telemetry response with a voltage entry (0x74) — a base-table
  decoder stalls at entry #1 and silently drops all telemetry (found
  the hard way against a T1000-E; regression-tested here).

## License

MIT © 2026 IoTone, Inc.
