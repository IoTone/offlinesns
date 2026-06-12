// Copyright (c) 2026 IoTone, Inc.
// SPDX-License-Identifier: MIT
package io.iotone.meshcore.frames;

import io.iotone.meshcore.model.BatteryStorage;

/**
 * {@code RESP_CODE_BATT_AND_STORAGE} (0x0C).
 *
 * @param battery battery voltage + storage usage readout
 */
public record BatteryStorageFrame(BatteryStorage battery)
        implements MeshcoreInbound {

    @Override
    public String toString() {
        return "BatteryStorageFrame(" + battery + ")";
    }
}
