// Copyright (c) 2026 IoTone, Inc.
// SPDX-License-Identifier: MIT
package io.iotone.meshcore.frames;

/**
 * {@code RESP_CODE_NO_MORE_MESSAGES} (0x0A) — the sync drain is empty;
 * stop issuing {@code CMD_SYNC_NEXT_MESSAGE}.
 */
public record NoMoreMessagesFrame() implements MeshcoreInbound {

    @Override
    public String toString() {
        return "NoMoreMessagesFrame()";
    }
}
