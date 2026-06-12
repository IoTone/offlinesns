// Copyright (c) 2026 IoTone, Inc.
// SPDX-License-Identifier: MIT
/**
 * libmeshcore — pure-Java client for the MeshCore companion-radio
 * protocol.
 *
 * <p>A faithful port of the hardware-verified Dart reference
 * implementation ({@code meshmore_sns_app/packages/meshcore}), pinned to
 * MeshCore firmware {@code companion-v1.15.0} and validated against the
 * same JSON golden vectors. Android- (API-level-tolerant crypto, no JCE
 * provider registration) and server-ready (any JDK 17+).</p>
 *
 * <p>This root package holds the protocol constants and opcode tables:
 * {@link io.iotone.meshcore.MeshcoreConstants},
 * {@link io.iotone.meshcore.MeshcoreCommand},
 * {@link io.iotone.meshcore.MeshcoreResponse} and
 * {@link io.iotone.meshcore.MeshcoreBle}. Start at
 * {@link io.iotone.meshcore.codec.MeshcoreFrameCodec} for
 * encoding/decoding frames.</p>
 */
package io.iotone.meshcore;
