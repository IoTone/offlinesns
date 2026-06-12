// Copyright (c) 2026 IoTone, Inc.
// SPDX-License-Identifier: MIT
package io.iotone.meshcore.model;

/**
 * Decoded {@code RESP_CODE_SENT} (0x06) — send confirmation.
 *
 * <pre>
 * [06][flood_flag][expected_ack u32 LE][est_timeout_ms u32 LE]
 * </pre>
 *
 * @param isFlood      {@code true} = flood routed, {@code false} = direct
 * @param expectedAck  tag the device will report back in the matching
 *                     ACK push (u32; match against
 *                     {@link io.iotone.meshcore.frames.AckFrame#ackCrc()})
 * @param estTimeoutMs estimated round-trip timeout in milliseconds
 */
public record MsgSent(boolean isFlood, long expectedAck, long estTimeoutMs) {

    @Override
    public String toString() {
        return "MsgSent(" + (isFlood ? "flood" : "direct") + ", ack: "
                + expectedAck + ", timeout: " + estTimeoutMs + "ms)";
    }
}
