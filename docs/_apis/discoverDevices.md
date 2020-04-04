---
title: discoverDevices
categories: adapter
platforms:
  - android
---

Requests that the Bluetooth adapter attempt to discover new devices.  This functionality is only available on **Android** as IOS requires that MFi devices are connected to through the system settings.

``` javascript
discoverDevices() : Promise
```

##### Parameters

{:.table.table-striped.table-sm}
| Name | Type | Description|
| N/A |  |  |

##### Promise

- Resolves `[NativeDevice]`. 
- Rejects `BTError` if:
  - the Bluetooth adapter is disabled
  - the discovery was cancelled
  - there was an issue during discovery

The `NativeDevice` contains the added fields:
- RSSI (of the last reading)

Todo - Eventually the Device discovery should not be monitored through a promise, but rather an Event with each device being provided.  This would allow clients to update their UI with live RSSI data (which is becoming more requested).

##### Examples

``` javascript
async initialize() {
  try {
    let unpaired = await RNBluetoothClassic.discoverDevices();
    this.setState({unpaired});
  } catch (err) {
    Toast.show({ text: err.message });
  }
}
```

