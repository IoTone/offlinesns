// Copyright (c) 2026 IoTone, Inc.
// SPDX-License-Identifier: MIT
package io.iotone.meshcore;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.fail;

import io.iotone.meshcore.codec.MeshcoreFrameCodec;
import io.iotone.meshcore.frames.BatteryStorageFrame;
import io.iotone.meshcore.frames.ChannelMessageFrame;
import io.iotone.meshcore.frames.ContactMessageFrame;
import io.iotone.meshcore.frames.ContactsStartFrame;
import io.iotone.meshcore.frames.CurrentTimeFrame;
import io.iotone.meshcore.frames.EndOfContactsFrame;
import io.iotone.meshcore.frames.ErrorFrame;
import io.iotone.meshcore.frames.MeshcoreInbound;
import io.iotone.meshcore.frames.MsgSentFrame;
import io.iotone.meshcore.frames.NoMoreMessagesFrame;
import io.iotone.meshcore.frames.OkFrame;
import io.iotone.meshcore.frames.UnsupportedFrame;
import io.iotone.meshcore.model.ChannelMessage;
import io.iotone.meshcore.model.ContactMessage;
import io.iotone.meshcore.model.RadioParams;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

/**
 * Cross-implementation conformance: replays the SAME JSON golden
 * vectors that validate the Dart reference codec (m1–m4), so the two
 * libraries are provably byte-identical on every covered frame.
 */
class GoldenVectorsTest {

    private static final String[] FILES = {
        "m1_frames.json",
        "m2_channel_frames.json",
        "m3_contact_advert_frames.json",
        "m4_config_frames.json",
    };

    @TestFactory
    List<DynamicTest> goldenVectors() {
        List<DynamicTest> tests = new ArrayList<>();
        for (String file : FILES) {
            JSONObject doc = TestUtil.loadVectors(file);
            JSONArray encode = doc.optJSONArray("encode");
            if (encode != null) {
                for (int i = 0; i < encode.length(); i++) {
                    JSONObject c = encode.getJSONObject(i);
                    tests.add(DynamicTest.dynamicTest(
                            file + " encode: " + c.getString("name"),
                            () -> runEncode(c)));
                }
            }
            JSONArray decode = doc.optJSONArray("decode");
            if (decode != null) {
                for (int i = 0; i < decode.length(); i++) {
                    JSONObject c = decode.getJSONObject(i);
                    tests.add(DynamicTest.dynamicTest(
                            file + " decode: " + c.getString("name"),
                            () -> runDecode(c)));
                }
            }
        }
        return tests;
    }

    private static void runEncode(JSONObject c) {
        JSONObject args = c.optJSONObject("args");
        if (args == null) {
            args = new JSONObject();
        }
        byte[] got = encodeByName(c.getString("encoder"), args);
        assertEquals(c.getString("hex"), TestUtil.toHex(got));
    }

    private static byte[] encodeByName(String name, JSONObject a) {
        switch (name) {
            case "appStart":
                return MeshcoreFrameCodec.appStart(a.getString("appName"));
            case "getContacts":
                return a.has("since")
                        ? MeshcoreFrameCodec.getContacts(a.getLong("since"))
                        : MeshcoreFrameCodec.getContacts();
            case "getDeviceTime":
                return MeshcoreFrameCodec.getDeviceTime();
            case "setDeviceTime":
                return MeshcoreFrameCodec.setDeviceTime(
                        a.getLong("unixSeconds"));
            case "syncNextMessage":
                return MeshcoreFrameCodec.syncNextMessage();
            case "sendChannelTextMessage":
                return MeshcoreFrameCodec.sendChannelTextMessage(
                        a.getInt("channelIdx"), a.getLong("timestamp"),
                        a.getString("text"));
            case "getChannel":
                return MeshcoreFrameCodec.getChannel(a.getInt("channelIdx"));
            case "sendTextMessage":
                return MeshcoreFrameCodec.sendTextMessage(
                        TestUtil.hex(a.getString("pubKeyPrefix")),
                        a.getLong("timestamp"), a.getString("text"));
            case "sendSelfAdvert":
                return MeshcoreFrameCodec.sendSelfAdvert(
                        a.getBoolean("flood"));
            case "setAdvertName":
                return MeshcoreFrameCodec.setAdvertName(a.getString("name"));
            case "setRadioParams": {
                OptionalInt repeat = a.has("repeat")
                        ? OptionalInt.of(a.getInt("repeat"))
                        : OptionalInt.empty();
                return MeshcoreFrameCodec.setRadioParams(new RadioParams(
                        a.getDouble("freq"), a.getDouble("bw"),
                        a.getInt("sf"), a.getInt("cr"), repeat));
            }
            case "setRadioTxPower":
                return MeshcoreFrameCodec.setRadioTxPower(a.getInt("dbm"));
            case "setAdvertLatLon":
                return a.has("altMicros")
                        ? MeshcoreFrameCodec.setAdvertLatLon(
                                a.getInt("latMicros"), a.getInt("lonMicros"),
                                a.getInt("altMicros"))
                        : MeshcoreFrameCodec.setAdvertLatLon(
                                a.getInt("latMicros"), a.getInt("lonMicros"));
            case "setOtherParams":
                return MeshcoreFrameCodec.setOtherParams(
                        a.getInt("manualAddContacts"),
                        a.getInt("telemetryModePacked"),
                        a.has("advertLocPolicy")
                                ? a.getInt("advertLocPolicy") : null,
                        a.has("multiAcks") ? a.getInt("multiAcks") : null);
            case "setTuningParams":
                return MeshcoreFrameCodec.setTuningParams(
                        a.getDouble("rxDelayBaseSeconds"),
                        a.getDouble("airtimeFactor"));
            case "deviceQuery":
                return MeshcoreFrameCodec.deviceQuery(
                        a.getInt("appTargetVer"));
            case "getBatteryStorage":
                return MeshcoreFrameCodec.getBatteryStorage();
            default:
                fail("unknown encoder \"" + name + "\" in vectors");
                return null; // unreachable
        }
    }

    private static void runDecode(JSONObject c) {
        MeshcoreInbound got =
                MeshcoreFrameCodec.decode(TestUtil.hex(c.getString("hex")));
        JSONObject exp = c.getJSONObject("expect");
        switch (exp.getString("type")) {
            case "OkFrame": {
                OkFrame f = assertInstanceOf(OkFrame.class, got);
                if (exp.isNull("value")) {
                    assertEquals(false, f.value().isPresent());
                } else {
                    assertEquals(exp.getLong("value"),
                            f.value().getAsLong());
                }
                break;
            }
            case "ErrorFrame": {
                ErrorFrame f = assertInstanceOf(ErrorFrame.class, got);
                if (exp.isNull("code")) {
                    assertEquals(false, f.code().isPresent());
                } else {
                    assertEquals(exp.getInt("code"), f.code().getAsInt());
                }
                break;
            }
            case "ContactsStartFrame":
                assertEquals(exp.getLong("count"),
                        assertInstanceOf(ContactsStartFrame.class, got)
                                .count());
                break;
            case "EndOfContactsFrame":
                assertEquals(exp.getLong("mostRecentLastMod"),
                        assertInstanceOf(EndOfContactsFrame.class, got)
                                .mostRecentLastMod());
                break;
            case "CurrentTimeFrame":
                assertEquals(exp.getLong("unixSeconds"),
                        assertInstanceOf(CurrentTimeFrame.class, got)
                                .unixSeconds());
                break;
            case "NoMoreMessagesFrame":
                assertInstanceOf(NoMoreMessagesFrame.class, got);
                break;
            case "UnsupportedFrame":
                assertEquals(exp.getInt("opcode"),
                        assertInstanceOf(UnsupportedFrame.class, got)
                                .opcode());
                break;
            case "MsgSentFrame": {
                MsgSentFrame f = assertInstanceOf(MsgSentFrame.class, got);
                assertEquals(exp.getBoolean("isFlood"), f.sent().isFlood());
                assertEquals(exp.getLong("expectedAck"),
                        f.sent().expectedAck());
                assertEquals(exp.getLong("estTimeoutMs"),
                        f.sent().estTimeoutMs());
                break;
            }
            case "ChannelMessageFrame": {
                ChannelMessage m =
                        assertInstanceOf(ChannelMessageFrame.class, got)
                                .message();
                assertEquals(exp.getInt("channelIdx"), m.channelIdx());
                assertEquals(exp.getInt("pathLen"), m.pathLen());
                assertEquals(exp.getBoolean("isFlood"), m.isFlood());
                assertEquals(exp.getInt("txtType"), m.txtType());
                assertEquals(exp.getLong("timestamp"), m.timestamp());
                assertEquals(exp.getString("text"), m.text());
                assertEquals(exp.getBoolean("isV3"), m.isV3());
                if (exp.isNull("snrDb")) {
                    assertEquals(null, m.snrDb());
                } else {
                    assertEquals(exp.getDouble("snrDb"), m.snrDb(), 1e-9);
                }
                break;
            }
            case "ContactMessageFrame": {
                ContactMessage m =
                        assertInstanceOf(ContactMessageFrame.class, got)
                                .message();
                assertEquals(exp.getString("pubKeyPrefix"),
                        TestUtil.toHex(m.pubKeyPrefix()));
                assertEquals(exp.getInt("pathLen"), m.pathLen());
                assertEquals(exp.getBoolean("isFlood"), m.isFlood());
                assertEquals(exp.getInt("txtType"), m.txtType());
                assertEquals(exp.getLong("timestamp"), m.timestamp());
                assertEquals(exp.getString("text"), m.text());
                assertEquals(exp.getBoolean("isV3"), m.isV3());
                if (exp.isNull("snrDb")) {
                    assertEquals(null, m.snrDb());
                } else {
                    assertEquals(exp.getDouble("snrDb"), m.snrDb(), 1e-9);
                }
                assertEquals(exp.getBoolean("isSigned"), m.isSigned());
                if (exp.has("signaturePrefix")) {
                    assertEquals(exp.getString("signaturePrefix"),
                            TestUtil.toHex(m.signaturePrefix()));
                }
                break;
            }
            case "BatteryStorageFrame": {
                BatteryStorageFrame f =
                        assertInstanceOf(BatteryStorageFrame.class, got);
                assertEquals(exp.getInt("batteryMillivolts"),
                        f.battery().batteryMillivolts());
                assertEquals(exp.getLong("storageUsedKb"),
                        f.battery().storageUsedKb());
                assertEquals(exp.getLong("storageTotalKb"),
                        f.battery().storageTotalKb());
                break;
            }
            default:
                fail("unhandled expected type " + exp.getString("type"));
        }
    }
}
