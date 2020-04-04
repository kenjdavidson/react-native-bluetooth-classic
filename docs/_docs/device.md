---
chapter:
  title: API
  index: 3
title: Device
---

`RNBluetoothClassic` attempts to standardize the native Android and IOS implementations of Bluetooth Devices/Peripherals for communication and access within React Native.

## Android NativeDevice

Android maintains device information and communication by wrapping the `BluetoothDevice` within a `NativeDevice` object.  The `NativeDevice` is the direct tie to serialization to React Native.  At it's base it defines the following:

```json
{
  name: 'Device Name',
  address: 'AA:AA:AA:AA:AA',

  extra: {
    // See specific API calls for added Intent extras
  }
}
```

## IOS



## API

{% include apiaccordion.html id="device" categories="device" %}
