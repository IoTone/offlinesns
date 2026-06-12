// Copyright (c) 2026 IoTone, Inc.
// SPDX-License-Identifier: MIT
package io.iotone.meshcore.codec;

import java.util.OptionalInt;

/**
 * A structured decode-failure report carried by
 * {@link io.iotone.meshcore.frames.DecodeFailure}.
 *
 * <p>Never thrown — always returned as a value (the codec's "total
 * decode" invariant; see {@link DecodeErrorKind}).</p>
 *
 * @param kind    structural failure category
 * @param message human-readable detail (field context, byte counts)
 * @param opcode  the leading opcode byte when one was present, else empty
 */
public record MeshcoreDecodeError(
        DecodeErrorKind kind,
        String message,
        OptionalInt opcode) {

    /**
     * Convenience factory for a failure with no opcode (empty frame).
     *
     * @param kind    structural failure category
     * @param message human-readable detail
     * @return error value without an opcode
     */
    public static MeshcoreDecodeError of(DecodeErrorKind kind, String message) {
        return new MeshcoreDecodeError(kind, message, OptionalInt.empty());
    }

    /**
     * Convenience factory for a failure attributed to an opcode.
     *
     * @param kind    structural failure category
     * @param message human-readable detail
     * @param opcode  the frame's leading opcode byte
     * @return error value carrying the opcode
     */
    public static MeshcoreDecodeError of(
            DecodeErrorKind kind, String message, int opcode) {
        return new MeshcoreDecodeError(kind, message, OptionalInt.of(opcode));
    }

    @Override
    public String toString() {
        String op = opcode.isPresent()
                ? ", op=0x" + Integer.toHexString(opcode.getAsInt())
                : "";
        return "MeshcoreDecodeError(" + kind.name().toLowerCase() + op + "): "
                + message;
    }
}
