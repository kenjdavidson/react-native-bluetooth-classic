---
title: isEnabled
categories: adapter
platforms:
  - android
  - apple
---

Requests whether the Bluetooth adapted is enabled.  This call is available on both Android (BluetoothAdapter) and IOS (CoreBluetooth).

``` javascript
isEnabled() : Promise
```

##### Parameters

{:.table.table-striped.table-sm}
| Name | Type | Description|
| N/A |  |  |

##### Promise

- Resolves `true` | `false` based on whether Bluetooth is enabled.

##### Examples

``` javascript
async initialize() {
  let enabled = await RNBluetoothClassic.isEnabled();
  if (!enabled) {
      Toast.show({text: 'Bluetooth services are not enabled'});
      return;
  }
}
```

