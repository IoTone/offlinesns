// Copyright (c) 2026 IoTone, Inc.
// SPDX-License-Identifier: MIT
package io.iotone.meshcore;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import io.iotone.meshcore.crypto.ChannelCrypto;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

/**
 * Channel-cipher KATs (NIST / RFC anchors) + the MeshCore
 * encryptThenMAC composition, mirroring the Dart reference tests.
 */
class ChannelCryptoTest {

    private static final byte[] KEY =
            TestUtil.hex("000102030405060708090a0b0c0d0e0f");
    private static final byte[] PT =
            TestUtil.hex("00112233445566778899aabbccddeeff");
    private static final byte[] CT =
            TestUtil.hex("69c4e0d86a7b0430d8cdb78070b4c55a");

    @Test
    void aesEcbEncryptKat() {
        assertArrayEquals(CT, ChannelCrypto.aes128EcbEncrypt(KEY, PT));
    }

    @Test
    void aesEcbDecryptKat() {
        assertArrayEquals(PT, ChannelCrypto.aes128EcbDecrypt(KEY, CT));
    }

    @Test
    void aesEcbZeroPadsTrailingPartialBlock() {
        byte[] enc = ChannelCrypto.aes128EcbEncrypt(KEY, TestUtil.hex("0011"));
        assertEquals(16, enc.length);
        byte[] padded = new byte[16];
        padded[0] = 0x00;
        padded[1] = 0x11;
        assertArrayEquals(ChannelCrypto.aes128EcbEncrypt(KEY, padded), enc);
    }

    @Test
    void sha256Kat() {
        assertEquals(
                "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
                TestUtil.toHex(ChannelCrypto.sha256(
                        "abc".getBytes(StandardCharsets.UTF_8))));
    }

    @Test
    void sha256PairEqualsConcatenation() {
        byte[] a = "ab".getBytes(StandardCharsets.UTF_8);
        byte[] b = "c".getBytes(StandardCharsets.UTF_8);
        assertArrayEquals(
                ChannelCrypto.sha256("abc".getBytes(StandardCharsets.UTF_8)),
                ChannelCrypto.sha256Pair(a, b));
    }

    @Test
    void hmacSha256Rfc4231Case2() {
        assertEquals(
                "5bdcc146bf60754e6a042426089575c75a003f089d2739839dec58b964ec3843",
                TestUtil.toHex(ChannelCrypto.hmacSha256(
                        "Jefe".getBytes(StandardCharsets.UTF_8),
                        "what do ya want for nothing?"
                                .getBytes(StandardCharsets.UTF_8))));
    }

    private static byte[] secret32() {
        byte[] s = new byte[32];
        for (int i = 0; i < 32; i++) {
            s[i] = (byte) i;
        }
        return s;
    }

    private static final byte[] PLAINTEXT =
            "MESHMORE-SNS hello".getBytes(StandardCharsets.UTF_8);

    @Test
    void frameIsTruncatedHmacPlusCiphertext() {
        byte[] secret = secret32();
        byte[] key = Arrays.copyOfRange(secret, 0,
                MeshcoreConstants.CIPHER_KEY_SIZE);
        byte[] ct = ChannelCrypto.aes128EcbEncrypt(key, PLAINTEXT);
        byte[] mac = ChannelCrypto.hmacSha256(secret, ct);

        byte[] frame = ChannelCrypto.encryptThenMac(secret, PLAINTEXT);
        assertEquals(MeshcoreConstants.CIPHER_MAC_SIZE + ct.length,
                frame.length);
        assertEquals(0, ct.length % MeshcoreConstants.CIPHER_BLOCK_SIZE);
        assertArrayEquals(
                Arrays.copyOfRange(mac, 0, MeshcoreConstants.CIPHER_MAC_SIZE),
                Arrays.copyOfRange(frame, 0,
                        MeshcoreConstants.CIPHER_MAC_SIZE));
        assertArrayEquals(ct, Arrays.copyOfRange(frame,
                MeshcoreConstants.CIPHER_MAC_SIZE, frame.length));
    }

    @Test
    void roundTripsWithZeroPaddedPlaintext() {
        byte[] secret = secret32();
        byte[] frame = ChannelCrypto.encryptThenMac(secret, PLAINTEXT);
        byte[] dec = ChannelCrypto.macThenDecrypt(secret, frame);
        assertNotNull(dec);
        assertEquals(0, dec.length % MeshcoreConstants.CIPHER_BLOCK_SIZE);
        assertArrayEquals(PLAINTEXT,
                Arrays.copyOfRange(dec, 0, PLAINTEXT.length));
        for (int i = PLAINTEXT.length; i < dec.length; i++) {
            assertEquals(0, dec[i]);
        }
    }

    @Test
    void exactBlockMultipleNotOverPadded() {
        byte[] pt16 = new byte[16];
        pt16[0] = 1;
        pt16[1] = 2;
        pt16[2] = 3;
        byte[] frame = ChannelCrypto.encryptThenMac(secret32(), pt16);
        assertEquals(MeshcoreConstants.CIPHER_MAC_SIZE + 16, frame.length);
    }

    @Test
    void tamperedMacReturnsNull() {
        byte[] frame = ChannelCrypto.encryptThenMac(secret32(), PLAINTEXT);
        frame[0] ^= (byte) 0xFF;
        assertNull(ChannelCrypto.macThenDecrypt(secret32(), frame));
    }

    @Test
    void tamperedCiphertextReturnsNull() {
        byte[] frame = ChannelCrypto.encryptThenMac(secret32(), PLAINTEXT);
        frame[frame.length - 1] ^= 0x01;
        assertNull(ChannelCrypto.macThenDecrypt(secret32(), frame));
    }

    @Test
    void shortFramesReturnNull() {
        assertNull(ChannelCrypto.macThenDecrypt(secret32(),
                TestUtil.hex("00")));
        assertNull(ChannelCrypto.macThenDecrypt(secret32(),
                TestUtil.hex("0011")));
    }

    @Test
    void channelHashFromPskIsFirstShaByte() {
        byte[] psk = new byte[16];
        for (int i = 0; i < 16; i++) {
            psk[i] = (byte) (i * 7);
        }
        assertEquals(ChannelCrypto.sha256(psk)[0] & 0xFF,
                ChannelCrypto.channelHashFromPsk(psk));
    }

    @Test
    void hashtagDerivationKat() {
        assertEquals("9cd8fcf22a47333b591d96a2b848b73f",
                TestUtil.toHex(ChannelCrypto.channelPskFromHashtag("#test")));
    }

    @Test
    void publicChannelHashIs0x11() {
        assertEquals(0x11, ChannelCrypto.channelHashFromPsk(
                MeshcoreConstants.publicChannelPsk()));
    }

    @Test
    void channelSecretFromPskLayout() {
        byte[] psk = new byte[16];
        for (int i = 0; i < 16; i++) {
            psk[i] = (byte) (i + 1);
        }
        byte[] s = ChannelCrypto.channelSecretFromPsk(psk);
        assertEquals(MeshcoreConstants.CHANNEL_SECRET_SIZE, s.length);
        assertArrayEquals(psk, Arrays.copyOfRange(s, 0, 16));
        assertArrayEquals(new byte[16], Arrays.copyOfRange(s, 16, 32));
    }
}
