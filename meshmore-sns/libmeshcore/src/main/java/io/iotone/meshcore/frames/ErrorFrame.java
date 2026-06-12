// Copyright (c) 2026 IoTone, Inc.
// SPDX-License-Identifier: MIT
package io.iotone.meshcore.frames;

import java.util.OptionalInt;

/**
 * {@code RESP_CODE_ERR} (0x01).
 *
 * <p>Known error codes (firmware {@code MyMesh.cpp}): 1 =
 * unsupported command, 2 = not found, 3 = table full, 4 = bad state,
 * 5 = file I/O error, 6 = illegal argument.</p>
 *
 * @param code the optional trailing error byte, else empty
 */
public record ErrorFrame(OptionalInt code) implements MeshcoreInbound {

    @Override
    public String toString() {
        return "ErrorFrame(" + (code.isPresent() ? code.getAsInt() : "null")
                + ")";
    }
}
