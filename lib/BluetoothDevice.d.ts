import BluetoothModule from "./BluetoothModule";
import BluetoothNativeDevice from "./BluetoothNativeDevice";
import { BluetoothEventListener, BluetoothDeviceReadEvent, BluetoothEventSubscription } from "./BluetoothEvent";
import { StandardOptions } from "./BluetoothNativeModule";
/**
 * Implements the BluetoothNativeDevice which is used to communicate with the Android
 * and IOS native module.  Provides access to the BluetoothDevice (Android) and
 * EAAccessory (IOS) details as well as configuration of listeners.
 *
 * @author kendavidson
 */
export default class BluetoothDevice implements BluetoothNativeDevice {
    private _bluetoothModule;
    private _nativeDevice;
    name: string;
    address: string;
    id: string;
    bonded?: Boolean;
    deviceClass?: string;
    rssi: Number;
    extra: Map<string, Object>;
    constructor(nativeDevice: BluetoothNativeDevice, bluetoothModule: BluetoothModule);
    /**
     * Attempts to open a BluetoothSocket (Android) or Stream (IOS) with the device.  When this
     * is completed successfully the device is said to be **connected**, otherwise the device
     * is referred to as **bonded**
     *
     * @param options 	used to perform connetion and communication.  This is currently a generic
     * 					map based on the native implementation of the RNBluetoothClassic module,
     * 					DeviceConnector and DeviceConnection.
     * @return Promise resolving true|false whether the connetion was established
     */
    connect<T extends StandardOptions>(options?: T): Promise<boolean>;
    /**
     * Determine whether the device is currently connected.   Again it's important to note that
     * **connected** means that there is an active BluetoothSocket/Stream available.
     *
     * @return Promise resolving true|false based on connection status
     */
    isConnected(): Promise<boolean>;
    /**
     * Disconnect from the device.
     *
     * @return Promise resolving true|false whether disconnection was successful
     */
    disconnect(): Promise<boolean>;
    /**
     * How many bytes/messages are available.  This depends completely on the implementation
     * of the DeviceConnection.  The standard implementation is based on delimited String(s)
     * so this will return the number of messages available for reading.
     *
     * @return Promise resolving the number of messages/data available
     */
    available(): Promise<number>;
    /**
     * Read an individual message/data package from the device.  This depends completely on the
     * implementation of DeviceConnection.  The standard implemenation is based on delimited
     * String(s) so this will return 1 delimtied message.
     *
     * @return Promise resolved with the message content (not including delimited)
     */
    read(): Promise<String>;
    /**
     * Writes the provided data to the device.  This accepts String or Buffer data, if String
     * it will be converted to a Buffer and then Base64 encoded prior to sending to the
     * Native module.
     *
     * @param data to be written to the device.
     */
    write(data: any): Promise<boolean>;
    /**
     * Adds a listener to the device.  Once completed this will:
     * - send queued data already read from the device (if implemented by DeviceConnection)
     * - send all subsequent data
     *
     * @param listener the BluetoothEventListener which will receive incoming data
     */
    onDataReceived(listener: BluetoothEventListener<BluetoothDeviceReadEvent>): BluetoothEventSubscription;
}
