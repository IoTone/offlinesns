#!/usr/bin/env python3
"""Generate the "Per-theme rendering plan" section (R18 grid + U6 cues
+ R13 haptic parity) and inject it into both the HTML and Markdown
design briefs. Idempotent: re-runs replace any prior injected block.

Source of truth is the CONCEPTS list below; HTML and Markdown blocks
are rendered from the same data so they stay in sync.
"""
import os, re

# Per-concept palette anchors (matches mm_tokens.dart) for HTML accents.
P = {
    "seele":      dict(accent="#EDE6D6", bg="#0E0E0C", alt="#C8102E"),
    "nerv":       dict(accent="#FF7A00", bg="#121826", alt="#9CFF00"),
    "aghud":      dict(accent="#22D3EE", bg="#10131F", alt="#FF2D78"),
    "hyperlocal": dict(accent="#35E0F0", bg="#161B26", alt="#7CFF6B"),
    "drpop":      dict(accent="#FF2E88", bg="#1C1C24", alt="#D7FF00"),
    "recon":      dict(accent="#FFB000", bg="#0A0A07", alt="#B3231F"),
}

CUE_ORDER = [
    ("messageIn", "channel message"),
    ("dmIn",      "DM"),
    ("discovery", "new node"),
    ("send",      "sent"),
    ("linkUp",    "link up"),
    ("linkDown",  "link down"),
    ("alert",     "alert"),
]

# Per-concept rendering matrix.  `key, label, pack_name, grid, cues,
# haptic`.  Order = the brief's concept order (A..F = NERV, AGHUD,
# Hyperlocal, SEELE, DR Pop, Recon).
CONCEPTS = [
    ("nerv", "A · NERV Terminal", "Mission Control", dict(
        grid="Orange phosphor markers on deep blue; rings drawn as thin "
             "orange brackets with NERV-style corner ticks; range labels "
             "in mono. Self = orange diamond. Pulse = mechanical \"BIP\" "
             "ring overlay at ~0.7 Hz. Rapid-blink = scan-bar sweeps "
             "across the marker. Anonymous-channel ripple = one hard "
             "orange sweep.",
        cues=dict(messageIn="short intercom click",
                  dmIn="double pip (priority)",
                  discovery="harmonic intercom ping",
                  send="soft confirm tick",
                  linkUp="ascending two-tone",
                  linkDown="descending two-tone",
                  alert="klaxon stab"),
        haptic="light on messageIn; medium on linkUp/down; heavy on "
               "alert; selection-click on send.")),
    ("aghud", "B · AG-HUD", "Velocity", dict(
        grid="Cyan markers with neon glow on near-black; rings carry "
             "chevron tick marks (Wipeout HUD). Self = cyan chevron. "
             "Pulse = halo grow/shrink ~0.7 Hz. Rapid-blink = chevron "
             "strobe ~5 Hz. Ripple = velocity-streak from centre, "
             "pink-magenta `alt` tail.",
        cues=dict(messageIn="bright bleep",
                  dmIn="lock-on twin tone",
                  discovery="ping-up arpeggio",
                  send="arcade confirm",
                  linkUp="rising whoosh",
                  linkDown="pitch drop",
                  alert="filter-sweep alarm"),
        haptic="light on messageIn; medium on send/discovery; "
               "selection-click on linkUp; heavy on alert.")),
    ("hyperlocal", "C · Hyperlocal Field", "Sonar", dict(
        grid="Teal markers over a faint sub-grid lattice (map vibe); "
             "rings styled as range bands. Self = teal crosshair. "
             "Pulse = soft glow breathe. Rapid-blink = a small green "
             "satellite orbiting the marker (R13 alternative to flash). "
             "Ripple = sonar-style two-band wave with green accent.",
        cues=dict(messageIn="sonar ping",
                  dmIn="double ping (closer pitch)",
                  discovery="sonar sweep",
                  send="soft chirp",
                  linkUp="filter-open swell",
                  linkDown="filter-close fall",
                  alert="low sonar warble"),
        haptic="light on most events; selection-click on linkUp/down; "
               "medium on alert.")),
    ("seele", "D · SEELE Monolith", "Tribunal", dict(
        grid="Austere — cream markers on pure black; thin cream ring "
             "strokes, no glow. Self = small cream square. Pulse = "
             "severe slow breath ~0.4 Hz. Rapid-blink = a vertical bar "
             "slide across the marker (not a flash — R13 high-contrast "
             "safer). Ripple = one sharp cream ring with a blood-red "
             "accent core. Default theme; sets the discipline.",
        cues=dict(messageIn="soft mallet tone",
                  dmIn="ceremonial double-strike",
                  discovery="distant chime cluster",
                  send="high bell",
                  linkUp="low gong",
                  linkDown="damped gong",
                  alert="sub-bass strike"),
        haptic="light on messageIn; medium on linkUp/down (gavel "
               "feel); selection on send; heavy on alert.")),
    ("drpop", "E · DR Pop", "Pure Phase", dict(
        grid="Magenta markers with lime accents; rings drawn as bold "
             "pop bands. Self = magenta blob. Pulse = bouncy halo ~1 "
             "Hz. Rapid-blink = halftone strobe (kept gentle for R13). "
             "Ripple = alternating magenta + lime band (concept E is "
             "the shipped logo).",
        cues=dict(messageIn="pop bleep",
                  dmIn="funky double-blip",
                  discovery="bouncy ping",
                  send="snap",
                  linkUp="ascending arpeggio",
                  linkDown="descending arpeggio",
                  alert="punky riser"),
        haptic="light/selection on most; medium on send; heavy on "
               "alert.")),
    ("recon", "F · Tactical Recon", "Codec", dict(
        grid="Amber markers on pure black; rings styled as range "
             "gates with tick marks. Self = amber reticle. Pulse = "
             "slow breathing reticle. Rapid-blink = corner target-"
             "lock brackets snapping in. Ripple = radar sweep wedge "
             "fading.",
        cues=dict(messageIn="comm click",
                  dmIn="double click (priority traffic)",
                  discovery="sweep ping",
                  send="key-up click",
                  linkUp="squelch-up",
                  linkDown="squelch-down",
                  alert="comm-channel alarm"),
        haptic="selection-click predominant (comm feel); medium on "
               "linkUp/down; heavy on alert.")),
]

# ----- HTML render -------------------------------------------------

def _html_concept(key, label, pack, d):
    pal = P[key]
    cues_rows = "".join(
        f'<tr><td style="padding:2px 10px 2px 0;color:#8a93a8">'
        f'<code>{ck}</code></td><td style="padding:2px 0">'
        f'{d["cues"][ck]}</td></tr>'
        for ck, _ in CUE_ORDER
    )
    return f'''<div style="background:#0d111c;border:1px solid #1d2436;\
border-radius:12px;padding:14px;margin:10px 0">
<div style="display:flex;align-items:center;gap:10px;margin-bottom:8px">
<div style="width:14px;height:14px;border-radius:3px;background:\
{pal['accent']};box-shadow:inset 0 0 0 1px {pal['alt']}"></div>
<div style="font-size:15px;letter-spacing:.04em;color:#dfe">{label}</div>
<div style="font-size:11px;color:#8a93a8">pack: <strong>{pack}</strong></div>
</div>
<div style="display:grid;grid-template-columns:1.3fr 1fr;gap:14px">
<div>
<div style="font-size:11px;letter-spacing:2px;color:{pal['accent']};\
margin-bottom:4px">GRID (R18)</div>
<div style="font-size:13px;color:#cfe;line-height:1.45">{d['grid']}</div>
<div style="font-size:11px;letter-spacing:2px;color:{pal['accent']};\
margin:10px 0 4px">HAPTIC PARITY (R13)</div>
<div style="font-size:13px;color:#cfe;line-height:1.45">{d['haptic']}</div>
</div>
<div>
<div style="font-size:11px;letter-spacing:2px;color:{pal['accent']};\
margin-bottom:4px">AUDIBLE PACK · per cue</div>
<table style="font-size:12px;color:#cfe;border-collapse:collapse;\
width:100%">{cues_rows}</table>
</div>
</div>
</div>'''

def html_section():
    cards = "".join(_html_concept(k, l, p, d) for k, l, p, d in CONCEPTS)
    return f'''
<section class="concept" id="render-plan">
  <h2>Per-theme rendering plan
    <span class="tag">How R18's hyperlocal grid + U6's CueService
    render per theme — same code path, theme-driven visuals + audio
    pack. Defaults ship now (SystemSound + HapticFeedback); per-theme
    audio assets are a later authoring step.</span></h2>
  <div class="cbody">
    <div class="note">Discipline: <strong>colour is never the sole
    channel of information</strong> (R13). Brightness encodes
    recency (R18), position encodes who; animation distinguishes
    "known" (pulse) from "contact" (rapid blink); a haptic always
    accompanies an audible cue.  Reduce-motion replaces flashes with
    static badges/ring overlays.</div>
    {cards}
  </div>
</section>
'''

# ----- Markdown render ---------------------------------------------

def _md_concept(key, label, pack, d):
    cues = "\n".join(
        f'- `{ck}` ({short}): {d["cues"][ck]}' for ck, short in CUE_ORDER)
    return f'''### {label}

- **Audible pack:** **{pack}**
- **Grid (R18):** {d['grid']}
- **Audible cues:**
{cues}
- **Haptic parity (R13):** {d['haptic']}
'''

def md_section():
    cards = "\n".join(_md_concept(k, l, p, d) for k, l, p, d in CONCEPTS)
    return f'''
## Per-theme rendering plan (R18 grid · U6 cues · R13 haptic parity)

How the in-app hyperlocal grid (R18 / U9) and the CueService event
cues (R12 / U6) render per theme. Same code paths; theme drives
visuals, palette, and audio pack. Defaults ship now (built-in
`SystemSound` + `HapticFeedback`); per-theme audio assets are a
later authoring step.

> Discipline: **colour is never the sole channel of information**
> (R13). Brightness encodes recency (R18); position encodes who;
> animation distinguishes "known" (pulse) from "contact" (rapid
> blink); a haptic always accompanies an audible cue. Reduce-motion
> replaces flashes with static badges/ring overlays.

{cards}
'''

# ----- Injectors ---------------------------------------------------

HERE = os.path.dirname(os.path.abspath(__file__))
BRIEF_HTML = os.path.join(os.path.dirname(HERE), "meshmore-sns-UX-brief.html")
BRIEF_MD   = os.path.join(os.path.dirname(HERE), "meshmore-sns-UX-brief.md")

def inject_html():
    html = open(BRIEF_HTML).read()
    html = re.sub(
        r'\n<section class="concept" id="render-plan">.*?</section>\n',
        '\n', html, flags=re.S)
    # Insert right after the concept-grid wrapper closes (line with
    # `</div>` immediately preceding the "Shared screens" section).
    marker = '\n</div>\n\n<section style="padding:8px clamp(16px,4vw,48px) 0">'
    assert marker in html, "could not find brief grid-close marker"
    html = html.replace(
        marker,
        '\n</div>\n' + html_section() + '\n<section style="padding:8px clamp(16px,4vw,48px) 0">',
        1)
    open(BRIEF_HTML, "w").write(html)

def inject_md():
    md = open(BRIEF_MD).read()
    md = re.sub(
        r'\n## Per-theme rendering plan.*?(?=\n## )',
        '\n', md, flags=re.S)
    marker = '\n## Configuration & profile screens'
    assert marker in md, "could not find brief 'Configuration' anchor"
    md = md.replace(marker, md_section() + marker, 1)
    open(BRIEF_MD, "w").write(md)

if __name__ == "__main__":
    inject_html()
    inject_md()
    print("brief updated: per-theme rendering plan injected")
