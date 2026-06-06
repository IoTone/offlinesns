#!/usr/bin/env python3
"""Synthesize the per-theme audio assets for R12 CueService — one WAV
per (theme × CueKind). Pure stdlib (wave + struct + math), so
re-runnable from any Python 3.

Each theme has a **sonic signature** (base note + waveshape + envelope
character), and each CueKind has a distinct semantic shape applied on
top of that signature — so the same `tick` motif sounds different in
SEELE vs DR Pop, and `messageIn` sounds different from `alert` in
both themes.

Output: meshmore_sns_app/responsive_starter_app/assets/audio/<theme>/<cue>.wav
"""
import os
import math
import struct
import wave

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(HERE)
OUT = os.path.join(
    ROOT, "meshmore_sns_app", "responsive_starter_app",
    "assets", "audio")

SR = 22050  # sample rate (good enough for short cues, ~half MP3 size)

# ---------------------------------------------------------------- waveshapes

def _sine(f, t):
    return math.sin(2 * math.pi * f * t)

def _square(f, t, harmonics=5):
    """Band-limited square (additive synthesis, sum of odd harmonics)."""
    s = 0.0
    for k in range(1, harmonics * 2, 2):
        s += math.sin(2 * math.pi * f * k * t) / k
    return s * (4 / math.pi)

def _saw(f, t, harmonics=10):
    """Band-limited sawtooth."""
    s = 0.0
    for k in range(1, harmonics + 1):
        s += math.sin(2 * math.pi * f * k * t) / k
    return s * (2 / math.pi)

def _triangle(f, t, harmonics=6):
    s = 0.0
    for k in range(1, harmonics * 2, 2):
        s += ((-1) ** ((k - 1) // 2)) * math.sin(
            2 * math.pi * f * k * t) / (k * k)
    return s * (8 / (math.pi * math.pi))

# Per-theme synth: returns a fn (freq, t) -> sample in [-1, 1].
THEMES = {
    "seele": dict(  # default — stark monolith, dark
        base=220.0,                       # A3
        timbre=lambda f, t: 0.85 * _sine(f, t)
                            + 0.15 * _sine(f / 2, t),  # add a sub
        decay=3.5,
    ),
    "nerv": dict(  # mission-control telemetry blip
        base=523.25,                      # C5
        timbre=lambda f, t: 0.7 * _square(f, t)
                            + 0.3 * _sine(f * 2, t),
        decay=8.0,
        # NERV "Mission Control" — austere, synthetic, terse. Overrides
        # the shared motifs (which read generic) with the brief's
        # specifics: a terse 2-tone message-in, Geiger-counter discovery
        # ticks, and a descending **sawtooth klaxon** for critical /
        # link-lost. The rest fall through to square-wave blips.
        cues={
            # Crisp upper-register double-tick — a "telemetry packet
            # arrived" blip, clearly synthetic and distinct from a chime.
            "messageIn": dict(score=[(2.0, 0.035), (1.5, 0.05)], amp=0.52),
            # DM is a distinct 3-blip burst so it doesn't read as channel.
            "dmIn": dict(
                score=[(2.0, 0.04), (1.5, 0.045), (2.0, 0.055)],
                amp=0.55),
            # New node — a soft rising two-blip "a node appeared". (Was a
            # sharp Geiger-tick burst; the user heard it as static/insects
            # over the air, so this is a cleaner tonal cue instead.)
            "discovery": dict(
                score=[(1.0, 0.05), (1.5, 0.07)], amp=0.42),
            # Re-advert — a SINGLE soft low blip (ambient "still out
            # there"), quiet and sparse so the background re-advert traffic
            # doesn't read as static. Distinct from discovery's rising pair.
            "advert": dict(
                score=[(0.75, 0.06)], amp=0.24),
            # App power-on — an ascending square sweep (terminal warming
            # up), distinct from the tonal triad the other themes use.
            "boot": dict(
                kind="sweep", start=0.5, end=1.5, dur=0.40, amp=0.55),
            # Forward swipe — crisp ascending square pair.
            "navNext": dict(
                score=[(1.5, 0.03), (2.0, 0.04)], amp=0.34),
            # Back swipe — crisp descending square pair (mirror).
            "navPrev": dict(
                score=[(2.0, 0.03), (1.5, 0.04)], amp=0.34),
            # Descending sawtooth klaxon, wailed twice (emergency).
            "alert": dict(
                kind="sweep", start=2.4, end=0.7, dur=0.32, repeat=2,
                amp=0.82, saw=True),
            # Link lost — a single shorter descending wail.
            "linkDown": dict(
                kind="sweep", start=1.7, end=0.55, dur=0.34, amp=0.68,
                saw=True),
            # Link up — terse ascending square arpeggio (positive).
            "linkUp": dict(
                score=[(1.0, 0.06), (1.5, 0.06), (2.0, 0.08)], amp=0.62),
            # Task error — a short descending saw (softer than alert).
            "taskError": dict(
                kind="sweep", start=1.25, end=0.85, dur=0.18, amp=0.55,
                saw=True),
        },
    ),
    "aghud": dict(  # clean HUD digital
        base=659.25,                      # E5
        timbre=lambda f, t: 0.6 * _sine(f, t)
                            + 0.4 * _square(f, t, harmonics=3),
        decay=6.0,
        # B — AG-HUD "Velocity" pack: arcade / electronic, energetic.
        # Bright ascending arpeggios, rising/falling whooshes on swipe, a
        # major-third confirm on send, an urgent rising alert.
        cues={
            "messageIn": dict(score=[(1.5, 0.05), (2.0, 0.06)], amp=0.50),
            "dmIn": dict(
                score=[(1.0, 0.05), (1.5, 0.05), (2.0, 0.07)], amp=0.55),
            # Discovery — a bright "pickup" arpeggio.
            "discovery": dict(
                score=[(1.0, 0.05), (1.5, 0.05), (2.5, 0.08)], amp=0.50),
            # Send — a clean major-third confirm (1 : 5/4).
            "send": dict(score=[(1.0, 0.05), (1.25, 0.08)], amp=0.45),
            # Navigation — rising / falling whooshes (glissando).
            "navNext": dict(
                kind="sweep", start=1.0, end=2.2, dur=0.18, amp=0.42),
            "navPrev": dict(
                kind="sweep", start=2.2, end=1.0, dur=0.18, amp=0.42),
            # Link up — energetic ascending triad.
            "linkUp": dict(
                score=[(1.0, 0.06), (1.5, 0.06), (2.0, 0.09)], amp=0.60),
            # Alert — an urgent rising sweep, wailed twice.
            "alert": dict(
                kind="sweep", start=1.5, end=3.0, dur=0.22, repeat=2,
                amp=0.74),
            "taskOk": dict(score=[(2.0, 0.07), (2.5, 0.10)], amp=0.55),
        },
    ),
    "hyperlocal": dict(  # sonar ping, long decay
        base=440.0,                       # A4
        timbre=lambda f, t: _sine(f, t),
        decay=2.0,                        # long ring-out
        # C — Hyperlocal "Sonar" pack: calm, organic, field-tuned. The
        # discovery ping sonically mirrors the radar blip; messages are a
        # gentle two-note; link state is low calm tones. Sparse + soft so
        # it doesn't fatigue outdoors. Pure sine + the theme's long ring.
        cues={
            # Node discovery — a classic sonar "ping": a sine tone that
            # glides slightly down and rings out (mirrors the radar blip).
            "discovery": dict(
                kind="sweep", start=1.6, end=1.18, dur=0.5, amp=0.5),
            # Re-advert — a soft single ping, quiet and sparse (the
            # ambient "still out there" pulse). Rides the long ring.
            "advert": dict(score=[(1.0, 0.12)], amp=0.26),
            # Gentle two-note message (calm rising pair).
            "messageIn": dict(
                score=[(1.0, 0.10), (1.25, 0.16)], amp=0.40),
            # DM — a more present two-note (wider interval).
            "dmIn": dict(
                score=[(1.0, 0.10), (1.5, 0.18)], amp=0.46),
            # Link state — low calm tones (down an octave from base).
            "linkUp": dict(
                score=[(0.75, 0.12), (1.0, 0.18)], amp=0.46),
            "linkDown": dict(
                score=[(1.0, 0.12), (0.75, 0.18)], amp=0.46),
        },
    ),
    "drpop": dict(  # bright pop / synth pluck
        base=783.99,                      # G5
        timbre=lambda f, t: 0.55 * _saw(f, t)
                            + 0.45 * _sine(f * 2, t),
        decay=7.0,
        # E — DR Pop "Pure Phase" pack: bright syncopated arcade blips,
        # funky confirms, Wipeout-soundtrack energy. Short + punchy (the
        # theme's fast decay) and pitched high for a rave-y pop.
        cues={
            "messageIn": dict(score=[(2.0, 0.04), (2.0, 0.05)], amp=0.50),
            "dmIn": dict(
                score=[(2.0, 0.04), (1.5, 0.04), (3.0, 0.06)], amp=0.55),
            # Discovery — an arcade "coin" pickup.
            "discovery": dict(
                score=[(1.5, 0.04), (2.0, 0.04), (3.0, 0.07)], amp=0.50),
            # Send — a funky two-blip confirm.
            "send": dict(score=[(2.0, 0.04), (3.0, 0.07)], amp=0.50),
            "navNext": dict(score=[(2.0, 0.03), (3.0, 0.05)], amp=0.40),
            "navPrev": dict(score=[(3.0, 0.03), (2.0, 0.05)], amp=0.40),
            "linkUp": dict(
                score=[(1.0, 0.05), (2.0, 0.05), (3.0, 0.08)], amp=0.60),
            # Alert — a syncopated rave alarm (triple wail).
            "alert": dict(
                kind="sweep", start=2.0, end=3.0, dur=0.16, repeat=3,
                amp=0.70),
            "taskOk": dict(score=[(2.0, 0.05), (3.0, 0.09)], amp=0.55),
        },
    ),
    "recon": dict(  # codec chirp, stuttery
        base=587.33,                      # D5
        timbre=lambda f, t: 0.85 * _triangle(f, t)
                            + 0.15 * _sine(f * 3, t),
        decay=9.0,
        stutter=True,
        # F — Recon "Codec" pack: comms-radio. Terse UHF beeps (the
        # theme's stutter gate makes score cues chirp like a codec),
        # squelch sweeps on link change, and the MGS-style codec ring for
        # critical. Quiet — designed to run *fully silent* without losing
        # information (parity is the amber blink + haptic).
        cues={
            # Terse UHF beeps.
            "messageIn": dict(score=[(2.0, 0.05)], amp=0.45),
            "dmIn": dict(score=[(2.0, 0.04), (2.0, 0.05)], amp=0.48),
            "discovery": dict(score=[(1.5, 0.05), (2.0, 0.06)], amp=0.45),
            # Re-advert — a faint single beep.
            "advert": dict(score=[(2.0, 0.04)], amp=0.28),
            "send": dict(score=[(2.0, 0.05)], amp=0.40),
            # Link change — short squelch sweeps (open / close).
            "linkUp": dict(
                kind="sweep", start=1.0, end=1.8, dur=0.14, amp=0.50),
            "linkDown": dict(
                kind="sweep", start=1.8, end=0.8, dur=0.16, amp=0.50),
            # Critical — the codec-call ring: an alternating two-tone,
            # repeated (doo-doo-doo-doo).
            "alert": dict(
                score=[(1.5, 0.06), (1.0, 0.06), (1.5, 0.06), (1.0, 0.07)],
                amp=0.60),
            "taskOk": dict(score=[(2.0, 0.05), (2.5, 0.07)], amp=0.48),
        },
    ),
}

# ---------------------------------------------------------------- envelopes

def _exp_env(t, dur, decay):
    """Sharp attack (5 ms) + exponential decay."""
    attack = 0.005
    if t < attack:
        return t / attack
    return math.exp(-decay * (t - attack) / dur)

def _stutter(t, dur, period=0.025):
    """Square-wave gate on top of any envelope — gives a codec-chirp
    pulsed feel. period = full cycle (on/off) in seconds."""
    return 1.0 if (t % period) < (period / 2) else 0.0

# ---------------------------------------------------------------- CueKind motifs
# Each CueKind has a *score* — a sequence of (freq_ratio, dur_s) pairs
# played sequentially, plus a duration modifier and an amplitude.
# Frequencies are ratios of the theme's base note so each theme keeps
# its identity while the motif shape says "this is a DM" vs "this is
# an alert".

CUES = {
    "messageIn": dict(
        # short single note, soft
        score=[(1.0, 0.12)],
        amp=0.45,
    ),
    "dmIn": dict(
        # base + perfect 5th, quick two-note arpeggio
        score=[(1.0, 0.10), (1.5, 0.12)],
        amp=0.55,
    ),
    "discovery": dict(
        # quick rising pair
        score=[(1.0, 0.06), (4 / 3, 0.10)],
        amp=0.50,
    ),
    "advert": dict(
        # a single soft mid ping — "a node is out there", gentler and
        # sparser than discovery (a brand-new node). Rate-limited app-side.
        score=[(1.5, 0.08)],
        amp=0.30,
    ),
    "send": dict(
        # very brief, just-acknowledged
        score=[(2.0, 0.06)],
        amp=0.35,
    ),
    "boot": dict(
        # app power-on — a warm ascending triad, a touch longer so it
        # reads as "coming to life" under the splash.
        score=[(0.5, 0.10), (1.0, 0.10), (1.5, 0.16)],
        amp=0.55,
    ),
    "navNext": dict(
        # forward swipe — a short upward blip
        score=[(1.0, 0.04), (1.5, 0.05)],
        amp=0.32,
    ),
    "navPrev": dict(
        # back swipe — a short downward blip (mirror of navNext)
        score=[(1.5, 0.04), (1.0, 0.05)],
        amp=0.32,
    ),
    "linkUp": dict(
        # positive ascending 2-note (root, fifth, octave-ish)
        score=[(1.0, 0.09), (1.5, 0.09), (2.0, 0.11)],
        amp=0.60,
    ),
    "linkDown": dict(
        # negative descending 2-note (octave, fifth, root)
        score=[(2.0, 0.09), (1.5, 0.09), (1.0, 0.13)],
        amp=0.60,
    ),
    "alert": dict(
        # base + dissonant tritone, longer + louder
        score=[(1.0, 0.16), (math.sqrt(2), 0.20)],
        amp=0.75,
    ),
    "scanStart": dict(
        # The "scanning…" drone — LOOPED by the player for the whole scan
        # window (not a one-shot). A held low tone with slow vibrato; long
        # dur + short fades so the loop is near-steady and click-free, and
        # a quieter amp (the loop player also lowers gain) so it sits under
        # everything during a multi-second scan.
        kind="hum", ratio=0.5, dur=1.5, vib_depth=0.010, vib_rate=5.0,
        attack=0.02, release=0.05, amp=0.26,
    ),
    "taskOk": dict(
        # rising 5th to octave — positive resolve, subtler than linkUp
        score=[(1.5, 0.10), (2.0, 0.13)],
        amp=0.55,
    ),
    "taskError": dict(
        # descending minor-third pair + slight detune — softer than
        # full `alert` (which is reserved for protocol-critical fail)
        score=[(1.0, 0.10), (5 / 6, 0.16)],
        amp=0.60,
    ),
}

# ---------------------------------------------------------------- synth

_GEIGER_GAPS = [0.03, 0.075, 0.04, 0.095, 0.05, 0.065, 0.085]


def _synth_score(th, cue):
    """Tonal motif — a sequence of (freq_ratio, dur) notes in the
    theme's timbre. The default cue shape."""
    base, timbre, decay = th["base"], th["timbre"], th["decay"]
    stutter = th.get("stutter", False)
    samples = []
    for ratio, dur in cue["score"]:
        n = int(SR * dur)
        f = base * ratio
        for i in range(n):
            t = i / SR
            s = timbre(f, t) * _exp_env(t, dur, decay)
            if stutter:
                s *= _stutter(t, dur)
            s = cue["amp"] * s
            samples.append(int(max(-1.0, min(1.0, s)) * 32767))
    return samples


def _synth_sweep(th, cue):
    """A glissando — frequency glides start→end (exponential), with
    phase accumulation so the pitch bends cleanly. `saw=True` uses a
    raw (gritty) sawtooth for the descending-klaxon character; `repeat`
    wails it N times. Used for NERV's alert / link-lost."""
    base, decay = th["base"], th["decay"]
    start, end = base * cue["start"], base * cue["end"]
    dur, amp = cue["dur"], cue["amp"]
    repeat, use_saw = cue.get("repeat", 1), cue.get("saw", False)
    samples = []
    for _ in range(repeat):
        n = int(SR * dur)
        phase = 0.0
        for i in range(n):
            t = i / SR
            frac = i / n
            f = start * (end / start) ** frac
            phase += 2 * math.pi * f / SR
            if use_saw:
                s = 2.0 * ((phase / (2 * math.pi)) % 1.0) - 1.0
            else:
                s = th["timbre"](f, t)
            s = amp * s * _exp_env(t, dur, decay)
            samples.append(int(max(-1.0, min(1.0, s)) * 32767))
    return samples


def _synth_hum(th, cue):
    """A sustained hum — a held tone in the theme's timbre with a soft
    linear attack/release (no exp decay, so it actually *sustains*) and a
    slow vibrato. Used for the "scanning…" drone."""
    base = th["base"]
    f = base * cue.get("ratio", 0.5)
    dur, amp = cue["dur"], cue["amp"]
    depth, vrate = cue.get("vib_depth", 0.012), cue.get("vib_rate", 6.0)
    # Attack/release fade the clip ends to zero so it loops click-free
    # (the player repeats this WAV for the whole scan window). Keep them
    # short so the looped drone reads as near-steady, not pulsing.
    attack = cue.get("attack", 0.03)
    release = cue.get("release", 0.10)
    timbre = th["timbre"]
    n = int(SR * dur)
    phase = 0.0
    samples = []
    for i in range(n):
        t = i / SR
        # vibrato — accumulate phase so the pitch wobbles cleanly
        fi = f * (1.0 + depth * math.sin(2 * math.pi * vrate * t))
        phase += 2 * math.pi * fi / SR
        # render the theme timbre at the wobbled phase (t≈phase/2πf)
        s = timbre(fi, phase / (2 * math.pi * fi)) if fi else 0.0
        if t < attack:
            env = t / attack
        elif t > dur - release:
            env = max(0.0, (dur - t) / release)
        else:
            env = 1.0
        samples.append(int(max(-1.0, min(1.0, amp * s * env)) * 32767))
    return samples


def _synth_ticks(th, cue):
    """Geiger-counter ticks — a run of very short, sharp high square
    clicks at irregular (deterministic) spacing. Used for NERV's node
    discovery ("a node decayed into view")."""
    f = th["base"] * cue.get("ratio", 3.0)
    amp = cue["amp"]
    count = cue["count"]
    click = 0.008
    cn = int(SR * click)
    samples = []
    for c in range(count):
        samples.extend([0] * int(SR * _GEIGER_GAPS[c % len(_GEIGER_GAPS)]))
        for i in range(cn):
            t = i / SR
            s = _square(f, t, harmonics=8) * math.exp(-40 * t / click)
            samples.append(int(max(-1.0, min(1.0, amp * s)) * 32767))
    return samples


def _synth_one(theme_key, cue_key):
    th = THEMES[theme_key]
    # A theme may override a cue's whole character (NERV's Mission
    # Control set); otherwise the shared motif applies.
    cue = th.get("cues", {}).get(cue_key, CUES[cue_key])
    kind = cue.get("kind", "score")
    if kind == "sweep":
        samples = _synth_sweep(th, cue)
    elif kind == "ticks":
        samples = _synth_ticks(th, cue)
    elif kind == "hum":
        samples = _synth_hum(th, cue)
    else:
        samples = _synth_score(th, cue)
    # Small fade-out at the very end (8 ms) to avoid clicks.
    fade_n = int(SR * 0.008)
    for i in range(max(0, len(samples) - fade_n), len(samples)):
        k = (len(samples) - i) / fade_n
        samples[i] = int(samples[i] * k)
    return samples


def _write_wav(path, samples):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with wave.open(path, "wb") as w:
        w.setnchannels(1)
        w.setsampwidth(2)
        w.setframerate(SR)
        w.writeframes(struct.pack("<" + "h" * len(samples), *samples))


def main():
    n = 0
    for theme_key in THEMES:
        for cue_key in CUES:
            path = os.path.join(OUT, theme_key, f"{cue_key}.wav")
            _write_wav(path, _synth_one(theme_key, cue_key))
            n += 1
            print(f"  {os.path.relpath(path, ROOT)}")
    print(f"\nwrote {n} files (= {len(THEMES)} themes × {len(CUES)} cues)")


if __name__ == "__main__":
    main()
