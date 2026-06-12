// Copyright (c) 2026 IoTone, Inc.
// SPDX-License-Identifier: MIT
package io.iotone.meshcore.codec;

/**
 * Thrown internally by {@link ByteCursor} on a bounds violation.
 *
 * <p>The public {@link MeshcoreFrameCodec#decode(byte[])} entry point
 * catches this and turns it into a typed decode-failure value — decoding
 * never throws to callers (the "total decode" invariant).</p>
 */
public final class FrameTruncatedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** Number of bytes the failed read required. */
    private final int needed;

    /** Number of bytes that actually remained in the frame. */
    private final int available;

    /** Human-readable field context (e.g. {@code "selfInfo.pubKey"}). */
    private final String context;

    /**
     * Creates a truncation report.
     *
     * @param needed    bytes required by the failed read
     * @param available bytes remaining in the frame
     * @param context   field being read when the frame ran out
     */
    public FrameTruncatedException(int needed, int available, String context) {
        super("FrameTruncated: needed " + needed + " byte(s) for \"" + context
                + "\" but only " + available + " remain");
        this.needed = needed;
        this.available = available;
        this.context = context;
    }

    /**
     * Returns the number of bytes the failed read required.
     *
     * @return required byte count
     */
    public int needed() {
        return needed;
    }

    /**
     * Returns the number of bytes that remained when the read failed.
     *
     * @return remaining byte count
     */
    public int available() {
        return available;
    }

    /**
     * Returns the field context of the failed read.
     *
     * @return human-readable field name (e.g. {@code "contact.pubKey"})
     */
    public String context() {
        return context;
    }
}
