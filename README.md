# react-native-bluetooth-classic-with-server

This repo is with added listening (server) functionality.

React Native Bluetooth Classic is meand to bridge the gap found with regards to IOS Bluetooth Classic using the External Accessory framework.

Based off the [react-native-bluetooth-serial](https://github.com/rusel1989/react-native-bluetooth-serial) port, and updated to replace [CoreBluetooth](https://developer.apple.com/documentation/corebluetooth) (BLE) on IOS with [External Accessory](https://developer.apple.com/documentation/externalaccessory/eaaccessory).

## Getting started

### Install from NPM

```
$ npm install react-native-bluetooth-classic --save
```

### Install from NPM (local)

If making or testing custom changes to react-native-bluetooth-classic you'll want to Install locally.

```
$ git clone https://github.com/kenjdavidson/react-native-bluetooth-classic
```

With NPM lower than version 5 just running the command `npm install file://../` will install correctly. In NPM 5+ this creates a symlink which will stop React Native / Metro from working. In order to resolve this there are two options:

1. Manually install the react-native-bluetooth-classic project into node_modules
2. Use the following, which will package and install the local project into node_modules. For more information on install-local please see (Install Local)[https://www.npmjs.com/package/install-local].

```
npm install -g install-local
install-local -S ../react-native-bluetooth-classic
```

### Automatic installation

If installing locally, you'll need to perform the local install after each subsequent react-native link. Just be aware of that, if you run react-native link on any future packages, you'll find that the Android project becomes uncompilable (just run the install local command again).

`$ react-native link react-native-bluetooth-classic`

### Manual installation

#### iOS

1. In XCode, in the project navigator, right click `Libraries` ➜ `Add Files to [your project's name]`
2. Go to `node_modules` ➜ `react-native-bluetooth-classic` and add `RNBluetoothClassic.xcodeproj`
3. In XCode, in the project navigator, select your project. Add `libRNBluetoothClassic.a` to your project's `Build Phases` ➜ `Link Binary With Libraries`
4. Run your project (`Cmd+R`)<

#### Android

1. Open up `android/app/src/main/java/[...]/MainApplication.java`

- Add `import kjd.reactnative.bluetooth.RNBluetoothClassicPackage;` to the imports at the top of the file
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

TODO - but leaving the section here.

[Read it! :D](https://github.com/ReactWindows/react-native)

1. In Visual Studio add the `RNBluetoothClassic.sln` in `node_modules/react-native-bluetooth-classic/windows/RNBluetoothClassic.sln` folder to their solution, reference from their app.
2. Open up your `MainPage.cs` app

- Add `using Bluetooth.Classic.RNBluetoothClassic;` to the usings at the top of the file
- Add `new RNBluetoothClassicPackage()` to the `List<IReactPackage>` returned by the `Packages` method

## Contribute

When setting up the project for contribution follow all the usual Git contribution best practices.

#### Android

When first building the Android project there were issues with react-native-create-library and the version of Android/Gradle installed on my machine. This needed to be resolved by ensuring that the project was inline with the version of Android Studio and the Android plugin for gradle. In my case, the project was configured with 1.3.1 and 2.2, which caused problems, in order to resolve [Android plugin for gradle versions](https://developer.android.com/studio/releases/gradle-plugin.html)

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

Much like Android, the IOS project was being edited through the BluetoothClassicExample project by opening files through the RNBluetoothClassic library folder. Since React Native is a peer dependency (and annoying to download and install locally with the changes to NPM5) I just continued to do the development this way, it worked out since it allowed me to test the changes on the fly.

Again - if someone can point me on how to resolve these issues easily, I'd love to get it sorted.

## License

The MIT License (MIT) - see full [license file](LICENSE)

## Example

The BluetoothClassicExample is included within the ./BluetoothClassicExample React Native project.

## Usage

Import the module using the following:

```javascript
import RNBluetoothClassic, { BTEvents } from "react-native-bluetooth-classic";
```

In all cases the following API/Events are available within Javascript for both Android and IOS (no code splitting) if there are any native calls that are not available on the native side, the promise will be rejected with an appropriate message (kind of like UnssupportedOperationException since I'm used to Java) - I found this important as I see no point in duplicating code as the whole purpose of React Native was for me not to.

## API

The following API is available on both Android and IOS (unless specifically stated not). I've done my best to duplciate all the methods available on both, so there should be no need to use Platform or file switching. Each call will return a Promise - for any API calls that aren't supported on a specific environment, they should reject the promise with an 'UnsupportedOperation' error.

| Function                             | Description                                                                                                                                                                                                                                                                |      Android       |        IOS         |
| ------------------------------------ | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | :----------------: | :----------------: |
| requestEnabled()                     | Requests that the environment enables the Bluetooth adapter.                                                                                                                                                                                                               | :white_check_mark: |     :no_entry:     |
| isEnabled()                          | Resolves **true\|false** based on whether the Platform Bluetooth is enabled. IOS uses the CoreBluetooth framework which might not be the best way to do things (mixing classic with BLE) but it seems to work.                                                             | :white_check_mark: | :white_check_mark: |
| list()                               | Resolves with a list of the currently paired/connected (Android/IOS with MFi protocol respectively) devices. Returns with an empty list if there are none available.                                                                                                       | :white_check_mark: | :white_check_mark: |
| discoverDevices()                    | Resolves to a list of discovered devices.                                                                                                                                                                                                                                  | :white_check_mark: |     :no_entry:     |
| cancelDiscovery()                    | Resolves **true\|false** based on whether discovery was cancelled.                                                                                                                                                                                                         | :white_check_mark: |     :no_entry:     |
| pairDevice(deviceId:String)          | Resolves with the status of the requested device if paired. Rejects if unable to pair.                                                                                                                                                                                     | :white_check_mark: |     :no_entry:     |
| unpairDevice(deviceId:String)        | Resolves with a list of the unpaired devices.                                                                                                                                                                                                                              | :white_check_mark: |     :no_entry:     |
| connect(deviceId:String)             | Resolves with the device details if successfully paired. Rejects if the connection is unsuccessful - if already connected the rejection will also disconnect the currently connected device.                                                                               | :white_check_mark: | :white_check_mark: |
| accept()                             | Starts accepting connections and resolves with the device details of successfully connected device.                                                                                                                                                                        | :white_check_mark  |     :no_entry:     |
| disconnect()                         | Resolves **true\|false** based on whether disconnection was successful.                                                                                                                                                                                                    | :white_check_mark: | :white_check_mark: |
| isConnected()                        | Resolves **true\|false** whether a device is currently connected.                                                                                                                                                                                                          | :white_check_mark: | :white_check_mark: |
| getConnectedDevice()                 | Resolves with the currently connected devices, or rejects if there is none.                                                                                                                                                                                                | :white_check_mark: | :white_check_mark: |
| write(message: String)               | Writes the provided message to the device. The String should be Base64 encoded. Resovles true when the write completes.                                                                                                                                                    | :white_check_mark: | :white_check_mark: |
| readFromDevice()                     | Resolves with the entire content of the devices buffer, ignoring any delimiters and clearing the buffer when complete. This method should not be used in conjunction with the BTEvent.READ event as it could cause some unexpected behaviour.                              | :white_check_mark: | :white_check_mark: |
| readUntilDelimiter()                 | Resovles with the content of the buffer up until the default delimiter. To update the delimiter for the session use setDelimiter(delimiter:String). This method should not be used in conjunction with the BTEvent.READ event as it could cause some unexpected behaviour. | :white_check_mark: | :white_check_mark: |
| readUntilDelimiter(delimiter:String) | Resolves with the content of the buffer up until the provided delimiter. This method should not be used in conjunction with the BTEvent.READ event as it could cause some unexpected behaviour.                                                                            | :white_check_mark: | :white_check_mark: |
| setDelimiter(delimiter:String)       | Sets the new delimiter for future reads/read events and resolves true, resolves with the API to allow for fluent chaining                                                                                                                                                  | :white_check_mark: | :white_check_mark: |
| available()                          | Resolves **true\|false** based on whether data is available. Use in conjunction with the read[until\|from] functions.                                                                                                                                                      | :white_check_mark: | :white_check_mark: |

### Code Examples

#### requestEnabled(): Promise

TODO

#### isEnabled(): Promise

```javascript
let enabled = await RNBluetoothClassic.isEnabled();
console.log(`Bluetooth enabled? ${enabled}`);
```

#### list(): Promise

```javascript
let devices = await RNBluetoothClassic.list();
console.log(`Available devices: ${devices.length});
```

#### discoverDevices(): Promise

TODO

#### cancelDiscovery(): Promise

TODO

#### pairDevice(deviceId:String): Promise

TODO

#### unpairDevice(deviceId:String): Promise

TODO

#### connect(deviceId:String): Promise

```javascript
try {
  let connectedDevice = await RNBluetoothClassic.connect(device.id);
  this.setState({ connectedDevice });
} catch (error) {
  console.log(error.message);
}
```

#### accept(): Promise

```javascript
let connectedDevice = await RNBluetoothClassic.accept();
this.setState({ connectedDevice });
```

#### disconnect(): promise

```javascript
await RNBluetoothClassic.disconnect();
this.setState({ connectedDevice: undefined });
```

#### isConnected(): Promise

```javascript
let connectedDevice = await RNBluetoothClassic.isConnected();
if (connectedDevice) let device = RNBluetoothClassic.getConnectedDevice();
else console.log(`Not currently connected to a device`);
```

#### getConnectedDevice(): Promise

```javascript
let connectedDevice = await RNBluetoothClassic.getConnectedDevice();
if (connectedDevice)
  console.log(`Currently connected to ${connectedDevice.address}`);
else console.log(`Not currently connected to a device`);
```

#### write(message: String): Promise

```javascript
let message = this.state.text + "\r"; // Commands should end with \r
await RNBluetoothClassic.write(message);
```

#### readFromDevice(): Promise

```javascript
// Reads all content in the buffer - regardless of delimiter
let message = await RNBluetoothClassic.readFromDevice();
```

#### readUntilDelimiter(): Promise

```javascript
// Delimiter defaults to '\n' without setting manually
let message = await RNBluetoothClassic.readUntilDelimiter();
```

#### readUntilDelimiter(delimiter:String): Promise

```javascript
let message = await RNBluetoothClassic.readUntilDelimiter("~");
```

#### setDelimiter(String delimiter): Promise

```javascript
await RNBluetoothClassic.setDelimiter("~");
let message = RNBluetoothClassic.readUntilDelimiter();
```

#### available(): Promise

```javascript
let available = await RNBluetoothClassic.available();
if (available)
  let message = await RNBluetoothClassic.readFromDevice();  // All content or .readUntilDelimiter()
```

## Events

Attaching (and disconnecting) from events can be completed in the `componentWillMount` (`componentWillUnmount` respectively) using the following:

```javascript
componentWillMount() {
  this.onRead = RNBluetoothClassic.addListener(BTEvents.READ, this.handleRead, this);
}

componentWillUnmount() {
  this.onRead.remove();
}
```

| Event                          | Description                                                                                                                                                                                                                                                                                                                                                                                                                                                                |      Android       |        IOS         |
| ------------------------------ | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | :----------------: | :----------------: |
| BTEvent.BLUETOOTH_ENABLED      | When the platform enables the bluetooth adapter.                                                                                                                                                                                                                                                                                                                                                                                                                           | :white_check_mark: | :white_check_mark: |
| BTEvent.BLUETOOTH_DISABLED     | When the platform disables the bluetooth adapter.                                                                                                                                                                                                                                                                                                                                                                                                                          | :white_check_mark: | :white_check_mark: |
| BTEvent.BLUETOOTH_CONNECTED    | When a bluetooth device is connected. The event data contains information regarding the Device which was just connected. Generally a new `RNBluetoothModule.list()` should be completed at this time.                                                                                                                                                                                                                                                                      | :white_check_mark: | :white_check_mark: |
| BTEvent.BLUETOOTH_DISCONNECTED | When a bluetooth device is connected. The event data contains information regarding the Device which was just disconnected. Generally a new `RNBluetoothModule.list()` should be completed at this time.                                                                                                                                                                                                                                                                   | :white_check_mark: | :white_check_mark: |
| BTEvent.CONNECTION_SUCCESS     | When a connection request has been completed. Generally if you're calling `RNBluetoothModule.connect()` you shouldn't really need to subscribe to these, but if you want to there is not stopping it.                                                                                                                                                                                                                                                                      | :white_check_mark: | :white_check_mark: |
| BTEvent.CONNECTION_FAILED      | When connect() is called but fails. Again it generally isn't required if you're using the Promise version of `RNBluetoothModule.connect()`                                                                                                                                                                                                                                                                                                                                 | :white_check_mark: | :white_check_mark: |
| BTEvent.CONNECTION_LOST        | When an open connection is lost. This occurs when a BluetoothDevice which may have an open connection/stream turns itself off. On Android this will signify an error, but on IOS this could possibly happen if there is no activity. In most cases a `BTEvent.BLUETOOTH_DISCONNECTED` is also fired, in which case it may be easier to listen to that in order to change status.                                                                                           | :white_check_mark: | :white_check_mark: |
| BTEvent.BLUETOOTH_ENABLED      | `BTEvent.BLUETOOTH_ENABLED` is fired when the platform enables the bluetooth adapter.                                                                                                                                                                                                                                                                                                                                                                                      | :white_check_mark: | :white_check_mark: |
| BTEvent.READ                   | When new data is available. The current implementation is to publish any number of data in chunks based on the delimiter. For exapmle, if the delimiter is '\n' (default) and data comes in with three messages (three delmited messages) then the client will get three READ events which it should handle. In the future I hope I can move the reading logic from the `RNBluetoothModule` into an Interface/Protocol so that the client can call custom implementations. | :white_check_mark: | :white_check_mark: |
| BTEvent.ERROR                  | Any time an error (which is not classified above) occurs.                                                                                                                                                                                                                                                                                                                                                                                                                  | :white_check_mark: | :white_check_mark: |

### Listener Examples

#### BLUETOOTH_ENABLED

TODO

#### BLUETOOTH_DISABLED

TODO

#### BLUETOOTH_CONNECTED

TODO

#### BLUETOOTH_DISCONNECTED

TODO

#### CONNECTION_SUCCESS

TODO

#### CONNECTION_FAILED

TODO

#### CONNECTION_LOST

TODO

#### READ

TODO

#### ERROR

TODO
