---
chapter:
  title: API
  index: 3
title: Device
---

Once the Bluetooth adapter has been turned on and configured you can start attempting to pair and connect with Bluetooth devices.  Currently there is only one connection available at a time, so these functions are actually called against the `RNBluetoothClassic` module.  

When multiple device connections are competed, these will be used against the `BTConnectedDevice` or whatever naming is used.

{% include apiaccordion.html id="device" categories="device" %}
