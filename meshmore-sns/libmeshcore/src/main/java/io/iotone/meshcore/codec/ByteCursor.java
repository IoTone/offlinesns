// Copyright (c) 2026 IoTone, Inc.
// SPDX-License-Identifier: MIT
package io.iotone.meshcore.codec;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Little-endian, bounds-checked sequential reader over a frame.
 *
 * <p>All MeshCore multi-byte integers are little-endian (per the pinned
 * {@code companion_protocol.md}). CayenneLPP (big-endian) is handled
 * separately by {@link CayenneLpp}.</p>
 *
 * <p>Every read takes a {@code context} string naming the field, so a
 * truncated frame produces a precise {@link FrameTruncatedException}
 * report (which the codec converts into a typed failure value).</p>
 */
public final class ByteCursor {

    private final byte[] bytes;
    private int pos;

    /**
     * Creates a cursor over {@code bytes}, positioned at offset 0.
     *
     * <p>The array is <em>not</em> copied; the caller must not mutate it
     * while the cursor is in use.</p>
     *
     * @param bytes frame bytes to read
     */
    public ByteCursor(byte[] bytes) {
        this.bytes = bytes;
    }

    /**
     * Returns the current read offset.
     *
     * @return offset from the start of the frame
     */
    public int position() {
        return pos;
    }

    /**
     * Returns the number of unread bytes.
     *
     * @return bytes remaining after the current position
     */
    public int remaining() {
        return bytes.length - pos;
    }

    /**
     * Returns whether the cursor has consumed the whole frame.
     *
     * @return {@code true} when no bytes remain
     */
    public boolean atEnd() {
        return pos >= bytes.length;
    }

    private void need(int n, String ctx) {
        if (remaining() < n) {
            throw new FrameTruncatedException(n, remaining(), ctx);
        }
    }

    /**
     * Reads one unsigned byte.
     *
     * @param ctx field context for truncation reporting
     * @return value in {@code [0, 255]}
     * @throws FrameTruncatedException if no byte remains
     */
    public int u8(String ctx) {
        need(1, ctx);
        return bytes[pos++] & 0xFF;
    }

    /**
     * Reads one signed byte (used for the scaled SNR byte in V3 receive
     * frames and the RSSI byte of RF-log pushes).
     *
     * @param ctx field context for truncation reporting
     * @return value in {@code [-128, 127]}
     * @throws FrameTruncatedException if no byte remains
     */
    public int i8(String ctx) {
        need(1, ctx);
        return bytes[pos++];
    }

    /**
     * Reads an unsigned 16-bit little-endian integer.
     *
     * @param ctx field context for truncation reporting
     * @return value in {@code [0, 65535]}
     * @throws FrameTruncatedException if fewer than 2 bytes remain
     */
    public int u16(String ctx) {
        need(2, ctx);
        int v = (bytes[pos] & 0xFF) | ((bytes[pos + 1] & 0xFF) << 8);
        pos += 2;
        return v;
    }

    /**
     * Reads an unsigned 32-bit little-endian integer.
     *
     * <p>Returned as {@code long} so the full unsigned range is
     * representable (timestamps, ACK CRC tags).</p>
     *
     * @param ctx field context for truncation reporting
     * @return value in {@code [0, 4294967295]}
     * @throws FrameTruncatedException if fewer than 4 bytes remain
     */
    public long u32(String ctx) {
        need(4, ctx);
        long v = (bytes[pos] & 0xFFL)
                | ((bytes[pos + 1] & 0xFFL) << 8)
                | ((bytes[pos + 2] & 0xFFL) << 16)
                | ((bytes[pos + 3] & 0xFFL) << 24);
        pos += 4;
        return v;
    }

    /**
     * Reads a signed 32-bit little-endian integer (used for scaled
     * lat/lon, which can be negative).
     *
     * @param ctx field context for truncation reporting
     * @return signed 32-bit value
     * @throws FrameTruncatedException if fewer than 4 bytes remain
     */
    public int i32(String ctx) {
        need(4, ctx);
        int v = (bytes[pos] & 0xFF)
                | ((bytes[pos + 1] & 0xFF) << 8)
                | ((bytes[pos + 2] & 0xFF) << 16)
                | ((bytes[pos + 3] & 0xFF) << 24);
        pos += 4;
        return v;
    }

    /**
     * Reads {@code n} bytes as a fresh array (a copy — safe to retain).
     *
     * @param n   byte count to read
     * @param ctx field context for truncation reporting
     * @return copy of the next {@code n} bytes
     * @throws FrameTruncatedException if fewer than {@code n} bytes remain
     */
    public byte[] bytes(int n, String ctx) {
        need(n, ctx);
        byte[] out = Arrays.copyOfRange(bytes, pos, pos + n);
        pos += n;
        return out;
    }

    /**
     * Reads the remaining bytes as a UTF-8 string.
     *
     * <p>Lenient — invalid sequences become U+FFFD rather than throwing
     * (matches Dart's {@code allowMalformed: true}; Java's
     * {@code new String(bytes, UTF_8)} replaces malformed input by
     * default). Used for trailing name/text fields, which the companion
     * protocol delimits by frame length.</p>
     *
     * @param ctx field context for truncation reporting
     * @return decoded string (possibly empty)
     */
    public String utf8ToEnd(String ctx) {
        byte[] rest = bytes(remaining(), ctx);
        return new String(rest, StandardCharsets.UTF_8);
    }

    /**
     * Reads a fixed-width field decoded as UTF-8, trimmed at the first
     * NUL (C-string semantics used by 32-byte name buffers etc.).
     *
     * @param n   fixed field width in bytes
     * @param ctx field context for truncation reporting
     * @return decoded string up to (not including) the first NUL
     * @throws FrameTruncatedException if fewer than {@code n} bytes remain
     */
    public String fixedCString(int n, String ctx) {
        byte[] raw = bytes(n, ctx);
        int end = 0;
        while (end < raw.length && raw[end] != 0) {
            end++;
        }
        return new String(raw, 0, end, StandardCharsets.UTF_8);
    }
}
