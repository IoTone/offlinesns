// Copyright (c) 2026 IoTone, Inc.
// SPDX-License-Identifier: MIT
package io.iotone.meshcore.model;

/**
 * GRP_TXT payload split: {@code [channel_hash][MAC(2)‖ciphertext]}.
 *
 * <p>The array is owned by the record — callers must not mutate it.</p>
 *
 * @param channelHash      first byte of {@code SHA256(psk)} —
 *                         identifies the channel (see
 *                         {@link io.iotone.meshcore.crypto.ChannelCrypto#channelHashFromPsk(byte[])})
 * @param macAndCiphertext exactly the {@code Utils::encryptThenMAC}
 *                         output for the channel
 *                         ({@code [MAC(2)]‖ciphertext}); feed to
 *                         {@link io.iotone.meshcore.crypto.ChannelCrypto#macThenDecrypt(byte[], byte[])}
 */
public record GrpTxtPayload(int channelHash, byte[] macAndCiphertext) {
}
