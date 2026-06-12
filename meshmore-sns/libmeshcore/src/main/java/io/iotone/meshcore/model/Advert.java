// Copyright (c) 2026 IoTone, Inc.
// SPDX-License-Identifier: MIT
package io.iotone.meshcore.model;

import io.iotone.meshcore.MeshcoreConstants;
import io.iotone.meshcore.codec.ByteCursor;
import io.iotone.meshcore.codec.FrameBuilder;
import io.iotone.meshcore.codec.FrameTruncatedException;

import java.util.OptionalInt;

/**
 * A decoded advertisement ({@code PUSH_CODE_ADVERTISEMENT}, 0x80).
 *
 * <p>Packet payload (from {@code src/Mesh.cpp}, pinned commit):</p>
 * <pre>
 * [pub_key 32][timestamp u32 LE][signature 64][app_data …]
 * </pre>
 *
 * <p>app_data ({@code AdvertDataHelpers}): {@code [flags]} then,
 * conditionally, {@code [lat i32][lon i32]} (ADV_LATLON 0x10),
 * {@code [feat1 u16]} (0x20), {@code [feat2 u16]} (0x40),
 * {@code [name UTF-8]} (0x80). The low nibble of {@code flags} is the
 * advert type (0=none, 1=chat, 2=repeater, 3=room, 4=sensor).</p>
 *
 * <p>The Ed25519-signed message is
 * {@code pub_key ‖ timestamp ‖ app_data}, exposed as
 * {@link #signedMessage()} for verification with
 * {@link io.iotone.meshcore.crypto.IdentityCrypto#verifyAdvert(Advert)}.</p>
 *
 * <p>Arrays are owned by the record — callers must not mutate them.</p>
 *
 * @param publicKey     32-byte Ed25519 public key (full node identity)
 * @param timestamp     advert emission time (unix seconds, unsigned)
 * @param signature     64-byte Ed25519 signature
 * @param appData       raw app_data bytes (flags + optional fields)
 * @param signedMessage {@code pub_key ‖ timestamp(4 LE) ‖ app_data} —
 *                      the exact bytes the device signed
 * @param flags         the app_data flags byte (0 when app_data empty)
 * @param latitude      degrees (raw int32 &#247; 1e6), or {@code null}
 *                      when ADV_LATLON was not set
 * @param longitude     degrees (raw int32 &#247; 1e6), or {@code null}
 *                      when ADV_LATLON was not set
 * @param feat1         feature-1 value, or empty when not present
 * @param feat2         feature-2 value, or empty when not present
 * @param name          advertised node name, or {@code null} when not
 *                      present
 */
public record Advert(
        byte[] publicKey,
        long timestamp,
        byte[] signature,
        byte[] appData,
        byte[] signedMessage,
        int flags,
        Double latitude,
        Double longitude,
        OptionalInt feat1,
        OptionalInt feat2,
        String name) {

    /**
     * Returns the advert node type ({@code flags & 0x0F}): 0=none,
     * 1=chat, 2=repeater, 3=room, 4=sensor.
     *
     * @return type nibble
     */
    public int type() {
        return flags & MeshcoreConstants.ADV_TYPE_MASK;
    }

    /**
     * Parses an advert payload —
     * {@code pub_key(32) ‖ ts(4 LE) ‖ sig(64) ‖ app_data}. Shared by the
     * companion 0x80 decoder and {@link OtaPacket#advert()} (the OTA
     * {@code PAYLOAD_TYPE_ADVERT} body has the same layout).
     *
     * @param payload raw advert payload bytes
     * @return the decoded advert
     * @throws FrameTruncatedException on a short/garbled payload; the
     *         callers turn that into a typed failure / {@code null}
     */
    public static Advert parse(byte[] payload) {
        ByteCursor c = new ByteCursor(payload);
        byte[] pubKey = c.bytes(MeshcoreConstants.PUB_KEY_SIZE, "advert.pubKey");
        long ts = c.u32("advert.timestamp");
        byte[] sig = c.bytes(MeshcoreConstants.SIGNATURE_SIZE, "advert.signature");
        byte[] appData = c.atEnd()
                ? new byte[0]
                : c.bytes(c.remaining(), "advert.appData");

        byte[] signedMessage = new FrameBuilder()
                .raw(pubKey)
                .u32(ts)
                .raw(appData)
                .build();

        int flags = 0;
        Integer latMicros = null;
        Integer lonMicros = null;
        OptionalInt feat1 = OptionalInt.empty();
        OptionalInt feat2 = OptionalInt.empty();
        String name = null;
        if (appData.length > 0) {
            ByteCursor a = new ByteCursor(appData);
            flags = a.u8("advert.flags");
            if ((flags & MeshcoreConstants.ADV_LATLON_MASK) != 0) {
                latMicros = a.i32("advert.lat");
                lonMicros = a.i32("advert.lon");
            }
            if ((flags & MeshcoreConstants.ADV_FEAT1_MASK) != 0) {
                feat1 = OptionalInt.of(a.u16("advert.feat1"));
            }
            if ((flags & MeshcoreConstants.ADV_FEAT2_MASK) != 0) {
                feat2 = OptionalInt.of(a.u16("advert.feat2"));
            }
            if ((flags & MeshcoreConstants.ADV_NAME_MASK) != 0 && !a.atEnd()) {
                name = a.utf8ToEnd("advert.name");
            }
        }
        return new Advert(
                pubKey,
                ts,
                sig,
                appData,
                signedMessage,
                flags,
                latMicros == null ? null : latMicros / 1e6,
                lonMicros == null ? null : lonMicros / 1e6,
                feat1,
                feat2,
                name);
    }

    /**
     * Total variant of {@link #parse(byte[])} — {@code null} instead of
     * throwing on a truncated payload.
     *
     * @param payload raw advert payload bytes
     * @return the decoded advert, or {@code null} when malformed
     */
    public static Advert tryParse(byte[] payload) {
        try {
            return parse(payload);
        } catch (FrameTruncatedException e) {
            return null;
        }
    }

    @Override
    public String toString() {
        return "Advert(type: " + type() + ", name: "
                + (name != null ? name : "-") + ", ts: " + timestamp
                + ", hasLoc: " + (latitude != null) + ")";
    }
}
