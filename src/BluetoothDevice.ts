import BluetoothModule from './BluetoothModule';
import BluetoothNativeDevice from './BluetoothNativeDevice';
import {
  BluetoothEventListener,
  BluetoothDeviceReadEvent,
  BluetoothEventSubscription,
} from './BluetoothEvent';
import { StandardOptions } from './BluetoothNativeModule';
import { Buffer } from 'buffer';

/**
 * Implements the BluetoothNativeDevice which is used to communicate with the Android
 * and IOS native module.  Provides access to the BluetoothDevice (Android) and
 * EAAccessory (IOS) details as well as configuration of listeners.
 *
 * @author kendavidson
 */
export default class BluetoothDevice implements BluetoothNativeDevice {
  private _bluetoothModule: BluetoothModule;
  private _nativeDevice: BluetoothNativeDevice;

  name: string;
  address: string;
  id: string;
  bonded?: Boolean;
  deviceClass?: string;
  rssi: Number;
  extra: Map<string, Object>;

  constructor(nativeDevice: BluetoothNativeDevice, bluetoothModule: BluetoothModule) {
    this._bluetoothModule = bluetoothModule;
    this._nativeDevice = nativeDevice;

    this.name = nativeDevice.name;
    this.address = nativeDevice.address;
    this.id = nativeDevice.id;
    this.bonded = nativeDevice.bonded;
    this.deviceClass = nativeDevice.deviceClass;
    this.rssi = nativeDevice.rssi;
    this.extra = nativeDevice.extra;
  }

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
  async connect<T extends StandardOptions>(options?: T): Promise<boolean> {
    return new Promise(async (resolve, reject) => {
      try {
        let connected = await this._bluetoothModule.connectToDevice(this.address, options);
        resolve(!!connected);
      } catch (err) {
        reject(err);
      }
    });
  }

  /**
   * Determine whether the device is currently connected.   Again it's important to note that
   * **connected** means that there is an active BluetoothSocket/Stream available.
   *
   * @return Promise resolving true|false based on connection status
   */
  async isConnected(): Promise<boolean> {
    return this._bluetoothModule.isDeviceConnected(this.address);
  }

  /**
   * Disconnect from the device.
   *
   * @return Promise resolving true|false whether disconnection was successful
   */
  async disconnect(): Promise<boolean> {
    return this._bluetoothModule.disconnectFromDevice(this.address);
  }

  /**
   * How many bytes/messages are available.  This depends completely on the implementation
   * of the DeviceConnection.  The standard implementation is based on delimited String(s)
   * so this will return the number of messages available for reading.
   *
   * @return Promise resolving the number of messages/data available
   */
  async available(): Promise<number> {
    return this._bluetoothModule.availableFromDevice(this.address);
  }

  /**
   * Read an individual message/data package from the device.  This depends completely on the
   * implementation of DeviceConnection.  The standard implemenation is based on delimited
   * String(s) so this will return 1 delimtied message.
   *
   * @return Promise resolved with the message content (not including delimited)
   */
  async read(): Promise<String> {
    return this._bluetoothModule.readFromDevice(this.address);
  }

  /**
   * Clear the current device buffer - this will generally only be required when using
   * manual reads (as `onRead` should continually keep the buffer clean).
   *
   * @return Promise resolving whether the clear was successful
   */
  async clear(): Promise<boolean> {
    return this._bluetoothModule.clearFromDevice(this.address);
  }

  /**
   * Writes the provided data to the device.  This accepts String or Buffer data, if String
   * it will be converted to a Buffer and then Base64 encoded prior to sending to the
   * Native module.
   *
   * @param data to be written to the device.
   * @param encoding the encoding used when wrapping non Buffer data
   */
  async write(
    data: string | Buffer,
    encoding?:
      | 'utf-8'
      | 'ascii'
      | 'utf8'
      | 'utf16le'
      | 'ucs2'
      | 'ucs-2'
      | 'base64'
      | 'latin1'
      | 'binary'
      | 'hex'
      | undefined
  ): Promise<boolean> {
    return this._bluetoothModule.writeToDevice(this.address, data, encoding);
  }

  /**
   * Adds a listener to the device.  Once completed this will:
   * - send queued data already read from the device (if implemented by DeviceConnection)
   * - send all subsequent data
   *
   * @param listener the BluetoothEventListener which will receive incoming data
   */
  onDataReceived(
    listener: BluetoothEventListener<BluetoothDeviceReadEvent>
  ): BluetoothEventSubscription {
    return this._bluetoothModule.onDeviceRead(this.address, listener);
  }
}
