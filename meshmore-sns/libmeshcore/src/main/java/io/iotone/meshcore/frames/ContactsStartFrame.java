// Copyright (c) 2026 IoTone, Inc.
// SPDX-License-Identifier: MIT
package io.iotone.meshcore.frames;

/**
 * {@code RESP_CODE_CONTACTS_START} (0x02) — begins a contact dump.
 *
 * @param count number of {@link ContactFrame}s that will follow
 */
public record ContactsStartFrame(long count) implements MeshcoreInbound {

    @Override
    public String toString() {
        return "ContactsStartFrame(" + count + ")";
    }
}
