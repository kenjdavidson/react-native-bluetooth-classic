---
title: Getting Started
description: ""
summary: Introduction and installation of React Native Bluetooth Classic
---

## Introduction

**React Native Bluetooth Classic** was developed to bridge the gaps in the available React Native Bluetooth moduless:

- [https://github.com/rusel1989/react-native-bluetooth-serial](https://github.com/rusel1989/react-native-bluetooth-serial)
- [https://github.com/nuttawutmalee/react-native-bluetooth-serial-next](https://github.com/nuttawutmalee/react-native-bluetooth-serial-next)

are both fantastic implementations but they fall back to BLE on IOS.  

### IOS 

The hardware in which my company was working would only communicate over Bluetooth Classic and required communication through the [External Accessory](https://developer.apple.com/documentation/externalaccessory) framework.  Please note that while working with this library on IOS, you'll need to become accustomed with the Apple and their [MFi program](https://en.wikipedia.org/wiki/MFi_Program).  Here are some links to check out:

- [https://en.wikipedia.org/wiki/MFi_Program](https://en.wikipedia.org/wiki/MFi_Program)
- [https://developer.apple.com/library/archive/samplecode/EADemo/Introduction/Intro.html](https://developer.apple.com/library/archive/samplecode/EADemo/Introduction/Intro.html)

### Android 

The Android functionality is pretty common and well documented, there are a number of examples which can be reviewed.

## Working Versions

The goal is to make this library work with as many devices as possible.  For that reason I've tried to keep the Android and IOS versions as low as their respective stores will allow.

| Version | React Native | Android | IOS | Branch |
| --- | --- | --- | --- | --- | --- |
| 0.9.x | 0.59.9 | 4.1 (16) | IOS 9 | releases/0.9.x |
| 0.10.x | 0.60.0 | 4.1 (16) | IOS 9 | releases/0.10.x |
| 1.0.x | 0.60.0 | 8.0 (24) | IOS 9 | master |

#### Caveat

It may be possible to use the library in lower versions that those noted, if you confirm it working, please submit a pull request with updated version details.

## Changes

Please see the [change log](https://github.com/kenjdavidson/react-native-bluetooth-classic/blob/master/CHANGELOG.md) for complete(ish) details.

## Installation

Installation, like almost everything, is done through `npm`:

```shell
$ npm install react-native-bluetooth-classic --save
```

Once installed [autolinking](https://github.com/react-native-community/cli/blob/master/docs/autolinking.md) will take over within your application.  

> With the changes in v1.0.0 it's possible that Autolinking doesn't actually work (just be prepared for that).  The goal is to have it 100% working and customizable as per the React Native documentation, but until then just beware.

#### Manual Linking / Customization

There may be times where overriding autolinking is required, for those examples please see the specific [android](android/) and [ios](ios/) APi(s).

## Contribute

### Issues / Enhancements

Feel free to submit any [issues](https://github.com/kenjdavidson/react-native-bluetooth-classic/issues) or [enhancements](https://github.com/kenjdavidson/react-native-bluetooth-classic/issues) through Github.  Please follow the provided templates (respective) and provide as much information as you can:

- stack trace
- images
- example(s)/repositories
- etc.

### Pull Requests

Feel free to submit any pull requests.  Try to document which issue (if any) it resolves, or provide some details on what you're adding.  I regularly try to look at the forks to see what changes there are and if there is value in adding them to the project. 

**Please update it accordingly when submitting a pull request!!**
