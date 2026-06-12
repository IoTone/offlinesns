// Copyright (c) 2026 IoTone, Inc.
// SPDX-License-Identifier: MIT
package io.iotone.meshcore;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.iotone.meshcore.codec.CayenneLpp;
import io.iotone.meshcore.codec.CayenneLpp.LppEntry;
import io.iotone.meshcore.codec.CayenneLpp.LppType;

import java.util.List;

import org.junit.jupiter.api.Test;

/** CayenneLPP decoder KATs, mirroring the Dart reference tests. */
class CayenneLppTest {

    @Test
    void emptyBufferNoEntries() {
        assertTrue(CayenneLpp.decode(new byte[0]).isEmpty());
    }

    @Test
    void gpsFixtureFromReferenceDocs() {
        // Channel 1, type 0x88, lat=42.3519 lon=-87.9094 alt=10.00 m.
        List<LppEntry> entries = CayenneLpp.decode(
                TestUtil.hex("01 88 06 76 5F F2 96 0A 00 03 E8"));
        assertEquals(1, entries.size());
        LppEntry e = entries.get(0);
        assertEquals(1, e.channel());
        assertEquals(LppType.GPS_LOCATION, e.type());
        double[] gps = e.gps();
        assertNotNull(gps);
        assertEquals(42.3519, gps[0], 1e-4);
        assertEquals(-87.9094, gps[1], 1e-4);
        assertEquals(10.00, gps[2], 1e-2);
    }

    @Test
    void gpsNegativeLonAndBelowSeaLevel() {
        // lat=10.0000, lon=-20.0000, alt=-50.00 m (s24 two's complement).
        double[] gps = CayenneLpp.decode(
                        TestUtil.hex("01 88 01 86 A0 FC F2 C0 FF EC 78"))
                .get(0).gps();
        assertNotNull(gps);
        assertEquals(10.0, gps[0], 1e-4);
        assertEquals(-20.0, gps[1], 1e-4);
        assertEquals(-50.0, gps[2], 1e-2);
    }

    @Test
    void unknownTypeStopsParsingEarlierEntriesKept() {
        List<LppEntry> entries = CayenneLpp.decode(
                TestUtil.hex("03 67 01 10 01 CC 00 00"));
        assertEquals(1, entries.size());
        assertEquals(LppType.TEMPERATURE, entries.get(0).type());
    }

    @Test
    void t1000eWireOrderRegression() {
        // Mirrors companion_radio MyMesh.cpp: addVoltage(ch1) FIRST, then
        // querySensors → addGPS, addLuminosity, addTemperature — all on
        // TELEM_CHANNEL_SELF (1). Regression: voltage 0x74 used to stall
        // the decoder at entry #1, dropping ALL env telemetry behind it.
        byte[] bytes = TestUtil.hex(
                "01 74 01 9C  01 88 01 86 A0 FC F2 C0 FF EC 78  "
                + "01 65 00 3E  01 67 01 17");
        List<LppEntry> entries = CayenneLpp.decode(bytes);
        assertEquals(4, entries.size(), "nothing may be dropped");
        assertEquals(LppType.VOLTAGE, entries.get(0).type());
        assertEquals(4.12, entries.get(0).values().get(0), 1e-9);
        assertEquals(LppType.GPS_LOCATION, entries.get(1).type());
        assertEquals(10.0, entries.get(1).gps()[0], 1e-4);
        assertEquals(LppType.ILLUMINANCE, entries.get(2).type());
        assertEquals(62.0, entries.get(2).values().get(0), 1e-9);
        assertEquals(LppType.TEMPERATURE, entries.get(3).type());
        assertEquals(27.9, entries.get(3).values().get(0), 1e-9);
        assertTrue(entries.stream().allMatch(e -> e.channel() == 1),
                "TELEM_CHANNEL_SELF = 1 throughout");
    }

    @Test
    void extendedTypesDecodeWithElectronicCatsDivisors() {
        // current 0.250 A; altitude -120 m; percentage 87; distance 3.5 m.
        List<LppEntry> entries = CayenneLpp.decode(TestUtil.hex(
                "01 75 00 FA  01 79 FF 88  01 78 57  01 82 00 00 0D AC"));
        assertEquals(4, entries.size());
        assertEquals(0.250, entries.get(0).values().get(0), 1e-9);
        assertEquals(-120.0, entries.get(1).values().get(0), 1e-9);
        assertEquals(87.0, entries.get(2).values().get(0), 1e-9);
        assertEquals(3.5, entries.get(3).values().get(0), 1e-9);
    }

    @Test
    void truncatedEntryStopsCleanly() {
        // A good humidity entry then a temperature missing its 2nd byte.
        List<LppEntry> entries =
                CayenneLpp.decode(TestUtil.hex("02 68 64 01 67 01"));
        assertEquals(1, entries.size());
        assertEquals(LppType.HUMIDITY, entries.get(0).type());
        assertEquals(50.0, entries.get(0).values().get(0), 1e-9);
    }

    @Test
    void rawPayloadExposed() {
        byte[] raw = CayenneLpp.decode(
                        TestUtil.hex("01 88 06 76 5F F2 96 0A 00 03 E8"))
                .get(0).rawPayload();
        assertEquals(9, raw.length);
        assertEquals(0x06, raw[0] & 0xFF);
        assertEquals(0xE8, raw[8] & 0xFF);
    }
}
