// Copyright (c) 2026 IoTone, Inc.
// SPDX-License-Identifier: MIT
package io.iotone.meshcore.frames;

import io.iotone.meshcore.model.SelfInfo;

/**
 * {@code RESP_CODE_SELF_INFO} (0x05) — also the APP_START reply.
 *
 * @param selfInfo the decoded device self-description
 */
public record SelfInfoFrame(SelfInfo selfInfo) implements MeshcoreInbound {

    @Override
    public String toString() {
        return "SelfInfoFrame(" + selfInfo + ")";
    }
}
