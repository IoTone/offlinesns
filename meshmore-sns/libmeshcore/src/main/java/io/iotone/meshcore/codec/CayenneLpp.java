// Copyright (c) 2026 IoTone, Inc.
// SPDX-License-Identifier: MIT
package io.iotone.meshcore.codec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * CayenneLPP (Low Power Payload) decoder — big-endian, full
 * ElectronicCats type table.
 *
 * <p>Used to interpret the payload of MeshCore's
 * {@code PUSH_CODE_TELEMETRY_RESPONSE} (0x8B), which carries the bytes
 * produced by {@code CayenneLPP::addGPS/addVoltage/addTemperature/...}
 * on the device side. The firmware vendors
 * {@code electroniccats/CayenneLPP @ 1.6.1}; this decoder carries that
 * library's <em>complete</em> type table, not just the base MyDevices
 * set.</p>
 *
 * <p><strong>Why the full table matters:</strong> CayenneLPP has no
 * per-entry length prefix — a decoder advances by looking the type byte
 * up in a size table, so an unknown type cannot be skipped and parsing
 * must stop. The companion firmware <em>leads</em> every telemetry
 * response with {@code addVoltage} (0x74, battery), so a decoder
 * without the extended table stalls on entry #1 and silently drops
 * everything behind it (GPS, luminosity, temperature…). This was
 * root-caused against a T1000-E and is locked in by a regression test
 * replaying the exact wire order.</p>
 */
public final class CayenneLpp {

    private CayenneLpp() {
        // Static decoder only.
    }

    /**
     * Standard CayenneLPP IPSO type codes (ElectronicCats 1.6.1 table).
     */
    public static final class LppType {

        private LppType() {
            // Constants only.
        }

        /** Digital input (1 B). */
        public static final int DIGITAL_INPUT = 0x00;

        /** Digital output (1 B). */
        public static final int DIGITAL_OUTPUT = 0x01;

        /** Analog input (2 B, &#247;100, signed). */
        public static final int ANALOG_INPUT = 0x02;

        /** Analog output (2 B, &#247;100, signed). */
        public static final int ANALOG_OUTPUT = 0x03;

        /** Generic sensor (4 B, unsigned). Decimal 100. */
        public static final int GENERIC_SENSOR = 0x64;

        /**
         * Luminosity ({@code LPP_LUMINOSITY}, 2 B, unsigned). Decimal
         * 101. Unit caveat: I2C light sensors report lux, but the
         * T1000-E firmware maps its photocell to a 0–100 scale shipped
         * through this same type — treat as "brightness", not
         * calibrated lux.
         */
        public static final int ILLUMINANCE = 0x65;

        /** Presence (1 B, boolean). Decimal 102. */
        public static final int PRESENCE = 0x66;

        /** Temperature (2 B, &#247;10 &#176;C, signed). Decimal 103. */
        public static final int TEMPERATURE = 0x67;

        /** Relative humidity (1 B, &#247;2 %). Decimal 104. */
        public static final int HUMIDITY = 0x68;

        /** Accelerometer (6 B, &#247;1000 G per axis, signed). 113. */
        public static final int ACCELEROMETER = 0x71;

        /** Barometric pressure (2 B, &#247;10 hPa, unsigned). 115. */
        public static final int BAROMETER = 0x73;

        /**
         * Voltage (2 B, &#247;100 V, unsigned). Decimal 116. The
         * companion firmware leads every telemetry response with a
         * battery-voltage entry of this type.
         */
        public static final int VOLTAGE = 0x74;

        /** Current (2 B, &#247;1000 A, unsigned). Decimal 117. */
        public static final int CURRENT = 0x75;

        /** Frequency (4 B, 1 Hz, unsigned). Decimal 118. */
        public static final int FREQUENCY = 0x76;

        /** Percentage (1 B, 1–100 %, unsigned). Decimal 120. */
        public static final int PERCENTAGE = 0x78;

        /** Altitude (2 B, 1 m, signed). Decimal 121. */
        public static final int ALTITUDE = 0x79;

        /** Concentration (2 B, 1 ppm, unsigned). Decimal 125. */
        public static final int CONCENTRATION = 0x7D;

        /** Power (2 B, 1 W, unsigned). Decimal 128. */
        public static final int POWER = 0x80;

        /** Distance (4 B, &#247;1000 m, unsigned). Decimal 130. */
        public static final int DISTANCE = 0x82;

        /** Energy (4 B, &#247;1000 kWh, unsigned). Decimal 131. */
        public static final int ENERGY = 0x83;

        /** Direction (2 B, 1&#176;, unsigned). Decimal 132. */
        public static final int DIRECTION = 0x84;

        /** Unix time (4 B, unsigned seconds). Decimal 133. */
        public static final int UNIX_TIME = 0x85;

        /** Gyrometer (6 B, &#247;100 &#176;/s per axis, signed). 134. */
        public static final int GYROMETER = 0x86;

        /** Colour (3 B, one byte per RGB component). Decimal 135. */
        public static final int COLOUR = 0x87;

        /**
         * GPS location (9 B: s24 lat &#247;10000, s24 lon &#247;10000,
         * s24 alt &#247;100 m; all big-endian). Decimal 136.
         */
        public static final int GPS_LOCATION = 0x88;

        /** Switch (1 B, 0/1). Decimal 142. */
        public static final int SWITCH_STATE = 0x8E;
    }

    /**
     * Payload size in bytes for each LPP type (ElectronicCats
     * {@code LPP_*_SIZE}). The CayenneLPP wire format has NO per-entry
     * length prefix — to advance past an entry we must know its size
     * from the type byte alone. An unknown type means we cannot safely
     * skip it, so parsing stops there.
     */
    private static final Map<Integer, Integer> PAYLOAD_SIZE = Map.ofEntries(
            Map.entry(LppType.DIGITAL_INPUT, 1),
            Map.entry(LppType.DIGITAL_OUTPUT, 1),
            Map.entry(LppType.ANALOG_INPUT, 2),
            Map.entry(LppType.ANALOG_OUTPUT, 2),
            Map.entry(LppType.GENERIC_SENSOR, 4),
            Map.entry(LppType.ILLUMINANCE, 2),
            Map.entry(LppType.PRESENCE, 1),
            Map.entry(LppType.TEMPERATURE, 2),
            Map.entry(LppType.HUMIDITY, 1),
            Map.entry(LppType.ACCELEROMETER, 6),
            Map.entry(LppType.BAROMETER, 2),
            Map.entry(LppType.VOLTAGE, 2),
            Map.entry(LppType.CURRENT, 2),
            Map.entry(LppType.FREQUENCY, 4),
            Map.entry(LppType.PERCENTAGE, 1),
            Map.entry(LppType.ALTITUDE, 2),
            Map.entry(LppType.CONCENTRATION, 2),
            Map.entry(LppType.POWER, 2),
            Map.entry(LppType.DISTANCE, 4),
            Map.entry(LppType.ENERGY, 4),
            Map.entry(LppType.DIRECTION, 2),
            Map.entry(LppType.UNIX_TIME, 4),
            Map.entry(LppType.GYROMETER, 6),
            Map.entry(LppType.COLOUR, 3),
            Map.entry(LppType.GPS_LOCATION, 9),
            Map.entry(LppType.SWITCH_STATE, 1));

    /**
     * One decoded CayenneLPP entry — channel/type plus interpreted
     * values.
     *
     * <p>{@code values} is the <em>physical-unit</em> form (after the
     * type-specific divisor); raw bytes are kept in {@code rawPayload}
     * for callers that want to compute their own interpretation. For
     * unknown types {@code values} is empty. Arrays are owned by the
     * record — callers must not mutate them.</p>
     *
     * @param channel    LPP channel byte (e.g. {@code TELEM_CHANNEL_SELF} = 1)
     * @param type       LPP type byte (e.g. {@link LppType#GPS_LOCATION})
     * @param values     decoded values in physical units; cardinality
     *                   depends on type: GPS = [lat, lon, alt],
     *                   accelerometer/gyrometer = [x, y, z],
     *                   colour = [r, g, b], scalar types = [v]
     * @param rawPayload raw payload bytes (excluding the 2-byte
     *                   channel/type header)
     */
    public record LppEntry(
            int channel,
            int type,
            List<Double> values,
            byte[] rawPayload) {

        /**
         * GPS triplet helper.
         *
         * @return {@code [lat, lon, altMeters]} if this entry is a GPS
         *         location, else {@code null}. Altitude can still be 0
         *         for chips with no altitude data — the caller decides
         *         how to treat that.
         */
        public double[] gps() {
            if (type != LppType.GPS_LOCATION || values.size() < 3) {
                return null;
            }
            return new double[] {values.get(0), values.get(1), values.get(2)};
        }

        @Override
        public String toString() {
            return "LppEntry(ch=" + channel + ", type=0x"
                    + Integer.toHexString(type) + ", values=" + values + ")";
        }
    }

    /**
     * Decodes a CayenneLPP byte buffer into a list of {@link LppEntry}
     * records.
     *
     * <p>Big-endian throughout. Stops cleanly at end-of-buffer. If an
     * unknown type byte is encountered or the buffer is truncated
     * mid-entry, parsing stops and the entries decoded so far are
     * returned — callers should treat the absence of an expected entry
     * as "not reported" rather than as a parse failure.</p>
     *
     * @param bytes raw LPP payload (e.g. from a telemetry response)
     * @return immutable list of decoded entries, possibly empty
     */
    public static List<LppEntry> decode(byte[] bytes) {
        List<LppEntry> result = new ArrayList<>();
        int i = 0;
        while (i + 2 <= bytes.length) {
            int channel = bytes[i] & 0xFF;
            int type = bytes[i + 1] & 0xFF;
            Integer size = PAYLOAD_SIZE.get(type);
            if (size == null) {
                break; // unknown type — cannot advance safely
            }
            if (i + 2 + size > bytes.length) {
                break; // truncated
            }
            byte[] raw = Arrays.copyOfRange(bytes, i + 2, i + 2 + size);
            result.add(new LppEntry(channel, type, decodeValues(type, raw), raw));
            i += 2 + size;
        }
        return Collections.unmodifiableList(result);
    }

    private static List<Double> decodeValues(int type, byte[] raw) {
        switch (type) {
            case LppType.DIGITAL_INPUT:
            case LppType.DIGITAL_OUTPUT:
            case LppType.PRESENCE:
            case LppType.PERCENTAGE:
            case LppType.SWITCH_STATE:
                return List.of((double) (raw[0] & 0xFF));
            case LppType.HUMIDITY:
                return List.of((raw[0] & 0xFF) / 2.0);
            case LppType.ANALOG_INPUT:
            case LppType.ANALOG_OUTPUT:
                return List.of(s16be(raw, 0) / 100.0);
            case LppType.TEMPERATURE:
                return List.of(s16be(raw, 0) / 10.0);
            case LppType.ALTITUDE:
                return List.of((double) s16be(raw, 0));
            case LppType.ILLUMINANCE:
            case LppType.CONCENTRATION:
            case LppType.POWER:
            case LppType.DIRECTION:
                return List.of((double) u16be(raw, 0));
            case LppType.BAROMETER:
                return List.of(u16be(raw, 0) / 10.0);
            case LppType.VOLTAGE:
                return List.of(u16be(raw, 0) / 100.0);
            case LppType.CURRENT:
                return List.of(u16be(raw, 0) / 1000.0);
            case LppType.GENERIC_SENSOR:
            case LppType.FREQUENCY:
            case LppType.UNIX_TIME:
                return List.of((double) u32be(raw, 0));
            case LppType.DISTANCE:
            case LppType.ENERGY:
                return List.of(u32be(raw, 0) / 1000.0);
            case LppType.COLOUR:
                return List.of(
                        (double) (raw[0] & 0xFF),
                        (double) (raw[1] & 0xFF),
                        (double) (raw[2] & 0xFF));
            case LppType.ACCELEROMETER:
                return List.of(
                        s16be(raw, 0) / 1000.0,
                        s16be(raw, 2) / 1000.0,
                        s16be(raw, 4) / 1000.0);
            case LppType.GYROMETER:
                return List.of(
                        s16be(raw, 0) / 100.0,
                        s16be(raw, 2) / 100.0,
                        s16be(raw, 4) / 100.0);
            case LppType.GPS_LOCATION:
                return List.of(
                        s24be(raw, 0) / 10000.0,
                        s24be(raw, 3) / 10000.0,
                        s24be(raw, 6) / 100.0);
            default:
                return List.of();
        }
    }

    private static int u16be(byte[] b, int off) {
        return ((b[off] & 0xFF) << 8) | (b[off + 1] & 0xFF);
    }

    private static int s16be(byte[] b, int off) {
        int v = u16be(b, off);
        return v >= 0x8000 ? v - 0x10000 : v;
    }

    private static long u32be(byte[] b, int off) {
        return ((b[off] & 0xFFL) << 24)
                | ((b[off + 1] & 0xFFL) << 16)
                | ((b[off + 2] & 0xFFL) << 8)
                | (b[off + 3] & 0xFFL);
    }

    private static int s24be(byte[] b, int off) {
        int v = ((b[off] & 0xFF) << 16)
                | ((b[off + 1] & 0xFF) << 8)
                | (b[off + 2] & 0xFF);
        return v >= 0x800000 ? v - 0x1000000 : v;
    }
}
