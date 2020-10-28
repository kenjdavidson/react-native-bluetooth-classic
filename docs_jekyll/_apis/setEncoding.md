---
title: setEncoding
categories: device
platforms:
  - android
  - apple
---

Encoding accepts the `BTCharsets` value and configures the Charset to use when parsing `byte[]` data into String.  Internally all data is parsed as `String`s, whether this is a good or bad thing, it's the way the original projects were developed and the way this will continue until I have time to allow customization.  For that reason encoding plays a very important role, as I found out while while parsing data on IOS (as UTF8) and receiving different data than I was on Android (as ISO_8859_1).  

```javascript
setEncoding(encoding:BTCharsets) : Promise 
```

##### Parameters

{:.table.table-striped.table-sm}
| Name | Type | Description |
| encoding | BTCharsets | The native  [Charset](https://developer.android.com/reference/java/nio/charset/Charset) or  [String.Encoding](https://developer.apple.com/documentation/swift/string/encoding) for Android or IOS (respectively). |

##### Promise

- Resolves `true` when the Encoding is set
- Rejects with `BTError` if the requested `BTCharsets` cannot map to a native value

##### Examples

```javascript
async initialize() {
  await RNBluetoothClassic.setEncoding(BTCharset.LATIN);
}
```

##### BTCharsets

```javascript
BTCharsets: {
  LATIN,
  ASCII,
  UTF8,
  UTF16
}
```