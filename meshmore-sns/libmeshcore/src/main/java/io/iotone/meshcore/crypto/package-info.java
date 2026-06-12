// Copyright (c) 2026 IoTone, Inc.
// SPDX-License-Identifier: MIT
/**
 * MeshCore cryptography: the channel/DM cipher
 * ({@link io.iotone.meshcore.crypto.ChannelCrypto},
 * {@link io.iotone.meshcore.crypto.DmCrypto}) and Ed25519 identity +
 * ECDH ({@link io.iotone.meshcore.crypto.IdentityCrypto}).
 *
 * <p>Symmetric primitives (AES-128-ECB, HMAC-SHA256, SHA-256/512) use
 * {@code javax.crypto} / {@code java.security} — present on every
 * Android API level and JDK. Ed25519/X25519 use BouncyCastle's
 * lightweight math API directly (no JCE provider registration), which
 * sidesteps Android's bundled-BC provider conflicts. The wire scheme
 * (AES-ECB + truncated HMAC) is mandated by the firmware — this library
 * implements the protocol as deployed, it does not choose it.</p>
 */
package io.iotone.meshcore.crypto;
