// Copyright (c) 2026 IoTone, Inc.
// SPDX-License-Identifier: MIT
package io.iotone.meshcore;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.json.JSONObject;

/** Shared helpers for the test suite (hex codecs + vector loading). */
public final class TestUtil {

    private TestUtil() {
    }

    /**
     * Parses a hex string (whitespace tolerated) into bytes.
     *
     * @param s hex string
     * @return decoded bytes
     */
    public static byte[] hex(String s) {
        String clean = s.replace(" ", "").toLowerCase();
        byte[] out = new byte[clean.length() / 2];
        for (int i = 0; i < out.length; i++) {
            out[i] = (byte) Integer.parseInt(
                    clean.substring(i * 2, i * 2 + 2), 16);
        }
        return out;
    }

    /**
     * Encodes bytes as lowercase hex.
     *
     * @param b bytes
     * @return hex string
     */
    public static String toHex(byte[] b) {
        StringBuilder sb = new StringBuilder(b.length * 2);
        for (byte x : b) {
            sb.append(String.format("%02x", x));
        }
        return sb.toString();
    }

    /**
     * Loads a JSON golden-vector resource from {@code /vectors/<name>}.
     *
     * @param name resource file name (e.g. {@code "m1_frames.json"})
     * @return parsed JSON document
     */
    public static JSONObject loadVectors(String name) {
        try (InputStream in =
                TestUtil.class.getResourceAsStream("/vectors/" + name)) {
            if (in == null) {
                throw new IllegalStateException("missing vectors: " + name);
            }
            return new JSONObject(
                    new String(in.readAllBytes(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new IllegalStateException("cannot read vectors: " + name, e);
        }
    }
}
