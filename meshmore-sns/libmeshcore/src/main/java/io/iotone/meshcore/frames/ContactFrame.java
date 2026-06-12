// Copyright (c) 2026 IoTone, Inc.
// SPDX-License-Identifier: MIT
package io.iotone.meshcore.frames;

import io.iotone.meshcore.model.Contact;

/**
 * {@code RESP_CODE_CONTACT} (0x03) — one entry of a contact dump.
 *
 * @param contact the decoded 148-byte contact body
 */
public record ContactFrame(Contact contact) implements MeshcoreInbound {

    @Override
    public String toString() {
        return "ContactFrame(" + contact + ")";
    }
}
