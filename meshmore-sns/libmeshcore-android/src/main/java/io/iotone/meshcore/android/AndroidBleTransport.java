// Copyright (c) 2026 IoTone, Inc.
// SPDX-License-Identifier: MIT
package io.iotone.meshcore.android;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;
import java.util.UUID;

import io.iotone.meshcore.MeshcoreBle;
import io.iotone.meshcore.transport.MeshcoreTransport;
import no.nordicsemi.android.ble.BleManager;
import no.nordicsemi.android.ble.observer.ConnectionObserver;

/**
 * Android BLE transport for the MeshCore companion-radio protocol,
 * backed by the Nordic Android BLE Library ({@code no.nordicsemi.android:ble}).
 *
 * <p>The MeshCore companion link uses a Nordic-UART-style BLE service
 * (UUIDs in {@link MeshcoreBle}): the app <em>writes</em> command frames
 * to the RX characteristic and <em>subscribes</em> to notifications on
 * the TX characteristic — one write and one notification = one complete
 * protocol frame.</p>
 *
 * <p>This class composes a private {@link BleManager} subclass rather
 * than extending it directly. This avoids conflicts with the several
 * {@code final} methods on {@link BleManager} ({@code isConnected()},
 * {@code close()}) that would shadow the identically-named
 * {@link MeshcoreTransport} interface methods.</p>
 *
 * <h3>Usage</h3>
 * <pre>{@code
 * AndroidBleTransport transport = new AndroidBleTransport(context);
 * MeshcoreSession session = new MeshcoreSession("MyApp", transport, listener);
 * transport.connectToDevice(device);   // scanning is the caller's job
 * }</pre>
 *
 * <p>All callbacks are dispatched on the Android main thread.</p>
 */
public final class AndroidBleTransport implements MeshcoreTransport {

    /** Requested MTU — fits any MeshCore frame with room to spare. */
    private static final int PREFERRED_MTU = 256;

    private static final UUID SERVICE_UUID =
            UUID.fromString(MeshcoreBle.SERVICE_UUID);
    private static final UUID RX_UUID =
            UUID.fromString(MeshcoreBle.RX_CHARACTERISTIC_UUID);
    private static final UUID TX_UUID =
            UUID.fromString(MeshcoreBle.TX_CHARACTERISTIC_UUID);

    private volatile Listener frameListener;
    private final Manager manager;

    /**
     * Creates a transport. Pass the Application {@code Context} for
     * long-lived sessions (do not pass an Activity context).
     *
     * @param context application context
     */
    public AndroidBleTransport(@NonNull Context context) {
        manager = new Manager(context);
    }

    // -------------------------------------------------------------------------
    // MeshcoreTransport
    // -------------------------------------------------------------------------

    @Override
    public void setListener(@Nullable Listener listener) {
        this.frameListener = listener;
    }

    /**
     * Returns {@code true} when the BLE connection is established AND the
     * MeshCore GATT profile has been fully initialised (TX notifications
     * enabled, MTU negotiated). Only then may {@link #send} be called.
     *
     * @return {@code true} when ready for protocol traffic
     */
    @Override
    public boolean isConnected() {
        return manager.isConnected() && manager.isReady();
    }

    /**
     * Sends exactly one protocol frame (one BLE {@code WRITE_NO_RESPONSE}
     * to the RX characteristic). Writes are queued by the Nordic library
     * and execute sequentially.
     *
     * @param frame a single complete protocol frame
     * @throws IllegalStateException if the transport is not connected
     * @throws IOException           (declared for the interface contract)
     */
    @Override
    public void send(@NonNull byte[] frame) throws IOException {
        if (!isConnected()) {
            throw new IllegalStateException(
                    "Not connected to a MeshCore device");
        }
        manager.sendFrame(frame);
    }

    /**
     * Disconnects from the radio and releases resources. Safe to call
     * from any thread.
     *
     * @throws IOException never (declared for the interface contract)
     */
    @Override
    public void close() throws IOException {
        manager.shutdown();
    }

    // -------------------------------------------------------------------------
    // Public helpers
    // -------------------------------------------------------------------------

    /**
     * Initiates a BLE connection to {@code device}. Call this after
     * the BLE scan has located the target radio and you have a
     * {@link BluetoothDevice} handle. The session listener receives
     * {@link Listener#onConnectionChanged onConnectionChanged(true)} when
     * the connection is ready to use.
     *
     * @param device the target BLE device
     */
    public void connectToDevice(@NonNull BluetoothDevice device) {
        manager.connect(device)
                .useAutoConnect(false)
                .retry(3, 200)
                .timeout(12_000)
                .enqueue();
    }

    // -------------------------------------------------------------------------
    // Inner BleManager — composition avoids final-method shadowing
    // -------------------------------------------------------------------------

    private final class Manager extends BleManager {

        private BluetoothGattCharacteristic rxCharacteristic;
        private BluetoothGattCharacteristic txCharacteristic;

        Manager(@NonNull Context context) {
            super(context);
            setConnectionObserver(new ConnectionObserver() {
                @Override
                public void onDeviceConnecting(@NonNull BluetoothDevice d) {
                }

                @Override
                public void onDeviceConnected(@NonNull BluetoothDevice d) {
                }

                @Override
                public void onDeviceFailedToConnect(
                        @NonNull BluetoothDevice d, int reason) {
                    notifyConnectionChanged(false);
                }

                /** Fires after {@code initialize()} completes — transport is usable. */
                @Override
                public void onDeviceReady(@NonNull BluetoothDevice d) {
                    notifyConnectionChanged(true);
                }

                @Override
                public void onDeviceDisconnecting(@NonNull BluetoothDevice d) {
                }

                @Override
                public void onDeviceDisconnected(
                        @NonNull BluetoothDevice d, int reason) {
                    notifyConnectionChanged(false);
                }
            });
        }

        void sendFrame(@NonNull byte[] frame) {
            writeCharacteristic(rxCharacteristic, frame,
                    BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE)
                    .enqueue();
        }

        void shutdown() {
            disconnect().enqueue();
        }

        // --- BleManager overrides ---

        @Override
        protected boolean isRequiredServiceSupported(
                @NonNull BluetoothGatt gatt) {
            BluetoothGattService service = gatt.getService(SERVICE_UUID);
            if (service == null) return false;
            rxCharacteristic = service.getCharacteristic(RX_UUID);
            txCharacteristic = service.getCharacteristic(TX_UUID);
            return rxCharacteristic != null && txCharacteristic != null;
        }

        @Override
        protected void initialize() {
            // Negotiate a larger MTU first so frames up to 255 B fit in
            // one write/notification (default is 23 B minus 3 B overhead).
            requestMtu(PREFERRED_MTU).enqueue();

            setNotificationCallback(txCharacteristic)
                    .with((device, data) -> {
                        byte[] raw = data.getValue();
                        if (raw == null || raw.length == 0) return;
                        Listener l = frameListener;
                        if (l != null) l.onFrame(raw.clone());
                    });
            enableNotifications(txCharacteristic).enqueue();
        }

        @Override
        protected void onServicesInvalidated() {
            rxCharacteristic = null;
            txCharacteristic = null;
        }
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private void notifyConnectionChanged(boolean connected) {
        Listener l = frameListener;
        if (l != null) l.onConnectionChanged(connected);
    }
}
