// Copyright (c) 2026 IoTone, Inc.
// SPDX-License-Identifier: MIT
package io.iotone.meshcore.frames;

import java.time.Instant;

/**
 * {@code RESP_CODE_CURR_TIME} (0x09) — the device's clock.
 *
 * @param unixSeconds device time in unix seconds (unsigned u32)
 */
public record CurrentTimeFrame(long unixSeconds) implements MeshcoreInbound {

    /**
     * Returns the device time as a UTC instant.
     *
     * @return {@link Instant} of {@link #unixSeconds()}
     */
    public Instant utc() {
        return Instant.ofEpochSecond(unixSeconds);
    }

    @Override
    public String toString() {
        return "CurrentTimeFrame(" + unixSeconds + ")";
    }
}
