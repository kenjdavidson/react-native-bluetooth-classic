# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.10.2] - 2020-01-10
- Resolved read issues on Android and IOS to allow for both polling and READ events [22](https://github.com/kenjdavidson/react-native-bluetooth-classic/issues/24)
- Refactored a number of IOS functions to include reject (which is required and wasn't done previously)
- Refactored `available` on IOS to return the same count value as Android
- Updated BluetoothClassicExample with both polling and READ events for testing

## [0.10.1] - 2020-01-05
- Merged 0.60.0 Podspec features provided by [iamandiradustefan@gmail.com](https://github.com/iamandiradu)

### Added
- Initial functionality for library
  
