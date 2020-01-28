
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

    // Set the _nativeModule for Android, which isn't actually done in NativeEventEmitter
    if (Platform.OS === 'android') 
      this._nativeModule = nativeModule;

    this.setAdapterName = nativeModule.setAdapterName;
    this.requestEnable = nativeModule.requestEnable;
    this.isEnabled = nativeModule.isEnabled;
    this.list = nativeModule.list;
    this.discoverDevices = nativeModule.discoverDevices;
    this.cancelDiscovery = nativeModule.cancelDiscovery;
    this.pairDevice = nativeModule.pairDevice;
    this.unpairDevice = nativeModule.unpairDevice;

    this.accept = nativeModule.accept;
    this.cancelAccept = nativeModule.cancelAccept;

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
    let subscriber = super.addListener(eventName, handler, context);
    this.applyReadListeners();
    return subscriber;
  }

  /**
   * Remove all the listeners for an eventName.
   * 
   * @param {string} eventName which will have all it's listeners removed
   */
  removeAllListeners = (eventName) => {
    super.removeAllListeners(eventName);
    this.applyReadListeners();
  }

  /**
   * Remove the subscription - this is actually called from subscription.remove()
   * and provides a way for determining if we have any BTEvents.READ event still.
   * 
   * @param {Subscription} subscription the subscription to be removed
   */
  removeSubscription = (subscription) => {
    super.removeSubscription(subscription);
    this.applyReadListeners();
  }

  /**
   * Determines if there are any READ Listeners.  If there are any listeners, then 
   * the RNBluetoothClassicModule needs to know that it should continue to send data.
   */
  applyReadListeners = () => {
    let count = this.listeners(BTEvents.READ).length;
    this._nativeModule.setReadObserving(0 < count);  
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

export const BTEvents = NativeModules.RNBluetoothClassic.BTEvents;
export const BTCharsets = NativeModules.RNBluetoothClassic.BTCharsets;
export default new RNBluetoothClassic(NativeModules.RNBluetoothClassic);

