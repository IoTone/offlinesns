# Overview

OfflineSNS is the local Meshtastic Community in Fukuoka Japan.

## Mission Statement

We need a private, secure network for communications that can survive natural disaster and acts of humans, and our group will promote approaches to creating this network, maintaining the network, and teaching others responsible use of this network.

## Network Settings

### Fukuoka Test Net: engcafe

Description: This network is known as "engcafe" and is intended to be used for events at Engineer Cafe or for demos and testing.  We will utilize this as our first network on channel 1.

Geography: Fukuka-Shi
Detailed settings:

- name: engcafe
- psk: v0YmAcxlV9lo8xubMXAF7w==
- keysize: 128bit
- role: Secondary
- allow position requests: ON
- Precise Location: ON


We have no further rules on this channel, but we ask users to be courteous, and no spam or advertising.

## Community Guidelines

TODO: we will write up community guidelines.

## Tools

To use the tools, we recommend you install the meshtastic-cli.  These can be installed from this location: https://meshtastic.org/docs/software/python/cli/installation/

Before using the scripts, please activate your virtualenv since meshtastic-cli uses python.

Onetime: virtualenv menv
Everytime: . ./menv/bin/activate

- setup_engcafe_channel_device.sh : use this to setup the engcafe channel for private communications

We will add Japanese instructions in the future.

## Website

The website for this community is created with http://gohugo.io .  Maintainers and contributors can fork this repo, and submit a PR to make updates.

Otherwise, the maintainers can build the project for testing simply:

- cd elsitio
- hugo serve

When it is looking good:

- git commit
- git push

