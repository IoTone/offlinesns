// Copyright (c) 2026 IoTone, Inc.
// SPDX-License-Identifier: MIT
package io.iotone.meshcore.frames;

import io.iotone.meshcore.model.RfLog;

/**
 * {@code PUSH_CODE_LOG_RX_DATA} (0x88) — raw received OTA packet plus
 * SNR/RSSI.
 *
 * @param log the RF capture; parse {@link RfLog#raw()} with
 *            {@link io.iotone.meshcore.model.OtaPacket#parse(byte[])}
 */
public record RfLogFrame(RfLog log) implements MeshcoreInbound {

    @Override
    public String toString() {
        return "RfLogFrame(" + log + ")";
    }
}
