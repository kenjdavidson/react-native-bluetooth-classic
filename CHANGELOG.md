# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

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
  
