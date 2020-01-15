# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.9.6] - 2020-01-14
- Added `RNBluetoothClassic.setEncoding(BTCharsets)` to allow configuration of parsing prior to connection.
- Brought a number of `sendEvent(BTEvents.ERROR, data)` into alignment between Android and IOS.  Errors should always contain the following: `error` (Dictionary | String), `message` (String) and `device` (Dictionary | WritableMap).

## [0.9.5] - 2020-01-10
- Resolved read issues on Android and IOS to allow for both polling and READ events [22](https://github.com/kenjdavidson/react-native-bluetooth-classic/issues/24)
- Refactored a number of IOS functions to include reject (which is required and wasn't done previously)
- Refactored `available` on IOS to return the same count value as Android
- Updated BluetoothClassicExample with both polling and READ events for testing

## [0.9.4] - 2020-01-05
- Merged podspec pull request, which was validated with 0.60.x (so lets cross our fingers)

## [0.9.3] - 2019-12-10
- Resolved bugs with iOS regarding NSError() vs nil in promise rejection
- Aligned some of the rejections vs resolves(false) to match Android
- Added extra error handling within Android Bluetooth adapter calls

## [0.9.2] - 2019-11-29
- Added installation instructions to README.md for local installations
- Added missing bridge functions for Android and IOS

## [0.9.1] - 2019-09-12
### Added
- discoverDevices function access on RNBluetoothClassic javascript

## [0.9.0] - 2019-09-12
Initial tag created for release for the purpose of getting something on NPMJS so that current projects do not require `npm local-install` in order to function.  Specifically so that I can start adding features and bug fixes (breaking changes).

### Added
- Initial functionality for library
  
