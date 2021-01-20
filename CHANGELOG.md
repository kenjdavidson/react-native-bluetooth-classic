# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.60.0-rc7]

## Edited

- Issue/84 Resolved issue with missing default connection options on Android (both React and Native)

## [1.60.0-rc6]

## Edited

- Reverted all the sdk 24 version functionality (streams, BiConsumer, etc) to their original.  This will allow correct installation and functionality on minSdk 16 which was requested.
- Resolved issue with `accept()` not returning the device once completed - therefore not letting the client/app know things were good

### Removed

- Removed a number of unused classes, interfaces and enums.
## [1.60.0-rc5]

### Oops

- Completely missed this version, I really need to automate this.
## [1.60.0-rc.4]

### Edited

- Issue/71 Resolved issue **Invalid Connector Type**
## [1.60.0-rc.3]

Initial release
### Added

- Refactored project to Typescript
- Refactored Android and IOS modules to align names and Promise resolution
- Added the ability to connect to multiple devices
- Wrapped `DeviceConnection` communication on React Native
