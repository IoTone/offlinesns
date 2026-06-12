// Copyright (c) 2026 IoTone, Inc.
// SPDX-License-Identifier: MIT
package io.iotone.meshcore.codec;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Little-endian frame builder for app&#8594;device command frames.
 *
 * <p>Mirrors the firmware's expectations: multi-byte integers are
 * little-endian, strings are raw UTF-8 with no length prefix or NUL
 * terminator (the companion protocol delimits trailing strings by frame
 * length), and fixed C-buffer fields are zero-padded / truncated to
 * their declared width.</p>
 */
public final class FrameBuilder {

    private final ByteArrayOutputStream out = new ByteArrayOutputStream();

    /** Creates an empty builder. */
    public FrameBuilder() {
        // Nothing to initialise beyond the buffer.
    }

    /**
     * Appends one unsigned byte.
     *
     * @param v value; only the low 8 bits are written
     * @return this builder, for chaining
     */
    public FrameBuilder u8(int v) {
        out.write(v & 0xFF);
        return this;
    }

    /**
     * Appends an unsigned 32-bit little-endian integer.
     *
     * @param v value; only the low 32 bits are written
     * @return this builder, for chaining
     */
    public FrameBuilder u32(long v) {
        out.write((int) (v & 0xFF));
        out.write((int) ((v >>> 8) & 0xFF));
        out.write((int) ((v >>> 16) & 0xFF));
        out.write((int) ((v >>> 24) & 0xFF));
        return this;
    }

    /**
     * Appends a signed 32-bit little-endian integer (scaled lat/lon).
     *
     * @param v signed value
     * @return this builder, for chaining
     */
    public FrameBuilder i32(int v) {
        return u32(v & 0xFFFFFFFFL);
    }

    /**
     * Appends raw bytes verbatim.
     *
     * @param v bytes to append
     * @return this builder, for chaining
     */
    public FrameBuilder raw(byte[] v) {
        out.write(v, 0, v.length);
        return this;
    }

    /**
     * Appends the UTF-8 bytes of {@code s} (no length prefix, no NUL
     * terminator — the companion protocol delimits trailing strings by
     * frame length).
     *
     * @param s string to encode
     * @return this builder, for chaining
     */
    public FrameBuilder utf8String(String s) {
        return raw(s.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Appends {@code n} zero bytes (reserved fields).
     *
     * @param n number of zero bytes
     * @return this builder, for chaining
     */
    public FrameBuilder zeros(int n) {
        for (int i = 0; i < n; i++) {
            out.write(0);
        }
        return this;
    }

    /**
     * Appends exactly {@code size} bytes: {@code src} truncated if
     * longer, or zero-padded on the right if shorter (fixed C-buffer
     * fields like the 32-byte channel name and 16-byte secret in
     * SET_CHANNEL).
     *
     * @param src  source bytes
     * @param size fixed field width
     * @return this builder, for chaining
     */
    public FrameBuilder fixed(byte[] src, int size) {
        int n = Math.min(src.length, size);
        out.write(src, 0, n);
        return zeros(size - n);
    }

    /**
     * Returns the accumulated frame bytes.
     *
     * @return a fresh array holding the built frame
     */
    public byte[] build() {
        return out.toByteArray();
    }
}
