---
title: isConnected
categories: adapter
platforms:
  - android
  - apple
---

Whether the application is currently connected to a device - note there is only one device limit at the moment, but the goal is to expand upon this.

``` javascript
isConnected() : Promise
```

##### Parameters

{:.table.table-striped.table-sm}
| Name | Type | Description|
| N/A | | |

##### Promise

- Resolves `true` | `false` 

##### Examples

``` javascript
  let connected = RNBluetoothClassic.isConnected();
}
```

