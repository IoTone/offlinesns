// Copyright (c) 2026 IoTone, Inc.
// SPDX-License-Identifier: MIT
/**
 * The decoded device&#8594;app frame hierarchy.
 *
 * <p>{@link io.iotone.meshcore.frames.MeshcoreInbound} is sealed over
 * every frame this codec models, plus
 * {@link io.iotone.meshcore.frames.UnsupportedFrame} (well-formed,
 * unmodelled opcode) and
 * {@link io.iotone.meshcore.frames.DecodeFailure} (empty/truncated) —
 * so a {@code switch} over a decode result is exhaustively checked by
 * the compiler.</p>
 */
package io.iotone.meshcore.frames;
