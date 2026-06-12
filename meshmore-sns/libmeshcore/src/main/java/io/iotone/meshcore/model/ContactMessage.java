// Copyright (c) 2026 IoTone, Inc.
// SPDX-License-Identifier: MIT
package io.iotone.meshcore.model;

import io.iotone.meshcore.MeshcoreConstants;

import java.time.Instant;

/**
 * A received direct (contact) text message.
 *
 * <p>Unifies {@code RESP_CODE_CONTACT_MSG_RECV} (0x07) and its V3
 * variant (0x10). {@code snrDb} is {@code null} for the legacy 0x07
 * frame.</p>
 *
 * <p>Layouts (from {@code examples/companion_radio/MyMesh.cpp}, pinned
 * commit):</p>
 * <pre>
 * 0x07: [07][pubkey_prefix 6][path_len][txt_type][ts u32 LE]
 *       [sig_prefix 4 IF txt_type==2][text…]
 * 0x10: [10][snr*4 i8][rsvd][rsvd][pubkey_prefix 6][path_len]
 *       [txt_type][ts u32 LE][sig_prefix 4 IF txt_type==2][text…]
 * </pre>
 *
 * <p>The sender is identified by a 6-byte public-key prefix.
 * {@code txt_type == 2} ({@code TXT_TYPE_SIGNED_PLAIN}) carries a
 * 4-byte signature prefix. Arrays are owned by the record — callers
 * must not mutate them.</p>
 *
 * @param pubKeyPrefix    first 6 bytes of the sender's public key
 * @param pathLen         raw hop count, or 0xFF for flood
 * @param txtType         text type (2 = signed plain)
 * @param timestamp       sender timestamp (unix seconds, unsigned)
 * @param text            message text (UTF-8, lenient decode)
 * @param isV3            {@code true} if decoded from the V3 (0x10) frame
 * @param snrDb           signal-to-noise in dB (V3 only; {@code null}
 *                        for legacy)
 * @param signaturePrefix 4-byte signature prefix, present iff
 *                        {@code txtType == 2}; {@code null} otherwise
 */
public record ContactMessage(
        byte[] pubKeyPrefix,
        int pathLen,
        int txtType,
        long timestamp,
        String text,
        boolean isV3,
        Double snrDb,
        byte[] signaturePrefix) {

    /**
     * Returns whether the message arrived via flood routing
     * ({@code path_len == 0xFF}).
     *
     * @return {@code true} for flood-routed messages
     */
    public boolean isFlood() {
        return pathLen == MeshcoreConstants.PATH_LEN_FLOOD;
    }

    /**
     * Returns whether the message carried a signature prefix
     * ({@code txt_type == TXT_TYPE_SIGNED_PLAIN}).
     *
     * @return {@code true} when {@link #signaturePrefix()} is present
     */
    public boolean isSigned() {
        return signaturePrefix != null;
    }

    /**
     * Returns the sender timestamp as a UTC instant.
     *
     * @return {@link Instant} of {@link #timestamp()} seconds
     */
    public Instant timestampUtc() {
        return Instant.ofEpochSecond(timestamp);
    }

    @Override
    public String toString() {
        StringBuilder hex = new StringBuilder();
        for (byte b : pubKeyPrefix) {
            hex.append(String.format("%02x", b));
        }
        return "ContactMessage(from: " + hex + ", "
                + (isFlood() ? "flood" : "path:" + pathLen)
                + ", txt_type: " + txtType + ", v3: " + isV3
                + ", signed: " + isSigned() + ", text: \"" + text + "\")";
    }
}
