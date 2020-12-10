import BluetoothNativeDevice from './BluetoothNativeDevice';
/**
 * kjd.reactnative.RNBluetoothClassicModule
 *
 * Provides direct access to Bluetooth Adapter (Android) and External
 * Accessory (IOS) libraries.
 *
 * @author kenjdavidson
 *
 */
export default interface BluetoothNativeModule {
    /**
     * Requests whether or not Bluetooth is enabled.
     *
     * @return Promise<boolean> resolved based on the current Bluetooth status
     */
    isBluetoothEnabled(): Promise<boolean>;
    /**
     * Retrieves a list of the currently bonded devices.  This was originally
     * called list but caused some confusion.
     *
     * @return Promise resolved with the bonded BluetoothNativeDevice(s)
     */
    getBondedDevices(): Promise<BluetoothNativeDevice[]>;
    /**
     * Retrieves a list of the current connections.
     *
     * @return the connected BluetoothDevice(s)
     */
    getConnectedDevices(): Promise<BluetoothNativeDevice[]>;
    /**
     * Attempts to connect to the provided address.
     *
     * @param address to which the connection will be attempted
     * @param properties for requesting special connecting/connection settings
     * @return Promise resolved with BluetoothDevice which is now connected
     */
    connectToDevice<T extends StandardOptions>(address: string, properties?: T): Promise<BluetoothNativeDevice>;
    /**
     * Attempts to disconnect from the requested address.
     *
     * @param address of which we will try to disconnect
     * @return Promise resolved with connection success
     */
    disconnectFromDevice(address: string): Promise<boolean>;
    /**
     * Determine if the provided address as an established connection.
     *
     * @param address address in which to check for connection
     */
    isDeviceConnected(address: string): Promise<boolean>;
    /**
     * Attempts to get the BluetoothDevice specified by the address.  This should generally
     * be called after isConnected(address:string) unless you're expecting to
     * handle the error.
     *
     * @param address the device address.
     * @return Promise resovled with conneted device or error if not connected
     */
    getConnectedDevice(address: string): Promise<BluetoothNativeDevice>;
    /**
     * Requests whether there is data available for a read.
     *
     * @param address the address from which we will read
     * @return Promise resolved with bytes available for read
     */
    availableFromDevice(address: string): Promise<number>;
    /**
     * Attempts to read from the device.   Will always resolve, whether empty or
     * not.  string response should be parsed/decoded appropriately based on your
     * device and configuration.
     *
     * @param address address from which we will read
     * @return Promise resolved with next available message or data
     */
    readFromDevice(address: string): Promise<string>;
    /**
     * Attempt to clear the devices current buffer.
     *
     * @param address address of the device to be cleared
     * @return Promise resolved whether clear was successful
     */
    clearFromDevice(address: string): Promise<boolean>;
    /**
     * Write the provdied data to the device.
     *
     * @param address address to whic hwe will write
     * @param data string data which will be encoded and written
     * @return Promise resolved whether write was successful
     */
    writeToDevice(address: string, data: string): Promise<boolean>;
    /**
     * Attempts to enable the BluetoothAdapter.
     *
     * This is an Android only function.
     *
     * @return Promise resolved whether bluetooth is enabled
     */
    requestBluetoothEnabled(): Promise<boolean>;
    /**
     * Attempts to rename the BluetoothAdapter.
     *
     * This is an Android only function.
     *
     * @param name to which the BluetoothAdapter will be renamed
     * @return Promise resolved whether adapter name was set
     */
    setBluetoothAdapterName(name: string): Promise<boolean>;
    /**
     * Attempts to accept a connection from a client device.
     *
     * This is an Android only function.
     *
     * @return Promise resolved with the connected device
     */
    accept(properties: Map<string, object>): Promise<BluetoothNativeDevice>;
    /**
     * Cancel the current accept.
     *
     * This is an Android only function.
     *
     * @return Promise resolved whether cancel was successful
     */
    cancelAccept(): Promise<boolean>;
    /**
     * Starts discovery.
     *
     * This is an Android only function.
     *
     * @returns Promise resolved with the newly found devices
     */
    startDiscovery(): Promise<BluetoothNativeDevice[]>;
    /**
     * Cancel discovery.
     *
     * This is an Android only function.
     *
     * @returns Promise resolved whether discover cancelled successfully
     */
    cancelDiscovery(): Promise<boolean>;
    /**
     * Attempt to pair the device.
     *
     * This is an Android only function.
     *
     * @param address address of device we wish to pair
     * @return Promise resolved with paired device
     */
    pairDevice(address: string): Promise<BluetoothNativeDevice>;
    /**
     *
     * @param address Attempts to unpair the device.
     *
     * This is an Android only function.
     *
     * @return Promise resolved on whether device was unpaired
     */
    unpairDevice(address: string): Promise<boolean>;
    /**
     * Informs the RNBluetoothClassic native module about the addition of the
     * requested eventType listener.  This enables the specified eventType messages
     * to be sent from native to the React Native.
     *
     * Event listeners on the native side are solely just counters used to determine
     * whethere there is a point in  sending the event across the bridge.
     *
     * Event with device context should be in the format `EVENT_TYPE@DEIVCE_ADDRESS`
     *
     * @param eventType
     */
    addListener(eventType: string): void;
    /**
     * Removes a single listener count of the specified event type.  Once all listeners
     * are removed, the native side will not send an more events.
     *
     * @param eventType
     */
    removeListener(eventType: string): void;
    /**
     * Clears out all listener counts on the native side for the provided event type.   This
     * may need to be separated into a removeAllListeners with no argument that would
     * remove every listener all together.
     *
     * @param eventType
     */
    removeAllListeners(eventType: string): void;
}
export interface StandardOptions {
    /**
     * Instructs the module on which type of connector to use to
     * initiate a connection.  The default for this is rfcomm which
     * uses the connetor RfcommConnectorThreadImpl.
     *
     * Also accepts connector_type and CONNETOR_TYPE.
     */
    connectorType?: string;
    /**
     * Instructs the module on which type of acceptor to use while
     * waiting for a device connection.  The default for this is rfcomm
     * which uses the acceptor RfcommAcceptorThreadImpl.
     *
     * Also accepts acceptor_type and ACCEPTOR_TYPE/
     */
    acceptorType?: string;
    /**
     * Instructs the module on what type of connection will be
     * used during connection.  The default for this delimited, which
     * uses the connection type DelimitedStringDeviceConnectionImpl.
     *
     * Also accepts connection_type and CONNECTION_TYPE.
     */
    connectionType?: string;
    /**
     * Sets the delimiter used for parsing messages.  The default is
     * '\n'.
     *
     * Also accepts DELIMITER
     */
    delimiter?: string;
    /**
     * Sets the appropriate character set for communication.  The default
     * is ascii for both Android and IOS.  When setting this you must use
     * the string representation of Android Charset and the integer value
     * supporting IOS (which I forget what it is, but it's in the
     * documentation so check that out).
     *
     * Also accepts DEVICE_CHARSET and device_charset.
     */
    charset?: string | number;
    /**
     * This is a hold over from the original library (defaulted to 300), as
     * of now it's defaulted to 0 and ignored.  Since reading (on Android)
     * is already a blocking method, having an added timeout was just
     * causing too much slowdown.
     *
     * This is ignored on IOS.
     */
    readTimeout?: number;
    /**
     * Configures the read buffer size, this defaults to 1024.  Increasing
     * this will increase your throughput while working with streaming
     * connections.
     *
     * This is ignored on IOS.
     *
     * Also accepts READ_SIZE and read_size
     */
    readSize?: number;
    /**
     * Whether or not the connector/acceptor should be created using
     * an insecure or secure socket.  Defaults to true.
     *
     * Also accepts SECURE_SOCKET and secure_socket.
     */
    secureSocket?: boolean;
    /**
     * Provide a service name while accepting connections.  This name
     * will be display to discovering devices, defaults to
     * RNBluetoothClassic.
     *
     * This is ignored on IOS.
     *
     * Also accepts SERVICE_NAME and service_name.
     */
    serviceName?: string;
}
