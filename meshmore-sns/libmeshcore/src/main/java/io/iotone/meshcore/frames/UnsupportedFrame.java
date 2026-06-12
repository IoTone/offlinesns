// Copyright (c) 2026 IoTone, Inc.
// SPDX-License-Identifier: MIT
package io.iotone.meshcore.frames;

/**
 * A well-formed frame whose opcode this codec does not decode.
 *
 * <p><strong>Not a failure</strong> — carries the raw bytes so higher
 * layers can log/skip without data loss (forward compatibility with
 * newer firmware opcodes).</p>
 *
 * <p>The array is owned by the record — callers must not mutate it.</p>
 *
 * @param opcode the unrecognised leading opcode byte
 * @param raw    the complete frame bytes, including the opcode
 */
public record UnsupportedFrame(int opcode, byte[] raw)
        implements MeshcoreInbound {

    @Override
    public String toString() {
        return "UnsupportedFrame(0x" + Integer.toHexString(opcode) + ", "
                + raw.length + "B)";
    }
}
