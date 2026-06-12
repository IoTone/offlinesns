// Copyright (c) 2026 IoTone, Inc.
// SPDX-License-Identifier: MIT
package io.iotone.meshcore.frames;

/**
 * {@code PUSH_CODE_TELEMETRY_RESPONSE} (0x8B) — the device delivered a
 * CayenneLPP telemetry payload, either for itself (immediate after a
 * self-telemetry {@code CMD_SEND_TELEMETRY_REQ}) or for a peer it
 * queried over the air on our behalf.
 *
 * <p>Wire layout:
 * {@code [0x8B][reserved 1B][6B pubkey-prefix][LPP payload]}. The
 * pubkey-prefix identifies <em>which</em> node the payload belongs to —
 * it matches the first 6 bytes of the originator's public key. For
 * self-telemetry the prefix is the device's own pubkey6.</p>
 *
 * <p>Decode {@code lppPayload} with
 * {@link io.iotone.meshcore.codec.CayenneLpp#decode(byte[])}. Note the
 * companion firmware leads the payload with a battery-voltage entry
 * (LPP type 0x74) — see the {@code CayenneLpp} class docs. Arrays are
 * owned by the record — callers must not mutate them.</p>
 *
 * @param pubKeyPrefix 6-byte public-key prefix of the reporting node
 * @param lppPayload   raw CayenneLPP bytes (possibly empty)
 */
public record TelemetryResponseFrame(byte[] pubKeyPrefix, byte[] lppPayload)
        implements MeshcoreInbound {

    @Override
    public String toString() {
        return "TelemetryResponseFrame(pub6=" + pubKeyPrefix.length
                + "B, lpp=" + lppPayload.length + "B)";
    }
}
