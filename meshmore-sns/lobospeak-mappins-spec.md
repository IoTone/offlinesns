# lobospeak — Map Pins & Triggers spec

> A pin is a point on the world the operator cares about. lobospeak turns
> pins into **destinations**, **labels**, and **rules** — the operator's
> surface for acting on robots and watching a closed mesh, all offline.

**Status:** SPEC (design, no code yet). Expands the "Map pins & waypoints"
earmark in `lobospeak-design.md`. Parent: `lobospeak-design.md` (wire format,
command set, trust model). Sibling: `meshmore-sns-geofence-tracking.md` (a
pin + radius = a geofence; the trigger model here subsumes it).

This spec is organised around the four capabilities you asked to be sure of:

1. **Pin with GPS coordinates** — §2
2. **Pin with tags** — §3
3. **Pins that are waypoints** — §4
4. **Pins that trigger business logic** — §5 (the part that needed design
   thought; ends with the open decisions I need your steer on, §5.7)

§6 is the unified data model, §7 the on-mesh wire mapping, §8 UI, §9 the
honest offline constraints, §10 phasing, §11 open decisions, §12 glossary.

---

## 1. What a pin is

One model underlies all four capabilities — they're not four features, they're
one `MapPin` with a `kind` and optional attachments (tags, a route slot, a
rule). The minimum a pin has is **a location and an identity**; everything
else is layered on.

```
MapPin {
  id:        u128 / uuid          // stable, local
  lat, lon:  double               // WGS84 degrees (see §2 for wire encoding)
  altM:      double?              // optional elevation
  label:     string               // "rally point", "sample site 3"
  kind:      marker | waypoint | hazard | zone | sample | trigger
  tags:      Set<string>          // §3
  color/icon: derived from kind+tags, overridable
  source:    userDropped | fromNode | fromInferredPlace | fromGoto | fromSample
  route:     RouteRef?            // set when this pin is a waypoint in a route (§4)
  rule:      TriggerRule?         // set when this pin triggers logic (§5)
  createdAt, createdByKey         // self in v1; createdByKey matters once shared
  scope:     global               // pins outlive any one channel (v1)
}
```

A pin's `kind` is mostly a UI/semantics hint; the *capabilities* come from
which attachments are present. A single pin can be a tagged hazard **and**
carry a trigger rule — kinds are the headline, attachments are the behaviour.

---

## 2. Pin with GPS coordinates

The non-negotiable base. A pin is anchored to a real lat/lon and can be
created from any of the ways an operator actually has a coordinate:

| Entry path | How | Notes |
|---|---|---|
| **Tap / long-press the map** | reverse the grid's lat/lon projection at the touch point | the common case; any map-like `_GridViewMode` |
| **From a node** | "pin this node's position" off the node detail sheet | snapshots the node's last fix |
| **Type coordinates** | decimal `47.6062, -122.3321` or DMS | reuse the place-inference **coordinate parser** (R54) |
| **Maidenhead grid** | `CN87` / `CN87uo` | reuse the place-inference **grid-locator parser** |
| **Promote an inferred place** | one tap on a low-confidence R54 marker → fixed pin | bridges text-mined places to real pins |
| **From a `SAMPLE` result** | drop a `sample` pin at the robot's reported position | a sensor map accretes (§5 action) |

**Internal representation:** `double` lat/lon (full WGS84 precision) in the
app. **Wire/GOTO encoding:** `s32` **degE7** (degrees × 10⁷) for both lat and
lon — ±180.0° fits in s32 with ~1.1 cm resolution, and it matches the
`GOTO [lat s32][lon s32]` field in `lobospeak-design.md`. (Flag: confirm the
firmware's GOTO scaling is degE7 and not microdegrees/degE6; the codec must
match the robot's expectation exactly. This is the one cross-implementation
contract in the pin feature.)

**Precision honesty:** a pin shows the precision it was created at (a tapped
pin is "~map precision", a typed coordinate is exact, a promoted inferred
place carries R54's confidence). A robot acting on a pin gets the full degE7;
the *operator* sees the honesty.

---

## 3. Pin with tags

Tags are how pins stop being a flat soup and become queryable, layerable,
and — critically — **scopable for rules** ("fire for every `#hazard` pin",
"route through `#checkpoint` pins in order").

```
MapPin.tags : Set<string>
```

- **Vocabulary:** freeform, lowercase, kebab. The picker **suggests the
  existing node-tag vocabulary** (`allTags`) so pin tags and contact tags
  share a namespace — a `#team` tag means the same thing on a node and on a
  pin. (See contact-terminology; reuse `NodeTagsStore`'s pattern, separate
  store `PinTagsStore` or fold into the pin record.)
- **What tags drive:**
  - **Filter & layer:** show/hide pins by tag; a tag becomes a toggleable map
    layer ("show only `#hazard`").
  - **Colour:** a tag can pin a colour (overrides the kind default), so a
    class of pins reads as one Mondrian-bright field on the grid.
  - **Rule scope (the big one):** a trigger rule can target *a tag*, not just
    one pin — "WHEN any robot enters any `#exclusion` zone, HALT" applies to
    every present and future pin carrying that tag. This is what makes tags
    load-bearing rather than cosmetic.
  - **Bulk ops:** select-by-tag to send a route, share a set, or arm/disarm a
    family of rules at once.
- **Reserved/semantic tags (optional):** a small set the app understands
  specially, e.g. `#waypoint`, `#exclusion`, `#rally`, `#staging`. These are
  just tags with built-in templates/colours; everything else is freeform.

---

## 4. Pins that are waypoints

A **waypoint** is a pin a robot can be sent to. Single waypoint → one `GOTO`;
an ordered set of waypoints → a **Route**.

```
Route {
  id, label,
  legs: [ pinId… ]          // ordered; each leg references a waypoint pin
  speed: u8?                // default cruise; per-leg override allowed
  loop: bool                // patrol vs one-shot
}
```

- **Single GOTO:** from a waypoint pin → pick a peer robot → encode
  `GOTO [lat degE7][lon degE7][alt_m s16][speed u8]` (`0x09`), await the
  returned `task_id`, then poll `TASK_QUERY` (`0x08`). The pin shows a live
  **en-route → arrived/fault** state driven by the poll.
- **Route execution (two options, pick per deployment):**
  1. **App-sequenced (v1, no firmware change):** the app sends leg 1's GOTO,
     watches `TASK_QUERY` until arrival (position within the leg pin's
     `arriveRadiusM`), then sends leg 2, etc. Works with any robot that
     speaks plain GOTO. Cost: one round-trip per leg; the app is the
     conductor (and must stay reachable — see §9).
  2. **Robot-sequenced (later):** a `0x80`-space **ROUTE** app-extension
     command ships the whole leg list in one payload. Budget: ~180 B ÷ 9 B
     per leg (`lat s32 + lon s32 + speed u8`) ≈ **18 legs/packet**; longer
     routes chunk across packets or fall back to app-sequencing. The robot
     conducts itself; survives the operator going dark mid-route.
- **Arrival semantics:** a leg is "reached" when the robot's reported
  position (via STATE/TASK_QUERY or its own advert) is within
  `arriveRadiusM` of the leg pin. Arrival is also a **trigger condition**
  (§5) — "WHEN robot reaches waypoint 3, SAMPLE".
- **Waypoint ≠ destination-only:** a waypoint pin can also carry tags and a
  rule. "Patrol these `#checkpoint` waypoints; at each, SAMPLE and pin the
  result" is a route + a per-arrival trigger.

---

## 5. Pins that trigger business logic

This is the part that needs design thought. The goal: a pin isn't just a dot
or a destination — it can **watch for something and then do something**. That
is a small, safe, offline **rule engine** keyed to pins.

### 5.1 The shape of a rule

Every trigger is the same shape — deliberately boring, because the danger is
in the actions, not the syntax:

```
TriggerRule {
  id,
  enabled: bool,
  armed:   bool                 // see §5.5 — the safety gate for commands
  when:    Condition
  do:      [ Action… ]          // ordered; stop-on-first-failure optional
  scope:   thisPin | tag(#x) | roster(fav|known|tag)   // what/who it watches
  cooldownS: u32                // de-bounce; min gap between fires
  window:   { quietHours?, maxFiresPerHour? }
  log:      [ TriggerEvent… ]   // audit trail (local, append-only)
}
```

`WHEN <condition> DO <actions>`, scoped to a pin, a tag-family of pins, or a
roster of nodes. Read it aloud and it should be obvious what it does — that's
the design test.

### 5.2 Conditions (what fires it)

| Condition | Fires when… | Data it needs | Honest? |
|---|---|---|---|
| **enter(radiusM)** | a watched node's fix moves inside the pin's radius | node positions | only as often as fixes arrive (§9) |
| **exit(radiusM)** | …moves outside (with hysteresis) | node positions | same |
| **dwell(radiusM, minutes)** | a node stays inside ≥ N min | node positions + time | same |
| **arrive(waypoint)** | a robot's GOTO/route leg completes within `arriveRadiusM` | TASK_QUERY / STATE | event-driven, reliable while polling |
| **sensorThreshold** | a `SAMPLE`/telemetry value near the pin crosses a bound | telemetry / SAMPLE | only when a reading lands |
| **schedule** | a wall-clock time / interval | local clock | reliable (local) |
| **manual** | the operator taps "run" | — | always reliable |

Conditions compose minimally: a rule has **one** primary condition in v1
(no boolean trees). If you need "enter AND time-of-day", that's
`enter` + a `window.quietHours`. Keep the trees out until templates prove
insufficient (§5.6).

### 5.3 Actions (what it does)

Two classes, and the split is the whole safety story:

**Class A — local / read-only (auto-fire, no gate):**
- `notify` — OS notification + CueService cue (a new `CueKind.trigger*`) +
  per-theme haptic.
- `log` — append a `TriggerEvent` to the pin's audit trail.
- `pinSample` / `mark` — drop or recolour a pin (e.g. flag a zone occupied).
- `chain` — enable/disable another rule or activate a route locally.

**Class B — state-changing over the mesh (gated, §5.5):**
- `command(robot, lobospeakCmd, args)` — issue a lobospeak command to a
  target peer. Allowed commands are themselves tiered:
  - **read-only commands** (PING/IDENT/STATE/GET_PARAM/SAMPLE) — low risk;
    may auto-fire under a relaxed policy.
  - **state-changing** (GOTO/TASK_START/TASK_STOP/SET_PARAM/RESUME) — high
    risk; **armed + owner only**.
  - **HALT** — special: per lobospeak's safety rule HALT is always allowed and
    can be triggered even by a relaxed policy (a geofence-exit→HALT is a
    legitimate dead-man). But auto-HALT is still rate-limited.
- `broadcast(team)` — push the pin/alert to teammates on the **closed control
  channel** (never Public), as a `0x80`-space PIN-SHARE extension.

### 5.4 Where rules evaluate (the offline reality)

There is **no reliable background collection** (established across the
meshbook/wx/geofence work). So:

- **v1: rules evaluate on the operator's device**, on two clocks: (a)
  event-driven, each time a relevant frame arrives (a node advert, a
  TASK_QUERY response, a SAMPLE); (b) a coarse **foreground tick** (15–30 s)
  for time/dwell. This means a trigger is *as live as the mesh traffic*, and
  the UI must say so ("evaluated on last fix, 6 min ago"). Triggers are an
  operator-assist, not a guaranteed autonomous controller.
- **Robot-side autonomy (later, the honest answer to "real" automation):** a
  rule whose condition and action both live on the robot (a geofence the
  *robot* enforces on itself, sent once via SET_PARAM / a RULE extension) is
  the only way to get reliability when the phone is away. v1 ships the
  operator-side engine; the data model leaves room to *push* a rule to a
  capable robot in a later phase. Spelling this out matters so we don't
  oversell phone-side triggers as firmware-grade.

### 5.5 Safety & trust (the load-bearing part)

Auto-issuing commands over a radio to a physical machine is the sharp edge.
Layered guards, strictest for the most dangerous actions:

1. **Arm switch.** A rule with any Class-B action is **disarmed by default**.
   Arming is an explicit, deliberate operator action (toggle + confirm),
   visually loud, and **auto-disarms** after a configurable TTL or on app
   restart. A disarmed rule still *evaluates and logs/notifies* (Class A) — it
   just won't fire Class B. So you can watch a rule behave before trusting it.
2. **Ownership.** Class-B `command` actions inherit lobospeak's trust model:
   the target robot must have the operator's pubkey in its `owners` set, or
   the command is rejected at the robot (UNAUTHORIZED). The app also checks
   locally before sending. HALT is the deliberate exception.
3. **Rate limits & cooldown.** Per-rule `cooldownS`, global per-hour cap,
   `quietHours`. A flapping geofence can't machine-gun GOTOs.
4. **Dead-man / disarm-all.** A single prominent control disarms every rule
   at once (the "stop the automation" button). Losing connectivity past a
   threshold auto-disarms Class B (you can't supervise what you can't see).
5. **Audit log.** Every fire (and every *suppressed* fire, with reason) is
   appended to the pin's `log` and a global trigger journal. Nothing a rule
   does is invisible after the fact.
6. **Confirmation modes.** Per-rule: `auto` (armed → fires), `confirm` (fires
   raise a notification you must approve within a window), `notifyOnly`
   (never fires Class B, just tells you the condition hit). Default for new
   command rules = `confirm`.

### 5.6 Authoring: templates first, DSL maybe later

- **v1 = templates.** The operator builds a rule from menus: pick a condition
  (with its one parameter), pick action(s) from the allowed set, set scope +
  cooldown + confirmation mode. No free-text. Ships the common cases
  ("geofence exit → HALT + notify", "arrive waypoint → SAMPLE → pin result",
  "node dwells in staging > 30 min → notify team") without a parser or a
  sandbox to secure.
- **Later, only if needed = a tiny expression DSL** for conditions
  (`speed > 5 && tag == "#exclusion"`), evaluated in a pure, side-effect-free
  interpreter. Explicitly deferred — templates cover the use cases we can name
  today, and a DSL is a security surface (it can encode surprising automation)
  that wants its own design pass.

### 5.7 Worked examples (read these to sanity-check the model)

- **Exclusion zone (safety):** Trigger pin, `kind=zone`, `tag=#exclusion`,
  rule `WHEN enter(50m) by roster(#team-robots) DO HALT(thatRobot) + notify`,
  scope = `tag(#exclusion)`, mode = `auto`, armed. → any robot crossing into
  any exclusion pin stops and you're told.
- **Survey patrol (ops):** Route over `#checkpoint` waypoints; per-leg rule
  `WHEN arrive(thisWaypoint) DO command(robot, SAMPLE) + pinSample`. →
  robot walks the checkpoints, sampling, and a sensor map builds itself.
- **Rally watch (team):** `WHEN exit(staging, 100m) by roster(#team) DO
  notify + broadcast(team)`, Class A only, no arm needed. → "someone left
  the staging area" with zero robot risk.
- **Scheduled check (ops):** `WHEN schedule(every 1h) DO command(robot, PING)
  + log`. → a heartbeat probe; if PING times out, that's its own Class-A
  notify.

---

## 6. Unified data model

```
PinStore        // SharedPreferences JSON, global scope, abstract-final pattern
  pins:    Map<id, MapPin>
  routes:  Map<id, Route>
  rules:   Map<id, TriggerRule>   // a rule references its owning pin(s)/scope
  journal: [ TriggerEvent ]        // global audit, capped ring

MapPin       (§1)
Route        (§4)
TriggerRule  (§5.1)
TriggerEvent { ruleId, pinId, atMs, condition, action, outcome, suppressedReason? }
```

Pins are the explicit, confidence-1.0 cousin of R54's `InferredMarker`
(same lat/lon + label + metadata shape) — promotion is a copy with the
source flipped. Reuse, don't re-invent: the marker model, the coordinate /
grid parsers, `geo.haversineMeters`, node tags, CueService + R21
notifications, the grid map projections.

---

## 7. On-mesh wire mapping (what touches lobospeak)

| Pin action | lobospeak frame | Notes |
|---|---|---|
| Send waypoint | `GOTO 0x09` `[lat degE7][lon degE7][alt_m s16][speed u8]` | existing command |
| Poll arrival | `TASK_QUERY 0x08` | existing |
| Trigger → command | any allowed command (§5.3) | gated by §5.5 |
| Robot-sequenced route | **ROUTE** `0x80`-space ext `[n u8][leg…]` | later; ~18 legs/packet |
| Share pin/route to team | **PIN-SHARE** `0x80`-space ext | closed channel only; later |
| Push rule to robot | **RULE** `0x80`-space ext / SET_PARAM | robot-side autonomy; much later |

Everything in the `0x80`-space is **closed-network only** and owners-scoped —
pins and rules never touch the Public channel (lobospeak's hard rule).

---

## 8. UI

- **Pins layer** across the map-like grid modes (`context.skin`-themed):
  drop / drag-nudge / rename / recolour / tag / delete; tag-toggle layers.
- **Pin sheet:** coordinate (copyable, in degE7 + human DMS), tags, kind,
  and — if it's a waypoint — "send to robot"; if it's a trigger — the rule
  editor + its arm switch + recent fires.
- **Rule editor:** the template builder (§5.6) — condition menu, action menu,
  scope, cooldown, confirmation mode, the loud arm toggle.
- **Trigger journal:** a global list of fires/suppressions (the audit log,
  §5.5.5) — also the place a `confirm`-mode rule's pending approvals surface.
- **Disarm-all** control, prominent whenever any Class-B rule is armed.

---

## 9. Honest constraints (carry these into every phase)

- **No background guarantee.** Phone-side triggers are as live as mesh
  traffic + the foreground tick. Surface fix staleness everywhere; never imply
  real-time. Robot-side rules (later) are the only firmware-grade path.
- **Hiding location = unlocated (0/0).** A node with no fix never satisfies a
  geofence condition; it reads "position unknown", not "at 0,0".
- **Payload budget ~180 B.** Routes/shares chunk; degE7 keeps coordinates at
  4 B each.
- **Closed-network only.** Pins, routes, rules, shares — never Public.
- **Local-first.** Pins/rules live on-device; sharing is opt-in and explicit.

---

## 10. Phasing

- **P1 — pins you can see:** MapPin + PinStore; all GPS entry paths (§2);
  tags (§3); the pins layer + sheet. No mesh, no rules. Useful standalone as a
  map-annotation layer; needs no lobospeak codec.
- **P2 — waypoints:** Route model; send-single-GOTO + app-sequenced routes;
  live en-route/arrival state. (Needs the lobospeak codec/controller — the
  first real lobospeak implementation milestone.)
- **P3 — triggers, Class A:** the rule engine, Class-A actions only
  (notify/log/pin/chain), the journal, the geofence/dwell/arrive/schedule
  conditions. Fully useful and **zero robot risk** — this is also where the
  `geofence-node-tracking` earmark lands.
- **P4 — triggers, Class B:** command + broadcast actions, the full arm /
  ownership / rate-limit / confirm / disarm-all safety stack (§5.5).
- **P5 — robot-side autonomy & sharing:** push a rule/route to a capable
  robot; PIN-SHARE to the team over the closed channel.

---

## 11. Open decisions (where I need your steer)

1. **Trigger autonomy default.** For Class-B command rules, is the default
   `confirm` (operator approves each fire) or `auto` once armed? (I've
   speced `confirm` as the safe default — but a true safety reflex like
   geofence→HALT arguably wants `auto`.)
2. **Robot-side rules — in scope or explicitly future?** Phone-side triggers
   are operator-assist, not guaranteed. Do you want the spec to commit to a
   robot-side RULE extension (real autonomy) as a named phase, or leave it as
   "later / depends on robot firmware"?
3. **DSL or templates-only?** Are the named templates (§5.6) enough for your
   use cases, or do you foresee needing the expression DSL — i.e. should I
   design the condition language now?
4. **Route execution model.** App-sequenced (works with any GOTO robot, needs
   the operator present) vs robot-sequenced ROUTE extension (needs firmware
   support, survives operator going dark) — or both, selectable per
   deployment? (Speced as both, app-sequenced first.)
5. **Coordinate scaling.** Confirm GOTO is **degE7** so the pin codec matches
   the robot firmware. If it's degE6/microdegrees, that's a one-constant
   change but must be pinned down before P2.
6. **Pin scope.** Global (a pin is a pin everywhere) vs per-channel. I've
   speced global; teams operate across channels and tags carry team identity.

---

## 12. Glossary

- **pin** — a saved point on the world (lat/lon + label + tags), optionally a
  waypoint and/or a trigger.
- **waypoint** — a pin a robot can be sent to (GOTO); ordered into a route.
- **route** — an ordered list of waypoint legs; app- or robot-sequenced.
- **trigger / rule** — `WHEN condition DO actions`, scoped to a pin, a tag, or
  a roster; the pin-keyed rule engine.
- **Class A / Class B action** — local/read-only (auto) vs state-changing
  over the mesh (gated). The safety split.
- **arm** — the explicit, TTL'd operator gate that lets a rule fire Class-B
  actions; disarmed by default.
- **degE7** — degrees × 10⁷, the s32 wire encoding for lat/lon (~1 cm).
- **PIN-SHARE / ROUTE / RULE** — `0x80`-space lobospeak app-extension commands
  (closed-network only), for later phases.
