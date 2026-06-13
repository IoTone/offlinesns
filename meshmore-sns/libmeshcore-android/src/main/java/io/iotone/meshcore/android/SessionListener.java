// Copyright (c) 2026 IoTone, Inc.
// SPDX-License-Identifier: MIT
package io.iotone.meshcore.android;

import androidx.annotation.NonNull;

import io.iotone.meshcore.frames.AdvertFrame;
import io.iotone.meshcore.frames.ChannelMessageFrame;
import io.iotone.meshcore.frames.ContactFrame;
import io.iotone.meshcore.frames.ContactMessageFrame;
import io.iotone.meshcore.frames.DecodeFailure;
import io.iotone.meshcore.frames.MeshcoreInbound;
import io.iotone.meshcore.frames.SelfInfoFrame;
import io.iotone.meshcore.model.SelfInfo;

/**
 * Typed callbacks from a {@link MeshcoreSession}.
 *
 * <p>All methods are called on the Android main thread (the same thread
 * the Nordic BLE library dispatches to). Implementations that need to
 * update UI can do so directly; implementations that do heavy work
 * should dispatch to a background executor.</p>
 *
 * <p>Default no-op implementations are provided for all optional
 * methods — override only what you need.</p>
 */
public interface SessionListener {

    /**
     * The session state changed. This fires for every transition:
     * {@code DISCONNECTED &#8594; CONNECTING &#8594; HANDSHAKING &#8594; READY}
     * and back.
     *
     * @param state the new session state
     */
    void onStateChanged(@NonNull SessionState state);

    /**
     * The handshake completed — the device replied to {@code APP_START}
     * with {@code SELF_INFO} and the session is now {@link SessionState#READY}.
     * Use this to read the device name, frequency, and identity.
     *
     * @param selfInfo the decoded device self-description
     */
    void onReady(@NonNull SelfInfo selfInfo);

    /**
     * A channel (group) text message arrived. Called for both legacy
     * (0x08) and V3 (0x11) frames.
     *
     * @param frame the decoded frame (check {@code frame.message().isV3()}
     *              for SNR availability)
     */
    default void onChannelMessage(@NonNull ChannelMessageFrame frame) {
    }

    /**
     * A direct (contact) text message arrived. Called for both legacy
     * (0x07) and V3 (0x10) frames.
     *
     * @param frame the decoded frame
     */
    default void onContactMessage(@NonNull ContactMessageFrame frame) {
    }

    /**
     * A node advert was heard ({@code PUSH_CODE_ADVERTISEMENT}, 0x80).
     * Verify with
     * {@link io.iotone.meshcore.crypto.IdentityCrypto#verifyAdvert(io.iotone.meshcore.model.Advert)}
     * before acting on the content.
     *
     * @param frame the decoded frame
     */
    default void onAdvert(@NonNull AdvertFrame frame) {
    }

    /**
     * One contact entry arrived during a contact-sync drain (0x03).
     *
     * @param frame the decoded contact frame
     */
    default void onContact(@NonNull ContactFrame frame) {
    }

    /**
     * A frame arrived that the session did not handle specially (e.g. a
     * battery readout, device info, or an unsupported opcode). Use for
     * logging or custom handling.
     *
     * @param frame any {@link MeshcoreInbound} subtype
     */
    default void onOtherFrame(@NonNull MeshcoreInbound frame) {
    }

    /**
     * A frame arrived but failed to decode (empty or truncated). This
     * is almost always a BLE transport issue rather than a firmware bug.
     *
     * @param failure the decode failure report
     */
    default void onDecodeFailure(@NonNull DecodeFailure failure) {
    }
}
