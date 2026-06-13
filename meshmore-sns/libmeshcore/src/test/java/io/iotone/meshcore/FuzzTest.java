// Copyright (c) 2026 IoTone, Inc.
// SPDX-License-Identifier: MIT
package io.iotone.meshcore;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.iotone.meshcore.codec.DecodeErrorKind;
import io.iotone.meshcore.codec.MeshcoreFrameCodec;
import io.iotone.meshcore.crypto.ChannelCrypto;
import io.iotone.meshcore.crypto.IdentityCrypto;
import io.iotone.meshcore.diagnostics.ChannelTailOracle;
import io.iotone.meshcore.diagnostics.ChannelTailResult;
import io.iotone.meshcore.frames.DecodeFailure;
import io.iotone.meshcore.frames.MeshcoreInbound;
import io.iotone.meshcore.frames.UnsupportedFrame;
import io.iotone.meshcore.model.GrpTxtPayload;
import io.iotone.meshcore.model.OtaPacket;

import java.util.Arrays;
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

    @Test
    void macThenDecryptNeverThrowsOnGarbage() {
        Random r = new Random(0x5EED);
        for (int i = 0; i < ITERATIONS; i++) {
            byte[] secret = randomBytes(r, 32);
            byte[] padded = Arrays.copyOf(secret, 32);
            byte[] d = ChannelCrypto.macThenDecrypt(padded,
                    randomBytes(r, 80));
            if (d != null) {
                assertEquals(0, d.length % 16);
            }
        }
    }

    @Test
    void resolveChannelTailNeverThrowsOnRandomGrpTxt() {
        Random r = new Random(0xA11CE);
        for (int i = 0; i < 1000; i++) {
            ChannelTailResult res = ChannelTailOracle.resolveChannelTail(
                    randomBytes(r, 16),
                    randomBytes(r, 24),
                    new GrpTxtPayload(r.nextInt(256), randomBytes(r, 64)));
            assertNotNull(res);
        }
    }

    @Test
    void montgomeryUMapTotalOrControlledError() {
        Random r = new Random(0xED2519);
        for (int i = 0; i < 1500; i++) {
            byte[] pub = new byte[32];
            r.nextBytes(pub);
            try {
                byte[] u = IdentityCrypto.edPublicKeyToMontgomeryU(pub);
                assertEquals(32, u.length);
            } catch (IllegalArgumentException e) {
                // Acceptable: degenerate point — a *controlled* error.
            }
        }
    }

    @Test
    void decodeErrorTaxonomyInvariants() {
        DecodeFailure empty = assertInstanceOf(DecodeFailure.class,
                MeshcoreFrameCodec.decode(new byte[0]));
        assertEquals(DecodeErrorKind.EMPTY, empty.error().kind());

        // CONTACTS_START needs a u32 count; give 1 byte.
        DecodeFailure trunc = assertInstanceOf(DecodeFailure.class,
                MeshcoreFrameCodec.decode(new byte[] {0x02, 0x01}));
        assertEquals(DecodeErrorKind.TRUNCATED, trunc.error().kind());

        // Unknown opcode → UnsupportedFrame (NOT a failure), raw kept.
        byte[] frame = {0x7E, (byte) 0xDE, (byte) 0xAD};
        UnsupportedFrame u = assertInstanceOf(UnsupportedFrame.class,
                MeshcoreFrameCodec.decode(frame));
        assertEquals(0x7E, u.opcode());
        assertArrayEquals(frame, u.raw());
    }
}
