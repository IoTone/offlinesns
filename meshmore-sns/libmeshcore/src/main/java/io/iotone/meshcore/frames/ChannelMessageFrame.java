// Copyright (c) 2026 IoTone, Inc.
// SPDX-License-Identifier: MIT
package io.iotone.meshcore.frames;

import io.iotone.meshcore.model.ChannelMessage;

/**
 * {@code RESP_CODE_CHANNEL_MSG_RECV} (0x08) and its V3 variant (0x11).
 *
 * @param message the decoded channel message (V3 carries SNR)
 */
public record ChannelMessageFrame(ChannelMessage message)
        implements MeshcoreInbound {

    @Override
    public String toString() {
        return "ChannelMessageFrame(" + message + ")";
    }
}
