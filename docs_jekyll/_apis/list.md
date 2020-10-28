---
title: list
categories: adapter
platforms:
  - android
  - apple
---

Requests a list of all the devices currently connected (IOS) or paired (Android) with the device.

``` javascript
list() : Promise
```

##### Parameters

{:.table.table-striped.table-sm}
| Name | Type | Description|
| N/A |  |  |

##### Promise

- Resolves `[BTDevice]` containing the paired devices (could be empty).
- Rejects `BTError` if:
  - the Bluetooth adapter is disabled
  - there was an issue during listing

##### Examples

``` javascript
async initialize() {
  try {
    let devices = await RNBluetoothClassic.list();
    this.setState({connectedDevices: devices});
  } catch (err) {
    Toast.show({ text: err.message });
  }
}
```

