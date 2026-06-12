// Copyright (c) 2026 IoTone, Inc.
// SPDX-License-Identifier: MIT
package io.iotone.meshcore.model;

import io.iotone.meshcore.MeshcoreConstants;
import io.iotone.meshcore.codec.ByteCursor;
import io.iotone.meshcore.codec.FrameTruncatedException;

import java.util.Arrays;

/**
 * A parsed MeshCore over-the-air packet (as carried raw inside an
 * {@link io.iotone.meshcore.frames.RfLogFrame}). Layout from
 * {@code docs/packet_format.md} (pinned commit):
 *
 * <pre>
 * [header 1][transport codes 4?][path-len 1][path N][payload …]
 * header: bits0-1 route, bits2-5 payload type, bits6-7 version
 * path-len: bits0-5 hop count, bits6-7 hash-size code (0→1B,1→2B,2→3B)
 * transport codes present iff route is TRANSPORT_FLOOD/TRANSPORT_DIRECT
 * </pre>
 *
 * <p>The array is owned by the record — callers must not mutate it.</p>
 *
 * @param routeType   route type (bits 0-1 of the header)
 * @param payloadType payload type (bits 2-5 of the header)
 * @param version     packet version (bits 6-7 of the header)
 * @param hopCount    hops recorded in the path
 * @param hashSize    per-hop hash size in bytes (1–3)
 * @param payload     raw payload bytes (type-specific; see
 *                    {@link #grpTxt()})
 */
public record OtaPacket(
        int routeType,
        int payloadType,
        int version,
        int hopCount,
        int hashSize,
        byte[] payload) {

    /**
     * Returns whether this packet carries a group (channel) text
     * payload.
     *
     * @return {@code true} for {@code PAYLOAD_TYPE_GRP_TXT}
     */
    public boolean isGrpTxt() {
        return payloadType == MeshcoreConstants.PAYLOAD_TYPE_GRP_TXT;
    }

    /**
     * Returns whether this packet carries an advert payload.
     *
     * @return {@code true} for {@code PAYLOAD_TYPE_ADVERT}
     */
    public boolean isAdvert() {
        return payloadType == MeshcoreConstants.PAYLOAD_TYPE_ADVERT;
    }

    /**
     * Returns whether this packet carries a direct text payload.
     *
     * @return {@code true} for {@code PAYLOAD_TYPE_TXT_MSG}
     */
    public boolean isTxtMsg() {
        return payloadType == MeshcoreConstants.PAYLOAD_TYPE_TXT_MSG;
    }

    /**
     * For {@code PAYLOAD_TYPE_GRP_TXT}, splits the payload into the
     * 1-byte channel hash and the {@code [MAC(2)]‖ciphertext} blob
     * (exactly the output of {@code Utils::encryptThenMAC}).
     *
     * @return the split payload, or {@code null} if this is not a
     *         GRP_TXT packet or the payload is too short
     */
    public GrpTxtPayload grpTxt() {
        if (!isGrpTxt()
                || payload.length < 1 + MeshcoreConstants.CIPHER_MAC_SIZE) {
            return null;
        }
        return new GrpTxtPayload(
                payload[0] & 0xFF,
                Arrays.copyOfRange(payload, 1, payload.length));
    }

    /**
     * For {@code PAYLOAD_TYPE_ADVERT}, the decoded advert (same payload
     * layout as the companion 0x80 push). Lets RF-log (0x88) captures
     * yield adverts <em>with</em> SNR/RSSI for "what's in my area"
     * scanning.
     *
     * @return the advert, or {@code null} if not an advert or malformed
     */
    public Advert advert() {
        return isAdvert() ? Advert.tryParse(payload) : null;
    }

    /**
     * Parses from the raw OTA bytes (everything after the
     * {@code [0x88][snr][rssi]} RF-log prefix).
     *
     * @param raw full OTA packet bytes
     * @return the parsed packet, or {@code null} on a malformed or
     *         truncated packet — never throws
     */
    public static OtaPacket parse(byte[] raw) {
        try {
            ByteCursor c = new ByteCursor(raw);
            int h = c.u8("pkt.header");
            int route = h & MeshcoreConstants.PKT_ROUTE_TYPE_MASK;
            int ptype = (h & MeshcoreConstants.PKT_PAYLOAD_TYPE_MASK)
                    >> MeshcoreConstants.PKT_PAYLOAD_TYPE_SHIFT;
            int ver = (h & MeshcoreConstants.PKT_VERSION_MASK) >> 6;
            if (route == MeshcoreConstants.ROUTE_TRANSPORT_FLOOD
                    || route == MeshcoreConstants.ROUTE_TRANSPORT_DIRECT) {
                c.bytes(MeshcoreConstants.TRANSPORT_CODES_SIZE,
                        "pkt.transportCodes");
            }
            int pl = c.u8("pkt.pathLen");
            int hop = pl & MeshcoreConstants.PATH_HOP_MASK;
            int code = (pl >> MeshcoreConstants.PATH_HASH_SIZE_SHIFT) & 0x03;
            if (code == 3) {
                return null; // reserved hash-size code
            }
            int hashSize = code + 1; // 0→1, 1→2, 2→3
            c.bytes(hop * hashSize, "pkt.path");
            byte[] payload = c.atEnd()
                    ? new byte[0]
                    : c.bytes(c.remaining(), "pkt.payload");
            return new OtaPacket(route, ptype, ver, hop, hashSize, payload);
        } catch (FrameTruncatedException e) {
            return null;
        }
    }

    @Override
    public String toString() {
        return "OtaPacket(route: " + routeType + ", type: " + payloadType
                + ", v" + (version + 1) + ", hops: " + hopCount
                + ", payload: " + payload.length + "B)";
    }
}
