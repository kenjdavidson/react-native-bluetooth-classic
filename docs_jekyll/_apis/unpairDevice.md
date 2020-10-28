---
title: unpairDevice
categories: adapter
platforms:
  - android
---

Attempts to unpair with a device identified by the unique device id.  This functionality is only available on **Android** as IOS requires that MFi devices are connected to through the system settings.

``` javascript
pairDevice(deviceId:string) : Promise
```

##### Parameters

{:.table.table-striped.table-sm}
| Name | Type | Description|
| deviceId | string | The device id for which we will attempt to connect. |

##### Promise

- Resolves [`true` | `false`] based on whether the unpair was successful.
- Rejects `BTError` if:
  - the Bluetooth adapter is not enabled
  - there is an error while attempting to unpair

##### Examples

``` javascript
// TODO add pairDevice example
```

