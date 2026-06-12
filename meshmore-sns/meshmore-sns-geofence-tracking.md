# Meshmore SNS — Node tracking & geofencing (team ops)

**Status:** EARMARK — design only, NOT built. Captured 2026-06-08.
**Sibling docs:** place inference (R54), microclimate/wx, the hyperlocal
grid `_GridViewMode` family. Composes with the map-pins design (see
`lobospeak-design.md` → "Map pins & waypoints").

---

## 1. What & why

Today the hyperlocal grid shows where nodes *are* (radar/globe/street/
fabric/elevation views) and distance is always measured **from self**. For
a team operating together — search & rescue, an event crew, a convoy, a
work site — the useful questions are about **relationships between other
nodes** and **about zones**, not about me:

- How far apart are A and B right now, and is that gap opening or closing?
- Did anyone on the team leave the staging area?
- Tell me when node X gets within 500 m of the rally point.
- Has anyone been parked at the trailhead for >30 min (dwell)?

Two new capabilities cover this:

1. **Inter-node distance tracking** — visualize the live distance/bearing
   between an arbitrary pair (or a hub-and-spokes set) of nodes, and watch
   it change over time (closing / opening / steady).
2. **Geofencing + automated notifications** — define zones (a circle around
   a point/node, later a polygon) and get a cue + OS notification when a
   tracked node **enters / exits / dwells** in a zone.

This is the first explicitly **team-oriented** use case in the app — it
reads a *roster* (favourites / known / a `#tag`) rather than a single peer.

---

## 2. Honest constraints (offline-first, no background collection)

- **No reliable background collection** (see meshbook/wx rolling-24h work):
  positions only update when an **advert / telemetry frame actually
  arrives** while connected. Geofence evaluation is therefore **event-
  driven on received position updates** plus a foreground re-eval tick —
  NOT a continuous GPS tracker. The UI must be honest about *staleness*
  ("last fix 6 min ago") so a fence isn't trusted as real-time.
- **Privacy default:** a node hiding its location reports lat/lon 0/0 and is
  treated as *unlocated* (consistent with the contact-scrub rule) — it never
  triggers a fence and shows as "position unknown", not as "at 0,0".
- **All on-device, display/notify-only.** Nothing about fences or tracks is
  ever echoed back onto the mesh in v1. (Sharing zones with the team is a
  later phase and would ride a closed channel — never Public.)
- **Battery:** the foreground re-eval tick is coarse (e.g. 15–30 s) and the
  heavy work (haversine over the roster) is O(nodes × fences), trivially
  cheap for realistic counts (<350 contacts, a handful of fences).

---

## 3. Data model (sketch)

```
TrackedLink { aKey, bKey }              // an A↔B pair the user is watching
   → derived: distanceM, bearingDeg, trend (closing/opening/steady),
     lastUpdate, both-located?  (history ring for the trend sparkline)

Geofence {
  id, label,
  shape: Circle(centerLat, centerLon, radiusM)   // v1: circle only
         | Anchor(nodeKey, radiusM)               // moves with a node
         | Polygon([latlon…])                     // v2
  watch: Roster                                   // who is evaluated:
         all | favourites | known | tag(#team) | explicit [keys]
  triggerOn: { enter?, exit?, dwell(minutes)? }
  active: bool,
  createdAt,
}

GeofenceState   // per (fence × node): inside?, since, lastNotifiedEdge
GeofenceEvent   { fenceId, nodeKey, edge: enter|exit|dwell, atMs, distanceM }
```

Stores: `geofence_store.dart` + `tracked_link_store.dart` — SharedPreferences
JSON, same `abstract final class` pattern as FavoriteStore / NodeTagsStore.

---

## 4. Visualization (hyperlocal grid)

A new grid view mode **`_GridViewMode.tracking`** (icon: `Icons.timeline`
or `share_location`), plus a lightweight **overlay** other map-like modes can
opt into (radial/street/globe):

- **Distance tethers:** draw a line between each TrackedLink's two nodes,
  labelled with live distance + a trend glyph (↘ closing / ↗ opening / →
  steady). Colour the tether by trend or by distance band.
- **Hub mode:** pick one node as a hub → spokes to every roster member with
  per-spoke distance, sorted nearest-first (the convoy / "keep formation"
  view).
- **Geofence rings:** render each circle/anchor fence as a ring; fill tints
  by occupancy (how many watched nodes are inside); a node crossing the ring
  pulses. Anchor fences move with their node.
- **Trend sparkline:** a TrackedLink detail sheet shows the distance history
  ring as a sparkline (reuse the battery/heat sparkline widget pattern).
- Per-theme skin via `context.skin` + `MmScaffold`/painters (consistent with
  the other grid views; HOLD on per-theme forking until features solidify,
  per the existing grid note).

---

## 5. Notifications / cue path

- New `CueKind.geofence` (audible cue) — and likely split `geofenceEnter` /
  `geofenceExit` so they're distinguishable by ear; synthesized per theme in
  `brand/_audio_pack.py` like the rest (a "zone crossed" earcon).
- **OS notification** via the existing R21 Android foreground-service /
  notification plumbing, so a fence can fire while the app is backgrounded
  *if* a position update happens to arrive (honest: only as often as adverts
  land — surfaced in the notification copy).
- **De-bounce / hysteresis:** require the node to be a margin past the radius
  (e.g. radius ± 5 %) before flipping inside/outside, so a node sitting on
  the boundary doesn't spam. `dwell` only fires once per continuous stay.
- Respect `audioMaster` / `visualHapticOnly` / per-theme haptics like every
  other cue; a fence event always has a visual + haptic parity (a node that
  runs silent still gets the banner + buzz).

---

## 6. Reuse (already in the codebase)

- `geo.haversineMeters` (distance), bearing helper, `formatDistance`.
- `DiscoveredNode.lat/lon/hasLocation`, the node store, `telemetryFor`.
- Roster sources: `favorites`, `known`, **node tags** (`tagsFor` / `allTags`)
  — `#team` tag is the natural team roster selector. See [[contact-terminology]].
- The grid `_GridViewMode` switch + the existing map painters/legends.
- Notification + cue infra (CueService, R21 foreground service).
- Place inference / map pins to *name and place* a fence centre (drop a pin →
  "fence around this pin"); the pins design is the placement UX for fences.

---

## 7. Phasing

- **P1 — inter-node distance:** TrackedLink model + store; a `tracking` grid
  mode with tethers (distance + bearing + trend) and a hub/spokes mode. No
  fences yet. Pure-Dart distance/trend logic is unit-testable.
- **P2 — circle/anchor geofences + notifications:** Geofence model + store;
  ring rendering; event-driven + foreground-tick evaluation with hysteresis;
  `CueKind.geofence*` + OS notification; roster = favourites/known/tag.
- **P3 — team polish:** dwell triggers, per-tag fences, a "who's inside /
  who left" roster panel, fence templates ("rally point", "staging area"),
  staleness surfacing.
- **P4 (optional) — share zones over mesh:** broadcast a fence/waypoint to
  the team on a **closed** control channel (never Public) — a natural
  lobospeak app-extension command; converges with the map-pins sharing path.

---

## 8. Open questions

- Anchor-fence semantics when the anchor node's own fix is stale — freeze the
  ring at last-known, or hide it? (lean: freeze + grey it.)
- Trend window length for "closing/opening" (avoid jitter from noisy fixes).
- Do TrackedLinks/fences scope per-channel or globally? (lean: global — a
  team operates across channels; tags carry the team identity.)
- Notification fatigue: a global rate cap / quiet-hours for fence cues.
