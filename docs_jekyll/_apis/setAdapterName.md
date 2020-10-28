---
title: setAdapterName
categories: adapter
platforms:
  - android
---

Sets a new Bluetooth adapter name.

``` javascript
setAdapterName(name:string) : Promise
```

##### Parameters

{:.table.table-striped.table-sm}
| Name | Type | Description|
| name| string | The new name for the Bluetooth adapter. |

##### Promise

- Resolves `true`

##### Examples

``` javascript
let size = await RNBluetoothClassic.setAdapterName('RNBTAdapter');
```

