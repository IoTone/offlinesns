// Copyright (c) 2026 IoTone, Inc.
// SPDX-License-Identifier: MIT
package io.iotone.meshcore.model;

/**
 * Decoded {@code RESP_CODE_SELF_INFO} (0x05) — also the reply to
 * APP_START.
 *
 * <p>Layout transcribed from {@code examples/companion_radio/MyMesh.cpp}
 * (pinned commit). Scaling: lat/lon are signed int32 &#247; 1e6;
 * frequency and bandwidth are uint32 &#247; 1000. The array is owned by
 * the record — callers must not mutate it.</p>
 *
 * @param advType           advert node type
 * @param txPowerDbm        configured TX power (dBm)
 * @param maxTxPowerDbm     hardware maximum TX power (dBm)
 * @param publicKey         32-byte Ed25519 public key
 * @param latitude          degrees (raw int32 &#247; 1e6)
 * @param longitude         degrees (raw int32 &#247; 1e6)
 * @param multiAcks         multi-ACK setting byte
 * @param advertLocPolicy   advert location-policy byte
 * @param telemetryModeRaw  packed telemetry-mode byte
 * @param manualAddContacts whether manual contact adding is enabled
 * @param frequencyMhz      MHz (raw uint32 &#247; 1000)
 * @param bandwidthKhz      kHz (raw uint32 &#247; 1000)
 * @param spreadingFactor   LoRa spreading factor
 * @param codingRate        LoRa coding rate denominator
 * @param name              device/node name (trailing UTF-8)
 */
public record SelfInfo(
        int advType,
        int txPowerDbm,
        int maxTxPowerDbm,
        byte[] publicKey,
        double latitude,
        double longitude,
        int multiAcks,
        int advertLocPolicy,
        int telemetryModeRaw,
        boolean manualAddContacts,
        double frequencyMhz,
        double bandwidthKhz,
        int spreadingFactor,
        int codingRate,
        String name) {

    @Override
    public String toString() {
        return "SelfInfo(name: " + name + ", freq: " + frequencyMhz
                + "MHz, bw: " + bandwidthKhz + "kHz, sf: " + spreadingFactor
                + ", cr: " + codingRate + ")";
    }
}
