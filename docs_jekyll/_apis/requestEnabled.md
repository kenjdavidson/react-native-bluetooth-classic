---
title: requestEnabled
categories: adapter
platforms:
  - android
---

Requests that the Bluetooth adapter be enabled. This functionality is currently only available on **Android**, on IOS users must be directed to their Bluetooth settings.

``` javascript
requestEnabled(): Promise
```

##### Parameters

{:.table.table-striped.table-sm}
| Name | Type | Description|
| N/A |  |  |

##### Promise

* Resolves `true` | `false` based on whether Bluetooth is enabled successfully.
* Rejects if:
    - there is an error while attempting to enabled Bluetooth
    - the platform is IOS

##### Examples

``` javascript
async initialize() {
  try {
    await RNBluetoothClassic.requestEnabled();
  } catch (err) {
    Toast.show({ text: err.message });
  }
}
```

