import { NativeEventEmitter } from "react-native";
import RNBluetoothClassicModule, { StandardOptions } from "./BluetoothNativeModule";
import BluetoothDevice from "./BluetoothDevice";
import { BluetoothEventListener, StateChangeEvent, BluetoothDeviceEvent, BluetoothEventSubscription } from "./BluetoothEvent";
import { BluetoothDeviceReadEvent } from "./BluetoothEvent";
/**
 * Provides access to native module.  In general the methods will be direct calls
 * through to {@code NativeModules.RNBluetoothClassc}, although there are instances
 * where methods are overwritten where additional information is required.  These
 * methods are related to {@code BluetoothConnection} requests, where the response
 * must be wrapped.
 *
 * @author kenjdavidson
 */
export default class BluetoothModule {
    /**
     * Native RNBluetoothClassicModule provided from Java and IOS through
     * the NativeModules.
     *
     * @private
     */
    _nativeModule: RNBluetoothClassicModule;
    /**
     * NativeEventEmitter - the BluetoothModule used to extend this, but it became
     * apparent that I needed more customizable messaging between JS and Native that
     * the RCTEventEmitter couldn't manage.
     *
     * @private
     */
    _eventEmitter: NativeEventEmitter;
    constructor(nativeModule: RNBluetoothClassicModule);
    /**
     * Requests enabled status from the BluetoothAdapter.
     *
     * @return Promise resolved with whether Bluetooth is enabled
     */
    isBluetoothEnabled(): Promise<boolean>;
    /**
     * Requests whether there is data availabled on the Device.  At this point
     * this is a yes/no, it may be wise to turn it into number of bytes based
     * on devices that send messages of set size with no delimiter.
     *
     * @param address of the Device we wish to check
     * @return Promise resolved with whether data is available
     */
    availableFromDevice(address: string): Promise<number>;
    /**
     * Read from the specified device.  This uses the configured device read
     * functionality - see the Native documentation for how that is configured.
     *
     * @param address address from which to read
     * @return Promise resovled with individual read
     */
    readFromDevice(address: string): Promise<string>;
    /**
     * Clears the device buffer.
     *
     * @param address for which device we will clear
     * @return Promise resolved with whether clear was successful
     */
    clearFromDevice(address: string): Promise<boolean>;
    /**
     * Disconnect from device.
     *
     * @param address of Device we will disconnect
     * @return Promise resolved with disconnection success status
     */
    disconnectFromDevice(address: string): Promise<boolean>;
    /**
     * Determines whether device is currently connected.  Connected means that there
     * is an actual Socket open (not just pairing)
     *
     * @param address of which we are checking for connection
     * @returns Promise resolved with whether there is a connection
     */
    isDeviceConnected(address: string): Promise<boolean>;
    /**
     * Wraps the bonded native devices with a BluetoothDevice to allow for
     * event management.
     *
     * @return Promise containing array of pair devices
     */
    getBondedDevices(): Promise<BluetoothDevice[]>;
    /**
     * Wraps the connected native devices with a BluetoothDevice to allow for
     * event management.
     *
     * @return Promise resolved with array of connected devices
     */
    getConnectedDevices(): Promise<BluetoothDevice[]>;
    /**
     * Attempts to connect to the requested device.  Defaults the properties to an empty
     * map.
     *
     * @param address the address to which we are connecting
     * @param properties extra properties required for the connection.
     */
    connectToDevice<T extends StandardOptions>(address: string, options?: T): Promise<BluetoothDevice>;
    /**
     * Wraps connected NativeDevice.
     *
     * @param address the address to check for connection
     */
    getConnectedDevice(address: string): Promise<BluetoothDevice>;
    /**
     * Write data to the device.  Eventually this will be updated to accept data and type,
     * allowing the sending of different data elements to the device.  From the issues on
     * bluetooth-serial it seems like images and hex values are the top priorties, but method
     * to send any data would be preferable.
     *
     * @param address the address to which we will send data
     * @param message String or Buffer which will be sent
     */
    writeToDevice(address: string, message: any): Promise<boolean>;
    /**
     * Starts discovery on the bluetooth adatper.
     *
     * This is an Anroid only function.
     */
    startDiscovery(): Promise<BluetoothDevice[]>;
    /**
     * Cancels discovery.  If discovery was alreayd stopped, this will end gracefully
     * by resolving the promise.
     *
     * This is an Android only feature.
     */
    cancelDiscovery(): Promise<boolean>;
    /**
     * Pair the device request.
     *
     * This is an Android only feature.
     *
     * @param address address of the device we wish to pair
     */
    pairDevice(address: string): Promise<BluetoothDevice>;
    /**
     * Unpair the device request.
     *
     * This is an Android only feature.
     *
     * @param address address of the device we wish to unpair
     */
    unpairDevice(address: string): Promise<boolean>;
    /**
     * Attempt to start accepting connections.   Accepts only one connection at a time,
     * once this has been established the device is returned and accepting is disabled.
     *
     * This is an Android only feature.
     *
     * @param properties used during the connection and connected process(es)
     */
    accept(properties: Map<string, object>): Promise<BluetoothDevice>;
    /**
     * Attempt to cancel the accepting state.
     *
     * This is an Android only feature.
     */
    cancelAccept(): Promise<boolean>;
    /**
     * Request user to turn on Bluetooth Adapter
     *
     * This is an Android only feature.
     *
     * @param state
     */
    requestBluetoothEnabled(): Promise<boolean>;
    /**
     * Attempts to set the bluetooth adapter name.
     *
     * This is an Android only feature.
     *
     * @param name the name to which we will change BluetoothAdapter
     */
    setBluetoothAdapterName(name: string): Promise<boolean>;
    private createBluetoothEventSubscription;
    /**
     * Creates an EventSubscription which calls the provided listener when the native
     * device is notified of the BluetoothAdapter being enabled.
     *
     * @param listener
     */
    onBluetoothEnabled(listener: BluetoothEventListener<StateChangeEvent>): BluetoothEventSubscription;
    /**
     * Creates an EventSubscription which calls the provided listener when the native
     * device is notified of the BluetoothAdapter being disabled.
     *
     * @param listener
     */
    onBluetoothDisabled(listener: BluetoothEventListener<StateChangeEvent>): BluetoothEventSubscription;
    /**
     * Creates an EventSubscription which wraps both enabled and disabled.
     *
     * @param listener
     */
    onStateChanged(listener: BluetoothEventListener<StateChangeEvent>): BluetoothEventSubscription;
    /**
     * Creates an EventSubscription which wraps the DEVICE_CONNECTED event type.
     *
     * @param listener
     */
    onDeviceConnected(listener: BluetoothEventListener<BluetoothDeviceEvent>): BluetoothEventSubscription;
    /**
     * Creates an EventSubscription which wraps the DEVICE_DISCONNECTED event type.  Device disconnected events
     * can be thrown for the following:
     * - During a read the DeviceConnection receives an un-cancelled exception (generally a closure)
     * - The AclReceiver receives an on disconnect (this seems less informative as it will still fire a disconnect
     * event if the connect had been cancelled.  So at this point it may need to be removed.)
     *
     * @param listener
     */
    onDeviceDisconnected(listener: BluetoothEventListener<BluetoothDeviceEvent>): BluetoothEventSubscription;
    /**
     * Creates an EventSubscription based on the read event from a specified device.  If the device
     * is not currently connected an exception will be thrown, although I'm not sure if
     * this is required, since it may be annoying to continually add/remove subscriptions.
     *
     * @param listener
     */
    onDeviceRead(address: string, listener: BluetoothEventListener<BluetoothDeviceReadEvent>): BluetoothEventSubscription;
    /**
     * Creates an EventSubscription which wraps the ERROR event.
     *
     * @param listener
     */
    onError(listener: BluetoothEventListener<BluetoothDeviceEvent>): BluetoothEventSubscription;
    /**
     * Creates an event subscription wrapping the DEVICE_DISCOVERED events.  DEVICE_DISCOVERED is fired during the
     * discovery process, when a new device is found.  Note this is only fired on the first discovery, it will not
     * be fired (at this point) with the updated RSSI value on the next device discovery.
     *
     * Remember to remove the subscription when you've found your device, or you stop discovery.
     *
     * @param listener
     */
    onDeviceDiscovered(listener: BluetoothEventListener<BluetoothDeviceEvent>): BluetoothEventSubscription;
}
