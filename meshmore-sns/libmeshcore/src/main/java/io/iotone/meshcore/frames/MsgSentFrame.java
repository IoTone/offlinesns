// Copyright (c) 2026 IoTone, Inc.
// SPDX-License-Identifier: MIT
package io.iotone.meshcore.frames;

import io.iotone.meshcore.model.MsgSent;

/**
 * {@code RESP_CODE_SENT} (0x06) — send confirmation.
 *
 * @param sent flood flag, expected-ACK tag and estimated timeout
 */
public record MsgSentFrame(MsgSent sent) implements MeshcoreInbound {

    @Override
    public String toString() {
        return "MsgSentFrame(" + sent + ")";
    }
}
