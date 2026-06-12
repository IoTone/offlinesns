// Copyright (c) 2026 IoTone, Inc.
// SPDX-License-Identifier: MIT
package io.iotone.meshcore.diagnostics;

/**
 * Outcome of running the channel-tail oracle against one captured
 * GRP_TXT packet.
 *
 * <p>The array is owned by the record — callers must not mutate it.</p>
 *
 * @param match               the hypothesis whose 32-byte secret
 *                            produced a MAC-valid decryption recovering
 *                            {@code recoveredPlaintext}, or {@code null}
 *                            when unresolved
 * @param channelHashOk       whether {@code SHA256(psk)[0]} equals the
 *                            packet's channel-hash byte — a
 *                            tail-independent sanity that the PSK
 *                            identifies this channel (the on-air hash is
 *                            keyed on the 16-byte PSK only)
 * @param recoveredPlaintext  the decrypted (de-padded to the known
 *                            length) plaintext for the matching
 *                            hypothesis, or {@code null}
 */
public record ChannelTailResult(
        ChannelTailHypothesis match,
        boolean channelHashOk,
        byte[] recoveredPlaintext) {

    /**
     * Returns whether a hypothesis matched.
     *
     * @return {@code true} when {@link #match()} is non-null
     */
    public boolean resolved() {
        return match != null;
    }

    @Override
    public String toString() {
        return resolved()
                ? "ChannelTailResult(MATCH: " + match + "; channelHashOk="
                        + channelHashOk + ")"
                : "ChannelTailResult(unresolved; channelHashOk="
                        + channelHashOk + ")";
    }
}
