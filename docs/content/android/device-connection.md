---
title: Device Connection
description: 
summary: "Manage communication between Android and BluetoothDevice"
---

`DeviceConnection`(s) handle the communication with the device.  Once the connection is established from the `Connector` or `Acceptor` the appropriate/requested connection is started.

## AbstractDeviceConnection

A default implementation of the `DeviceConnection` providing some standard processing:

###### Parameters

`READ_SIZE` - the number of bytes to be read during a read request.  Defaults to `1024`.

`READ_TIMEOUT` - the timeout for read operations. Defaults to `0`.

## DelimitedStringDeviceConnectionImpl

A `DeviceConnection` implementation which attempts to chunk incoming data by the requested `delimter`.   Data is read from the connection in the context of delimited messages rather than bytes.

### Usage

This is the default connection type when none is provided during connection:

```
let connectedDevice = device.connect({
  CONNECTION_TYPE: 'delimited'
});
```

###### Parameters

All parameters provided for `AbstractDeviceConnectionImpl` +

`DELIMITER` - the delimiter by which data will be split into messages.  Defaults to `\n`.  Passing in an empty delimiter will cause the full buffer to be sent on `read()` or `onDataReceived` - this is an opt in that was requested and added.s

```
let connectedDevice = device.connect({
  DELIMITER: '\n'
});
```

`DEVICE_CHARSET` - the character set that to which data will be encoded.  Defaults to `ascii`.

```
let connectedDevice = device.connect({
  DEVICE_CHARSET: 'ascii'
});
```

## ByteArrayDeviceConnectionImpl

`DeviceConnection` implementation for passing binary/byte array data.

### Usage

Since `1.60.1-rc18` this is now available without any custom configuration:

```
let connectedDevice = device.connect({
  CONNECTION_TYPE: 'binary'
})
```

###### Parameters

All parameters provided for `AbstractDeviceConnectionImpl` +
