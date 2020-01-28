---
title: cancelAccept
categories: adapter
platforms:
  - android
---

Attempts to cancel the Bluetooth service accept mode.  This functionality is only available on **Android** as IOS requires that MFi devices are connected to through the system settings.

``` javascript
cancelAccept() : Promise
```

##### Parameters

{:.table.table-striped.table-sm}
| Name | Type | Description|
| N/A |  |  |

##### Promise

- Resolves `true` if the accept mode was cancelled.
- Rejects `BTError` if:
  - the Bluetooth adapter is disabled
  - the accept mode was not currently active

##### Examples

``` javascript
async cancelAcceptConnections() {
  console.log("Attempting to cancel accepting...");
  
  try {
    await RNBluetoothClassic.cancelAccept();
    this.setState({ connectedDevice: undefined, isAccepting: false });
  } catch(error) {
    console.log(error);
    this.refs.toast.show(
      `Unable to cancel client accept`,
      DURATION.LENGTH_SHORT
    );
  }
}
```

