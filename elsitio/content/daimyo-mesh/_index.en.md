+++
outputs = ["Reveal"]
title = "DaimyoMesh — A Public Data Network for Fukuoka"
+++

{{< slide transition="zoom" transition-speed="fast" >}}

# DaimyoMesh

## A Public Data Network for Fukuoka

Economic infrastructure — free to use, resilient by design

Proposal v3 · David J. Kordsmeier · IoTone Japan

---

## The problem

- Fukuoka is **Japan's startup capital** — with **no public network** to build on. Every IoT idea starts with a carrier contract, not an experiment.
- The city needs data — rivers, heat, air — but **every sensor pays a SIM fee, every month, forever.**
- **There is no free public layer.** Everything rides one commercial network.

---

## The proposal

> A **free, public, solar-powered data network** across central Fukuoka — open to everyone the way a public road is open to every vehicle — **for less than the cost of one traffic signal.**

Small solar radios on rooftops relay for each other. No SIM cards, no monthly fees, no dependence on the power grid or cell towers.

---

## Networks are the roads of an economy

- A city doesn't bill per trip on its roads — **a road's value is everything it enables.**
- The Internet is free at the point of use. Result: **21% of GDP growth** in mature economies.
- Wi-Fi was born in 1985 when the US opened "garbage" spectrum nobody wanted — including 902–928 MHz, **the American twin of Japan's 920 MHz band**. Its value today: trillions of dollars a year.
- Japan's 920 MHz band is that road: **free, legal, already built.** No Japanese city has put public infrastructure on it. **Fukuoka can be first.**

---

## What Fukuoka gets

1. **A startup catalyst** — free public test infrastructure no other Japanese city offers.
2. **City sensing at 1/20th the cost** — the city's own 2018 pilot: river gauge ¥1M vs ¥20M conventional.
3. **Thought leadership** — the first public mesh network in Japan, and the first economic measurement of one anywhere in the world.
4. **Resilience, free** — solar and tower-independent; a best-effort supplement to official emergency systems, at no extra cost.

---

## Proven, not experimental

- **Fukuoka proved the demand:** its 2017 free municipal LoRaWAN — Japan's largest — drew 50+ companies in its first year.
- **The world proved the scale:** 38,000+ MeshCore nodes worldwide; a 2,000-node network at DEF CON; Burning Man's 1,606-node mesh carried coordination through the 2025 desert storms — the author used it there first-hand.
- **Research proved the physics:** a LoRa mesh delivered an earthquake warning across 30 km in 2.4 seconds — faster than the quake itself.

<small>*Full evidence, citations, and honest limitations → appendix.*</small>

---

## The plan, the cost, the funding

**Phase I** — proof of concept across the bay (3 months, self-funded).
**Phase II** — **the ask:** 100 nodes across Daimyo → Tenjin, public report after one year.
**Phase III** — hand over to a permanent city + community model.

- Hardware for all 100 nodes: **≈ ¥1.4M** — one traffic signal costs ¥20–50M.
- The funding path already exists: **mirai@** → **MIC ½-rate digital-infrastructure subsidy** (Ota City precedent) → **disaster-mitigation bonds**.

---

## The ask

1. **Adopt the pilot into mirai@** (福岡市実証実験フルサポート事業).
2. **Fund Phase II** — structured to fit the MIC ½-rate subsidy.
3. **Rooftop access** at ~15 city facilities.
4. **One named city contact** — one meeting a month.

In return: open data, open software, and a public report with Fukuoka's name on the world's first municipal MeshCore network.

---

# Thank you

**Roads carried Fukuoka's last century of growth. Public networks carry the next.**

David J. Kordsmeier · IoTone Japan

<a class="deck-link" href="/en/">← OfflineSNS home</a>

---

# Appendix

Evidence, use cases, technical detail, and full citations — designed for reading, not presenting. Long pages scroll.

---

{{< slide class="scrollable" >}}

## Appendix — What is a mesh network? (no jargon)

- Each **node** is a radio about the size of a bar of soap. It runs on a small battery, often with a palm-sized solar panel. Hardware cost: **¥5,000–¥15,000 per node**.
- Nodes **relay messages for each other**. A message hops node-to-node until it arrives — like a bucket brigade. There is no tower and no center to fail.
- Each hop covers **hundreds of meters to several kilometers**, even in dense city blocks; rooftop nodes can span the bay.
- The network **gets stronger as it grows**: every added node is new coverage and a new path around damage.
- It carries **short messages and sensor readings**, not video — which is precisely what city sensing and everyday coordination need, at a tiny fraction of the power and cost.
- **No monthly fees.** After a node is placed, it costs nearly nothing to run.

---

{{< slide class="scrollable" >}}

## Appendix — Why MeshCore, what changed since v2

Version 2 of this proposal was based on the Meshtastic protocol. We have re-based on **MeshCore**, and the reasons matter for a city deployment:

| | Why it matters for Fukuoka |
|---|---|
| **Explicit infrastructure roles** | MeshCore nodes are typed: *repeater* (mast-mounted relay), *room server* (neighborhood message hub), *sensor*, *handheld*. This maps directly onto city assets — a repeater on a ward office roof, a room server at an evacuation site. |
| **Path-aware routing** | Instead of every node re-shouting every message (flood routing), MeshCore discovers routes and then uses them. In a dense urban network this scales better and wastes less airtime. |
| **Narrower radio channels** | MeshCore community settings fit in one legal Japanese channel cleanly, coexisting politely with the many other users of the 920 MHz band. |
| **Certified hardware exists now** | As of 2026 there are 技適-certified (Japan radio-law compliant) devices sold with MeshCore firmware — including solar outdoor nodes. A public deployment can be fully legal from day one. |
| **Our team ships MeshCore software** | IoTone builds open-source MeshCore client software (Meshmore SNS, a Flutter app) and protocol libraries (Dart and Java). We are contributors, not just consumers — local capability the city can draw on. |

*A lesson from the field: after Hurricane Helene, community reports describe the ad-hoc mesh choking when newcomers flooded it with automatic position updates — and every large event deployment (DEF CON, Burning Man; see the events evidence slide) has only worked by shipping centrally-managed "Event Mode" firmware. Unmanaged flood-routing has real limits — which is the argument for a **managed, city-configured, routed** network. That is what MeshCore's design and this proposal provide.*

---

{{< slide class="scrollable" >}}

## Appendix — Use cases: community & economy

*The original proposal imagined chat and sensing. A public network enables much more — and, like a road, the best uses will be ones nobody planned:*

- **Startup testbed** — founders prototype IoT products against real public infrastructure without negotiating carrier contracts — a concrete perk for the startup visa ecosystem, and the digital equivalent of giving every startup free road access.
- **Hyperlocal information chat + AI agent** — a public channel per district, with an AI assistant answering "what's open, what's on, where do I go" — usable offline at street level.
- **Festival & event coordination** — Yamakasa, Dontaku, fireworks: staff coordination on a network that doesn't melt when 100,000 phones saturate the cells.
- **Tourism** — multilingual offline info beacons at spots like Nokonoshima and the seaside parks, where visitors' roaming data is spotty.
- **Education** — school STEM programs build and adopt sensor nodes; the mesh is a live laboratory for radio, data, and civic tech. Every node a class builds strengthens the city's network — public infrastructure that residents can *contribute to*, not just consume.

---

{{< slide class="scrollable" >}}

## Appendix — Use cases: city operations

- **Urban heat-island grid** — dozens of cheap temperature/humidity nodes produce a block-by-block heat map. (Our Meshmore SNS app already ships a working microclimate weather view built on exactly this data.)
- **Air & noise sensing** — particulate and noise nodes near arterials and construction zones, dense enough to see patterns, cheap enough to move.
- **Asset & fleet tracking** — city bicycles, shared equipment, and maintenance vehicles carry tracker nodes; positions flow over the mesh with no per-device contract.
- **Infrastructure monitoring** — vibration and tilt sensors on aging bridges, underpasses, and retaining walls — continuous data where inspections are currently periodic.
- **Wildlife alerts** — boar activity on the urban fringe detected by trap and trail sensors, alerting parks staff over the mesh from locations with no cellular coverage.

---

{{< slide class="scrollable" >}}

## Appendix — Use cases: the resilience co-benefit

*These uses come free with the same hardware. To be precise about scope: this is a **best-effort civic layer** that supplements official emergency systems (防災行政無線, J-ALERT, 緊急速報メール) — it replaces nothing, guarantees nothing, and carries no life-safety responsibility. The same way a road network supplements but doesn't replace ambulances.*

- **Flood & river gauges** — solar sensor nodes on bridges and drainage canals feed city dashboards; the same network can carry advisories back out to neighborhood nodes.
- **Slope & landslide monitors** — tilt/moisture sensors on at-risk slopes in the hilly districts, reporting continuously for the cost of the hardware alone.
- **Community status boards** — a community center's room-server node can publish local status (open/closed, supplies, notices) to ward officials even when other channels are congested.
- **Neighborhood check-ins** — residents with a ¥7,000 handheld (or a phone app over Bluetooth to a pocket node) can send short "all fine here" messages that hop across town — useful every day, not just in emergencies.
- **School-route presence** — beacon nodes along 通学路 let parents' associations run opt-in presence check-ins without commercial tracking services.

---

{{< slide class="scrollable" >}}

## Appendix — Evidence: the economics of open networks, measured

**Network adoption drives growth — the most-replicated result in digital economics.** The causally cleanest estimate (Czernich et al., *The Economic Journal* 2011, OECD panel): a 10-point rise in broadband penetration raises annual per-capita growth by **0.9–1.5 points**. The World Bank's landmark 2009 study found +1.21 to +1.38 points; the ITU's 2020 update found 10% fixed-broadband growth lifting GDP per capita up to **+2.94%** in high-income economies. (The literature is correlation-heavy — we cite the instrumented estimate first deliberately.)

**The theory is textbook.** Hotelling (1938) and Vickrey (1963) established that the welfare-optimal price for uncongested, non-rival infrastructure is **zero at the point of use**, funded from general budgets — tolls on an uncongested bridge destroy value. The IMF finds public infrastructure investment in advanced economies raises output ~1.5% within four years — it "can pay for itself." And Wi-Fi itself exists because the US FCC opened unlicensed "garbage" bands in 1985 — including 902–928 MHz, the American twin of Japan's 920 MHz band. Nobel laureate Paul Milgrom's verdict on auctioning such bands: "akin to asking users of public parks to bid against developers." Industry-commissioned studies value Wi-Fi at $3.3–4.9 trillion per year globally.

**Removing the toll multiplies the value.** When the US made Landsat satellite data free in 2008 (it had been earning ~$5M/year in fees), usage grew 100-fold and measured annual economic benefit reached **$25.6B by 2023 — a return of more than 1,000×** on the abolished toll. The counter-example is instructive: Copenhagen's *paid* City Data Exchange failed and shut down, while the free Things Network grew from a 6-week crowdsourced build in Amsterdam (2015) to **21,200 gateways in 153 countries**.

**Free community networks create businesses and jobs.** Chattanooga's community fiber network: **$2.69B in economic benefit and 9,516 jobs over ten years — 4.4× its cost** (EPB-commissioned university study; the 15-year update reads $5.34B). Catalonia's guifi.net commons network (peer-reviewed, *Computer Networks* 2015): 28,675 nodes with dozens of small companies earning millions of euros a year on shared infrastructure.

**Japan's own numbers point the same way.** ICT is now **10.0% of Japan's GDP (¥57.4T)**, and 総務省's analysis shows ICT capital contributed **+0.51 points of Japan's total 0.27% growth** in 2015–2022 — ICT is what is holding Japanese growth up. The ministry projects IoT/AI adding **¥132T to 2030 GDP**.

**And the measurement gap is an opportunity:** no study anywhere has quantified the economic value of a free community LoRa network — Japan's 920 MHz band has no economic literature at all. DaimyoMesh's Phase II report would be the **first economic measurement of public mesh infrastructure**, with Fukuoka's name on it.

---

{{< slide class="scrollable" >}}

## Appendix — Evidence: Fukuoka has done this before, and measured the economics

**Fukuoka is Japan's startup capital — the demand side is proven.** The city's business-opening rate (開業率) is **4.9%, first among Japan's 21 major cities for seven consecutive years**; Fukuoka Growth Next alone has incubated **653 startups that raised ¥42.2B** during tenancy. This is the population of builders a free public network serves.

**And the city has already run this experiment.** Fukuoka launched Japan's largest municipal LoRaWAN in July 2017 (with NTT West and Fukuoka's own Braveridge) — roughly 70% of the city covered, **explicitly free of charge to remove communication-cost barriers**. Over 50 companies engaged within a year, and the testbed produced the world's first commercial LoRaWAN Class B service. In the city's own 2018–19 river-gauge pilot, LoRaWAN radar gauges at Inari Bridge (Chuo-ku) and Harada Bridge (Sawara-ku) cost **about ¥1M per site versus ~¥20M for a conventional gauge — one-twentieth the cost.**

**What didn't sustain was the architecture, not the demand:** the program depended on a single central operator and its gateway contracts, and quietly wound down (the city's program pages survive only in web archives). **DaimyoMesh is the corrected successor** — the same 920 MHz economics, but decentralized, solar, and community-extensible: no central gateway contract to expire, and every new participant strengthens the network instead of billing it.

**Japan already runs 920 MHz mesh and LPWA sensing at municipal scale:**
- **NICT NerveNet, Shirahama (Wakayama)** — the national institute's mesh entered full municipal operation in Dec 2022: 15 stations, solar with 3-day battery autonomy, funded by the Digital Garden City grant. NICT also licenses a LoRa Mesh SDK (10-hop relay, no internet required) for local-government use.
- **Otsuchi (Iwate)** — 920 MHz multi-hop wireless linking town hall and community sites, documented in the Fire and Disaster Management Agency's own guidance.
- **Itoshima** — LoRaWAN gateways at 20 public facilities running four civic services (child watch-over, agricultural water, community-bus tracking, wildlife traps) on one network.
- **Goto (Nagasaki)** — ICT boar traps: captures rose 47 → 134 in one year. **MLIT's crisis water-gauge program** — ≤¥1M target price, ~9,300 units deployed nationwide.
- **Ota (Gunma)** — citywide LoRaWAN with 20 gateways **funded by MIC's regional digital infrastructure program** — the same program Fukuoka is eligible for.

---

{{< slide class="scrollable" >}}

## Appendix — Evidence: stress-tested at the world's largest events

Nothing tests a mesh network like tens of thousands of people in one dense place with no infrastructure. Two annual events have become the de-facto proving grounds:

**DEF CON (Las Vegas).** The world's largest hacker conference has run official Meshtastic event deployments three years running: DEF CON 32 (2024) shipped the first official event firmware; **DEF CON 33 (2025) connected 2,000+ nodes — the largest known Meshtastic network ever assembled** (peak count 2,333); DEF CON 34 (Aug 2026) firmware is already announced. This is now institutional practice, not an experiment.

**Burning Man (Nevada desert).** The community "Burning Mesh" project grew from ~1,000 nodes (2024) to **1,606 nodes in 2025**, with a peak day of 28,817 packets. When hurricane-force dust storms hit the 2025 opening weekend — destroying major art installations — **the mesh carried coordination traffic with people stuck in the miles-long gate queue, during and after the storms.** The author of this proposal used the 2025 Burning Man mesh first-hand in those emergency conditions: it was genuinely useful when nothing else was.

**The honest part — why stock firmware fails at this scale:**
- Meshtastic's own project documentation puts the practical limit of *default* settings at roughly **50–80 nodes**; the Burning Mesh team states flatly that on defaults, their 1,000-node mesh "would have been 100% unusable." A New Zealand community mesh at 150+ nodes saturated its channel and became unusable.
- Every event success required special **"Event Mode" firmware** built with Meshtastic's own maintainers: a faster short-range preset instead of the default, hop limit capped at 3, telemetry throttled 4× more aggressively, router roles banned, one centrally-managed channel. **The network only worked because someone managed it.**
- Even then, limits showed: at DEF CON 33 device node databases (hardware caps of ~100–250 entries) overflowed and phone apps buckled at peak; at Burning Man 2025 one camp lost 4 of its 5 rooftop repeaters to a single storm — no engineered redundancy.

**What this means for DaimyoMesh:** every large deployment teaches the same lesson — mesh at civic scale needs *managed configuration, engineered redundant infrastructure, and routing that doesn't flood*. That is precisely this proposal: a city-configured MeshCore network (path-aware routing; handheld clients never rebroadcast) on professionally sited, redundant solar repeaters. The events prove the demand and the physics. The failures define the engineering job — and that job is what Phase II funds.

---

{{< slide class="scrollable" >}}

## Appendix — Evidence: the research says mesh works, and the world is building it

**Peer-reviewed results:**
- A 2024 *Sensors* field study showed a multi-hop LoRa mesh can deliver an **earthquake early warning across a 30 km urban radius in 2.4 seconds** — the alert travels at ~20 km/s versus ~3 km/s for the destructive S-wave. A cheap mesh can outrun an earthquake.
- A 2025 Stellenbosch University urban simulation (1,000 nodes): mesh routing lifted packet delivery at the network edge from **~40% to ~74%**, with far nodes using ~5× less power.
- TU Darmstadt's BPoL (IEEE 2023) validated LoRa + store-and-forward networking that survives total infrastructure loss — the academic twin of MeshCore's repeater/room-server design. A University of Zurich crisis-app field test achieved 1.2 km urban links at 92% delivery — the same architecture as a city mesh plus phone app.

**Momentum:** the MeshCore network map grew from **1,000 nodes (May 2025) → 10,600 (Dec 2025) → 38,000+ nodes and 100,000+ app users (April 2026)**. City-scale volunteer meshes run today in the SF Bay Area, Portland, Austin, London, Berlin, and Sydney.

**And the opening:** **no Japanese city has a public LoRa mesh network.** Fukuoka would be first in Japan — and because MeshCore has no academic literature yet, DaimyoMesh would produce the **first municipal-scale MeshCore dataset in the world**, a ready-made research asset for Kyushu University and the Smart EAST program.

---

{{< slide class="scrollable" >}}

## Appendix — Evidence: the resilience co-benefit, demonstrated elsewhere

*None of the following is why the city should build DaimyoMesh — the economics above are. But it shows what the same infrastructure quietly provides on the worst day, at no extra cost. DaimyoMesh makes no life-safety claims: it is a best-effort layer that supplements official systems.*

**Noto Peninsula earthquake, January 2024.** At the peak, **839 mobile base stations were down** across all four carriers; **24 communities — 3,345 residents — were cut off from information entirely.** ~700 Starlink terminals rushed to ~350 shelters restored *backhaul* — not the neighborhood many-to-many layer where residents and community sites coordinate. A pre-positioned solar mesh is that layer, already in place because the city built it for its everyday economic uses.

**Hurricane Helene, North Carolina, September 2024.** ~200,000 people without communications, **54% of cell sites down** in impacted counties. A local firm deployed ~30 Meshtastic nodes in coordination with county emergency management; the region then institutionalized the approach (NC Mesh statewide, MeshAVL in Asheville).

**Other governments are treating mesh as preparedness infrastructure (2025–26):** Jefferson County FL (~$100/node countywide proposal), Kerr County TX (post-flood warning stack), Marion County FL (solar nodes at community sites), and Los Angeles's RegionMesh (MeshCore, built out after the January 2025 fires).

The pattern to note: in every case, the network was worth the most where it **already existed before the event** — which is an argument for building it for everyday reasons, now.

---

{{< slide class="scrollable" >}}

## Appendix — The plan in detail: three phases

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

---

{{< slide class="scrollable" >}}

## Appendix — How the city can fund this: concrete vehicles

This proposal is designed to fit funding programs that already exist. Note the useful asymmetry: the *pitch* is economic infrastructure, but the *resilience co-benefit* is what makes the project eligible for Japan's best-funded program streams — the city can fund a startup-economy asset with disaster-preparedness money:

**Step 1 — no money required: 福岡市実証実験フルサポート事業 (mirai@).**
Fukuoka's own demonstration-experiment support program accepts applications on a rolling basis and provides test fields, city coordination, and joint PR. This is the administrative front door for Phase I/II — and **Fukuoka Growth Next sits in the middle of the Daimyo district** (the former Daimyo Elementary School), a natural anchor and room-server site for the network that shares its name.

**Step 2 — national subsidy for the buildout (½ subsidy rate):**
- **MIC 地域デジタル基盤活用推進事業** — explicitly funds LPWA infrastructure for regional problems including disaster resilience. Fukuoka City is eligible, and there is direct precedent: **Ota City's municipal LoRaWAN was funded by this very program.**
- **新しい地方経済・生活環境創生交付金** (successor to the Digital Garden City grant) — デジタル実装型 TYPE1 covers up to ¥100M national share at a ½ rate; its **地域防災緊急整備型** stream is aimed squarely at disaster-communications projects like this one.

**Step 3 — permanent infrastructure: 緊急防災・減災事業債** (extended to FY2030). Disaster-mitigation bonds cover permanent hardware under the national policy phrase this network embodies: **「災害情報伝達手段の多重化・多様化」** — diversifying and multiplexing disaster-information channels. The FDMA's own guidance already documents a 920 MHz multi-hop shelter network (Otsuchi) under this framing.

*Also free to join: the Digital Agency's 防災DX官民共創協議会 (public-private disaster-DX council) — visibility among every municipality working on this problem.*

---

{{< slide class="scrollable" >}}

## Appendix — Compliance & risk: done properly

- **Radio law:** Only 技適-certified (MIC technical conformance) hardware is deployed. The 920 MHz band rules (ARIB STD-T108) are followed precisely: ≤20 mW transmit power, mandatory listen-before-talk, duty-cycle limits. We have published our regulatory analysis openly.
- **Certified hardware exists today:** Seeed SenseCAP Solar P1 Pro (certified, solar outdoor, sold with MeshCore firmware), LilyGO T-Echo Japan-certification SKU, and certified Seeed radio modules stocked by Japanese distributors (Switch Science, Marutsu).
- **Privacy:** The network carries no personal data by default. Channels are encrypted; sensor data is environmental; any location feature is opt-in. No cameras, no microphones, no commercial tracking.
- **Interference:** 20 mW is a fraction of a phone's transmit power; LBT means nodes yield to other 920 MHz users (smart meters, RFID) by design.
- **Failure mode:** A failed node degrades coverage gracefully — traffic routes around it. There is no single point whose failure takes the network down. Maintenance is battery/panel replacement on a walk-by schedule.

---

{{< slide class="scrollable" >}}

## Appendix — Budget sketch: Phase II

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

---

## Appendix — Who is behind this

- **IoTone Japan** — IoT engineering; authors of open-source MeshCore software: the Meshmore SNS client app, a pure-Dart MeshCore protocol package, and a Java/Android library (libmeshcore).
- **Proposed collaborators** — Engineer Café (community hub + candidate room-server site), CIC Fukuoka, local maker spaces and co-working locations, and university partners for the research program.
- **Community** — the network is designed to be handed over: Phase III explicitly plans long-term community + city ownership.

---

{{< slide class="scrollable" >}}

## Appendix — References & resources

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

**Economics of open networks**
- Broadband → growth (causal, OECD panel): [Czernich et al., *The Economic Journal* 121:552 (2011)](https://doi.org/10.1111/j.1468-0297.2011.02420.x) · World Bank 2009 (+1.21–1.38pp): [IC4D Ch.3](https://documents1.worldbank.org/curated/en/645821468337815208/pdf/487910PUB0EPI1101Official0Use0Only1.pdf) · ITU 2020: [economic contribution of broadband](https://www.itu.int/dms_pub/itu-d/opb/pref/D-PREF-EF.BDR-2020-PDF-E.pdf)
- Zero-price theory: Hotelling 1938 / Vickrey 1963, via [Frischmann & Hogendorn, *JEP* 29(1) 2015](https://pubs.aeaweb.org/doi/pdf/10.1257/jep.29.1.193) · IMF infrastructure multiplier: [WEO Oct 2014, Ch.3](https://www.elibrary.imf.org/display/book/9781498331555/ch003.xml)
- Unlicensed spectrum: [Milgrom, Levin & Eilat 2011 (Stanford)](https://web.stanford.edu/~jdlevin/Papers/UnlicensedSpectrum.pdf) · Wi-Fi value (industry-commissioned): [Wi-Fi Alliance 2021](https://www.wi-fi.org/system/files/Global_Economic_Value_of_Wi-Fi_2021-2025_202109.pdf) · [WifiForward 2024](https://wififorward.org/wp-content/uploads/2024/09/Assessing-the-Economic-Value-of-Wi-Fi.pdf)
- Free-data multiplier: [Landsat $25.6B/yr (USGS 2023)](https://www.usgs.gov/news/featured-story/landsats-economic-value-increases-256-billion-2023) · paid-market contrast: [Copenhagen City Data Exchange lessons](https://thelivinglib.org/city-data-exchange-lessons-learned-from-a-publicprivate-data-collaboration/)
- Community networks: [Chattanooga 10-yr study (EPB-commissioned)](https://assets.epb.com/media/Lobo%20-%20Ten%20Years%20of%20Fiber%20Infrastructure%20in%20Hamilton%20County%20TN_Published.pdf) · [guifi.net, *Computer Networks* 90 (2015)](https://people.ac.upc.edu/leandro/pubs/crowds-guifi-en.pdf) · [The Things Network](https://www.thethingsnetwork.org/)
- Japan: [令和7年版 情報通信白書 (ICT = 10.0% GDP)](https://www.soumu.go.jp/johotsusintokei/whitepaper/ja/r07/pdf/n2110000.pdf) · [総務省 ICT経済分析 2024 (+0.51pp)](https://www.soumu.go.jp/johotsusintokei/linkdata/r05_01.pdf) · [H29白書 IoT/AI +¥132T](https://www.soumu.go.jp/johotsusintokei/whitepaper/ja/h29/html/nc135220.html)
- Fukuoka: [開業率 4.9% No.1 (Fukuoka Facts)](http://facts.city.fukuoka.lg.jp/data/business-opening/) · [FGN 653 startups / ¥42.2B](https://prtimes.jp/main/html/rd/p/000000054.000037341.html) · [2017 LoRaWAN (NTT West PDF)](https://www.ntt-west.co.jp/newscms/kyushu2/11186/20170704.pdf) · [NTT技術ジャーナル](https://journal.ntt.co.jp/article/5426)

**Large-scale events**
- DEF CON 33 official recap (2,000+ nodes): [meshtastic.org blog](https://meshtastic.org/blog/that-one-time-at-defcon/) · field report (peak 2,333): [shellntel](https://blog.shellntel.com/p/def-con-33-and-meshtastic-on-the-lilygo-t-deck-plus) · DC32 event firmware: [github.com/meshtastic/defcontastic](https://github.com/meshtastic/defcontastic)
- Burning Mesh project: [burningmesh.org](https://www.burningmesh.org) · docs/FAQ (node counts, default-preset limits): [docs.burningmesh.org](https://docs.burningmesh.org) · official 2024 firmware: [github.com/meshtastic/burntastic](https://github.com/meshtastic/burntastic)
- Burning Man 2025 field report (1,606 nodes, storm coordination): [Burners Without Borders](https://burnerswithoutborders.org/projects/meshtastic-meets-burning-man/)
- LongFast scaling limits (official): [meshtastic.org blog](https://meshtastic.org/blog/why-your-mesh-should-switch-from-longfast/)

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

<a class="deck-link" href="/en/">← OfflineSNS home</a>
