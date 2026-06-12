// Copyright (c) 2026 IoTone, Inc.
// SPDX-License-Identifier: MIT
package io.iotone.meshcore.model;

/**
 * Decoded {@code PUSH_CODE_LOG_RX_DATA} (0x88).
 *
 * <p>Firmware ({@code MyMesh.cpp::logRxRaw}, pinned commit):
 * {@code [0x88][SNR×4 int8][RSSI int8][raw OTA packet …]}. The raw
 * bytes are the <strong>full received over-the-air packet</strong>
 * (header + path + encrypted payload) — the basis for interop fixtures
 * and the channel-tail oracle. The array is owned by the record —
 * callers must not mutate it.</p>
 *
 * @param snrDb signal-to-noise in dB (raw byte &#247; 4)
 * @param rssi  received signal strength (signed dBm byte)
 * @param raw   the full OTA packet bytes; parse with
 *              {@link OtaPacket#parse(byte[])}
 */
public record RfLog(double snrDb, int rssi, byte[] raw) {

    /**
     * Parses the raw bytes as an OTA packet.
     *
     * @return the parsed packet, or {@code null} when malformed
     */
    public OtaPacket packet() {
        return OtaPacket.parse(raw);
    }

    @Override
    public String toString() {
        return "RfLog(snr: " + snrDb + ", rssi: " + rssi + ", raw: "
                + raw.length + "B)";
    }
}
