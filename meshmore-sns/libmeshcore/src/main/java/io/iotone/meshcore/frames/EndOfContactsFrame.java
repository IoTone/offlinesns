// Copyright (c) 2026 IoTone, Inc.
// SPDX-License-Identifier: MIT
package io.iotone.meshcore.frames;

/**
 * {@code RESP_CODE_END_OF_CONTACTS} (0x04) — ends a contact dump.
 *
 * @param mostRecentLastMod the highest {@code lastMod} seen; pass as
 *                          {@code since} to the next incremental
 *                          GET_CONTACTS
 */
public record EndOfContactsFrame(long mostRecentLastMod)
        implements MeshcoreInbound {

    @Override
    public String toString() {
        return "EndOfContactsFrame(" + mostRecentLastMod + ")";
    }
}
