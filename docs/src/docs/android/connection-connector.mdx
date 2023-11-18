---
title: Connection Connector
description: 
summary: "Implement connection processes for specified device types"
---

The `ConnectionConnector` is an abstract class that provides the basis for connecting to remote peripherals. All `ConnectionConnectors`(s) require the requested `BluetoothDevice` and a set of connection `Properties`.

> This should have probably been an interface extending `Runnable` and used that way.  But at this point it seems to be working well, and customizable.  If things come up where it's required, it can be changed for v2.x.x

## ConnectionConnector

`protected ConnectionConnector(BluetoothDevice device, Properties properties)`

Creates an instance of the ConnectionConnector parent which will be used to connect to the requested device, using the provided properties.

###### Parameters

`device` - the BluetoothDevice to which the connection is to be attempted

`properties` - set of Properties used for connection attempt

### addListener

`public void addListener(AcceptorListener<BluetoothSocket> listener)`

Provides the ConnectionConnector with a listener which is notified once a connection is successful or errored.

###### Parameters

`listener` - the listener to be notified upon successful connection or error

### connect

`public BluetoothSocket connect(Properties properties)`

Accepts to use during connection process.

###### Parameters

`properties` - the properties provided during creation

### cancel

`public void cancel()`

Attempts to cancel the connection attempt.  In most cases this doesn't do much, since the connection either happens or times out (at a reasonable time).

### notifyListeners

`protected void notifyListeners(BluetoothSocket result)`

Notifies the listener of a successful connection, by passing the BluetoothSocket.

###### Parameters

`result` - the successful BluetoothSocket

### notifyListeners

`protected void notifyListeners(Exception e)`

Notifies the listener of an error that occurred during the connection attempt.

###### Parameters

`result` - the successful BluetoothSocket

### run

`public void run()`

Implements the `Thread` run method.  Calls the `connect` method during processing and either notifies the provided listener of a successful connection (BluetoothSocket) or Exception.

## ConnectionConnector.ConnectorListener

`public interface ConnectorListener`

The conector  listener allows the RNBluetoothClassicModule to interact with the accepted connection or handle any errors that might have occurred.

### success

`void success(BluetoothSocket socket)`

Provides an openned BluetoothSocket to the implementing listener.

###### Parameters

`socket` - the BluetoothSocket which was accepted by the BluetoothServerSocket

### failure

`void failure(Exception e)`

Notifies the implementing listener of a failed connection attempt.

###### Parameters

`e` - the Exception causing the failure

## RfcommConnectionConnectorThreadImpl

> Thread just got left in there from the original implementation

Provides an implementation which performs connecting to a remote device using an **rfcomm** BluetoothSocket and the **SPP** service record.

###### Parameters

`SECURE_SOCKET` - boolean value allowing the selection of listening using a secure or insecure socket.  The default is `true`.
