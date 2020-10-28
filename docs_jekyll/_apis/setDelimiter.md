---
title: setDelimiter
categories: device
platforms:
  - android
  - apple
---

The delimiter (default `\n` ) is used for parsing the converted `String` data to determine if there are any full messages available. In most cases (unless streaming) a device will send a block of information consisting of a single or multiple messages.

``` javascript
setEncoding(delimiter: String): Promise
```

##### Parameters

{:.table.table-striped.table-sm}
| Name | Type | Description|
| delimiter | string | The delimiter to use while parsing individual messages |

##### Promise

* Resolves `true` .

##### Examples

``` javascript
async initialize() {
  await RNBluetoothClassic.setDelimiter(":");
}
```

