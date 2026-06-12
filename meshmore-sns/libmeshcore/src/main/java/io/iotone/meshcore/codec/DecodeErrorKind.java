// Copyright (c) 2026 IoTone, Inc.
// SPDX-License-Identifier: MIT
package io.iotone.meshcore.codec;

/**
 * Why a frame failed to decode.
 *
 * <p>Decoding is <strong>total</strong>: every input yields a
 * {@link io.iotone.meshcore.frames.MeshcoreInbound}, never an exception.
 * The full taxonomy of decode outcomes is:</p>
 *
 * <ul>
 *   <li>a typed frame (e.g.
 *       {@link io.iotone.meshcore.frames.SelfInfoFrame}) — recognised
 *       and parsed;</li>
 *   <li>{@link io.iotone.meshcore.frames.UnsupportedFrame} — a
 *       well-formed frame whose opcode this codec does not model; raw
 *       bytes preserved. <strong>Not a failure.</strong></li>
 *   <li>{@link io.iotone.meshcore.frames.DecodeFailure} — the only
 *       failure path, with a kind from this enum.</li>
 * </ul>
 *
 * <p>Hence the only failure kinds are structural: {@link #EMPTY} and
 * {@link #TRUNCATED}. An unknown opcode is <em>not</em> an error (it
 * becomes an {@code UnsupportedFrame}), so there is deliberately no
 * {@code UNSUPPORTED_OPCODE} kind.</p>
 */
public enum DecodeErrorKind {

    /** Frame was empty (no opcode byte). */
    EMPTY,

    /** Frame ended before a required field could be read. */
    TRUNCATED
}
