
# react-native-bluetooth-classic

React Native Bluetooth Classic functionality for both Android and IOS.  

Based off the [react-native-bluetooth-serial](https://github.com/rusel1989/react-native-bluetooth-serial) port, and updated to replace [CoreBluetooth](https://developer.apple.com/documentation/corebluetooth) (BLE) on IOS with [External Accessory](https://developer.apple.com/documentation/externalaccessory/eaaccessory).

The purpose of this was three fold:
- Take a run at learning how to work with React Native modules, I find the best way to learn is to take an established project and deconstruct/construct.
- The project I was working on required that both Android and IOS supported a Bluetooth Classic serial scanner.
- Seemed like a good idea.

## Getting started

### Install from NPM

Currently unavailable until v1.0.0 is released.

```
$ npm install react-native-bluetooth-classic --save
```

### Install from NPM (local)

Currently the project is not posted on NPM, it may in the future.  In order to install locally the following steps are required:

```
$ git clone https://github.com/kenjdavidson/react-native-bluetooth-classic
```

With NPM lower than version 5 just running the command `npm install file://../` will install correctly.  In NPM 5+ this creates a symlink which will stop React Native / Metro from working.  In order to resolve this there are two options:

1. Manually install the react-native-bluetooth-classic project into node_modules
2. Use the following, which will package and install the local project into node_modules.  For more information on install-local please see (Install Local)[https://www.npmjs.com/package/install-local].

```
npm install install-local
install-local ../react-native-bluetooth-classic
```

### Automatic installation

`$ react-native link react-native-bluetooth-classic`

### Manual installation

#### iOS

1. In XCode, in the project navigator, right click `Libraries` ➜ `Add Files to [your project's name]`
2. Go to `node_modules` ➜ `react-native-bluetooth-classic` and add `RNBluetoothClassic.xcodeproj`
3. In XCode, in the project navigator, select your project. Add `libRNBluetoothClassic.a` to your project's `Build Phases` ➜ `Link Binary With Libraries`
4. Run your project (`Cmd+R`)<

#### Android

1. Open up `android/app/src/main/java/[...]/MainActivity.java`
  - Add `import com.reactlibrary.RNBluetoothClassicPackage;` to the imports at the top of the file
  - Add `new RNBluetoothClassicPackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-bluetooth-classic'
  	project(':react-native-bluetooth-classic').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-bluetooth-classic/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      implementation project(':react-native-bluetooth-classic')
  	```

#### Windows
[Read it! :D](https://github.com/ReactWindows/react-native)

1. In Visual Studio add the `RNBluetoothClassic.sln` in `node_modules/react-native-bluetooth-classic/windows/RNBluetoothClassic.sln` folder to their solution, reference from their app.
2. Open up your `MainPage.cs` app
  - Add `using Bluetooth.Classic.RNBluetoothClassic;` to the usings at the top of the file
  - Add `new RNBluetoothClassicPackage()` to the `List<IReactPackage>` returned by the `Packages` method

## Contribution

When setting up the project for contribution follow all the usual Git contribution best practices.

### Annoyances

When setting up the project I rant into a couple of issues - this section describes those.

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

## Example

The BluetoothClassicExample is included within the ./BluetoothClassicExample React Native project.   Please see ./BluetoothClassicExample for details.

## Usage

Import the module using the following:

```javascript
import RNBluetoothClassic, { BTEvents } from 'react-native-bluetooth-classic';
```

In all cases the following API/Events are available within Javascript for both Android and IOS (no code splitting) if there are any native calls that are not available on the native side, the promise will be rejected with an appropriate message (kind of like UnssupportedOperationException since I'm used to Java) - I found this important as I see no point in duplicating code as the whole purpose of React Native was for me not to.

### API

The following API is available on both Android and IOS (unless specifically stated not).  I've done my best to duplciate all the methods available on both, so there should be no need to use Platform or file switching.  For any API calls that aren't supported on a specific environment, they should reject the promise with an 'UnsupportedOperation' error.

#### requestEnabled(): Promise

Requests that the platform Bluetooth adapter be enabled.

##### Android 

Starts the ACTION_REQUEST_ENABLED Intent on Anroid.  Resolves **true** if the user enables Bluetooth, and rejects if they do not.  Debating update to just resolve **true|false** instead of reject.

##### IOS

rejects automatically as this is not supported yet.

#### Promise isEnabled(): Promise

Resolves **true|false** based on whether the Platform Bluetooth is enabled.  

#####

IOS uses the CoreBluetooth framework which might not be the best way to do things (mixing classic with BLE) but it seems to work.

#### Promise list(): Promise

Resolves with a list of

##### Android

the currently paired devices

##### IOS

the currently connected devices (with appropriate MFi protocols)

#### Promise discoverDevices(): Promise

Attempts to start Bluetooth device discovery and

##### Android

resolves with a list [possibly empty] of nearby devices

##### IOS

rejects automatically as this is not supported yet.

#### cancelDiscovery(deviceId:String): Promise

Cancels the currently running device discovery, if no discovery is running then it

##### Android

Resolves **true|false** based on whether discovery was cancelled

##### IOS

rejects automatically as this is not supported yet.

#### pairDevice(deviceId:String): Promise

Attempts to pair the device with the provided Id

##### Android

Resovles with the Device when paired, rejects if the pairing fails or is not possible.

##### IOS

rejects automatically as this is not supported yet.

#### unpairDevice(deviceId:String): Promise

Attempts to un-pair the device with the provided Id

##### Android

Resovles with the Device when unpaired, rejects if the pairing fails or is not possible.

##### IOS

rejects automatically as this is not supported yet.

#### connect(deviceId:String): Promise

Attempts to connect to the device with the provided Id.  Currently it will attempt to disconnect the currently connected device - will attempt to update to allow for multiple Bluetooth devices at a single time.   Resolves with the newly connected device information or rejects if a connection is not available.

#### disconnect(): promise

Attempts to disconnect from a device.  Resolves **true|false** based on whether disconnection was successful.  This will need to be updated to accept a deviceId when multiple devices can be connected

#### isConnected(): Promise

Resolves **true|false** whether a device is currently connected.

#### getConnectedDevice(): Promise

Resolves with the currently connected devices, or rejects if there is none.  

#### writeToDevice(message: String): Promise

Writes the provided message to the device.  The String should be Base64 encoded.  Resovles true when the write completes.

#### readFromDevice(): Promise

Resolves with the content of the devices buffer.  Currently not implemented in IOS but next on the docket.  This method should not be used in conjunction with the BTEvent.READ event as it could cause some unexpected behaviour.

#### readUntilDelimiter(): Promise

Resovles with the content of the buffer up until the default delimiter.  To update the delimiter for the session use setDelimiter(delimiter:String).  This method should not be used in conjunction with the BTEvent.READ event as it could cause some unexpected behaviour.

#### readUntilDelimiter(delimiter:String): Promise

Resolves with the content of the buffer up until the provided delimiter.  This method should not be used in conjunction with the BTEvent.READ event as it could cause some unexpected behaviour.

#### setDelimiter(String delimiter): Promise

Sets the new delimiter for future reads/read events and resolves true.

#### available(): Promise

Resolves **true|false** based on whether data is available.  Use in conjunction with the read[until|from] functions.

### Events
  
The following events are currently available:

##### BLUETOOTH_ENABLED

`BTEvent.BLUETOOTH_ENABLED` is fired when the platform enables the bluetooth adapter.

##### BLUETOOTH_DISTABLED

`BTEvent.BLUETOOTH_DISABLED` is fired when the platform disables the bluetooth adapter.

##### BLUETOOTH_CONNECTED

`BTEvent.BLUETOOTH_CONNECTED` is fired when a bluetooth device is connected.  The event data contains information regarding the Device which was just connected.  Generally a new `RNBluetoothModule.list()` should be completed at this time.

##### BLUETOOTH_DISCONNECTED

`BTEvent.BLUETOOTH_DISCONNECTED` is fired when a bluetooth device is connected.  The event data contains information regarding the Device which was just disconnected.  Generally a new `RNBluetoothModule.list()` should be completed at this time.

##### CONNECTION_SUCCESS

`BTEvent.CONNECTION_SUCCESS` is fired when a connection request has been completed.  Generally if you're calling `RNBluetoothModule.connect()` you shouldn't really need to subscribe to these, but if you want to there is not stopping it.

##### CONNECTION_FAILED

`BTEvent.CONNECTION_FAILED` is fired when connect() is called but fails.  Again it generally isn't required if you're using the Promise version of `RNBluetoothModule.connect()`

##### CONNECTION_LOST

`BTEvent.CONNECTION_LOST` is fired when an open connection is lost.  This occurs when a BluetoothDevice which may have an open connection/stream turns itself off.  On Android this will signify an error, but on IOS this could possibly happen if there is no activity.  In most cases a `BTEvent.BLUETOOTH_DISCONNECTED` is also fired, in which case it may be easier to listen to that in order to change status.

##### READ

`BTEvent.READ` is fired whenever new data is available.  The current implementation is to publish any number of data in chunks based on the delimiter.  For exapmle, if the delimiter is '\n' (default) and data comes in with three messages (three delmited messages) then the client will get three READ events which it should handle.  In the future I hope I can move the reading logic from the `RNBluetoothModule` into an Interface/Protocol so that the client can call custom implementations.

##### ERROR

`BTEvent.ERROR` is fired any time an error (which is not classified above) occurs.