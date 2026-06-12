// Copyright (c) 2026 IoTone, Inc.
// SPDX-License-Identifier: MIT
/**
 * Frame encoding/decoding — the heart of the library.
 *
 * <p>{@link io.iotone.meshcore.codec.MeshcoreFrameCodec} encodes
 * app&#8594;device command frames and decodes device&#8594;app frames
 * into the sealed {@link io.iotone.meshcore.frames.MeshcoreInbound}
 * hierarchy. Decoding is <strong>total</strong> — it never throws.
 * {@link io.iotone.meshcore.codec.CayenneLpp} decodes telemetry
 * payloads (full ElectronicCats type table).</p>
 */
package io.iotone.meshcore.codec;
