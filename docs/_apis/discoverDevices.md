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

- Resolves `[BTDevice]` containing the unpaired devices (could be empty).
- Rejects `BTError` if:
  - the Bluetooth adapter is disabled
  - the discovery was cancelled
  - there was an issue during discovery

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

