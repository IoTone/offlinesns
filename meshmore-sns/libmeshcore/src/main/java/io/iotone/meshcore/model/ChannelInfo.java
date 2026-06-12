// Copyright (c) 2026 IoTone, Inc.
// SPDX-License-Identifier: MIT
package io.iotone.meshcore.model;

/**
 * Decoded {@code RESP_CODE_CHANNEL_INFO} (0x12) — reply to GET_CHANNEL.
 *
 * <pre>
 * [12][ch_idx][name 32B NUL-padded][secret 16B]
 * </pre>
 *
 * <p>Note: the companion link only conveys 16 bytes of the channel
 * secret (the AES-128 PSK). The firmware's full HMAC key is the 32-byte
 * {@code GroupChannel.secret}; see
 * {@link io.iotone.meshcore.MeshcoreConstants#CHANNEL_SECRET_SIZE} and
 * {@link io.iotone.meshcore.crypto.ChannelCrypto#channelSecretFromPsk(byte[])}.</p>
 *
 * @param channelIdx channel slot index
 * @param name       channel name (NUL-trimmed)
 * @param psk        the 16-byte channel pre-shared key (AES-128 key) as
 *                   carried by the companion protocol; owned by the
 *                   record — do not mutate
 */
public record ChannelInfo(int channelIdx, String name, byte[] psk) {

    @Override
    public String toString() {
        return "ChannelInfo(idx: " + channelIdx + ", name: \"" + name + "\")";
    }
}
