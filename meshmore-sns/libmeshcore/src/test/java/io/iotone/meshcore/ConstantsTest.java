// Copyright (c) 2026 IoTone, Inc.
// SPDX-License-Identifier: MIT
package io.iotone.meshcore;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Constants/pin sanity, mirroring the Dart {@code constants_test}: the
 * firmware pin metadata, BLE identifiers, opcode values, and the SNR
 * byte conversion.
 */
class ConstantsTest {

    @Test
    void firmwarePinMetadataIsRecorded() {
        assertEquals("companion-v1.15.0", MeshcoreConstants.FIRMWARE_PIN_TAG);
        assertEquals(40, MeshcoreConstants.FIRMWARE_PIN_COMMIT.length());
        assertTrue(MeshcoreConstants.FIRMWARE_PIN_COMMIT
                        .matches("^[0-9a-f]{40}$"),
                "pin must be a full 40-char git SHA");
    }

    @Test
    void bleIdentifiersMatchPinnedDoc() {
        assertTrue("6E400001-B5A3-F393-E0A9-E50E24DCCA9E"
                .equalsIgnoreCase(MeshcoreBle.SERVICE_UUID));
        assertTrue("6E400002-B5A3-F393-E0A9-E50E24DCCA9E"
                .equalsIgnoreCase(MeshcoreBle.RX_CHARACTERISTIC_UUID));
        assertTrue("6E400003-B5A3-F393-E0A9-E50E24DCCA9E"
                .equalsIgnoreCase(MeshcoreBle.TX_CHARACTERISTIC_UUID));
    }

    @Test
    void coreCommandOpcodes() {
        assertEquals(0x01, MeshcoreCommand.APP_START.code());
        assertEquals(0x03, MeshcoreCommand.SEND_CHANNEL_MESSAGE.code());
        assertEquals(0x04, MeshcoreCommand.GET_CONTACTS.code());
        assertEquals(0x05, MeshcoreCommand.GET_DEVICE_TIME.code());
        assertEquals(0x06, MeshcoreCommand.SET_DEVICE_TIME.code());
        assertEquals(0x0A, MeshcoreCommand.SYNC_NEXT_MESSAGE.code());
        assertEquals(0x20, MeshcoreCommand.SET_CHANNEL.code());
    }

    @Test
    void coreResponseOpcodes() {
        assertEquals(0x00, MeshcoreResponse.OK.code());
        assertEquals(0x05, MeshcoreResponse.SELF_INFO.code());
        assertEquals(0x08, MeshcoreResponse.CHANNEL_MSG_RECV.code());
    }

    @Test
    void pushCodesAreFlagged() {
        assertTrue(MeshcoreResponse.ADVERTISEMENT.isPush());
        assertTrue(MeshcoreResponse.MESSAGES_WAITING.isPush());
        assertFalse(MeshcoreResponse.OK.isPush());
        assertFalse(MeshcoreResponse.SELF_INFO.isPush());
    }

    @Test
    void snrByteToDbTwosComplement() {
        assertEquals(0.0, MeshcoreConstants.snrByteToDb(0x00), 1e-9);
        assertEquals(1.0, MeshcoreConstants.snrByteToDb(0x04), 1e-9);
        assertEquals(-1.0, MeshcoreConstants.snrByteToDb(0xFC), 1e-9);
        assertEquals(-32.0, MeshcoreConstants.snrByteToDb(0x80), 1e-9);
    }
}
