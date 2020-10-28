---
title: getConnectedDevice
categories: adapter
platforms:
  - android
  - apple
---

Gets the currently connected Bluetooth device.

``` javascript
getConnectedDevice() : Promise
```

##### Parameters

{:.table.table-striped.table-sm}
| Name | Type | Description|
| N/A | | |

##### Promise

- Resolves `BTDevice`
- Rejects `BTError` if no device connected

##### Examples

``` javascript
try {
  let device = RNBluetoothClassic.getConnectedDevice();
} catch(err) {
  console.log(err);
}
```

