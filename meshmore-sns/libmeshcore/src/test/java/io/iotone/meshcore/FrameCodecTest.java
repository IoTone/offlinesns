// Copyright (c) 2026 IoTone, Inc.
// SPDX-License-Identifier: MIT
package io.iotone.meshcore;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.iotone.meshcore.codec.CayenneLpp;
import io.iotone.meshcore.codec.DecodeErrorKind;
import io.iotone.meshcore.codec.FrameBuilder;
import io.iotone.meshcore.codec.MeshcoreFrameCodec;
import io.iotone.meshcore.frames.AckFrame;
import io.iotone.meshcore.frames.ContactFrame;
import io.iotone.meshcore.frames.CustomVarsFrame;
import io.iotone.meshcore.frames.DecodeFailure;
import io.iotone.meshcore.frames.MeshcoreInbound;
import io.iotone.meshcore.frames.MessagesWaitingFrame;
import io.iotone.meshcore.frames.MsgSentFrame;
import io.iotone.meshcore.frames.SelfInfoFrame;
import io.iotone.meshcore.frames.TelemetryResponseFrame;
import io.iotone.meshcore.model.Contact;
import io.iotone.meshcore.model.SelfInfo;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * Programmatic frame goldens (SELF_INFO / CONTACT / ACK / telemetry /
 * custom vars) + decode totality, mirroring the Dart reference tests.
 */
class FrameCodecTest {

    @Test
    void selfInfoAllFieldsScalingTrailingName() {
        byte[] pubkey = new byte[32];
        for (int i = 0; i < 32; i++) {
            pubkey[i] = (byte) i;
        }
        byte[] frame = new FrameBuilder()
                .u8(0x05) // opcode
                .u8(0x01) // advType
                .u8(14) // txPower
                .u8(22) // maxTxPower
                .raw(pubkey)
                .i32(1000000) // lat → 1.0
                .i32(-2000000) // lon → -2.0
                .u8(2) // multiAcks
                .u8(1) // advertLocPolicy
                .u8(0) // telemetry
                .u8(1) // manualAddContacts → true
                .u32(915000) // frequency → 915.0 MHz
                .u32(250000) // bandwidth → 250.0 kHz
                .u8(7) // sf
                .u8(5) // cr
                .utf8String("T1000-E")
                .build();
        // 1 + 3 + 32 + 4 + 4 + 4 + 4 + 2 = 58 header bytes, then name.
        assertEquals(58 + "T1000-E".length(), frame.length);

        SelfInfo s = assertInstanceOf(SelfInfoFrame.class,
                MeshcoreFrameCodec.decode(frame)).selfInfo();
        assertEquals(1, s.advType());
        assertEquals(14, s.txPowerDbm());
        assertEquals(22, s.maxTxPowerDbm());
        assertArrayEquals(pubkey, s.publicKey());
        assertEquals(1.0, s.latitude(), 1e-9);
        assertEquals(-2.0, s.longitude(), 1e-9);
        assertEquals(2, s.multiAcks());
        assertEquals(1, s.advertLocPolicy());
        assertEquals(0, s.telemetryModeRaw());
        assertTrue(s.manualAddContacts());
        assertEquals(915.0, s.frequencyMhz(), 1e-9);
        assertEquals(250.0, s.bandwidthKhz(), 1e-9);
        assertEquals(7, s.spreadingFactor());
        assertEquals(5, s.codingRate());
        assertEquals("T1000-E", s.name());
    }

    @Test
    void selfInfoWithEmptyTrailingNameDecodes() {
        byte[] frame = new FrameBuilder().u8(0x05).zeros(57).build();
        SelfInfo s = assertInstanceOf(SelfInfoFrame.class,
                MeshcoreFrameCodec.decode(frame)).selfInfo();
        assertEquals("", s.name());
    }

    @Test
    void contact148ByteFrameFieldsAndActivePath() {
        byte[] pubkey = new byte[32];
        for (int i = 0; i < 32; i++) {
            pubkey[i] = (byte) (i + 1);
        }
        byte[] path = new byte[64];
        path[0] = (byte) 0xAA;
        path[1] = (byte) 0xBB;
        path[2] = (byte) 0xCC;
        byte[] frame = new FrameBuilder()
                .u8(0x03)
                .raw(pubkey)
                .u8(2) // type
                .u8(0) // flags
                .u8(3) // outPathLen
                .raw(path)
                .fixed("Repeater-1".getBytes(StandardCharsets.UTF_8), 32)
                .u32(0x66000001L) // lastAdvertTs
                .i32(1000000) // lat → 1.0
                .i32(-2000000) // lon → -2.0
                .u32(255) // lastMod
                .build();
        assertEquals(148, frame.length, "CONTACT frame must be 148 bytes");

        Contact ct = assertInstanceOf(ContactFrame.class,
                MeshcoreFrameCodec.decode(frame)).contact();
        assertArrayEquals(pubkey, ct.publicKey());
        assertEquals(2, ct.type());
        assertEquals(0, ct.flags());
        assertEquals(3, ct.outPathLen());
        assertEquals(64, ct.outPath().length);
        assertArrayEquals(new byte[] {(byte) 0xAA, (byte) 0xBB, (byte) 0xCC},
                ct.activePath());
        assertEquals("Repeater-1", ct.name());
        assertEquals(0x66000001L, ct.lastAdvertTimestamp());
        assertEquals(1.0, ct.latitude(), 1e-9);
        assertEquals(-2.0, ct.longitude(), 1e-9);
        assertEquals(255, ct.lastMod());
    }

    @Test
    void contactRoundTripsThroughAddUpdateContact() {
        // decode(CONTACT) → addUpdateContact re-encodes the same 148
        // bytes with only the opcode differing (0x03 → 0x09).
        byte[] pubkey = new byte[32];
        Arrays.fill(pubkey, (byte) 0x42);
        byte[] frame = new FrameBuilder()
                .u8(0x03).raw(pubkey).u8(1).u8(5).u8(0).zeros(64)
                .fixed("R".getBytes(StandardCharsets.UTF_8), 32)
                .u32(7).i32(-1).i32(1).u32(9)
                .build();
        Contact ct = assertInstanceOf(ContactFrame.class,
                MeshcoreFrameCodec.decode(frame)).contact();
        byte[] reEncoded = MeshcoreFrameCodec.addUpdateContact(ct);
        assertEquals(0x09, reEncoded[0] & 0xFF);
        assertArrayEquals(Arrays.copyOfRange(frame, 1, frame.length),
                Arrays.copyOfRange(reEncoded, 1, reEncoded.length));
    }

    @Test
    void customVarsEncodeAndDecode() {
        assertEquals("28", TestUtil.toHex(MeshcoreFrameCodec.getCustomVars()));
        assertEquals("29" + TestUtil.toHex(
                        "gps:1".getBytes(StandardCharsets.UTF_8)),
                TestUtil.toHex(MeshcoreFrameCodec.setCustomVar("gps", "1")));

        byte[] frame = new FrameBuilder()
                .u8(0x15).utf8String("gps:1,gps_interval:30").build();
        Map<String, String> m = assertInstanceOf(CustomVarsFrame.class,
                MeshcoreFrameCodec.decode(frame)).values();
        assertEquals("1", m.get("gps"));
        assertEquals("30", m.get("gps_interval"));
        assertEquals(2, m.size());
    }

    @Test
    void customVarsEmptyAndMalformed() {
        assertTrue(assertInstanceOf(CustomVarsFrame.class,
                MeshcoreFrameCodec.decode(new byte[] {0x15}))
                .values().isEmpty());

        byte[] frame = new FrameBuilder()
                .u8(0x15)
                .utf8String("gps:1,no_colon_here,gps_interval:30")
                .build();
        Map<String, String> m = assertInstanceOf(CustomVarsFrame.class,
                MeshcoreFrameCodec.decode(frame)).values();
        assertEquals(Map.of("gps", "1", "gps_interval", "30"), m);
    }

    @Test
    void decodeIsTotalOnEmptyAndTruncated() {
        DecodeFailure empty = assertInstanceOf(DecodeFailure.class,
                MeshcoreFrameCodec.decode(new byte[0]));
        assertEquals(DecodeErrorKind.EMPTY, empty.error().kind());

        DecodeFailure trunc = assertInstanceOf(DecodeFailure.class,
                MeshcoreFrameCodec.decode(TestUtil.hex("020100")));
        assertEquals(DecodeErrorKind.TRUNCATED, trunc.error().kind());
        assertEquals(0x02, trunc.error().opcode().getAsInt());

        assertInstanceOf(DecodeFailure.class,
                MeshcoreFrameCodec.decode(TestUtil.hex("0501")));
    }

    @Test
    void ackDecodesAndCorrelatesWithMsgSent() {
        AckFrame ack = assertInstanceOf(AckFrame.class,
                MeshcoreFrameCodec.decode(TestUtil.hex("8278563412")));
        assertEquals(0x12345678L, ack.ackCrc());

        MsgSentFrame sent = assertInstanceOf(MsgSentFrame.class,
                MeshcoreFrameCodec.decode(
                        TestUtil.hex("060078563412e8030000")));
        assertEquals(sent.sent().expectedAck(), ack.ackCrc());

        // Short/empty payload decodes to ack 0 (no throw).
        assertEquals(0, assertInstanceOf(AckFrame.class,
                MeshcoreFrameCodec.decode(TestUtil.hex("82"))).ackCrc());
    }

    @Test
    void telemetryReqEncodersSelfAndPeer() {
        byte[] self = MeshcoreFrameCodec.sendTelemetryReq();
        assertArrayEquals(new byte[] {0x27, 0, 0, 0}, self);

        byte[] pub = new byte[32];
        for (int i = 0; i < 32; i++) {
            pub[i] = (byte) (i + 1);
        }
        byte[] peer = MeshcoreFrameCodec.sendTelemetryReq(pub);
        assertEquals(36, peer.length);
        assertArrayEquals(new byte[] {0x27, 0, 0, 0},
                Arrays.copyOfRange(peer, 0, 4));
        assertArrayEquals(pub, Arrays.copyOfRange(peer, 4, 36));
    }

    @Test
    void telemetryResponseDecodes() {
        byte[] frame = TestUtil.hex(
                "8B" + "00" + "AABBCCDDEEFF"
                + "018806765FF2960A0003E8"); // ch=1 GPS entry
        TelemetryResponseFrame t = assertInstanceOf(
                TelemetryResponseFrame.class,
                MeshcoreFrameCodec.decode(frame));
        assertEquals("aabbccddeeff", TestUtil.toHex(t.pubKeyPrefix()));
        assertEquals(11, t.lppPayload().length);
        assertEquals(10.0,
                CayenneLpp.decode(t.lppPayload()).get(0).gps()[2], 1e-2);

        // Empty LPP payload decodes safely.
        TelemetryResponseFrame empty = assertInstanceOf(
                TelemetryResponseFrame.class,
                MeshcoreFrameCodec.decode(TestUtil.hex("8B00AABBCCDDEEFF")));
        assertEquals(0, empty.lppPayload().length);

        // Truncated pubkey prefix → DecodeFailure.
        assertInstanceOf(DecodeFailure.class,
                MeshcoreFrameCodec.decode(TestUtil.hex("8B00AABB")));
    }

    @Test
    void messagesWaitingWithAndWithoutCount() {
        MessagesWaitingFrame bare = assertInstanceOf(
                MessagesWaitingFrame.class,
                MeshcoreFrameCodec.decode(TestUtil.hex("83")));
        assertTrue(bare.count().isEmpty());

        MessagesWaitingFrame counted = assertInstanceOf(
                MessagesWaitingFrame.class,
                MeshcoreFrameCodec.decode(TestUtil.hex("8303")));
        assertEquals(3, counted.count().getAsInt());
    }
}
