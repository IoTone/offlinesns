+++
outputs = ["Reveal"]
title = "Meshmore SNS"
+++

{{< slide transition="zoom" transition-speed="fast" >}}

# Meshmore SNS

### *The social network for when the network goes dark.*

A Flutter client for MeshCore — opinionated, themeable, and unapologetically built with AI in the loop.

---

## What it is

- An **open-source** MeshCore companion app, written in Flutter (iOS + Android + Linux + macOS).
- A **pure-Dart MeshCore codec** library (`packages/meshcore/`) — protocol-faithful, no Bluetooth or platform dependencies in the encode/decode layer, so it's usable from CLIs, tests, and other apps.
- A **UI study** — what does mesh-native UX look like if you don't start from a port of the official app?

---

<section data-state="scrollable">

## Design goals

- **Offline-first.** No login, no cloud, no telemetry pipe to anyone we don't know. Everything you see came over the air or off the device's BLE link.
- **Aesthetic of the future.** NERV-styled HUD, terminal-mono typography, signal-aware visualisation. The mesh is a *physics object* on screen, not a chat list.
- **Honest about precision.** When we know a peer's reach, we draw it. When we don't, we degrade visibly (dashed lines, "?" bands) rather than fabricate.
- **Bilingual at the seams.** English and Japanese in lock-step; i18n is in the bones, not a bolt-on.
- **Hooks, not silos.** AI integration, machine-to-machine messaging, theme customisation — three first-class extension points, not afterthoughts.
- **Transparent provenance.** Built collaboratively with Claude. See "Disclosure" below.

</section>

---

<section data-state="scrollable">

## Three things it tries to do well

1. **Show you the mesh, spatially.** Globe view, fabric-survey quilt, street-map view, elevation profile, equal-grid HUD — every screen treats peers as positioned objects with signal characteristics, not rows in a list.
2. **Tell the truth about reach.** Per-peer signal-budget circles (RSSI + distance + LoRa sensitivity floor + repeater type), topology drawn through actual `outPath` repeater chains, not star arcs to self.
3. **Stay out of your way.** Background-keepalive only when you flip it on. Auto-publish location only when you opt in. No analytics, no remote config.

</section>

---

## Screenshots

<small>(Placeholders — drop screenshots into <code>static/images/meshmore/</code> and they'll render here.)</small>

<div style="display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 1em; margin-top: 1em;">
  <div><img src="/images/meshmore/dashboard.png" alt="Dashboard" onerror="this.style.opacity=0.2"/><small>Dashboard</small></div>
  <div><img src="/images/meshmore/globe.png" alt="Globe view" onerror="this.style.opacity=0.2"/><small>Globe view</small></div>
  <div><img src="/images/meshmore/grid.png" alt="Equal grid" onerror="this.style.opacity=0.2"/><small>Equal grid</small></div>
  <div><img src="/images/meshmore/fabric.png" alt="Fabric survey" onerror="this.style.opacity=0.2"/><small>Fabric survey</small></div>
  <div><img src="/images/meshmore/elevation.png" alt="Elevation profile" onerror="this.style.opacity=0.2"/><small>Elevation profile</small></div>
  <div><img src="/images/meshmore/dm.png" alt="DM" onerror="this.style.opacity=0.2"/><small>Direct message</small></div>
</div>

---

<section data-state="scrollable">

## Roadmap

**Shipped (1.0.x series)**

- BLE companion link + reconnect + background keep-alive.
- Channel + DM messaging, persisted chat history.
- Node discovery, advert ingestion, contact sync.
- LoRa region presets + per-locale auto-pick.
- Globe view, equal-grid HUD, street-map view, fabric-survey quilt.
- Elevation profile (R45).
- Auto-publish location (R36).
- Self + peer telemetry, altitude routing (R47).
- Per-peer signal-budget reach circles (R48).
- Topology drawing via `outPath` repeater chains (R49).

**Next (drafts in `meshmore-sns-spec.md`)**

- AI hooks: pluggable "intent" detector on inbound DMs (summarise, translate, classify, *all local LLM*).
- Machine-to-machine messaging: structured-payload channel with schemas and per-schema handlers.
- Theme studio: ship-your-own colours, fonts, sound packs, animation curves.
- Map alternatives (F2) — vector tiles, offline tile bundles.
- Persistent telemetry charts (battery / temperature / barometer trends per peer).

</section>

---

<section data-state="scrollable">

## Three extension hooks, by design

**AI integration** — every inbound DM / channel message can run through a local intent handler before it lands. The handler is a Dart plugin, signed by you, with no network access. Use cases: auto-translate ja↔en, summarise long messages, surface "emergency" classifiers, draft replies.

**Machine-to-machine** — a typed-payload channel that sits alongside the chat channel. Schemas are JSON-Schema; handlers are registered by schema URI. A weather station can publish, a logger can subscribe, and neither needs to know about the other.

**Themes** — Reskin everything. Colour scheme, typography, sound palette, animation curves, even the slide-up sheet ergonomics. Themes are bundles in `themes/` (TOML + assets); we ship NERV-default + a clean "office" alternate.

The point: stop thinking of mesh radios as walkie-talkies. They're a programmable, persistent, low-bandwidth substrate.

</section>

---

# Disclosure

## Built with Claude.

A short, honest section.

---

<section data-state="scrollable">

## Built with Claude — the unredacted version

- The Dart MeshCore codec, the Flutter UI, the tests, and (yes) these slides are written *collaboratively* with Claude Code.
- The human in the loop sets direction, accepts/rejects changes, debugs hardware-in-the-loop behaviour, and owns the merge button. Every commit is reviewed before it lands.
- The protocol implementation is **cross-checked** against the official MeshCore firmware source — every opcode, every field layout. The codec is "AI-assisted, human-verified," not "AI-fabricated."
- Test coverage is the safety net: 200+ tests, including conformance vectors against hex captures from a real device. If the codec drifts, the suite catches it.
- We disclose this **on the about screen** and **here**. Anyone considering using or extending the app can make an informed decision.

This is the position the [MeshCore "split"](/tboc/meshcore-intro/#/9) put a spotlight on — and we're choosing the opposite end of the disclosure axis from what triggered it. You can disagree with the choice; you can't say it's hidden.

</section>

---

## Where to find it

<a class="deck-link" href="https://github.com/IoTone/offlinesns/tree/main/meshmore-sns">**Source** — `IoTone/offlinesns` (tree/main/meshmore-sns)
<small>Flutter app + the pure-Dart `packages/meshcore` library. MIT-licensed.</small></a>

<a class="deck-link" href="https://github.com/IoTone/offlinesns/blob/main/meshmore-sns/meshmore-sns-spec.md">**Spec** — `meshmore-sns-spec.md`
<small>Living design doc — what's shipped, what's drafted, what's deferred.</small></a>

<a class="deck-link" href="https://github.com/IoTone/offlinesns/blob/main/meshmore-sns/meshmore-sns-UX-brief.md">**UX brief** — `meshmore-sns-UX-brief.md`
<small>The visual / interaction brief that drove the NERV aesthetic and the six concept directions.</small></a>

---

# End

<a class="deck-link" href="/tboc/">← Back to TBOC</a>
<a class="deck-link" href="/tboc/intro/">Restart with Intro →</a>
