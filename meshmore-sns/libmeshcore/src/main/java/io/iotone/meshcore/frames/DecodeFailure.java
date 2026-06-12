// Copyright (c) 2026 IoTone, Inc.
// SPDX-License-Identifier: MIT
package io.iotone.meshcore.frames;

import io.iotone.meshcore.codec.MeshcoreDecodeError;

/**
 * Decoding failed (empty/truncated frame).
 *
 * <p>Never thrown — always returned as a value, preserving the codec's
 * "total decode" invariant. See
 * {@link io.iotone.meshcore.codec.DecodeErrorKind} for the (small)
 * failure taxonomy.</p>
 *
 * @param error structured failure report
 */
public record DecodeFailure(MeshcoreDecodeError error)
        implements MeshcoreInbound {

    @Override
    public String toString() {
        return "DecodeFailure(" + error + ")";
    }
}
