// Copyright (c) 2026 IoTone, Inc.
// SPDX-License-Identifier: MIT
package io.iotone.meshcore;

/**
 * Response / push opcodes (Device &#8594; App) — the first byte of an
 * inbound frame.
 *
 * <p>Opcodes &#8805; 0x80 are asynchronous push notifications (not
 * direct replies to a command); see {@link #isPush()}.</p>
 */
public enum MeshcoreResponse {

    /** {@code RESP_CODE_OK}. */
    OK(0x00),

    /** {@code RESP_CODE_ERR}. */
    ERROR(0x01),

    /** {@code RESP_CODE_CONTACTS_START}. */
    CONTACT_START(0x02),

    /** {@code RESP_CODE_CONTACT}. */
    CONTACT(0x03),

    /** {@code RESP_CODE_END_OF_CONTACTS}. */
    CONTACT_END(0x04),

    /** {@code RESP_CODE_SELF_INFO} — also the APP_START reply. */
    SELF_INFO(0x05),

    /** {@code RESP_CODE_SENT} — send confirmation. */
    MSG_SENT(0x06),

    /** {@code RESP_CODE_CONTACT_MSG_RECV} (legacy). */
    CONTACT_MSG_RECV(0x07),

    /** {@code RESP_CODE_CHANNEL_MSG_RECV} (legacy). */
    CHANNEL_MSG_RECV(0x08),

    /** {@code RESP_CODE_CURR_TIME}. */
    CURRENT_TIME(0x09),

    /** {@code RESP_CODE_NO_MORE_MESSAGES}. */
    NO_MORE_MSGS(0x0A),

    /** {@code RESP_CODE_BATT_AND_STORAGE}. */
    BATTERY(0x0C),

    /** {@code RESP_CODE_DEVICE_INFO}. */
    DEVICE_INFO(0x0D),

    /** {@code RESP_CODE_CONTACT_MSG_RECV_V3} (carries SNR). */
    CONTACT_MSG_RECV_V3(0x10),

    /** {@code RESP_CODE_CHANNEL_MSG_RECV_V3} (carries SNR). */
    CHANNEL_MSG_RECV_V3(0x11),

    /** {@code RESP_CODE_CHANNEL_INFO} — reply to GET_CHANNEL. */
    CHANNEL_INFO(0x12),

    /**
     * {@code RESP_CODE_CUSTOM_VARS} (21 = 0x15) — reply to
     * {@link MeshcoreCommand#GET_CUSTOM_VARS}. Payload: comma-separated
     * {@code name:value} pairs (ASCII, no NUL).
     */
    CUSTOM_VARS(0x15),

    /** {@code PUSH_CODE_ADVERTISEMENT}. */
    ADVERTISEMENT(0x80),

    /** {@code PUSH_CODE_ACK} — delivery acknowledgement. */
    ACK(0x82),

    /** {@code PUSH_CODE_MSGS_WAITING} — inbound items are queued. */
    MESSAGES_WAITING(0x83),

    /** {@code PUSH_CODE_LOG_RX_DATA} — RF log; safely ignorable. */
    LOG_DATA(0x88),

    /**
     * {@code PUSH_CODE_TELEMETRY_RESPONSE} (0x8B). Async push delivering
     * a telemetry payload either for self (immediate after a
     * {@code CMD_SEND_TELEMETRY_REQ} with len==4) or for a peer (arrives
     * some seconds later after the OTA round-trip). Wire format:
     * {@code [0x8B][reserved 1B][pubkey_prefix 6B][CayenneLPP payload]}.
     */
    TELEMETRY_RESPONSE(0x8B);

    private final int code;

    MeshcoreResponse(int code) {
        this.code = code;
    }

    /**
     * Returns the on-wire opcode byte.
     *
     * @return opcode in {@code [0, 255]}
     */
    public int code() {
        return code;
    }

    /**
     * Returns whether this opcode is an asynchronous push notification
     * (opcodes &#8805; 0x80) rather than a direct command reply.
     *
     * @return {@code true} for push codes
     */
    public boolean isPush() {
        return code >= 0x80;
    }
}
