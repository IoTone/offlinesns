# Overview

Meshmore SNS is a mobile client built in Flutter, and used to communicate on the Meshcore network.

## References

- Meshcore : https://meshcore.co.uk/
- LoRa : https://resources.lora-alliance.org/technical-specifications 
- Flutter : https://flutter.dev/
- Meshcore Github: https://github.com/meshcore-dev/MeshCore

## Problem

While Meshcore protocol is open source, the client is not open source.  We want ta general purpose Meshcore compatible client available under a friendly academic open source license.
 

## Requirements

- R1: app is written in flutter
- R2: app runs on Android and iOS
- R3: app uses bluetooth to communicate with a Meshcore client device. 
- R4: app is localized to English and Japanese
- R5: app can perform TTS using the GPU on the phone and should have a TTS mode that can be turned on, off by default
- R6: app should support a small scrollable, collapsable chat interface that is using the active channel, and has an easy way to switch channels 
- R7: app should be able to configure all Meshtastic settings, and input radio settings if defaults are not available
- R8: app has a dashboard as the default home screen
- R9: app has an about screen with the app information
- R10: app has a Terms and Condition
- R11: app has a swipe gesture to left or right to navigate to diferent views, and a long press on the icon to get a navigation menu. 

## Future

- F1: Live map
 
## Test

- TC1: Connect to network
- TC2: Meshcore protocol parser passes all conformance tests
- TC3: Can send messages
- TC4: Can handle encrypted and unencrypted data
- TBD
