+++
outputs = ["Reveal"]
title = "Offline SNS"

+++

{{< slide transition="zoom" transition-speed="fast" >}}

# Meashtastic / Offline SNS

---

- Mesh Networking 101
- An Overview of Meshtastic
- Demo
- Projects (we can do some coding)
- Call to Action

---

Mesh networks 101


---

<section data-noprocess>
  <p>Typical Meshnetwork Configurations
  <img src="/images/meshexamples.jpg" />
</section>

---

<section data-noprocess>
  <p>are just abstractions for this
  <img src="/images/mesh_fabric.jpg" />
</section>

--- 

Thing about the previous photo
- Every point in the mesh that intersects, is X hops to any other point
- Mesh Frabric is strong because it can survive tears
- Other mesh "nets" can carry the load if one area is weak
- The "edges" of a net may connect to something
- Quick demo to show how a mesh works using people

---

Pro / Con of A Mesh Network
- +In theory, it is infrastructure free
- +In theory, zeroconf
- +Able to self heal
- -Lower Bandwidth
- -May be less energy efficient
- -Protocol overhead is hire, spectrum saturates more quickly
- -May require specialized hardware

---

# Meshtastic Overview

---

## Problem Statement

We need an always available, public (privacy focused) network for communications to share messages, location, send telemetry, convey timely information, that is zero cost to use, resistant to nature or human intervention, and capable of operating over metro-WAN scale areas without wired interconnect.

---

## Many Solutions (that don't solve the problem)

- ~~5G~~
- ~~WIFI~~
- ~~BLE~~
- ~~Ethernet~~

---

<section data-noprocess data-background-color="#80d280">
  <H1>Solution: Meshtastic (based on LoRa)</H1>
  <p>"An open source, off-grid, decentralized, mesh network built to run on affordable, low-power devices"
  <img src="/images/meshtastic_typelogo.svg" />
</section>

---

## Open Source built on LoRa

https://lora-alliance.org/

---

<section data-noprocess>
  <H1>LoRa is a Global Specification</H1>
  <img src="/images/lora-topology.webp" />
</section>

---

## Why Is LoRA Special compared to 5G or WIFI

- No license needed to use
- No subscription needed to use
- Theoretical range is 9km between two nodes
- Can operate in a true mesh topology
- Can be secured / private end to end

---

## Weaknesses of LoRa

- Open Standard but doesn't specify an application layer, stops at the MAC layer (in OSI terms)
- Very low data rate, and high latency
- Networks are not as prevelant as 4G/5G or wifi
- The most common computing devices lack a LoRa radio

---


## Meshtastic solves multiple things

1. Provides firmware that runs on many LoRa radio devices
2. Provides an application protocol for P2P networking
3. Provides client Applications that run on PC, Mac, Linux, iOS, Android, Chrome

---

## All The Code is on GH

https://github.com/meshtastic

---

## Use Cases

### 1. Privacy Centric Networks

"It's not free if you don't own the communications"

--- 

## Use Cases

### 2. Emergency Communications

Japan has more opportunities for disaster from the weather and the environment.
This is probably the main reason people keep a device nearby.


---

## Use Cases

### 3. P2P Chat 

This is the main application sited by casual users ... talking with friends or family.
Note: Some people use it for live game maps when playing Airsoft/Paintball.

---

## Use Cases

### 4. Long Range Communications Between two points

One of the often mentioned uses is setting up some IoT communications between a remote site.  Extending a WiFi network can be done but is much more expensive and perhaps overkill if power requirements need to be low.

--- 

## Use Cases

### 5. Extreme Sports/Outdoor Adventuring

In Japan it is hard to find a place 5G network doesn't work.  But out at sea, or in a cave, etc., it is good to have a sure way to communicate.  In the US, it is very common use case sited is to share position while doing outdoor sports: hiking, skiing, etc.

--- 

## Use Cases

### 6. Industrial IoT / Freight & Logistics

Easy math suggests the savings from using a free Meshtastic/LoRa network vs using mBIoT / 5G service X N nodes in an IoT deployment is significant savings.  It isn't perfect for all use cases, but it is good enough for many use cases.

--- 

## Use Cases

### 8.  Citizen Data / HAM Radio sidechannel

A popular use case these days is personal data collection projects

---

## Use Cases

### 9.  Event Communications

At large festivals, political protests, etc., it can be difficult to maintain communication with your people.  Cellular communication often can be unreliable if everyone tries to use their phones.  Meshtastic can scale to a degree, though above 1000 active nodes in one place, it can become saturated.

---

## Use Cases

### 10.  Zero Cost/Subscription Free Networks

Like Wifi or Bluetooth, Meshtastic operates without subscriptions.  There is no "carrier" to pay unless you are operating on a privately funded network.  The only costs are creating the network: antennas, routers, repeaters, edge devices, cables, solar.

---

## Proximity of Communications (is Hyperlocal and MWAN scale)

- < 1m OK (0 hops) : fine for simple device communication nearby
- 1m-300m (0 hops) : a suitable alternative to wifi or BLE for signaling, logging, chat
- < 1km (>= 1 hops): small scale network, point to point comms between two sites
- < 5km (>= 2 hops): multi-site network, coverage of neighborhoods
- < 9km (>= 3 hops): metro-area network scale, tracking things moving around, robust

--- 

## Device Zoo

Let's look at some devices

---

{{< slide background-image="/images/WiFi-LoRa-32-structure-chart-1024x610.png" >}}

---


{{< slide background-image="/images/T-ECHO_3-558631079.jpg" >}}

---

{{< slide background-image="/images/lilygo-tdeck.jpg" >}}

---


{{< slide background-image="/images/RAKMeshtasticStarterKit_5.webp" >}}

---

{{< slide background-image="/images/wouldnt-something-like-this-be-awesome-v0-1dkm167bzpl91.webp" >}}

---

{{< slide background-image="/images/solar-vehicle-trackers-v0-jqyqfmz9vcaf1.webp" >}}

---

{{< slide background-image="/images/solar-vehicle-trackers-v0-jqyqfmz9vcaf1.webp" >}}

---


{{< slide background-image="/images/built-my-first-node-to-accompany-my-uconsoles-lora-board-v0-ss5kf2a9mx9f1.webp" >}}

---

## Get Your Devices

- Amazon:  you can get everything here https://www.amazon.co.jp/s?k=meshtastic
- Heltec: an original manufactuerer https://heltec.org/
- Lilygo: they have a great designs: https://lilygo.cc/collections/lora-or-gps
- Alibaba : It's hard to find things, but everything is for sale there, it's just hard to make sure you are ordering devices and not socks for the devices (featured in the description).
- IoTone Japan : We have 8 units in stock sales@iotone.jp

---

## Demos

- Peer To Peer Chat / Range Test
- Flash Meshtastic Firmware using Chrome
- 3D Print a Case
- Meshtastic CLI demo
- Other stuff??

---

## Projects (page 1)

- Localize Meshtastic iOS
- Localize Meshtastic Docs 日本語 (Maybe AI can help ... it's a huge task)
- For learning: set up a private network
- Set up Meshtastic CLI to operate your devices instead of a phone
- Hardware 3d cases

---

## Projects (page 2)

- Get the HeltecV3 "exception" filed for the "GITEKI" mark: https://www.tele.soumu.go.jp/e/sys/equ/tech/
- Create an aplication layer for a Pub/Sub protocol on top of any meshtastic network (without firmware changes)
- Create a "robot" communication language using the chat channel to let AIs speak to each other and coordinate IoT device activity

---

## Let's build the first Kyushu WAN

We need a handful of nodes in "high places":
- Kyushi University
- Fukuoka Tower
- Hakata Tower
- Your "Mansion", Your Office rooftop

---

## Resources

- http://meshtastic.org
- Node list globally (Japan has 7 < ) https://meshmap.net/
- Our PR to get JA localization added to the iOS app https://github.com/meshtastic/Meshtastic-Apple/pull/1292
- OfflineSNS Notes from IoTone Japan https://note.com/truedata_iotone
- Japanese Meshtastic Group (FB): https://www.facebook.com/groups/1749997532422254
- Global Communities: https://github.com/meshtastic/meshtastic/blob/master/docs/community/local-groups.mdx

## More Resources

- Some great tips on client settings, repeater settings, etc: https://pole1.co.uk/meshtastic-roles/


## Github Repos / Meshtastic

- Interesting Olama/NodeJS integration https://github.com/NerdsCorp/meshtastic-controller

---

## Videos

- Get Started Fast: https://www.youtube.com/watch?v=gH-K9fRuhfQ&t=8s
- Getting Started: https://www.youtube.com/watch?v=DUz6cVSaSl4
- A complete offgrid setup: https://www.youtube.com/watch?v=_v11m2FQQZU&t=466s
- Getting Started / Antennas: https://www.youtube.com/watch?v=F6w4QtYE6L8
- Meshtastic vs Meshcore: https://www.youtube.com/watch?v=tXoAhebQc0c

## Call To Action

- Get involved: Github: https://github.com/IoTone/offlinesns
- Contact: (David) djk @ iotone.jp

---

{{< slide background-image="/images/meshtastic_wide.png" >}}

---

Thankyou
ありがとうございました