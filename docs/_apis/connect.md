---
title: connect
categories: device
platforms:
  - android
  - apple
---

Attempts to connect to a Bluetooth device.

``` javascript
connect(deviceId:string) : Promise
```

##### Parameters

{:.table.table-striped.table-sm}
| Name | Type | Description|
| deviceId | string | The id of the device to which we will attempt to connect. |

##### Promise

- Resolves `BTDevice`
- Rejects `BTError` if:
  - the Bluetooth adapter is not enabled
  - there is an exception while attempting to connect

##### Examples

``` javascript
try {
  let connectedDevice = await RNBluetoothClassic.connect(deviceId);
  this.setState({connectedDevice});
} catch (error) {
  console.log(error.message);
  Toast.show({
    text: `Connection to ${device.name} unsuccessful`,
    duration: 3000,
  });
}
```

