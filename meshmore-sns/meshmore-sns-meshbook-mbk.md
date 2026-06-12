# Meshbook (MBk) — design (earmark)

**Status:** design only — earmarked, not built. Captured 2026-06-07.
**Owner concept:** a Hyperlocal-grid analysis view, sibling to **wx**
(microclimate). Where **wx** mines *weather* from channel chat, **MBk**
mines the *social shape* of a channel's day.

## 1. One-liner

A daily "who's talking, about what, how much" readout for the **current
channel** — a leaderboard of the top voices, an hourly activity
histogram, light topical analysis, and a reply rate — scoped to **today**
and reset each day.

## 2. Scope (from the brief)

- **Source:** messages on the **currently-selected channel** (not DMs).
- **Top 10 "names":** the most active senders (by message count), with
  per-sender stats.
- **Frequency stats per hour:** an hourly histogram of message volume
  across the channel (a 24-slot bar chart / heat strip).
- **Topical analysis:** lightweight — recurring keywords/topics in the
  day's messages (offline; reuse the wx-lexicon style + place inference
  for "where", and a simple keyword/term-frequency pass for "what").
- **% replies:** share of messages that are replies (detect the reply
  quote prefix — see `buildReplyQuote` / the `>` quote convention used by
  R20 chat actions).
- **Window:** default **24 h**, but **focus on the current day** (local
  midnight → now), and **reset daily** (a new day starts fresh).
- **Refresh:** a **setting for how often** the analysis recomputes
  (e.g. 1 / 5 / 15 min, or manual).

## 3. What exists to build on

- **Messages:** `MeshcoreController.messagesFor(channelIdx)` (oldest
  first) + `incomingChannelMessages`. `ChatMessage{text, peerPubKeyHex,
  at, channelIdx, outgoing}`.
- **Sender → name:** `_resolveChannelSender(text)` parses a `"name: "`
  prefix to a `DiscoveredNode`; channel messages carry the sender name
  inline (anonymous-by-protocol otherwise). Top-10 "names" likely keys
  off the parsed display name, not pubkey (channel senders aren't
  attributable by key).
- **Replies:** the chat-actions reply flow prefixes a quote
  (`buildReplyQuote` / a leading `> …`); detect that prefix.
- **Topics:** mirror the wx approach — a pure-Dart term pass; optionally
  reuse `PlaceInferenceEngine` for location topics and the
  `weather_lexicon` pattern for a small topic lexicon.
- **Surface:** a Hyperlocal-grid `_GridViewMode` (like `weather`/wx), a
  "MBk" / "Meshbook" mode. Per-locale labels via ARB.
- **Settings pattern:** the place-inference per-channel toggle +
  persisted prefs are the model for the refresh-interval setting.

## 4. Sketch data model

```
class MbkSender { String name; int count; int replies; DateTime lastAt;
                  Map<int,int> perHour; }     // 0..23 local hour → count
class MbkDay {
  int channelIdx;
  DateTime dayStart;                          // local midnight
  List<int> hourly;                           // 24 slots, channel-wide
  List<MbkSender> top;                         // top 10 by count
  int total, replies;                          // % replies = replies/total
  List<({String term, int n})> topics;         // light topical pass
  DateTime computedAt;
}
```

`MbkEngine.analyse(messages, dayStart, {now})` → `MbkDay` (pure,
unit-testable). A small store recomputes on the refresh interval and on
day rollover.

## 5. UI sketch

```
┌ MESHBOOK · CH0 · today ──────────── ⟳ ┐
│ 37 msgs · 12 replies (32%) · 6 voices │
│ ▁▁▂▅█▆▃▂▁ … hourly →                   │
│ 1 Kanako.1   14  ▓▓▓▓▓ · 5 replies     │
│ 2 Davi1       9  ▓▓▓                   │
│ … top 10                              │
│ topics: weather · ferry · meetup      │
└───────────────────────────────────────┘
```

Skin-aware (MmTokens + VizPalette). The hourly strip uses the heat
palette; bars/rows use fg/accent.

## 6. Phasing

- **P1** — pure-Dart `MbkEngine.analyse` (top-N, hourly, reply %) + tests;
  a basic Meshbook grid view rendering it for the current day.
- **P2** — topical analysis (term pass + optional place topics).
- **P3** — refresh-interval setting + daily reset/rollover + persistence.

## 7. Open questions

- **Identity on channels:** channel messages are anonymous-by-protocol;
  "names" come from the inline `name:` prefix, which can collide or be
  spoofed. Treat "names" as display handles, not identities.
- **Reset semantics:** reset *daily* but keep yesterday for a
  "yesterday vs today" compare? (brief says reset daily — keep it simple
  first; archive later if wanted.)
- **Topic quality:** keep it light + offline; avoid over-promising
  "topics" — start with term frequency + the wx/place lexicons.
