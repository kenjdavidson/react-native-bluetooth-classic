---
title: accept
categories: adapter
platforms:
  - android
---

Requests that the Bluetooth service is placed into accept connection mode, making it available to other devices.  Multiple requests to this function will be ignored - and should be restarted for numerous expected devices.  Once this has been tested and the need arises, the goal is to have a `ConnectionConfiguration.expectedDevices` which would allow for grouping of clients.  This functionality is only available on **Android** as IOS requires that MFi devices are connected to through the system settings.

``` javascript
accept() : Promise
```

##### Parameters

{:.table.table-striped.table-sm}
| Name | Type | Description|
| N/A |  |  |

##### Promise

- Resolves `[BTDevice]` containing the connected devices.  It's possible that the resolve is `undefined` - if the accept was cancelled gracefully (this will probably be changed).
- Rejects `BTError` if:
  - the Bluetooth adapter is disabled
  - the accept connection times out or fails

##### Examples

``` javascript
async acceptConnections() {
  console.log("App is accepting connections now...");
  this.setState({ isAccepting: true });

  try {
    let connectedDevice = await RNBluetoothClassic.accept();

    if (connectedDevice) {  // Undefined if cancelled
      this.setState({ connectedDevice, isAccepting: false });
    }      
  } catch(error) {
    console.log(error);
    this.refs.toast.show(
      `Unable to accept client connection`,
      DURATION.LENGTH_SHORT
    );
    this.setState({ isAccepting: false });
  }
}
```

