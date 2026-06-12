// Copyright (c) 2026 IoTone, Inc.
// SPDX-License-Identifier: MIT
package io.iotone.meshcore;

/**
 * MeshCore companion-radio protocol constants.
 *
 * <p><strong>SOURCE OF TRUTH</strong> — transcribed from the MeshCore
 * firmware repository, pinned to:</p>
 *
 * <pre>
 *   tag    : companion-v1.15.0
 *   commit : dee3e26ac081a5c668c69b66c16a6544a44ddc5b
 *   file   : docs/companion_protocol.md
 * </pre>
 *
 * <p>These values mirror the Dart reference implementation
 * ({@code meshmore_sns_app/packages/meshcore}) byte-for-byte; both
 * libraries are validated against the same JSON golden vectors. When the
 * pin is bumped, update the header above and re-verify every value.
 * <strong>Do not change a numeric value here without re-reading the
 * pinned doc.</strong></p>
 */
public final class MeshcoreConstants {

    private MeshcoreConstants() {
        // Static constants only.
    }

    /** Pinned firmware reference tag. Bump deliberately, never silently. */
    public static final String FIRMWARE_PIN_TAG = "companion-v1.15.0";

    /** Pinned firmware reference commit (full SHA-1). */
    public static final String FIRMWARE_PIN_COMMIT =
            "dee3e26ac081a5c668c69b66c16a6544a44ddc5b";

    // --- Common protocol field sizes (firmware, pinned commit) ----------

    /** Ed25519 public key size ({@code PUB_KEY_SIZE}). */
    public static final int PUB_KEY_SIZE = 32;

    /** orlp-style expanded private key size ({@code PRV_KEY_SIZE}). */
    public static final int PRIV_KEY_SIZE = 64;

    /** Ed25519 signature size ({@code SIGNATURE_SIZE}). */
    public static final int SIGNATURE_SIZE = 64;

    /** Routing path buffer size ({@code MAX_PATH_SIZE}). */
    public static final int MAX_PATH_SIZE = 64;

    /** Fixed contact-name field width. */
    public static final int CONTACT_NAME_SIZE = 32;

    // --- Channel + cipher sizes (src/MeshCore.h / src/Mesh.h) -----------
    // mesh::GroupChannel { uint8_t hash[PATH_HASH_SIZE];
    //   uint8_t secret[PUB_KEY_SIZE]; } — the channel secret BUFFER is
    // PUB_KEY_SIZE (32) bytes. The AES-128 key is the first
    // CIPHER_KEY_SIZE (16) bytes; the HMAC is keyed over all 32. The
    // companion protocol (SET_CHANNEL / CHANNEL_INFO) only conveys the
    // first CHANNEL_PSK_SIZE (16) bytes.

    /** AES-128 key size ({@code CIPHER_KEY_SIZE}). */
    public static final int CIPHER_KEY_SIZE = 16;

    /** AES block size ({@code CIPHER_BLOCK_SIZE}). */
    public static final int CIPHER_BLOCK_SIZE = 16;

    /** Truncated HMAC tag size ({@code CIPHER_MAC_SIZE}). */
    public static final int CIPHER_MAC_SIZE = 2;

    /** Full channel secret size ({@code GroupChannel.secret} = 32). */
    public static final int CHANNEL_SECRET_SIZE = PUB_KEY_SIZE;

    /** PSK bytes carried by SET_CHANNEL / CHANNEL_INFO. */
    public static final int CHANNEL_PSK_SIZE = 16;

    /** Fixed channel-name field width. */
    public static final int CHANNEL_NAME_SIZE = 32;

    // --- Text types and routing sentinels --------------------------------

    /** {@code txt_type} — plain text. */
    public static final int TXT_TYPE_PLAIN = 0;

    /**
     * {@code TXT_TYPE_SIGNED_PLAIN} — contact message carries a 4-byte
     * signature prefix before the text.
     */
    public static final int TXT_TYPE_SIGNED_PLAIN = 2;

    /** {@code path_len} sentinel: the message arrived via flood routing. */
    public static final int PATH_LEN_FLOOD = 0xFF;

    /**
     * Contacts are referenced in messages by a 6-byte public-key prefix
     * ({@code memcpy(&out_frame[i], from.id.pub_key, 6)}).
     */
    public static final int PUB_KEY_PREFIX_SIZE = 6;

    /** Signature prefix carried by signed contact messages (txt_type 2). */
    public static final int SIGNATURE_PREFIX_SIZE = 4;

    /** {@code CMD_SET_ADVERT_NAME} maximum name length in bytes. */
    public static final int MAX_ADVERT_NAME = 31;

    // --- RESP_CODE_DEVICE_INFO (0x0D) fixed string-field sizes -----------

    /** Device build-date field width. */
    public static final int DEVICE_BUILD_DATE_SIZE = 12;

    /** Device manufacturer field width. */
    public static final int DEVICE_MANUFACTURER_SIZE = 40;

    /** Device firmware-version field width. */
    public static final int DEVICE_FIRMWARE_VERSION_SIZE = 20;

    /** Radio/tuning scale factor: on-wire uint32 = value &#215; 1000. */
    public static final int RADIO_SCALE = 1000;

    // --- Advert (AdvertDataHelpers.h, pinned commit) ----------------------

    /** Advert node type (low nibble): none. */
    public static final int ADV_TYPE_NONE = 0;

    /** Advert node type (low nibble): chat node. */
    public static final int ADV_TYPE_CHAT = 1;

    /** Advert node type (low nibble): repeater. */
    public static final int ADV_TYPE_REPEATER = 2;

    /** Advert node type (low nibble): room server. */
    public static final int ADV_TYPE_ROOM = 3;

    /** Advert node type (low nibble): sensor node. */
    public static final int ADV_TYPE_SENSOR = 4;

    /** Mask extracting the advert type nibble from the flags byte. */
    public static final int ADV_TYPE_MASK = 0x0F;

    /** Advert flags: lat/lon fields present. */
    public static final int ADV_LATLON_MASK = 0x10;

    /** Advert flags: feature-1 u16 present. */
    public static final int ADV_FEAT1_MASK = 0x20;

    /** Advert flags: feature-2 u16 present. */
    public static final int ADV_FEAT2_MASK = 0x40;

    /** Advert flags: trailing UTF-8 name present. */
    public static final int ADV_NAME_MASK = 0x80;

    /**
     * Advert packet envelope size before app_data
     * ({@code pub_key(32) ‖ timestamp(4 LE) ‖ signature(64)} = 100).
     * The Ed25519-signed message is
     * {@code pub_key ‖ timestamp ‖ app_data}.
     */
    public static final int ADVERT_HEADER_SIZE =
            PUB_KEY_SIZE + 4 + SIGNATURE_SIZE;

    // --- Over-the-air packet format (docs/packet_format.md, pinned) ------
    // Header byte 0: bits0-1 route type, bits2-5 payload type,
    // bits6-7 version.

    /** OTA header: route-type mask (bits 0-1). */
    public static final int PKT_ROUTE_TYPE_MASK = 0x03;

    /** OTA header: payload-type mask (bits 2-5, see shift). */
    public static final int PKT_PAYLOAD_TYPE_MASK = 0x3C;

    /** OTA header: payload-type shift. */
    public static final int PKT_PAYLOAD_TYPE_SHIFT = 2;

    /** OTA header: version mask (bits 6-7). */
    public static final int PKT_VERSION_MASK = 0xC0;

    /** Route type: transport flood. */
    public static final int ROUTE_TRANSPORT_FLOOD = 0x00;

    /** Route type: flood. */
    public static final int ROUTE_FLOOD = 0x01;

    /** Route type: direct. */
    public static final int ROUTE_DIRECT = 0x02;

    /** Route type: transport direct. */
    public static final int ROUTE_TRANSPORT_DIRECT = 0x03;

    /**
     * Transport codes (4 bytes) precede the path for the
     * {@code TRANSPORT_*} routes.
     */
    public static final int TRANSPORT_CODES_SIZE = 4;

    /** OTA payload type: request. */
    public static final int PAYLOAD_TYPE_REQ = 0x00;

    /** OTA payload type: response. */
    public static final int PAYLOAD_TYPE_RESPONSE = 0x01;

    /** OTA payload type: direct text message. */
    public static final int PAYLOAD_TYPE_TXT_MSG = 0x02;

    /** OTA payload type: acknowledgement. */
    public static final int PAYLOAD_TYPE_ACK = 0x03;

    /** OTA payload type: advert. */
    public static final int PAYLOAD_TYPE_ADVERT = 0x04;

    /** OTA payload type: group (channel) text. */
    public static final int PAYLOAD_TYPE_GRP_TXT = 0x05;

    /** OTA payload type: group (channel) data. */
    public static final int PAYLOAD_TYPE_GRP_DATA = 0x06;

    /** OTA payload type: anonymous request. */
    public static final int PAYLOAD_TYPE_ANON_REQ = 0x07;

    /** OTA payload type: path. */
    public static final int PAYLOAD_TYPE_PATH = 0x08;

    /** OTA payload type: trace. */
    public static final int PAYLOAD_TYPE_TRACE = 0x09;

    /** Path-length byte: hop-count mask (bits 0-5). */
    public static final int PATH_HOP_MASK = 0x3F;

    /** Path-length byte: hash-size code shift (bits 6-7). */
    public static final int PATH_HASH_SIZE_SHIFT = 6;

    // --- Well-known PUBLIC channel (docs/qr_codes.md, pinned commit) ------

    /** The well-known public channel's name. */
    public static final String PUBLIC_CHANNEL_NAME = "Public";

    /**
     * The well-known public channel's 16-byte PSK
     * ({@code meshcore://channel/add?name=Public&secret=8b3387…}).
     * The shared secret is 16 bytes throughout the ecosystem; the
     * firmware {@code GroupChannel.secret} tail (bytes 16..32) is not
     * carried — see {@link #CHANNEL_SECRET_SIZE}.
     *
     * @return a fresh copy of the 16-byte PSK
     */
    public static byte[] publicChannelPsk() {
        return new byte[] {
            (byte) 0x8b, 0x33, (byte) 0x87, (byte) 0xe9,
            (byte) 0xc5, (byte) 0xcd, (byte) 0xea, 0x6a,
            (byte) 0xc9, (byte) 0xe5, (byte) 0xed, (byte) 0xba,
            (byte) 0xa1, 0x15, (byte) 0xcd, 0x72,
        };
    }

    /**
     * Converts a V3 receive-frame SNR byte to dB:
     * {@code snrDb = signed8(value) / 4.0}.
     *
     * @param rawByte the unsigned byte as read off the wire (0..255)
     * @return signal-to-noise ratio in dB
     */
    public static double snrByteToDb(int rawByte) {
        int signed = rawByte < 128 ? rawByte : rawByte - 256;
        return signed / 4.0;
    }
}
