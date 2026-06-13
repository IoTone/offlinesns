// Copyright (c) 2026 IoTone, Inc.
// SPDX-License-Identifier: MIT
/**
 * Android/AndroidXR BLE transport and protocol session for libmeshcore.
 *
 * <p>This package provides the Android-specific layer that bridges
 * the platform BLE stack to the transport-agnostic
 * {@link io.iotone.meshcore.transport.MeshcoreTransport} SPI:</p>
 *
 * <ul>
 *   <li>{@link io.iotone.meshcore.android.AndroidBleTransport} —
 *       implements {@code MeshcoreTransport} over Android BLE
 *       ({@code android.bluetooth.BluetoothGatt}) backed by the Nordic
 *       Android BLE Library for robust connection management.</li>
 *   <li>{@link io.iotone.meshcore.android.MeshcoreSession} — drives the
 *       full protocol lifecycle (handshake, message-drain loop, frame
 *       dispatch) over any {@code MeshcoreTransport}.</li>
 *   <li>{@link io.iotone.meshcore.android.SessionListener} — typed
 *       callbacks (channel messages, adverts, contacts, decode failures)
 *       dispatched on the Android main thread.</li>
 *   <li>{@link io.iotone.meshcore.android.SessionState} — the session
 *       state machine (DISCONNECTED &#8594; CONNECTING &#8594;
 *       HANDSHAKING &#8594; READY).</li>
 * </ul>
 *
 * <p>This library targets Android API 26+ and AndroidXR (API 35). The
 * BLE UUIDs are defined in
 * {@link io.iotone.meshcore.MeshcoreBle}. All frame encoding/decoding
 * and cryptography live in {@code libmeshcore} ({@code io.iotone.meshcore}),
 * which this package depends on but does not duplicate.</p>
 */
package io.iotone.meshcore.android;
