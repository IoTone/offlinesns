// Copyright (c) 2026 IoTone, Inc.
// SPDX-License-Identifier: MIT
package io.iotone.meshcore.diagnostics;

import io.iotone.meshcore.MeshcoreConstants;
import io.iotone.meshcore.crypto.ChannelCrypto;
import io.iotone.meshcore.model.GrpTxtPayload;

import java.util.Arrays;

/**
 * Resolves the channel-secret tail from a <em>real captured</em>
 * GRP_TXT over-the-air packet (extracted from an
 * {@link io.iotone.meshcore.frames.RfLogFrame} / 0x88).
 *
 * <p>Inputs are all known at capture time: the 16-byte channel
 * {@code psk} (e.g.
 * {@link MeshcoreConstants#publicChannelPsk()}), the exact
 * {@code knownPlaintext} the sender transmitted, and the
 * {@link io.iotone.meshcore.model.OtaPacket#grpTxt()} payload of the
 * captured packet.</p>
 *
 * <p>{@code channelHashOk} (tail-independent, {@code SHA256(psk)[0]})
 * confirms the packet is for this PSK; the tail is then resolved purely
 * by the HMAC-validated decryption recovering the known plaintext.
 * NOTE: the {@link ChannelTailHypothesis#ZEROS} tail is authoritative —
 * this oracle is on-device corroboration / regression anchor, not the
 * sole proof.</p>
 */
public final class ChannelTailOracle {

    private ChannelTailOracle() {
        // Static oracle only.
    }

    /**
     * Builds the candidate 32-byte secret for a hypothesis.
     *
     * @param h   tail hypothesis
     * @param psk the 16-byte channel PSK
     * @return the candidate 32-byte secret
     */
    public static byte[] secretFor(ChannelTailHypothesis h, byte[] psk) {
        int n = Math.min(psk.length, MeshcoreConstants.CIPHER_KEY_SIZE);
        byte[] s = new byte[MeshcoreConstants.CHANNEL_SECRET_SIZE];
        System.arraycopy(psk, 0, s, 0, n);
        switch (h) {
            case ZEROS:
                break; // already zero
            case PSK_REPEAT:
                System.arraycopy(psk, 0, s,
                        MeshcoreConstants.CIPHER_KEY_SIZE, n);
                break;
            case SHA256_LOW:
                System.arraycopy(ChannelCrypto.sha256(psk), 0, s,
                        MeshcoreConstants.CIPHER_KEY_SIZE, 16);
                break;
            case SHA256_HIGH:
                System.arraycopy(ChannelCrypto.sha256(psk), 16, s,
                        MeshcoreConstants.CIPHER_KEY_SIZE, 16);
                break;
        }
        return s;
    }

    /**
     * Runs the oracle: tries every {@link ChannelTailHypothesis} against
     * the captured packet and reports which (if any) yields a MAC-valid
     * decryption recovering the known plaintext.
     *
     * <p>The tail is disambiguated solely by the HMAC over the candidate
     * 32-byte secret (2-byte MAC: 1/65536 collision; the
     * plaintext-recovery check makes a false positive negligible).</p>
     *
     * @param psk            the 16-byte channel PSK
     * @param knownPlaintext the exact bytes the sender transmitted
     * @param grpTxt         the captured packet's GRP_TXT payload split
     * @return the oracle outcome
     */
    public static ChannelTailResult resolveChannelTail(
            byte[] psk, byte[] knownPlaintext, GrpTxtPayload grpTxt) {
        // The on-air channel hash is keyed on the 16-byte PSK only, so it
        // does NOT vary by tail hypothesis — it just confirms the packet
        // is for this channel/PSK.
        boolean channelHashOk =
                ChannelCrypto.channelHashFromPsk(psk) == grpTxt.channelHash();

        ChannelTailHypothesis match = null;
        byte[] recovered = null;

        for (ChannelTailHypothesis h : ChannelTailHypothesis.values()) {
            byte[] secret = secretFor(h, psk);
            byte[] dec = ChannelCrypto.macThenDecrypt(
                    secret, grpTxt.macAndCiphertext());
            if (dec == null || dec.length < knownPlaintext.length) {
                continue;
            }
            byte[] prefix = Arrays.copyOfRange(dec, 0, knownPlaintext.length);
            if (Arrays.equals(prefix, knownPlaintext)) {
                match = h;
                recovered = prefix;
                break;
            }
        }

        return new ChannelTailResult(match, channelHashOk, recovered);
    }
}
