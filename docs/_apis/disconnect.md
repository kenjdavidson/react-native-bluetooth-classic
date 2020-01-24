---
title: disconnect
categories: device
platforms:
  - android
  - apple
---

Disconnects from the current device - currently there is only one device connection limit.

``` javascript
disconnect() : Promise
```

##### Parameters

{:.table.table-striped.table-sm}
| Name | Type | Description|
| N/A | | |

##### Promise

- Resolves `true` when disconnected.
- Rejects `BTError` if:
  - the Bluetooth adapter is not enabled
  - there is an exception while attempting to disconnect

##### Examples

``` javascript
try {
  await RNBluetoothClassic.disconnect();
  this.setState({connectedDevice: undefined});
} catch (error) {  
  Toast.show({
    text: `Disconnection from ${device.name} unsuccessful`,
  });
}
```

