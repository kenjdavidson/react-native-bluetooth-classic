
# react-native-bluetooth-classic

React Native Bluetooth Classic is meand to bridge the gap found with regards to IOS Bluetooth Classic using the External Accessory framework. 

Based off the [react-native-bluetooth-serial](https://github.com/rusel1989/react-native-bluetooth-serial) port, and updated to replace [CoreBluetooth](https://developer.apple.com/documentation/corebluetooth) (BLE) on IOS with [External Accessory](https://developer.apple.com/documentation/externalaccessory/eaaccessory).

## Versions

Since there seem to be some breaking changes introduced within React Native 0.60 and I'm not entirely sure how or if these changes will affect this projec; or that reason I feel it's important to start running with a number of release branches (for the time being) just in case things go down.  In the following table, the React Native version is the lowest version (from package.json).

| Version | React Native | Android | IOS | Notable Changes |
| --- | --- | --- | --- | --- |
| 0.9.x | 0.41.0 - 0.59.9 | >= 4.1 (16) | >= IOS 9 | - Accept connection mode |
| 0.10.x | >= 0.60.0 | >= 4.1 (16) | >= IOS 9 | - Accept connection mode |
| 1.60.x | >= 0.60.0 | >= 8 (26) | >= IOS 9 | |

If this breaks in a newer version of React Native, Android or IOS please open an issue.  Not sure what the resulting versions will (or should be) since `1.0.x (0.60.0)`, `2.0.x (0.64.0)` for example seems weird.  Although maybe `1.60.0` and `1.64.0` is pretty apparent to which version needs to be used?

## Getting started

### Install from NPM

```
$ npm install react-native-bluetooth-classic --save
```

React Native 0.60.0 [autolinking](https://github.com/react-native-community/cli/blob/master/docs/autolinking.md).

### Manual installation

Manual installation should really only need to be used for contribution, or if there are issues with autolinking that I'm not aware of.  Follow the old steps for manually linking.

#### IOS Manual

1. In XCode, in the project navigator, right click `Libraries` ➜ `Add Files to [your project's name]`
2. Go to `node_modules` ➜ `react-native-bluetooth-classic` and add `RNBluetoothClassic.xcodeproj`
3. In XCode, in the project navigator, select your project. Add `libRNBluetoothClassic.a` to your project's `Build Phases` ➜ `Link Binary With Libraries`
4. Run your project (`Cmd+R`)<

#### IOS Pods

The development pod can be added updating the `Podfile` with the appropriate line:

```
pod 'react-native-bluetooth-classic', :path => '<PATH TO RNBluetoothClassic>'
```

#### MFi Protocol Strings

**REMINDER** Remember to make sure you have your protocol strings provided within your application `plist` file - this is a requirement of the External Accessory framework.  This is the top cause of `Unhandled JS Exception: TypeError: Cannot read property 'xxx' of undefined` while attempting to use the library.

An example of what this looks like is:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
  <key>UISupportedExternalAccessoryProtocols</key>
  <array>
    <string>com.apple.m1</string>
  </array>
</dict>
</plist>
```

#### Android

As of React Native 0.60.0 **autolinking** should take over.  This library has been updated to provide default autolinking configuration, along with the ability to easily override when you require your own implementations.

###### Disable Autolinking

If you want to skip autolinking please follow the documentation provided:

1. Disable auto linking in `react-native.config.js`
2. Open up `android/app/src/main/java/[...]/MainApplication.java`
  - Add `import kjd.reactnative.bluetooth.RNBluetoothClassicPackage;` to the imports at the top of the file
  - Add `new RNBluetoothClassicPackage()` to the list returned by the `getPackages()` method
3. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-bluetooth-classic'
  	project(':react-native-bluetooth-classic').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-bluetooth-classic/android')
  	```
4. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      implementation project(':react-native-bluetooth-classic')
  	```

#### Windows

Windows isn't added yet - it looks like with the latest `react-native init` there is no Windows by default.  I'm assuming this is due to the React Native windows project (future Ken's problem).

[Read it! :D](https://github.com/ReactWindows/react-native)

1. In Visual Studio add the `RNBluetoothClassic.sln` in `node_modules/react-native-bluetooth-classic/windows/RNBluetoothClassic.sln` folder to their solution, reference from their app.
2. Open up your `MainPage.cs` app
  - Add `using Bluetooth.Classic.RNBluetoothClassic;` to the usings at the top of the file
  - Add `new RNBluetoothClassicPackage()` to the `List<IReactPackage>` returned by the `Packages` method

## Contribute

Feel free to contribute any changes or bug fixes you see fit.  Although when doing so please try to take into account that:

- Changes should be customizable where possible (especially when possibly breaking to others)
- Changes should be documented

#### Android

When first building the Android project there were issues with react-native-create-library and the version of Android/Gradle installed on my machine.  This needed to be resolved by ensuring that the project was inline with the version of Android Studio and the Android plugin for gradle.  In my case, the project was configured with 1.3.1 and 2.2, which caused problems, in order to resolve [Android plugin for gradle versions](https://developer.android.com/studio/releases/gradle-plugin.html)

1. Updated `gradle-wrapper.properties` to modify the line:
`distributionUrl=https\://services.gradle.org/distributions/gradle-5.1.1-all.zip`

2. Updated `build.gradle` to ensure the buildscript section matched the following:
  - Added google() to buildscript
  - added jcenter() to repositories

```
buildscript {
    repositories {
        google()
        jcenter()
    }

    dependencies {
        classpath 'com.android.tools.build:gradle:3.4.0'
    }
}

repositories {
    mavenCentral()
    google()
    jcenter()
}
```

#### IOS (xcode)

Much like Android, the IOS project was being edited through the BluetoothClassicExample project by opening files through the RNBluetoothClassic library folder.  Since React Native is a peer dependency (and annoying to download and install locally with the changes to NPM5) I just continued to do the development this way, it worked out since it allowed me to test the changes on the fly. 

Again - if someone can point me on how to resolve these issues easily, I'd love to get it sorted.

## License

The MIT License (MIT) - see full [license file](LICENSE)

## Example(s)

The development app [BluetoothClassicExample](https://github.com/kenjdavidson/react-native-bluetooth-classic-apps/tree/main/BluetoothClassicExample) has been moved out of this repository into [react-native-bluetooth-classic-apps](https://github.com/kenjdavidson/react-native-bluetooth-classic-apps) to allow for a little less head ache while attempting to replicate and debug issues with the different versions.  This will also allow me to maintain a repository of example applications or showcase applications for those that wish.

### Showcase

Feel free to submit a pull request to the [react-native-bluetooth-classic-apps](https://github.com/kenjdavidson/react-native-bluetooth-classic-apps) project if you've got a sample (or production) which you believe might help others.

## Usage

Import the module using the following (forgive the pluralization, it just happened and it's come to far now):

```javascript
import RNBluetoothClassic, { BluetoothEventType } from 'react-native-bluetooth-classic';
```

In all cases the following API/Events are available within Javascript for both Android and IOS (no code splitting) if there are any native calls that are not available on the native side, the promise will be rejected with an appropriate message (kind of like UnssupportedOperationException since I'm used to Java) - I found this important as I see no point in duplicating code as the whole purpose of React Native was for me not to.

for more information see the [documentation](https://kenjdavidson.github.io/react-native-bluetooth-classic).

## Testing

The following devices are used for testing:

### Universal Worldscan Reader

The [Universal Worldscan Reader](https://microchip.homeagain.com/starter-kit.html) provides both Android and IOS (Mfi) reading and writing functionality.

### Eyoyo Barcode Scanner

The [Eyoyo Barcode Scanner](https://www.amazon.ca/Eyoyo-Portable-Handheld-compatible-Computers/dp/B01DVTC4R8) is only available for testing on Android as it's not MFi compliant.  It has both a classic and BLE mode.
