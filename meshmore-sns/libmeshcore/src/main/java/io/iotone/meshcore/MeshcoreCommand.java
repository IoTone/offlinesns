// Copyright (c) 2026 IoTone, Inc.
// SPDX-License-Identifier: MIT
package io.iotone.meshcore;

/**
 * Command opcodes (App &#8594; Device) — the first byte of an outbound
 * frame.
 *
 * <p>Values are cross-checked against
 * {@code examples/companion_radio/MyMesh.cpp} at the pinned commit
 * ({@link MeshcoreConstants#FIRMWARE_PIN_COMMIT}); the markdown protocol
 * doc omits several commands.</p>
 */
public enum MeshcoreCommand {

    /** {@code CMD_APP_START} — must be the first frame after connect. */
    APP_START(0x01),

    /** {@code CMD_SEND_TXT_MSG} — direct/contact text message. */
    SEND_TEXT_MESSAGE(0x02),

    /** {@code CMD_SEND_CHANNEL_TXT_MSG} — channel text message. */
    SEND_CHANNEL_MESSAGE(0x03),

    /** {@code CMD_GET_CONTACTS} — source-only (not in the markdown doc). */
    GET_CONTACTS(0x04),

    /** {@code CMD_GET_DEVICE_TIME} — source-only. */
    GET_DEVICE_TIME(0x05),

    /** {@code CMD_SET_DEVICE_TIME} — source-only. */
    SET_DEVICE_TIME(0x06),

    /** {@code CMD_SEND_SELF_ADVERT} — emit our advert (flood/zero-hop). */
    SEND_SELF_ADVERT(0x07),

    /** {@code CMD_SET_ADVERT_NAME} — set our advertised node name. */
    SET_ADVERT_NAME(0x08),

    /** {@code CMD_ADD_UPDATE_CONTACT} — add/replace a contact (148 B). */
    ADD_UPDATE_CONTACT(0x09),

    /** {@code CMD_SYNC_NEXT_MESSAGE} — pull the next queued message. */
    SYNC_NEXT_MESSAGE(0x0A),

    /** {@code CMD_SET_RADIO_PARAMS} — frequency / bandwidth / SF / CR. */
    SET_RADIO_PARAMS(0x0B),

    /** {@code CMD_SET_RADIO_TX_POWER} — TX power (int8 dBm). */
    SET_RADIO_TX_POWER(0x0C),

    /** {@code CMD_SET_ADVERT_LATLON} — advertised location. */
    SET_ADVERT_LATLON(0x0E),

    /**
     * {@code CMD_REMOVE_CONTACT} — remove a contact by public key
     * (frees a slot in the radio's contact table).
     */
    REMOVE_CONTACT(0x0F),

    /** {@code CMD_GET_BATT_AND_STORAGE} — battery + storage readout. */
    GET_BATTERY_STORAGE(0x14),

    /** {@code CMD_SET_TUNING_PARAMS} — rx-delay base / airtime factor. */
    SET_TUNING_PARAMS(0x15),

    /** {@code CMD_DEVICE_QUERY} — device info query. */
    DEVICE_QUERY(0x16),

    /** {@code CMD_GET_CHANNEL} — read one channel slot. */
    GET_CHANNEL(0x1F),

    /** {@code CMD_SET_CHANNEL} — write one channel slot (50-byte body). */
    SET_CHANNEL(0x20),

    /** {@code CMD_SET_OTHER_PARAMS} — manual-add / telemetry / loc policy. */
    SET_OTHER_PARAMS(0x26),

    /**
     * {@code CMD_SEND_TELEMETRY_REQ} (39 = 0x27). Frame is either
     * {@code 27 [0 0 0]} (self-telemetry, len == 4) or
     * {@code 27 [0 0 0] [pub_key 32B]} (peer-telemetry, len == 36).
     * The 3 bytes after the opcode are reserved/padding in firmware
     * {@code companion-v1.15.0} and may carry a permission mask in newer
     * builds — sending zeros is safe today. Response is async via
     * {@link MeshcoreResponse#TELEMETRY_RESPONSE} (0x8B).
     */
    SEND_TELEMETRY_REQ(0x27),

    /**
     * {@code CMD_GET_CUSTOM_VARS} — request the device's custom
     * string-keyed settings map. Reply is {@code RESP_CODE_CUSTOM_VARS}
     * (0x15) carrying a comma-separated {@code name:value,...} payload.
     * Used to read {@code gps}, {@code gps_interval}, etc.
     */
    GET_CUSTOM_VARS(0x28),

    /**
     * {@code CMD_SET_CUSTOM_VAR} — write a single string-keyed setting.
     * Body: {@code [name]:[value]} (ASCII, no NUL — frame length defines
     * the end). Reply: OK on success, ERR on bad arg.
     */
    SET_CUSTOM_VAR(0x29),

    /** {@code CMD_SEND_CHANNEL_DATAGRAM} — raw channel datagram. */
    SEND_CHANNEL_DATAGRAM(0x3E);

    private final int code;

    MeshcoreCommand(int code) {
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
}
