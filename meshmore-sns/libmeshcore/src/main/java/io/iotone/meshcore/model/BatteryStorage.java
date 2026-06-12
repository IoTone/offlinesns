// Copyright (c) 2026 IoTone, Inc.
// SPDX-License-Identifier: MIT
package io.iotone.meshcore.model;

/**
 * Decoded {@code RESP_CODE_BATT_AND_STORAGE} (0x0C).
 *
 * <pre>
 * [0C][batt_mv u16 LE][used_kb u32 LE][total_kb u32 LE]
 * </pre>
 *
 * @param batteryMillivolts battery voltage in millivolts
 * @param storageUsedKb     used storage in KB
 * @param storageTotalKb    total storage in KB
 */
public record BatteryStorage(
        int batteryMillivolts,
        long storageUsedKb,
        long storageTotalKb) {

    /**
     * Returns the battery voltage in volts.
     *
     * @return {@link #batteryMillivolts()} &#247; 1000
     */
    public double batteryVolts() {
        return batteryMillivolts / 1000.0;
    }

    @Override
    public String toString() {
        return "BatteryStorage(" + batteryVolts() + "V, " + storageUsedKb
                + "/" + storageTotalKb + " KB)";
    }
}
