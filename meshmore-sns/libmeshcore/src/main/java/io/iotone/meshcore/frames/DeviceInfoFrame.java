// Copyright (c) 2026 IoTone, Inc.
// SPDX-License-Identifier: MIT
package io.iotone.meshcore.frames;

import io.iotone.meshcore.model.DeviceInfo;

/**
 * {@code RESP_CODE_DEVICE_INFO} (0x0D) — reply to DEVICE_QUERY.
 *
 * @param info the decoded device description
 */
public record DeviceInfoFrame(DeviceInfo info) implements MeshcoreInbound {

    @Override
    public String toString() {
        return "DeviceInfoFrame(" + info + ")";
    }
}
