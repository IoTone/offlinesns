// Copyright (c) 2026 IoTone, Inc.
// SPDX-License-Identifier: MIT
package io.iotone.meshcore.model;

/**
 * Decoded {@code RESP_CODE_DEVICE_INFO} (0x0D).
 *
 * <pre>
 * [0D][fw_ver][max_contacts/2][max_group_channels][ble_pin u32 LE]
 * [build_date 12][manufacturer 40][firmware_version 20]
 * [client_repeat][path_hash_mode]
 * </pre>
 *
 * @param firmwareVerCode   firmware protocol version code
 * @param maxContacts       contact-table capacity — already doubled back
 *                          from the on-wire {@code max_contacts / 2}
 * @param maxGroupChannels  group-channel slot count
 * @param blePin            BLE pairing PIN (u32)
 * @param firmwareBuildDate fixed 12-byte build-date string (NUL-trimmed)
 * @param manufacturer      fixed 40-byte manufacturer string (NUL-trimmed)
 * @param firmwareVersion   fixed 20-byte version string (NUL-trimmed)
 * @param clientRepeat      client-repeat capability byte
 * @param pathHashMode      path-hash mode byte
 */
public record DeviceInfo(
        int firmwareVerCode,
        int maxContacts,
        int maxGroupChannels,
        long blePin,
        String firmwareBuildDate,
        String manufacturer,
        String firmwareVersion,
        int clientRepeat,
        int pathHashMode) {

    @Override
    public String toString() {
        return "DeviceInfo(fw: " + firmwareVersion + " (" + firmwareBuildDate
                + "), mfr: " + manufacturer + ", maxContacts: " + maxContacts
                + ", maxChannels: " + maxGroupChannels + ")";
    }
}
