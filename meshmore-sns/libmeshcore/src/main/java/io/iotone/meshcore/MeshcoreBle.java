// Copyright (c) 2026 IoTone, Inc.
// SPDX-License-Identifier: MIT
package io.iotone.meshcore;

/**
 * BLE transport identifiers (Nordic-UART-style service used by the
 * MeshCore companion link).
 *
 * <p><strong>Framing rule:</strong> each characteristic write /
 * notification is exactly ONE protocol frame. There is no length prefix;
 * frame boundaries are the BLE MTU boundaries. Multi-byte integers are
 * little-endian; strings are UTF-8. (CayenneLPP payloads, where present,
 * are big-endian.)</p>
 *
 * <p>This library is transport-free — these UUIDs are published so an
 * Android (or other) BLE layer can locate the service and wire a
 * {@link io.iotone.meshcore.transport.MeshcoreTransport}.</p>
 */
public final class MeshcoreBle {

    private MeshcoreBle() {
        // Static constants only.
    }

    /** The companion-radio GATT service UUID. */
    public static final String SERVICE_UUID =
            "6E400001-B5A3-F393-E0A9-E50E24DCCA9E";

    /** App &#8594; Device: the app WRITEs command frames here. */
    public static final String RX_CHARACTERISTIC_UUID =
            "6E400002-B5A3-F393-E0A9-E50E24DCCA9E";

    /** Device &#8594; App: the app SUBSCRIBEs for response/push frames. */
    public static final String TX_CHARACTERISTIC_UUID =
            "6E400003-B5A3-F393-E0A9-E50E24DCCA9E";
}
