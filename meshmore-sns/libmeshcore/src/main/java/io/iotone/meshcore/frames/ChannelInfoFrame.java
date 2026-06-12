// Copyright (c) 2026 IoTone, Inc.
// SPDX-License-Identifier: MIT
package io.iotone.meshcore.frames;

import io.iotone.meshcore.model.ChannelInfo;

/**
 * {@code RESP_CODE_CHANNEL_INFO} (0x12) — reply to GET_CHANNEL.
 *
 * @param info the decoded channel slot (index, name, 16-byte PSK)
 */
public record ChannelInfoFrame(ChannelInfo info) implements MeshcoreInbound {

    @Override
    public String toString() {
        return "ChannelInfoFrame(" + info + ")";
    }
}
