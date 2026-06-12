// Copyright (c) 2026 IoTone, Inc.
// SPDX-License-Identifier: MIT
package io.iotone.meshcore.frames;

/**
 * A decoded device&#8594;app frame.
 *
 * <p>Sealed so callers can exhaustively {@code switch} over every
 * outcome of
 * {@link io.iotone.meshcore.codec.MeshcoreFrameCodec#decode(byte[])}:</p>
 *
 * <pre>{@code
 * MeshcoreInbound in = MeshcoreFrameCodec.decode(frame);
 * switch (in) {
 *     case SelfInfoFrame f -> onSelfInfo(f.selfInfo());
 *     case ChannelMessageFrame f -> onChannelMessage(f.message());
 *     case DecodeFailure f -> log(f.error());
 *     case UnsupportedFrame f -> ignore(f.opcode());
 *     // ... every other frame type
 * }
 * }</pre>
 *
 * <p>Decoding is <strong>total</strong>: every input yields exactly one
 * of these — a typed frame, an {@link UnsupportedFrame} (well-formed,
 * unmodelled opcode; <em>not</em> a failure), or a
 * {@link DecodeFailure} (empty/truncated). It never throws.</p>
 */
public sealed interface MeshcoreInbound
        permits OkFrame,
                ErrorFrame,
                ContactsStartFrame,
                ContactFrame,
                EndOfContactsFrame,
                SelfInfoFrame,
                MsgSentFrame,
                AckFrame,
                ChannelMessageFrame,
                ChannelInfoFrame,
                ContactMessageFrame,
                AdvertFrame,
                DeviceInfoFrame,
                BatteryStorageFrame,
                RfLogFrame,
                CurrentTimeFrame,
                NoMoreMessagesFrame,
                CustomVarsFrame,
                MessagesWaitingFrame,
                TelemetryResponseFrame,
                UnsupportedFrame,
                DecodeFailure {
}
