// Copyright (c) 2026 IoTone, Inc.
// SPDX-License-Identifier: MIT
package io.iotone.meshcore.frames;

import io.iotone.meshcore.model.Advert;

/**
 * {@code PUSH_CODE_ADVERTISEMENT} (0x80) — a node advert was heard.
 *
 * @param advert the decoded advert (verify with
 *               {@link io.iotone.meshcore.crypto.IdentityCrypto#verifyAdvert(Advert)})
 */
public record AdvertFrame(Advert advert) implements MeshcoreInbound {

    @Override
    public String toString() {
        return "AdvertFrame(" + advert + ")";
    }
}
