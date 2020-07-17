declare module 'react-native-bluetooth-classic' {
  import {NativeEventEmitter} from 'react-native';

  export interface BTDevice {
    name: string;
    address: string;
    extra: {
      [key: string]: any;
    };
  }

  export enum BTEvents {
    BLUETOOTH_CONNECTED = 'bluetoothConnected',
    BLUETOOTH_DISABLED = 'bluetoothDisabled',
    BLUETOOTH_DISCONNECTED = 'bluetoothDisconnected',
    BLUETOOTH_ENABLED = 'bluetoothEnabled',
    CONNECTION_FAILED = 'connectionFailed',
    CONNECTION_LOST = 'connectionLost',
    CONNECTION_SUCCESS = 'connectionSuccess',
    ERROR = 'error',
    READ = 'read',
  }

  export enum BTCharsets {
    LATIN,
    ASCII,
    UTF8,
    UTF16,
  }

  /**
   * Combines some common functionality between NativeEventEmitter and DeviceEventEmitter for
   * IOS and Android respectively.  The key thing here is that we need to:
   * - Make the Native methods available in JS (check)
   * - Make the add|remove listener functionality available in JS(check)
   * - Make the remove() functionality available in JS (check)
   *
   * I decided to stick with the IOS side as the main way of doing things because I found it
   * more annoying up front and would therefore want to be reminded of in whenever fixes
   * or features are added.
   */
  namespace RNBluetoothClassic {
    export const addListener: NativeEventEmitter['addListener'];
    export const removeAllListeners: NativeEventEmitter['removeAllListeners'];
    export const removeCurrentListener: NativeEventEmitter['removeCurrentListener'];
    export const removeListener: NativeEventEmitter['removeListener'];
    export const removeSubscription: NativeEventEmitter['removeSubscription'];
    export const listeners: NativeEventEmitter['listeners'];
    export const emit: NativeEventEmitter['emit'];
    export const once: NativeEventEmitter['once'];

    /**
     * Sets a new Bluetooth adapter name.
     *
     * **Note:** This functionality is only available on Android as IOS requires
     * that MFi devices are connected to through the system settings.
     */
    function setAdapterName(): Promise<true>;

    /**
     * Requests that the Bluetooth adapter attempt to discover new devices.
     *
     * **Note:** This functionality is only available on Android as IOS requires
     * that MFi devices are connected to through the system settings.
     */
    function requestEnabled(): Promise<boolean>;

    /**
     * Requests whether the Bluetooth adapter is enabled.
     */
    function isEnabled(): Promise<boolean>;

    /**
     * Requests a list of all the devices currently connected (IOS) or paired (Android) with the device.
     */
    function list(): Promise<BTDevice[]>;

    /**
     * Requests that the Bluetooth adapter attempt to discover new devices.
     *
     * **Note:** This functionality is only available on Android as IOS requires
     * that MFi devices are connected to through the system settings.
     */
    function discoverDevices(): Promise<BTDevice[]>;

    /**
     * Requests that the previous/current discovery request is cancelled.
     *
     * **Note:** This functionality is only available on Android as IOS requires
     * that MFi devices are connected to through the system settings.
     */
    function cancelDiscovery(): Promise<true>;

    /**
     * Attempts to unpair with a device identified by the unique device id.
     *
     * **Note:** This functionality is only available on Android as IOS
     * requires that MFi devices are connected to through the system settings
     *
     * @param { string } deviceId  The id of the device to which we will attempt to unpair.
     */
    function unpairDevice(deviceId: string): Promise<boolean>;

    /**
     * Attempts to pair with a device identified by the unique device id.
     *
     * **Note:** This functionality is only available on Android as IOS
     * requires that MFi devices are connected to through the system settings
     *
     * @param { string } deviceId  The id of the device to which we will attempt to pair.
     */
    function pairDevice(deviceId: string): Promise<boolean>;

    /**
     * Attempts to establish a communication connection with a device.
     *
     * @param { string } deviceId  The id of the device to which we will attempt to connect.
     */
    function connect(deviceId: string): Promise<BTDevice>;

    /**
     * Disconnects from the current device
     *
     * **Note:** currently there is only one device connection limit.
     */
    function disconnect(): Promise<void>;

    /**
     * Whether the application is currently connected to a device
     *
     * **Note:** there is only one device limit at the moment
     */
    function isConnected(): Promise<boolean>;

    /**
     * Gets the currently connected device.
     */
    function getConnectedDevice(): Promise<BTDevice>;

    /**
     * Whether there is currently data available on the current device,
     * used in conjunction with the read[until|from] functions.
     */
    function available(): Promise<boolean>;

    /**
     * Resolves with the entire content of the devices buffer, ignoring any delimiters
     * and clearing the buffer when complete.
     *
     * This method should not be used in conjunction with the `BTEvent.READ` event as
     * it could cause some unexpected behaviour.
     */
    function readFromDevice(): Promise<string>;

    /**
     * Resolves with the content of the buffer up until the provided delimiter.
     *
     * If no delimiter is passed as an argument, the default delimiter is used.
     *
     * To update the delimiter for the session use setDelimiter(delimiter:String).
     *
     * This method should not be used in conjunction with the `BTEvent.READ` event
     * as it could cause some unexpected behaviour.
     *
     * @param { string } delimiter The delimiter to use while parsing individual messages
     */
    function readUntilDelimiter(delimiter?: string): Promise<string>;

    /**
     * Sets a new delimiter used for default reading
     *
     * @param { string } delimiter The delimiter to use while parsing individual messages
     */
    function setDelimiter(delimiter: string): Promise<boolean>;

    /**
     * setEncoding accepts the BTCharsets value and configures the Charset to use
     * when parsing byte[] data into String. Internally all data is parsed as
     * Strings, whether this is a good or bad thing, it's the way the original
     * projects were developed and the way this will continue until I have time to
     * allow customization. For that reason encoding plays a very important role,
     * as I found out while while parsing data on IOS (as UTF8) and receiving
     * different data than I was on Android (as ISO_8859_1).
     *
     * @param { BTCharsets } encoding The native Charset or String.Encoding for Android or IOS (respectively).
     */
    function setEncoding(encoding: BTCharsets): Promise<boolean>;

    /**
     * Write data to the device.  Eventually this will be updated to accept data and type,
     * allowing the sending of different data elements to the device.  From the issues on
     * bluetooth-serial it seems like images and hex values are the top priorties, but method
     * to send any data would be preferable.
     *
     * @param {string|buffer} data to be sent to the device as base64 string
     */
    function write(data: string): Promise<boolean>;
  }

  export default RNBluetoothClassic;
}
