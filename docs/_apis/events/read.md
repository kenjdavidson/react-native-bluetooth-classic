---
title: READ
categories: events
platforms:
  - android
  - apple
---

The read event occurs when a connected device receives data, which can be parsed to contain one or many delimited messages.  As such, this requires that a delmiter is set (although it some may want to have data be a constant stream, this isn't currently available).  The *READ* event should generally not be used on conjunction with the `read` and `readUntil` functions, as this could cause some problem with data - although it hasn't been tested nor disabled - it's just something to watch for.

##### Event

The `BluetoothMessage` default content is:

{:.table.table-striped.table-sm}
| Field | Type | Description |
| --- | --- | --- |
| device | BluetoothDevice | A bluetooth device implementation containing common and platform specific data |
| timestamp | Date | The date at which this message was received - was a requirement when the device was scanning but no READ listener was enabled |
| data | string | The current data format is a string, although it's a goal to have this customizable as others have requested different encodings (Base64, ByteArray, etc) |

##### Example

```javascript
componentDidMount() {
  this.onRead = RNBluetoothClassic.addListener(
    BTEvents.READ,
    this.handleRead,
    this,
  );
}

componentWillUnmount() {
  this.onRead.remove();
  RNBluetoothClassic.disconnect();
}
```