// Copyright (c) 2026 IoTone, Inc.
// SPDX-License-Identifier: MIT
package io.iotone.meshcore.frames;

import java.util.OptionalInt;

/**
 * {@code PUSH_CODE_MSGS_WAITING} (0x83) — the device has queued inbound
 * item(s) (received messages and/or newly heard contacts/adverts).
 *
 * <p>The app must drain them with {@code CMD_SYNC_NEXT_MESSAGE} until
 * it gets a {@link NoMoreMessagesFrame}. Some firmware appends a count
 * byte; it is advisory only, so {@code count} is empty when absent.</p>
 *
 * @param count advisory queued-item count, when the firmware sent one
 */
public record MessagesWaitingFrame(OptionalInt count)
        implements MeshcoreInbound {

    @Override
    public String toString() {
        return "MessagesWaitingFrame("
                + (count.isPresent() ? count.getAsInt() : "null") + ")";
    }
}
