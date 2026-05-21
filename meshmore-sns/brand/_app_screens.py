#!/usr/bin/env python3
"""Generate per-screen SVG mockups of the app at this development
stage — used to illustrate the README. Honest mockups (not real
device screenshots) drawn in the SEELE Monolith default theme.

Outputs land in `meshmore-sns/screens/*.svg`; the README references
them as relative `screens/<name>.svg` paths.
"""
import os
import math

HERE = os.path.dirname(os.path.abspath(__file__))
OUT = os.path.join(os.path.dirname(HERE), "screens")
os.makedirs(OUT, exist_ok=True)

# SEELE Monolith default palette (mirrors mm_tokens.dart).
BG = "#0E0E0C"
BG_DEEP = "#000000"
FG = "#EDE6D6"
DIM = "#8a847a"
RING = "#3b3833"
ALT = "#C8102E"
PRIMARY = "#EDE6D6"
SURF_HIGH = "#1a1815"

W, H = 360, 720  # phone-ish 1:2 canvas


def shell(inner: str, title: str = "Meshmore SNS") -> str:
    """Wrap inner SVG content in a phone frame + status bar + AppBar."""
    return f'''<svg xmlns="http://www.w3.org/2000/svg" \
viewBox="0 0 {W} {H}" width="100%" style="max-width:380px;\
height:auto;display:block;background:{BG_DEEP};border-radius:18px;\
box-shadow:0 0 0 1px #2a2a26 inset">
<!-- Backdrop -->
<defs>
  <linearGradient id="bg" x1="0" y1="0" x2="0" y2="1">
    <stop offset="0" stop-color="{BG}"/>
    <stop offset="1" stop-color="{BG_DEEP}"/>
  </linearGradient>
</defs>
<rect width="{W}" height="{H}" fill="url(#bg)" rx="18"/>
<!-- Status bar -->
<text x="20" y="22" fill="{DIM}" font-size="10"
      font-family="Menlo,monospace">9:41</text>
<text x="{W-20}" y="22" text-anchor="end" fill="{DIM}"
      font-size="10" font-family="Menlo,monospace">▲ ▲ ░░</text>
<!-- AppBar -->
<rect x="0" y="32" width="{W}" height="44" fill="{BG}"/>
<text x="16" y="60" fill="{FG}" font-size="16"
      font-family="Inter,Helvetica,sans-serif" font-weight="500">{title}</text>
{inner}
</svg>'''


def text(x, y, t, fill=FG, size=12, family="Inter,Helvetica,sans-serif",
         weight="400", letter_spacing=None, anchor=None):
    parts = [f'x="{x}"', f'y="{y}"', f'fill="{fill}"',
             f'font-size="{size}"', f'font-family="{family}"',
             f'font-weight="{weight}"']
    if letter_spacing is not None:
        parts.append(f'letter-spacing="{letter_spacing}"')
    if anchor:
        parts.append(f'text-anchor="{anchor}"')
    return f'<text {" ".join(parts)}>{t}</text>'


def chip(x, y, text_, fill=PRIMARY, w=80, h=20):
    return (f'<g><rect x="{x}" y="{y}" width="{w}" height="{h}" rx="10" '
            f'fill="none" stroke="{fill}" stroke-opacity=".45"/>'
            f'<text x="{x + w / 2}" y="{y + 14}" fill="{fill}" '
            f'font-size="10" font-family="Menlo,monospace" '
            f'text-anchor="middle" letter-spacing="1">{text_}</text></g>')


# ---------------------------------------------------------------- Dashboard
def dashboard():
    inner = []
    # PEERS IN RANGE label
    inner.append(text(20, 110, "PEERS IN RANGE", fill=DIM, size=10,
                      letter_spacing=4))
    # Big numeral
    inner.append(text(20, 180, "7", fill=FG, size=72, weight="300"))
    inner.append(text(20, 202, "24 known", fill=DIM, size=11))
    # Status slab
    inner.append(f'<rect x="0" y="220" width="{W}" height="40" '
                 f'fill="{SURF_HIGH}"/>')
    inner.append(f'<circle cx="20" cy="240" r="5" fill="{FG}"/>')
    inner.append(text(36, 244, "▌ LINKED · NO ALERTS", fill=FG,
                      size=11, weight="600", letter_spacing=2))
    # RADIO
    inner.append(text(20, 290, "RADIO", fill=DIM, size=10,
                      letter_spacing=4))
    inner.append(text(20, 310, "T1000-E", fill=FG, size=12,
                      family="Menlo,monospace"))
    inner.append(text(20, 326, "920.0 MHz  SF11  CR5  20 dBm",
                      fill=FG, size=12, family="Menlo,monospace"))
    # LOCATION tile
    inner.append(text(20, 366, "LOCATION", fill=DIM, size=10,
                      letter_spacing=4))
    inner.append(text(20, 386, "35.68130, 139.76710", fill=FG,
                      size=12, family="Menlo,monospace"))
    inner.append(text(20, 402, "source · device", fill=DIM, size=11,
                      family="Menlo,monospace"))
    # BATTERY
    inner.append(text(20, 440, "BATTERY", fill=DIM, size=10,
                      letter_spacing=4))
    inner.append(text(20, 460, "4.10V · ~92%  ⚡ CHARGING",
                      fill="#7CFF6B", size=12,
                      family="Menlo,monospace"))
    # RECENT
    inner.append(text(20, 500, "RECENT", fill=DIM, size=10,
                      letter_spacing=4))
    for i, (t, e) in enumerate([
        ("21:44:08", "advert · Yaron · -71 dBm"),
        ("21:43:51", "channel-msg · Public · ack on the way"),
        ("21:43:12", "advert · NodeX · -85 dBm"),
        ("21:42:30", "self-info · meshmore"),
        ("21:41:54", "battery · 4.10V · 92 %"),
    ]):
        y = 522 + i * 18
        inner.append(text(20, y, t, fill=DIM, size=10,
                          family="Menlo,monospace"))
        inner.append(text(74, y, e, fill=FG, size=11))
    # Tab bar
    inner.append(_tab_bar(active="Dashboard"))
    return shell("\n".join(inner), title="Meshmore SNS")


def _tab_bar(active="Dashboard"):
    tabs = ["Dashboard", "Chat", "Nodes", "Settings"]
    bar = [f'<rect x="0" y="{H - 56}" width="{W}" height="56" '
           f'fill="{BG}" stroke="{RING}" stroke-opacity=".6" '
           f'stroke-width="1"/>']
    col_w = W / len(tabs)
    icons = {"Dashboard": "⬜", "Chat": "💬", "Nodes": "📡",
             "Settings": "⚙"}
    for i, label in enumerate(tabs):
        cx = col_w * i + col_w / 2
        col = FG if label == active else DIM
        bar.append(f'<text x="{cx}" y="{H - 28}" fill="{col}" '
                   f'font-size="14" text-anchor="middle">{icons[label]}</text>')
        bar.append(f'<text x="{cx}" y="{H - 10}" fill="{col}" '
                   f'font-size="9" text-anchor="middle" '
                   f'letter-spacing="1">{label}</text>')
    return "\n".join(bar)


# ---------------------------------------------------------------- Nodes
def nodes():
    inner = []
    inner.append(f'<g transform="translate({W - 110},40)">')
    inner.append('<text x="0" y="22" fill="' + DIM + '" font-size="11">scan</text>')
    inner.append('<text x="40" y="22" fill="' + DIM + '" font-size="11">advert</text>')
    inner.append('<text x="90" y="22" fill="' + FG + '" font-size="14">⌖</text>')
    inner.append('</g>')
    inner.append(text(20, 100, "DISCOVERED NODES", fill=DIM, size=10,
                      letter_spacing=4))
    inner.append(text(20, 120, "8 in fabric · 4 known · 2 contacts",
                      fill=DIM, size=11))
    rows = [
        ("📡", "Yaron",     "advert · -65 dBm SNR 8 · ≈ 420 m · 35.6818,139.7672 · be4f…", "1m", "★", True),
        ("📡", "Hub-NW",    "advert · -71 dBm SNR 6 · ≈ 1.2 km · 35.6905,139.7654 · 92a3…", "3m", "★", True),
        ("📻", "NodeX",     "advert · -85 dBm · ≈ 3.4 km · 5e90…", "12m", "☆", False),
        ("📻", "Roam-3",    "advert · -88 dBm · 7c11…", "26m", "☆", False),
        ("👤", "Mira",      "companion · ≈ 180 m · 8801…", "1h", "☆", False),
        ("📻", "Repeat-1",  "advert · -92 dBm SNR -2 · a02f…", "2h", "☆", False),
        ("📻", "Edge",      "advert · -95 dBm · cc8b…", "5h", "☆", False),
        ("👤", "Drone-R",   "companion · paired earlier · ec00…", "1d", "☆", False),
    ]
    y0 = 150
    for i, (icon, name, sub, ago, star, fav) in enumerate(rows):
        y = y0 + i * 56
        # Row background subtle on fav
        if fav:
            inner.append(f'<rect x="0" y="{y - 14}" width="{W}" '
                         f'height="48" fill="{SURF_HIGH}" '
                         f'fill-opacity=".4"/>')
        inner.append(text(20, y + 6, icon, size=14))
        inner.append(text(44, y + 6, name, fill=FG, size=13,
                          weight="500"))
        if name == "Yaron":
            inner.append(chip(110, y - 6, "IN RANGE", w=64))
        inner.append(text(44, y + 24, sub, fill=DIM, size=10,
                          family="Menlo,monospace"))
        inner.append(text(W - 60, y + 6, ago, fill=DIM, size=11,
                          family="Menlo,monospace"))
        col = "#FFD56B" if fav else DIM
        inner.append(text(W - 24, y + 6, star, fill=col, size=14))
        # Divider
        inner.append(f'<line x1="0" y1="{y + 34}" x2="{W}" '
                     f'y2="{y + 34}" stroke="{RING}" '
                     f'stroke-opacity=".5"/>')
    inner.append(_tab_bar(active="Nodes"))
    return shell("\n".join(inner), title="Nodes  ⊕  ⏵  ▦")


# ---------------------------------------------------------------- Grid
def grid_screen():
    inner = []
    # App bar actions
    inner.append(text(W - 76, 60, "⏸", fill=FG, size=14))
    inner.append(text(W - 54, 60, "15s ▼", fill=DIM, size=11))
    inner.append(text(W - 20, 60, "ⓘ", fill=FG, size=14))
    # Status line
    inner.append(text(16, 92, "8 in fabric · 4 known · 2 contacts · "
                      "paused", fill=DIM, size=11))
    # Range slider (visual)
    inner.append(text(16, 116, "Range", fill=DIM, size=10,
                      letter_spacing=1))
    slider_y = 130
    inner.append(f'<line x1="60" y1="{slider_y}" x2="{W - 100}" '
                 f'y2="{slider_y}" stroke="{RING}" '
                 f'stroke-width="2"/>')
    # 6 stops; thumb at "Wide" (right-most)
    stops = 6
    sx0, sx1 = 60, W - 100
    for i in range(stops):
        x = sx0 + i * (sx1 - sx0) / (stops - 1)
        inner.append(f'<circle cx="{x:.0f}" cy="{slider_y}" r="3" '
                     f'fill="{DIM}"/>')
    thumb_x = sx1
    inner.append(f'<circle cx="{thumb_x}" cy="{slider_y}" r="7" '
                 f'fill="{FG}"/>')
    inner.append(text(W - 92, slider_y + 4, "Wide · 5 km",
                      fill=FG, size=10, anchor="start"))
    # Radar canvas
    cx, cy = W // 2, 380
    R = 130
    for f in (1 / 3, 2 / 3, 1.0):
        inner.append(f'<circle cx="{cx}" cy="{cy}" r="{R * f:.0f}" '
                     f'fill="none" stroke="{RING}" '
                     f'stroke-opacity=".6"/>')
    # cross-hair
    inner.append(f'<line x1="{cx - R}" y1="{cy}" x2="{cx + R}" '
                 f'y2="{cy}" stroke="{RING}" stroke-opacity=".25"/>')
    inner.append(f'<line x1="{cx}" y1="{cy - R}" x2="{cx}" '
                 f'y2="{cy + R}" stroke="{RING}" '
                 f'stroke-opacity=".25"/>')
    # self marker (cream square)
    inner.append(f'<rect x="{cx - 7}" y="{cy - 7}" width="14" '
                 f'height="14" fill="{FG}"/>')
    # ripple (dashed mid)
    inner.append(f'<circle cx="{cx}" cy="{cy}" r="55" fill="none" '
                 f'stroke="{FG}" stroke-width="1.4" '
                 f'stroke-dasharray="5 6" stroke-opacity=".4"/>')
    # Nodes
    nodes_pp = [
        (45, 0.65, "fresh"),     # NE plain
        (200, 0.85, "fav"),      # SW favourite
        (300, 1.0, "fresh"),     # NW outer
        (350, 0.35, "known"),    # near N
        (130, 1.0, "stale"),     # SE outer
        (90, 0.45, "fresh"),     # E inner
    ]
    for ang_deg, radius_frac, kind in nodes_pp:
        a = math.radians(ang_deg)
        nx = cx + radius_frac * R * math.sin(a)
        ny = cy - radius_frac * R * math.cos(a)
        if kind == "known":
            inner.append(f'<circle cx="{nx:.0f}" cy="{ny:.0f}" '
                         f'r="13" fill="none" stroke="{FG}" '
                         f'stroke-opacity=".55" stroke-width="1.4"/>')
            inner.append(f'<circle cx="{nx:.0f}" cy="{ny:.0f}" '
                         f'r="5" fill="{FG}"/>')
        elif kind == "fav":
            inner.append(f'<circle cx="{nx:.0f}" cy="{ny:.0f}" '
                         f'r="11" fill="none" stroke="{ALT}" '
                         f'stroke-width="2"/>')
            inner.append(f'<circle cx="{nx:.0f}" cy="{ny:.0f}" '
                         f'r="5" fill="{FG}"/>')
        elif kind == "fresh":
            inner.append(f'<circle cx="{nx:.0f}" cy="{ny:.0f}" '
                         f'r="5" fill="{FG}"/>')
        else:  # stale
            inner.append(f'<circle cx="{nx:.0f}" cy="{ny:.0f}" '
                         f'r="4" fill="{FG}" opacity=".22"/>')
    # Legend
    inner.append(f'<rect x="14" y="540" width="{W - 28}" height="64" '
                 f'rx="6" fill="{SURF_HIGH}" '
                 f'stroke="{RING}" stroke-opacity=".5"/>')
    inner.append(text(24, 558, "LEGEND", fill=DIM, size=10,
                      letter_spacing=3))
    inner.append(text(24, 574, "○ rings · GPS bands → ~5 km outer",
                      fill=FG, size=10))
    inner.append(text(24, 588, "● fabric · ⊙ known (pulse) · "
                      "◉ fav (blink) · ↺ ripple = channel-msg",
                      fill=FG, size=10))
    # Footer
    inner.append(text(16, 622, "Outer ring ≈ wide (5 km) · "
                      "tap a node for details", fill=DIM, size=10))
    inner.append(_tab_bar(active="Nodes"))
    return shell("\n".join(inner), title="Hyperlocal grid")


# ---------------------------------------------------------------- Chat
def chat():
    inner = []
    # Strip header
    inner.append(text(16, 92, "CHANNEL · PUBLIC", fill=DIM, size=11,
                      letter_spacing=3))
    inner.append(text(W - 60, 60, "#  ◐  ⧊", fill=FG, size=14))
    # Chip strip
    chips = [("Public", True), ("Ops", False), ("Field", False),
             ("XR Test", False)]
    x = 16
    y = 108
    for label, sel in chips:
        w = 56 + len(label) * 4
        if sel:
            inner.append(f'<rect x="{x}" y="{y}" width="{w}" '
                         f'height="24" rx="12" fill="{FG}"/>')
            inner.append(text(x + w // 2, y + 16, label, fill=BG,
                              size=11, anchor="middle"))
        else:
            inner.append(f'<rect x="{x}" y="{y}" width="{w}" '
                         f'height="24" rx="12" fill="none" '
                         f'stroke="{DIM}"/>')
            inner.append(text(x + w // 2, y + 16, label, fill=DIM,
                              size=11, anchor="middle"))
        x += w + 8
    inner.append(f'<line x1="0" y1="142" x2="{W}" y2="142" '
                 f'stroke="{RING}" stroke-opacity=".6"/>')
    # Messages
    msgs = [
        ("21:40 «", "Yaron",    "anyone copy on Ops?"),
        ("21:41 «", "Hub-NW",   "5x5 from the hill"),
        ("21:42 »", "me",       "rolling out to the site, ETA 12 min"),
        ("21:43 «", "Yaron",    "ack — meet at the gate"),
        ("21:44 «", "Hub-NW",   "weather looks fine; light wind"),
    ]
    y = 168
    for ts, who, txt in msgs:
        col = FG if "»" in ts else FG
        sub = "#9CFF00" if "»" in ts else DIM
        inner.append(text(16, y, ts, fill=sub, size=10,
                          family="Menlo,monospace"))
        inner.append(text(72, y, txt, fill=col, size=12))
        y += 30
    # Composer
    inner.append(f'<rect x="12" y="{H - 116}" width="{W - 64}" '
                 f'height="40" rx="6" fill="none" '
                 f'stroke="{DIM}"/>')
    inner.append(text(24, H - 92, "Message Public",
                      fill=DIM, size=12))
    inner.append(text(W - 32, H - 90, "↗", fill=FG, size=18))
    inner.append(_tab_bar(active="Chat"))
    return shell("\n".join(inner), title="Chat")


# ---------------------------------------------------------------- Device config
def device_config():
    inner = []
    inner.append(text(16, 100, "IDENTITY / ADVERT", fill=FG, size=11,
                      letter_spacing=3))
    # Advert name field
    inner.append(text(16, 130, "Advert name", fill=DIM, size=10))
    inner.append(f'<rect x="16" y="138" width="{W - 32}" height="32" '
                 f'rx="4" fill="none" stroke="{DIM}"/>')
    inner.append(text(28, 160, "meshmore", fill=FG, size=12,
                      family="Menlo,monospace"))
    inner.append(text(W - 76, 188, "Set name", fill=FG, size=11))
    # Advert location source segmented button
    inner.append(text(16, 220, "Advert location source",
                      fill=DIM, size=10))
    segs = [("None", False), ("Pinned", False), ("Device GPS", True)]
    x = 16
    w_each = (W - 32) // 3
    for label, sel in segs:
        fill = FG if sel else "none"
        txt_col = BG if sel else FG
        inner.append(f'<rect x="{x}" y="{230}" width="{w_each}" '
                     f'height="32" fill="{fill}" stroke="{DIM}"/>')
        inner.append(text(x + w_each // 2, 250, label,
                          fill=txt_col, size=11, anchor="middle"))
        x += w_each
    inner.append(text(16, 280, "GPS = use device on-board fix "
                      "(T1000-E etc.).", fill=DIM, size=10))
    # Lat / lon fields
    inner.append(text(16, 312, "Advert latitude (°)", fill=DIM,
                      size=10))
    inner.append(f'<rect x="16" y="320" width="{W - 32}" height="32" '
                 f'rx="4" fill="none" stroke="{DIM}"/>')
    inner.append(text(28, 342, "35.681300", fill=FG, size=12,
                      family="Menlo,monospace"))
    inner.append(text(16, 376, "Advert longitude (°)", fill=DIM,
                      size=10))
    inner.append(f'<rect x="16" y="384" width="{W - 32}" height="32" '
                 f'rx="4" fill="none" stroke="{DIM}"/>')
    inner.append(text(28, 406, "139.767100", fill=FG, size=12,
                      family="Menlo,monospace"))
    # Use phone / Read device buttons
    half = (W - 40) // 2
    inner.append(f'<rect x="16" y="430" width="{half}" height="32" '
                 f'rx="4" fill="none" stroke="{FG}"/>')
    inner.append(text(16 + half // 2, 451, "📱 Use phone location",
                      fill=FG, size=10, anchor="middle"))
    inner.append(f'<rect x="{16 + half + 8}" y="430" width="{half}" '
                 f'height="32" rx="4" fill="none" '
                 f'stroke="{FG}"/>')
    inner.append(text(16 + half + 8 + half // 2, 451,
                      "📍 Read device location", fill=FG, size=10,
                      anchor="middle"))
    inner.append(text(W - 16, 488, "Set advert location",
                      fill=FG, size=11, anchor="end"))
    inner.append(f'<line x1="0" y1="510" x2="{W}" y2="510" '
                 f'stroke="{RING}" stroke-opacity=".6"/>')
    inner.append(text(16, 530, "DEVICE", fill=FG, size=11,
                      letter_spacing=3))
    inner.append(text(16, 552, "T1000-E\n", fill=FG, size=12,
                      family="Menlo,monospace"))
    inner.append(text(16, 566, "max contacts: 350",
                      fill=DIM, size=11, family="Menlo,monospace"))
    inner.append(text(16, 580, "advert loc policy: 2",
                      fill=DIM, size=11, family="Menlo,monospace"))
    inner.append(_tab_bar(active="Settings"))
    return shell("\n".join(inner), title="Device config")


# ---------------------------------------------------------------- First-run
def first_run():
    inner = []
    inner.append(text(20, 96, "MESHMORE · WELCOME", fill=DIM,
                      size=10, letter_spacing=3))
    inner.append(text(20, 128, "Quick heads-up on what we'll "
                      "ask for", fill=FG, size=18, weight="500"))
    blocks = [
        ("🔵", "Bluetooth",
         "To pair with your MeshCore radio and exchange messages "
         "over the local mesh."),
        ("🔔", "Notifications",
         'Asked only if you turn on "Stay connected in background" '
         "later in App settings."),
        ("☁", "Offline is fine",
         "If you skip Bluetooth, the app still works — browse "
         "history, configure channels, read diagnostics."),
    ]
    y = 170
    for emoji, title, body in blocks:
        inner.append(f'<rect x="20" y="{y}" width="40" height="40" '
                     f'rx="6" fill="{SURF_HIGH}"/>')
        inner.append(text(40, y + 27, emoji, fill=FG, size=18,
                          anchor="middle"))
        inner.append(text(72, y + 16, title, fill=FG, size=13,
                          weight="500"))
        # body — wrap manually
        for i, line in enumerate(_wrap(body, 38)):
            inner.append(text(72, y + 34 + i * 14, line, fill=DIM,
                              size=10))
        y += 90
    # CTAs
    inner.append(f'<rect x="20" y="{H - 196}" width="{W - 40}" '
                 f'height="40" rx="6" fill="{FG}"/>')
    inner.append(text(W // 2, H - 170,
                      "✓  Grant Bluetooth & continue",
                      fill=BG, size=12, anchor="middle"))
    inner.append(f'<rect x="20" y="{H - 144}" width="{W - 40}" '
                 f'height="40" rx="6" fill="none" '
                 f'stroke="{FG}"/>')
    inner.append(text(W // 2, H - 118,
                      "☁  Continue offline (skip permissions)",
                      fill=FG, size=12, anchor="middle"))
    return shell("\n".join(inner), title="Meshmore SNS")


def _wrap(text_, width):
    out = []
    line = []
    n = 0
    for w in text_.split():
        if n + len(w) + 1 > width:
            out.append(" ".join(line))
            line = [w]
            n = len(w)
        else:
            line.append(w)
            n += len(w) + 1
    if line:
        out.append(" ".join(line))
    return out


SCREENS = {
    "dashboard.svg":     dashboard,
    "nodes.svg":         nodes,
    "grid.svg":          grid_screen,
    "chat.svg":          chat,
    "device-config.svg": device_config,
    "first-run.svg":     first_run,
}


def main():
    for fn, gen in SCREENS.items():
        path = os.path.join(OUT, fn)
        with open(path, "w", encoding="utf-8") as f:
            f.write(gen())
        print(f"  wrote {os.path.relpath(path)}")


if __name__ == "__main__":
    main()
