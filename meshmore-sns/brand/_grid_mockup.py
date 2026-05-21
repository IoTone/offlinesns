#!/usr/bin/env python3
"""Generate per-theme SVG mockups of the R18 / U9 hyperlocal grid and
inject them into the HTML design brief. Companion to
`brand/_render_plan.py` (textual plan); this one is the picture.

Each mockup uses the same node fleet + the same channel-message
ripple snapshot, so the differences read as pure theme identity.
"""
import os, re, math

# Palette anchors — mirror mm_tokens.dart.
P = {
    "seele":      dict(bg="#0E0E0C", bg2="#000000", accent="#EDE6D6",
                       alt="#C8102E"),
    "nerv":       dict(bg="#121826", bg2="#0A0E1A", accent="#FF7A00",
                       alt="#9CFF00"),
    "aghud":      dict(bg="#10131F", bg2="#05060B", accent="#22D3EE",
                       alt="#FF2D78"),
    "hyperlocal": dict(bg="#161B26", bg2="#0B0F17", accent="#35E0F0",
                       alt="#7CFF6B"),
    "drpop":      dict(bg="#1C1C24", bg2="#101014", accent="#FF2E88",
                       alt="#D7FF00"),
    "recon":      dict(bg="#0A0A07", bg2="#000000", accent="#FFB000",
                       alt="#B3231F"),
}

# Brief presentation order — leads with the shipped logo (E) and
# default theme (D) for context.
ORDER = [
    ("drpop",      "E · DR Pop",          "Pure Phase",
     "shipped logo"),
    ("seele",      "D · SEELE Monolith",  "Tribunal",
     "default theme"),
    ("nerv",       "A · NERV Terminal",   "Mission Control", ""),
    ("aghud",      "B · AG-HUD",          "Velocity",        ""),
    ("hyperlocal", "C · Hyperlocal Field","Sonar",           ""),
    ("recon",      "F · Tactical Recon",  "Codec",           ""),
]

# Canvas: 480×480, centre (240,240), three rings at 80 / 160 / 220.
def pos(angle_deg, radius):
    a = math.radians(angle_deg)
    return (240 + radius * math.sin(a), 240 - radius * math.cos(a))

# A small node fleet — same layout & kinds for every theme so the
# only thing that changes between mockups is the theme's visual
# identity.
NODES = [
    (60,  140, "known"),   # mid-distance, NE — known (pulse halo)
    (200, 160, "fav"),     # mid-distance, SW — favourite (alt ring)
    (300, 215, "fresh"),   # outer ring, NW — plain fresh
    (350,  78, "neigh"),   # near ring, N — recently registered
    (130, 215, "stale"),   # outer ring, SE — stale (low brightness)
]
RIPPLE_R = 110  # mid-progress anonymous-channel ripple

def _self_marker(key, pal):
    a = pal["accent"]
    if key == "nerv":
        return (f'<polygon points="240,224 256,240 240,256 224,240" '
                f'fill="{a}"/>'
                f'<rect x="222" y="222" width="36" height="36" '
                f'fill="none" stroke="{a}" stroke-opacity=".45"/>')
    if key == "aghud":
        return (f'<polyline points="222,256 240,234 258,256" '
                f'stroke="{a}" stroke-width="3" fill="none"/>'
                f'<circle cx="240" cy="240" r="2.5" fill="{a}"/>')
    if key == "hyperlocal":
        return (f'<line x1="226" y1="240" x2="254" y2="240" '
                f'stroke="{a}" stroke-width="2"/>'
                f'<line x1="240" y1="226" x2="240" y2="254" '
                f'stroke="{a}" stroke-width="2"/>'
                f'<circle cx="240" cy="240" r="3" fill="{a}"/>')
    if key == "seele":
        return f'<rect x="232" y="232" width="16" height="16" fill="{a}"/>'
    if key == "drpop":
        return (f'<circle cx="240" cy="240" r="9" fill="{a}"/>'
                f'<circle cx="240" cy="240" r="14" fill="none" '
                f'stroke="{a}" stroke-opacity=".55"/>')
    if key == "recon":
        return (f'<circle cx="240" cy="240" r="8" fill="none" '
                f'stroke="{a}" stroke-width="2"/>'
                f'<line x1="226" y1="240" x2="254" y2="240" stroke="{a}"/>'
                f'<line x1="240" y1="226" x2="240" y2="254" stroke="{a}"/>')
    return f'<circle cx="240" cy="240" r="6" fill="{a}"/>'

def _motif(key, pal):
    a = pal["accent"]; alt = pal["alt"]
    if key == "nerv":
        # corner brackets (4 corners) + faint outer hex hint
        b = ('M14,64 V14 H64 M416,14 H466 V64 M466,416 V466 H416 '
             'M64,466 H14 V416')
        return (f'<path d="{b}" fill="none" stroke="{a}" '
                f'stroke-opacity=".55" stroke-width="3"/>')
    if key == "aghud":
        # chevrons on the outer ring at the four cardinals
        out = []
        for ang in (0, 90, 180, 270):
            x, y = pos(ang, 220)
            x2, y2 = pos(ang, 232)
            dx = (x2 - x); dy = (y2 - y)
            # tangent (perpendicular to radial direction)
            tx, ty = -dy, dx
            mag = (tx * tx + ty * ty) ** 0.5
            tx, ty = tx / mag, ty / mag
            out.append(
                f'<path d="M{x + tx*9:.0f},{y + ty*9:.0f} '
                f'L{x2:.0f},{y2:.0f} '
                f'L{x - tx*9:.0f},{y - ty*9:.0f}" '
                f'stroke="{a}" stroke-width="2" fill="none" '
                f'stroke-opacity=".55"/>')
        return "".join(out)
    if key == "hyperlocal":
        ls = []
        for v in (60, 120, 180, 300, 360, 420):
            ls.append(f'<line x1="{v}" y1="0" x2="{v}" y2="480" '
                      f'stroke="{a}" stroke-opacity=".08"/>')
            ls.append(f'<line x1="0" y1="{v}" x2="480" y2="{v}" '
                      f'stroke="{a}" stroke-opacity=".08"/>')
        return "".join(ls)
    if key == "seele":
        return (f'<rect x="30" y="40" width="14" height="400" '
                f'fill="{a}" fill-opacity=".10"/>')
    if key == "drpop":
        bursts = []
        for k in range(12):
            ang = k * 30
            x1, y1 = pos(ang, 70)
            x2, y2 = pos(ang, 230)
            col = a if k % 2 == 0 else alt
            bursts.append(
                f'<line x1="{x1:.0f}" y1="{y1:.0f}" '
                f'x2="{x2:.0f}" y2="{y2:.0f}" '
                f'stroke="{col}" stroke-opacity=".18" '
                f'stroke-width="3"/>')
        return "".join(bursts)
    if key == "recon":
        return (f'<path d="M240,240 L240,30 A210,210 0 0 1 410,140 Z" '
                f'fill="{a}" fill-opacity=".14"/>')
    return ""

def _node(kind, x, y, pal):
    a = pal["accent"]; alt = pal["alt"]
    if kind == "known":
        return (f'<circle cx="{x:.0f}" cy="{y:.0f}" r="14" fill="none" '
                f'stroke="{a}" stroke-opacity=".55" stroke-width="1.4"/>'
                f'<circle cx="{x:.0f}" cy="{y:.0f}" r="5" fill="{a}"/>')
    if kind == "fav":
        return (f'<circle cx="{x:.0f}" cy="{y:.0f}" r="11" fill="none" '
                f'stroke="{alt}" stroke-width="2.2"/>'
                f'<circle cx="{x:.0f}" cy="{y:.0f}" r="5" fill="{a}"/>')
    if kind == "fresh":
        return f'<circle cx="{x:.0f}" cy="{y:.0f}" r="5" fill="{a}"/>'
    if kind == "neigh":
        return (f'<circle cx="{x:.0f}" cy="{y:.0f}" r="5" fill="{a}" '
                f'opacity=".85"/>')
    if kind == "stale":
        return (f'<circle cx="{x:.0f}" cy="{y:.0f}" r="4" fill="{a}" '
                f'opacity=".22"/>')
    return ""

def svg_for(key, pal):
    bg, bg2, a = pal["bg"], pal["bg2"], pal["accent"]
    ring = (f'<g stroke="{a}" stroke-opacity=".25" stroke-width="1" '
            f'fill="none">'
            f'<circle cx="240" cy="240" r="80"/>'
            f'<circle cx="240" cy="240" r="160"/>'
            f'<circle cx="240" cy="240" r="220"/></g>')
    cross = (f'<g stroke="{a}" stroke-opacity=".07" stroke-width="1">'
             f'<line x1="20" y1="240" x2="460" y2="240"/>'
             f'<line x1="240" y1="20" x2="240" y2="460"/></g>')
    ripple = (f'<circle cx="240" cy="240" r="{RIPPLE_R}" fill="none" '
              f'stroke="{a}" stroke-width="2" stroke-dasharray="6 8" '
              f'stroke-opacity=".5"/>')
    motif = _motif(key, pal)
    nodes = "".join(_node(k, *pos(ang, r), pal) for ang, r, k in NODES)
    sm = _self_marker(key, pal)
    return f'''<svg xmlns="http://www.w3.org/2000/svg" \
viewBox="0 0 480 480" style="display:block;width:100%;height:auto">
<defs><linearGradient id="bg{key}" x1="0" y1="0" x2="0" y2="1">
<stop offset="0" stop-color="{bg}"/>
<stop offset="1" stop-color="{bg2}"/></linearGradient></defs>
<rect width="480" height="480" fill="url(#bg{key})"/>
{motif}
{cross}
{ring}
{ripple}
{nodes}
{sm}
</svg>'''

def html_section():
    cards = []
    for key, label, pack, note in ORDER:
        s = svg_for(key, P[key])
        note_html = f' · {note}' if note else ''
        cards.append(f'''<div style="background:#0d111c;border:1px solid \
#1d2436;border-radius:12px;padding:12px">
<div style="display:flex;justify-content:space-between;\
align-items:baseline;margin-bottom:6px">
<div style="font-size:14px;letter-spacing:.04em;color:#dfe">{label}</div>
<div style="font-size:11px;color:#8a93a8">pack: {pack}{note_html}</div>
</div>
{s}
</div>''')
    legend = ('<div style="font-size:12px;color:#8a93a8;'
              'margin:8px 0 12px;line-height:1.55"><strong>Legend</strong> '
              '— centre = self · three concentric rings (near / mid / far) '
              '· dashed ring = transient channel-message ripple (mid '
              'progress) · small dot = fabric node · halo around dot = '
              '<em>known</em> (pulse, mid-phase) · alt-colour ring = '
              '<em>favourite</em> (rapid blink) · faded dot = stale '
              '(low brightness). Position is hybrid (GPS bearing+distance '
              'when available, else RSSI/SNR ring with a stable per-pubkey '
              'hash bearing). Same fleet across themes; only the visual '
              'identity changes.</div>')
    return f'''
<section class="concept" id="grid-mockup">
  <h2>Hyperlocal grid — per-theme mockup
    <span class="tag">A snapshot of the R18 / U9 grid rendered through
    each theme. Pairs with the textual Per-theme rendering plan
    above.</span></h2>
  <div class="cbody">
    {legend}
    <div style="display:grid;grid-template-columns:repeat(2,1fr);\
gap:14px;margin-top:8px">
      {''.join(cards)}
    </div>
  </div>
</section>
'''

HERE = os.path.dirname(os.path.abspath(__file__))
BRIEF_HTML = os.path.join(os.path.dirname(HERE),
                          "meshmore-sns-UX-brief.html")
BRIEF_MD = os.path.join(os.path.dirname(HERE),
                        "meshmore-sns-UX-brief.md")

def inject_html():
    html = open(BRIEF_HTML).read()
    # idempotent: drop a previously injected block.
    html = re.sub(
        r'\n<section class="concept" id="grid-mockup">.*?</section>\n',
        '\n', html, flags=re.S)
    # Insert right after the render-plan section closes.
    rp = re.search(
        r'<section class="concept" id="render-plan">.*?</section>\n',
        html, flags=re.S)
    if not rp:
        raise RuntimeError("render-plan section not found in brief")
    p = rp.end()
    html = html[:p] + html_section() + html[p:]
    open(BRIEF_HTML, "w").write(html)

def note_md():
    md = open(BRIEF_MD).read()
    note = ('\n> **Grid mockup:** per-theme SVG snapshots of the '
            'R18 / U9 hyperlocal grid are embedded in the **HTML** '
            'brief (`id="grid-mockup"`); the Markdown is kept text-only '
            'for diffing.\n')
    if note.strip() in md:
        return
    marker = '\n## Configuration & profile screens'
    if marker not in md:
        raise RuntimeError("could not find Configuration anchor in MD")
    md = md.replace(marker, note + marker, 1)
    open(BRIEF_MD, "w").write(md)

if __name__ == "__main__":
    inject_html()
    note_md()
    print("brief updated: per-theme grid mockups injected (HTML); "
          "MD got a pointer note")
