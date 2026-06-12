// Copyright (c) 2026 IoTone, Inc.
// SPDX-License-Identifier: MIT
package io.iotone.meshcore.frames;

import java.util.Map;

/**
 * {@code RESP_CODE_CUSTOM_VARS} (0x15) — reply to GET_CUSTOM_VARS.
 *
 * <p>Carries the device's string-keyed settings (e.g. {@code gps},
 * {@code gps_interval}). Known keys are documented in the firmware
 * {@code examples/companion_radio/MyMesh.cpp} — the codec is permissive
 * and surfaces everything the device reports.</p>
 *
 * @param values immutable name &#8594; value map
 */
public record CustomVarsFrame(Map<String, String> values)
        implements MeshcoreInbound {

    @Override
    public String toString() {
        return "CustomVarsFrame(" + values + ")";
    }
}
