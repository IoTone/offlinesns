// Copyright (c) 2026 IoTone, Inc.
// SPDX-License-Identifier: MIT
package io.iotone.meshcore;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.iotone.meshcore.codec.DecodeErrorKind;
import io.iotone.meshcore.codec.MeshcoreFrameCodec;
import io.iotone.meshcore.frames.DecodeFailure;
import io.iotone.meshcore.frames.MeshcoreInbound;
import io.iotone.meshcore.model.OtaPacket;

import java.util.Random;

import org.junit.jupiter.api.Test;

/**
 * Totality hardening: the decode/parse surface must yield a value or a
 * <em>controlled</em> failure for every input — never an uncaught
 * exception. Deterministic (fixed-seed RNG) so the gate is
 * reproducible.
 */
class FuzzTest {

    private static final int ITERATIONS = 4000;

    private static byte[] randomBytes(Random r, int maxLen) {
        byte[] b = new byte[r.nextInt(maxLen + 1)];
        r.nextBytes(b);
        return b;
    }

    @Test
    void frameDecodeIsTotal() {
        Random r = new Random(0xC0FFEE);
        int[] ops = {
            0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08,
            0x09, 0x0A, 0x0C, 0x0D, 0x10, 0x11, 0x12, 0x15,
            0x80, 0x82, 0x83, 0x88, 0x8B,
        };
        for (int i = 0; i < ITERATIONS; i++) {
            // Half pure-random, half "valid opcode + random tail" so we
            // exercise every typed decoder's truncation paths.
            byte[] frame = randomBytes(r, 300);
            if (i % 2 == 0 && frame.length > 0) {
                frame[0] = (byte) ops[r.nextInt(ops.length)];
            }
            MeshcoreInbound out = MeshcoreFrameCodec.decode(frame);
            assertNotNull(out);
            if (out instanceof DecodeFailure df) {
                // The only failure kinds are structural.
                assertTrue(df.error().kind() == DecodeErrorKind.EMPTY
                        || df.error().kind() == DecodeErrorKind.TRUNCATED);
            }
        }
    }

    @Test
    void otaPacketParseNeverThrows() {
        Random r = new Random(0xBEEF);
        for (int i = 0; i < ITERATIONS; i++) {
            OtaPacket p = OtaPacket.parse(randomBytes(r, 250));
            if (p != null) {
                assertNotNull(p.payload());
                assertTrue(p.hopCount() >= 0 && p.hopCount() <= 63);
            }
        }
    }
}
