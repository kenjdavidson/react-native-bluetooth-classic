import { NativeEventEmitter, EmitterSubscription } from 'react-native';
import RNBluetoothClassicModule, { BluetoothNativeModule } from './BluetoothNativeModule';
import BluetoothDevice from './BluetoothDevice';
/**
 * Provides access to native module.  In general the methods will be direct calls
 * through to {@code NativeModules.RNBluetoothClassc}, although there are instances
 * where methods are overwritten where additional information is required.  These
 * methods are related to {@code BluetoothConnection} requests, where the response
 * must be wrapped.
 *
 * @author kendavidson
 */
export default class BluetoothModule implements BluetoothNativeModule {
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
    isBluetoothEnabled(): Promise<Boolean>;
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
    clearFromDevice(address: string): Promise<Boolean>;
    /**
     * Disconnect from device.
     *
     * @param address of Device we will disconnect
     * @return Promise resolved with disconnection success status
     */
    disconnectFromDevice(address: string): Promise<Boolean>;
    /**
     * Determines whether device is currently connected.  Connected means that there
     * is an actual Socket open (not just pairing)
     *
     * @param address of which we are checking for connection
     * @returns Promise resolved with whether there is a connection
     */
    isDeviceConnected(address: string): Promise<Boolean>;
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
    connectToDevice(address: string, properties?: Map<string, Object>): Promise<BluetoothDevice>;
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
    writeToDevice(address: string, message: any): Promise<Boolean>;
    /**
     * Override the NativeEventEmitter#addListener method providing functionality for
     * Android.  I felt this was important as I didn't want clients to have to determine
     * which platform to use the event listening features of React.
     *
     * When adding a listener for READ event, it's required to include the address in
     * the string - READ:address - or else an exception will be thrown.  Since React Native
     * RCTEventEmitter had no way to do this normally this needed to be improvised.  This should
     * be improved, but I need to spend some time getting used to the React Native
     * functionality.
     *
     * @param {string} eventName to which the listener will be attached, if this is for read
     *    it should be READ:address
     * @param {function} handler which will be called on event
     * @param {object} context optional context object of the listener
     */
    addListener(eventType: string, listener: (...args: any[]) => any, context?: any): EmitterSubscription;
    /**
     * Remove all the listeners for an eventName.  This should probably never be called, as
     * I'm not entirely sure the upstream effects after the latest changes.  If this were to
     * remove all READ listeners, we would need to loop through all Devices and remove
     * their listeners.
     *
     * @param eventType which will have all it's listeners removed
     *
     */
    removeAllListeners(eventType: string): void;
    /**
     * Remove the subscription - this is actually called from subscription.remove()
     * and provides a way for determining if we have any BTEvents.READ event still.
     *
     * @param {Subscription} subscription the subscription to be removed
     */
    removeListener(subscription: EmitterSubscription): void;
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
    cancelDiscovery(): Promise<Boolean>;
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
    unpairDevice(address: string): Promise<Boolean>;
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
    cancelAccept(): Promise<Boolean>;
    /**
     * Request user to turn on Bluetooth Adapter
     *
     * This is an Android only feature.
     *
     * @param state
     */
    requestBluetoothEnabled(): Promise<Boolean>;
    /**
     * Attempts to set the bluetooth adapter name.
     *
     * This is an Android only feature.
     *
     * @param name the name to which we will change BluetoothAdapter
     */
    setBluetoothAdapterName(name: string): Promise<Boolean>;
}
