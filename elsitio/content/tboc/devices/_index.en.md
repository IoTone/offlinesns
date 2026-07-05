+++
outputs = ["Reveal"]
title = "Devices 2025–2026"
+++

{{< slide transition="zoom" transition-speed="fast" >}}

# Devices 2025–2026

Hardware for infrastructure buildout — outdoor, solar, and the new generation.

---

## Why hardware matters for infra

Building a mesh that survives real conditions means getting the hardware layer right before the software layer.

- **Outdoor nodes** must tolerate sun, rain, condensation, and temperature swings.
- **Solar nodes** must run indefinitely on a rooftop or pole — no one is going to swap batteries.
- **Repeaters** must have enough TX power and antenna gain to cover a hilltop or building gap.
- **Form factor is protocol-agnostic** — the same enclosures and solar kits serve both MeshCore and Meshtastic.

The 2025–2026 hardware generation arrived with a clear dominant platform: **nRF52840 + SX1262**, paired with increasingly mature outdoor enclosure and solar accessory ecosystems.

---

<section data-state="scrollable">

## Platform baseline: nRF52840 + SX1262

Virtually every serious outdoor or solar device shipping today is built on this pairing.

| | Why it won |
|---|---|
| **nRF52840** (Nordic) | Low-power ARM Cortex-M4 with BLE 5.3, large flash/RAM, good peripheral support. FreeRTOS + Zephyr both well-supported. |
| **SX1262** (Semtech) | Successor to SX1276. Higher TX efficiency (+3 dBm at same current draw), lower receive current, LoRa 2.0. Widely sourced. |

**What this means for firmware:** MeshCore and Meshtastic both target this combo as their primary CI/test platform. Hardware on this baseline gets the fastest firmware updates and the most community support.

**Emerging:** The **LR1121** (Semtech, 2024) adds simultaneous sub-GHz + 2.4 GHz operation in one chip. Hardware is arriving (LilyGO T-Display S3 Pro LR1121, Jan 2026) but firmware support is alpha-only on both MeshCore and Meshtastic as of mid-2026. Worth watching; not yet production-ready for infra.

</section>

---

# Outdoor / IP-rated

Infrastructure nodes that handle the elements.

---

<section data-state="scrollable">

## Heltec MeshTower V2

**The standout for pole-mount infrastructure — IP66, 30 dBm, integrated solar.**

| Spec | Value |
|---|---|
| IP rating | IP66 |
| Chipset | nRF52840 + SX1262 |
| TX power | 30 dBm (1 W) |
| GPS | Yes |
| Solar | 10 W panel + 8.4 Ah LiFePO₄ |
| Display | None (headless infra node) |
| Price | ~$109–129 |
| Protocol support | MeshCore ✓, Meshtastic ✓ |
| Release | 2025 |

**Notes:** Designed as a complete mast-mount unit. Arrives with SMA antenna port — add a fiberglass omni or Yagi. The LiFePO₄ chemistry is better than lithium-ion for outdoor temperature cycles and has a much longer calendar life. Best single-SKU choice for a permanent outdoor repeater.

</section>

---

<section data-state="scrollable">

## RAK WisMesh Repeater Mini & PRO

**RAK's dedicated outdoor mesh repeater line — IP67, solar-ready.**

| | **Repeater Mini** | **Repeater PRO** |
|---|---|---|
| IP rating | IP67 | IP67 |
| Chipset | nRF52840 + SX1262 | nRF52840 + SX1262 |
| TX power | 22 dBm | 30 dBm |
| Solar input | Lid-integrated panel | 10 W external panel |
| GPS | Optional add-on | Yes |
| Price | ~$99 | ~$149 |
| Protocol | MeshCore ✓ (confirmed) | MeshCore ✓ |
| Release | 2025 | 2025 |

**Notes:** RAK's WisMesh line is notable for the modular WisBlock ecosystem underneath — you can swap IO boards (adding environmental sensors, RS-485, etc.) without replacing the core. The PRO's 30 dBm + external panel covers serious hilltop deployments.

</section>

---

<section data-state="scrollable">

## Seeed SenseCAP Solar P1 Pro

**Best bang-for-buck solar node — dedicated MeshCore SKU, $89.90.**

| Spec | Value |
|---|---|
| IP rating | IPX5 |
| Chipset | nRF52840 + SX1262 |
| Battery | 13,400 mAh |
| Solar panel | 5 W |
| GPS | Yes |
| Display | E-ink |
| Price | $89.90 |
| Protocol | MeshCore (dedicated SKU) ✓, Meshtastic ✓ |
| Release | 2025 |

**Notes:** Seeed sells a specific MeshCore-flashed SKU at seeedstudio.com — arrives pre-configured for `Public` channel. The 13.4 Ah battery is generous; expect multi-week runtime in low-traffic conditions. IPX5 means protected against water jets — adequate for most outdoor placements if kept out of direct pooling water. Not as rugged as IP67 but dramatically cheaper than the WisMesh PRO.

</section>

---

<section data-state="scrollable">

## Elecrow ThinkNode M3

**Compact infrastructure form factor — LR1110 chipset, $35.**

| Spec | Value |
|---|---|
| IP rating | IP66 |
| Chipset | nRF52840 + LR1110 |
| Solar input | Yes (external panel connector) |
| GPS | Yes (integrated L-band) |
| Form factor | Card / DIN-rail mount |
| Price | ~$35 |
| Protocol | MeshCore ✓, Meshtastic ✓ |
| Release | 2026 |

**Notes:** LR1110 (not LR1121) — this is the single-band predecessor. Firmware-mature; treats as equivalent to SX1262 for most purposes. The DIN-rail form factor makes it interesting for enclosure builds and rack-style deployments. At $35 it's the lowest-cost IP66 option currently available.

</section>

---

# Solar Kits & Infrastructure Hardware

For when you're bringing your own board.

---

<section data-state="scrollable">

## Heltec Solar Kit enclosure

**IP67 bring-your-board solar kit — $45.20.**

| Spec | Value |
|---|---|
| IP rating | IP67 |
| Solar panel | Included |
| Compatible boards | T114, Heltec WiFi LoRa 32 V4, others |
| Battery | 3,000–5,000 mAh (board-dependent) |
| Antenna | External SMA |
| Price | ~$45.20 |
| Release | 2025 |

**Best for:** Taking a development board you already own and putting it outdoors permanently. The enclosure handles weatherproofing; you supply the LoRa+BLE board. Works well with a Heltec WiFi LoRa 32 V4 running MeshCore room-server or repeater firmware.

---

## RAK WisMesh 1W Booster Kit

**30 dBm upgrade kit for existing WisBlock nodes — $39.**

A drop-in PA + SAW filter add-on board that lifts any nRF52840 WisBlock node from ~22 dBm to 30 dBm. Significant range increase for hilltop or building-gap repeaters already in the field. No firmware changes required — the PA is transparent to the radio stack.

</section>

---

# New Handhelds 2025–2026

Pocket and wearable nodes — plus what's actually useful for infra staging.

---

<section data-state="scrollable">

## LilyGO T-LoRa Pager

**QWERTY pager form factor — pre-flashed with MeshCore, Aug 2025.**

| Spec | Value |
|---|---|
| Chipset | nRF52840 + SX1262 |
| GPS | Yes (GNSS) |
| Display | E-ink QWERTY |
| Extras | NFC, IMU |
| Price | ~$83 |
| Protocol | MeshCore (reseller pre-flash) |
| Release | Aug 2025 |

A third-party reseller on Amazon sells it pre-flashed with MeshCore companion firmware. Interesting as a field terminal — the physical keyboard is useful when you need to type a message without looking at your phone.

</section>

---

<section data-state="scrollable">

## Heltec Mesh Node T096

**New handheld — 28 dBm, dual-band L1+L5 GPS, solar input, Apr 2026.**

| Spec | Value |
|---|---|
| Chipset | nRF52840 + SX1262 |
| TX power | 28 dBm |
| GPS | UC6580 dual-band L1+L5 |
| Solar input | Yes (USB-C solar charger compatible) |
| Display | 0.96" OLED |
| Price | $30–34 |
| Protocol | Added in MeshCore v1.15 (Apr 2026) ✓ |
| Release | Apr 2026 |

**Notable:** Dual-band L1+L5 GPS is unusual at this price — it gives significantly better position accuracy in urban canyons and under tree cover. The 28 dBm TX is higher than most handhelds. Good candidate as a low-cost portable node that can also double as a temporary repeater.

</section>

---

<section data-state="scrollable">

## LilyGO T-Echo Plus

**Updated e-paper handheld — larger battery, mounting points, Dec 2025.**

| Spec | Value |
|---|---|
| Chipset | nRF52840 + SX1262 |
| Battery | 2,400 mAh |
| Display | E-paper |
| Extras | Climbing hook + tripod mount |
| Price | ~$64 |
| Protocol | MeshCore ✓, Meshtastic ✓ |
| Release | Dec 2025 |

The original T-Echo was popular for its e-paper display and long battery life. The Plus adds a larger battery and mounting options — the tripod mount makes it usable as a temporary outdoor node staked to a post or railing.

---

## muzi works R1 Neo

**Machined aluminum handheld — sealed USB-C port, dual GPS, 2025.**

| Spec | Value |
|---|---|
| Housing | Aircraft aluminum + PETG |
| GPS | Dual GPS modules |
| USB-C | Sealed port |
| Price | ~$89 |
| Protocol | Added in MeshCore v1.15 (Apr 2026) ✓ |
| Release | 2025 |

Note: "IP68" in some listings refers to the USB-C port only — the full enclosure is not rated. Premium feel; dual GPS adds redundancy.

</section>

---

<section data-state="scrollable">

## LilyGO T-Watch Ultra

**IP65 wearable — AMOLED, GNSS, Apr 2026.**

| Spec | Value |
|---|---|
| IP rating | IP65 |
| Display | AMOLED round |
| GPS | GNSS |
| Price | ~$78 |
| Protocol | Meshtastic (support pending); MeshCore not yet |
| Release | Apr 2026 |

Interesting as a wearable node concept for field deployments. Firmware support is still catching up — Meshtastic has a branch for it; MeshCore has not yet added a variant. Worth watching for H2 2026.

</section>

---

<section data-state="scrollable">

## LR1121 — Dual-band, watch this space

**Sub-GHz + 2.4 GHz simultaneously. Hardware is here; firmware is catching up.**

The LR1121 (Semtech, 2024) adds a second radio path at 2.4 GHz while retaining the standard sub-GHz LoRa operation. The theoretical use case for mesh: long-range backbone on sub-GHz + short-range high-density on 2.4 GHz, all from one chip.

**Hardware available now:**
- **LilyGO T-Display S3 Pro LR1121** (Jan 2026, ~$65) — the first widely-available LR1121 board

**Firmware status as of Jul 2026:**
- Meshtastic: alpha support, not recommended for production
- MeshCore: no variant yet

**Recommendation:** Don't build infrastructure on LR1121 yet. Check back in 2027 — if firmware matures, it could meaningfully change the repeater economics.

</section>

---

<section data-state="scrollable">

## Infrastructure build recommendations

A practical starting point for outdoor deployment:

| Goal | Recommendation | Why |
|---|---|---|
| **Permanent outdoor repeater** | Heltec MeshTower V2 | IP66, 30 dBm, solar+LiFePO₄ all-in-one |
| **Budget outdoor node** | Seeed SenseCAP Solar P1 Pro | $89.90, MeshCore SKU, 13.4 Ah |
| **High-density urban cluster** | RAK WisMesh Repeater Mini | IP67, modular, WisBlock ecosystem |
| **Hilltop high-power** | RAK WisMesh Repeater PRO | IP67, 30 dBm, 10 W panel |
| **DIY weatherproofed node** | Heltec Solar Kit + WiFi LoRa 32 V4 | IP67 enclosure, flexible firmware |
| **Portable field terminal** | Heltec Mesh Node T096 | $30–34, 28 dBm, L1+L5 GPS |
| **TX power upgrade** | RAK 1W Booster Kit ($39) | Lifts existing WisBlock to 30 dBm |

**Antenna note:** Every outdoor node benefits from a tuned fiberglass omni over the stock rubber duck. A $15–25 antenna upgrade at the repeater site often doubles effective range.

</section>

---

# Japan — Regulatory & Sourcing

技適・周波数・購入先

---

<section data-state="scrollable">

## Japan regulatory — 技適 is mandatory

Japan's 920 MHz band (ARIB STD-T108) has specific rules. An uncertified transmitter is illegal under the 電波法 regardless of power level.

**Three rules to know:**

| Rule | Detail |
|---|---|
| **技適 required** | Every transmitter needs 技術基準適合証明 (technical conformance). No exceptions for hobbyists. |
| **Meshtastic region** | Set to **JP** (not AS923). JP is a Japan-specific ARIB variant — different channel plan and duty cycle. |
| **Max EIRP** | 20 mW unlicensed. LBT (Listen-Before-Talk) required. |

**Verify any device before purchase:** [総務省 技適データベース](https://www.tele.soumu.go.jp/giteki/)

</section>

---

<section data-state="scrollable">

## What's certified for Japan (mid-2026)

| Device | 技適 status | Notes |
|---|---|---|
| **Seeed SenseCAP P1 Pro** | ✅ Confirmed | Direct from seeedstudio.com |
| **Seeed Wio-SX1262 kit** | ✅ 201-250230 | Bare module; Marutsu/DigiKey JP stocks it |
| **LilyGO T-Echo [For Japan Cert]** | ✅ Confirmed | Must buy the "For Japan Certification" SKU specifically |
| **Seeed Wio Tracker 1110** | ✅ Confirmed | LoRaWAN focus but same chipset |
| **SenseCAP T1000-E** | ⏳ In progress | Amazon.co.jp (ASIN B0DJ6KGXKB); cert pending community reports |
| **M5Stack C6L Meshtastic** | ✅ In stock | Switch Science ¥4,290; 868–923 MHz, SX1262 + ESP32-C6 |
| **RAK4631 WisBlock Core** | ⚠️ Unconfirmed | In stock at Marutsu; verify 技適 with RAK before use |
| **Heltec MeshTower V2** | ❌ No JP variant | EU/US bands only — cannot legally operate in Japan |
| **Heltec Mesh Node T096** | ❌ No JP variant | Same — no AS923/JP SKU exists |
| **Heltec WiFi LoRa 32 V4** | ❌ No JP variant | Same |
| **muzi works R1 Neo** | ❌ No JP variant | 915/868 MHz only |
| **LilyGO T-Echo Plus 920MHz** | ⚠️ No cert label | 920 MHz hardware but no Japan cert on this SKU |
| **RAK WisMesh Repeater** | ⚠️ Unconfirmed | AS923 firmware SKU exists; 技適 unconfirmed |

</section>

---

<section data-state="scrollable">

## Where to buy in Japan

**Domestic stock — ships from Japan:**

| Store | What they carry | URL |
|---|---|---|
| **Switch Science** | M5Stack C6L Meshtastic (¥4,290, 技適済) | switch-science.com |
| **Marutsu / DigiKey JP** | Seeed Wio-SX1262 module (¥1,451, 技適 201-250230), RAK4631 core (¥4,523) | marutsu.co.jp |
| **Amazon.co.jp** | LilyGO T-Echo [For Japan Certification] (4 variants), SenseCAP T1000-E (cert pending) | amazon.co.jp |

**Direct import — ships from overseas (2–3 weeks):**

| Store | What they carry | Notes |
|---|---|---|
| **seeedstudio.com** | SenseCAP P1 Pro (¥約13,000), Wio-SX1262 kit, T1000-E | Ships CN/US/DE; choose JP-cert SKU |
| **lilygo.cc / AliExpress** | T-Echo "920-923MHz [For Japan Certification]" | T-Echo Plus and T-LoRa Pager do NOT have this label |
| **store.rakwireless.com** | WisMesh Repeater (AS923 SKU), WisBlock modules | Verify 技適 with RAK before ordering |

**Japan infrastructure recommendation (2026):**

For a Japan-legal outdoor solar node today: **SenseCAP P1 Pro** (技適済, $89.90 direct, or via Amazon.co.jp when T1000-E cert completes). For a handheld: **LilyGO T-Echo [For Japan Certification]** via Amazon.co.jp. Everything Heltec is currently a non-starter for Japan operation.

</section>

---

## Where to buy (global)

| Source | Notes |
|---|---|
| seeedstudio.com | Official Seeed + SenseCAP; MeshCore-specific SKUs |
| heltec.org | Heltec boards + Solar Kit enclosures (non-Japan only) |
| store.rakwireless.com | WisMesh line + WisBlock ecosystem |
| aliexpress.com | LilyGO boards (official LilyGO store) |
| amazon.com / amazon.co.jp | Check firmware and certification SKU carefully |
| muzi.works | R1 Neo direct (non-Japan only) |

Prices in USD as of mid-2026. Supply on some new SKUs is limited — check lead times before planning a deployment.

---

## Where to read next

- Meshtastic hardware docs: [meshtastic.org/docs/hardware](https://meshtastic.org/docs/hardware)
- MeshCore firmware variants: [github.com/meshcore-dev/MeshCore/tree/main/variants](https://github.com/meshcore-dev/MeshCore/tree/main/variants)
- MeshCore blog: [blog.meshcore.io](https://blog.meshcore.io)
- Seeed MeshCore SKUs: [seeedstudio.com](https://seeedstudio.com)

---

# End

<a class="deck-link" href="/en/tboc/">← Back to TBOC</a>
<a class="deck-link" href="/en/tboc/meshcore-intro/">← MeshCore Intro</a>
