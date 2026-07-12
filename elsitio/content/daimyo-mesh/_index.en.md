+++
outputs = ["Reveal"]
title = "DaimyoMesh — A Resilient Public Mesh Network for Fukuoka"
+++

{{< slide transition="zoom" transition-speed="fast" >}}

# DaimyoMesh

## A Resilient Public Mesh Network for Fukuoka

Proposal v3 — MeshCore edition

David J. Kordsmeier · IoTone Japan

---

<section data-state="scrollable">

## The problem, in 30 seconds

**1. When disaster strikes, the phone network is the first thing to fail.**
In the 2024 Noto Peninsula earthquake, mobile base stations went dark across wide areas for days — exactly when residents, volunteers, and city staff needed to coordinate most. Fukuoka faces typhoons, storm surge, and earthquake risk with the same dependency: one cellular system, no fallback.

**2. The city needs eyes, but sensors are expensive to connect.**
River levels, drainage, heat, air quality, slopes — every cellular-connected sensor carries a SIM contract and monthly fees, forever. That cost ceiling decides how much of the city we can actually see.

**3. Fukuoka has Japan's best startup ecosystem — and no public network to build on.**
A National Strategic Special Zone for startups, Engineer Café, universities, maker spaces — but no shared, free, city-scale IoT layer where a student or founder can put a device on the air today.

</section>

---

## The proposal, in one sentence

> Build a **free, public, battery-and-solar mesh network** across central Fukuoka that keeps working when everything else is down — for less than the cost of installing a single traffic signal.

DaimyoMesh is that network: small solar radios on rooftops relaying for each other, no SIM cards, no monthly fees, no dependence on the power grid or cell towers.

---

<section data-state="scrollable">

## What is a mesh network? (no jargon)

- Each **node** is a radio about the size of a bar of soap. It runs on a small battery, often with a palm-sized solar panel. Hardware cost: **¥5,000–¥15,000 per node**.
- Nodes **relay messages for each other**. A message hops node-to-node until it arrives — like a bucket brigade. There is no tower and no center to fail.
- Each hop covers **hundreds of meters to several kilometers**, even in dense city blocks; rooftop nodes can span the bay.
- The network **gets stronger as it grows**: every added node is new coverage and a new path around damage.
- It carries **short messages and sensor readings**, not video — which is precisely what disaster coordination and city sensing need, at a tiny fraction of the power and cost.
- **No monthly fees.** After a node is placed, it costs nearly nothing to run.

</section>

---

<section data-state="scrollable">

## Why MeshCore — what changed since v2

Version 2 of this proposal was based on the Meshtastic protocol. We have re-based on **MeshCore**, and the reasons matter for a city deployment:

| | Why it matters for Fukuoka |
|---|---|
| **Explicit infrastructure roles** | MeshCore nodes are typed: *repeater* (mast-mounted relay), *room server* (neighborhood message hub), *sensor*, *handheld*. This maps directly onto city assets — a repeater on a ward office roof, a room server at an evacuation site. |
| **Path-aware routing** | Instead of every node re-shouting every message (flood routing), MeshCore discovers routes and then uses them. In a dense urban network this scales better and wastes less airtime. |
| **Narrower radio channels** | MeshCore community settings fit in one legal Japanese channel cleanly, coexisting politely with the many other users of the 920 MHz band. |
| **Certified hardware exists now** | As of 2026 there are 技適-certified (Japan radio-law compliant) devices sold with MeshCore firmware — including solar outdoor nodes. A public deployment can be fully legal from day one. |
| **Our team ships MeshCore software** | IoTone builds open-source MeshCore client software (Meshmore SNS, a Flutter app) and protocol libraries (Dart and Java). We are contributors, not just consumers — local capability the city can draw on. |

*A lesson from the field: after Hurricane Helene, community reports describe the ad-hoc mesh choking when newcomers flooded it with automatic position updates. Unmanaged flood-routing has real limits — which is the argument for a **managed, city-configured, routed** network. That is what MeshCore's design and this proposal provide.*

</section>

---

<section data-state="scrollable">

## What the city gets

**1. A disaster-resilient communications layer.**
A mesh across wards and evacuation sites that functions with the grid down and towers out. Not a replacement for professional emergency radio — a **civic layer** for shelters, volunteers, and neighborhood associations (町内会).

**2. City-wide sensing without monthly fees.**
River and drainage levels, urban heat, air quality, slope movement — readings flow over the mesh at near-zero operating cost. Sensor coverage decisions stop being contract decisions.

**3. A catalyst for the startup and education ecosystem.**
A free public IoT layer that any student, lab, or startup can build on — the network *is* the sandbox. Engineer Café and the maker community become the front door.

**4. Visible technical leadership.**
The first Japanese city with public MeshCore infrastructure — a concrete, photographable, internationally legible demonstration of Fukuoka's smart-city and startup credentials.

</section>

---

<section data-state="scrollable">

## Use cases — disaster & safety

*The original proposal imagined chat and sensing. A city network can do much more:*

- **Evacuation-site status boards** — each shelter's room-server node publishes occupancy, supply needs, and medical flags to ward officials, even with cellular down.
- **Safety check-ins (安否確認)** — residents with a ¥7,000 handheld (or a phone app over Bluetooth to a pocket node) can send "I am safe + location" messages that hop to city hall.
- **Flood & river gauges** — solar sensor nodes on bridges and drainage canals feed early-warning dashboards; the same network carries the alert back out to neighborhood nodes.
- **Slope & landslide monitors** — tilt/moisture sensors on at-risk slopes in the hilly districts, reporting continuously for the cost of the hardware alone.
- **Typhoon mode** — when cellular networks saturate or fail during storms, the mesh continues carrying coordination traffic for city staff and registered volunteers.
- **School-route safety** — beacon nodes along 通学路 let parents' associations run opt-in presence check-ins without commercial tracking services.

</section>

---

<section data-state="scrollable">

## Use cases — city operations

- **Urban heat-island grid** — dozens of cheap temperature/humidity nodes produce a block-by-block heat map. (Our Meshmore SNS app already ships a working microclimate weather view built on exactly this data.)
- **Air & noise sensing** — particulate and noise nodes near arterials and construction zones, dense enough to see patterns, cheap enough to move.
- **Asset & fleet tracking** — city bicycles, shared equipment, and maintenance vehicles carry tracker nodes; positions flow over the mesh with no per-device contract.
- **Infrastructure monitoring** — vibration and tilt sensors on aging bridges, underpasses, and retaining walls — continuous data where inspections are currently periodic.
- **Wildlife alerts** — boar activity on the urban fringe detected by trap and trail sensors, alerting parks staff over the mesh from locations with no cellular coverage.

</section>

---

<section data-state="scrollable">

## Use cases — community & economy

- **Hyperlocal information chat + AI agent** — a public channel per district, with an AI assistant answering "what's open, what's on, where's the shelter" — usable offline at street level.
- **Festival & event coordination** — Yamakasa, Dontaku, fireworks: staff coordination on a network that doesn't melt when 100,000 phones saturate the cells.
- **Tourism** — multilingual offline info beacons at spots like Nokonoshima and the seaside parks, where visitors' roaming data is spotty.
- **Education** — school STEM programs build and adopt sensor nodes; the mesh is a live laboratory for radio, data, and civic tech. Every node a class builds strengthens the city's network.
- **Startup testbed** — founders prototype IoT products against real public infrastructure without negotiating carrier contracts — a concrete perk for the startup visa ecosystem.

</section>

---

<section data-state="scrollable">

## Evidence — this works in disasters

**Noto Peninsula earthquake, January 2024 — the gap, measured.**
At the peak, **839 mobile base stations were down** across all four carriers. **24 communities — 3,345 residents — were cut off from information entirely.** Roughly 700 Starlink terminals were rushed to ~350 shelters as a stopgap — but satellite restores *backhaul*, not the many-to-many neighborhood layer where residents, volunteers, and shelters coordinate. That first-72-hours layer is exactly what a pre-positioned solar mesh provides.

**Hurricane Helene, North Carolina, September 2024 — mesh in action.**
Officials called it the closest thing to a total communications blackout in recent US history: ~200,000 people without communications, 1,700+ miles of fiber destroyed, **54% of cell sites down** in impacted counties. A local firm (Appalachian Technologies) deployed ~30 Meshtastic LoRa nodes from its stockpile — placing them on high points in coordination with county emergency management — while community accounts describe residents adopting mesh alongside voice radio. Afterward the region *institutionalized* it: NC Mesh is now a statewide solar mesh network, and Asheville's MeshAVL runs both Meshtastic and MeshCore.

**Governments are funding exactly this, right now (2025–26):**
- **Jefferson County, Florida** — county CERT proposal for a countywide solar Meshtastic network at **~$100/node**, restoring a network "within hours after a storm."
- **Kerr County, Texas** — after the July 2025 Guadalupe River flood (139+ dead), the county's new official warning stack includes mesh radio in development.
- **Marion County, Florida** — solar mesh nodes at churches and community sites for hurricane season 2026.
- **Los Angeles** — RegionMesh, a MeshCore network built out after the January 2025 Palisades and Eaton fires.

</section>

---

<section data-state="scrollable">

## Evidence — Fukuoka has done this before, and it worked

**Fukuoka City launched Japan's largest municipal LoRaWAN in July 2017** (with NTT West and Fukuoka's own Braveridge) — roughly 70% of the city covered, free for company proofs-of-concept in disaster prevention, childcare, utilities, and agriculture. In the city's own 2018–19 river-gauge pilot, LoRaWAN radar gauges at Inari Bridge (Chuo-ku) and Harada Bridge (Sawara-ku) cost **about ¥1M per site versus ~¥20M for a conventional gauge — one-twentieth the cost.**

**DaimyoMesh is the successor to the city's own program** — same 920 MHz economics, but decentralized and solar, so the network itself survives the disaster it is reporting on.

**Japan already runs 920 MHz mesh and LPWA sensing at municipal scale:**
- **NICT NerveNet, Shirahama (Wakayama)** — the national institute's disaster-resilient mesh entered full municipal operation in Dec 2022: 15 stations, solar with 3-day battery autonomy, funded by the Digital Garden City grant. NICT also licenses a LoRa Mesh SDK (10-hop relay, no internet required) for shelters and government offices.
- **Otsuchi (Iwate)** — 920 MHz multi-hop wireless linking town hall and shelters, documented in the Fire and Disaster Management Agency's own guidance on diversifying disaster-information channels.
- **Itoshima** — LoRaWAN gateways at 20 public facilities running four civic services (child watch-over, agricultural water, community-bus tracking, wildlife traps) on one network.
- **Goto (Nagasaki)** — ICT boar traps: captures rose 47 → 134 in one year. **MLIT's crisis water-gauge program** — ≤¥1M target price, ~9,300 units deployed nationwide.
- **Ota (Gunma)** — citywide LoRaWAN with 20 gateways **funded by MIC's regional digital infrastructure program** — the same program Fukuoka is eligible for.

</section>

---

<section data-state="scrollable">

## Evidence — the research says mesh works, and the world is building it

**Peer-reviewed results:**
- A 2024 *Sensors* field study showed a multi-hop LoRa mesh can deliver an **earthquake early warning across a 30 km urban radius in 2.4 seconds** — the alert travels at ~20 km/s versus ~3 km/s for the destructive S-wave. A cheap mesh can outrun an earthquake.
- A 2025 Stellenbosch University urban simulation (1,000 nodes): mesh routing lifted packet delivery at the network edge from **~40% to ~74%**, with far nodes using ~5× less power.
- TU Darmstadt's BPoL (IEEE 2023) validated LoRa + store-and-forward networking that survives total infrastructure loss — the academic twin of MeshCore's repeater/room-server design. A University of Zurich crisis-app field test achieved 1.2 km urban links at 92% delivery — the same architecture as a city mesh plus phone app.

**Momentum:** the MeshCore network map grew from **1,000 nodes (May 2025) → 10,600 (Dec 2025) → 38,000+ nodes and 100,000+ app users (April 2026)**. City-scale volunteer meshes run today in the SF Bay Area, Portland, Austin, London, Berlin, and Sydney.

**And the opening:** **no Japanese city has a public LoRa mesh network.** Fukuoka would be first in Japan — and because MeshCore has no academic literature yet, DaimyoMesh would produce the **first municipal-scale MeshCore dataset in the world**, a ready-made research asset for Kyushu University and the Smart EAST program.

</section>

---

<section data-state="scrollable">

## The plan — three phases

**Phase I — Proof of concept** *(3 months, self-funded)*
- Long-range shot: Fukuoka One Building rooftop ↔ Nokonoshima, measuring range, latency, reliability with commodity hardware.
- Urban link: Daimyo ↔ Engineer Café through dense blocks.
- Public write-up; results to social media and local press (Fukuoka NOW).

**Phase II — DaimyoNet testbed** *(12 months — this is the funding ask)*
- ~**100 nodes** spanning Daimyo (west) → Watanabe-dori → Engineer Café.
- Site surveys; installation on rooftops, upper-floor windows, and utility poles — this is where **city facility access** matters.
- Mixed deployment: solar repeaters, room servers at community sites, sensor nodes, loaner handhelds.
- Research program: application pilots (heat grid, river gauge, event trial), operations data, failure analysis.
- **Final public report after 1 year** — every result open.

**Phase III — Formalization** *(after Phase II report)*
- Long-term ownership and maintenance model for public/private portions.
- Community building: schools, industry, government, startups; SNS presence (LINE, Instagram, Discord).
- Long-term funding plan submitted to public and private bodies.

</section>

---

<section data-state="scrollable">

## How the city can fund this — concrete vehicles

This proposal is designed to fit funding programs that already exist:

**Step 1 — no money required: 福岡市実証実験フルサポート事業 (mirai@).**
Fukuoka's own demonstration-experiment support program accepts applications on a rolling basis and provides test fields, city coordination, and joint PR. This is the administrative front door for Phase I/II — and **Fukuoka Growth Next sits in the middle of the Daimyo district** (the former Daimyo Elementary School), a natural anchor and room-server site for the network that shares its name.

**Step 2 — national subsidy for the buildout (½ subsidy rate):**
- **MIC 地域デジタル基盤活用推進事業** — explicitly funds LPWA infrastructure for regional problems including disaster resilience. Fukuoka City is eligible, and there is direct precedent: **Ota City's municipal LoRaWAN was funded by this very program.**
- **新しい地方経済・生活環境創生交付金** (successor to the Digital Garden City grant) — デジタル実装型 TYPE1 covers up to ¥100M national share at a ½ rate; its **地域防災緊急整備型** stream is aimed squarely at disaster-communications projects like this one.

**Step 3 — permanent infrastructure: 緊急防災・減災事業債** (extended to FY2030). Disaster-mitigation bonds cover permanent hardware under the national policy phrase this network embodies: **「災害情報伝達手段の多重化・多様化」** — diversifying and multiplexing disaster-information channels. The FDMA's own guidance already documents a 920 MHz multi-hop shelter network (Otsuchi) under this framing.

*Also free to join: the Digital Agency's 防災DX官民共創協議会 (public-private disaster-DX council) — visibility among every municipality working on this problem.*

</section>

---

<section data-state="scrollable">

## Compliance & risk — done properly

- **Radio law:** Only 技適-certified (MIC technical conformance) hardware is deployed. The 920 MHz band rules (ARIB STD-T108) are followed precisely: ≤20 mW transmit power, mandatory listen-before-talk, duty-cycle limits. We have published our regulatory analysis openly.
- **Certified hardware exists today:** Seeed SenseCAP Solar P1 Pro (certified, solar outdoor, sold with MeshCore firmware), LilyGO T-Echo Japan-certification SKU, and certified Seeed radio modules stocked by Japanese distributors (Switch Science, Marutsu).
- **Privacy:** The network carries no personal data by default. Channels are encrypted; sensor data is environmental; any location feature is opt-in. No cameras, no microphones, no commercial tracking.
- **Interference:** 20 mW is a fraction of a phone's transmit power; LBT means nodes yield to other 920 MHz users (smart meters, RFID) by design.
- **Failure mode:** A failed node degrades coverage gracefully — traffic routes around it. There is no single point whose failure takes the network down. Maintenance is battery/panel replacement on a walk-by schedule.

</section>

---

<section data-state="scrollable">

## Budget sketch — Phase II

Hardware at 2026 street prices (all 技適-certified or Japan-stocked):

| Item | Unit ¥ (approx) | Qty | Subtotal |
|---|---|---|---|
| Solar outdoor nodes (SenseCAP P1 Pro class) | ¥13,000 | 30 | ¥390,000 |
| Repeater/room-server nodes + enclosures | ¥15,000 | 15 | ¥225,000 |
| Sensor nodes (river, heat, air, tilt) | ¥10,000 | 30 | ¥300,000 |
| Loaner handhelds (T-Echo JP-cert class) | ¥8,000 | 25 | ¥200,000 |
| Antennas, mounts, cabling, spares | — | — | ¥250,000 |
| **Hardware total (~100 nodes)** | | | **≈ ¥1.4M** |

Plus: site survey and installation labor, insurance, and a research/reporting budget — full itemization in the funding application. **Total Phase II ask is in the range of a single small public-works line item**, and every deliverable (software, data, reports) is open.

*Comparison: one intersection's traffic-signal installation is commonly quoted at ¥20–50M. The entire 100-node network is under 1/10th of that.*

</section>

---

## Who is behind this

- **IoTone Japan** — IoT engineering; authors of open-source MeshCore software: the Meshmore SNS client app, a pure-Dart MeshCore protocol package, and a Java/Android library (libmeshcore).
- **Proposed collaborators** — Engineer Café (community hub + candidate room-server site), CIC Fukuoka, local maker spaces and co-working locations, and university partners for the research program.
- **Community** — the network is designed to be handed over: Phase III explicitly plans long-term community + city ownership.

---

## The ask

1. **Adopt us into mirai@** (福岡市実証実験フルサポート事業) as the administrative vehicle for the pilot — the program exists for exactly this.
2. **Phase II funding** (hardware + installation + 1-year research program — itemized application to follow this deck, structured to fit the MIC regional digital infrastructure subsidy at a ½ rate).
3. **Access to city facility rooftops and poles** for ~15 repeater sites (ward offices, community centers, schools).
4. **A named city contact** for the pilot — one person, one meeting a month.

In return: an open network, open data, open software, a public report the city can cite, and a working disaster-resilience layer that costs almost nothing to keep running.

---

<section data-state="scrollable">

## References & resources

**Our published groundwork**
- Meshmore SNS (open-source MeshCore client): [github.com/IoTone/offlinesns](https://github.com/IoTone/offlinesns/tree/main/meshmore-sns)
- Device & regulatory research (技適, ARIB STD-T108, hardware index): [/en/tboc/devices/](/en/tboc/devices/)
- MeshCore protocol deep-dive: [/en/tboc/meshcore-intro/](/en/tboc/meshcore-intro/)

**Protocol & hardware**
- MeshCore firmware: [github.com/meshcore-dev/MeshCore](https://github.com/meshcore-dev/MeshCore)
- Certified solar hardware: [seeedstudio.com](https://seeedstudio.com) (SenseCAP Solar P1 Pro, MeshCore SKU)

**Disaster evidence**
- Noto restoration timeline (NTT East): [business.ntt-east.co.jp](https://business.ntt-east.co.jp/column/bizdrive/noto-quake-comms-restoration.html) · isolated communities: [news.yahoo.co.jp](https://news.yahoo.co.jp/articles/1a38ae9b46e3adaca052c56b409aa15e154f370a)
- Helene blackout analysis: [benton.org](https://www.benton.org/headlines/helene-created-unprecedented-communications-blackout-year-later-vulnerabilities-remain) · WNC mesh deployment: [constellationresponse.com](https://constellationresponse.com/blogs/news/the-details-are-not-the-details-they-make-the-design) · community account: [qrper.com](https://qrper.com/2024/10/helene-aftermath-update-adopting-an-off-grid-community-radio-network-thursday-october-24-2024/)
- County programs: [Jefferson Co. FL](https://www.wtxl.com/news/local-news/in-your-neighborhood/monticello/monticello-council-considers-new-emergency-communication-system-proposal) · [Kerr Co. TX](https://www.ksat.com/news/local/2026/07/03/west-kerr-county-pushes-training-on-new-emergency-warning-systems/) · [Marion Co. FL](https://www.wcjb.com/2026/07/11/marion-county-residents-build-off-grid-mesh-network-emergency-messaging/) · [RegionMesh LA](https://www.regionmesh.com/mesh-network-los-angeles/)

**Japan municipal precedent**
- Fukuoka City LoRaWAN 2017: [businessnetwork.jp](https://businessnetwork.jp/article/6208/) · city page ([archived](http://web.archive.org/web/20170724121321/http://www.city.fukuoka.lg.jp/keizai/kagakugijutsu/business/lorawan.html))
- Fukuoka river-gauge PoC (¥1M vs ¥20M): [Nikkei xTech](https://xtech.nikkei.com/atcl/nxt/mag/nc/18/091800071/091800003/) · city page ([archived](http://web.archive.org/web/20210118045639/https://www.city.fukuoka.lg.jp/keizai/kagakugijutsu/business/lorawan_river_water_level.html))
- NICT NerveNet Shirahama: [nict.go.jp](https://www.nict.go.jp/publicity/topics/2022/12/23-1.html) · LoRa Mesh SDK: [kyodonewsprwire.jp](https://kyodonewsprwire.jp/release/202109139968)
- Otsuchi 920 MHz mesh in FDMA guidance: [fdma.go.jp (PDF)](https://www.fdma.go.jp/mission/prepare/transmission/items/transmission001_05_3104-1.pdf)
- Itoshima: [internet.watch.impress.co.jp](https://internet.watch.impress.co.jp/docs/event/1127627.html) · Goto boar traps: [alic.go.jp](https://www.alic.go.jp/joho-s/joho07_002006.html) · MLIT water gauges: [mlit.go.jp (PDF)](https://www.mlit.go.jp/river/shishin_guideline/kasen/pdf/kikikanri_tebiki.pdf) · Ota City: [gunma-u.ac.jp](https://www.gunma-u.ac.jp/information/214471)

**Academic**
- LoRa mesh earthquake early warning (30 km / 2.4 s): [Sensors 24(18):5960, 2024](https://doi.org/10.3390/s24185960)
- Urban mesh performance (40%→74%): [Sensors, 2025](https://pmc.ncbi.nlm.nih.gov/articles/PMC11902654/)
- Multi-hop LoRa survey: [ACM Computing Surveys 56(6), 2024](https://dl.acm.org/doi/10.1145/3638241)
- Disruption-tolerant LoRa (BPoL): [IEEE GHTC 2023 (PDF)](https://peasec.de/paper/2023/2023_SchmidtKuntkeBauerBaumgaertner_BPOL_GHTC.pdf)
- Off-grid crisis app, Univ. of Zurich: [arXiv:2509.22568](https://arxiv.org/abs/2509.22568)

**Momentum & community networks**
- MeshCore map growth: [blog.meshcore.io (Apr 2026)](https://blog.meshcore.io/2026/04/04/meshcore-map) · [year in review (Dec 2025)](https://blog.meshcore.io/2025/12/12/the-year-in-review)
- BayMesh: [bayme.sh](https://bayme.sh) · PDXMesh: [pdxmesh.com](https://pdxmesh.com) · UK Midlands: [meshhubmids.com](https://www.meshhubmids.com/online.html) · NSW: [nswmesh.au](https://nswmesh.au) · local groups directory: [meshtastic.org](https://meshtastic.org/docs/community/local-groups/)

**Funding programs**
- mirai@ (福岡市実証実験フルサポート事業): [mirai.city.fukuoka.lg.jp](https://mirai.city.fukuoka.lg.jp/) · Fukuoka Growth Next: [growth-next.com](https://growth-next.com/about)
- MIC 地域デジタル基盤活用推進事業: [soumu.go.jp](https://www.soumu.go.jp/menu_seisaku/ictseisaku/ictriyou/digital_kiban/index.html)
- 新しい地方経済・生活環境創生交付金: [chisou.go.jp](https://www.chisou.go.jp/sousei/about/shinchihoukoufukin/index.html)
- 緊急防災・減災事業債 (FY2030 extension): [fdma.go.jp (PDF)](https://www.fdma.go.jp/pressrelease/info/items/R8shouboutihouzaisei.pdf)
- 防災DX官民共創協議会: [ppp-bosai-dx.jp](https://ppp-bosai-dx.jp/)

</section>

---

# Thank you

**DaimyoMesh** — a network the city owns, that no disaster can take away.

<a class="deck-link" href="/en/">← OfflineSNS home</a>
