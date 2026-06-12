// Copyright (c) 2026 IoTone, Inc.
// SPDX-License-Identifier: MIT
package io.iotone.meshcore.crypto;

import io.iotone.meshcore.MeshcoreConstants;
import io.iotone.meshcore.model.Advert;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.bouncycastle.math.ec.rfc7748.X25519;
import org.bouncycastle.math.ec.rfc8032.Ed25519;

/**
 * Ed25519 identity + the MeshCore {@code ed25519_key_exchange} ECDH.
 *
 * <p>MeshCore ({@code src/Identity.cpp}, pinned commit) uses the
 * <strong>orlp/ed25519</strong> C library: 32-byte public key, 64-byte
 * private key (= clamped {@code SHA512(seed)}), 64-byte signatures.
 * {@code ed25519_key_exchange} derives a 32-byte shared secret by
 * converting the peer's Ed25519 public key to its Montgomery
 * u-coordinate and running X25519 with the clamped private scalar (the
 * raw scalar-mult output — it is <em>not</em> hashed).</p>
 *
 * <p>Implementation split:</p>
 * <ul>
 *   <li>Ed25519 verify/keygen + X25519 scalar-mult: BouncyCastle's
 *       <em>lightweight</em> math API
 *       ({@code org.bouncycastle.math.ec.rfc8032.Ed25519} /
 *       {@code rfc7748.X25519}, RFC 8032 / RFC 7748) — called directly,
 *       with <strong>no JCE provider registration</strong>, so it works
 *       unmodified on Android (whose bundled, stripped BC provider
 *       conflicts with registering the full one) and on any JDK.</li>
 *   <li>The Ed25519&#8594;Montgomery-u birational map
 *       {@code u = (1+y)/(1-y) mod p} is implemented here
 *       ({@link BigInteger} field math) — the one piece the libraries
 *       don't expose.</li>
 * </ul>
 *
 * <p>The orlp clamp ({@code e[0]&=248; e[31]&=63; e[31]|=64}) is
 * bit-identical to and idempotent under the RFC 7748 clamp (BC's
 * {@code X25519.scalarMult} re-applies it), so a 64-byte orlp private
 * key's first 32 bytes are fed directly as the X25519 scalar.</p>
 *
 * <p>Anchored by RFC 8032 &#167;7.1 (Ed25519 verify), RFC 7748
 * &#167;5.2 (X25519), and offline libsodium KATs for the conversion +
 * full key exchange (the {@code m3b_x25519_kat.json} golden vectors,
 * shared verbatim with the Dart reference implementation).</p>
 */
public final class IdentityCrypto {

    private IdentityCrypto() {
        // Static crypto only.
    }

    /** p = 2^255 − 19 (the Curve25519 field prime). */
    private static final BigInteger P =
            BigInteger.ONE.shiftLeft(255).subtract(BigInteger.valueOf(19));

    /**
     * Verifies an Ed25519 {@code signature} (64 bytes) over
     * {@code message} for the 32-byte {@code publicKey}.
     *
     * @param publicKey 32-byte Ed25519 public key
     * @param message   the signed bytes
     * @param signature 64-byte signature
     * @return {@code true} iff the signature verifies
     */
    public static boolean verifySignature(
            byte[] publicKey, byte[] message, byte[] signature) {
        if (publicKey.length != MeshcoreConstants.PUB_KEY_SIZE
                || signature.length != MeshcoreConstants.SIGNATURE_SIZE) {
            return false;
        }
        return Ed25519.verify(signature, 0, publicKey, 0, message, 0,
                message.length);
    }

    /**
     * Verifies an advert's Ed25519 signature over
     * {@code pub_key ‖ timestamp ‖ app_data}
     * ({@link Advert#signedMessage()}).
     *
     * @param advert the decoded advert
     * @return {@code true} iff the advert's signature verifies
     */
    public static boolean verifyAdvert(Advert advert) {
        return verifySignature(advert.publicKey(), advert.signedMessage(),
                advert.signature());
    }

    /**
     * Maps an Ed25519 public key to its Montgomery u-coordinate:
     * {@code u = (1 + y) / (1 - y) mod p}, where {@code y} is the
     * compressed point's low 255 bits (sign bit cleared).
     *
     * @param edPublicKey 32-byte Ed25519 public key
     * @return the u-coordinate as 32 bytes little-endian
     * @throws IllegalArgumentException if the key is not 32 bytes or is
     *         the degenerate point with {@code y ≡ 1} (the singular
     *         point of the birational map — not a valid public key)
     */
    public static byte[] edPublicKeyToMontgomeryU(byte[] edPublicKey) {
        if (edPublicKey.length != MeshcoreConstants.PUB_KEY_SIZE) {
            throw new IllegalArgumentException("ed public key must be 32 bytes");
        }
        // Decode y little-endian, clear the x-sign bit (bit 255).
        BigInteger y = BigInteger.ZERO;
        for (int i = MeshcoreConstants.PUB_KEY_SIZE - 1; i >= 0; i--) {
            y = y.shiftLeft(8).or(BigInteger.valueOf(edPublicKey[i] & 0xFF));
        }
        y = y.and(BigInteger.ONE.shiftLeft(255).subtract(BigInteger.ONE));
        y = y.mod(P);

        BigInteger num = BigInteger.ONE.add(y).mod(P);
        BigInteger den = BigInteger.ONE.subtract(y).mod(P); // in [0, p)
        if (den.signum() == 0) {
            // y ≡ 1 ⇒ singular point of the birational map.
            throw new IllegalArgumentException(
                    "degenerate Ed25519 public key (y == 1)");
        }
        BigInteger u = num.multiply(den.modInverse(P)).mod(P);

        byte[] out = new byte[32];
        BigInteger t = u;
        for (int i = 0; i < 32; i++) {
            out[i] = (byte) t.and(BigInteger.valueOf(0xFF)).intValue();
            t = t.shiftRight(8);
        }
        return out;
    }

    /**
     * MeshCore {@code ed25519_key_exchange(secret, theirPub, ourPrv64)}.
     *
     * <p>{@code privateKey64} is the orlp-style 64-byte private key
     * (only the first 32 bytes — the clamped scalar — are used).
     * Returns the 32-byte shared secret (raw X25519 scalar-mult output,
     * unhashed) suitable as the DM key for
     * {@link ChannelCrypto#encryptThenMac(byte[], byte[])}.</p>
     *
     * @param privateKey64   our orlp-style 64-byte expanded private key
     * @param theirPublicKey the peer's 32-byte Ed25519 public key
     * @return the 32-byte shared secret
     * @throws IllegalArgumentException if {@code privateKey64} is not
     *         64 bytes or {@code theirPublicKey} is degenerate
     */
    public static byte[] ed25519KeyExchange(
            byte[] privateKey64, byte[] theirPublicKey) {
        if (privateKey64.length != MeshcoreConstants.PRIV_KEY_SIZE) {
            throw new IllegalArgumentException(
                    "private key must be 64 bytes (orlp expanded)");
        }
        byte[] scalar = new byte[X25519.SCALAR_SIZE];
        System.arraycopy(privateKey64, 0, scalar, 0, X25519.SCALAR_SIZE);
        byte[] u = edPublicKeyToMontgomeryU(theirPublicKey);
        byte[] shared = new byte[X25519.POINT_SIZE];
        // scalarMult applies the RFC 7748 clamp internally — idempotent
        // over the already-clamped orlp scalar.
        X25519.scalarMult(scalar, 0, u, 0, shared, 0);
        return shared;
    }

    /**
     * orlp-style expanded private key from a 32-byte seed:
     * {@code SHA512(seed)} with the standard clamp applied. Matches the
     * 64-byte {@code prv_key} MeshCore stores.
     *
     * @param seed the 32-byte Ed25519 seed
     * @return the 64-byte expanded private key
     * @throws IllegalArgumentException if the seed is not 32 bytes
     */
    public static byte[] expandedPrivateKeyFromSeed(byte[] seed) {
        if (seed.length != 32) {
            throw new IllegalArgumentException("seed must be 32 bytes");
        }
        byte[] h = sha512(seed); // 64 bytes
        h[0] &= (byte) 248;
        h[31] &= 63;
        h[31] |= 64;
        return h;
    }

    /**
     * The 32-byte Ed25519 public key for a 32-byte seed.
     *
     * @param seed the 32-byte Ed25519 seed
     * @return the 32-byte public key
     * @throws IllegalArgumentException if the seed is not 32 bytes
     */
    public static byte[] ed25519PublicKeyFromSeed(byte[] seed) {
        if (seed.length != 32) {
            throw new IllegalArgumentException("seed must be 32 bytes");
        }
        byte[] pk = new byte[Ed25519.PUBLIC_KEY_SIZE];
        Ed25519.generatePublicKey(seed, 0, pk, 0);
        return pk;
    }

    private static byte[] sha512(byte[] data) {
        try {
            return MessageDigest.getInstance("SHA-512").digest(data);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-512 unavailable", e);
        }
    }
}
