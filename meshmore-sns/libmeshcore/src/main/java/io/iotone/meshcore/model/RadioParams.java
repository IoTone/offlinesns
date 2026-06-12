// Copyright (c) 2026 IoTone, Inc.
// SPDX-License-Identifier: MIT
package io.iotone.meshcore.model;

import java.util.OptionalInt;

/**
 * Radio parameters for {@code CMD_SET_RADIO_PARAMS} (0x0B).
 *
 * <p>On wire: {@code freq_kHz = round(frequencyMhz × 1000)} and
 * {@code bw = round(bandwidthKhz × 1000)} as uint32 LE, then SF/CR
 * bytes ({@code MyMesh.cpp}, pinned commit).</p>
 *
 * @param frequencyMhz    centre frequency in MHz
 * @param bandwidthKhz    bandwidth in kHz
 * @param spreadingFactor LoRa spreading factor (5–12)
 * @param codingRate      LoRa coding rate denominator (5–8)
 * @param repeat          optional repeat flag (firmware v9+); omitted
 *                        from the frame when empty
 */
public record RadioParams(
        double frequencyMhz,
        double bandwidthKhz,
        int spreadingFactor,
        int codingRate,
        OptionalInt repeat) {

    /**
     * Convenience factory without the optional repeat flag.
     *
     * @param frequencyMhz    centre frequency in MHz
     * @param bandwidthKhz    bandwidth in kHz
     * @param spreadingFactor LoRa spreading factor (5–12)
     * @param codingRate      LoRa coding rate denominator (5–8)
     * @return params with {@code repeat} empty
     */
    public static RadioParams of(
            double frequencyMhz,
            double bandwidthKhz,
            int spreadingFactor,
            int codingRate) {
        return new RadioParams(frequencyMhz, bandwidthKhz, spreadingFactor,
                codingRate, OptionalInt.empty());
    }

    @Override
    public String toString() {
        return "RadioParams(" + frequencyMhz + "MHz/" + bandwidthKhz
                + "kHz SF" + spreadingFactor + " CR" + codingRate + ")";
    }
}
