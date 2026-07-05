+++
outputs = ["Reveal"]
title = "MeshCore Intro"
+++

{{< slide transition="zoom" transition-speed="fast" >}}

# MeshCore Intro

A deep dive — what it is, how it compares, and the awkward parts.

---

## What is MeshCore?

- A LoRa mesh protocol + firmware stack — sub-GHz, long-range, low-bitrate.
- Companion-app model: the radio is the device, the phone is the UI over BLE.
- Aimed at delay-tolerant, infrastructure-free communication: chat, position, telemetry.
- Open-source firmware, hardware-agnostic across the common LoRa boards.

---

<section data-state="scrollable">

## MeshCore vs Meshtastic — the gist

| | **Meshtastic** | **MeshCore** |
|---|---|---|
| Packet format | Protobuf; field-extensible | Compact binary, fixed fields per opcode |
| Routing | Hop-count flood with implicit deduplication | Path-aware: floods discover paths, unicast follows them |
| Companion link | Protobuf over BLE/Serial/TCP | Length-delimited binary opcodes over BLE/Serial |
| Roles | Generic node + optional router | Explicit roles: chat / repeater / room / sensor |
| Onboarding | Plug in, get on a default channel | Same, but channels are explicit and named (`Public` is the well-known one) |
| Telemetry | Native (battery, env, GPS) on the message bus | Cayenne LPP payload via dedicated request/response |
| Crypto | Per-channel AES-CTR + per-DM ECDH | Per-channel AES-CTR + per-DM ECDH; signed adverts |

Neither is "better" in the abstract — they make different tradeoffs. MeshCore tends to be more compact on the air; Meshtastic tends to be more featureful per node.

</section>

---

## Roles, briefly

- **Chat (1)** — your handheld. Talks to people.
- **Repeater (2)** — relay. Usually mast-mounted, mains-powered, big antenna.
- **Room server (3)** — a server-class node that hosts named "rooms" for many-to-many chat.
- **Sensor (4)** — narrow-purpose telemetry node (BME280, GPS, INA219, etc.).

Adverts carry the role byte, so any client can colour / weight nodes accordingly.

---

## The companion-protocol pattern

Frames are `[opcode][payload]`, one frame per BLE notification.

A short tour of the opcodes:

- `0x01` `APP_START` — handshake.
- `0x02` / `0x03` — send DM / channel message.
- `0x05` `SELF_INFO` — device-side identity + radio tuple.
- `0x07` — broadcast your advert (with or without flood).
- `0x0E` `SET_ADVERT_LATLON` — push your location to the radio.
- `0x14` `BATT_AND_STORAGE` — battery telemetry.
- `0x27` `SEND_TELEMETRY_REQ` — pull telemetry from a peer (or self).
- `0x83` / `0x88` / `0x8B` — async pushes: messages waiting, RF log, telemetry response.

Plus the modern companion v3 frames carry SNR / RSSI inline, which is what makes signal-aware UIs possible.

---

## Hardware ecosystem

- **T1000-E** (Seeed) — the canonical handheld. GPS, e-ink, small battery.
- **Heltec / LilyGO** — common boards for repeaters and room servers.
- **WisBlock / RAK** — sensor and stationary deployments.
- **Anything with an SX1262/SX1276** — community ports exist.

The firmware is a single C++ codebase with per-board variants under `variants/`.

**2025–2026 additions** added in MeshCore v1.15 (Apr 2026): Heltec Mesh Node T096, muzi works R1 Neo, GAT562 Mesh EVB Pro. Solar-integrated outdoor nodes from Seeed, Heltec, and RAK WisMesh are now standard infrastructure options. See the [Devices 2025–2026](/en/tboc/devices/) deck for the full hardware index.

---

# Section break

## The split

What happened in April 2026, in detail.

---

<section data-state="scrollable">

## The split — context

In late April 2026 the MeshCore community fractured. The shorthand "the split" refers to a public falling-out between the core team (Scott et al.) and Andy Kirby, a previously-active contributor.

Read the team's account: [blog.meshcore.io/2026/04/23/the-split](https://blog.meshcore.io/2026/04/23/the-split)

The disagreement is worth understanding for two reasons:

1. It clarifies **who owns what** today — the official `meshcore.io` site, the GitHub org, the trademark, and the Discord are all currently contested or freshly-relaunched.
2. The substance of the dispute — **how transparent should AI-generated code be in firmware?** — is a question every project using Claude / Copilot / etc. will face soon.

</section>

---

<section data-state="scrollable">

## The split — what happened

- The MeshCore project was driven by a small team: **Scott** (founder, firmware lead), **Liam Cottle** (apps), **Recrof** (maps), **FDLamotte** (Python tooling), **Oltaco** (bootloader). **Andy Kirby** was a member.
- Andy began using **Claude Code extensively** to develop components across the MeshCore stack — devices, mobile apps, a web flasher, config tools — but didn't disclose this to the team.
- On **2026-03-29** Andy applied for the **MeshCore trademark** without informing the team, then stopped responding when the team tried to discuss it.
- Andy retained control of the original `meshcore.co.uk` domain and Discord server, and (per the team) copied the team's visual design.
- The team's account describes it as having "an insider team up with a robot and a lawyer," and explicitly commits to "human-written software" going forward.
- They relaunched at `meshcore.io`, with `blog.meshcore.io` and a new Discord. The trademark situation remains unresolved.

The team's stated position is **human-written** as a core value. The split therefore reads as much about **disclosure** as it does about authorship — the team's objection is to a long period of undisclosed AI involvement, not (necessarily) AI assistance itself.

</section>

---

<section data-state="scrollable">

## The split — multiple readings

Reasonable people read this story differently. A few framings to hold in tension:

- **Trust and disclosure first.** The most-cited objection is that AI use was *concealed*. If Andy had been open about it from the start, the trust dynamics — and the trademark question — might have played out very differently.
- **Authorship and accountability.** "Human-written" is a defensible posture for firmware that runs on people's devices: it implies a person *understands* every line and can answer for it. AI-assisted code raises real questions about who debugs the obscure failure mode at 3 AM.
- **The pragmatic counter.** Modern infrastructure is increasingly co-authored with LLMs. Drawing a hard line risks foreclosing a productivity gain that's already mainstream — and many tools the community uses (compilers, linters, language servers) are also "robots" in a sense.
- **Trademark vs. open source.** A trademark grab on an established open-source project, by any party, is a separate concern from how the code was written. Both can be true.

Where you land probably depends on which value you weight first: transparency, authorship, or pragmatism.

</section>

---

# Section break

## OpenCore — the honest story

The firmware is open. The official mobile app is not.

---

<section data-state="scrollable">

## OpenCore — what's open, what isn't

- **Firmware** — open at [github.com/meshcore-dev/MeshCore](https://github.com/meshcore-dev/MeshCore). MIT-licensed. Every opcode in the companion protocol is implementable from this source.
- **Companion protocol spec** — open at `docs/companion_protocol.md` in the same repo. (Incomplete; the source is the source of truth.)
- **Official iOS / Android apps** — **closed source.** Distributed via the app stores by the core team.
- **Official web flasher** — open-source.
- **Map server / room servers** — open-source.

This is a textbook *opencore* split: the protocol and reference hardware are open, the polished consumer surface is not. Reasonable from a sustainability standpoint, frustrating if you want to (a) inspect what the app does on your device, (b) extend it, or (c) build something the official app intentionally doesn't.

</section>

---

## Alternative open clients

Two community efforts try to fill the closed-app gap:

<a class="deck-link" href="https://github.com/zjs81/meshcore-open">**meshcore-open** — `zjs81/meshcore-open`
<small>An open-source third-party Android client. Functional companion app; pragmatic UI; tracks upstream protocol changes.</small></a>

<a class="deck-link" href="https://github.com/IoTone/offlinesns/tree/main/meshmore-sns">**Meshmore SNS** — `IoTone/offlinesns`
<small>Our own Flutter-based open client. Pure-Dart MeshCore codec, NERV-styled UI, AI/M2M hooks. (Next deck.)</small></a>

---

## Why two?

- **meshcore-open** is the conservative answer — match the official app feature-for-feature, in the open. Excellent baseline.
- **Meshmore SNS** is the *opinionated* answer — explore what mesh-native UX could look like if you started from a design brief instead of a port. Different design surface, different theme system, different feature priorities.

Both are healthy for the ecosystem. Neither replaces the other.

---

## Where to read next

- The team's blog: [blog.meshcore.io](https://blog.meshcore.io)
- The split post (verbatim): [blog.meshcore.io/2026/04/23/the-split](https://blog.meshcore.io/2026/04/23/the-split)
- MeshCore firmware: [github.com/meshcore-dev/MeshCore](https://github.com/meshcore-dev/MeshCore)
- meshcore-open: [github.com/zjs81/meshcore-open](https://github.com/zjs81/meshcore-open)
- Meshmore SNS: [github.com/IoTone/offlinesns](https://github.com/IoTone/offlinesns/tree/main/meshmore-sns)

---

# End

<a class="deck-link" href="/en/tboc/">← Back to TBOC</a>
<a class="deck-link" href="/en/tboc/meshmore-sns/">Next: Meshmore SNS →</a>
