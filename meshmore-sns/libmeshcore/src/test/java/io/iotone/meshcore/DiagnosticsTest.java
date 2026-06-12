// Copyright (c) 2026 IoTone, Inc.
// SPDX-License-Identifier: MIT
package io.iotone.meshcore;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.iotone.meshcore.codec.MeshcoreFrameCodec;
import io.iotone.meshcore.crypto.ChannelCrypto;
import io.iotone.meshcore.diagnostics.ChannelTailHypothesis;
import io.iotone.meshcore.diagnostics.ChannelTailOracle;
import io.iotone.meshcore.diagnostics.ChannelTailResult;
import io.iotone.meshcore.frames.AdvertFrame;
import io.iotone.meshcore.frames.RfLogFrame;
import io.iotone.meshcore.model.Advert;
import io.iotone.meshcore.model.GrpTxtPayload;
import io.iotone.meshcore.model.OtaPacket;
import io.iotone.meshcore.model.RfLog;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

/**
 * OTA packet parsing (RF-log wrapped) + the channel-secret-tail oracle,
 * mirroring the Dart reference tests.
 */
class DiagnosticsTest {

    /**
     * Builds a GRP_TXT over-the-air packet (FLOOD route, 0 hops)
     * carrying {@code encryptThenMac(secret, plaintext)}, wrapped in a
     * 0x88 RF-log frame — a synthetic stand-in for a device capture.
     */
    private static byte[] rfLogGrpTxt(byte[] secret32, byte[] plaintext) {
        int header = (MeshcoreConstants.PAYLOAD_TYPE_GRP_TXT
                << MeshcoreConstants.PKT_PAYLOAD_TYPE_SHIFT)
                | MeshcoreConstants.ROUTE_FLOOD; // ver 0
        int channelHash = ChannelCrypto.channelHashFromPsk(
                Arrays.copyOfRange(secret32, 0, 16));
        byte[] macCt = ChannelCrypto.encryptThenMac(secret32, plaintext);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(0x88);
        out.write(7 * 4); // snr +7.0
        out.write(-92 & 0xFF); // rssi -92
        out.write(header);
        out.write(0x00); // path-len: 0 hops, 1-byte hash code
        out.write(channelHash);
        out.write(macCt, 0, macCt.length);
        return out.toByteArray();
    }

    private static byte[] advPayload(String name) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (int i = 0; i < 32; i++) {
            out.write(i + 3); // pubkey
        }
        out.write(0x04); // ts 0x01020304 LE
        out.write(0x03);
        out.write(0x02);
        out.write(0x01);
        for (int i = 0; i < 64; i++) {
            out.write(0x55); // sig
        }
        out.write(MeshcoreConstants.ADV_TYPE_CHAT
                | MeshcoreConstants.ADV_NAME_MASK);
        byte[] n = name.getBytes(StandardCharsets.UTF_8);
        out.write(n, 0, n.length);
        return out.toByteArray();
    }

    @Test
    void otaAdvertDecodesFromRfLogFrame() {
        int header = (MeshcoreConstants.PAYLOAD_TYPE_ADVERT
                << MeshcoreConstants.PKT_PAYLOAD_TYPE_SHIFT)
                | MeshcoreConstants.ROUTE_FLOOD;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(0x88);
        out.write(5 * 4); // snr +5.0
        out.write(-88 & 0xFF); // rssi -88
        out.write(header);
        out.write(0x00); // path-len 0 hops
        byte[] p = advPayload("NodeQ");
        out.write(p, 0, p.length);

        RfLog log = assertInstanceOf(RfLogFrame.class,
                MeshcoreFrameCodec.decode(out.toByteArray())).log();
        assertEquals(5.0, log.snrDb(), 1e-9);
        assertEquals(-88, log.rssi());
        OtaPacket pkt = log.packet();
        assertNotNull(pkt);
        Advert a = pkt.advert();
        assertNotNull(a);
        assertEquals("NodeQ", a.name());
        assertEquals(MeshcoreConstants.ADV_TYPE_CHAT, a.type());
        assertEquals(0x01020304L, a.timestamp());
    }

    @Test
    void advertParseMatchesCompanionPushDecode() {
        byte[] payload = advPayload("Twin");
        Advert viaParse = Advert.parse(payload);
        ByteArrayOutputStream frame = new ByteArrayOutputStream();
        frame.write(0x80);
        frame.write(payload, 0, payload.length);
        Advert viaPush = assertInstanceOf(AdvertFrame.class,
                MeshcoreFrameCodec.decode(frame.toByteArray())).advert();
        assertEquals(viaParse.name(), viaPush.name());
        assertEquals(viaParse.timestamp(), viaPush.timestamp());
        assertArrayEquals(viaParse.publicKey(), viaPush.publicKey());
        assertArrayEquals(viaParse.signedMessage(), viaPush.signedMessage());
    }

    @Test
    void advertTryParseNullOnShortPayload() {
        assertNull(Advert.tryParse(new byte[10]));
    }

    @Test
    void rfLogGrpTxtParsesAndChannelHashMatches() {
        byte[] secret = ChannelCrypto.channelSecretFromPsk(
                MeshcoreConstants.publicChannelPsk()); // zeros tail
        byte[] pt = "hello public".getBytes(StandardCharsets.UTF_8);
        RfLog log = assertInstanceOf(RfLogFrame.class,
                MeshcoreFrameCodec.decode(rfLogGrpTxt(secret, pt))).log();
        assertEquals(7.0, log.snrDb(), 1e-9);
        assertEquals(-92, log.rssi());

        OtaPacket pkt = log.packet();
        assertNotNull(pkt);
        assertTrue(pkt.isGrpTxt());
        GrpTxtPayload grp = pkt.grpTxt();
        assertNotNull(grp);
        assertEquals(0x11, grp.channelHash()); // public channel hash
    }

    @Test
    void oracleResolvesEveryTailHypothesis() {
        byte[] psk = MeshcoreConstants.publicChannelPsk();
        byte[] pt = "tail probe".getBytes(StandardCharsets.UTF_8);
        for (ChannelTailHypothesis h : ChannelTailHypothesis.values()) {
            byte[] secret = ChannelTailOracle.secretFor(h, psk);
            RfLog log = assertInstanceOf(RfLogFrame.class,
                    MeshcoreFrameCodec.decode(rfLogGrpTxt(secret, pt))).log();
            GrpTxtPayload grp = log.packet().grpTxt();
            ChannelTailResult r = ChannelTailOracle.resolveChannelTail(
                    psk, pt, grp);
            assertTrue(r.resolved(), "must resolve for " + h);
            assertEquals(h, r.match());
            assertTrue(r.channelHashOk());
            assertArrayEquals(pt, r.recoveredPlaintext());
        }
    }

    @Test
    void oracleUnresolvedForWrongPsk() {
        byte[] psk = MeshcoreConstants.publicChannelPsk();
        byte[] wrongSecret = new byte[32];
        Arrays.fill(wrongSecret, (byte) 0x77);
        byte[] pt = "??".getBytes(StandardCharsets.UTF_8);
        RfLog log = assertInstanceOf(RfLogFrame.class,
                MeshcoreFrameCodec.decode(rfLogGrpTxt(wrongSecret, pt))).log();
        ChannelTailResult r = ChannelTailOracle.resolveChannelTail(
                psk, pt, log.packet().grpTxt());
        assertEquals(false, r.resolved());
        assertNull(r.recoveredPlaintext());
    }
}
