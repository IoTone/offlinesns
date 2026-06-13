// Copyright (c) 2026 IoTone, Inc.
// SPDX-License-Identifier: MIT
package io.iotone.meshcore;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.iotone.meshcore.codec.MeshcoreFrameCodec;
import io.iotone.meshcore.diagnostics.ChannelTailOracle;
import io.iotone.meshcore.diagnostics.ChannelTailResult;
import io.iotone.meshcore.frames.RfLogFrame;
import io.iotone.meshcore.model.OtaPacket;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

/**
 * Replays real T1000-E captures dropped into
 * {@code src/test/resources/vectors/interop/} (same schema and fixtures
 * as the Dart package — see its {@code SCHEMA.md} and
 * {@code meshmore-sns/M6-interop-runbook.md}).
 *
 * <p>No fixtures yet &#8594; the suite reports one skipped placeholder
 * (CI stays green). Once captures are committed, each is asserted
 * against the codec + crypto and the channel-secret tail is resolved
 * mechanically — for BOTH libraries, from the same files.</p>
 */
class InteropReplayTest {

    private static List<File> fixtures() {
        File dir = new File("src/test/resources/vectors/interop");
        List<File> out = new ArrayList<>();
        File[] files = dir.listFiles(
                (d, name) -> name.endsWith(".json"));
        if (files != null) {
            for (File f : files) {
                out.add(f);
            }
        }
        return out;
    }

    @Test
    void interopFixturesPresence() {
        // Mirrors the Dart harness's skip: green-but-skipped until real
        // captures land (never hand-write fixtures — real device only).
        Assumptions.assumeFalse(fixtures().isEmpty(),
                "No real captures in vectors/interop/ — see "
                        + "meshmore-sns/M6-interop-runbook.md");
    }

    @TestFactory
    List<DynamicTest> interopReplays() {
        List<DynamicTest> tests = new ArrayList<>();
        for (File f : fixtures()) {
            tests.add(DynamicTest.dynamicTest("interop: " + f.getName(),
                    () -> replay(f)));
        }
        return tests;
    }

    private static void replay(File f) throws Exception {
        JSONObject fx = new JSONObject(Files.readString(f.toPath()));
        assertTrue("grp_txt_capture".equals(fx.getString("kind")),
                "unknown fixture kind");

        RfLogFrame frame = assertInstanceOf(RfLogFrame.class,
                MeshcoreFrameCodec.decode(
                        TestUtil.hex(fx.getString("rf_log_frame_hex"))),
                "capture must be a 0x88 RF-log frame");

        OtaPacket pkt = frame.log().packet();
        assertNotNull(pkt, "OTA packet failed to parse");
        assertTrue(pkt.isGrpTxt(), "expected a GRP_TXT packet");

        ChannelTailResult r = ChannelTailOracle.resolveChannelTail(
                TestUtil.hex(fx.getString("psk_hex")),
                fx.getString("known_plaintext_utf8")
                        .getBytes(StandardCharsets.UTF_8),
                pkt.grpTxt());

        System.out.println("[interop] " + f.getName()
                + " → channel-secret tail = "
                + (r.match() != null ? r.match() : "UNRESOLVED")
                + " (channelHashOk: " + r.channelHashOk() + ")");
        assertTrue(r.resolved(),
                "no tail hypothesis reproduced the real device MAC + "
                        + "channel hash + plaintext for " + f.getName());
    }
}
