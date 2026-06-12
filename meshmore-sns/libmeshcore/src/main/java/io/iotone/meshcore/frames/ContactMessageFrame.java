// Copyright (c) 2026 IoTone, Inc.
// SPDX-License-Identifier: MIT
package io.iotone.meshcore.frames;

import io.iotone.meshcore.model.ContactMessage;

/**
 * {@code RESP_CODE_CONTACT_MSG_RECV} (0x07) and its V3 variant (0x10).
 *
 * @param message the decoded direct message (V3 carries SNR)
 */
public record ContactMessageFrame(ContactMessage message)
        implements MeshcoreInbound {

    @Override
    public String toString() {
        return "ContactMessageFrame(" + message + ")";
    }
}
