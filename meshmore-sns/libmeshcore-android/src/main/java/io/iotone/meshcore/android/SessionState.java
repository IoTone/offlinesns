// Copyright (c) 2026 IoTone, Inc.
// SPDX-License-Identifier: MIT
package io.iotone.meshcore.android;

/**
 * State machine for a {@link MeshcoreSession}.
 *
 * <p>The normal progression is:
 * {@link #DISCONNECTED} &#8594; {@link #CONNECTING} &#8594;
 * {@link #HANDSHAKING} &#8594; {@link #READY}. A link loss moves back
 * to {@link #DISCONNECTED}.</p>
 */
public enum SessionState {

    /**
     * No BLE connection — the initial state and the state after any
     * disconnection.
     */
    DISCONNECTED,

    /**
     * BLE connection is being established; GATT services are being
     * discovered.
     */
    CONNECTING,

    /**
     * BLE is connected; the {@code CMD_APP_START} (0x01) frame has been
     * sent and the session is waiting for the {@code RESP_CODE_SELF_INFO}
     * (0x05) reply.
     */
    HANDSHAKING,

    /**
     * The handshake completed successfully; the device is ready to send
     * and receive frames. Message draining runs automatically whenever
     * {@code PUSH_CODE_MSGS_WAITING} (0x83) is received.
     */
    READY
}
