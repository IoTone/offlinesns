// Copyright (c) 2026 IoTone, Inc.
// SPDX-License-Identifier: MIT
/**
 * Protocol data models: contacts, adverts, messages, device/self info,
 * radio parameters, and the over-the-air packet parser
 * ({@link io.iotone.meshcore.model.OtaPacket}).
 *
 * <p>All types are immutable records whose byte arrays are owned by the
 * record — callers must not mutate them. Field layouts are transcribed
 * from the pinned firmware sources (see
 * {@link io.iotone.meshcore.MeshcoreConstants#FIRMWARE_PIN_TAG}).</p>
 */
package io.iotone.meshcore.model;
