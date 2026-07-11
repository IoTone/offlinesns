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
| **Power** | ≤13 dBm (20 mW) **conducted**, referenced to a ≤3 dBi antenna. LBT (Listen-Before-Talk) required: carrier sense −80 dBm, ≥128 µs. |

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

---

# Region Settings

Regulatory requirements and community defaults by region.

---

<section data-state="scrollable">

## Region regulatory requirements

The regulatory layer — frequency, power, duty cycle, LBT — applies equally to Meshtastic and MeshCore. The firmware must be configured to the correct region; the radio enforces the limits.

| Region | Band (MHz) | Max TX (dBm) | Duty cycle | LBT | Countries |
|---|---|---|---|---|---|
| **US** | 902–928 | 30 | 100% | No | USA, Canada |
| **EU_868** | 869.4–869.65 | 27 | **10%** (rolling 1h) | No | EU, UK, CH, NO |
| **EU_N_868** ¹ | 869.4–869.65 | 27 | **10%** | No | EU — narrow BW variant (develop, Jul 2026) |
| **EU_866** ¹ | 865.6–867.6 | 27 | **2.5%** | No | EU — SRD G1 sub-band (develop, Jul 2026) |
| **EU_433** | 433.0–434.0 | 12 | **10%** | No | EU (secondary) |
| **JP** | 920.5–928.1 ² | **13** | ≤10%/h ² | **Yes** (ARIB) | Japan |
| **ANZ** | 915–928 | 30 | 100% | No | Australia, NZ |
| **ANZ_433** | 433.05–434.79 | 14 | 100% | No | AU/NZ 433 MHz |
| **KR** | 920–923 | — | 100% | No | South Korea |
| **TW** | 920–925 | 27 | 100% | No | Taiwan |
| **RU** | 868.7–869.2 | 20 | 100% | No | Russia |
| **IN** | 865–867 | 30 | 100% | No | India |
| **CN** | 470–510 | 19 | 100% | No | China |
| **MY_919** | 919–924 | 27 | 100% | No | Malaysia |
| **SG_923** | 917–925 | 20 | 100% | No | Singapore |
| **TH** | 920–925 | 16 | 100% | No | Thailand |
| **UA_868** | 868.0–868.6 | 14 | **1%** | No | Ukraine |
| **UA_433** | 433.0–434.7 | 10 | **10%** | No | Ukraine (433) |
| **BR_902** | 902–907.5 | 30 | 100% | No | Brazil |
| **PH_915** | 915–918 | 24 | 100% | No | Philippines |
| **NZ_865** | 864–868 | 36 | 100% | No | New Zealand (865) |
| **LORA_24** | 2400–2483.5 | 10 | 100% | No | Worldwide (2.4 GHz) |

**JP note (verified 2026-07):** License-free 特定小電力 ceiling is **13 dBm (20 mW) conducted**, referenced to a ≤3 dBi antenna (EIRP ceiling ≈16 dBm). Higher-gain antenna → you must cut conducted power. **LBT** is carrier sense at −80 dBm for **≥128 µs** (the often-quoted "5 ms" is the long-burst class boundary, not the floor) — mandatory and firmware-enforced, not app-enforced. Note current Meshtastic firmware (v2.7.26) restricts JP to **920.5–923.5 MHz / 13 dBm**; the older "920.8–927.8 / 16 dBm" figure still on the docs page is stale.

² **Duty cycle:** channels **CH33–38 (922.4–923.4 MHz, incl. the 923.2 default) cap TX at ≤360 s per rolling hour (~10%)**; CH24–32 (incl. 920.8) have no explicit hourly cap in long-sense LBT mode. Band edges 920.5–928.1 MHz; channel centres 920.6–928.0 MHz at 200 kHz spacing (CHn = 915.8 + 0.2·n).

**技適 is the load-bearing point:** legal *parameters* do not make operation legal — the radio **hardware must carry 技適 certification**. Most grey-import LoRa boards (T1000-E, Heltec, …) do not; operating one is an offence under the 電波法 regardless of settings.

**EU_868 note:** 10% duty cycle is calculated on a rolling one-hour basis. Narrower BW and lower SF reduce airtime per packet and make the limit easier to stay within. Meshtastic firmware explicitly blocks SHORT_TURBO and LONG_TURBO (500 kHz BW) for EU_868 — the 869.4–869.65 MHz slot is only 250 kHz wide.

¹ EU_N_868 and EU_866 are in the Meshtastic `develop` branch as of 2026-07-03 — not yet in a stable release.

</section>

---

<section data-state="scrollable">

## MeshCore vs Meshtastic — radio defaults by region

The two ecosystems configure the radio layer differently. Both transmit standard LoRa — they are **not** interoperable on-air, but the regulatory constraints above apply to both.

| Region | **MeshCore** community default | **Meshtastic** default |
|---|---|---|
| **US / ANZ** | 910.525 MHz · SF7 · BW 62.5 kHz · CR **4/5** | Long Fast: SF11 · 250 kHz · CR **4/5** |
| **EU_868** | 869.618 MHz · SF8 · BW 62.5 kHz · CR **4/5** · 22 dBm · 10% duty | Long Fast: SF11 · 250 kHz · CR **4/5** |
| **EU_N_868** ¹ | 869.618 MHz · SF8 · BW 62.5 kHz · CR **4/5** | Narrow Slow: SF8 · 62.5 kHz · CR **4/6** ← ⚠️ |
| **ANZ (AU specific)** | 915.800 MHz · SF10 · BW 250 kHz · CR 4/5 (some nodes) | As US/ANZ above |
| **JP** | No *official* preset upstream; de-facto community tuple **920.8 MHz · SF12 · BW 125 kHz · CR 4/8** (issues #2079/#2218, open) | JP region: **920.5–923.5 MHz · 13 dBm** · Long Fast (firmware v2.7.26) |
| **All regions** | One shared freq per community — all nodes must match | Preset-based; region sets band, preset sets SF/BW |

**The key difference:**

- **Meshtastic** uses named modem presets (Long Fast, Medium Fast, etc.) on top of a region band. The preset determines SF and BW. Nodes must share the same preset to communicate.
- **MeshCore** sets a single frequency + BW + SF per community. There is no preset layer — all nodes on a network must use the same explicit values.

**⚠️ EU_N_868 incompatibility trap:** Meshtastic's new EU_N_868 region uses the same centre frequency and BW as MeshCore's EU community default, but its Narrow Slow preset uses **CR 4/6** while MeshCore uses **CR 4/5**. Even if you force both to the same exact frequency, they remain on-air incompatible. This is a coding-rate mismatch, not a frequency mismatch.

**Licensed operator bypass (Meshtastic only):** Setting `is_licensed = true` on a Meshtastic device bypasses regional TX power limits. No equivalent in MeshCore. Only valid if you hold a licence for the band in your jurisdiction.

**Meshmore SNS ships both JP options:** because a MeshCore client can only reach other MeshCore nodes, our app carries two Japan presets — `jp_arib_t108` (**923.2 · SF10 · CR 4/5**, ARIB CH37, geofence default) and `jp_meshcore` (**920.8 · SF12 · CR 4/8**, matches the MeshCore-JP community). Pick the latter to actually interoperate with MeshCore-JP nodes; both are legal.

¹ EU_N_868 is in the Meshtastic `develop` branch (merged 2026-07-03) — not yet stable release.

</section>

---

<section data-state="scrollable">

## Meshtastic modem presets

Meshtastic separates the *region* (which sets the legal frequency band and power limit) from the *modem preset* (which sets SF, BW, coding rate). Both must match between nodes.

| Preset | BW | SF | CR | Data rate | Link budget | Notes |
|---|---|---|---|---|---|---|
| Short Turbo | 500 kHz | 7 | 4/5 | 21.9 kbps | 140 dB | Not legal in EU (500 kHz BW) |
| Short Fast | 250 kHz | 7 | 4/5 | 10.9 kbps | 143 dB | |
| Short Slow | 250 kHz | 8 | 4/5 | 6.3 kbps | 145.5 dB | |
| Medium Fast | 250 kHz | 9 | 4/5 | 3.5 kbps | 148 dB | |
| Medium Slow | 250 kHz | 10 | 4/5 | 2.0 kbps | 150.5 dB | |
| Long Turbo | 500 kHz | 11 | 4/8 | 1.3 kbps | 150 dB | Not legal in EU |
| **Long Fast** | **250 kHz** | **11** | **4/5** | **1.1 kbps** | **153 dB** | **Default — matches public networks** |
| Long Moderate | 125 kHz | 11 | 4/8 | 0.34 kbps | 156 dB | |
| Long Slow | 125 kHz | 12 | 4/8 | 0.18 kbps | 158.5 dB | Max range |

**EU_868 constraint:** Short Turbo and Long Turbo use 500 kHz BW which is not permitted in the EU 868 MHz band (channel width too wide for the 869.4–869.65 MHz slot). In the EU, use Long Fast or narrower.

**JP constraint:** At the 20 mW (13 dBm) conducted limit, Long Slow or Long Moderate maximise range within the power budget. Long Fast at reduced power is the community default.

**MeshCore equivalent:** BW 62.5 kHz + SF7–9 is roughly equivalent to Medium Fast in range/data rate terms, but with a narrower channel that fits better in congested ISM bands.

</section>

---

## Where to read next

- Meshtastic hardware docs: [meshtastic.org/docs/hardware](https://meshtastic.org/docs/hardware)
- Meshtastic region/LoRa config: [meshtastic.org/docs/configuration/radio/lora](https://meshtastic.org/docs/configuration/radio/lora/)
- MeshCore firmware variants: [github.com/meshcore-dev/MeshCore/tree/main/variants](https://github.com/meshcore-dev/MeshCore/tree/main/variants)
- MeshCore config tool: [config.meshcore.io](https://config.meshcore.io)
- MeshCore blog: [blog.meshcore.io](https://blog.meshcore.io)
- Seeed MeshCore SKUs: [seeedstudio.com](https://seeedstudio.com)

---

# End

<a class="deck-link" href="/en/tboc/">← Back to TBOC</a>
<a class="deck-link" href="/en/tboc/meshcore-intro/">← MeshCore Intro</a>
