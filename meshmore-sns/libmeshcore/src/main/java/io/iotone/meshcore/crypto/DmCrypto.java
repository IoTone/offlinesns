// Copyright (c) 2026 IoTone, Inc.
// SPDX-License-Identifier: MIT
package io.iotone.meshcore.crypto;

/**
 * Direct-message payload crypto.
 *
 * <p>MeshCore encrypts DMs with the <strong>same</strong>
 * {@code Utils::encryptThenMAC} / {@code MACThenDecrypt} routine used
 * for channels ({@code src/Mesh.cpp}, pinned commit) — only the key
 * differs: a 32-byte ECDH shared secret from
 * {@link IdentityCrypto#ed25519KeyExchange(byte[], byte[])} instead of
 * a channel secret.</p>
 *
 * <p>Unlike the channel case there is no secret-tail subtlety: the DM
 * secret is a full 32 bytes, so the HMAC (keyed over all 32) is fully
 * determined.</p>
 */
public final class DmCrypto {

    private DmCrypto() {
        // Static crypto only.
    }

    /**
     * Derives the 32-byte DM shared secret with a contact.
     *
     * @param privateKey64   our orlp-style 64-byte private key
     * @param theirPublicKey the contact's 32-byte Ed25519 public key
     * @return the 32-byte shared secret
     */
    public static byte[] deriveSharedSecret(
            byte[] privateKey64, byte[] theirPublicKey) {
        return IdentityCrypto.ed25519KeyExchange(privateKey64, theirPublicKey);
    }

    /**
     * {@code [MAC(2)] ‖ ciphertext} for {@code plaintext} under
     * {@code sharedSecret} (32 bytes).
     *
     * @param sharedSecret the 32-byte DM shared secret
     * @param plaintext    message bytes
     * @return the MAC-prefixed ciphertext frame body
     */
    public static byte[] encrypt(byte[] sharedSecret, byte[] plaintext) {
        return ChannelCrypto.encryptThenMac(sharedSecret, plaintext);
    }

    /**
     * Inverse of {@link #encrypt(byte[], byte[])}.
     *
     * @param sharedSecret the 32-byte DM shared secret
     * @param frame        {@code [MAC(2)]‖ciphertext}
     * @return the (block-padded) plaintext, or {@code null} if the MAC
     *         fails / the frame is too short
     */
    public static byte[] decrypt(byte[] sharedSecret, byte[] frame) {
        return ChannelCrypto.macThenDecrypt(sharedSecret, frame);
    }
}
