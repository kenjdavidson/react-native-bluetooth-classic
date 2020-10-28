---
title: available
categories: device
platforms:
  - android
  - apple
---

Checks to see if there are any available `byte[]` on the Device buffer.   This can be used to determine how much data is available prior to reading.  With the way in which the read functions work with Promises and data, this function may not be that useful, but it's a hold over from the original library.

TODO - look into deprecating for removal

``` javascript
available() : Promise
```

##### Parameters

{:.table.table-striped.table-sm}
| Name | Type | Description|
| N/A | | |

##### Promise

- Resolves `int`, the length of data which available.

##### Examples

``` javascript
let size = await RNBluetoothClassic.available();
if (size > 0) {
  // Perform reading
}

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

