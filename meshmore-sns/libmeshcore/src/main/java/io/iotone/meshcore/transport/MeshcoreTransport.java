// Copyright (c) 2026 IoTone, Inc.
// SPDX-License-Identifier: MIT
package io.iotone.meshcore.transport;

import java.io.IOException;

/**
 * Abstract bidirectional byte transport to a MeshCore companion radio.
 *
 * <p>This library is transport-free: the concrete BLE implementation
 * (Android BLE + the Nordic-UART-style service, see
 * {@link io.iotone.meshcore.MeshcoreBle}) or a serial/TCP bridge lives
 * in the consuming application. Tests feed bytes directly.</p>
 *
 * <p>Contract:</p>
 * <ul>
 *   <li>Each frame delivered to {@link Listener#onFrame(byte[])} is
 *       exactly ONE protocol frame (one BLE notification = one frame;
 *       the transport is responsible for not splitting or coalescing
 *       notifications).</li>
 *   <li>{@link #send(byte[])} transmits exactly one protocol frame
 *       (one BLE write).</li>
 *   <li>{@link Listener#onConnectionChanged(boolean)} reflects link
 *       state; session layers react to it for reconnection.</li>
 * </ul>
 *
 * <p>A simple listener interface (rather than {@code Flow}/reactive
 * types) keeps the contract implementable on every Android API level
 * and trivially adaptable to coroutines/RxJava/executors.</p>
 */
public interface MeshcoreTransport {

    /**
     * Receiver of transport events. Implementations should hand frames
     * to {@link io.iotone.meshcore.codec.MeshcoreFrameCodec#decode(byte[])}.
     */
    interface Listener {

        /**
         * One protocol frame arrived (one BLE notification's bytes).
         *
         * @param frame raw frame bytes; ownership passes to the listener
         */
        void onFrame(byte[] frame);

        /**
         * The link state changed.
         *
         * @param connected {@code true} = ready to
         *                  {@link MeshcoreTransport#send(byte[])}
         */
        void onConnectionChanged(boolean connected);
    }

    /**
     * Registers the single event listener (replacing any previous one).
     *
     * @param listener receiver of frames and link-state changes
     */
    void setListener(Listener listener);

    /**
     * Returns whether the transport is currently connected.
     *
     * @return {@code true} when {@link #send(byte[])} may be called
     */
    boolean isConnected();

    /**
     * Sends exactly one protocol frame.
     *
     * @param frame the frame bytes (one BLE write)
     * @throws IOException           on a transport write failure
     * @throws IllegalStateException if not connected
     */
    void send(byte[] frame) throws IOException;

    /**
     * Releases resources and closes the link.
     *
     * @throws IOException on a transport close failure
     */
    void close() throws IOException;
}
