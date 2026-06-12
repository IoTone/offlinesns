// Copyright (c) 2026 IoTone, Inc.
// SPDX-License-Identifier: MIT
package io.iotone.meshcore.model;

import io.iotone.meshcore.MeshcoreConstants;

import java.time.Instant;

/**
 * A received channel (group) text message.
 *
 * <p>Unifies {@code RESP_CODE_CHANNEL_MSG_RECV} (0x08) and its V3
 * variant (0x11). {@code snrDb} is {@code null} for the legacy 0x08
 * frame and set for V3.</p>
 *
 * <p>Layouts (from {@code examples/companion_radio/MyMesh.cpp}, pinned
 * commit):</p>
 * <pre>
 * 0x08: [08][ch_idx][path_len][txt_type][ts u32 LE][text…]
 * 0x11: [11][snr*4 i8][rsvd][rsvd][ch_idx][path_len][txt_type]
 *       [ts u32 LE][text…]
 * </pre>
 *
 * <p>{@code path_len == 0xFF} means the message arrived via flood
 * routing (see {@link #isFlood()}).</p>
 *
 * @param channelIdx channel slot index the message arrived on
 * @param pathLen    raw hop count, or 0xFF for flood
 * @param txtType    text type (see
 *                   {@link MeshcoreConstants#TXT_TYPE_PLAIN})
 * @param timestamp  sender timestamp (unix seconds, unsigned)
 * @param text       message text (UTF-8, lenient decode)
 * @param isV3       {@code true} if decoded from the V3 (0x11) frame
 * @param snrDb      signal-to-noise in dB (V3 only; {@code null} for
 *                   legacy 0x08)
 */
public record ChannelMessage(
        int channelIdx,
        int pathLen,
        int txtType,
        long timestamp,
        String text,
        boolean isV3,
        Double snrDb) {

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
     * Returns the sender timestamp as a UTC instant.
     *
     * @return {@link Instant} of {@link #timestamp()} seconds
     */
    public Instant timestampUtc() {
        return Instant.ofEpochSecond(timestamp);
    }

    @Override
    public String toString() {
        return "ChannelMessage(ch: " + channelIdx + ", "
                + (isFlood() ? "flood" : "path:" + pathLen)
                + ", txt_type: " + txtType + ", v3: " + isV3
                + ", snr: " + snrDb + ", text: \"" + text + "\")";
    }
}
