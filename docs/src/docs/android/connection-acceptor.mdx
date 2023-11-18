---
title: Connection Acceptor
description: 
summary: "Provide implementation for the Android device to accept connections"
---

The `ConnectionAcceptor` is an abstract class that provides the basis for accepting connections from remote peripherals.  All `ConnectionAcceptor`(s) require the `BluetoothAdapter` and a set of `Properties` used during connection.

> This should have probably been an interface extending `Runnable` and used that way.  But at this point it seems to be working well, and customizable.  If things come up where it's required, it can be changed for v2.x.x

## ConnectionAcceptor

`protected ConnectionAcceptor(BluetoothAdapter bluetoothAdapter, Properties properties)`

Creates an instance of the parent ConnectionAcceptor providing the Android BluetoothAdapter and connection properties.

###### Parameters

`bluetoothAdapter` - the Android `BluetoothAdatper`

`properties` - properties used customize how connections are accepted

### addListener

`public void addListener(AcceptorListener<BluetoothSocket> listener)`

Provides the ConnectionAcceptor with listener which is notified once a connection is accepted or errored.

###### Parameters

`listener` - the listener to be notified upon successful connection or error

### connect

`public BluetoothSocket connect(Properties properties)`

Accepts to accept connection(s) using the provided properties.

###### Parameters

`properties` - the properties provided during creation

### cancel

`public void cancel()`

Attempts to cancel the accepting process.

### notifyListeners

`protected void notifyListeners(BluetoothSocket result)`

Notifies the listener of a successful connection, by passing the BluetoothSocket.

###### Parameters

`result` - the successful BluetoothSocket

### notifyListeners

`protected void notifyListeners(Exception e)`

Notifies the listener of an error that occurred during the accept attempt.

###### Parameters

`result` - the successful BluetoothSocket

### run

`public void run()`

Implements the `Thread` run method.  Calls the `connect` method during processing and either notifies the provided listener of a successful connection (BluetoothSocket) or Exception.

## ConnectionAcceptor.AcceptorListener

`public interface AcceptorListener`

The acceptor listener allows the RNBluetoothClassicModule to interact with the accepted connection or handle any errors that might have occurred.

### success

`void success(BluetoothSocket socket)`

Provides an openned BluetoothSocket to the implementing listener.

###### Parameters

`socket` - the BluetoothSocket which was accepted by the BluetoothServerSocket

### failure

`void failure(Exception e)`

Notifies the implementing listener of a failed accept attempt.

###### Parameters

`e` - the Exception causing the failure

## RfcommConnectionAcceptorThreadImpl

> Thread just got left in there from the original implementation

Provides an implemenation of the `ConnectionAcceptor` which opens a `BluetoothServerSocket` using **rfcomm** and the **SPP** service record.  This only accepts a single connection and then returns; if it's found that it would be better to allow multiple connections it can be discussed on the best way in which to perform that.

###### Properties

`SECURE_SOCKET` - boolean value allowing the selection of listening using a secure or insecure server socket.  The default is `true`.

`SERVICE_NAME` - the name in which the BluetoothServerSocket will broadcast.  The default is `RNBluetoothClassic`.

