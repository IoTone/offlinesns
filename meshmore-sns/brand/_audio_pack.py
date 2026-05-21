#!/usr/bin/env python3
"""Synthesize the per-theme audio assets for R12 CueService — 6 themes
× 7 CueKinds = 42 WAV files. Pure stdlib (wave + struct + math), so
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
    ),
    "aghud": dict(  # clean HUD digital
        base=659.25,                      # E5
        timbre=lambda f, t: 0.6 * _sine(f, t)
                            + 0.4 * _square(f, t, harmonics=3),
        decay=6.0,
    ),
    "hyperlocal": dict(  # sonar ping, long decay
        base=440.0,                       # A4
        timbre=lambda f, t: _sine(f, t),
        decay=2.0,                        # long ring-out
    ),
    "drpop": dict(  # bright pop / synth pluck
        base=783.99,                      # G5
        timbre=lambda f, t: 0.55 * _saw(f, t)
                            + 0.45 * _sine(f * 2, t),
        decay=7.0,
    ),
    "recon": dict(  # codec chirp, stuttery
        base=587.33,                      # D5
        timbre=lambda f, t: 0.85 * _triangle(f, t)
                            + 0.15 * _sine(f * 3, t),
        decay=9.0,
        stutter=True,
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
    "send": dict(
        # very brief, just-acknowledged
        score=[(2.0, 0.06)],
        amp=0.35,
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
}

# ---------------------------------------------------------------- synth

def _synth_one(theme_key, cue_key):
    th = THEMES[theme_key]
    cue = CUES[cue_key]
    score = cue["score"]
    amp = cue["amp"]
    base = th["base"]
    timbre = th["timbre"]
    decay = th["decay"]
    stutter = th.get("stutter", False)

    samples = []
    for ratio, dur in score:
        n = int(SR * dur)
        f = base * ratio
        for i in range(n):
            t = i / SR
            s = timbre(f, t) * _exp_env(t, dur, decay)
            if stutter:
                s *= _stutter(t, dur)
            s = amp * s
            # Hard clip to int16
            s = max(-1.0, min(1.0, s))
            samples.append(int(s * 32767))
    # Optional small fade-out at the very end (8 ms) to avoid clicks
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
