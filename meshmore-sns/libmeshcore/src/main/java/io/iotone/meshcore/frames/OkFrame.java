// Copyright (c) 2026 IoTone, Inc.
// SPDX-License-Identifier: MIT
package io.iotone.meshcore.frames;

import java.util.OptionalLong;

/**
 * {@code RESP_CODE_OK} (0x00).
 *
 * @param value the optional trailing uint32 (present on some replies,
 *              e.g. device time), else empty
 */
public record OkFrame(OptionalLong value) implements MeshcoreInbound {

    @Override
    public String toString() {
        return "OkFrame(" + (value.isPresent() ? value.getAsLong() : "null")
                + ")";
    }
}
