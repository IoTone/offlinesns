#!/usr/bin/env python3
"""Generate the 6 Meshmore SNS brand concepts (icon + splash) as SVG.

One coherent "mesh signal" mark — a central node, three linked
satellites, signal rings — restyled per design concept using the exact
in-app MmTokens palette so the launcher icon matches the chosen theme.
Shapes are kept to rect/circle/line/polygon/path/linearGradient so
ImageMagick can rasterise the icons (no filters/masks/blur/text in the
icon itself; the splash adds a wordmark for browser preview).
"""
import os

# concept: (bg, bg2, accent, fg, alt, motif-color)  — from mm_tokens.dart
C = {
    "seele":      dict(bg="#000000", bg2="#0E0E0C", accent="#EDE6D6",
                       fg="#EDE6D6", alt="#C8102E", line="#2A2A26"),
    "nerv":       dict(bg="#0A0E1A", bg2="#121826", accent="#FF7A00",
                       fg="#E6ECF5", alt="#9CFF00", line="#243049"),
    "aghud":      dict(bg="#05060B", bg2="#10131F", accent="#22D3EE",
                       fg="#DDF6FF", alt="#FF2D78", line="#20283B"),
    "hyperlocal": dict(bg="#0B0F17", bg2="#161B26", accent="#35E0F0",
                       fg="#DDE7EF", alt="#7CFF6B", line="#263041"),
    "drpop":      dict(bg="#101014", bg2="#1C1C24", accent="#FF2E88",
                       fg="#F2F0E6", alt="#D7FF00", line="#33333F"),
    "recon":      dict(bg="#000000", bg2="#0A0A07", accent="#FFB000",
                       fg="#FFB000", alt="#B3231F", line="#6E4E00"),
}
NAME = {
    "seele": "SEELE Monolith", "nerv": "NERV Terminal",
    "aghud": "AG-HUD", "hyperlocal": "Hyperlocal Field",
    "drpop": "DR Pop", "recon": "Tactical Recon",
}

# 3 satellites around centre (256,256)
SAT = [(256, 96), (114, 338), (398, 338)]


def mark(p, scale=1.0, cx=256, cy=256):
    """The shared mesh mark, themed by palette p."""
    a, fg, alt, ln = p["accent"], p["fg"], p["alt"], p["line"]
    s = []
    # signal rings
    for r, o in ((168, .22), (124, .35)):
        s.append(f'<circle cx="{cx}" cy="{cy}" r="{r}" fill="none" '
                 f'stroke="{a}" stroke-opacity="{o}" stroke-width="3"/>')
    # links
    for (x, y) in SAT:
        x = cx + (x - 256); y = cy + (y - 256)
        s.append(f'<line x1="{cx}" y1="{cy}" x2="{x}" y2="{y}" '
                 f'stroke="{a}" stroke-opacity=".55" stroke-width="6"/>')
    # satellites
    for (x, y) in SAT:
        x = cx + (x - 256); y = cy + (y - 256)
        s.append(f'<circle cx="{x}" cy="{y}" r="22" fill="{p["bg"]}" '
                 f'stroke="{a}" stroke-width="6"/>')
    # central node — a diamond
    d = 78
    s.append(f'<polygon points="{cx},{cy-d} {cx+d},{cy} {cx},{cy+d} '
             f'{cx-d},{cy}" fill="{a}"/>')
    s.append(f'<polygon points="{cx},{cy-40} {cx+40},{cy} {cx},{cy+40} '
             f'{cx-40},{cy}" fill="{p["bg"]}"/>')
    s.append(f'<circle cx="{cx}" cy="{cy}" r="15" fill="{alt}"/>')
    return "\n".join(s)


def motif(name, p):
    a, fg, alt, ln, bg = (p["accent"], p["fg"], p["alt"], p["line"],
                          p["bg"])
    if name == "seele":   # austere monolith bar + red mote
        return (f'<rect x="243" y="60" width="26" height="392" '
                f'fill="{fg}" fill-opacity=".10"/>'
                f'<rect x="40" y="40" width="432" height="432" '
                f'fill="none" stroke="{fg}" stroke-opacity=".35" '
                f'stroke-width="3"/>')
    if name == "nerv":    # hexagon frame + corner brackets
        hx = "256,36 446,146 446,366 256,476 66,366 66,146"
        b = ('M44,120 V44 H120 M392,44 H468 V120 M468,392 V468 H392 '
             'M120,468 H44 V392')
        return (f'<polygon points="{hx}" fill="none" stroke="{a}" '
                f'stroke-opacity=".5" stroke-width="4"/>'
                f'<path d="{b}" fill="none" stroke="{alt}" '
                f'stroke-width="6"/>')
    if name == "aghud":   # speed chevrons
        ch = []
        for i, o in ((0, .55), (34, .35), (68, .18)):
            ch.append(f'<path d="M{120+i},150 L256,80 L{392-i},150" '
                      f'fill="none" stroke="{a}" stroke-opacity="{o}" '
                      f'stroke-width="10" stroke-linecap="round"/>')
            ch.append(f'<path d="M{120+i},362 L256,432 L{392-i},362" '
                      f'fill="none" stroke="{a}" stroke-opacity="{o}" '
                      f'stroke-width="10" stroke-linecap="round"/>')
        return "".join(ch)
    if name == "hyperlocal":  # map grid + outer range ring
        g = []
        for v in (96, 176, 336, 416):
            g.append(f'<line x1="{v}" y1="40" x2="{v}" y2="472" '
                     f'stroke="{ln}" stroke-width="2"/>')
            g.append(f'<line x1="40" y1="{v}" x2="472" y2="{v}" '
                     f'stroke="{ln}" stroke-width="2"/>')
        g.append(f'<circle cx="256" cy="256" r="210" fill="none" '
                 f'stroke="{alt}" stroke-opacity=".5" '
                 f'stroke-width="3" stroke-dasharray="10 12"/>')
        return "".join(g)
    if name == "drpop":   # halftone burst
        import math
        r = []
        for k in range(12):
            ang = math.radians(k * 30)
            x1 = 256 + 150 * math.cos(ang); y1 = 256 + 150 * math.sin(ang)
            x2 = 256 + 232 * math.cos(ang); y2 = 256 + 232 * math.sin(ang)
            col = a if k % 2 == 0 else alt
            r.append(f'<line x1="{x1:.0f}" y1="{y1:.0f}" x2="{x2:.0f}" '
                     f'y2="{y2:.0f}" stroke="{col}" stroke-width="14" '
                     f'stroke-linecap="round"/>')
        return "".join(r)
    if name == "recon":   # radar sweep wedge + reticle ticks
        t = [f'<path d="M256,256 L256,40 A216,216 0 0 1 432,150 Z" '
             f'fill="{a}" fill-opacity=".12"/>']
        for ang in (0, 90, 180, 270):
            import math
            rad = math.radians(ang)
            x1 = 256 + 196 * math.cos(rad); y1 = 256 + 196 * math.sin(rad)
            x2 = 256 + 224 * math.cos(rad); y2 = 256 + 224 * math.sin(rad)
            t.append(f'<line x1="{x1:.0f}" y1="{y1:.0f}" x2="{x2:.0f}" '
                     f'y2="{y2:.0f}" stroke="{a}" stroke-width="6"/>')
        t.append(f'<circle cx="256" cy="256" r="224" fill="none" '
                 f'stroke="{ln}" stroke-width="4"/>')
        return "".join(t)
    return ""


def icon_svg(key):
    p = C[key]
    return f'''<svg xmlns="http://www.w3.org/2000/svg" width="512" \
height="512" viewBox="0 0 512 512">
<defs><linearGradient id="bg{key}" x1="0" y1="0" x2="0" y2="1">
<stop offset="0" stop-color="{p['bg2']}"/>
<stop offset="1" stop-color="{p['bg']}"/></linearGradient></defs>
<rect width="512" height="512" fill="url(#bg{key})"/>
{motif(key, p)}
{mark(p)}
</svg>
'''


def splash_svg(key):
    p = C[key]
    a, fg = p["accent"], p["fg"]
    nm = NAME[key].upper()
    return f'''<svg xmlns="http://www.w3.org/2000/svg" width="1080" \
height="2280" viewBox="0 0 1080 2280">
<defs><linearGradient id="s{key}" x1="0" y1="0" x2="0" y2="1">
<stop offset="0" stop-color="{p['bg2']}"/>
<stop offset="1" stop-color="{p['bg']}"/></linearGradient></defs>
<rect width="1080" height="2280" fill="url(#s{key})"/>
<g transform="translate(284 760) scale(1.0)">
{motif(key, p)}
{mark(p)}
</g>
<text x="540" y="1500" text-anchor="middle" fill="{fg}" \
font-family="Menlo,Consolas,monospace" font-size="78" \
letter-spacing="14">MESHMORE</text>
<text x="540" y="1576" text-anchor="middle" fill="{a}" \
font-family="Menlo,Consolas,monospace" font-size="40" \
letter-spacing="26">S N S</text>
<text x="540" y="2160" text-anchor="middle" fill="{fg}" \
fill-opacity="0.5" font-family="Menlo,Consolas,monospace" \
font-size="30" letter-spacing="8">{nm}</text>
</svg>
'''


here = os.path.dirname(os.path.abspath(__file__))
for key in C:
    d = os.path.join(here, key)
    with open(os.path.join(d, "icon.svg"), "w") as f:
        f.write(icon_svg(key))
    with open(os.path.join(d, "splash.svg"), "w") as f:
        f.write(splash_svg(key))
print("wrote", ", ".join(C))
