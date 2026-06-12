// Copyright (c) 2026 IoTone, Inc.
// SPDX-License-Identifier: MIT
package io.iotone.meshcore.crypto;

import io.iotone.meshcore.MeshcoreConstants;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * MeshCore group/channel cipher — a faithful Java port of
 * {@code Utils::encrypt/decrypt/encryptThenMAC/MACThenDecrypt} and the
 * channel hash, transcribed from {@code src/Utils.cpp} /
 * {@code src/Mesh.h} / {@code src/MeshCore.h} at the pinned commit.
 *
 * <p>Scheme (verbatim from firmware):</p>
 * <ul>
 *   <li>Cipher: <strong>AES-128 ECB</strong>, 16-byte blocks, no IV. On
 *       encrypt, a trailing partial block is zero-padded to 16
 *       (ciphertext length is always a multiple of 16). On decrypt,
 *       every block is decrypted and the padded length returned — the
 *       <em>plaintext</em> length is recovered from the inner message
 *       structure, not from padding.</li>
 *   <li>AES key = the first
 *       {@link MeshcoreConstants#CIPHER_KEY_SIZE} (16) bytes of the
 *       secret.</li>
 *   <li>MAC = HMAC-SHA256 keyed with the <strong>full</strong> secret
 *       ({@link MeshcoreConstants#CHANNEL_SECRET_SIZE} = 32 bytes),
 *       over the ciphertext, truncated to
 *       {@link MeshcoreConstants#CIPHER_MAC_SIZE} (2) bytes.</li>
 *   <li>Frame body = {@code [MAC (2)] [ciphertext (16-aligned)]}.</li>
 *   <li>Channel hash = first byte of {@code SHA256(psk)}.</li>
 * </ul>
 *
 * <p><strong>Secret length.</strong> {@code mesh::GroupChannel.secret}
 * is 32 bytes, and the HMAC is keyed over all 32. The companion link
 * only conveys the first 16 (the AES PSK); the firmware zero-pads to 32
 * — {@link #channelSecretFromPsk(byte[])} applies that authoritative
 * construction ({@code psk ‖ 0x00·16}).</p>
 *
 * <p>Uses only {@code javax.crypto} / {@code java.security} primitives
 * (AES/ECB, HmacSHA256, SHA-256) — present on every Android API level
 * and JDK; no provider registration required. AES-ECB is mandated by
 * the firmware wire format, not a choice this library makes.</p>
 */
public final class ChannelCrypto {

    private ChannelCrypto() {
        // Static crypto only.
    }

    /**
     * SHA-256 digest.
     *
     * @param data input bytes
     * @return the 32-byte digest
     */
    public static byte[] sha256(byte[] data) {
        return sha256Digest().digest(data);
    }

    /**
     * SHA-256 over two fragments (mirrors the firmware's 2-arg
     * {@code Utils::sha256}).
     *
     * @param a first fragment
     * @param b second fragment
     * @return the 32-byte digest of {@code a ‖ b}
     */
    public static byte[] sha256Pair(byte[] a, byte[] b) {
        MessageDigest d = sha256Digest();
        d.update(a);
        d.update(b);
        return d.digest();
    }

    /**
     * On-air channel hash — {@code SHA256(psk)[0]} over the
     * <strong>16-byte PSK</strong> ({@code PATH_HASH_SIZE} = 1).
     * Identifies which channel a GRP_TXT packet belongs to. The hash is
     * keyed on the PSK only — independent of the 32-byte HMAC secret.
     *
     * @param psk the 16-byte channel pre-shared key
     * @return the channel hash byte in {@code [0, 255]}
     */
    public static int channelHashFromPsk(byte[] psk) {
        return sha256(psk)[0] & 0xFF;
    }

    /**
     * Channel hash for a {@code #hashtag}-named channel:
     * {@code SHA256(channelPskFromHashtag(tag))[0]}.
     *
     * @param tag the hashtag channel name (e.g. {@code "#test"})
     * @return the channel hash byte in {@code [0, 255]}
     */
    public static int channelHashFromHashtag(String tag) {
        return channelHashFromPsk(channelPskFromHashtag(tag));
    }

    /**
     * Derives a 16-byte channel PSK from a {@code #hashtag} name:
     * {@code SHA256(utf8(tag))[0..16]}. Confirmed: {@code #test} &#8594;
     * {@code 9cd8fcf22a47333b591d96a2b848b73f}.
     *
     * @param tag the hashtag channel name
     * @return the 16-byte PSK
     */
    public static byte[] channelPskFromHashtag(String tag) {
        return Arrays.copyOfRange(
                sha256(tag.getBytes(StandardCharsets.UTF_8)),
                0,
                MeshcoreConstants.CIPHER_KEY_SIZE);
    }

    /**
     * Builds the 32-byte channel secret from the 16-byte PSK:
     * <strong>{@code psk ‖ 0x00·16}</strong>. Authoritative — the
     * companion protocol only carries 16 bytes and the firmware
     * zero-pads to {@code PUB_KEY_SIZE} for the HMAC key. The AES-128
     * key is the first 16 bytes (= the PSK).
     *
     * @param psk the channel PSK (truncated to 16 bytes if longer)
     * @return the 32-byte channel secret
     */
    public static byte[] channelSecretFromPsk(byte[] psk) {
        byte[] s = new byte[MeshcoreConstants.CHANNEL_SECRET_SIZE];
        int n = Math.min(psk.length, MeshcoreConstants.CIPHER_KEY_SIZE);
        System.arraycopy(psk, 0, s, 0, n);
        return s;
    }

    /**
     * HMAC-SHA256(key, msg) — full 32-byte tag.
     *
     * @param key MAC key (any length)
     * @param msg message bytes
     * @return the 32-byte tag
     */
    public static byte[] hmacSha256(byte[] key, byte[] msg) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            return mac.doFinal(msg);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            // HmacSHA256 is mandatory on every JDK/Android.
            throw new IllegalStateException("HmacSHA256 unavailable", e);
        }
    }

    /**
     * AES-128-ECB encrypt, zero-padding a trailing partial block.
     * Mirrors {@code Utils::encrypt}.
     *
     * @param key the 16-byte AES key
     * @param src plaintext of any length
     * @return ciphertext, always a multiple of 16 bytes
     */
    public static byte[] aes128EcbEncrypt(byte[] key, byte[] src) {
        int full = src.length / MeshcoreConstants.CIPHER_BLOCK_SIZE;
        int rem = src.length % MeshcoreConstants.CIPHER_BLOCK_SIZE;
        int outLen = (full + (rem > 0 ? 1 : 0))
                * MeshcoreConstants.CIPHER_BLOCK_SIZE;
        byte[] padded = Arrays.copyOf(src, outLen); // zero-pads the tail
        return aesEcb(Cipher.ENCRYPT_MODE, key, padded);
    }

    /**
     * AES-128-ECB decrypt. Mirrors {@code Utils::decrypt}: returns the
     * full (still padded) plaintext — the caller recovers the true
     * length from the inner message structure.
     *
     * @param key the 16-byte AES key
     * @param src ciphertext; length must be a multiple of 16
     * @return the block-padded plaintext
     * @throws IllegalArgumentException if {@code src} is not
     *         block-aligned
     */
    public static byte[] aes128EcbDecrypt(byte[] key, byte[] src) {
        if (src.length % MeshcoreConstants.CIPHER_BLOCK_SIZE != 0) {
            throw new IllegalArgumentException(
                    "ciphertext not block-aligned: " + src.length);
        }
        return aesEcb(Cipher.DECRYPT_MODE, key, src);
    }

    private static byte[] aesEcb(int mode, byte[] key, byte[] alignedSrc) {
        try {
            // AES/ECB/NoPadding is the firmware's wire format (block-by-
            // block, zero-padded final block) — not a mode this library
            // chose. Present on every Android API level and JDK.
            Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
            cipher.init(mode, new SecretKeySpec(key, "AES"));
            return cipher.doFinal(alignedSrc);
        } catch (java.security.GeneralSecurityException e) {
            // AES/ECB/NoPadding is mandatory on every JDK/Android; a
            // failure here means a programming error (e.g. bad key size).
            throw new IllegalStateException("AES-128-ECB failed", e);
        }
    }

    /**
     * {@code Utils::encryptThenMAC} &#8594;
     * {@code [MAC (2)] [ciphertext]}.
     *
     * @param secret    the 32-byte channel secret (or DM shared secret)
     * @param plaintext message bytes
     * @return the MAC-prefixed ciphertext frame body
     */
    public static byte[] encryptThenMac(byte[] secret, byte[] plaintext) {
        byte[] key = Arrays.copyOfRange(secret, 0,
                MeshcoreConstants.CIPHER_KEY_SIZE);
        byte[] ct = aes128EcbEncrypt(key, plaintext);
        byte[] mac = hmacSha256(secret, ct);
        byte[] out = new byte[MeshcoreConstants.CIPHER_MAC_SIZE + ct.length];
        System.arraycopy(mac, 0, out, 0, MeshcoreConstants.CIPHER_MAC_SIZE);
        System.arraycopy(ct, 0, out, MeshcoreConstants.CIPHER_MAC_SIZE,
                ct.length);
        return out;
    }

    /**
     * {@code Utils::MACThenDecrypt}.
     *
     * <p>Total: returns {@code null} (rather than throwing) if the frame
     * is too short, the ciphertext is not block-aligned, or the MAC does
     * not verify. The MAC comparison is constant-time.</p>
     *
     * @param secret the 32-byte channel secret (or DM shared secret)
     * @param frame  {@code [MAC(2)]‖ciphertext} as carried on air
     * @return the block-padded plaintext, or {@code null} on failure
     */
    public static byte[] macThenDecrypt(byte[] secret, byte[] frame) {
        if (frame.length <= MeshcoreConstants.CIPHER_MAC_SIZE) {
            return null;
        }
        byte[] ct = Arrays.copyOfRange(frame,
                MeshcoreConstants.CIPHER_MAC_SIZE, frame.length);
        // MeshCore ciphertext is always block-aligned (encrypt zero-pads
        // the final block). A non-aligned or empty body cannot be a valid
        // frame — reject it rather than letting AES throw. Keeps this
        // entry point total.
        if (ct.length == 0
                || ct.length % MeshcoreConstants.CIPHER_BLOCK_SIZE != 0) {
            return null;
        }
        byte[] calc = hmacSha256(secret, ct);
        if (!constantTimeEquals(frame, 0, calc, 0,
                MeshcoreConstants.CIPHER_MAC_SIZE)) {
            return null;
        }
        byte[] key = Arrays.copyOfRange(secret, 0,
                MeshcoreConstants.CIPHER_KEY_SIZE);
        return aes128EcbDecrypt(key, ct);
    }

    private static boolean constantTimeEquals(
            byte[] a, int aOff, byte[] b, int bOff, int len) {
        int diff = 0;
        for (int i = 0; i < len; i++) {
            diff |= a[aOff + i] ^ b[bOff + i];
        }
        return diff == 0;
    }

    private static MessageDigest sha256Digest() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }
}
