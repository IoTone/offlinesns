#!/usr/bin/env python3
"""Inject a static 'App icon & splash — pick one' gallery (6 inline
SVGs) into the UX brief, just before the concept grid."""
import os, re

here = os.path.dirname(os.path.abspath(__file__))
brief = os.path.join(os.path.dirname(here), "meshmore-sns-UX-brief.html")

ORDER = [("drpop", "E · DR Pop", "bold pop / halftone — shipped logo"),
         ("seele", "D · SEELE Monolith", "default in-app theme"),
         ("nerv", "A · NERV Terminal", "Evangelion ops console"),
         ("aghud", "B · AG-HUD", "Wipeout racing HUD"),
         ("hyperlocal", "C · Hyperlocal Field", "map / range rings"),
         ("recon", "F · Tactical Recon", "amber phosphor radar")]
SHIPPED = "drpop"

cards = []
for key, title, sub in ORDER:
    icon = open(os.path.join(here, key, "icon.svg")).read().strip()
    splash = open(os.path.join(here, key, "splash.svg")).read().strip()
    # size the inline SVGs for preview
    icon = icon.replace('width="512" height="512"',
                        'width="128" height="128"', 1)
    splash = splash.replace('width="1080" height="2280"',
                            'width="104" height="220"', 1)
    star = " ★" if key == SHIPPED else ""
    cards.append(f'''<div style="background:#0d111c;border:1px solid \
#1d2436;border-radius:14px;padding:14px;display:flex;gap:14px;\
align-items:center">
<div style="border-radius:24px;overflow:hidden;flex:0 0 auto;\
box-shadow:0 4px 16px #0008">{icon}</div>
<div style="flex:0 0 auto;border:1px solid #1d2436;border-radius:8px;\
overflow:hidden">{splash}</div>
<div style="min-width:120px">
<div style="font-size:15px;letter-spacing:.04em;color:#dfe">\
{title}{star}</div>
<div style="font-size:12px;color:#8a93a8;margin-top:4px">{sub}</div>
<div style="font-size:11px;color:#5f6b82;margin-top:8px">icon · splash</div>
</div></div>''')

section = f'''
<section class="concept" id="brand">
  <h2>App Icon &amp; Splash — pick one
    <span class="tag">6 concepts, same "mesh signal" mark restyled
    per theme palette (matches the in-app preset)</span></h2>
  <div class="cbody">
    <div class="note">E · DR Pop is the shipped logo (★), wired as the
    Android adaptive icon + splash. The in-app default <em>theme</em>
    remains D · SEELE. To adopt another, run
    <code>brand/apply.sh &lt;concept&gt;</code> (seele|nerv|aghud|
    hyperlocal|drpop|recon).</div>
    <div style="display:grid;grid-template-columns:repeat(2,1fr);\
gap:14px;margin-top:12px">
      {''.join(cards)}
    </div>
  </div>
</section>
'''

html = open(brief).read()
# idempotent: drop a previously injected block
html = re.sub(r'\n<section class="concept" id="brand">.*?</section>\n',
              '\n', html, flags=re.S)
marker = '<div class="grid">'
assert marker in html, "grid marker not found"
html = html.replace(marker, section + "\n" + marker, 1)
open(brief, "w").write(html)
print("injected brand gallery (%d bytes)" % len(html))
