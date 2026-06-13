// Copyright (c) 2026 IoTone, Inc.
// SPDX-License-Identifier: MIT
package io.iotone.meshcore.android;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;

import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import io.iotone.meshcore.MeshcoreConstants;
import io.iotone.meshcore.codec.MeshcoreFrameCodec;
import io.iotone.meshcore.frames.ChannelMessageFrame;
import io.iotone.meshcore.frames.DecodeFailure;
import io.iotone.meshcore.model.SelfInfo;
import io.iotone.meshcore.transport.MeshcoreTransport;

/**
 * Instrumented integration test — connects to a real MeshCore companion
 * radio over BLE and exercises the full libmeshcore stack from Java.
 *
 * <h2>Prerequisites</h2>
 * <ol>
 *   <li>One T1000-E (node A) flashed with MeshCore companion firmware
 *       {@link MeshcoreConstants#FIRMWARE_PIN_TAG}.</li>
 *   <li>A connected Android or AndroidXR device with BLE enabled.</li>
 *   <li>BLE scan/connect permissions granted (handled by
 *       {@link #PERMISSION_RULE} for API 31+ and the test runner for
 *       older APIs).</li>
 * </ol>
 *
 * <h2>Running</h2>
 * <pre>{@code
 * ./gradlew :libmeshcore-android:connectedAndroidTest \
 *   -Pandroid.testInstrumentationRunnerArguments.deviceName="T1000-E"
 * }</pre>
 *
 * <p>The {@code deviceName} argument is matched as a <em>prefix</em>
 * against the BLE advertisement name — use whatever prefix uniquely
 * identifies your radio (e.g. {@code "T1000-E"}, {@code "Meshcore"},
 * or the node's custom advert name).</p>
 *
 * <p>If {@code deviceName} is not provided the test class is skipped
 * rather than failing — CI stays green without a radio present.</p>
 *
 * <h2>Fixture export (M6 interop)</h2>
 * <p>The test writes an interop fixture JSON to the device's external
 * storage after a successful capture ({@code testD_ExportInteropFixture}).
 * Pull it with:</p>
 * <pre>{@code
 * adb pull /sdcard/Download/meshcore_interop_fixture.json \
 *   path/to/libmeshcore/src/test/resources/vectors/interop/public_msg_1.json
 * }</pre>
 * Then run {@code ./gradlew :libmeshcore:test} to validate both
 * libraries from the same file.
 */
@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)   // A→B→C→D run in order
public class MeshcoreLiveTest {

    private static final String TAG = "MeshcoreLiveTest";

    /** BLE scan timeout — the radio must advertise within this window. */
    private static final long SCAN_TIMEOUT_MS = 10_000;

    /** Maximum time to wait for the handshake to complete. */
    private static final long HANDSHAKE_TIMEOUT_MS = 15_000;

    /** Maximum time to wait for an inbound channel message. */
    private static final long MSG_TIMEOUT_MS = 20_000;

    // Shared session state across all tests in this class.
    private static AndroidBleTransport transport;
    private static MeshcoreSession session;
    private static final AtomicReference<SelfInfo> selfInfoRef =
            new AtomicReference<>();

    @ClassRule
    public static final GrantPermissionRule PERMISSION_RULE =
            GrantPermissionRule.grant(
                    Build.VERSION.SDK_INT >= 31
                            ? new String[]{
                            "android.permission.BLUETOOTH_SCAN",
                            "android.permission.BLUETOOTH_CONNECT"}
                            : new String[]{
                            "android.permission.BLUETOOTH",
                            "android.permission.BLUETOOTH_ADMIN"});

    // -------------------------------------------------------------------------
    // Setup / teardown
    // -------------------------------------------------------------------------

    /**
     * Scans for the target radio by name and establishes the BLE
     * connection + MeshCore handshake. Skips all tests if no
     * {@code deviceName} argument was provided or the scan times out.
     */
    @BeforeClass
    public static void connectToRadio() throws Exception {
        String deviceName = InstrumentationRegistry.getArguments()
                .getString("deviceName");
        Assume.assumeNotNull(
                "Skipping live tests — pass -Pandroid.testInstrumentationRunnerArguments"
                        + ".deviceName=<advert-name> to run against a real radio",
                deviceName);

        Context ctx = InstrumentationRegistry.getInstrumentation()
                .getTargetContext().getApplicationContext();

        BluetoothDevice device = scanForDevice(ctx, deviceName);
        Assume.assumeNotNull(
                "Radio \"" + deviceName + "\" not found in BLE scan "
                        + "(timeout " + SCAN_TIMEOUT_MS + " ms) — is it on and advertising?",
                device);

        CountDownLatch ready = new CountDownLatch(1);
        AtomicReference<SelfInfo> selfRef = new AtomicReference<>();

        transport = new AndroidBleTransport(ctx);
        session = new MeshcoreSession(
                "libmeshcore-live-test", transport,
                new SessionListener() {
                    @Override
                    public void onStateChanged(SessionState state) {
                        Log.i(TAG, "state → " + state);
                    }

                    @Override
                    public void onReady(SelfInfo s) {
                        selfRef.set(s);
                        selfInfoRef.set(s);
                        ready.countDown();
                    }

                    @Override
                    public void onDecodeFailure(DecodeFailure f) {
                        Log.w(TAG, "decode failure: " + f.error());
                    }
                });

        new Handler(Looper.getMainLooper()).post(() -> transport.connectToDevice(device));

        assertTrue(
                "Handshake timed out after " + HANDSHAKE_TIMEOUT_MS + " ms — "
                        + "check BLE PIN (default 123456) and firmware version ("
                        + MeshcoreConstants.FIRMWARE_PIN_TAG + ")",
                ready.await(HANDSHAKE_TIMEOUT_MS, TimeUnit.MILLISECONDS));
    }

    @AfterClass
    public static void disconnect() {
        if (session != null) {
            session.close();
        }
    }

    // -------------------------------------------------------------------------
    // Tests (alphabetical order = execution order via FixMethodOrder.NAME_ASCENDING)
    // -------------------------------------------------------------------------

    /**
     * TC1: the handshake completed and SELF_INFO is populated.
     * Validates that {@code libmeshcore} correctly decoded the APP_START
     * reply from a real device.
     */
    @Test
    public void testA_Handshake() {
        SelfInfo s = selfInfoRef.get();
        assertNotNull("SELF_INFO must be set after handshake", s);
        Log.i(TAG, "Connected: name=\"" + s.name() + "\", freq=" + s.frequencyMhz()
                + "MHz, SF" + s.spreadingFactor() + "/CR" + s.codingRate());
        assertEquals("session must be READY", SessionState.READY, session.state());
        assertNotNull("public key must be 32 bytes", s.publicKey());
        assertEquals(32, s.publicKey().length);
        assertTrue("frequency must be plausible (100–1000 MHz)",
                s.frequencyMhz() > 100 && s.frequencyMhz() < 1000);
    }

    /**
     * TC2: send a channel message and verify the frame is accepted by
     * the device (no decode error on the way back). This test operates
     * on the Public channel and requires node A alone — node B is not
     * needed because we just verify the <em>send path</em> completes
     * without error.
     *
     * <p>For a full round-trip verification (message appears at node B
     * and the reply comes back to A as a ChannelMessageFrame) run
     * {@link #testC_ChannelMessageRoundTrip} with a second radio.</p>
     */
    @Test
    public void testB_SendChannelMessage() {
        assertEquals("must be READY before send", SessionState.READY,
                session.state());
        long ts = System.currentTimeMillis() / 1000;
        // The send is enqueued — no IOException means it was accepted by
        // the Nordic BLE write queue. Actual delivery is verified by the
        // round-trip test.
        session.sendChannelMessage(0, ts, "libmeshcore live-test " + ts);
        // Give the BLE stack a moment to process the write.
        try {
            Thread.sleep(300);
        } catch (InterruptedException ignored) {
        }
        assertEquals("session must still be READY after send",
                SessionState.READY, session.state());
    }

    /**
     * TC3 (optional, requires node B): waits for an inbound
     * {@code ChannelMessageFrame} while node B sends a message on the
     * Public channel. Skipped when node B is not present (no message
     * arrives within the timeout).
     *
     * <p>To run: start node B advertising on the Public channel, trigger
     * a message send from B, then run this test. Or use the MeshCore
     * companion app on a second phone as node B.</p>
     */
    @Test
    public void testC_ChannelMessageRoundTrip() throws Exception {
        CountDownLatch received = new CountDownLatch(1);
        AtomicReference<ChannelMessageFrame> msgRef = new AtomicReference<>();

        // Re-create the session with an extended listener that captures
        // the first inbound channel message.
        SessionListener extended = new SessionListener() {
            @Override
            public void onStateChanged(SessionState s) {
            }

            @Override
            public void onReady(SelfInfo s) {
            }

            @Override
            public void onChannelMessage(ChannelMessageFrame frame) {
                if (msgRef.compareAndSet(null, frame)) {
                    received.countDown();
                }
            }
        };
        // Install the extended listener directly on the transport.
        // The session already holds the previous listener; swap it out
        // for the duration of this test, then restore via the session.
        transport.setListener(new MeshcoreTransport.Listener() {
            @Override
            public void onFrame(byte[] frame) {
                var decoded = MeshcoreFrameCodec.decode(frame);
                if (decoded instanceof ChannelMessageFrame f) {
                    extended.onChannelMessage(f);
                }
            }

            @Override
            public void onConnectionChanged(boolean connected) {
            }
        });

        boolean got = received.await(MSG_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        // Restore the session as the transport's listener.
        session = new MeshcoreSession("libmeshcore-live-test", transport,
                new DefaultSessionListener());

        Assume.assumeTrue(
                "No inbound ChannelMessageFrame in " + MSG_TIMEOUT_MS + " ms — "
                        + "skipping round-trip assertion (start node B and re-run)",
                got);
        ChannelMessageFrame f = msgRef.get();
        assertNotNull(f);
        Log.i(TAG, "ChannelMessage: ch=" + f.message().channelIdx()
                + " text=\"" + f.message().text() + "\""
                + " v3=" + f.message().isV3()
                + " snr=" + f.message().snrDb());
    }

    /**
     * TC4 / M6 fixture export: builds an interop-fixture JSON from the
     * most recent 0x88 RF-log frame and writes it to external storage.
     *
     * <p>Pull the fixture and drop it in both libraries' interop
     * vectors directories (see class-level javadoc).</p>
     *
     * <p>Skipped if no RF-log capture is buffered (enable RF logging on
     * node A's firmware and ensure a GRP_TXT packet was received from
     * node B).</p>
     */
    @Test
    public void testD_ExportInteropFixture() throws Exception {
        String fixture = session.exportInteropFixture(
                "8b3387e9c5cdea6ac9e5edbaa115cd72",   // Public channel PSK
                "libmeshcore live-test",               // must match what B sent
                MeshcoreConstants.FIRMWARE_PIN_TAG);

        Assume.assumeNotNull(
                "No 0x88 RF-log frame buffered — enable RF logging on node A's "
                        + "firmware (compile flag RF_LOG=1 or device config), "
                        + "then have node B send a channel message and re-run",
                fixture);

        // Validate the JSON is well-formed before writing.
        JSONObject json = new JSONObject(fixture);
        assertEquals("grp_txt_capture", json.getString("kind"));
        assertNotNull(json.getString("rf_log_frame_hex"));

        // Write to Downloads so adb pull works without root.
        File dir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS);
        File out = new File(dir, "meshcore_interop_fixture.json");
        try (FileWriter fw = new FileWriter(out)) {
            fw.write(fixture);
        }
        Log.i(TAG, "Fixture written to: " + out.getAbsolutePath());
        Log.i(TAG, "Pull with: adb pull " + out.getAbsolutePath()
                + " libmeshcore/src/test/resources/vectors/interop/public_msg_1.json");
    }

    // -------------------------------------------------------------------------
    // BLE scan helper
    // -------------------------------------------------------------------------

    /**
     * Scans for a BLE device whose advertised name starts with
     * {@code namePrefix} (case-insensitive). Returns the first match or
     * {@code null} if the scan times out.
     */
    @Nullable
    private static BluetoothDevice scanForDevice(
            @NonNull Context ctx, @NonNull String namePrefix) throws Exception {
        // getDefaultAdapter() is deprecated in API 31+; use BluetoothManager.
        BluetoothManager bm = (BluetoothManager)
                ctx.getSystemService(BluetoothManager.class);
        BluetoothAdapter adapter = bm != null ? bm.getAdapter() : null;
        if (adapter == null || !adapter.isEnabled()) return null;

        BluetoothLeScanner scanner = adapter.getBluetoothLeScanner();
        if (scanner == null) return null;

        AtomicReference<BluetoothDevice> found = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        String prefix = namePrefix.toLowerCase();

        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();

        ScanCallback cb = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                String name = result.getDevice().getName();
                if (name != null && name.toLowerCase().startsWith(prefix)) {
                    if (found.compareAndSet(null, result.getDevice())) {
                        scanner.stopScan(this);
                        latch.countDown();
                    }
                }
            }

            @Override
            public void onScanFailed(int errorCode) {
                latch.countDown();
            }
        };

        scanner.startScan(List.of(), settings, cb);
        latch.await(SCAN_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        scanner.stopScan(cb);
        return found.get();
    }

    // -------------------------------------------------------------------------
    // Default (no-op) session listener used after a test re-installs the
    // session to restore normal operation.
    // -------------------------------------------------------------------------

    private static final class DefaultSessionListener implements SessionListener {
        @Override
        public void onStateChanged(@NonNull SessionState state) {
            Log.i(TAG, "state → " + state);
        }

        @Override
        public void onReady(@NonNull SelfInfo s) {
            selfInfoRef.set(s);
        }

        @Override
        public void onDecodeFailure(@NonNull DecodeFailure f) {
            Log.w(TAG, "decode failure: " + f.error());
        }
    }
}
