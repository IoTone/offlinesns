// Copyright (c) 2026 IoTone, Inc.
// SPDX-License-Identifier: MIT
package io.iotone.meshcore.diagnostics;

/**
 * Candidate constructions for the 32-byte {@code GroupChannel.secret}
 * from the 16-byte PSK the companion link carries.
 *
 * <p>The AES-128 key is always {@code secret[0..16] == psk} (the
 * firmware copies the 16 sent bytes there); only the HMAC-only tail
 * {@code secret[16..32]} is in question. The {@link #ZEROS} tail is the
 * authoritative answer (docs.meshcore.io + independent packet-builder
 * sources: 32-byte secret = {@code psk ‖ 0·16}); the oracle keeps the
 * alternatives as on-device corroboration / regression anchors.</p>
 */
public enum ChannelTailHypothesis {

    /**
     * {@code psk ‖ 0x00·16} — struct zero-init; the authoritative
     * construction (the ecosystem only ever shares 16 bytes).
     */
    ZEROS,

    /** {@code psk ‖ psk}. */
    PSK_REPEAT,

    /** {@code psk ‖ SHA256(psk)[0..16]}. */
    SHA256_LOW,

    /** {@code psk ‖ SHA256(psk)[16..32]}. */
    SHA256_HIGH
}
