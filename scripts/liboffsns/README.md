# Overview

liboffsns is what it sounds like.  It is not a social networking service.  It is not online.  And yet, it is a network, and can be used to communicate with a group.

This is a first PoC for building appilcations on top of the platform.

## POC1

Some rough ideas on a tiny protocol for connecting things (I'll just call it MinIOT for the moment)

- We interconnect on Meshtastic public network
- We have a raspi or esp32 that acts as the proxy for the sensor
- When sensor data comes in that is interesting, we publish using the "cli" or API for meshtastic... basically you can talk to the device over UART or BLE
- Interested parties in minIOT data can subscribe by filtering on Interest
- in a pub/sub system, you ignore things you aren't interested in
- addressing scheme isn't necessary, you are filtering on some "interest tags... these can be any text string or a regex"
- Such a system, I could see a way to filter interest on "fukuoka weather data" published by node.Kanako.1 which sends out weather alerts.  Node.Davi1 is outside of any formal internet connection and benefits from the live feed coming over the meshtastic network
- An improvement on efficiency would be to insert packets in such a way that you limit the number of hopos the message will propagate , or add a propagation filter based on geography ... if locations are set, maybe we can be sure these are ignored by the network 

Anyway, this is a quick 10 minute idea / sketch.  We can scheme up something at our meeting.  I am trying to think about how to get around the limitation of the default firmware, which is basically a chat network with some sidechannel for certain kinds of sensor data.

TODO: Convert this diagram into mermaidjs

```
Sensor A
|
RPI or ESP32--Sensor B
|
Meshtastic Node.David1
^
|
V
[MESHTASTIC CLOUD]
^
|
V
Meshtastic Node  Kanako.1
|
RPI or ESP32 --Sensor D - WIFI -- AWS
|
Sensor C
```

### node.js

- Install node 20
- npm install
- node test_comms.js

### rust

This first example is a PC/meshtastic bridge over uart, using a web server chat GUI as the UI.  In theory, you can build on this to communicate to your network as a human.  You could also use this to write a monitoring tool (observability ... slack for meshtastic robots), or to write raw the pub sub interface described in PoC1

- cargo build
- cargo run
- follow the prompts to choose the uart
- open a Chrome browser to: localhost:3333

