// Copyright (c) 2026 IoTone, Inc.
// SPDX-License-Identifier: MIT
package io.iotone.meshcore.frames;

/**
 * {@code PUSH_CODE_ACK} (0x82) — delivery acknowledgement for a sent
 * (direct) message.
 *
 * <p>Carries the 4-byte CRC tag the device reported as
 * {@link io.iotone.meshcore.model.MsgSent#expectedAck()}, so the client
 * can match it back to the originating message and mark it delivered.
 * Broadcast/channel messages are flood-routed and never produce an
 * ACK.</p>
 *
 * @param ackCrc the acknowledged message's CRC tag (u32 LE), equal to
 *               the {@code expectedAck} returned when the message was
 *               submitted
 */
public record AckFrame(long ackCrc) implements MeshcoreInbound {

    @Override
    public String toString() {
        return String.format("AckFrame(0x%08x)", ackCrc);
    }
}
