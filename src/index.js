
import {
  Platform,
  NativeModules,
  DeviceEventEmitter,
  NativeEventEmitter
} from 'react-native';
import { Buffer } from 'buffer';

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
class RNBluetoothClassic extends NativeEventEmitter {
  constructor(nativeModule) {
    super(nativeModule)

    if (Platform.OS === 'android') this._nativeModule = nativeModule;

    this.setAdapterName = nativeModule.setAdapterName;
    this.requestEnable = nativeModule.requestEnable;
    this.isEnabled = nativeModule.isEnabled;
    this.list = nativeModule.list;
    this.discoverDevices = nativeModule.discoverDevices;
    this.cancelDiscovery = nativeModule.cancelDiscovery;
    this.pairDevice = nativeModule.pairDevice;
    this.unpairDevice = nativeModule.unpairDevice;

    this.connect = nativeModule.connect;
    this.disconnect = nativeModule.disconnect;
    this.isConnected = nativeModule.isConnected;
    this.getConnectedDevice = nativeModule.getConnectedDevice;

    this.writeToDevice = nativeModule.writeToDevice;
    this.available = nativeModule.available;
    this.readFromDevice = nativeModule.readFromDevice;
    this.readUntilDelimiter = nativeModule.readUntilDelimiter;
    this.clear = nativeModule.clear;

    this.setDelimiter = nativeModule.setDelimiter;
    this.setEncoding = nativeModule.setEncoding;
  }

  /**
   * Override the NativeEventEmitter#addListener method providing functionality for 
   * Android.  I felt this was important as I didn't want clients to have to determine
   * which platform to use the event listening features of React.
   * 
   * @param {string} eventName to which the listener will be attached
   * @param {function} handler which will be called on event
   * @param {object} context optional context object of the listener
   */
  addListener = (eventName, handler, context) => {
    return super.addListener(eventName, handler, context);
  }

  /**
   * Remove all the listeners for an eventName.
   * 
   * @param {string} eventName which will have all it's listeners removed
   */
  removeAllListeners = (eventName) => {
    super.removeAllListeners(eventName);
  }

  /**
   * Write data to the device.  Eventually this will be updated to accept data and type, 
   * allowing the sending of different data elements to the device.  From the issues on
   * bluetooth-serial it seems like images and hex values are the top priorties, but method
   * to send any data would be preferable.
   * 
   * @param {string|buffer} data to be sent to the device as base64 string 
   * 
   * TODO modify for byte[] instead of string
   */
  write = (data) => {
    if (typeof data === 'string') {
      data = new Buffer(data);
    }
    this._nativeModule.writeToDevice(data.toString('base64'));
  }
}

/**
 * BTEvents were previously provided by the RNBluetoothClassic getConstants().  They've been
 * moved here to allow for intellisense to pick them up.  This way there is no difference
 * between IOS and Android with regards to the available events (React Native side) as they
 * still need to be managed on the Native side using RCTEventEmitter#supportedEvents()
 */
export const BTEvents = {
  BLUETOOTH_ENABLED: "bluetoothEnabled",
  BLUETOOTH_DISABLED: "bluetoothDisabled",
  BLUETOOTH_CONNECTED: "bluetoothConnected",
  BLUETOOTH_DISCONNECTED: "bluetoothDisconnected",
  CONNECTION_SUCCESS: "connectionSuccess",
  CONNECTION_FAILED: "connectionFailed",
  CONNECTION_LOST: "connectionLost",
  READ: "read",
  ERROR: "error"
};

export default new RNBluetoothClassic(NativeModules.RNBluetoothClassic);
