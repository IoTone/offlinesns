// Copyright (c) 2026 IoTone, Inc.
// SPDX-License-Identifier: MIT
package io.iotone.meshcore;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.iotone.meshcore.crypto.ChannelCrypto;
import io.iotone.meshcore.crypto.DmCrypto;
import io.iotone.meshcore.crypto.IdentityCrypto;
import io.iotone.meshcore.model.Advert;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.OptionalInt;

import org.bouncycastle.math.ec.rfc7748.X25519;
import org.bouncycastle.math.ec.rfc8032.Ed25519;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

/**
 * Identity-crypto anchors: RFC 8032 Ed25519 verify vectors, the
 * RFC 7748 X25519 KAT, and the libsodium-generated conversion/ECDH
 * golden vectors shared verbatim with the Dart reference
 * implementation.
 */
class IdentityCryptoTest {

    // pub, msg, sig — RFC 8032 §7.1 TEST 1–3.
    private static final String[][] RFC8032 = {
        {
            "d75a980182b10ab7d54bfed3c964073a0ee172f3daa62325af021a68f707511a",
            "",
            "e5564300c360ac729086e2cc806e828a84877f1eb8e5d974d873e06522490155"
                + "5fb8821590a33bacc61e39701cf9b46bd25bf5f0595bbe24655141438e7a100b",
        },
        {
            "3d4017c3e843895a92b70aa74d1b7ebc9c982ccf2ec4968cc0cd55f12af4660c",
            "72",
            "92a009a9f0d4cab8720e820b5f642540a2b27b5416503f8fb3762223ebdb69da"
                + "085ac1e43e15996e458f3613d0f11d8c387b2eaeb4302aeeb00d291612bb0c00",
        },
        {
            "fc51cd8e6218a1a38da47ed00230f0580816ed13ba3303ac5deb911548908025",
            "af82",
            "6291d657deec24024827e69c3abe01a30ce548a284743a445e3680d7db5ac3ac"
                + "18ff9b538d16f290ae67f760984dc6594a7c15e9716ed28dc027beceea1ec40a",
        },
    };

    @TestFactory
    List<DynamicTest> ed25519VerifyKats() {
        List<DynamicTest> tests = new ArrayList<>();
        for (int i = 0; i < RFC8032.length; i++) {
            String[] v = RFC8032[i];
            int n = i + 1;
            tests.add(DynamicTest.dynamicTest(
                    "RFC 8032 vector " + n + " verifies; tamper fails", () -> {
                        byte[] pub = TestUtil.hex(v[0]);
                        byte[] msg = TestUtil.hex(v[1]);
                        byte[] sig = TestUtil.hex(v[2]);
                        assertTrue(IdentityCrypto.verifySignature(
                                pub, msg, sig));
                        byte[] bad = sig.clone();
                        bad[0] ^= (byte) 0xFF;
                        assertFalse(IdentityCrypto.verifySignature(
                                pub, msg, bad));
                    }));
        }
        return tests;
    }

    @Test
    void x25519Rfc7748Kat() {
        // Validates the BouncyCastle dependency against RFC 7748 §5.2.
        byte[] scalar = TestUtil.hex(
                "a546e36bf0527c9d3b16154b82465edd62144c0ac1fc5a18506a2244ba449ac4");
        byte[] u = TestUtil.hex(
                "e6db6867583030db3594c1a424b15f7c726624ec26b3353b10a903a6d0ab1c4c");
        byte[] out = new byte[32];
        X25519.scalarMult(scalar, 0, u, 0, out, 0);
        assertEquals(
                "c3da55379de9c6908e94ea4df28d084f32eccf03491c71f754b4075577a28552",
                TestUtil.toHex(out));
    }

    @TestFactory
    List<DynamicTest> libsodiumConversionKats() {
        JSONObject doc = TestUtil.loadVectors("m3b_x25519_kat.json");
        JSONArray conv = doc.getJSONArray("conversion");
        List<DynamicTest> tests = new ArrayList<>();
        for (int i = 0; i < conv.length(); i++) {
            JSONObject e = conv.getJSONObject(i);
            int n = i + 1;
            tests.add(DynamicTest.dynamicTest("conversion KAT " + n, () -> {
                byte[] seed = TestUtil.hex(e.getString("seed"));
                assertEquals(e.getString("ed_pub"), TestUtil.toHex(
                        IdentityCrypto.ed25519PublicKeyFromSeed(seed)));
                assertEquals(e.getString("prv64"), TestUtil.toHex(
                        IdentityCrypto.expandedPrivateKeyFromSeed(seed)));
                assertEquals(e.getString("mont_u"), TestUtil.toHex(
                        IdentityCrypto.edPublicKeyToMontgomeryU(
                                TestUtil.hex(e.getString("ed_pub")))));
            }));
        }
        return tests;
    }

    @Test
    void libsodiumEcdhKat() {
        JSONObject e = TestUtil.loadVectors("m3b_x25519_kat.json")
                .getJSONObject("ecdh");
        byte[] alicePrv =
                TestUtil.hex(e.getString("alice_prv64"));
        byte[] bobPrv = TestUtil.hex(e.getString("bob_prv64"));
        byte[] alicePub = TestUtil.hex(e.getString("alice_ed_pub"));
        byte[] bobPub = TestUtil.hex(e.getString("bob_ed_pub"));
        byte[] ab = IdentityCrypto.ed25519KeyExchange(alicePrv, bobPub);
        byte[] ba = IdentityCrypto.ed25519KeyExchange(bobPrv, alicePub);
        assertEquals(e.getString("shared"), TestUtil.toHex(ab));
        assertEquals(e.getString("shared"), TestUtil.toHex(ba)); // symmetry
    }

    @Test
    void dmCryptoRoundTripsBothDirections() {
        byte[] aSeed = new byte[32];
        Arrays.fill(aSeed, (byte) 0x11);
        byte[] bSeed = new byte[32];
        Arrays.fill(bSeed, (byte) 0x22);
        byte[] aPrv = IdentityCrypto.expandedPrivateKeyFromSeed(aSeed);
        byte[] bPrv = IdentityCrypto.expandedPrivateKeyFromSeed(bSeed);
        byte[] aPub = IdentityCrypto.ed25519PublicKeyFromSeed(aSeed);
        byte[] bPub = IdentityCrypto.ed25519PublicKeyFromSeed(bSeed);

        byte[] sa = DmCrypto.deriveSharedSecret(aPrv, bPub);
        byte[] sb = DmCrypto.deriveSharedSecret(bPrv, aPub);
        assertArrayEquals(sa, sb);
        assertEquals(32, sa.length);

        byte[] pt = "direct hello, Meshmore".getBytes(StandardCharsets.UTF_8);
        byte[] frame = DmCrypto.encrypt(sa, pt);
        // Delegates to the channel routine.
        assertArrayEquals(ChannelCrypto.encryptThenMac(sb, pt), frame);

        byte[] dec = DmCrypto.decrypt(sb, frame);
        assertNotNull(dec);
        assertArrayEquals(pt, Arrays.copyOfRange(dec, 0, pt.length));

        frame[0] ^= (byte) 0xFF;
        assertNull(DmCrypto.decrypt(sb, frame));
    }

    @Test
    void verifyAdvertEndToEnd() {
        // Build a real Ed25519-signed advert and verify; mutate → fail.
        byte[] seed = new byte[32];
        for (int i = 0; i < 32; i++) {
            seed[i] = (byte) (i + 3);
        }
        byte[] pub = IdentityCrypto.ed25519PublicKeyFromSeed(seed);

        byte[] nameBytes = "node-Z".getBytes(StandardCharsets.UTF_8);
        byte[] appData = new byte[1 + nameBytes.length];
        appData[0] = (byte) (MeshcoreConstants.ADV_TYPE_CHAT
                | MeshcoreConstants.ADV_NAME_MASK);
        System.arraycopy(nameBytes, 0, appData, 1, nameBytes.length);

        byte[] signed = new byte[32 + 4 + appData.length];
        System.arraycopy(pub, 0, signed, 0, 32);
        signed[32] = 0x04; // ts 0x01020304 LE
        signed[33] = 0x03;
        signed[34] = 0x02;
        signed[35] = 0x01;
        System.arraycopy(appData, 0, signed, 36, appData.length);

        byte[] sig = new byte[Ed25519.SIGNATURE_SIZE];
        Ed25519.sign(seed, 0, signed, 0, signed.length, sig, 0);

        Advert good = new Advert(pub, 0x01020304L, sig, appData, signed,
                appData[0] & 0xFF, null, null, OptionalInt.empty(),
                OptionalInt.empty(), "node-Z");
        assertTrue(IdentityCrypto.verifyAdvert(good));

        byte[] mutated = signed.clone();
        mutated[36] ^= 0x01; // flip a bit of app_data
        Advert tampered = new Advert(pub, 0x01020304L, sig, appData, mutated,
                appData[0] & 0xFF, null, null, OptionalInt.empty(),
                OptionalInt.empty(), "node-Z");
        assertFalse(IdentityCrypto.verifyAdvert(tampered));
    }
}
