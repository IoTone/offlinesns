// Copyright (c) 2026 IoTone, Inc.
// SPDX-License-Identifier: MIT
package io.iotone.meshcore;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.iotone.meshcore.codec.DecodeErrorKind;
import io.iotone.meshcore.codec.FrameBuilder;
import io.iotone.meshcore.codec.MeshcoreFrameCodec;
import io.iotone.meshcore.crypto.ChannelCrypto;
import io.iotone.meshcore.frames.AdvertFrame;
import io.iotone.meshcore.frames.DecodeFailure;
import io.iotone.meshcore.model.Advert;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

/**
 * Advert (0x80) programmatic goldens + the well-known public-channel
 * constants, mirroring the Dart {@code contact_advert_codec_test}.
 */
class AdvertCodecTest {

    @Test
    void advertWithLatLonAndNameSignedMessageExact() {
        byte[] pk = new byte[32];
        for (int i = 0; i < 32; i++) {
            pk[i] = (byte) i;
        }
        byte[] sig = new byte[64];
        Arrays.fill(sig, (byte) 0x55);
        // flags: ADV_NAME(0x80) | ADV_LATLON(0x10) | type CHAT(1) = 0x91
        byte[] appData = new FrameBuilder()
                .u8(0x91)
                .i32(1000000) // lat → 1.0
                .i32(-2000000) // lon → -2.0
                .utf8String("RPTR-1")
                .build();
        byte[] frame = new FrameBuilder()
                .u8(0x80)
                .raw(pk)
                .u32(0x01020304L)
                .raw(sig)
                .raw(appData)
                .build();

        Advert a = assertInstanceOf(AdvertFrame.class,
                MeshcoreFrameCodec.decode(frame)).advert();
        assertArrayEquals(pk, a.publicKey());
        assertEquals(0x01020304L, a.timestamp());
        assertArrayEquals(sig, a.signature());
        assertEquals(0x91, a.flags());
        assertEquals(MeshcoreConstants.ADV_TYPE_CHAT, a.type());
        assertEquals(1.0, a.latitude(), 1e-9);
        assertEquals(-2.0, a.longitude(), 1e-9);
        assertTrue(a.feat1().isEmpty());
        assertTrue(a.feat2().isEmpty());
        assertEquals("RPTR-1", a.name());
        // signedMessage = pub_key ‖ ts(4 LE) ‖ app_data
        byte[] expected = new FrameBuilder()
                .raw(pk).u32(0x01020304L).raw(appData).build();
        assertArrayEquals(expected, a.signedMessage());
    }

    @Test
    void advertWithNoAppData() {
        byte[] pk = new byte[32];
        Arrays.fill(pk, (byte) 7);
        byte[] sig = new byte[64];
        Arrays.fill(sig, (byte) 9);
        byte[] frame = new FrameBuilder()
                .u8(0x80).raw(pk).u32(0).raw(sig).build();

        Advert a = assertInstanceOf(AdvertFrame.class,
                MeshcoreFrameCodec.decode(frame)).advert();
        assertEquals(0, a.appData().length);
        assertEquals(0, a.flags());
        assertNull(a.name());
        assertNull(a.latitude());
    }

    @Test
    void truncatedAdvertFailsControlled() {
        // Signature should be 64 bytes; give 10.
        byte[] frame = new FrameBuilder()
                .u8(0x80).zeros(32).u32(0).zeros(10).build();
        DecodeFailure df = assertInstanceOf(DecodeFailure.class,
                MeshcoreFrameCodec.decode(frame));
        assertEquals(DecodeErrorKind.TRUNCATED, df.error().kind());
    }

    @Test
    void publicChannelConstants() {
        assertEquals("Public", MeshcoreConstants.PUBLIC_CHANNEL_NAME);
        byte[] psk = MeshcoreConstants.publicChannelPsk();
        assertEquals(16, psk.length);
        assertEquals("8b3387e9c5cdea6ac9e5edbaa115cd72", TestUtil.toHex(psk));
    }

    @Test
    void publicChannelSecretAndHashDeterministic() {
        byte[] psk = MeshcoreConstants.publicChannelPsk();
        byte[] secret = ChannelCrypto.channelSecretFromPsk(psk);
        assertEquals(MeshcoreConstants.CHANNEL_SECRET_SIZE, secret.length);
        assertArrayEquals(psk, Arrays.copyOfRange(secret, 0, 16));
        assertArrayEquals(new byte[16], Arrays.copyOfRange(secret, 16, 32));
        // On-air channel hash is SHA256(psk16)[0] — keyed on the PSK,
        // NOT the 32-byte secret. Cross-checked vs docs: public → 0x11.
        assertEquals(ChannelCrypto.sha256(psk)[0] & 0xFF,
                ChannelCrypto.channelHashFromPsk(psk));
        assertEquals(0x11, ChannelCrypto.channelHashFromPsk(psk));
    }

    @Test
    void utf8RoundTripInChannelText() {
        // Multi-byte UTF-8 (Japanese) survives encode → decode.
        String text = "天気は晴れです";
        byte[] enc = MeshcoreFrameCodec.sendChannelTextMessage(
                0, 1700000000L, text);
        byte[] textBytes = Arrays.copyOfRange(enc, 7, enc.length);
        assertEquals(text, new String(textBytes, StandardCharsets.UTF_8));
    }
}
