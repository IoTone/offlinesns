// Copyright (c) 2026 IoTone, Inc.
// SPDX-License-Identifier: MIT
package io.iotone.meshcore;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import io.iotone.meshcore.codec.DecodeErrorKind;
import io.iotone.meshcore.codec.FrameBuilder;
import io.iotone.meshcore.codec.MeshcoreFrameCodec;
import io.iotone.meshcore.frames.ChannelInfoFrame;
import io.iotone.meshcore.frames.ChannelMessageFrame;
import io.iotone.meshcore.frames.DecodeFailure;
import io.iotone.meshcore.frames.DeviceInfoFrame;
import io.iotone.meshcore.model.ChannelInfo;
import io.iotone.meshcore.model.DeviceInfo;
import io.iotone.meshcore.model.RadioParams;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

/**
 * Channel/config programmatic goldens beyond the JSON vectors:
 * SET_CHANNEL layout, CHANNEL_INFO and DEVICE_INFO decodes, truncation
 * totality, and the radio-parameter scaling round-trip — mirroring the
 * Dart {@code channel_codec_test} / {@code config_codec_test}.
 */
class ChannelConfigCodecTest {

    @Test
    void setChannelFiftyByteLayout() {
        byte[] psk = new byte[16];
        for (int i = 0; i < 16; i++) {
            psk[i] = (byte) i;
        }
        byte[] f = MeshcoreFrameCodec.setChannel(1, "public", psk);
        assertEquals(50, f.length, "1+1+32+16");
        assertEquals(0x20, f[0] & 0xFF);
        assertEquals(1, f[1]);
        assertEquals("public", new String(f, 2, 6, StandardCharsets.UTF_8));
        assertArrayEquals(new byte[26], Arrays.copyOfRange(f, 8, 34));
        assertArrayEquals(psk, Arrays.copyOfRange(f, 34, 50));
    }

    @Test
    void setChannelTruncatesNameZeroPadsPsk() {
        byte[] f = MeshcoreFrameCodec.setChannel(0, "X".repeat(40),
                new byte[] {1, 2, 3});
        assertEquals(50, f.length);
        byte[] allX = new byte[32];
        Arrays.fill(allX, (byte) 'X');
        assertArrayEquals(allX, Arrays.copyOfRange(f, 2, 34));
        byte[] paddedPsk = new byte[16];
        paddedPsk[0] = 1;
        paddedPsk[1] = 2;
        paddedPsk[2] = 3;
        assertArrayEquals(paddedPsk, Arrays.copyOfRange(f, 34, 50));
    }

    @Test
    void channelInfoDecode() {
        byte[] psk = new byte[16];
        for (int i = 0; i < 16; i++) {
            psk[i] = (byte) (i + 1);
        }
        byte[] frame = new FrameBuilder()
                .u8(0x12)
                .u8(7)
                .fixed("public".getBytes(StandardCharsets.UTF_8), 32)
                .raw(psk)
                .build();
        assertEquals(50, frame.length);

        ChannelInfo ci = assertInstanceOf(ChannelInfoFrame.class,
                MeshcoreFrameCodec.decode(frame)).info();
        assertEquals(7, ci.channelIdx());
        assertEquals("public", ci.name());
        assertArrayEquals(psk, ci.psk());
    }

    @Test
    void deviceInfoDecodeFixedStringsAndDoubledContacts() {
        byte[] frame = new FrameBuilder()
                .u8(0x0D)
                .u8(10) // fw ver code
                .u8(50) // max_contacts / 2 → 100
                .u8(8) // max group channels
                .u32(123456) // ble pin
                .fixed("2026-05-01".getBytes(StandardCharsets.UTF_8), 12)
                .fixed("IoTone Japan".getBytes(StandardCharsets.UTF_8), 40)
                .fixed("1.15.0".getBytes(StandardCharsets.UTF_8), 20)
                .u8(0) // client_repeat
                .u8(1) // path_hash_mode
                .build();
        assertEquals(82, frame.length, "1+1+1+1+4+12+40+20+1+1");

        DeviceInfo d = assertInstanceOf(DeviceInfoFrame.class,
                MeshcoreFrameCodec.decode(frame)).info();
        assertEquals(10, d.firmwareVerCode());
        assertEquals(100, d.maxContacts());
        assertEquals(8, d.maxGroupChannels());
        assertEquals(123456L, d.blePin());
        assertEquals("2026-05-01", d.firmwareBuildDate());
        assertEquals("IoTone Japan", d.manufacturer());
        assertEquals("1.15.0", d.firmwareVersion());
        assertEquals(0, d.clientRepeat());
        assertEquals(1, d.pathHashMode());
    }

    @Test
    void truncatedConfigFramesFailControlled() {
        DecodeFailure batt = assertInstanceOf(DecodeFailure.class,
                MeshcoreFrameCodec.decode(TestUtil.hex("0c0410")));
        assertEquals(DecodeErrorKind.TRUNCATED, batt.error().kind());

        DecodeFailure dev = assertInstanceOf(DecodeFailure.class,
                MeshcoreFrameCodec.decode(TestUtil.hex("0d0a3208")));
        assertEquals(DecodeErrorKind.TRUNCATED, dev.error().kind());

        DecodeFailure sent = assertInstanceOf(DecodeFailure.class,
                MeshcoreFrameCodec.decode(TestUtil.hex("060178")));
        assertEquals(DecodeErrorKind.TRUNCATED, sent.error().kind());

        // V3 channel msg cut inside the reserved bytes.
        DecodeFailure v3 = assertInstanceOf(DecodeFailure.class,
                MeshcoreFrameCodec.decode(TestUtil.hex("111e")));
        assertEquals(DecodeErrorKind.TRUNCATED, v3.error().kind());
    }

    @Test
    void emptyTextChannelMessageDecodes() {
        // Legacy 0x08 with header only — text region empty.
        byte[] frame = new FrameBuilder()
                .u8(0x08).u8(0).u8(3).u8(0).u32(16909060).build();
        ChannelMessageFrame f = assertInstanceOf(ChannelMessageFrame.class,
                MeshcoreFrameCodec.decode(frame));
        assertEquals("", f.message().text());
    }

    @Test
    void radioParamsScalingRoundTripsAgainstSelfInfoScale() {
        byte[] f = MeshcoreFrameCodec.setRadioParams(
                RadioParams.of(868.5, 125.0, 9, 6));
        // [0B][freq u32][bw u32][sf][cr]
        long freqRaw = (f[1] & 0xFFL) | ((f[2] & 0xFFL) << 8)
                | ((f[3] & 0xFFL) << 16) | ((f[4] & 0xFFL) << 24);
        long bwRaw = (f[5] & 0xFFL) | ((f[6] & 0xFFL) << 8)
                | ((f[7] & 0xFFL) << 16) | ((f[8] & 0xFFL) << 24);
        assertEquals(868.5, freqRaw / 1000.0, 1e-9);
        assertEquals(125.0, bwRaw / 1000.0, 1e-9);
        assertEquals(9, f[9]);
        assertEquals(6, f[10]);
    }
}
