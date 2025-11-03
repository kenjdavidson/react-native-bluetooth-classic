# GitHub Actions Workflows

This directory contains GitHub Actions workflows for continuous integration and deployment.

## Active Workflows

### Android Build (`android-build.yml`)

Automatically builds and tests the Android library on pull requests.

**Triggers:**
- Pull request opened, synchronized, or reopened
- Only runs when relevant files change (android/, src/, package.json)

**What it does:**
1. Sets up Java 17 and Node.js 18
2. Installs npm dependencies
3. Validates Gradle wrapper
4. Builds the Android library using Gradle
5. Uploads build reports on failure

### iOS Build (`ios-build.yml`)

Automatically builds and tests the iOS library on pull requests.

**Triggers:**
- Pull request opened, synchronized, or reopened
- Only runs when relevant files change (ios/, src/, package.json, podspec)

**What it does:**
1. Sets up Node.js 18 and Ruby 3.0
2. Installs npm dependencies
3. Installs CocoaPods
4. Validates the podspec
5. Builds the iOS library using xcodebuild
6. Uploads build logs on failure

### Test and Build (`test-and-build.yml`)

Manual workflow for comprehensive testing across all environments.

**Triggers:**
- Manual dispatch only (`workflow_dispatch`)

**What it does:**
1. Runs JavaScript tests
2. Builds the TypeScript library
3. Builds Android with Gradle
4. Validates iOS build

### Other Workflows

- `publish-documentation.yml` - Publishes documentation to GitHub Pages
- `release-and-publish-npm.yml` - Handles releases and npm publishing

## Running Workflows Locally

### Android Build

```bash
cd android
./gradlew build
```

### iOS Build

```bash
cd ios
xcodebuild clean build \
  -project RNBluetoothClassic.xcodeproj \
  -scheme RNBluetoothClassic \
  -sdk iphonesimulator \
  -configuration Release
```

### JavaScript/TypeScript

```bash
npm ci
npm run test
npm run build
```

## Notes

- The Android and iOS workflows use path filters to only run when relevant code changes
- Build artifacts and logs are uploaded on failure for debugging
- The workflows use the latest stable versions of actions and tools
