---
layout: default
title: Getting Started
---

**React Native Bluetooth Classic** was developed to bridge the gap between the available React Native Bluetooth modules such as:

- [https://github.com/rusel1989/react-native-bluetooth-serial](https://github.com/rusel1989/react-native-bluetooth-serial)
- [https://github.com/nuttawutmalee/react-native-bluetooth-serial-next](https://github.com/nuttawutmalee/react-native-bluetooth-serial-next)

in that both of these libraries use Bluetooth Classic for Android, but fall back to BLE on IOS.  The hardware in which my company was working would only communicate over Bluetooth Classic and required communication through the [External Accessory](https://developer.apple.com/documentation/externalaccessory) framework.  Please note that while working with this library on IOS, you'll need to become accustomed with the Apple and their [MFi program](https://en.wikipedia.org/wiki/MFi_Program).  Here are some links to check out:

- [https://en.wikipedia.org/wiki/MFi_Program](https://en.wikipedia.org/wiki/MFi_Program)
- [https://developer.apple.com/library/archive/samplecode/EADemo/Introduction/Intro.html](https://developer.apple.com/library/archive/samplecode/EADemo/Introduction/Intro.html)

The Android functionality is pretty common and well documented, there are a number of examples which can be reviewed.

## React Native Versions

Although it's probably not required, there are two versions of the project being maintained for the time being to allow for any major differences with the advent of **v0.60.0** of React Native.  This table probably isn't complete, and may need some updating as things go on, but I'll try to document the matching React Native, bluetooth classic and 

{:.table.table-striped.table-sm}
| Version | React Native | Android | IOS |
| --- | --- | --- | --- | --- |
| 0.9.x | 0.59.9 | > 4.1 (16) | > IOS 9 | 
| 0.10.x | 0.60.0 | > 4.1 (16) | > IOS 9 |

For the purpose of this documentation only **0.10.x** will be used moving forward.

## Notable Changes

**0.10.6** 
- Added listening functionality - The [`accept`](https://kenjdavidson.github.io/react-native-bluetooth-classic/adapter/#accept) call will start a `BluetoothServerSocket` which will wait (indefinitely) for a client to connect.

## Installation

Installation, like almost everything, is done through `npm`:

{% highlight shell %}
$ npm install react-native-bluetooth-classic --save
{% endhighlight %}

Once installed [autolinking](https://github.com/react-native-community/cli/blob/master/docs/autolinking.md) will take over and all that is required is starting the application as normal.  Be sure to check out the previous link if you need to customize or disable the autolinking of this (or any) module.

## Usage

There are there imports available with the `RNBluetoothClassic` module:

{% highlight javascript %}
import RNBluetoothClassic, {
  BTEvents,
  BTCharsets,
} from 'react-native-bluetooth-classic';
{% endhighlight %}

where:

#### RNBluetoothClassic

All API calls are return a `Promise` for the purpose of keeping Android and IOS inline.   A secondary goal of the project was to ensure that there is no more `Platform.IOS` checking nor `*.[android|ios].js` files.

{:.table.table-striped.table-sm}
| API | Description | Android | IOS |
| --- | --- | :---: | :---: |
| requestEnabled() | Requests that the environment enables the Bluetooth adapter. | :white_check_mark: | :no_entry: |
| isEnabled() | Whether Bluetooth is enabled on the platform. | :white_check_mark: | :white_check_mark: |
| list() | Lists the available paired (Android) or connected (IOS) devices. | :white_check_mark: | :white_check_mark: |
| discoverDevices() | Requests that the system discover new devices. | :white_check_mark: | :no_entry: |
| cancelDiscovery() | Attempts to cancel the previous discovery request. | :white_check_mark: | :no_entry: |
| pairDevice(deviceId:String) | Attempts to pair a device. | :white_check_mark: | :no_entry: |
| unpairDevice(deviceId:String) | Attempts to unpair a device. | :white_check_mark: | :no_entry: |
| accept() | Places the device/application into server accept connection mode - currently only accepts one connection at a time.  Any subseqeuent calls will be ignored, if the device is already accepting connections. | :white_check_mark: | :no_entry: |
| cancelAccept() | Attempts to cancel the previously requested server accept mode. | :white_check_mark: | :no_entry: |
| connect(deviceId:String) | Attempts to establish a communication connection with a device. | :white_check_mark: | :white_check_mark: |
| disconnect() | Attempts to disconnected from a device. | :white_check_mark: | :white_check_mark: |
| isConnected() | Whether or not there is currently a device connected. | :white_check_mark: | :white_check_mark: |
| getConnectedDevice() | Gets the currently connected device. | :white_check_mark: | :white_check_mark: |
| write(message: String) | Attempts to write to the currently connected device. | :white_check_mark: | :white_check_mark: |
| readFromDevice() | Resolves with the entire content of the devices buffer, ignoring any delimiters and clearing the buffer when complete.  This method should not be used in conjunction with the BTEvent.READ event as it could cause some unexpected behaviour. | :white_check_mark: | :white_check_mark: |
| readUntilDelimiter() | Resovles with the content of the buffer up until the default delimiter.  To update the delimiter for the session use setDelimiter(delimiter:String).  This method should not be used in conjunction with the BTEvent.READ event as it could cause some unexpected behaviour. | :white_check_mark: | :white_check_mark: |
| readUntilDelimiter(delimiter:String) | Resolves with the content of the buffer up until the provided delimiter.  This method should not be used in conjunction with the BTEvent.READ event as it could cause some unexpected behaviour. | :white_check_mark: | :white_check_mark: |
| setDelimiter(delimiter:String) | Sets the new delimiter to be used for reading and event parsing. | :white_check_mark: | :white_check_mark: |
| setEncoding(delimiter:BTCharsets) | Sets the character encoding for parsing Bluetooth data.  Android uses String encoding values while IOS uses Encoding UInt8 values, which are correctly mapped to `BTCharsets`. | :white_check_mark: | :white_check_mark: |
| available() | Whether there is currently data available on the current device, used in conjunction with the read[until\|from] functions. | :white_check_mark: | :white_check_mark: |


{:.primary}
> Currently only one device can be connected at a time, which is apparent based on the communication functions.  A goal is to have multiple devices available as soon as possible.

#### Events

Events are fired through the lifecycle of the `RNBluetoothClassic` module as configuration, devices and connectivity are changed:

{:.table.table-striped.table-sm}
| Event | Description | Android | IOS
| --- | --- | :---: | :---: |
| BTEvent.BLUETOOTH_ENABLED | When the platform enables the bluetooth adapter. | :white_check_mark: | :white_check_mark: |
| BTEvent.BLUETOOTH_DISABLED | When the platform disables the bluetooth adapter. | :white_check_mark: | :white_check_mark: |
| BTEvent.BLUETOOTH_CONNECTED | When a bluetooth device is connected.  The event data contains information regarding the Device which was just connected.  Generally a new `RNBluetoothModule.list()` should be completed at this time. | :white_check_mark: | :white_check_mark: |
| BTEvent.BLUETOOTH_DISCONNECTED |  When a bluetooth device is connected.  The event data contains information regarding the Device which was just disconnected.  Generally a new `RNBluetoothModule.list()` should be completed at this time. | :white_check_mark: | :white_check_mark: |
| BTEvent.CONNECTION_SUCCESS | When a connection request has been completed.  Generally if you're calling `RNBluetoothModule.connect()` you shouldn't really need to subscribe to these, but if you want to there is not stopping it. | :white_check_mark: | :white_check_mark: |
| BTEvent.CONNECTION_FAILED | When connect() is called but fails.  Again it generally isn't required if you're using the Promise version of `RNBluetoothModule.connect()` | :white_check_mark: | :white_check_mark: |
| BTEvent.CONNECTION_LOST | When an open connection is lost.  This occurs when a BluetoothDevice which may have an open connection/stream turns itself off.  On Android this will signify an error, but on IOS this could possibly happen if there is no activity.  In most cases a `BTEvent.BLUETOOTH_DISCONNECTED` is also fired, in which case it may be easier to listen to that in order to change status. | :white_check_mark: | :white_check_mark: |
| BTEvent.BLUETOOTH_ENABLED | `BTEvent.BLUETOOTH_ENABLED` is fired when the platform enables the bluetooth adapter. | :white_check_mark: | :white_check_mark: |
|BTEvent.READ | When new data is available.  The current implementation is to publish any number of data in chunks based on the delimiter.  For exapmle, if the delimiter is '\n' (default) and data comes in with three messages (three delmited messages) then the client will get three READ events which it should handle.  In the future I hope I can move the reading logic from the `RNBluetoothModule` into an Interface/Protocol so that the client can call custom implementations. | :white_check_mark: | :white_check_mark: |
|BTEvent.ERROR | Any time an error (which is not classified above) occurs. | :white_check_mark: | :white_check_mark: |


{:.warning}
> In most cases BTEvents.READ should not be used in conjunction with manual reading.  Doing so will result in some unexpected or empty manual reads.

## Contribution

I'm always looking to improve upon the the project.  Whether it be in terms of submitting [issues](https://github.com/kenjdavidson/react-native-bluetooth-classic/issues) or [features](https://github.com/kenjdavidson/react-native-bluetooth-classic/issues); submitting pull requests; or suggesting some best practices that could be implemented (specifically with regards to IOS, as it's definitely not within my comfort zone, yet).



